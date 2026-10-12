package com.example.myfitnessapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myfitnessapp.sensors.GyroscopeMovementType
import com.example.myfitnessapp.ui.components.RealTimeChart
import com.example.myfitnessapp.ui.theme.*
import com.example.myfitnessapp.viewmodel.RunningViewModel
import kotlin.math.sqrt

@Composable
fun SensorsScreen(viewModel: RunningViewModel) {
    val accelerationData by viewModel.accelerationData.collectAsState()
    val currentAccel     by viewModel.currentAcceleration.collectAsState()
    val lightLevel       by viewModel.lightLevel.collectAsState()
    val lightHistory     by viewModel.lightHistory.collectAsState()
    val gyroData         by viewModel.gyroscopeData.collectAsState()
    val gyroHistory      by viewModel.gyroscopeMagnitudeHistory.collectAsState()
    val movementType     by viewModel.movementType.collectAsState()
    val isBrightLight    by viewModel.isBrightLight.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Cabecera
        Text(
            text = "Sensores del Dispositivo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Lecturas en tiempo real de los componentes de hardware",
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnDarkTertiary
        )


        if (isBrightLight) {
            Spacer(modifier = Modifier.height(12.dp))
            val pulseAlpha by rememberInfiniteTransition(label = "brightness_pulse").animateFloat(
                initialValue = 0.7f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                label = "brightness_pulse_alpha"
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AmberAccent.copy(alpha = 0.18f * pulseAlpha),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Brillo al maximo — Luz exterior intensa detectada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                        Text(
                            "La pantalla se ajusto automaticamente para facilitar la lectura",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextOnDarkSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        SensorDetailCard(
            title = "Acelerometro",
            icon = Icons.Outlined.Speed,
            color = AccelerationColor,
            available = viewModel.hasAccelerometer
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LiveValueChip(
                        label = "Magnitud Total",
                        value = "${String.format("%.2f", currentAccel)} m/s²",
                        color = AccelerationColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Registro de aceleracion:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextOnDarkTertiary,
                    modifier = Modifier.padding(start = 4.dp)
                )

                RealTimeChart(
                    data = accelerationData,
                    lineColors = listOf(AccelerationColor, CyanPrimary),
                    fillColors = listOf(AccelerationColor.copy(alpha = 0.25f), Color.Transparent),
                    maxValue = 25f,
                    minValue = 5f,
                    yAxisLabel = "m/s²"
                )

                Text(
                    text = "Funcion principal: Deteccion dinamica de pasos y fuerza de impacto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        SensorDetailCard(
            title = "Sensor de Luz Ambiental",
            icon = Icons.Outlined.LightMode,
            color = LightSensorColor,
            available = viewModel.hasLightSensor
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val (conditionLabel, conditionColor) = when {
                        lightLevel >= 8000  -> "Luz solar directa" to RedAccent
                        lightLevel >= 1000  -> "Exterior brillante" to OrangeAccent
                        lightLevel >= 200   -> "Interior iluminado" to AmberAccent
                        lightLevel >= 50    -> "Interior tenue"     to LightSensorColor
                        else                -> "Oscuridad"          to PurpleAccent
                    }
                    LiveValueChip("Intensidad",  "${lightLevel.toInt()} lux", LightSensorColor)
                    LiveValueChip("Condicion",   conditionLabel,              conditionColor)
                }

                Spacer(modifier = Modifier.height(8.dp))


                val brightProgress = (lightLevel / 12000f).coerceIn(0f, 1f)
                LightBar(progress = brightProgress, isOutdoor = isBrightLight)

                Spacer(modifier = Modifier.height(8.dp))

                if (lightHistory.isNotEmpty()) {
                    val normalizedLight = lightHistory.map { it.coerceAtMost(3000f) }
                    RealTimeChart(
                        data = normalizedLight,
                        lineColors = listOf(LightSensorColor, OrangeAccent),
                        fillColors = listOf(LightSensorColor.copy(alpha = 0.2f), Color.Transparent),
                        maxDataPoints = 50,
                        maxValue = 3000f,
                        minValue = 0f,
                        showLabels = true,
                        yAxisLabel = "lux"
                    )
                }

                Text(
                    text = "Funcion principal: Control automatico de brillo cuando se supera el umbral exterior (8000 lux).",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        SensorDetailCard(
            title = "Giroscopio",
            icon = Icons.Outlined.ScreenRotation,
            color = GyroscopeColor,
            available = viewModel.hasGyroscope
        ) {
            Column {

                GyroscopeMovementCard(movementType = movementType)

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LiveValueChip("Eje X", "${String.format("%.2f", gyroData.first)} rad/s",  GyroscopeColor)
                    LiveValueChip("Eje Y", "${String.format("%.2f", gyroData.second)} rad/s", GreenDark)
                    LiveValueChip("Eje Z", "${String.format("%.2f", gyroData.third)} rad/s",  GreenAccent)
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (gyroHistory.isNotEmpty()) {
                    Text(
                        "Velocidad angular total:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextOnDarkTertiary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    RealTimeChart(
                        data = gyroHistory,
                        lineColors = listOf(GyroscopeColor, GreenDark),
                        fillColors = listOf(GyroscopeColor.copy(alpha = 0.2f), Color.Transparent),
                        maxDataPoints = 100,
                        maxValue = 5f,
                        minValue = 0f,
                        showLabels = true,
                        yAxisLabel = "rad/s"
                    )
                }

                Text(
                    text = "Funcion principal: Clasificacion del tipo de movimiento (reposo, caminata, carrera, giro) en tiempo real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


@Composable
private fun GyroscopeMovementCard(movementType: GyroscopeMovementType) {
    val (cardColor, cardIcon) = when (movementType) {
        GyroscopeMovementType.STILL    -> GreenAccent  to Icons.Outlined.HourglassEmpty
        GyroscopeMovementType.WALKING  -> AmberAccent  to Icons.Outlined.DirectionsWalk
        GyroscopeMovementType.RUNNING  -> OrangeAccent to Icons.Outlined.DirectionsRun
        GyroscopeMovementType.ROTATING -> PinkAccent   to Icons.Outlined.Rotate90DegreesCcw
    }

    val animatedColor by animateColorAsState(
        targetValue = cardColor,
        animationSpec = tween(500),
        label = "gyroColorAnim"
    )

    val pulseScale by rememberInfiniteTransition(label = "gyro_pulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "gyro_scale"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = animatedColor.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = animatedColor.copy(alpha = 0.2f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = cardIcon,
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Column {
                Text(
                    text = movementType.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = animatedColor
                )
                Text(
                    text = movementType.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary
                )
            }
        }
    }
}


@Composable
private fun LightBar(progress: Float, isOutdoor: Boolean) {
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "lightBarAnim"
    )
    val barColor = if (isOutdoor) AmberAccent else LightSensorColor

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Oscuro", style = MaterialTheme.typography.labelSmall, color = TextOnDarkTertiary)
            Text("Interior", style = MaterialTheme.typography.labelSmall, color = TextOnDarkTertiary)
            Text("Exterior", style = MaterialTheme.typography.labelSmall, color = TextOnDarkTertiary)
            Text("Sol", style = MaterialTheme.typography.labelSmall, color = AmberAccent)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color(0xFF1A1F38))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animProgress.coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(PurpleAccent, LightSensorColor, OrangeAccent, AmberAccent, RedAccent)
                        )
                    )
            )
        }
    }
}


@Composable
private fun SensorDetailCard(
    title: String,
    icon: ImageVector,
    color: Color,
    available: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (available) GreenAccent.copy(alpha = 0.15f)
                    else RedAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (available) "Disponible" else "No disponible",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (available) GreenAccent else RedAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (available) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun LiveValueChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
