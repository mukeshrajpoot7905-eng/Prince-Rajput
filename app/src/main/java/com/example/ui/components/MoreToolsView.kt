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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.BorderStroke
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
    var selectedSolverType by remember { mutableStateOf(0) } // 0 = Quadratic, 1 = AI Any Equation Solver
    var isAiSolverUnlocked by rememberSaveable { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    var adCountdown by remember { mutableStateOf(5) }

    val scroll = rememberScrollState()

    if (showAdDialog) {
        Dialog(
            onDismissRequest = { 
                if (adCountdown <= 0) {
                    showAdDialog = false
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SPONSOR ADVERTISING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Display the Adsterra ad
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AdsterraAdView()
                    }

                    // Countdown timer ticks down
                    LaunchedEffect(showAdDialog) {
                        while (adCountdown > 0) {
                            kotlinx.coroutines.delay(1000)
                            adCountdown--
                        }
                    }

                    if (adCountdown > 0) {
                        Text(
                            text = "Unlocking AI solver in $adCountdown seconds...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Button(
                            onClick = {
                                isAiSolverUnlocked = true
                                showAdDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Dismiss & Use AI Solver ✨")
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toggle Switcher between Standard and AI general solver
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedSolverType = 0 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSolverType == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedSolverType == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("ax² + bx + c = 0", fontSize = 11.sp)
            }

            Button(
                onClick = { selectedSolverType = 1 },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSolverType == 1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selectedSolverType == 1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1.5f)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI Any Solver ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (selectedSolverType == 0) {
            // Traditional Quadratic solver layout
            val a by viewModel.eqA.collectAsState()
            val b by viewModel.eqB.collectAsState()
            val c by viewModel.eqC.collectAsState()
            val result by viewModel.eqResult.collectAsState()

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
        } else {
            if (!isAiSolverUnlocked) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Premium Mode",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Unlock AI Solver with Sponsor Ad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "AI Any Solver is a premium feature powered by Gemini AI that can solve any complex query or formula. Watch a quick 5-second sponsor ad to unlock it instantly!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                        Button(
                            onClick = { 
                                showAdDialog = true
                                adCountdown = 5
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch Sponsor Ad to Unlock ✨")
                        }
                    }
                }
            } else {
                // General Equation Solver using Gemini API
                val eqQuery by viewModel.eqGeneral.collectAsState()
            val eqRoots by viewModel.eqGeneralRoots.collectAsState()
            val eqExplanation by viewModel.eqGeneralExplanation.collectAsState()
            val isSolving by viewModel.isSolvingGeneral.collectAsState()

            Text(
                text = "Type any mathematical equation, expression, or system of formulas:",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = eqQuery,
                onValueChange = { viewModel.eqGeneral.value = it },
                label = { Text("Equation/Formula to Solve") },
                placeholder = { Text("e.g. sin(x) = 0.5 or 3x + 15 = 0") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.solveGeneralEquation() },
                enabled = !isSolving && eqQuery.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSolving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solving equation with Gemini AI...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Solve with Gemini AI")
                }
            }

            if (eqRoots.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "AI Calculated Solutions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = eqRoots,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (eqExplanation.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Detailed Explanation Steps", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val annotatedExplanation = remember(eqExplanation, primaryColor) {
                            androidx.compose.ui.text.buildAnnotatedString {
                                val parts = eqExplanation.split("**")
                                for (i in parts.indices) {
                                    if (i % 2 == 1) {
                                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor))
                                        append(parts[i])
                                        pop()
                                    } else {
                                        append(parts[i])
                                    }
                                }
                            }
                        }
                        Text(
                            text = annotatedExplanation,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            }
        }
    }
}

fun sanitizeFormulaForPlotting(input: String): String {
    var formula = input.trim().replace("X", "x")
    if (formula.startsWith("y=", ignoreCase = true)) {
        formula = formula.substring(2)
    } else if (formula.startsWith("f(x)=", ignoreCase = true)) {
        formula = formula.substring(5)
    } else if (formula.startsWith("f1(x)=", ignoreCase = true)) {
        formula = formula.substring(6)
    } else if (formula.startsWith("f2(x)=", ignoreCase = true)) {
        formula = formula.substring(6)
    }
    
    // Replace typical representation characters with MathEvaluator counterparts
    formula = formula
        .replace("sin⁻¹", "asin")
        .replace("cos⁻¹", "acos")
        .replace("tan⁻¹", "atan")
        .replace("√", "sqrt")
        .replace("∛", "cbrt")

    // Insert implicit multiplication operators safely where omitted (e.g., 2x -> 2*x, x(x+1) -> x*(x+1))
    var prev: String
    do {
        prev = formula
        // Digit followed directly by a variable or symbol/opening parenthesis
        formula = formula.replace(Regex("(\\d+)([a-zA-Z\\(])"), "$1*$2")
        // Variable x followed by another function name start or opening parenthesis
        formula = formula.replace(Regex("(x)([a-df-wyz\\(])"), "$1*$2")
        // Closing parenthesis followed directly by a number or variable
        formula = formula.replace(Regex("\\)([0-9x])"), ")*$1")
    } while (formula != prev)

    return formula
}

@Composable
fun GraphDialogContent(viewModel: CalculatorViewModel) {
    val expr1 by viewModel.graphEquation.collectAsState()
    val expr2 by viewModel.graphEquation2.collectAsState()
    val showSecond by viewModel.showSecondGraph.collectAsState()

    val minX by viewModel.graphMinX.collectAsState()
    val maxX by viewModel.graphMaxX.collectAsState()
    val minY by viewModel.graphMinY.collectAsState()
    val maxY by viewModel.graphMaxY.collectAsState()

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Equation Inputs with nice design headers
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = expr1,
                    onValueChange = { viewModel.graphEquation.value = it },
                    label = { Text("Primary function f₁(x)") },
                    placeholder = { Text("e.g. sin(x) or x^2-2x or x^3") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkPrimary,
                        unfocusedBorderColor = DarkPrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add 2nd function comparison f₂(x)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = showSecond,
                        onCheckedChange = { viewModel.showSecondGraph.value = it }
                    )
                }

                if (showSecond) {
                    OutlinedTextField(
                        value = expr2,
                        onValueChange = { viewModel.graphEquation2.value = it },
                        label = { Text("Secondary function f₂(x)") },
                        placeholder = { Text("e.g. cos(x) or x + 2") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkSecondary,
                            unfocusedBorderColor = DarkSecondary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Beautiful pitch dark Canvas for high contrast Neon trace plots
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F111A))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val gridColor = Color(0xFF23253B)
                val axisColor = Color(0xFF555A7E)

                // Render grid subdivisions (11 divisions)
                for (i in 1..10) {
                    val y = height * (i / 11f)
                    drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
                }
                for (j in 1..10) {
                    val x = width * (j / 11f)
                    drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
                }

                // Render coordinate axis lines if they fall within viewport boundaries
                val drawAxisY = minX <= 0.0 && maxX >= 0.0
                val drawAxisX = minY <= 0.0 && maxY >= 0.0

                if (drawAxisY) {
                    val x0 = (((0.0 - minX) / (maxX - minX)) * width).toFloat()
                    drawLine(color = axisColor, start = Offset(x0, 0f), end = Offset(x0, height), strokeWidth = 3f)
                }
                if (drawAxisX) {
                    val y0 = (height - (((0.0 - minY) / (maxY - minY)) * height)).toFloat()
                    drawLine(color = axisColor, start = Offset(0f, y0), end = Offset(width, y0), strokeWidth = 3f)
                }

                val evaluator = MathEvaluator(isDegreeMode = false)

                // Plot calculation helper
                fun drawEquationPath(rawExpr: String, color: Color) {
                    val path = Path()
                    var firstPoint = true
                    val step = (width / 200f).coerceAtLeast(1f).toInt() // fast resolution render step
                    val sanitized = sanitizeFormulaForPlotting(rawExpr)

                    for (px in 0..width.toInt() step step) {
                        val x = minX + (px.toDouble() / width.toDouble()) * (maxX - minX)
                        try {
                            val substituted = sanitized.replace("x", "($x)")
                            val yRes = evaluator.evaluate(substituted)

                            // Math Y to screen height pixel map
                            val py = (height - (((yRes - minY) / (maxY - minY)) * height)).toFloat()

                            if (!yRes.isNaN() && !yRes.isInfinite() && py >= -100f && py <= height + 100f) {
                                if (firstPoint) {
                                        path.moveTo(px.toFloat(), py)
                                        firstPoint = false
                                } else {
                                        path.lineTo(px.toFloat(), py)
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = 5f)
                    )
                }

                // Plot primary curve trace (Amber-Orange trace)
                if (expr1.isNotBlank()) {
                     drawEquationPath(expr1, Color(0xFFFFA000))
                }

                // Plot comparison secondary curve trace if Switch is on (Cyan-Blue trace)
                if (showSecond && expr2.isNotBlank()) {
                     drawEquationPath(expr2, Color(0xFF00E5FF))
                }
            }
        }

        // Navigation parameters readout and controller buttons card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.US, "Viewport: X [ %.1f to %.1f ] , Y [ %.1f to %.1f ]", minX, maxX, minY, maxY),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom Out Icon Button
                    IconButton(
                        onClick = { viewModel.zoomGraph(1.5) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }

                    // Layout 4-directional joystick d-pad controller
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { viewModel.panGraph(0.0, 2.0) }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Pan Up")
                        }
                        Row {
                            IconButton(onClick = { viewModel.panGraph(-2.0, 0.0) }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Pan Left")
                            }
                            IconButton(onClick = { viewModel.resetGraphView() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset View")
                            }
                            IconButton(onClick = { viewModel.panGraph(2.0, 0.0) }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Pan Right")
                            }
                        }
                        IconButton(onClick = { viewModel.panGraph(0.0, -2.0) }) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Pan Down")
                        }
                    }

                    // Zoom In Icon Button
                    IconButton(
                        onClick = { viewModel.zoomGraph(0.6) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(10.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
    }
}

data class ToolItem(
    val id: String,
    val title: String,
    val desc: String,
    val icon: ImageVector
)
