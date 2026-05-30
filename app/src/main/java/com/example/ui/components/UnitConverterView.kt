package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CalculatorViewModel
import com.example.util.UnitCategory
import com.example.util.UnitConverterUtil
import com.example.util.UnitInfo

@Composable
fun UnitConverterView(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentCategory by viewModel.unitCategory.collectAsState()
    val fromUnit by viewModel.selectedFromUnit.collectAsState()
    val toUnit by viewModel.selectedToUnit.collectAsState()
    val inputAmount by viewModel.unitInputAmount.collectAsState()
    val outputAmount by viewModel.unitOutputAmount.collectAsState()

    var searchUnitText by remember { mutableStateOf("") }
    var fromDropdownExpanded by remember { mutableStateOf(false) }
    var toDropdownExpanded by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    val scrollState = rememberScrollState()

    // Helper map of categories with icons and display text
    val categoriesMeta = listOf(
        CategoryMeta(UnitCategory.LENGTH, "Length", Icons.Default.Straighten),
        CategoryMeta(UnitCategory.WEIGHT, "Weight", Icons.Default.Scale),
        CategoryMeta(UnitCategory.TEMPERATURE, "Temp", Icons.Default.DeviceThermostat),
        CategoryMeta(UnitCategory.SPEED, "Speed", Icons.Default.Speed),
        CategoryMeta(UnitCategory.AREA, "Area", Icons.Default.Layers),
        CategoryMeta(UnitCategory.VOLUME, "Volume", Icons.Default.LocalActivity), // can map to generalized cup
        CategoryMeta(UnitCategory.TIME, "Time", Icons.Default.Schedule),
        CategoryMeta(UnitCategory.PRESSURE, "Pressure", Icons.Default.Compress),
        CategoryMeta(UnitCategory.ENERGY, "Energy", Icons.Default.ElectricBolt),
        CategoryMeta(UnitCategory.DATA_STORAGE, "Data", Icons.Default.Storage)
    )

    // Retrieve units for active category
    val allUnits = UnitConverterUtil.categories[currentCategory] ?: emptyList()
    val filteredFromUnits = allUnits.filter { it.name.contains(searchUnitText, ignoreCase = true) }
    val filteredToUnits = allUnits.filter { it.name.contains(searchUnitText, ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        item {
            Column {
                Text(
                    text = "Unit Converter",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Instant conversion across 10 categories",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Categories Horiz Slider ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriesMeta.forEach { meta ->
                    val isSelected = meta.cat == currentCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setUnitCategory(meta.cat) },
                        label = { Text(text = meta.label) },
                        leadingIcon = { Icon(imageVector = meta.icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }
        }

        // --- Conversion Card Panels ---
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // LEFT side: FROM Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown trigger FROM
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { fromDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fromUnit?.name ?: "Select",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = fromDropdownExpanded,
                                onDismissRequest = { fromDropdownExpanded = false }
                            ) {
                                allUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(text = unit.name) },
                                        onClick = {
                                            viewModel.selectedFromUnit.value = unit
                                            viewModel.calculateUnitConversion()
                                            fromDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Input field
                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = {
                                viewModel.updateUnitInputAmount(it)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 20.sp,
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

                    // Tactical swap row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { viewModel.swapUnitConversion() },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Swap units",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // RIGHT side: TO Output
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dropdown trigger TO
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { toDropdownExpanded = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = toUnit?.name ?: "Select",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = toDropdownExpanded,
                                onDismissRequest = { toDropdownExpanded = false }
                            ) {
                                allUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(text = unit.name) },
                                        onClick = {
                                            viewModel.selectedToUnit.value = unit
                                            viewModel.calculateUnitConversion()
                                            toDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Out text representation
                        Text(
                            text = outputAmount,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        )

                        // Copy button
                        IconButton(onClick = {
                            if (outputAmount.isNotEmpty()) {
                                clipboardManager.setText(AnnotatedString(outputAmount))
                            }
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

        // --- Common conversion cheat-sheet list ---
        item {
            Text(
                text = "Unit Definitions & Equivalents",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(allUnits.size) { index ->
            val u = allUnits[index]
            val standardEquivalent = if (u.factor != 1.0) {
                "1 ${u.code} = ${u.factor} base"
            } else {
                "1 ${u.code} = Base standard unit"
            }
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = standardEquivalent, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class CategoryMeta(
    val cat: UnitCategory,
    val label: String,
    val icon: ImageVector
)
