package com.example.myfitnessapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DonutSegment(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    centerText: String = "",
    centerSubText: String = "",
    strokeWidth: Float = 40f
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    val animationProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(segments) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Spacer(
                modifier = Modifier
                    .size(200.dp)
                    .drawWithCache {
                        val padding = strokeWidth / 2f + 4f
                        val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                        val topLeft = Offset(padding, padding)

                        onDrawBehind {
                            // Fondo del anillo
                            drawArc(
                                color = Color(0x1AFFFFFF),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )

                            // Segmentos animados
                            var startAngle = -90f
                            segments.forEach { segment ->
                                val sweep = (segment.value / total) * 360f * animationProgress.value
                                drawArc(
                                    color = segment.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                )
                                startAngle += (segment.value / total) * 360f * animationProgress.value
                            }

                            // Texto central
                            if (centerText.isNotEmpty()) {
                                val mainText = textMeasurer.measure(
                                    text = centerText,
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                drawText(
                                    textLayoutResult = mainText,
                                    topLeft = Offset(
                                        (size.width - mainText.size.width) / 2f,
                                        (size.height - mainText.size.height) / 2f - 10.dp.toPx()
                                    )
                                )
                            }

                            if (centerSubText.isNotEmpty()) {
                                val subText = textMeasurer.measure(
                                    text = centerSubText,
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = Color(0xFFB0BEC5),
                                        textAlign = TextAlign.Center
                                    )
                                )
                                drawText(
                                    textLayoutResult = subText,
                                    topLeft = Offset(
                                        (size.width - subText.size.width) / 2f,
                                        (size.height - subText.size.height) / 2f + 14.dp.toPx()
                                    )
                                )
                            }
                        }
                    }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leyenda
        segments.forEach { segment ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = segment.color,
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = segment.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0BEC5),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${segment.value.toInt()} min",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
