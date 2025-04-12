package com.ryzoft.bondportfolioapp.android.presentation.screens.addedit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzoft.bondportfolioapp.android.di.UseCaseProvider
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Add/Edit Bond Screen - Used for both adding a new bond and editing an existing bond
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBondScreen(
    bondId: Long?, // null means we're adding a new bond, non-null means we're editing
    onBackClick: () -> Unit,
    onSaveComplete: () -> Unit,
    viewModel: AddEditBondViewModel = createViewModel()
) {
    // Initialize the ViewModel with the bond ID
    LaunchedEffect(bondId) {
        viewModel.initialize(bondId)
    }
    
    // Observe the UI state
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle) },
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
                    IconButton(onClick = { viewModel.saveBond(onSaveComplete) }) {
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
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Bond Name
                    FormTextField(
                        label = "Bond Name *",
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        isError = uiState.nameError,
                        errorMessage = if (uiState.nameError) "Name is required" else ""
                    )
                    
                    // Issuer Name
                    FormTextField(
                        label = "Issuer Name",  // Removed the asterisk to indicate it's optional
                        value = uiState.issuer,
                        onValueChange = { viewModel.updateIssuer(it) },
                        isError = uiState.issuerError,
                        errorMessage = if (uiState.issuerError) "Issuer name is required" else ""
                    )
                    
                    // ISIN
                    FormTextField(
                        label = "ISIN/CUSIP",
                        value = uiState.isin,
                        onValueChange = { viewModel.updateIsin(it) },
                        isError = false,
                        errorMessage = ""
                    )
                    
                    // Bond Type
                    BondTypeDropdown(
                        selectedBondType = uiState.bondType,
                        onBondTypeSelected = { viewModel.updateBondType(it) }
                    )
                    
                    // Face Value
                    FormTextField(
                        label = "Face Value Per Bond *",
                        value = uiState.faceValuePerBond,
                        onValueChange = { viewModel.updateFaceValue(it) },
                        isError = uiState.faceValueError,
                        errorMessage = if (uiState.faceValueError) "Valid face value is required" else "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    // Quantity
                    FormTextField(
                        label = "Quantity Purchased *",
                        value = uiState.quantityPurchased,
                        onValueChange = { viewModel.updateQuantity(it) },
                        isError = uiState.quantityError,
                        errorMessage = if (uiState.quantityError) "Valid quantity is required" else "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    // Purchase Price
                    FormTextField(
                        label = "Purchase Price (per 100 face value) *",
                        value = uiState.purchasePrice,
                        onValueChange = { viewModel.updatePurchasePrice(it) },
                        isError = uiState.purchasePriceError,
                        errorMessage = if (uiState.purchasePriceError) "Valid purchase price is required" else "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    // Coupon Rate
                    FormTextField(
                        label = "Coupon Rate (%) *",
                        value = uiState.couponRate,
                        onValueChange = { viewModel.updateCouponRate(it) },
                        isError = uiState.couponRateError,
                        errorMessage = if (uiState.couponRateError) "Valid coupon rate is required" else "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    // Payment Frequency
                    PaymentFrequencyDropdown(
                        selectedFrequency = uiState.paymentFrequency,
                        onFrequencySelected = { viewModel.updatePaymentFrequency(it) }
                    )
                    
                    // Purchase Date
                    DatePickerField(
                        label = "Purchase Date *",
                        date = uiState.purchaseDate,
                        onDateSelected = { viewModel.updatePurchaseDate(it) }
                    )
                    
                    // Maturity Date
                    DatePickerField(
                        label = "Maturity Date *",
                        date = uiState.maturityDate,
                        onDateSelected = { viewModel.updateMaturityDate(it) }
                    )
                    
                    // Notes
                    FormTextField(
                        label = "Notes",
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        isError = false,
                        errorMessage = "",
                        singleLine = false,
                        maxLines = 5
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Save Button
                    Button(
                        onClick = { viewModel.saveBond(onSaveComplete) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Bond")
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BondTypeDropdown(
    selectedBondType: BondType,
    onBondTypeSelected: (BondType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val bondTypes = BondType.entries
    
    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedBondType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Bond Type *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(vertical = 4.dp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                bondTypes.forEach { bondType ->
                    DropdownMenuItem(
                        text = { Text(bondType.displayName) },
                        onClick = {
                            onBondTypeSelected(bondType)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFrequencyDropdown(
    selectedFrequency: PaymentFrequency,
    onFrequencySelected: (PaymentFrequency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val frequencies = PaymentFrequency.entries
    
    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedFrequency.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Payment Frequency *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .padding(vertical = 4.dp)
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                frequencies.forEach { frequency ->
                    DropdownMenuItem(
                        text = { Text(frequency.displayName) },
                        onClick = {
                            onFrequencySelected(frequency)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    
    // Convert kotlinx.datetime.LocalDate to java.time.LocalDate for formatting
    val javaLocalDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
    val formattedDate = formatter.format(javaLocalDate)
    
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formattedDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Select Date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        
        if (showDatePicker) {
            // Convert kotlinx.datetime.LocalDate to millis for DatePicker
            val millis = java.time.LocalDate
                .of(date.year, date.monthNumber, date.dayOfMonth)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = millis)
            
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedMillis ->
                                // Convert millis back to kotlinx.datetime.LocalDate
                                val instant = Instant.fromEpochMilliseconds(selectedMillis)
                                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                                onDateSelected(localDateTime.date)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// Helper function to create the ViewModel with proper dependencies
@Composable
private fun createViewModel(): AddEditBondViewModel {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    return viewModel(
        factory = AddEditBondViewModelFactory(
            addBondUseCase = UseCaseProvider.provideAddBondUseCase(context),
            updateBondUseCase = UseCaseProvider.provideUpdateBondUseCase(context),
            getBondDetailsUseCase = UseCaseProvider.provideGetBondDetailsUseCase(context)
        )
    )
}

// Extension properties for enum display names
private val BondType.displayName: String
    get() = when (this) {
        BondType.TREASURY -> "Treasury"
        BondType.CORPORATE -> "Corporate"
        BondType.MUNICIPAL -> "Municipal"
        BondType.AGENCY -> "Agency"
    }

private val PaymentFrequency.displayName: String
    get() = when (this) {
        PaymentFrequency.ANNUAL -> "Annual"
        PaymentFrequency.SEMI_ANNUAL -> "Semi-Annual"
        PaymentFrequency.QUARTERLY -> "Quarterly"
        PaymentFrequency.MONTHLY -> "Monthly"
        PaymentFrequency.ZERO_COUPON -> "Zero Coupon"
    }
