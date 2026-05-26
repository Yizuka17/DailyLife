package com.yizuka17.dailylife.feature.transaction.details.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yizuka17.dailylife.core.data.local.entity.TransactionEntity
import com.yizuka17.dailylife.core.data.repository.AssetAccountRepository
import com.yizuka17.dailylife.core.data.repository.TransactionCategoryDataRepository
import com.yizuka17.dailylife.core.data.repository.TransactionRepository
import com.yizuka17.dailylife.feature.transaction.details.model.TransactionDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val categoryRepository: TransactionCategoryDataRepository,
    private val accountRepository: AssetAccountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionDetailsUiState())
    val uiState: StateFlow<TransactionDetailsUiState> = _uiState.asStateFlow()

    private val transactionId: Int = savedStateHandle.get<Int>("transactionId")!!

    private var detailsJob: Job? = null

    init {
        loadTransactionDetails()
    }

    private fun loadTransactionDetails() {
        detailsJob?.cancel()
        detailsJob = viewModelScope.launch {
            repository.getTransactionById(transactionId).collectLatest { transaction ->
                if (transaction != null) {
                    val categoryNamesById = loadCategoryName(transaction.category)
                    val accountName = loadAccountName(transaction.accountId)
                    _uiState.value = TransactionDetailsUiState(
                        transaction = transaction,
                        categoryNamesById = categoryNamesById,
                        accountName = accountName,
                        isLoading = false,
                    )
                } else {
                    if (_uiState.value.transaction != null) {
                        _uiState.value = TransactionDetailsUiState(error = "Transaction not found", isLoading = false)
                    }
                }
            }
        }
    }

    private suspend fun loadCategoryName(categoryId: String): Map<String, String> {
        return categoryRepository.getCategoriesByIds(listOf(categoryId))
            .associate { it.id to it.name }
    }

    private suspend fun loadAccountName(accountId: Int?): String? {
        return accountId?.let { id -> accountRepository.getAccountIncludingDeleted(id)?.name }
    }

    fun deleteTransaction(transaction: TransactionEntity, onDeleted: () -> Unit) {
        detailsJob?.cancel()
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            withContext(Dispatchers.Main) {
                onDeleted()
            }
        }
    }
}
