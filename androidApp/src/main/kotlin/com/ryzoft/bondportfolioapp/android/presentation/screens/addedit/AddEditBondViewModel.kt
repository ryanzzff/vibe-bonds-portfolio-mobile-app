package com.ryzoft.bondportfolioapp.android.presentation.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ryzoft.bondportfolioapp.shared.domain.model.Bond
import com.ryzoft.bondportfolioapp.shared.domain.model.BondType
import com.ryzoft.bondportfolioapp.shared.domain.model.PaymentFrequency
import com.ryzoft.bondportfolioapp.shared.domain.usecase.AddBondUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.GetBondDetailsUseCase
import com.ryzoft.bondportfolioapp.shared.domain.usecase.UpdateBondUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Interface for the Add/Edit Bond screen ViewModel
 */
interface AddEditBondViewModel {
    val uiState: StateFlow<AddEditBondUiState>
    
    fun initialize(bondId: Long?)
    fun updateName(name: String)
    fun updateIssuer(issuer: String)
    fun updateIsin(isin: String)
    fun updateBondType(bondType: BondType)
    fun updateFaceValue(value: String)
    fun updateQuantity(quantity: String)
    fun updatePurchasePrice(price: String)
    fun updateCouponRate(rate: String)
    fun updatePaymentFrequency(frequency: PaymentFrequency)
    fun updatePurchaseDate(date: LocalDate)
    fun updateMaturityDate(date: LocalDate)
    fun updateNotes(notes: String)
    fun validateForm(): Boolean
    fun saveBond(onComplete: () -> Unit)
}

/**
 * Implementation of the Add/Edit Bond screen ViewModel
 */
class AddEditBondViewModelImpl(
    private val addBondUseCase: AddBondUseCase,
    private val updateBondUseCase: UpdateBondUseCase,
    private val getBondDetailsUseCase: GetBondDetailsUseCase
) : ViewModel(), AddEditBondViewModel {

    private val _uiState = MutableStateFlow(AddEditBondUiState())
    override val uiState: StateFlow<AddEditBondUiState> = _uiState.asStateFlow()

    private var currentBondId: Long? = null

    /**
     * Initialize the ViewModel with an optional bond ID
     * @param bondId If not null, load the bond details for editing
     */
    override fun initialize(bondId: Long?) {
        currentBondId = bondId
        if (bondId != null) {
            // Edit mode
            _uiState.update { it.copy(
                isEditMode = true,
                isLoading = true,
                screenTitle = "Edit Bond"
            )}
            loadBondDetails(bondId)
        } else {
            // Add mode
            _uiState.update { it.copy(
                isEditMode = false,
                isLoading = false,
                screenTitle = "Add Bond"
            )}
        }
    }

    private fun loadBondDetails(bondId: Long) {
        viewModelScope.launch {
            val bond = getBondDetailsUseCase(bondId)
            if (bond != null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        name = bond.name ?: "",
                        issuer = bond.issuerName,
                        bondType = bond.bondType,
                        faceValuePerBond = bond.faceValuePerBond.toString(),
                        quantityPurchased = bond.quantityPurchased.toString(),
                        purchasePrice = bond.purchasePrice.toString(),
                        couponRate = (bond.couponRate * 100).toString(),
                        paymentFrequency = bond.paymentFrequency,
                        purchaseDate = bond.purchaseDate,
                        maturityDate = bond.maturityDate,
                        notes = bond.notes ?: "",
                        isin = bond.isin ?: ""
                    )
                }
            } else {
                // Handle case when bond is not found
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = false) }
    }

    override fun updateIssuer(issuer: String) {
        _uiState.update { it.copy(issuer = issuer) }
    }

    override fun updateIsin(isin: String) {
        _uiState.update { it.copy(isin = isin) }
    }

    override fun updateBondType(bondType: BondType) {
        _uiState.update { it.copy(bondType = bondType) }
    }

    override fun updateFaceValue(value: String) {
        _uiState.update { it.copy(faceValuePerBond = value, faceValueError = false) }
    }

    override fun updateQuantity(quantity: String) {
        _uiState.update { it.copy(quantityPurchased = quantity, quantityError = false) }
    }

    override fun updatePurchasePrice(price: String) {
        _uiState.update { it.copy(purchasePrice = price, purchasePriceError = false) }
    }

    override fun updateCouponRate(rate: String) {
        _uiState.update { it.copy(couponRate = rate, couponRateError = false) }
    }

    override fun updatePaymentFrequency(frequency: PaymentFrequency) {
        _uiState.update { it.copy(paymentFrequency = frequency) }
    }

    override fun updatePurchaseDate(date: LocalDate) {
        _uiState.update { it.copy(purchaseDate = date) }
    }

    override fun updateMaturityDate(date: LocalDate) {
        _uiState.update { it.copy(maturityDate = date) }
    }

    override fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    /**
     * Validates the form inputs
     * @return True if all required fields are valid, false otherwise
     */
    override fun validateForm(): Boolean {
        // Reset all validation errors
        _uiState.update { it.copy(
            nameError = false,
            faceValueError = false,
            quantityError = false,
            purchasePriceError = false,
            couponRateError = false,
            errorMessage = null
        )}
        
        var isValid = true
        
        // Validate name
        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = true) }
            isValid = false
        }
        
        // Issuer name is now optional, so no validation needed
        
        // Validate face value
        val faceValue = _uiState.value.faceValuePerBond.toDoubleOrNull()
        if (faceValue == null || faceValue <= 0) {
            _uiState.update { it.copy(faceValueError = true) }
            isValid = false
        }
        
        // Validate quantity
        val quantity = _uiState.value.quantityPurchased.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            _uiState.update { it.copy(quantityError = true) }
            isValid = false
        }
        
        // Validate purchase price
        val purchasePrice = _uiState.value.purchasePrice.toDoubleOrNull()
        if (purchasePrice == null || purchasePrice <= 0) {
            _uiState.update { it.copy(purchasePriceError = true) }
            isValid = false
        }
        
        // Validate coupon rate
        val couponRate = _uiState.value.couponRate.toDoubleOrNull()
        if (couponRate == null || couponRate < 0) {
            _uiState.update { it.copy(couponRateError = true) }
            isValid = false
        }
        
        return isValid
    }

    /**
     * Save the bond (either add new or update existing)
     * @param onComplete Callback to execute when saving is complete
     */
    override fun saveBond(onComplete: () -> Unit) {
        // Show validation errors if form is invalid
        if (!validateForm()) {
            _uiState.update { it.copy(showValidationErrors = true) }
            return
        }
        
        // Set saving state to show loading indicator
        _uiState.update { it.copy(isSaving = true) }
        
        val state = _uiState.value
        
        val bond = Bond(
            id = currentBondId ?: 0L,
            bondType = state.bondType,
            issuerName = state.issuer,
            faceValuePerBond = state.faceValuePerBond.toDoubleOrNull() ?: 0.0,
            quantityPurchased = state.quantityPurchased.toIntOrNull() ?: 0,
            purchasePrice = state.purchasePrice.toDoubleOrNull() ?: 0.0,
            couponRate = (state.couponRate.toDoubleOrNull() ?: 0.0) / 100, // Convert percentage to decimal
            paymentFrequency = state.paymentFrequency,
            purchaseDate = state.purchaseDate,
            maturityDate = state.maturityDate,
            currency = "USD", // Default currency as per requirements
            name = state.name.ifBlank { null },
            notes = state.notes.ifBlank { null },
            isin = state.isin.ifBlank { null }
        )
        
        viewModelScope.launch {
            try {
                if (state.isEditMode) {
                    updateBondUseCase(bond)
                } else {
                    addBondUseCase(bond)
                }
                _uiState.update { it.copy(isSaving = false) }
                onComplete()
            } catch (e: Exception) {
                // Handle error by showing error message
                _uiState.update { it.copy(
                    isSaving = false,
                    errorMessage = "Failed to save bond: ${e.localizedMessage ?: "Unknown error"}"
                )}
            }
        }
    }
}

/**
 * UI state for the Add/Edit Bond screen
 */
data class AddEditBondUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val screenTitle: String = "Add Bond",
    val showValidationErrors: Boolean = false,
    
    // Form fields
    val name: String = "",
    val issuer: String = "",
    val isin: String = "",
    val bondType: BondType = BondType.CORPORATE,
    val faceValuePerBond: String = "1000", // Set default face value to 1000
    val quantityPurchased: String = "",
    val purchasePrice: String = "",
    val couponRate: String = "",
    val paymentFrequency: PaymentFrequency = PaymentFrequency.SEMI_ANNUAL,
    val purchaseDate: LocalDate = today(),
    val maturityDate: LocalDate = today().plus(5, DateTimeUnit.YEAR),
    val notes: String = "",
    
    // Validation errors
    val nameError: Boolean = false,
    // issuerError removed as issuer is now optional
    val faceValueError: Boolean = false,
    val quantityError: Boolean = false,
    val purchasePriceError: Boolean = false,
    val couponRateError: Boolean = false,
    val errorMessage: String? = null
)

private fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
