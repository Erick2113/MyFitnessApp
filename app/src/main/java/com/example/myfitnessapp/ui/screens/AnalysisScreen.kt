package com.example.myfitnessapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myfitnessapp.ui.components.*
import com.example.myfitnessapp.ui.theme.*
import com.example.myfitnessapp.viewmodel.RunningViewModel

@Composable
fun AnalysisScreen(viewModel: RunningViewModel) {
    val steps by viewModel.steps.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val calories by viewModel.calories.collectAsState()
    val sessionTime by viewModel.sessionTimeSeconds.collectAsState()
    val speedKmh by viewModel.speedKmh.collectAsState()
    val currentAccel by viewModel.currentAcceleration.collectAsState()
    val weeklySteps by viewModel.weeklySteps.collectAsState()
    val activityZones by viewModel.activityZones.collectAsState()
    val accelerationData by viewModel.accelerationData.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {

        Text(
            text = "Analisis de Rendimiento",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Informe cuantitativo y dinamico de la actividad",
            style = MaterialTheme.typography.bodyMedium,
            color = TextOnDarkTertiary
        )

        Spacer(modifier = Modifier.height(20.dp))


        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Resumen de la Sesion",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
                        value = "$steps",
                        label = "Pasos",
                        color = StepsColor
                    )
                    SummaryItem(
                        icon = Icons.Outlined.Route,
                        value = String.format("%.2f", distance),
                        label = "km",
                        color = DistanceColor
                    )
                    SummaryItem(
                        icon = Icons.Outlined.Speed,
                        value = String.format("%.1f", speedKmh),
                        label = "km/h",
                        color = GreenAccent
                    )
                    SummaryItem(
                        icon = Icons.Outlined.LocalFireDepartment,
                        value = String.format("%.0f", calories),
                        label = "kcal",
                        color = CaloriesColor
                    )
                    SummaryItem(
                        icon = Icons.Outlined.Timer,
                        value = viewModel.formatTime(sessionTime),
                        label = "Tiempo",
                        color = CyanPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


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
                    Column {
                        Text(
                            "Pasos Semanales",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Registro historico continuo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextOnDarkTertiary
                        )
                    }
                    val weekTotal = weeklySteps.sum()
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyanPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${weekTotal / 1000}k total",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyanPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                WeeklyBarChart(
                    data = weeklySteps,
                    barGradientColors = listOf(CyanPrimary, PurpleAccent),
                    goalLine = 6000,
                    goalLineColor = OrangeAccent.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Distribucion por Zonas de Intensidad",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Proporcion del tiempo invertido segun la cadencia",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkTertiary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                val totalMinutes = activityZones.sumOf { it.minutes }
                DonutChart(
                    segments = activityZones.map {
                        DonutSegment(
                            label = it.name,
                            value = it.minutes.toFloat(),
                            color = Color(it.color)
                        )
                    },
                    centerText = "$totalMinutes",
                    centerSubText = "minutos"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Registro Continuo de Impacto",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Serie temporal de aceleracion durante el entrenamiento",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkTertiary
                )

                RealTimeChart(
                    data = accelerationData,
                    lineColors = listOf(OrangeAccent, PinkAccent),
                    fillColors = listOf(OrangeAccent.copy(alpha = 0.2f), Color.Transparent)
                )

                if (accelerationData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip("Minimo", "${String.format("%.1f", accelerationData.minOrNull() ?: 0f)} m/s²", GreenAccent)
                        StatChip("Promedio", "${String.format("%.1f", accelerationData.average())} m/s²", AmberAccent)
                        StatChip("Maximo", "${String.format("%.1f", accelerationData.maxOrNull() ?: 0f)} m/s²", RedAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        HierarchicalAnalysisChart(
            steps = steps,
            distance = distance,
            calories = calories,
            sessionMinutes = (sessionTime / 60).toInt().coerceAtLeast(1),
            avgAcceleration = currentAccel
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
private fun StatChip(label: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}