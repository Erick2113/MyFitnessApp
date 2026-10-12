package com.example.myfitnessapp.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.ScreenRotation
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myfitnessapp.ui.components.RealTimeChart
import com.example.myfitnessapp.ui.theme.*
import com.example.myfitnessapp.viewmodel.RunningViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(viewModel: RunningViewModel) {
    val steps by viewModel.steps.collectAsState()
    val accelerationData by viewModel.accelerationData.collectAsState()
    val lightLevel by viewModel.lightLevel.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val sessionTime by viewModel.sessionTimeSeconds.collectAsState()
    val speedKmh by viewModel.speedKmh.collectAsState()
    val currentAccel by viewModel.currentAcceleration.collectAsState()
    val gyroData by viewModel.gyroscopeData.collectAsState()
    val movementType by viewModel.movementType.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkCard,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mi Entrenamiento",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isTracking) GreenAccent else RedAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTracking) "Sesion activa - ${viewModel.formatTime(sessionTime)}"
                                else "Sesion pausada",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isTracking) GreenAccent else TextOnDarkTertiary
                            )
                        }
                    }


                    FilledIconButton(
                        onClick = {
                            if (isTracking) viewModel.stopTracking()
                            else viewModel.startTracking()
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isTracking) RedAccent.copy(alpha = 0.2f)
                            else CyanPrimary.copy(alpha = 0.2f)
                        )
                    ) {
                        Icon(
                            imageVector = if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isTracking) "Pausar sesion" else "Iniciar sesion",
                            tint = if (isTracking) RedAccent else CyanPrimary
                        )
                    }
                }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            StepProgressRing(
                steps = steps,
                goal = 6000,
                modifier = Modifier.size(180.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlowMetricCard(
                title = "Distancia",
                value = String.format("%.2f", distance),
                unit = "km",
                icon = Icons.Outlined.Route,
                gradientColors = listOf(GradientPurpleStart, GradientPurpleEnd),
                modifier = Modifier.weight(1f)
            )
            GlowMetricCard(
                title = "Calorias",
                value = String.format("%.0f", calories),
                unit = "kcal",
                icon = Icons.Outlined.LocalFireDepartment,
                gradientColors = listOf(GradientOrangeStart, GradientOrangeEnd),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlowMetricCard(
                title = "Velocidad",
                value = String.format("%.1f", speedKmh),
                unit = "km/h",
                icon = Icons.Outlined.Speed,
                gradientColors = listOf(GradientGreenStart, GradientGreenEnd),
                modifier = Modifier.weight(1f)
            )
            GlowMetricCard(
                title = "Tiempo",
                value = viewModel.formatTime(sessionTime),
                unit = "",
                icon = Icons.Outlined.Timer,
                gradientColors = listOf(GradientCyanStart, GradientCyanEnd),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Aceleracion en Tiempo Real",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Captura continua de movimiento",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnDarkTertiary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccelerationColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${String.format("%.1f", currentAccel)} m/s²",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AccelerationColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                RealTimeChart(
                    data = accelerationData,
                    lineColors = listOf(AccelerationColor, PurpleAccent),
                    fillColors = listOf(AccelerationColor.copy(alpha = 0.3f), Color.Transparent)
                )
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sensores en Operacion",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensorMiniCard(
                name = "Luz Ambiental",
                value = "${lightLevel.toInt()} lux",
                icon = Icons.Outlined.LightMode,
                color = LightSensorColor,
                available = viewModel.hasLightSensor,
                modifier = Modifier.weight(1f)
            )
            SensorMiniCard(
                name = "Movimiento",
                value = movementType.label,
                icon = Icons.Outlined.ScreenRotation,
                color = GyroscopeColor,
                available = viewModel.hasGyroscope,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StepProgressRing(
    steps: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val progress = (steps.toFloat() / goal).coerceIn(0f, 1f)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(progress, animationSpec = tween(1000))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val strokeW = 14.dp.toPx()
                    val radius = (size.minDimension - strokeW) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    onDrawBehind {
                        drawCircle(
                            color = Color(0x15FFFFFF),
                            radius = radius,
                            center = center,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        val sweepAngle = 360f * animatedProgress.value
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(CyanPrimary, PurpleAccent, CyanPrimary),
                                center = center
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round),
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                        )

                        if (animatedProgress.value > 0.01f) {
                            val endAngle = Math.toRadians((-90.0 + sweepAngle))
                            val dotX = center.x + radius * cos(endAngle).toFloat()
                            val dotY = center.y + radius * sin(endAngle).toFloat()

                            drawCircle(
                                color = CyanPrimary,
                                radius = strokeW / 2f + 3.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                        }
                    }
                }
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$steps",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "/ $goal pasos",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnDarkTertiary
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CyanPrimary
            )
        }
    }
}

@Composable
private fun GlowMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            gradientColors.first().copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 300f
                    )
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextOnDarkSecondary
                    )
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = gradientColors.first(),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.bodySmall,
                            color = gradientColors.first(),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorMiniCard(
    name: String,
    value: String,
    icon: ImageVector,
    color: Color,
    available: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(72.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOnDarkTertiary
                )
                Text(
                    text = if (available) value else "No disponible",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (available) Color.White else RedAccent,
                    maxLines = 1
                )
            }
        }
    }
}