package com.yizuka17.dailylife.feature.assets.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.common.StringProvider
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.data.repository.AssetAccountRepository
import com.yizuka17.dailylife.feature.assets.model.AssetAccountEditorState
import com.yizuka17.dailylife.feature.assets.model.AssetsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AssetsViewModel @Inject constructor(
    private val repository: AssetAccountRepository,
    private val stringProvider: StringProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetsUiState())
    val uiState: StateFlow<AssetsUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultAccountsIfNeeded()
            repository.observeAccounts()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: stringProvider.getString(R.string.asset_error_load_failed),
                        )
                    }
                }
                .collect { accounts ->
                    _uiState.update {
                        it.copy(
                            accounts = accounts,
                            totalBalance = accounts.sumOf(AssetAccountEntity::balance),
                            isLoading = false,
                            error = null,
                        )
                    }
                }
        }
    }

    fun saveAccount(editorState: AssetAccountEditorState) {
        viewModelScope.launch {
            val balance = editorState.balance.toDoubleOrNull()
            when {
                editorState.name.isBlank() -> {
                    _messages.emit(stringProvider.getString(R.string.asset_error_name_empty))
                    return@launch
                }
                balance == null -> {
                    _messages.emit(stringProvider.getString(R.string.asset_error_invalid_balance))
                    return@launch
                }
            }

            runCatching {
                if (editorState.id == null) {
                    repository.createAccount(
                        name = editorState.name,
                        type = editorState.type,
                        balance = balance,
                        isDefault = editorState.isDefault,
                    )
                } else {
                    repository.updateAccount(
                        accountId = editorState.id,
                        name = editorState.name,
                        type = editorState.type,
                        balance = balance,
                        isDefault = editorState.isDefault,
                    )
                }
            }.onSuccess {
                _messages.emit(stringProvider.getString(R.string.asset_message_saved))
            }.onFailure { throwable ->
                _messages.emit(throwable.message ?: stringProvider.getString(R.string.asset_error_save_failed))
            }
        }
    }

    fun updateBalance(accountId: Int, balanceText: String) {
        viewModelScope.launch {
            val balance = balanceText.toDoubleOrNull()
            if (balance == null) {
                _messages.emit(stringProvider.getString(R.string.asset_error_invalid_balance))
                return@launch
            }
            repository.setBalance(accountId, balance)
            _messages.emit(stringProvider.getString(R.string.asset_message_balance_updated))
        }
    }

    fun reorderAccounts(accountIds: List<Int>) {
        viewModelScope.launch {
            repository.reorderAccounts(accountIds)
        }
    }

    fun deleteAccount(accountId: Int) {
        viewModelScope.launch {
            repository.softDeleteAccount(accountId)
            _messages.emit(stringProvider.getString(R.string.asset_message_deleted))
        }
    }
}
