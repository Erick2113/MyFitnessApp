package com.example.myfitnessapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeeklyBarChart(
    data: List<Int>,
    modifier: Modifier = Modifier,
    barGradientColors: List<Color> = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF)),
    labelColor: Color = Color(0x99FFFFFF),
    gridColor: Color = Color(0x1AFFFFFF),
    goalLine: Int? = 6000,
    goalLineColor: Color = Color(0x80FF6B35)
) {
    val dayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val animationProgress = remember { Animatable(0f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(data) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(start = 36.dp, end = 16.dp, top = 16.dp, bottom = 28.dp)
            .drawWithCache {
                val maxValue = (data.maxOrNull() ?: 1).coerceAtLeast(goalLine ?: 1).toFloat() * 1.15f
                val barCount = data.size.coerceAtMost(7)
                val totalGap = size.width * 0.35f
                val barWidth = (size.width - totalGap) / barCount
                val gap = totalGap / (barCount + 1)

                onDrawBehind {

                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = size.height * i / gridLines
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )

                        val labelVal = (maxValue * (1f - i.toFloat() / gridLines)).toInt()
                        val text = if (labelVal >= 1000) "${labelVal / 1000}k" else "$labelVal"
                        val textResult = textMeasurer.measure(
                            text = text,
                            style = TextStyle(fontSize = 9.sp, color = labelColor)
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(-34.dp.toPx(), y - textResult.size.height / 2f)
                        )
                    }


                    if (goalLine != null) {
                        val goalY = size.height * (1f - goalLine / maxValue)
                        drawLine(
                            color = goalLineColor,
                            start = Offset(0f, goalY),
                            end = Offset(size.width, goalY),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )

                        val goalText = textMeasurer.measure(
                            text = "Meta",
                            style = TextStyle(fontSize = 9.sp, color = goalLineColor, fontWeight = FontWeight.Bold)
                        )
                        drawText(
                            textLayoutResult = goalText,
                            topLeft = Offset(size.width - goalText.size.width, goalY - goalText.size.height - 2.dp.toPx())
                        )
                    }


                    for (i in 0 until barCount) {
                        val value = data.getOrElse(i) { 0 }
                        val barHeight = (value / maxValue) * size.height * animationProgress.value
                        val x = gap + i * (barWidth + gap)
                        val y = size.height - barHeight

                        val brush = Brush.verticalGradient(
                            colors = barGradientColors,
                            startY = y,
                            endY = size.height
                        )

                        drawRoundRect(
                            brush = brush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )


                        val dayLabel = dayLabels.getOrElse(i) { "" }
                        val dayText = textMeasurer.measure(
                            text = dayLabel,
                            style = TextStyle(fontSize = 9.sp, color = labelColor)
                        )
                        drawText(
                            textLayoutResult = dayText,
                            topLeft = Offset(
                                x + (barWidth - dayText.size.width) / 2f,
                                size.height + 6.dp.toPx()
                            )
                        )


                        if (value > 0) {
                            val valueLabel = if (value >= 1000) "${value / 1000}.${(value % 1000) / 100}k" else "$value"
                            val valueText = textMeasurer.measure(
                                text = valueLabel,
                                style = TextStyle(
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = valueText,
                                topLeft = Offset(
                                    x + (barWidth - valueText.size.width) / 2f,
                                    y - valueText.size.height - 3.dp.toPx()
                                )
                            )
                        }
                    }
                }
            }
    )
}
