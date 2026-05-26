package com.yizuka17.dailylife.feature.transaction.editor.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.common.StringProvider
import com.yizuka17.dailylife.core.data.local.entity.TransactionEntity
import com.yizuka17.dailylife.core.data.repository.AssetAccountRepository
import com.yizuka17.dailylife.core.data.repository.TransactionCategoryDataRepository
import com.yizuka17.dailylife.core.data.repository.TransactionRepository
import com.yizuka17.dailylife.core.model.TransactionSource
import com.yizuka17.dailylife.core.ui.model.CategoryFlow
import com.yizuka17.dailylife.core.ui.model.MoodRepository
import com.yizuka17.dailylife.core.ui.model.TransactionCategoryRepository
import com.yizuka17.dailylife.feature.transaction.editor.model.TransactionEditorEvent
import com.yizuka17.dailylife.feature.transaction.editor.model.TransactionEditorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class TransactionEditorViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: TransactionCategoryDataRepository,
    private val assetAccountRepository: AssetAccountRepository,
    private val stringProvider: StringProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionEditorUiState())
    val uiState: StateFlow<TransactionEditorUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TransactionEditorEvent>()
    val events: SharedFlow<TransactionEditorEvent> = _events.asSharedFlow()

    private val editingTransactionId: Int? =
        savedStateHandle.get<Int>("transactionId")?.takeIf { it != -1 }

    private val initialCategoryId: String? =
        savedStateHandle.get<String>("categoryId")?.takeIf { it.isNotBlank() }

    private val initialIsExpense: Boolean =
        savedStateHandle.get<Boolean>("isExpense") ?: true

    private val categoryFlow = MutableStateFlow(if (initialIsExpense) CategoryFlow.EXPENSE else CategoryFlow.INCOME)

    private var originalTransaction: TransactionEntity? = null

    init {
        viewModelScope.launch {
            categoryRepository.ensureSeededIfNeeded()
            categoryFlow.flatMapLatest { flow -> categoryRepository.observeCategories(flow) }
                .collect { categories ->
                    _uiState.update { it.copy(categories = categories) }
                }
        }

        viewModelScope.launch {
            assetAccountRepository.ensureDefaultAccountsIfNeeded()
            assetAccountRepository.observeAccounts().collect { accounts ->
                _uiState.update { current ->
                    val selectedAccountId = current.selectedAccountId
                        ?.takeIf { id -> accounts.any { it.id == id } }
                        ?: accounts.firstOrNull { it.isDefault }?.id
                        ?: accounts.firstOrNull()?.id
                    current.copy(accounts = accounts, selectedAccountId = selectedAccountId)
                }
            }
        }

        editingTransactionId?.let { id ->
            loadTransaction(id)
        } ?: run {
            _uiState.update { current ->
                current.copy(
                    isExpense = initialIsExpense,
                    categoryId = initialCategoryId ?: current.categoryId
                )
            }
        }
    }

    private fun loadTransaction(transactionId: Int) {
        viewModelScope.launch {
            val transaction = repository.getTransactionById(transactionId).firstOrNull()
            transaction?.let { entity ->
                val normalizedCategoryId = TransactionCategoryRepository.normalizeCategoryId(entity.category)
                val sanitizedTransaction = if (normalizedCategoryId != entity.category) {
                    entity.copy(category = normalizedCategoryId)
                } else {
                    entity
                }
                originalTransaction = sanitizedTransaction

                val moodName = entity.mood?.let { score ->
                    MoodRepository.getMoodNameByScore(stringProvider, score)
                } ?: ""

                _uiState.update {
                    it.copy(
                        amount = formatAmountForInput(abs(entity.amount)),
                        categoryId = normalizedCategoryId,
                        description = entity.description,
                        date = entity.date,
                        isExpense = entity.amount < 0,
                        mood = moodName,
                        transactionId = entity.id,
                        selectedAccountId = entity.accountId,
                        isEditing = true
                    )
                }
                categoryFlow.value = if (entity.amount < 0) CategoryFlow.EXPENSE else CategoryFlow.INCOME
            }
        }
    }

    private fun formatAmountForInput(amount: Double): String {
        val df = DecimalFormat("0.##")
        df.isGroupingUsed = false
        return df.format(amount)
    }


    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onCategoryChange(categoryId: String) {
        _uiState.update { it.copy(categoryId = categoryId, error = null) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, error = null) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(date = date, error = null) }
    }

    fun onAccountChange(accountId: Int) {
        _uiState.update { it.copy(selectedAccountId = accountId, error = null) }
    }

    fun onMoodChange(mood: String) {
        val newMood = if (_uiState.value.mood == mood) "" else mood
        _uiState.update { it.copy(mood = newMood) }
    }


    fun onTransactionTypeChange(isExpense: Boolean) {
        categoryFlow.value = if (isExpense) CategoryFlow.EXPENSE else CategoryFlow.INCOME
        _uiState.update {
            it.copy(
                isExpense = isExpense,
                categoryId = "",
                error = null
            )
        }
    }

    fun onCategoriesReordered(categoryIds: List<String>) {
        viewModelScope.launch {
            categoryRepository.reorderCategories(categoryIds)
        }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val amountValue = currentState.amount.toDoubleOrNull()

            if (amountValue == null || amountValue == 0.0) {
                val error = stringProvider.getString(R.string.editor_error_invalid_amount)
                _uiState.update { it.copy(error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
                return@launch
            }

            if (currentState.categoryId.isBlank()) {
                val error = stringProvider.getString(R.string.editor_error_select_category)
                _uiState.update { it.copy(error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
                return@launch
            }

            if (currentState.selectedAccountId == null) {
                val error = stringProvider.getString(R.string.editor_error_select_account)
                _uiState.update { it.copy(error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
                return@launch
            }

            _uiState.update { it.copy(isSaving = true, error = null) }

            val transactionAmount = if (currentState.isExpense) -abs(amountValue) else abs(
                amountValue
            )

            val moodScore = currentState.mood.takeIf { it.isNotEmpty() }?.let {
                MoodRepository.getMoodScoreByName(stringProvider, it)
            }


            val normalizedSource = originalTransaction?.source?.takeUnless {
                TransactionSource.isAppSource(it)
            } ?: TransactionSource.DEFAULT

            val newTransaction = originalTransaction?.copy(
                amount = transactionAmount,
                category = currentState.categoryId,
                description = currentState.description,
                mood = moodScore,
                source = normalizedSource,
                date = currentState.date,
                accountId = currentState.selectedAccountId
            ) ?: TransactionEntity(
                amount = transactionAmount,
                category = currentState.categoryId,
                description = currentState.description,
                mood = moodScore,
                source = normalizedSource,
                date = currentState.date,
                accountId = currentState.selectedAccountId,
            )

            runCatching {
                if (currentState.isEditing && currentState.transactionId != null) {
                    repository.updateTransaction(newTransaction)
                } else {
                    repository.insertTransaction(newTransaction)
                }
            }.onSuccess {
                if (currentState.isEditing) {
                    originalTransaction = newTransaction
                } else {
                    originalTransaction = null
                }
                if (currentState.isEditing) {
                    _uiState.update { it.copy(isSaving = false, error = null) }
                } else {
                    _uiState.update {
                        it.copy(
                            amount = "",
                            categoryId = "",
                            description = "",
                            mood = "",
                            isSaving = false,
                            error = null
                        )
                    }
                }
                _events.emit(TransactionEditorEvent.SaveSuccess(savedAt = newTransaction.date))
            }.onFailure { throwable ->
                val error = throwable.message
                    ?: stringProvider.getString(R.string.editor_error_save_failed)
                _uiState.update { it.copy(isSaving = false, error = error) }
                _events.emit(TransactionEditorEvent.ShowMessage(error))
            }
        }
    }
}
