package com.simulator.adder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

// Fun colors for kids
private val funBlue = Color(0xFF42A5F5)
private val funGreen = Color(0xFF66BB6A)
private val funOrange = Color(0xFFFF7043)
private val funPink = Color(0xFFEC407A)
private val funPurple = Color(0xFF7E57C2)
private val funYellow = Color(0xFFFFEE58)
private val bgCream = Color(0xFFFFF8E1)
private val hopColors = listOf(funOrange, funGreen, funBlue, funPink)

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
    var editingFirst by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<String?>(null) }
    var addendA by remember { mutableStateOf<Long?>(null) }
    var addendB by remember { mutableStateOf<Long?>(null) }
    var operation by remember { mutableStateOf("+") }
    var animationStep by remember { mutableIntStateOf(0) }
    var animationTrigger by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(animationTrigger) {
        val b = addendB
        if (b != null && b != 0L) {
            val totalSteps = abs(b).toInt()
            animationStep = 0
            isPaused = false
            for (i in 1..totalSteps + 1) {
                if (isPaused) {
                    snapshotFlow { isPaused }.first { !it }
                }
                val baseDelay = max(300L, 1500L / max(totalSteps.toLong(), 1L))
                delay((baseDelay / speedMultiplier).toLong().coerceAtLeast(80L))
                animationStep = i
            }
        }
    }

    fun onDigit(digit: String) {
        if (editingFirst) {
            if (firstNumber.length < 6) firstNumber += digit
        } else {
            if (secondNumber.length < 6) secondNumber += digit
        }
    }

    fun onDelete() {
        if (editingFirst) {
            firstNumber = firstNumber.dropLast(1)
        } else {
            secondNumber = secondNumber.dropLast(1)
        }
    }

    fun onSubmit() {
        if (editingFirst) {
            if (firstNumber.isNotEmpty() && firstNumber.toLongOrNull() != null) {
                editingFirst = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgCream)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\uD83D\uDD22 Number Hopper!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = funPurple
        )

        // Result card - always reserves space, text visible only after animation finishes
        val animationDone = addendB != null && animationStep > abs(addendB!!).toInt()
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = if (animationDone && result != null) funYellow else Color.Transparent,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = if (animationDone && result != null) 4.dp else 0.dp,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (animationDone && result != null) "Answer: ${result!!}" else " ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (animationDone) funPurple else Color.Transparent,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }

        if (addendA != null && addendB != null) {
            Spacer(modifier = Modifier.height(10.dp))

            val a = addendA!!
            val b = addendB!!
            val sum = a + b
            val lineMin = min(min(a, sum), 0L) - 2
            val lineMax = max(max(a, sum), 0L) + 2
            val rangeSpan = (lineMax - lineMin).coerceAtLeast(5)
            val tickSpacing = 48.dp
            val canvasWidth = tickSpacing * rangeSpan.toFloat()
            val scrollState = rememberScrollState()
            val density = LocalDensity.current

            LaunchedEffect(addendA, addendB) {
                val pxPerUnit = with(density) { tickSpacing.toPx() }
                val firstNumPx = ((a - lineMin) * pxPerUnit).toInt()
                val targetScroll = (firstNumPx - 200).coerceAtLeast(0)
                scrollState.scrollTo(targetScroll)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(canvasWidth)
                        .height(180.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val baseY = h * 0.7f
                    val pxPerUnit = w / rangeSpan.toFloat()

                    fun valueToX(v: Long): Float = (v - lineMin) * pxPerUnit

                    drawLine(color = Color(0xFF8D6E63), start = Offset(0f, baseY), end = Offset(w, baseY), strokeWidth = 4f)

                    for (tick in lineMin..lineMax) {
                        val x = valueToX(tick)
                        val isKeyTick = tick == a || tick == sum || tick == 0L
                        val tickHeight = if (isKeyTick) 24f else 14f
                        val tickColor = when (tick) {
                            a -> funBlue
                            sum -> funPink
                            else -> Color(0xFF8D6E63)
                        }
                        drawLine(color = tickColor, start = Offset(x, baseY - tickHeight), end = Offset(x, baseY + tickHeight), strokeWidth = if (isKeyTick) 4f else 2f)
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor(
                                    when (tick) {
                                        a -> "#42A5F5"
                                        sum -> "#EC407A"
                                        else -> "#5D4037"
                                    }
                                )
                                textSize = if (isKeyTick) 36f else 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isKeyTick
                            }
                            drawText(tick.toString(), x, baseY + tickHeight + paint.textSize + 6f, paint)
                        }
                    }

                    drawCircle(color = funBlue, radius = 16f, center = Offset(valueToX(a), baseY))

                    val absB = abs(b).toInt()
                    val direction = if (b > 0) 1 else -1
                    val stepsShown = min(animationStep, absB)
                    val showSummary = animationStep > absB
                    val currentPos = a + direction.toLong() * stepsShown
                    val dotPos = if (showSummary) sum else currentPos
                    drawCircle(color = funPink, radius = 16f, center = Offset(valueToX(dotPos), baseY))

                    if (b != 0L) {
                        val unitArcHeight = pxPerUnit * 0.4f
                        for (i in 0 until stepsShown) {
                            val arcColor = hopColors[i % hopColors.size]
                            val arcStart = a + direction.toLong() * i
                            val arcEnd = arcStart + direction.toLong()
                            val sx = valueToX(arcStart)
                            val ex = valueToX(arcEnd)
                            val unitPath = Path().apply {
                                moveTo(sx, baseY)
                                cubicTo(sx, baseY - unitArcHeight, ex, baseY - unitArcHeight, ex, baseY)
                            }
                            drawPath(path = unitPath, color = arcColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
                            val arrowSize = 10f
                            val arrowDir = if (b > 0) -1f else 1f
                            drawLine(color = arcColor, start = Offset(ex, baseY), end = Offset(ex + arrowDir * arrowSize, baseY - arrowSize), strokeWidth = 3f, cap = StrokeCap.Round)
                            drawLine(color = arcColor, start = Offset(ex, baseY), end = Offset(ex + arrowDir * arrowSize, baseY + arrowSize * 0.3f), strokeWidth = 3f, cap = StrokeCap.Round)
                            drawContext.canvas.nativeCanvas.apply {
                                val unitLabel = if (b > 0) "+1" else "\u22121"
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor(
                                        when (i % hopColors.size) {
                                            0 -> "#FF7043"
                                            1 -> "#66BB6A"
                                            2 -> "#42A5F5"
                                            else -> "#EC407A"
                                        }
                                    )
                                    textSize = 24f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                drawText(unitLabel, (sx + ex) / 2f, baseY - unitArcHeight - 2f, paint)
                            }
                        }

                        if (showSummary) {
                            val startX = valueToX(a)
                            val endX = valueToX(sum)
                            val arcHeight = min(abs(b).toFloat() * pxPerUnit * 0.4f, h * 0.5f).coerceAtLeast(pxPerUnit * 0.6f)
                            val midX = (startX + endX) / 2f
                            val arcPath = Path().apply {
                                moveTo(startX, baseY)
                                cubicTo(startX, baseY - arcHeight, endX, baseY - arcHeight, endX, baseY)
                            }
                            drawPath(path = arcPath, color = funPurple, style = Stroke(width = 4f, cap = StrokeCap.Round))
                            val arrowSize = 14f
                            val arrowDir = if (b > 0) -1f else 1f
                            drawLine(color = funPurple, start = Offset(endX, baseY), end = Offset(endX + arrowDir * arrowSize, baseY - arrowSize), strokeWidth = 4f, cap = StrokeCap.Round)
                            drawLine(color = funPurple, start = Offset(endX, baseY), end = Offset(endX + arrowDir * arrowSize, baseY + arrowSize * 0.3f), strokeWidth = 4f, cap = StrokeCap.Round)
                            drawContext.canvas.nativeCanvas.apply {
                                val displayB = abs(b)
                                val label = if (b > 0) "+$displayB" else "\u2212$displayB"
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.parseColor("#7E57C2")
                                    textSize = 36f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                                drawText(label, midX, baseY - arcHeight - 2f, paint)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Number displays - tap to select which one to edit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clickable { editingFirst = true }
                    .border(
                        width = if (editingFirst) 3.dp else 1.dp,
                        color = if (editingFirst) funBlue else Color.Gray,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = if (editingFirst) Color.White else Color(0xFFF5F5F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\uD83D\uDD35 First", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = firstNumber.ifEmpty { "?" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (firstNumber.isEmpty()) Color.LightGray else funBlue
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clickable { editingFirst = false }
                    .border(
                        width = if (!editingFirst) 3.dp else 1.dp,
                        color = if (!editingFirst) funGreen else Color.Gray,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = if (!editingFirst) Color.White else Color(0xFFF5F5F5)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("\uD83D\uDFE2 Second", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = secondNumber.ifEmpty { "?" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (secondNumber.isEmpty()) Color.LightGray else funGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Number pad: 1-9
        val buttonShape = RoundedCornerShape(14.dp)
        val digitRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )
        for (row in digitRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (digit in row) {
                    Button(
                        onClick = { onDigit(digit) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = buttonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(digit, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = funPurple)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Bottom row: Delete, 0, Submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onDelete() },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = funOrange)
            ) {
                Text("\u232B", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onDigit("0") },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text("0", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = funPurple)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add / Subtract buttons
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {
                    val a = firstNumber.toLongOrNull()
                    val b = secondNumber.toLongOrNull()
                    if (a != null && b != null) {
                        animationStep = 0
                        addendA = a
                        addendB = b
                        operation = "+"
                        result = "${a + b}"
                        animationTrigger++
                        editingFirst = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = funGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(50.dp),
                enabled = firstNumber.toLongOrNull() != null && secondNumber.toLongOrNull() != null
            ) {
                Text("\u2795 Add!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val a = firstNumber.toLongOrNull()
                    val b = secondNumber.toLongOrNull()
                    if (a != null && b != null) {
                        animationStep = 0
                        addendA = a
                        addendB = -b
                        operation = "\u2212"
                        result = "${a - b}"
                        animationTrigger++
                        editingFirst = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = funOrange),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.height(50.dp),
                enabled = firstNumber.toLongOrNull() != null && secondNumber.toLongOrNull() != null
            ) {
                Text("\u2796 Subtract!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
