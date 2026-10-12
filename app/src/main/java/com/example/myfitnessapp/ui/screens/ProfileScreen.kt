package com.example.myfitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import com.example.myfitnessapp.data.model.WorkoutSession
import com.example.myfitnessapp.ui.theme.*
import com.example.myfitnessapp.viewmodel.RunningViewModel

@Composable
fun ProfileScreen(viewModel: RunningViewModel) {
    val steps by viewModel.steps.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val sessionTime by viewModel.sessionTimeSeconds.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    val speedKmh by viewModel.speedKmh.collectAsState()


    val recentSessions by viewModel.recentSessions.collectAsState()
    val totalSteps by viewModel.totalHistoricalSteps.collectAsState()
    val totalDistance by viewModel.totalHistoricalDistance.collectAsState()
    val totalCalories by viewModel.totalHistoricalCalories.collectAsState()
    val sessionCount by viewModel.sessionCount.collectAsState()

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
                            PurpleAccent.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(90.dp),
                    color = CyanPrimary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Avatar de usuario",
                            modifier = Modifier.size(46.dp),
                            tint = CyanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Ruta y Pulso",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextOnDarkTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Ahuachapan, El Salvador",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnDarkTertiary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isTracking) GreenAccent.copy(alpha = 0.15f)
                    else TextOnDarkTertiary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isTracking) "En actividad" else "En pausa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isTracking) GreenAccent else TextOnDarkTertiary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Sesion en Curso",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(
                        value = "$steps",
                        label = "Pasos",
                        icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
                        color = StepsColor
                    )
                    ProfileStatItem(
                        value = String.format("%.2f", distance),
                        label = "km",
                        icon = Icons.Outlined.Route,
                        color = DistanceColor
                    )
                    ProfileStatItem(
                        value = String.format("%.1f", speedKmh),
                        label = "km/h",
                        icon = Icons.Outlined.Speed,
                        color = GreenAccent
                    )
                    ProfileStatItem(
                        value = String.format("%.0f", calories),
                        label = "kcal",
                        icon = Icons.Outlined.LocalFireDepartment,
                        color = CaloriesColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            val dailyGoalKm = 5.0f
            val goalProgress = (distance / dailyGoalKm).coerceIn(0f, 1f)

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = OrangeAccent.copy(alpha = 0.15f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.DirectionsRun,
                            contentDescription = null,
                            tint = OrangeAccent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Objetivo Diario de Distancia",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${String.format("%.2f", distance)} km de ${String.format("%.1f", dailyGoalKm)} km completados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextOnDarkSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = OrangeAccent,
                        trackColor = Color(0x1AFFFFFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(goalProgress * 100).toInt()}% alcanzado",
                        style = MaterialTheme.typography.labelSmall,
                        color = OrangeAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Totales Registrados",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                ProfileInfoRow(
                    icon = Icons.Outlined.FitnessCenter,
                    label = "Sesiones guardadas",
                    value = "$sessionCount sesiones",
                    color = CyanPrimary
                )
                ProfileInfoRow(
                    icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
                    label = "Pasos acumulados",
                    value = "$totalSteps pasos",
                    color = GreenAccent
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.Route,
                    label = "Distancia acumulada",
                    value = "${String.format("%.2f", totalDistance)} km",
                    color = OrangeAccent
                )
                ProfileInfoRow(
                    icon = Icons.Outlined.LocalFireDepartment,
                    label = "Calorias acumuladas",
                    value = "${String.format("%.0f", totalCalories)} kcal",
                    color = PinkAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Historial de Entrenamientos",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (recentSessions.isEmpty()) {
                    Text(
                        text = "Aun no hay sesiones guardadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextOnDarkTertiary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    recentSessions.forEach { session ->
                        SessionHistoryItem(session = session)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Sensores Utilizados",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))

                SensorStatusRow("Acelerometro", viewModel.hasAccelerometer)
                SensorStatusRow("Sensor de Luz Ambiental", viewModel.hasLightSensor)
                SensorStatusRow("Giroscopio", viewModel.hasGyroscope)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boton de reinicio
        Button(
            onClick = { viewModel.resetSession() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RedAccent.copy(alpha = 0.15f),
                contentColor = RedAccent
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Reiniciar Sesion",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SessionHistoryItem(session: WorkoutSession) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCardVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = session.dateFormatted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${session.steps} pasos - ${String.format("%.2f", session.distanceKm)} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.1f", session.avgSpeedKmh)} km/h",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = GreenAccent
                )
                Text(
                    text = "${String.format("%.0f", session.calories)} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = CaloriesColor
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextOnDarkTertiary
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnDarkSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun SensorStatusRow(name: String, available: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (available) GreenAccent else RedAccent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnDarkSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (available) "Disponible" else "No disponible",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (available) GreenAccent else RedAccent
        )
    }
}