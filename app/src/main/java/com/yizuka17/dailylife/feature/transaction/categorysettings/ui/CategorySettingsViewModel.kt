package com.yizuka17.dailylife.feature.transaction.categorysettings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.common.StringProvider
import com.yizuka17.dailylife.core.data.repository.CategoryEnabledResult
import com.yizuka17.dailylife.core.data.repository.DeleteCategoryResult
import com.yizuka17.dailylife.core.data.repository.TransactionCategoryDataRepository
import com.yizuka17.dailylife.core.ui.model.CategoryFlow
import com.yizuka17.dailylife.core.ui.model.TransactionCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CategorySettingsViewModel @Inject constructor(
    private val repository: TransactionCategoryDataRepository,
    private val stringProvider: StringProvider,
) : ViewModel() {
    private val selectedType = MutableStateFlow(CategoryFlow.EXPENSE)

    private val _uiState = MutableStateFlow(CategorySettingsUiState())
    val uiState: StateFlow<CategorySettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeededIfNeeded()
            selectedType.flatMapLatest { type ->
                repository.observeAllCategories(type)
            }.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun onTypeSelected(type: CategoryFlow) {
        selectedType.value = type
        _uiState.update { it.copy(selectedType = type) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.addCategory(name, _uiState.value.selectedType)
                .onSuccess { _events.emit(stringProvider.getString(R.string.category_settings_message_added)) }
                .onFailure { _events.emit(stringProvider.getString(R.string.category_settings_error_name_empty)) }
        }
    }

    fun renameCategory(categoryId: String, newName: String) {
        viewModelScope.launch {
            repository.renameCategory(categoryId, newName)
                .onSuccess { _events.emit(stringProvider.getString(R.string.category_settings_message_renamed)) }
                .onFailure { _events.emit(stringProvider.getString(R.string.category_settings_error_name_empty)) }
        }
    }

    fun setCategoryEnabled(categoryId: String, enabled: Boolean) {
        viewModelScope.launch {
            val message = when (val result = repository.setCategoryEnabled(categoryId, enabled)) {
                CategoryEnabledResult.Enabled -> stringProvider.getString(R.string.category_settings_message_enabled)
                CategoryEnabledResult.Disabled -> stringProvider.getString(R.string.category_settings_message_disabled)
                CategoryEnabledResult.NotFound -> stringProvider.getString(R.string.category_settings_error_not_found)
                is CategoryEnabledResult.HasTransactions -> stringProvider.getString(
                    R.string.category_settings_error_disable_has_transactions,
                    result.count,
                )
            }
            _events.emit(message)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val message = when (val result = repository.deleteCategory(categoryId)) {
                DeleteCategoryResult.Success -> stringProvider.getString(R.string.category_settings_message_deleted)
                DeleteCategoryResult.NotFound -> stringProvider.getString(R.string.category_settings_error_not_found)
                DeleteCategoryResult.BuiltinCategory -> stringProvider.getString(R.string.category_settings_error_builtin_delete)
                is DeleteCategoryResult.HasTransactions -> stringProvider.getString(
                    R.string.category_settings_error_has_transactions,
                    result.count,
                )
            }
            _events.emit(message)
        }
    }
}

data class CategorySettingsUiState(
    val selectedType: CategoryFlow = CategoryFlow.EXPENSE,
    val categories: List<TransactionCategory> = emptyList(),
)
