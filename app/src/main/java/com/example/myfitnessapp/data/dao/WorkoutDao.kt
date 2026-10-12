package com.example.myfitnessapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myfitnessapp.data.model.DailySummary
import com.example.myfitnessapp.data.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: WorkoutSession): Long

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<WorkoutSession>>

    @Query("SELECT COUNT(*) FROM workout_sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT SUM(steps) FROM workout_sessions")
    fun getTotalSteps(): Flow<Int?>

    @Query("SELECT SUM(distanceKm) FROM workout_sessions")
    fun getTotalDistance(): Flow<Float?>

    @Query("SELECT SUM(calories) FROM workout_sessions")
    fun getTotalCalories(): Flow<Float?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailySummary(summary: DailySummary): Long

    @Query("SELECT * FROM daily_summaries WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailySummary(dateKey: String): DailySummary?

    @Query("SELECT * FROM daily_summaries ORDER BY dateKey ASC LIMIT 7")
    fun getWeeklySummaries(): Flow<List<DailySummary>>

    @Query("DELETE FROM workout_sessions")
    suspend fun clearAllSessions(): Int
}
