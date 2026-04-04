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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
    var operation by remember { mutableStateOf("+") }
    var firstError by remember { mutableStateOf(false) }
    var secondError by remember { mutableStateOf(false) }
    var animationStep by remember { mutableIntStateOf(0) }
    var animationTrigger by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }

    // Animate: step through +1 arcs, then show the summary arc
    LaunchedEffect(animationTrigger) {
        val b = addendB
        if (b != null && b != 0L) {
            val totalSteps = abs(b).toInt()
            animationStep = 0
            isPaused = false
            for (i in 1..totalSteps + 1) {
                // Wait while paused
                if (isPaused) {
                    snapshotFlow { isPaused }.first { !it }
                }
                val baseDelay = max(80L, 500L / max(totalSteps.toLong(), 1L))
                delay((baseDelay / speedMultiplier).toLong().coerceAtLeast(30L))
                animationStep = i
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Integer Calculator",
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                val a = firstNumber.trim().toLongOrNull()
                val b = secondNumber.trim().toLongOrNull()
                firstError = a == null
                secondError = b == null
                if (a != null && b != null) {
                    animationStep = 0
                    addendA = a
                    addendB = b
                    operation = "+"
                    result = "${a + b}"
                    animationTrigger++
                }
            }) {
                Text("Add")
            }

            Button(onClick = {
                val a = firstNumber.trim().toLongOrNull()
                val b = secondNumber.trim().toLongOrNull()
                firstError = a == null
                secondError = b == null
                if (a != null && b != null) {
                    animationStep = 0
                    addendA = a
                    addendB = -b
                    operation = "\u2212"
                    result = "${a - b}"
                    animationTrigger++
                }
            }) {
                Text("Subtract")
            }
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

            // Animation controls: Restart, Pause/Resume, Speed
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = {
                    animationStep = 0
                    isPaused = false
                    animationTrigger++
                }) {
                    Text("\u21BB Restart")
                }

                OutlinedButton(onClick = { isPaused = !isPaused }) {
                    Text(if (isPaused) "\u25B6 Resume" else "\u23F8 Pause")
                }

                Text(
                    text = "Speed: ${"%,.1f".format(speedMultiplier)}x",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Slider(
                    value = speedMultiplier,
                    onValueChange = { speedMultiplier = it },
                    valueRange = 0.25f..4f,
                    steps = 14,
                    modifier = Modifier.width(140.dp)
                )
            }

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

                    val absB = abs(b).toInt()
                    val direction = if (b > 0) 1 else -1
                    val stepsShown = min(animationStep, absB)
                    val showSummary = animationStep > absB

                    // Draw progressive red dot at current position
                    val currentPos = a + direction.toLong() * stepsShown
                    val dotPos = if (showSummary) sum else currentPos
                    drawCircle(
                        color = errorColor,
                        radius = 10f,
                        center = Offset(valueToX(dotPos), baseY)
                    )

                    // Draw individual +1 / −1 arcs
                    if (b != 0L) {
                        val unitArcHeight = pxPerUnit * 0.35f
                        for (i in 0 until stepsShown) {
                            val arcStart = a + direction.toLong() * i
                            val arcEnd = arcStart + direction.toLong()
                            val sx = valueToX(arcStart)
                            val ex = valueToX(arcEnd)

                            val unitPath = Path().apply {
                                moveTo(sx, baseY)
                                cubicTo(
                                    sx, baseY - unitArcHeight,
                                    ex, baseY - unitArcHeight,
                                    ex, baseY
                                )
                            }
                            drawPath(
                                path = unitPath,
                                color = tertiaryColor.copy(alpha = 0.6f),
                                style = Stroke(width = 2f, cap = StrokeCap.Round)
                            )

                            // Arrowhead
                            val arrowSize = 8f
                            val arrowDir = if (b > 0) -1f else 1f
                            drawLine(
                                color = tertiaryColor.copy(alpha = 0.6f),
                                start = Offset(ex, baseY),
                                end = Offset(ex + arrowDir * arrowSize, baseY - arrowSize),
                                strokeWidth = 2f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = tertiaryColor.copy(alpha = 0.6f),
                                start = Offset(ex, baseY),
                                end = Offset(ex + arrowDir * arrowSize, baseY + arrowSize * 0.3f),
                                strokeWidth = 2f,
                                cap = StrokeCap.Round
                            )

                            // +1 / −1 label
                            drawContext.canvas.nativeCanvas.apply {
                                val unitLabel = if (b > 0) "+1" else "\u22121"
                                val paint = android.graphics.Paint().apply {
                                    color = tertiaryColor.copy(alpha = 0.6f).hashCode()
                                    textSize = 20f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                drawText(unitLabel, (sx + ex) / 2f, baseY - unitArcHeight + 2f, paint)
                            }
                        }

                        // Draw summary arc after all +1 arcs are done
                        if (showSummary) {
                            val startX = valueToX(a)
                            val endX = valueToX(sum)
                            val arcHeight = min(abs(b).toFloat() * pxPerUnit * 0.4f, h * 0.45f)
                                .coerceAtLeast(pxPerUnit * 0.55f) // taller than the +1 arcs
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

                            // Label on the arc showing the operation
                            drawContext.canvas.nativeCanvas.apply {
                                val displayB = abs(b)
                                val label = if (b > 0) "+$displayB" else "\u2212$displayB"
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
}
