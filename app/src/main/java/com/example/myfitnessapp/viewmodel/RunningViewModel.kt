package com.example.myfitnessapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfitnessapp.data.AppDatabase
import com.example.myfitnessapp.data.model.DailySummary
import com.example.myfitnessapp.data.model.WorkoutSession
import com.example.myfitnessapp.sensors.GyroscopeMovementType
import com.example.myfitnessapp.sensors.SensorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.sqrt

class RunningViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val dao = database.workoutDao()
    private val sensorHandler = SensorHandler(application)


    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _calories = MutableStateFlow(0f)
    val calories: StateFlow<Float> = _calories.asStateFlow()

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh: StateFlow<Float> = _speedKmh.asStateFlow()

    private val _avgSpeedKmh = MutableStateFlow(0f)
    val avgSpeedKmh: StateFlow<Float> = _avgSpeedKmh.asStateFlow()


    private val _accelerationData = MutableStateFlow<List<Float>>(emptyList())
    val accelerationData: StateFlow<List<Float>> = _accelerationData.asStateFlow()

    private val _currentAcceleration = MutableStateFlow(9.8f)
    val currentAcceleration: StateFlow<Float> = _currentAcceleration.asStateFlow()


    private val _lightLevel = MutableStateFlow(0f)
    val lightLevel: StateFlow<Float> = _lightLevel.asStateFlow()

    private val _lightHistory = MutableStateFlow<List<Float>>(emptyList())
    val lightHistory: StateFlow<List<Float>> = _lightHistory.asStateFlow()


    private val _isBrightLight = MutableStateFlow(false)
    val isBrightLight: StateFlow<Boolean> = _isBrightLight.asStateFlow()


    private val _gyroscopeData = MutableStateFlow(Triple(0f, 0f, 0f))
    val gyroscopeData: StateFlow<Triple<Float, Float, Float>> = _gyroscopeData.asStateFlow()

    private val _gyroscopeMagnitudeHistory = MutableStateFlow<List<Float>>(emptyList())
    val gyroscopeMagnitudeHistory: StateFlow<List<Float>> = _gyroscopeMagnitudeHistory.asStateFlow()


    private val _movementType = MutableStateFlow(GyroscopeMovementType.STILL)
    val movementType: StateFlow<GyroscopeMovementType> = _movementType.asStateFlow()


    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _sessionTimeSeconds = MutableStateFlow(0L)
    val sessionTimeSeconds: StateFlow<Long> = _sessionTimeSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartSteps = 0
    private var stepsWindow = mutableListOf<Long>()

    val hasAccelerometer: Boolean get() = sensorHandler.hasAccelerometer
    val hasLightSensor: Boolean   get() = sensorHandler.hasLightSensor
    val hasGyroscope: Boolean     get() = sensorHandler.hasGyroscope


    val recentSessions: StateFlow<List<WorkoutSession>> = dao.getRecentSessions(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalHistoricalSteps: StateFlow<Int> = dao.getTotalSteps()
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalHistoricalDistance: StateFlow<Float> = dao.getTotalDistance()
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalHistoricalCalories: StateFlow<Float> = dao.getTotalCalories()
        .map { it ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val sessionCount: StateFlow<Int> = dao.getSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    private val _weeklySteps = MutableStateFlow<List<Int>>(listOf(0, 0, 0, 0, 0, 0, 0))
    val weeklySteps: StateFlow<List<Int>> = _weeklySteps.asStateFlow()


    data class ActivityZone(val name: String, val minutes: Int, val color: Long)

    private val _activityZones = MutableStateFlow<List<ActivityZone>>(
        listOf(
            ActivityZone("Calentamiento",  5,  0xFF69F0AE),
            ActivityZone("Ritmo Moderado", 10, 0xFFFF6B35),
            ActivityZone("Alta Intensidad", 4, 0xFFFF4081),
            ActivityZone("Recuperacion",    3, 0xFF7C4DFF)
        )
    )
    val activityZones: StateFlow<List<ActivityZone>> = _activityZones.asStateFlow()

    private val _stepsPerMinuteHistory = MutableStateFlow<List<Int>>(emptyList())
    val stepsPerMinuteHistory: StateFlow<List<Int>> = _stepsPerMinuteHistory.asStateFlow()

    private var stepsAtLastMinute = 0

    // ─────────────────────────────────────────────────────────────────────────
    init {
        loadWeeklyDataFromRoom()


        sensorHandler.startListening()

        sensorHandler.onStepDetected = {
            if (_isTracking.value) {
                _steps.value += 1
                val currentSteps = _steps.value
                val dist = currentSteps * 0.00076f
                _distance.value = dist
                _calories.value = currentSteps * 0.04f

                val now = System.currentTimeMillis()
                synchronized(stepsWindow) {
                    stepsWindow.add(now)
                    stepsWindow.removeAll { now - it > 4000 }
                    val stepsInWindow = stepsWindow.size
                    val kmInWindow = stepsInWindow * 0.00076f
                    val hoursInWindow = 4f / 3600f
                    val instantSpeed = (kmInWindow / hoursInWindow).coerceAtMost(35f)
                    _speedKmh.value = instantSpeed
                }

                val currentSec = _sessionTimeSeconds.value
                if (currentSec > 2) {
                    val hours = currentSec / 3600f
                    _avgSpeedKmh.value = (dist / hours).coerceAtMost(35f)
                }
            }
        }

        sensorHandler.onAccelerationChanged = { accel ->
            _currentAcceleration.value = accel
            val currentList = _accelerationData.value.toMutableList()
            currentList.add(accel)
            if (currentList.size > 100) currentList.removeAt(0)
            _accelerationData.value = currentList
        }

        sensorHandler.onLightChanged = { lux ->
            _lightLevel.value = lux
            val currentList = _lightHistory.value.toMutableList()
            currentList.add(lux)
            if (currentList.size > 50) currentList.removeAt(0)
            _lightHistory.value = currentList
        }

        sensorHandler.onBrightLightDetected = { bright ->
            _isBrightLight.value = bright
        }

        sensorHandler.onGyroscopeChanged = { gx, gy, gz ->
            _gyroscopeData.value = Triple(gx, gy, gz)
            val magnitude = sqrt((gx * gx + gy * gy + gz * gz).toDouble()).toFloat()
            val currentList = _gyroscopeMagnitudeHistory.value.toMutableList()
            currentList.add(magnitude)
            if (currentList.size > 100) currentList.removeAt(0)
            _gyroscopeMagnitudeHistory.value = currentList
        }

        sensorHandler.onMovementTypeChanged = { type ->
            _movementType.value = type
        }
    }


    private fun loadWeeklyDataFromRoom() {
        viewModelScope.launch {
            dao.getWeeklySummaries().collect { summaries ->
                if (summaries.isNotEmpty()) {
                    val list = summaries.map { it.steps }
                    val padded = if (list.size < 7) {
                        List(7 - list.size) { 0 } + list
                    } else list.takeLast(7)
                    _weeklySteps.value = padded
                } else {
                    seedInitialDataIfEmpty()
                }
            }
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getDailySummary(getTodayKey())
        if (existing == null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sampleSteps = listOf(3200, 4800, 4100, 5600, 3900, 6200, 0)

            for (i in 0 until 7) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -(6 - i))
                val dateStr = sdf.format(cal.time)
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                val dayName = when (dayOfWeek) {
                    Calendar.MONDAY    -> "Lun"
                    Calendar.TUESDAY   -> "Mar"
                    Calendar.WEDNESDAY -> "Mie"
                    Calendar.THURSDAY  -> "Jue"
                    Calendar.FRIDAY    -> "Vie"
                    Calendar.SATURDAY  -> "Sab"
                    else               -> "Dom"
                }
                val st = sampleSteps[i]
                dao.insertOrUpdateDailySummary(
                    DailySummary(
                        dateKey = dateStr,
                        dayLabel = dayName,
                        steps = st,
                        distanceKm = st * 0.00076f,
                        calories = st * 0.04f,
                        activeSeconds = (st * 0.6f).toLong()
                    )
                )
            }
        }
    }


    fun startTracking() {
        if (_isTracking.value) return
        _isTracking.value = true
        sessionStartSteps = _steps.value
        stepsAtLastMinute = _steps.value

        timerJob = viewModelScope.launch {
            while (_isTracking.value) {
                delay(1000)
                _sessionTimeSeconds.value += 1

                val now = System.currentTimeMillis()
                synchronized(stepsWindow) {
                    stepsWindow.removeAll { now - it > 4000 }
                    if (stepsWindow.isEmpty()) {
                        _speedKmh.value = max(0f, _speedKmh.value * 0.8f)
                    }
                }

                if (_sessionTimeSeconds.value % 60 == 0L) {
                    val spmList = _stepsPerMinuteHistory.value.toMutableList()
                    val newStepsInMin = _steps.value - stepsAtLastMinute
                    spmList.add(newStepsInMin)
                    if (spmList.size > 60) spmList.removeAt(0)
                    _stepsPerMinuteHistory.value = spmList
                    stepsAtLastMinute = _steps.value

                    updateActivityZones()
                    syncTodayToRoom()
                }
            }
        }
    }

    fun stopTracking() {
        if (!_isTracking.value) return
        _isTracking.value = false
        timerJob?.cancel()

        saveSessionToRoom()
    }

    private fun saveSessionToRoom() {
        val sessionSteps = _steps.value - sessionStartSteps
        val sessionSec = _sessionTimeSeconds.value
        if (sessionSteps > 0 || sessionSec >= 10) {
            val dist = sessionSteps * 0.00076f
            val cal = sessionSteps * 0.04f
            val speed = if (sessionSec > 0) (dist / (sessionSec / 3600f)) else 0f
            val impact = if (_accelerationData.value.isNotEmpty())
                _accelerationData.value.average().toFloat() else 9.8f

            val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
            val session = WorkoutSession(
                dateFormatted = dateStr,
                durationSeconds = sessionSec,
                steps = sessionSteps,
                distanceKm = dist,
                calories = cal,
                avgSpeedKmh = speed,
                avgImpact = impact
            )
            viewModelScope.launch {
                dao.insertSession(session)
                syncTodayToRoom()
            }
        }
    }

    private fun syncTodayToRoom() {
        viewModelScope.launch {
            val todayKey = getTodayKey()
            val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(Date())
            val existing = dao.getDailySummary(todayKey)
            val totalTodaySteps = (existing?.steps ?: 0) + _steps.value
            dao.insertOrUpdateDailySummary(
                DailySummary(
                    dateKey = todayKey,
                    dayLabel = dayName,
                    steps = totalTodaySteps,
                    distanceKm = totalTodaySteps * 0.00076f,
                    calories = totalTodaySteps * 0.04f,
                    activeSeconds = (existing?.activeSeconds ?: 0) + _sessionTimeSeconds.value
                )
            )
        }
    }

    fun resetSession() {
        stopTracking()
        _steps.value = 0
        _distance.value = 0f
        _calories.value = 0f
        _speedKmh.value = 0f
        _avgSpeedKmh.value = 0f
        _sessionTimeSeconds.value = 0L
        _accelerationData.value = emptyList()
        _lightHistory.value = emptyList()
        _gyroscopeMagnitudeHistory.value = emptyList()
        _stepsPerMinuteHistory.value = emptyList()
        sessionStartSteps = 0
        stepsAtLastMinute = 0
        synchronized(stepsWindow) { stepsWindow.clear() }
    }

    private fun updateActivityZones() {
        val spmHistory = _stepsPerMinuteHistory.value
        if (spmHistory.isEmpty()) return

        var warmup = 0; var moderate = 0; var high = 0; var recovery = 0
        spmHistory.forEach { spm ->
            when {
                spm < 50  -> recovery++
                spm < 90  -> warmup++
                spm < 130 -> moderate++
                else       -> high++
            }
        }
        _activityZones.value = listOf(
            ActivityZone("Calentamiento",   warmup.coerceAtLeast(1),   0xFF69F0AE),
            ActivityZone("Ritmo Moderado",  moderate.coerceAtLeast(1), 0xFFFF6B35),
            ActivityZone("Alta Intensidad", high.coerceAtLeast(1),     0xFFFF4081),
            ActivityZone("Recuperacion",    recovery.coerceAtLeast(1), 0xFF7C4DFF)
        )
    }

    private fun getTodayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) String.format("%d:%02d:%02d", h, m, s)
        else String.format("%02d:%02d", m, s)
    }

    override fun onCleared() {
        super.onCleared()
        sensorHandler.stopListening()
    }
}