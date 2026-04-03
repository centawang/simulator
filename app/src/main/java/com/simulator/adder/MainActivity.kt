package com.simulator.adder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdderScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AdderScreen(modifier: Modifier = Modifier) {
    var firstNumber by remember { mutableStateOf("") }
    var secondNumber by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var addendA by remember { mutableStateOf<Long?>(null) }
    var addendB by remember { mutableStateOf<Long?>(null) }
    var firstError by remember { mutableStateOf(false) }
    var secondError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Integer Adder",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = firstNumber,
                onValueChange = {
                    firstNumber = it
                    firstError = false
                },
                label = { Text("First integer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = firstError,
                supportingText = if (firstError) {
                    { Text("Enter a valid integer") }
                } else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = secondNumber,
                onValueChange = {
                    secondNumber = it
                    secondError = false
                },
                label = { Text("Second integer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = secondError,
                supportingText = if (secondError) {
                    { Text("Enter a valid integer") }
                } else null,
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val a = firstNumber.trim().toLongOrNull()
            val b = secondNumber.trim().toLongOrNull()
            firstError = a == null
            secondError = b == null
            if (a != null && b != null) {
                addendA = a
                addendB = b
                result = "${a + b}"
            }
        }) {
            Text("Add")
        }

        Spacer(modifier = Modifier.height(24.dp))

        result?.let {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = "Result: $it",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }

        // Number line visualization
        if (addendA != null && addendB != null) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Number Line",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val a = addendA!!
            val b = addendB!!
            val sum = a + b
            val lineMin = min(min(a, sum), 0L) - 2
            val lineMax = max(max(a, sum), 0L) + 2
            val rangeSpan = (lineMax - lineMin).coerceAtLeast(5)
            val tickSpacing = 40.dp
            val canvasWidth = tickSpacing * rangeSpan.toFloat()
            val primaryColor = MaterialTheme.colorScheme.primary
            val errorColor = MaterialTheme.colorScheme.error
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val tertiaryColor = MaterialTheme.colorScheme.tertiary

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Canvas(
                    modifier = Modifier
                        .width(canvasWidth)
                        .height(160.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val baseY = h * 0.7f
                    val pxPerUnit = w / rangeSpan.toFloat()

                    fun valueToX(v: Long): Float = (v - lineMin) * pxPerUnit

                    // Draw main axis line
                    drawLine(
                        color = onSurfaceColor,
                        start = Offset(0f, baseY),
                        end = Offset(w, baseY),
                        strokeWidth = 3f
                    )

                    // Draw tick marks and labels
                    for (tick in lineMin..lineMax) {
                        val x = valueToX(tick)
                        val isKeyTick = tick == a || tick == sum || tick == 0L
                        val tickHeight = if (isKeyTick) 20f else 12f
                        val tickColor = when (tick) {
                            a -> primaryColor
                            sum -> errorColor
                            else -> onSurfaceColor
                        }
                        drawLine(
                            color = tickColor,
                            start = Offset(x, baseY - tickHeight),
                            end = Offset(x, baseY + tickHeight),
                            strokeWidth = if (isKeyTick) 3f else 1.5f
                        )
                        // Label
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = tickColor.hashCode()
                                textSize = if (isKeyTick) 32f else 24f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isKeyTick
                            }
                            drawText(tick.toString(), x, baseY + tickHeight + paint.textSize + 4f, paint)
                        }
                    }

                    // Draw dot at starting value (a)
                    drawCircle(
                        color = primaryColor,
                        radius = 10f,
                        center = Offset(valueToX(a), baseY)
                    )

                    // Draw dot at result (sum)
                    drawCircle(
                        color = errorColor,
                        radius = 10f,
                        center = Offset(valueToX(sum), baseY)
                    )

                    // Draw arc arrow from a to sum
                    if (b != 0L) {
                        val startX = valueToX(a)
                        val endX = valueToX(sum)
                        val arcHeight = min(abs(b).toFloat() * pxPerUnit * 0.4f, h * 0.45f)
                        val midX = (startX + endX) / 2f

                        val arcPath = Path().apply {
                            moveTo(startX, baseY)
                            cubicTo(
                                startX, baseY - arcHeight,
                                endX, baseY - arcHeight,
                                endX, baseY
                            )
                        }
                        drawPath(
                            path = arcPath,
                            color = tertiaryColor,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )

                        // Arrowhead at the end
                        val arrowSize = 12f
                        val arrowDir = if (b > 0) -1f else 1f
                        drawLine(
                            color = tertiaryColor,
                            start = Offset(endX, baseY),
                            end = Offset(endX + arrowDir * arrowSize, baseY - arrowSize),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = tertiaryColor,
                            start = Offset(endX, baseY),
                            end = Offset(endX + arrowDir * arrowSize, baseY + arrowSize * 0.3f),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )

                        // Label on the arc: "+b"
                        drawContext.canvas.nativeCanvas.apply {
                            val label = if (b > 0) "+$b" else "$b"
                            val paint = android.graphics.Paint().apply {
                                color = tertiaryColor.hashCode()
                                textSize = 30f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                            drawText(label, midX, baseY - arcHeight + 4f, paint)
                        }
                    }
                }
            }
        }
    }
}
