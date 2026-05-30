package com.example.ui.components

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkPrimary
import com.example.ui.theme.DarkSecondary
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.MathEvaluator
import java.util.*
import kotlin.math.sin

@Composable
fun MoreToolsView(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    var activeToolDialog by remember { mutableStateOf<String?>(null) }

    val toolsList = listOf(
        ToolItem("bmi", "BMI Calculator", "Body Mass Index parameters", Icons.Default.Accessibility),
        ToolItem("age", "Age Calculator", "Exact age & birthday tracker", Icons.Default.Cake),
        ToolItem("percentage", "Percent Calculator", "All major % fractions", Icons.Default.Percent),
        ToolItem("emi", "EMI Calculator", "Monthly home credit payment", Icons.Default.Payments),
        ToolItem("tip", "Tip Calculator", "Bill split and tip sliders", Icons.Default.LocalDining),
        ToolItem("equation", "Equation Solver", "Linear & Quadratic solvers", Icons.Default.Functions),
        ToolItem("graph", "Graph Plotter", "Plots coordinates on Canvas", Icons.Default.ShowChart)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Advanced Tools",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Extra math and financial utilities",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(toolsList, key = { it.id }) { tool ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { activeToolDialog = tool.id },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { activeToolDialog = tool.id },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = tool.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        }

                        Column {
                            Text(text = tool.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = tool.desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // --- Dynamic Tool Dialog Display ---
    activeToolDialog?.let { dialogId ->
        AlertDialog(
            onDismissRequest = { activeToolDialog = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeToolDialog = null }) {
                    Text("Close")
                }
            },
            title = {
                val tool = toolsList.find { it.id == dialogId }
                Text(text = tool?.title ?: "Tool", fontWeight = FontWeight.Bold)
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                ) {
                    when (dialogId) {
                        "bmi" -> BMIDialogContent(viewModel)
                        "age" -> AgeDialogContent(viewModel)
                        "percentage" -> PercentageDialogContent(viewModel)
                        "emi" -> EMIDialogContent(viewModel)
                        "tip" -> TipDialogContent(viewModel)
                        "equation" -> EquationDialogContent(viewModel)
                        "graph" -> GraphDialogContent(viewModel)
                        else -> Text("Comming Soon")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- Specific Content composables ---

@Composable
fun BMIDialogContent(viewModel: CalculatorViewModel) {
    val height by viewModel.bmiHeight.collectAsState()
    val weight by viewModel.bmiWeight.collectAsState()
    val result by viewModel.bmiResult.collectAsState()
    val category by viewModel.bmiCategory.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = height,
            onValueChange = { viewModel.bmiHeight.value = it; viewModel.calculateBMI() },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { viewModel.bmiWeight.value = it; viewModel.calculateBMI() },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (result.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Your BMI Score", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = result, fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = category,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (category) {
                            "Normal Weight" -> Color(0xFF4CAF50)
                            "Overweight" -> Color(0xFFFF9800)
                            "Obese" -> Color(0xFFF44336)
                            else -> DarkSecondary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AgeDialogContent(viewModel: CalculatorViewModel) {
    val context = LocalContext.current
    val year by viewModel.birthYear.collectAsState()
    val month by viewModel.birthMonth.collectAsState()
    val day by viewModel.birthDay.collectAsState()

    val yearStr by viewModel.birthYearStr.collectAsState()
    val monthStr by viewModel.birthMonthStr.collectAsState()
    val dayStr by viewModel.birthDayStr.collectAsState()

    val ageY by viewModel.ageYears.collectAsState()
    val ageM by viewModel.ageMonths.collectAsState()
    val ageD by viewModel.ageDays.collectAsState()
    val nextDays by viewModel.nextBirthdayDays.collectAsState()

    val scroll = rememberScrollState()

    // Show date picker dialog
    val showDatePicker = {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                viewModel.setBirthdate(selectedYear, selectedMonth, selectedDay)
            },
            year,
            month - 1,
            day
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(
            onClick = showDatePicker,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Calendar Picker")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Date from Calendar")
        }

        Text("Or edit manually below:", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dayStr,
                onValueChange = { viewModel.setBirthDayStr(it) },
                label = { Text("Day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = monthStr,
                onValueChange = { viewModel.setBirthMonthStr(it) },
                label = { Text("Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = yearStr,
                onValueChange = { viewModel.setBirthYearStr(it) },
                label = { Text("Year") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1.5f)
            )
        }

        if (ageY.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Current Age", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "$ageY Years, $ageM Months, $ageD Days", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Next Birthday in", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$nextDays Days remaining", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun PercentageDialogContent(viewModel: CalculatorViewModel) {
    val percentX by viewModel.percentX.collectAsState()
    val percentY by viewModel.percentY.collectAsState()
    val resOf by viewModel.percentResultOf.collectAsState()
    val resWhat by viewModel.percentResultWhatPercent.collectAsState()
    val resDiff by viewModel.percentResultDiff.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = percentX,
                onValueChange = { viewModel.percentX.value = it; viewModel.calculatePercentages() },
                label = { Text("Value X") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = percentY,
                onValueChange = { viewModel.percentY.value = it; viewModel.calculatePercentages() },
                label = { Text("Value Y") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$percentX% of $percentY", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = resOf, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$percentX is what % of $percentY", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = resWhat, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "% difference $percentX to $percentY", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = resDiff, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (resDiff.startsWith("+")) Color(0xFF4CAF50) else Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun EMIDialogContent(viewModel: CalculatorViewModel) {
    val p by viewModel.emiPrincipal.collectAsState()
    val r by viewModel.emiInterestRate.collectAsState()
    val tenure by viewModel.emiTenure.collectAsState()

    val mPayment by viewModel.emiMonthlyPayment.collectAsState()
    val tInterest by viewModel.emiTotalInterest.collectAsState()
    val tPayable by viewModel.emiTotalPayable.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = p,
            onValueChange = { viewModel.emiPrincipal.value = it; viewModel.calculateEMI() },
            label = { Text("Principal loan Amount ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = r,
                onValueChange = { viewModel.emiInterestRate.value = it; viewModel.calculateEMI() },
                label = { Text("Annual Interest %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = tenure,
                onValueChange = { viewModel.emiTenure.value = it; viewModel.calculateEMI() },
                label = { Text("Tenure (Months)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        if (mPayment.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Monthly EMI", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "$$mPayment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Interest", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "$$tInterest", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Payable Amount", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "$$tPayable", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
fun TipDialogContent(viewModel: CalculatorViewModel) {
    val bill by viewModel.tipBillAmount.collectAsState()
    val tipPct by viewModel.tipPercentage.collectAsState()
    val splitCount by viewModel.tipSplitCount.collectAsState()

    val tipAmt by viewModel.tipTotalAmount.collectAsState()
    val tipPerP by viewModel.tipPerPerson.collectAsState()
    val totalPerP by viewModel.tipTotalPerPerson.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = bill,
            onValueChange = { viewModel.tipBillAmount.value = it; viewModel.calculateTip() },
            label = { Text("Bill Amount ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text(text = "Tip Percentage: ${tipPct.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = tipPct,
                onValueChange = { viewModel.tipPercentage.value = it; viewModel.calculateTip() },
                valueRange = 0f..50f
            )
        }

        Column {
            Text(text = "Split Count: ${splitCount.toInt()} persons", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Slider(
                value = splitCount,
                onValueChange = { viewModel.tipSplitCount.value = it; viewModel.calculateTip() },
                valueRange = 1f..15f
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Tip Amount", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$$tipAmt", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Tip Per Person", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$$tipPerP", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Total Per Person Amount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "$$totalPerP", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun EquationDialogContent(viewModel: CalculatorViewModel) {
    val a by viewModel.eqA.collectAsState()
    val b by viewModel.eqB.collectAsState()
    val c by viewModel.eqC.collectAsState()
    val result by viewModel.eqResult.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Solves: a·x² + b·x + c = 0", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = a,
                onValueChange = { viewModel.eqA.value = it; viewModel.solveEquations() },
                label = { Text("a") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = b,
                onValueChange = { viewModel.eqB.value = it; viewModel.solveEquations() },
                label = { Text("b") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = c,
                onValueChange = { viewModel.eqC.value = it; viewModel.solveEquations() },
                label = { Text("c") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        if (result.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Calculated Roots", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun GraphDialogContent(viewModel: CalculatorViewModel) {
    val expr by viewModel.graphEquation.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = expr,
            onValueChange = { viewModel.graphEquation.value = it },
            label = { Text("Function f(x) to plot") },
            placeholder = { Text("e.g. sin(x) or cos(x) or x^2") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Custom Neon coordinate math Canvas grid drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F111A)) // pitch dark background for beautiful neon plotting
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Draw central coordinate axes grid (gray lines)
                val gridColor = Color(0xFF23253B)
                val axisColor = Color(0xFF4B5563)

                // Grid lines horizontal
                for (i in 1..10) {
                    val y = height * (i / 11f)
                    drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
                }
                // Grid lines vertical
                for (j in 1..10) {
                    val x = width * (j / 11f)
                    drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
                }

                // Main Central Axes
                drawLine(color = axisColor, start = Offset(0f, height / 2), end = Offset(width, height / 2), strokeWidth = 3f)
                drawLine(color = axisColor, start = Offset(width / 2, 0f), end = Offset(width / 2, height), strokeWidth = 3f)

                // Mathematically evaluate function from x = -10 to +10 and plot points
                val path = Path()
                val scaleX = 25f // pixels per unit
                val scaleY = 25f

                val centerX = width / 2
                val centerY = height / 2

                var firstPoint = true
                val evaluator = MathEvaluator(isDegreeMode = false) // Radians for sleek wave plots!

                // Loop across pixels to plot a smooth curve line
                val step = 2
                for (px in 0..width.toInt() step step) {
                    val x = (px - centerX) / scaleX // convert pixel back to mathematical math units

                    try {
                        // Dynamically substitute 'x' into the formula
                        // Since formula is like sin(x) or x^2, let's do a simple replacement parameter
                        val substituted = expr
                            .replace("x", "($x)")
                        val yRes = evaluator.evaluate(substituted)

                        // Convert y value to pixel offset
                        val py = centerY - (yRes * scaleY)

                        if (!yRes.isNaN() && !yRes.isInfinite() && py >= 0 && py <= height) {
                            if (firstPoint) {
                                path.moveTo(px.toFloat(), py.toFloat())
                                firstPoint = false
                            } else {
                                path.lineTo(px.toFloat(), py.toFloat())
                            }
                        }
                    } catch (_: Exception) {}
                }

                // Draw neon curve path on coordinate canvas
                drawPath(
                    path = path,
                    color = DarkPrimary, // neon Amber/orange color waveform
                    style = Stroke(width = 5f)
                )
            }
        }

        Text(
            text = "Grid bounds: x [-6, +6], y [-5, +5]",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class ToolItem(
    val id: String,
    val title: String,
    val desc: String,
    val icon: ImageVector
)
