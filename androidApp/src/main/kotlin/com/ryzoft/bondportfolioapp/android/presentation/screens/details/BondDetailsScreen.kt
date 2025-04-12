package com.ryzoft.bondportfolioapp.android.presentation.screens.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ryzoft.bondportfolioapp.android.di.UseCaseProvider
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import java.text.NumberFormat
import java.util.Locale
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

/**
 * Bond Details Screen - Displays the detailed information about a specific bond
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BondDetailsScreen(
    bondId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: BondDetailsViewModel = createViewModel()
) {
    // Load bond details when the screen is first displayed
    LaunchedEffect(bondId) {
        viewModel.loadBondDetails(bondId)
    }
    
    // Observe the UI state
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bond Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Edit button - only enabled if bond is loaded
                    IconButton(
                        onClick = onEditClick,
                        enabled = uiState.bond != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Bond"
                        )
                    }
                    
                    // Delete button - only enabled if bond is loaded
                    IconButton(
                        onClick = { viewModel.toggleDeleteConfirmDialog(true) },
                        enabled = uiState.bond != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Bond"
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
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.bond != null -> {
                    BondDetailsContent(bond = uiState.bond!!)
                }
            }
        }
        
        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleDeleteConfirmDialog(false) },
                title = { Text("Delete Bond") },
                text = { Text("Are you sure you want to delete this bond from your portfolio?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteBond(onComplete = onBackClick)
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.toggleDeleteConfirmDialog(false) }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun BondDetailsContent(bond: Bond) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = bond.name ?: bond.issuerName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Basic Information
                DetailItem(label = "Issuer", value = bond.issuerName)
                if (bond.name != null && bond.name != bond.issuerName) {
                    DetailItem(label = "Bond Name", value = bond.name!!)
                }
                if (bond.isin != null) {
                    DetailItem(label = "ISIN", value = bond.isin!!)
                }
                DetailItem(label = "Type", value = getBondTypeDisplayName(bond.bondType))
                
                // Financial Details
                DetailItem(label = "Face Value Per Bond", value = formatCurrency(bond.faceValuePerBond))
                DetailItem(label = "Quantity", value = bond.quantityPurchased.toString())
                DetailItem(label = "Total Face Value", value = formatCurrency(bond.faceValuePerBond * bond.quantityPurchased))
                DetailItem(label = "Purchase Price", value = formatCurrency(bond.purchasePrice))
                DetailItem(label = "Total Investment", value = formatCurrency(bond.purchasePrice / 100 * bond.faceValuePerBond * bond.quantityPurchased))
                DetailItem(label = "Coupon Rate", value = formatPercentage(bond.couponRate))
                DetailItem(label = "Payment Frequency", value = getPaymentFrequencyDisplayName(bond.paymentFrequency))

                // Next Interest Payment Amount
                val paymentPerPeriod = bond.faceValuePerBond * bond.couponRate / when (bond.paymentFrequency) {
                    PaymentFrequency.SEMI_ANNUAL -> 2.0
                    PaymentFrequency.QUARTERLY -> 4.0
                    PaymentFrequency.ANNUAL -> 1.0
                    // Assuming ANNUAL if frequency is somehow null or unknown - adjust if needed
                    else -> 1.0 
                }
                val nextInterestPayment = paymentPerPeriod * bond.quantityPurchased
                DetailItem(label = "Next Interest Payment", value = formatCurrency(nextInterestPayment))
                
                // Dates
                DetailItem(label = "Purchase Date", value = formatDate(bond.purchaseDate))
                DetailItem(label = "Maturity Date", value = formatDate(bond.maturityDate))
                
                // Current Yield
                val currentYield = if (bond.purchasePrice > 0) {
                    (bond.couponRate * bond.faceValuePerBond) / bond.purchasePrice
                } else {
                    0.0
                }
                DetailItem(label = "Current Yield", value = formatPercentage(currentYield))
                
                // Notes (if any)
                if (!bond.notes.isNullOrBlank()) {
                    DetailItem(label = "Notes", value = bond.notes!!)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Helper function to create the ViewModel with proper dependencies
 */
@Composable
private fun createViewModel(): BondDetailsViewModel {
    val context = LocalContext.current
    val getBondDetailsUseCase = UseCaseProvider.provideGetBondDetailsUseCase(context)
    val deleteBondUseCase = UseCaseProvider.provideDeleteBondUseCase(context)
    return viewModel<BondDetailsViewModelImpl>(
        factory = BondDetailsViewModelFactory(
            getBondDetailsUseCase = getBondDetailsUseCase,
            deleteBondUseCase = deleteBondUseCase
        )
    )
}

// Helper functions
private fun formatCurrency(amount: Double): String {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    return currencyFormat.format(amount)
}

private fun formatPercentage(value: Double): String {
    // Use Locale.US to ensure consistent formatting (e.g., decimal point)
    return String.format(Locale.US, "%.2f%%", value * 100)
}

private fun formatDate(date: kotlinx.datetime.LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return formatter.format(date.toJavaLocalDate())
}

private fun getBondTypeDisplayName(bondType: BondType): String {
    return when (bondType) {
        BondType.TREASURY -> "Treasury"
        BondType.CORPORATE -> "Corporate"
        BondType.MUNICIPAL -> "Municipal"
        BondType.AGENCY -> "Agency"
    }
}

private fun getPaymentFrequencyDisplayName(frequency: PaymentFrequency): String {
    return when (frequency) {
        PaymentFrequency.ANNUAL -> "Annual"
        PaymentFrequency.SEMI_ANNUAL -> "Semi-Annual"
        PaymentFrequency.QUARTERLY -> "Quarterly"
        PaymentFrequency.MONTHLY -> "Monthly"
        PaymentFrequency.ZERO_COUPON -> "Zero Coupon"
    }
}
