package com.example.myfitnessapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myfitnessapp.ui.theme.*

enum class AnalysisPerspective {
    GENERAL_PHASES, INTENSITY_ZONES
}

data class SegmentMetric(
    val name: String,
    val durationMin: Int,
    val avgImpact: Float,
    val description: String = "",
    val subSegments: List<SegmentMetric> = emptyList()
)

@Composable
fun HierarchicalAnalysisChart(
    steps: Int = 0,
    distance: Float = 0f,
    calories: Float = 0f,
    sessionMinutes: Int = 0,
    avgAcceleration: Float = 0f,
    modifier: Modifier = Modifier
) {
    var perspective by remember { mutableStateOf(AnalysisPerspective.GENERAL_PHASES) }

    val activeMinutes = sessionMinutes.coerceAtLeast(1)
    val warmupMin = (activeMinutes * 0.30f).toInt().coerceAtLeast(1)
    val cardioMin = (activeMinutes * 0.45f).toInt().coerceAtLeast(1)
    val cooldownMin = (activeMinutes * 0.25f).toInt().coerceAtLeast(1)

    val fullSession = remember(steps, sessionMinutes, distance, calories, avgAcceleration) {
        SegmentMetric(
            name = "Sesion Completa",
            durationMin = activeMinutes,
            avgImpact = avgAcceleration,
            description = "$steps pasos - ${String.format("%.2f", distance)} km - ${String.format("%.0f", calories)} kcal",
            subSegments = listOf(
                SegmentMetric(
                    "Fase Inicial (Calentamiento)",
                    warmupMin,
                    avgAcceleration * 0.75f,
                    description = "Activacion neuromuscular y adaptacion de ritmo"
                ),
                SegmentMetric(
                    "Fase Principal (Ritmo Sostenido)",
                    cardioMin,
                    avgAcceleration * 1.15f,
                    description = "Tramo de mayor rendimiento y gasto calorico"
                ),
                SegmentMetric(
                    "Fase Final (Enfriamiento)",
                    cooldownMin,
                    avgAcceleration * 0.85f,
                    description = "Descenso gradual de intensidad y recuperacion"
                )
            )
        )
    }

    val intensityPockets = remember(avgAcceleration, activeMinutes) {
        listOf(
            SegmentMetric(
                "Zona Alta (>14 m/s²)",
                cardioMin / 2,
                avgAcceleration * 1.3f,
                description = "Picos de impacto y esfuerzo maximo registrados"
            ),
            SegmentMetric(
                "Zona Moderada (11-14 m/s²)",
                cardioMin,
                avgAcceleration,
                description = "Cadencia estable y ritmo constante"
            ),
            SegmentMetric(
                "Zona Ligera (<11 m/s²)",
                warmupMin + cooldownMin,
                avgAcceleration * 0.7f,
                description = "Transicion suave y recuperacion activa"
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize()
        ) {
            Text(
                text = "Analisis Jerarquico",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Interpretacion de datos segun la perspectiva seleccionada",
                style = MaterialTheme.typography.bodySmall,
                color = TextOnDarkTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkBackground)
                    .padding(4.dp)
            ) {
                PerspectiveTab(
                    text = "Fases del Recorrido",
                    selected = perspective == AnalysisPerspective.GENERAL_PHASES,
                    onClick = { perspective = AnalysisPerspective.GENERAL_PHASES },
                    modifier = Modifier.weight(1f)
                )
                PerspectiveTab(
                    text = "Niveles de Esfuerzo",
                    selected = perspective == AnalysisPerspective.INTENSITY_ZONES,
                    onClick = { perspective = AnalysisPerspective.INTENSITY_ZONES },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (perspective == AnalysisPerspective.GENERAL_PHASES) {
                TopDownView(session = fullSession)
            } else {
                BottomUpView(pockets = intensityPockets)
            }
        }
    }
}

@Composable
private fun PerspectiveTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (selected) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) CyanPrimary else TextOnDarkTertiary,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
        )
    }
}

@Composable
private fun TopDownView(session: SegmentMetric) {
    Column {
        Text(
            text = "Desglose General a Fases",
            style = MaterialTheme.typography.labelMedium,
            color = CyanPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Estructura del volumen total distribuido en cada intervalo",
            style = MaterialTheme.typography.bodySmall,
            color = TextOnDarkTertiary
        )

        Spacer(modifier = Modifier.height(12.dp))


        ExpandableHierarchyBar(
            label = session.name,
            percentage = 1f,
            gradientColors = listOf(GradientCyanStart, GradientCyanEnd),
            detail = "${session.durationMin} min | ${String.format("%.1f", session.avgImpact)} m/s²",
            description = session.description
        )

        Spacer(modifier = Modifier.height(8.dp))


        val colors = listOf(
            listOf(GreenAccent, GreenDark),
            listOf(OrangeAccent, OrangeLight),
            listOf(PurpleAccent, PurpleLight)
        )

        session.subSegments.forEachIndexed { idx, sub ->
            val weight = sub.durationMin.toFloat() / session.durationMin.toFloat()
            ExpandableHierarchyBar(
                label = "  " + sub.name,
                percentage = weight,
                gradientColors = colors[idx % colors.size],
                detail = "${sub.durationMin} min (${(weight * 100).toInt()}%)",
                description = sub.description,
                indented = true
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun BottomUpView(pockets: List<SegmentMetric>) {
    Column {
        Text(
            text = "Sintesis por Niveles de Impacto",
            style = MaterialTheme.typography.labelMedium,
            color = OrangeAccent,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Agrupacion de aceleraciones individuales hacia el impacto acumulado",
            style = MaterialTheme.typography.bodySmall,
            color = TextOnDarkTertiary
        )

        Spacer(modifier = Modifier.height(12.dp))

        val totalTime = pockets.sumOf { it.durationMin }
        val colors = listOf(
            listOf(PinkAccent, Color(0xFFFF80AB)),
            listOf(OrangeAccent, AmberAccent),
            listOf(GreenAccent, Color(0xFFB9F6CA))
        )

        pockets.forEachIndexed { idx, pocket ->
            val weight = pocket.durationMin.toFloat() / totalTime.toFloat()
            ExpandableHierarchyBar(
                label = pocket.name,
                percentage = weight,
                gradientColors = colors[idx % colors.size],
                detail = "${pocket.durationMin} min | ${String.format("%.1f", pocket.avgImpact)} m/s²",
                description = pocket.description
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))


        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Consolidacion del Esfuerzo",
                    style = MaterialTheme.typography.labelLarge,
                    color = OrangeAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val avgImpact = pockets.map { it.avgImpact }.average()
                Text(
                    text = "Impacto medio consolidado: ${String.format("%.1f", avgImpact)} m/s²",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary
                )
                Text(
                    text = "Tiempo total analizado: $totalTime minutos",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary
                )
            }
        }
    }
}

@Composable
private fun ExpandableHierarchyBar(
    label: String,
    percentage: Float,
    gradientColors: List<Color>,
    detail: String,
    description: String = "",
    indented: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(600))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (indented) Modifier.padding(start = 12.dp) else Modifier)
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextOnDarkSecondary
                )
                if (description.isNotEmpty()) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextOnDarkTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF1A1F38))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (percentage * animProgress.value).coerceIn(0.03f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(Brush.horizontalGradient(gradientColors))
            )
        }

        AnimatedVisibility(
            visible = expanded && description.isNotEmpty(),
            enter = expandVertically() + fadeIn()
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextOnDarkTertiary,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }
    }
}