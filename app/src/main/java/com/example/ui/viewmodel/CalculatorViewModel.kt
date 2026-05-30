package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.entity.CurrencyRateEntity
import com.example.data.entity.HistoryEntity
import com.example.data.repository.CalculatorRepository
import com.example.util.MathEvaluator
import com.example.util.UnitCategory
import com.example.util.UnitConverterUtil
import com.example.util.UnitInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import kotlin.math.sqrt

enum class AppTab {
    CALCULATOR, EXCHANGE_RATE, UNIT_CONVERTER, TOOLS, SETTINGS
}

class CalculatorViewModel(private val repository: CalculatorRepository) : ViewModel() {

    private val decimalFormat = DecimalFormat("#.########")

    // --- Tab Navigation ---
    private val _currentTab = MutableStateFlow(AppTab.CALCULATOR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- App Preferences / Settings States ---
    val isDarkTheme = MutableStateFlow<Boolean?>(null) // null means Auto / System
    val isSoundEnabled = MutableStateFlow(true)
    val isVibrationEnabled = MutableStateFlow(true)

    // --- Calculator Screen State ---
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _resultPreview = MutableStateFlow("")
    val resultPreview: StateFlow<String> = _resultPreview.asStateFlow()

    private val _isDegreeMode = MutableStateFlow(true)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode.asStateFlow()

    private val _memoryValue = MutableStateFlow(0.0)
    val memoryValue: StateFlow<Double> = _memoryValue.asStateFlow()

    val calculationHistory: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteHistory: StateFlow<List<HistoryEntity>> = repository.favoriteHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Voice simulation state ---
    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    init {
        // Hydrate initial exchange rates in case offline
        viewModelScope.launch(Dispatchers.IO) {
            val initialRates = listOf(
                CurrencyRateEntity("USD", 1.0),
                CurrencyRateEntity("INR", 83.45),
                CurrencyRateEntity("EUR", 0.92),
                CurrencyRateEntity("GBP", 0.79),
                CurrencyRateEntity("JPY", 156.35),
                CurrencyRateEntity("AED", 3.67),
                CurrencyRateEntity("RUB", 89.40),
                CurrencyRateEntity("BTC", 0.000015)
            )
            val currentCached = repository.getRateByCode("USD")
            if (currentCached == null) {
                repository.cacheCurrencyRates(initialRates)
            }
            // Trigger remote fetch
            fetchLiveRates()
        }
    }

    // --- Calculator Actions ---
    fun onNumberPress(num: String) {
        _expression.value += num
        updateResultPreview()
    }

    fun onOperatorPress(op: String) {
        val current = _expression.value
        // If empty and op is minus, let it be unary minus
        if (current.isEmpty()) {
            if (op == "-") {
                _expression.value = "-"
            }
            return
        }
        val lastChar = current.last()
        if (lastChar == '+' || lastChar == '-' || lastChar == '×' || lastChar == '÷' || lastChar == '^' || lastChar == 'm') {
            // Replace operator (with support for mod)
            if (current.endsWith("mod")) {
                _expression.value = current.drop(3) + op
            } else {
                _expression.value = current.drop(1) + op
            }
        } else {
            _expression.value += op
        }
        updateResultPreview()
    }

    fun onScientificPress(func: String) {
        when (func) {
            "π" -> {
                _expression.value += "π"
            }
            "e" -> {
                _expression.value += "e"
            }
            "x²" -> {
                _expression.value += "^2"
            }
            "x³" -> {
                _expression.value += "^3"
            }
            "1/x" -> {
                _expression.value += "^(-1)"
            }
            "sin", "cos", "tan", "sinh", "cosh", "tanh", "sin⁻¹", "cos⁻¹", "tan⁻¹", "log", "ln", "√", "∛" -> {
                _expression.value += "$func("
            }
            "!" -> {
                _expression.value += "!"
            }
            "%" -> {
                _expression.value += "%"
            }
            "RAND" -> {
                val rand = String.format(Locale.US, "%.4f", Math.random())
                _expression.value += rand
            }
            "mod" -> {
                _expression.value += "mod"
            }
            else -> {
                _expression.value += func
            }
        }
        updateResultPreview()
    }

    fun onParenthesisPress() {
        val current = _expression.value
        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }
        if (openCount > closeCount && current.isNotEmpty() && current.last().isDigit()) {
            _expression.value += ")"
        } else {
            _expression.value += "("
        }
        updateResultPreview()
    }

    fun onPositiveNegativeToggle() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "-"
        } else if (current.startsWith("-") && current.count { it == '-' } == 1 && !current.contains(Regex("[+×÷]"))) {
            _expression.value = current.drop(1)
        } else if (!current.startsWith("-") && !current.contains(Regex("[+×÷]"))) {
            _expression.value = "-$current"
        } else {
            // Wrap in minus
            _expression.value = "-($current)"
        }
        updateResultPreview()
    }

    fun onClear() {
        _expression.value = ""
        _resultPreview.value = ""
    }

    fun onBackspace() {
        val current = _expression.value
        if (current.isNotEmpty()) {
            // Check if backspacing a full scientific function
            val funcs = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sinh(", "cosh(", "tanh(", "sin(", "cos(", "tan(", "log(", "ln(", "mod")
            var removed = false
            for (f in funcs) {
                if (current.endsWith(f)) {
                    _expression.value = current.substring(0, current.length - f.length)
                    removed = true
                    break
                }
            }
            if (!removed) {
                _expression.value = current.dropLast(1)
            }
        }
        updateResultPreview()
    }

    fun toggleDegreeMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        updateResultPreview()
    }

    // --- Calculator Memory Functions ---
    fun onMemoryClear() {
        _memoryValue.value = 0.0
    }

    fun onMemoryRecall() {
        if (_memoryValue.value != 0.0) {
            _expression.value += decimalFormat.format(_memoryValue.value)
            updateResultPreview()
        }
    }

    fun onMemoryAdd() {
        try {
            val eval = MathEvaluator(_isDegreeMode.value).evaluate(_expression.value)
            _memoryValue.value += eval
        } catch (_: Exception) {}
    }

    fun onMemorySubtract() {
        try {
            val eval = MathEvaluator(_isDegreeMode.value).evaluate(_expression.value)
            _memoryValue.value -= eval
        } catch (_: Exception) {}
    }

    private fun updateResultPreview() {
        val current = _expression.value
        if (current.isBlank()) {
            _resultPreview.value = ""
            return
        }
        // Balance open parentheses temporarily for preview
        var exprToEval = current
        val openCount = exprToEval.count { it == '(' }
        val closeCount = exprToEval.count { it == ')' }
        if (openCount > closeCount) {
            exprToEval += ")".repeat(openCount - closeCount)
        }
        try {
            val res = MathEvaluator(_isDegreeMode.value).evaluate(exprToEval)
            if (res.isNaN() || res.isInfinite()) {
                _resultPreview.value = ""
            } else {
                _resultPreview.value = decimalFormat.format(res)
            }
        } catch (e: Exception) {
            _resultPreview.value = ""
        }
    }

    fun onCalculate() {
        val current = _expression.value
        if (current.isBlank()) return
        val finalResult: String
        var isSuccess = false
        try {
            val eval = MathEvaluator(_isDegreeMode.value).evaluate(current)
            if (eval.isNaN() || eval.isInfinite()) {
                finalResult = "Error"
            } else {
                finalResult = decimalFormat.format(eval)
                isSuccess = true
            }
        } catch (e: Exception) {
            _resultPreview.value = "Error"
            return
        }

        if (isSuccess) {
            val history = HistoryEntity(expression = current, result = finalResult)
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertHistory(history)
            }
            _expression.value = finalResult
            _resultPreview.value = ""
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistoryById(id)
        }
    }

    fun toggleFavoriteHistory(id: Long, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateHistoryFavorite(id, isFavorite)
        }
    }

    fun applyHistoryExpression(expr: String) {
        _expression.value = expr
        updateResultPreview()
    }

    // --- Voice input transcription Simulation ---
    fun simulateVoiceMathInput() {
        if (_isRecordingVoice.value) return
        _isRecordingVoice.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // simulated listing
            val sampleVoiceMaths = listOf(
                "sin(45) + cos(45)",
                "log(100) × 15",
                "50% ÷ 2",
                "√(256) + 12",
                "7! + (120 × 2)",
                "e^2 + π"
            )
            val randomMath = sampleVoiceMaths.random()
            _expression.value += randomMath
            updateResultPreview()
            _isRecordingVoice.value = false
        }
    }

    // --- Exchange Rate Screen States & Functions ---
    private val _isCurrencyLoading = MutableStateFlow(false)
    val isCurrencyLoading: StateFlow<Boolean> = _isCurrencyLoading.asStateFlow()

    private val _currencyRatesList = MutableStateFlow<List<CurrencyRateEntity>>(emptyList())
    val currencyRatesList: StateFlow<List<CurrencyRateEntity>> = _currencyRatesList.asStateFlow()

    val currencyRatesFromDb: StateFlow<List<CurrencyRateEntity>> = repository.allCurrencyRates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseCurrency = MutableStateFlow("USD")
    val targetCurrency = MutableStateFlow("INR")
    val currencyInputAmount = MutableStateFlow("1")
    val currencyOutputAmount = MutableStateFlow("83.45")

    fun fetchLiveRates() {
        _isCurrencyLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.service.getLatestRates(baseCurrency.value)
                if (response.result == "success") {
                    val entities = response.rates.map { (code, value) ->
                        CurrencyRateEntity(code, value)
                    }
                    repository.cacheCurrencyRates(entities)
                    _currencyRatesList.value = entities
                    calculateCurrencyConversion()
                }
            } catch (e: Exception) {
                // If offline, rely totally on Room database cache handled via Flow
            } finally {
                _isCurrencyLoading.value = false
            }
        }
    }

    fun swapCurrencies() {
        val temp = baseCurrency.value
        baseCurrency.value = targetCurrency.value
        targetCurrency.value = temp
        fetchLiveRates()
    }

    fun setCurrencyAmount(amount: String) {
        currencyInputAmount.value = amount
        calculateCurrencyConversion()
    }

    fun calculateCurrencyConversion() {
        val input = currencyInputAmount.value.toDoubleOrNull() ?: 0.0
        viewModelScope.launch(Dispatchers.IO) {
            val baseRateObj = repository.getRateByCode(baseCurrency.value)
            val targetRateObj = repository.getRateByCode(targetCurrency.value)
            if (baseRateObj != null && targetRateObj != null) {
                // Since our database stores factors relative to active base or baseline SDK (USD)
                // Let's standardise relative to BASE. If rates are fetched with custom base (e.g. USD)
                // Conversions are input * (targetRate / baseRate)
                val conversion = input * (targetRateObj.rate / baseRateObj.rate)
                currencyOutputAmount.value = String.format(Locale.US, "%.4f", conversion)
            } else {
                // Try remote API sync
                fetchLiveRates()
            }
        }
    }

    // --- Unit Converter Screen States & Functions ---
    val unitCategory = MutableStateFlow(UnitCategory.LENGTH)
    val findUnitSearchQuery = MutableStateFlow("")

    val selectedFromUnit = MutableStateFlow<UnitInfo?>(null)
    val selectedToUnit = MutableStateFlow<UnitInfo?>(null)
    val unitInputAmount = MutableStateFlow("1")
    val unitOutputAmount = MutableStateFlow("")

    init {
        // Assign default units for category
        updateDefaultUnits(UnitCategory.LENGTH)
    }

    fun setUnitCategory(category: UnitCategory) {
        unitCategory.value = category
        updateDefaultUnits(category)
    }

    private fun updateDefaultUnits(category: UnitCategory) {
        val units = UnitConverterUtil.categories[category] ?: return
        selectedFromUnit.value = units.getOrNull(0)
        selectedToUnit.value = units.getOrNull(1) ?: units.getOrNull(0)
        calculateUnitConversion()
    }

    fun swapUnitConversion() {
        val temp = selectedFromUnit.value
        selectedFromUnit.value = selectedToUnit.value
        selectedToUnit.value = temp
        calculateUnitConversion()
    }

    fun updateUnitInputAmount(amount: String) {
        unitInputAmount.value = amount
        calculateUnitConversion()
    }

    fun calculateUnitConversion() {
        val input = unitInputAmount.value.toDoubleOrNull() ?: 0.0
        val from = selectedFromUnit.value
        val to = selectedToUnit.value
        if (from != null && to != null) {
            val conversion = UnitConverterUtil.convert(input, from, to, unitCategory.value)
            unitOutputAmount.value = decimalFormat.format(conversion)
        }
    }

    // --- Additional Tools States & Functions ---
    // 1. BMI state
    val bmiHeight = MutableStateFlow("170") // cm
    val bmiWeight = MutableStateFlow("65")  // kg
    val bmiResult = MutableStateFlow("")
    val bmiCategory = MutableStateFlow("")

    fun calculateBMI() {
        val h = bmiHeight.value.toDoubleOrNull() ?: 0.0
        val w = bmiWeight.value.toDoubleOrNull() ?: 0.0
        if (h > 0 && w > 0) {
            val heightInMeters = h / 100.0
            val bmiValue = w / (heightInMeters * heightInMeters)
            bmiResult.value = String.format(Locale.US, "%.1f", bmiValue)
            bmiCategory.value = when {
                bmiValue < 18.5 -> "Underweight"
                bmiValue < 25.0 -> "Normal Weight"
                bmiValue < 30.0 -> "Overweight"
                else -> "Obese"
            }
        }
    }

    // 2. Age state
    val birthYear = MutableStateFlow(2000)
    val birthMonth = MutableStateFlow(1) // 1 to 12
    val birthDay = MutableStateFlow(1)
    val ageYears = MutableStateFlow("")
    val ageMonths = MutableStateFlow("")
    val ageDays = MutableStateFlow("")
    val nextBirthdayDays = MutableStateFlow("")

    fun calculateAge() {
        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply {
            set(birthYear.value, birthMonth.value - 1, birthDay.value)
        }

        if (birth.after(today)) {
            ageYears.value = "0"
            ageMonths.value = "0"
            ageDays.value = "0"
            return
        }

        var years = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - birth.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val prevMonth = (today.get(Calendar.MONTH) - 1 + 12) % 12
            val tempCal = Calendar.getInstance().apply { set(Calendar.MONTH, prevMonth) }
            days += tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        if (months < 0) {
            years--
            months += 12
        }

        ageYears.value = years.toString()
        ageMonths.value = months.toString()
        ageDays.value = days.toString()

        // Next Birthday calculation
        val nextBday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthMonth.value - 1)
            set(Calendar.DAY_OF_MONTH, birthDay.value)
            if (before(today) || today.equals(this)) {
                add(Calendar.YEAR, 1)
            }
        }
        val diffMs = nextBday.timeInMillis - today.timeInMillis
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
        nextBirthdayDays.value = diffDays.toString()
    }

    // 3. Percentage state
    val percentX = MutableStateFlow("15")
    val percentY = MutableStateFlow("200")
    val percentResultOf = MutableStateFlow("") // X% of Y
    val percentResultWhatPercent = MutableStateFlow("") // X is what % of Y
    val percentResultDiff = MutableStateFlow("") // Percentage change from X to Y

    fun calculatePercentages() {
        val x = percentX.value.toDoubleOrNull() ?: 0.0
        val y = percentY.value.toDoubleOrNull() ?: 0.0

        // 1. X% of Y
        val ofVal = (x / 100.0) * y
        percentResultOf.value = decimalFormat.format(ofVal)

        // 2. X is what % of Y
        if (y != 0.0) {
            val whatVal = (x / y) * 100.0
            percentResultWhatPercent.value = String.format(Locale.US, "%.2f%%", whatVal)
        } else {
            percentResultWhatPercent.value = "0%"
        }

        // 3. Diff from X to Y
        if (x != 0.0) {
            val diffVal = ((y - x) / x) * 100.0
            percentResultDiff.value = String.format(Locale.US, "%+.2f%%", diffVal)
        } else {
            percentResultDiff.value = "N/A"
        }
    }

    // 4. EMI state
    val emiPrincipal = MutableStateFlow("100000")
    val emiInterestRate = MutableStateFlow("8.5") // annual %
    val emiTenure = MutableStateFlow("12") // months
    val emiMonthlyPayment = MutableStateFlow("")
    val emiTotalInterest = MutableStateFlow("")
    val emiTotalPayable = MutableStateFlow("")

    fun calculateEMI() {
        val p = emiPrincipal.value.toDoubleOrNull() ?: 0.0
        val rAnnual = emiInterestRate.value.toDoubleOrNull() ?: 0.0
        val n = emiTenure.value.toDoubleOrNull() ?: 0.0

        if (p > 0 && rAnnual > 0 && n > 0) {
            val rMonthly = rAnnual / (12.0 * 100.0)
            val emi = p * rMonthly * Math.pow(1.0 + rMonthly, n) / (Math.pow(1.0 + rMonthly, n) - 1.0)
            val totalPayable = emi * n
            val totalInterest = totalPayable - p

            emiMonthlyPayment.value = String.format(Locale.US, "%.2f", emi)
            emiTotalInterest.value = String.format(Locale.US, "%.2f", totalInterest)
            emiTotalPayable.value = String.format(Locale.US, "%.2f", totalPayable)
        }
    }

    // 5. Tip state
    val tipBillAmount = MutableStateFlow("150")
    val tipPercentage = MutableStateFlow(15f) // 0-100 float slider
    val tipSplitCount = MutableStateFlow(2f) // slider 1-20
    val tipPerPerson = MutableStateFlow("")
    val tipTotalAmount = MutableStateFlow("")
    val tipTotalPerPerson = MutableStateFlow("")

    fun calculateTip() {
        val bill = tipBillAmount.value.toDoubleOrNull() ?: 0.0
        val pct = tipPercentage.value.toDouble()
        val split = tipSplitCount.value.toInt().coerceAtLeast(1)

        val totalTip = bill * (pct / 100.0)
        val totalBill = bill + totalTip
        val perPersonTip = totalTip / split
        val perPersonTotal = totalBill / split

        tipTotalAmount.value = String.format(Locale.US, "%.2f", totalTip)
        tipPerPerson.value = String.format(Locale.US, "%.2f", perPersonTip)
        tipTotalPerPerson.value = String.format(Locale.US, "%.2f", perPersonTotal)
    }

    // 6. Equation Solver state
    val eqA = MutableStateFlow("1")
    val eqB = MutableStateFlow("-5")
    val eqC = MutableStateFlow("6")
    val eqResult = MutableStateFlow("")

    fun solveEquations() {
        // We will solve ax^2 + bx + c = 0
        val a = eqA.value.toDoubleOrNull() ?: 0.0
        val b = eqB.value.toDoubleOrNull() ?: 0.0
        val c = eqC.value.toDoubleOrNull() ?: 0.0

        if (a == 0.0) {
            // Linear bx + c = 0 -> x = -c/b
            if (b == 0.0) {
                eqResult.value = if (c == 0.0) "Infinite Solutions" else "No Solution"
            } else {
                val x = -c / b
                eqResult.value = "Linear Root x = ${decimalFormat.format(x)}"
            }
        } else {
            // Quadratic
            val discriminant = b * b - 4 * a * c
            if (discriminant > 0.0) {
                val r1 = (-b + sqrt(discriminant)) / (2.0 * a)
                val r2 = (-b - sqrt(discriminant)) / (2.0 * a)
                eqResult.value = "Two Real Roots:\nx₁ = ${decimalFormat.format(r1)}\nx₂ = ${decimalFormat.format(r2)}"
            } else if (discriminant == 0.0) {
                val r = -b / (2.0 * a)
                eqResult.value = "One Double Root:\nx = ${decimalFormat.format(r)}"
            } else {
                // Complex roots
                val real = -b / (2.0 * a)
                val imag = sqrt(-discriminant) / (2.0 * a)
                eqResult.value = "Complex Roots:\nx₁ = ${decimalFormat.format(real)} + ${decimalFormat.format(imag)}i\nx₂ = ${decimalFormat.format(real)} - ${decimalFormat.format(imag)}i"
            }
        }
    }

    // 7. Graph Plotter state
    val graphEquation = MutableStateFlow("sin(x)") // functions like sin(x), x^2, cos(x)

    // Execute initial tools calculations
    init {
        calculateBMI()
        calculateAge()
        calculatePercentages()
        calculateEMI()
        calculateTip()
        solveEquations()
    }
}

class CalculatorViewModelFactory(private val repository: CalculatorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
