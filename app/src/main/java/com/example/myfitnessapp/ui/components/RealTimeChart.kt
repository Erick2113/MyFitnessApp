package com.example.myfitnessapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RealTimeChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColors: List<Color> = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF)),
    fillColors: List<Color> = listOf(Color(0x6600E5FF), Color(0x007C4DFF)),
    gridColor: Color = Color(0x1AFFFFFF),
    labelColor: Color = Color(0x99FFFFFF),
    maxDataPoints: Int = 100,
    maxValue: Float = 25f,
    minValue: Float = 5f,
    showLabels: Boolean = true,
    yAxisLabel: String = "m/s²"
) {
    val textMeasurer = rememberTextMeasurer()

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(start = if (showLabels) 40.dp else 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
            .drawWithCache {
                val lineBrush = Brush.horizontalGradient(colors = lineColors)
                val fillBrush = Brush.verticalGradient(colors = fillColors)

                val linePath = Path()
                val fillPath = Path()
                val range = maxValue - minValue

                onDrawBehind {

                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = size.height * i / gridLines
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )


                        if (showLabels) {
                            val labelValue = maxValue - (range * i / gridLines)
                            val text = "${labelValue.toInt()}"
                            val textResult = textMeasurer.measure(
                                text = text,
                                style = TextStyle(fontSize = 9.sp, color = labelColor)
                            )
                            drawText(
                                textLayoutResult = textResult,
                                topLeft = Offset(-36.dp.toPx(), y - textResult.size.height / 2f)
                            )
                        }
                    }

                    if (data.isEmpty()) return@onDrawBehind

                    val stepX = size.width / maxDataPoints.toFloat()


                    linePath.reset()
                    fillPath.reset()

                    data.forEachIndexed { index, value ->
                        val x = index * stepX
                        val normalizedValue = ((value - minValue) / range).coerceIn(0f, 1f)
                        val y = size.height - (normalizedValue * size.height)

                        if (index == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, size.height)
                            fillPath.lineTo(x, y)
                        } else {
                            linePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }


                    val lastX = (data.size - 1) * stepX
                    fillPath.lineTo(lastX, size.height)
                    fillPath.close()


                    drawPath(path = fillPath, brush = fillBrush, style = Fill)


                    drawPath(
                        path = linePath,
                        brush = lineBrush,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )


                    if (data.isNotEmpty()) {
                        val lastValue = data.last()
                        val lastNorm = ((lastValue - minValue) / range).coerceIn(0f, 1f)
                        val lastY = size.height - (lastNorm * size.height)
                        val dotX = (data.size - 1) * stepX


                        drawCircle(
                            color = lineColors.first().copy(alpha = 0.3f),
                            radius = 8.dp.toPx(),
                            center = Offset(dotX, lastY)
                        )

                        drawCircle(
                            color = lineColors.first(),
                            radius = 4.dp.toPx(),
                            center = Offset(dotX, lastY)
                        )
                    }
                }
            }
    )
}