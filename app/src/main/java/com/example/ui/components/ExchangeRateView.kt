package com.example.ui.components

import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGradientEnd
import com.example.ui.theme.AccentGradientStart
import com.example.ui.viewmodel.CalculatorViewModel

@Composable
fun ExchangeRateView(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val isCurrencyLoading by viewModel.isCurrencyLoading.collectAsState()
    val baseCurr by viewModel.baseCurrency.collectAsState()
    val targetCurr by viewModel.targetCurrency.collectAsState()
    val inputAmount by viewModel.currencyInputAmount.collectAsState()
    val outputAmount by viewModel.currencyOutputAmount.collectAsState()
    val ratesList by viewModel.currencyRatesFromDb.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var baseDropdownExpanded by remember { mutableStateOf(false) }
    var targetDropdownExpanded by remember { mutableStateOf(false) }

    // Unicode flag mapping helper
    val flagMap = mapOf(
        "USD" to "🇺🇸", "INR" to "🇮🇳", "EUR" to "🇪🇺", "GBP" to "🇬🇧",
        "JPY" to "🇯🇵", "AED" to "🇦🇪", "RUB" to "🇷🇺", "BTC" to "🪙",
        "AUD" to "🇦🇺", "CAD" to "🇨🇦", "CHF" to "🇨🇭", "CNY" to "🇨🇳",
        "NZD" to "🇳🇿", "SGD" to "🇸🇬", "ZAR" to "🇿🇦", "BRL" to "🇧🇷",
        "MXN" to "🇲🇽", "SAR" to "🇸🇦", "TRY" to "🇹🇷", "KRW" to "🇰🇷",
        "IDR" to "🇮🇩", "MYR" to "🇲🇾", "PHP" to "🇵🇭", "THB" to "🇹🇭"
    )

    fun getFlag(code: String): String = flagMap[code] ?: "🏳️"

    val popularRates = ratesList.filter { flagMap.containsKey(it.code) }
    val filteredRates = popularRates.filter {
        it.code.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section with Reload ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Currencies",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time live exchange converter",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.fetchLiveRates() },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                ) {
                    if (isCurrencyLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh rates")
                    }
                }
            }
        }

        // --- Conversion Input Fields ---
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Base input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Dropdown Base selection
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { baseDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = getFlag(baseCurr), fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = baseCurr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            // Source Dropdown Selector
                            DropdownMenu(
                                expanded = baseDropdownExpanded,
                                onDismissRequest = { baseDropdownExpanded = false }
                            ) {
                                popularRates.forEach { rate ->
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Text(text = getFlag(rate.code), fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = rate.code, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            viewModel.baseCurrency.value = rate.code
                                            viewModel.calculateCurrencyConversion()
                                            baseDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Text input field
                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { string ->
                                viewModel.setCurrencyAmount(string)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            placeholder = { Text("0.0", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        )
                    }

                    // Tactile Swap button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { viewModel.swapCurrencies() },
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd)),
                                    RoundedCornerShape(12.dp)
                                )
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap currencies",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Target input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Dropdown Target selection
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { targetDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = getFlag(targetCurr), fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = targetCurr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            // Target Dropdown Selector
                            DropdownMenu(
                                expanded = targetDropdownExpanded,
                                onDismissRequest = { targetDropdownExpanded = false }
                            ) {
                                popularRates.forEach { rate ->
                                    DropdownMenuItem(
                                        text = {
                                            Row {
                                                Text(text = getFlag(rate.code), fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = rate.code, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            viewModel.targetCurrency.value = rate.code
                                            viewModel.calculateCurrencyConversion()
                                            targetDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Conversion Display text
                        Text(
                            text = outputAmount,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        )
                    }
                }
            }
        }

        // --- Search bar for rate sheet listings ---
        item {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search currencies rates...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- Trending Currency stats ---
        item {
            Text(
                text = "Trending Market Pairs",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Grid of Trending Pairs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrendCard(
                    pair = "BTC / USD",
                    rate = if (ratesList.isNotEmpty()) {
                        val usd = ratesList.find { it.code == "USD" }?.rate ?: 1.0
                        val btc = ratesList.find { it.code == "BTC" }?.rate ?: 1.5e-5
                        String.format(Locale.US, "%.1f", usd / btc)
                    } else "67450.5",
                    change = "+2.4%",
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    pair = "EUR / USD",
                    rate = if (ratesList.isNotEmpty()) {
                        val eur = ratesList.find { it.code == "EUR" }?.rate ?: 0.92
                        String.format(Locale.US, "%.4f", 1.0 / eur)
                    } else "1.0854",
                    change = "-0.15%",
                    isPositive = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live Rate rates list header
        item {
            Text(
                text = "Popular Exchange Rates (Base: $baseCurr)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Lazy currency rates list
        if (filteredRates.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No rates matches '$searchQuery'", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filteredRates, key = { it.code }) { rate ->
                // Calculate rate relative to our current base
                val currentBaseRate = ratesList.find { it.code == baseCurr }?.rate ?: 1.0
                val relRate = rate.rate / currentBaseRate

                CurrencyRateRowItem(
                    flag = getFlag(rate.code),
                    code = rate.code,
                    rateVal = relRate,
                    baseSymbol = baseCurr
                )
            }
        }
    }
}

@Composable
fun TrendCard(
    pair: String,
    rate: String,
    change: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = pair, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$$rate", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = change,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
fun CurrencyRateRowItem(
    flag: String,
    code: String,
    rateVal: Double,
    baseSymbol: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = flag, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = code, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "1 $baseSymbol", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = String.format(Locale.US, "%.5f", rateVal),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
