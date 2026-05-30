package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.HistoryEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.FeedbackUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScientificCalculatorView(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expr by viewModel.expression.collectAsState()
    val preview by viewModel.resultPreview.collectAsState()
    val isDeg by viewModel.isDegreeMode.collectAsState()
    val memory by viewModel.memoryValue.collectAsState()
    val hasMemory = memory != 0.0
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsState()

    val historyList by viewModel.calculationHistory.collectAsState()
    val favoriteList by viewModel.favoriteHistory.collectAsState()

    val soundOn by viewModel.isSoundEnabled.collectAsState()
    val vibeOn by viewModel.isVibrationEnabled.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var showHistory by remember { mutableStateOf(false) }
    var filterFavoritesOnly by remember { mutableStateOf(false) }
    var scientificExpanded by remember { mutableStateOf(false) }

    val activeHistory = if (filterFavoritesOnly) favoriteList else historyList

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- Scientific indicators line ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DEG/RAD Toggle
                Button(
                    onClick = { viewModel.toggleDegreeMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                ) {
                    Text(
                        text = if (isDeg) "DEG" else "RAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Memory Indicator
                if (hasMemory) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "M",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Microphone Voice Input Simulation
                IconButton(
                    onClick = { viewModel.simulateVoiceMathInput() },
                    modifier = Modifier
                        .background(
                            if (isRecordingVoice) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else Color.Transparent,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Voice command math",
                        tint = if (isRecordingVoice) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Call history drawer toggle
                IconButton(onClick = { showHistory = !showHistory }) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "Calculation history",
                        tint = if (showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // --- Voice Recording Ripple Status ---
            AnimatedVisibility(
                visible = isRecordingVoice,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.error,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Listening to maths speech...",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // --- Mathematical LED Screen ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd)
                ) {
                    // Current math expression text field
                    Text(
                        text = expr.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = if (expr.length > 15) 28.sp else 38.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = FontFamily.Default,
                            textAlign = TextAlign.End
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (expr.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(expr))
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Result Preview Text Flow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (preview.isNotEmpty()) {
                            Text(
                                text = "≈ $preview",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(preview))
                                    }
                            )
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(preview))
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy result",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- Main Custom Keypad Grid Panel ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Interactive Expand toggles for advanced scientific parameters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Scientific Toggle
                    Button(
                        onClick = { scientificExpanded = !scientificExpanded },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scientificExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (scientificExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
                    ) {
                        Icon(
                            imageVector = if (scientificExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle scientific Mode",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Scientific", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Sound Effects Mode status
                    Row {
                        IconButton(onClick = { viewModel.isSoundEnabled.value = !soundOn }) {
                            Icon(
                                imageVector = if (soundOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Toggle clicks sound",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.isVibrationEnabled.value = !vibeOn }) {
                            Icon(
                                imageVector = if (vibeOn) Icons.Default.Vibration else Icons.Default.Vibration,
                                contentDescription = "Toggle haptic vibration",
                                tint = if (vibeOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Memory operations row MC MR M+ M-
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val mButtons = listOf("MC", "MR", "M+", "M-")
                    mButtons.forEach { mKey ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (soundOn) { /* play */ }
                                    if (vibeOn) { /* vibration */ }

                                    when (mKey) {
                                        "MC" -> viewModel.onMemoryClear()
                                        "MR" -> viewModel.onMemoryRecall()
                                        "M+" -> viewModel.onMemoryAdd()
                                        "M-" -> viewModel.onMemorySubtract()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mKey,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Advanced Scientific Buttons (Animated Expandable)
                AnimatedVisibility(
                    visible = scientificExpanded,
                    enter = expandVertically(animationSpec = tween(250)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sciRows = listOf(
                            listOf("sin", "cos", "tan", "sin⁻¹"),
                            listOf("cos⁻¹", "tan⁻¹", "sinh", "cosh"),
                            listOf("tanh", "log", "ln", "√"),
                            listOf("∛", "!", "^", "mod"),
                            listOf("()", "π", "e", "1/x")
                        )
                        sciRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    CalculatorButton(
                                        text = key,
                                        modifier = Modifier.weight(1f),
                                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                        textColor = MaterialTheme.colorScheme.secondary,
                                        fontSize = 13.sp,
                                        onClick = {
                                            if (key == "()") {
                                                viewModel.onParenthesisPress()
                                            } else {
                                                viewModel.onScientificPress(key)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Standard Keypad Row layouts
                val keypadRows = listOf(
                    listOf("AC", "⌫", "%", "÷"),
                    listOf("7", "8", "9", "×"),
                    listOf("4", "5", "6", "-"),
                    listOf("1", "2", "3", "+"),
                    listOf("+/-", "0", ".", "=")
                )

                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            val isOperator = key == "÷" || key == "×" || key == "-" || key == "+" || key == "="
                            val isAction = key == "AC" || key == "⌫" || key == "%" || key == "+/-"

                            val backgroundBrush = if (key == "=") {
                                Brush.horizontalGradient(listOf(AccentGradientStart, AccentGradientEnd))
                            } else null

                            val backgroundColor = when {
                                key == "=" -> MaterialTheme.colorScheme.primary
                                isOperator -> MaterialTheme.colorScheme.surfaceVariant
                                isAction -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            val textColor = when {
                                key == "=" -> Color.White
                                isOperator -> MaterialTheme.colorScheme.primary
                                isAction -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onBackground
                            }

                            CalculatorButton(
                                text = if (key == "AC" && expr.isNotEmpty()) "C" else key,
                                modifier = Modifier.weight(1f),
                                backgroundColor = backgroundColor,
                                backgroundBrush = backgroundBrush,
                                textColor = textColor,
                                fontSize = if (isOperator || isAction) 20.sp else 22.sp,
                                onClick = {
                                    when (key) {
                                        "AC" -> viewModel.onClear()
                                        "⌫" -> viewModel.onBackspace()
                                        "%" -> viewModel.onScientificPress("%")
                                        "÷" -> viewModel.onOperatorPress("÷")
                                        "×" -> viewModel.onOperatorPress("×")
                                        "-" -> viewModel.onOperatorPress("-")
                                        "+" -> viewModel.onOperatorPress("+")
                                        "=" -> viewModel.onCalculate()
                                        "+/-" -> viewModel.onPositiveNegativeToggle()
                                        else -> {
                                            viewModel.onNumberPress(key)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- Slide-up/down History Panel overlay ---
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(250)) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Calculation History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Favorites",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Switch(
                                checked = filterFavoritesOnly,
                                onCheckedChange = { filterFavoritesOnly = it },
                                modifier = Modifier.scale(0.7f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { showHistory = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close history"
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    if (activeHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (filterFavoritesOnly) "No favorite calculations" else "History is empty",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(activeHistory, key = { it.id }) { item ->
                                HistoryRowItem(
                                    item = item,
                                    onSelect = {
                                        viewModel.applyHistoryExpression(item.expression)
                                        showHistory = false
                                    },
                                    onFavoriteToggle = {
                                        viewModel.toggleFavoriteHistory(item.id, !item.isFavorite)
                                    },
                                    onDelete = {
                                        viewModel.deleteHistoryItem(item.id)
                                    }
                                )
                            }
                        }
                    }

                    // Bottom controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearHistory()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All")
                        }
                    }
                }
            }
        }
    }
}

// Helper extension to scale small switches
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.scale(scale)
)

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    backgroundBrush: Brush? = null,
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 20.sp,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1.2f) // beautiful rectangular grid shape
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (backgroundBrush != null) {
                    Modifier.background(backgroundBrush)
                } else {
                    Modifier.background(backgroundColor)
                }
            )
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
fun HistoryRowItem(
    item: HistoryEntity,
    onSelect: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.expression,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Default
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "= ${item.result}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Default
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite toggle",
                        tint = if (item.isFavorite) DarkPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
