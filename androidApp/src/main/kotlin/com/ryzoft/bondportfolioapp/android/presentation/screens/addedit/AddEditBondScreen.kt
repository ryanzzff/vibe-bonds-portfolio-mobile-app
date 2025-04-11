package com.ryzoft.bondportfolioapp.android.presentation.screens.addedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * Add/Edit Bond Screen - Used for both adding a new bond and editing an existing bond
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBondScreen(
    bondId: Long?, // null means we're adding a new bond, non-null means we're editing
    onBackClick: () -> Unit,
    onSaveComplete: () -> Unit,
    // The ViewModel will be injected later
) {
    // These variables will be handled by the ViewModel later
    val isEditMode = bondId != null
    val screenTitle = if (isEditMode) "Edit Bond" else "Add Bond"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Save button
                    IconButton(onClick = { onSaveComplete() }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save Bond"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Form fields for bond details
                // In the actual implementation, these will be connected to the ViewModel state
                
                // Bond Name
                FormTextField(
                    label = "Bond Name *",
                    value = if (isEditMode) "Example Bond" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = ""
                )
                
                // ISIN
                FormTextField(
                    label = "ISIN",
                    value = if (isEditMode) "US123456AB12" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = ""
                )
                
                // Bond Type
                BondTypeDropdown()
                
                // Face Value
                FormTextField(
                    label = "Face Value Per Bond *",
                    value = if (isEditMode) "10000" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                // Quantity
                FormTextField(
                    label = "Quantity Purchased *",
                    value = if (isEditMode) "5" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                // Purchase Price
                FormTextField(
                    label = "Purchase Price Per Bond *",
                    value = if (isEditMode) "9800" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                // Coupon Rate
                FormTextField(
                    label = "Coupon Rate (%) *",
                    value = if (isEditMode) "5.5" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = "",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                // Payment Frequency
                PaymentFrequencyDropdown()
                
                // Issue Date
                FormTextField(
                    label = "Issue Date (MM/DD/YYYY)",
                    value = if (isEditMode) "01/15/2020" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = ""
                    // This will be replaced with a proper date picker in the actual implementation
                )
                
                // Maturity Date
                FormTextField(
                    label = "Maturity Date (MM/DD/YYYY) *",
                    value = if (isEditMode) "12/31/2030" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = ""
                    // This will be replaced with a proper date picker in the actual implementation
                )
                
                // Notes
                FormTextField(
                    label = "Notes",
                    value = if (isEditMode) "Example notes about this bond" else "",
                    onValueChange = { },
                    isError = false,
                    errorMessage = "",
                    singleLine = false,
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save Button
                Button(
                    onClick = { onSaveComplete() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Bond")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = isError,
            keyboardOptions = keyboardOptions,
            singleLine = singleLine,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (isError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BondTypeDropdown() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Corporate Bond") }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = selectedOption,
                onValueChange = {},
                label = { Text("Bond Type *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Corporate Bond", "Government Bond", "Municipal Bond", "Treasury Bond", "Zero-Coupon Bond").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentFrequencyDropdown() {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Semi-Annual") }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = selectedOption,
                onValueChange = {},
                label = { Text("Payment Frequency *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("Annual", "Semi-Annual", "Quarterly", "Monthly", "Zero-Coupon").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}
