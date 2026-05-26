package com.yizuka17.dailylife.feature.transaction.categorysettings.ui

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.ui.model.CategoryFlow
import com.yizuka17.dailylife.core.ui.model.TransactionCategory
import com.yizuka17.dailylife.core.ui.navigation.safePopBackStack
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class, ExperimentalFoundationApi::class)
@Composable
fun CategorySettingsScreen(
    navController: NavHostController,
    viewModel: CategorySettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var editingCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var deletingCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.category_settings_title),
        )

        SecondaryTabRow(selectedTabIndex = if (uiState.selectedType == CategoryFlow.EXPENSE) 0 else 1) {
            Tab(
                selected = uiState.selectedType == CategoryFlow.EXPENSE,
                onClick = { viewModel.onTypeSelected(CategoryFlow.EXPENSE) },
                text = { Text(text = stringResource(id = R.string.editor_tab_expense)) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Tab(
                selected = uiState.selectedType == CategoryFlow.INCOME,
                onClick = { viewModel.onTypeSelected(CategoryFlow.INCOME) },
                text = { Text(text = stringResource(id = R.string.editor_tab_income)) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RoundedColumn(modifier = Modifier.fillMaxWidth()) {
                    ItemTitle(text = stringResource(id = R.string.category_settings_manage_section))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(id = R.string.category_settings_manage_tip),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Button(onClick = { showAddDialog = true }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(id = R.string.category_settings_add))
                        }
                    }
                }
            }

            item {
                RoundedColumn(modifier = Modifier.fillMaxWidth()) {
                    ItemTitle(
                        text = if (uiState.selectedType == CategoryFlow.EXPENSE) {
                            stringResource(id = R.string.category_settings_expense_section)
                        } else {
                            stringResource(id = R.string.category_settings_income_section)
                        },
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        uiState.categories.forEach { category ->
                            CategoryRow(
                                category = category,
                                onEdit = { editingCategory = category },
                                onEnabledChange = { enabled ->
                                    viewModel.setCategoryEnabled(category.id, enabled)
                                },
                                onDelete = { deletingCategory = category },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryNameDialog(
            title = stringResource(id = R.string.category_settings_add_dialog_title),
            initialName = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                viewModel.addCategory(name)
                showAddDialog = false
            },
        )
    }

    editingCategory?.let { category ->
        CategoryNameDialog(
            title = stringResource(id = R.string.category_settings_rename_dialog_title),
            initialName = category.name,
            onDismiss = { editingCategory = null },
            onConfirm = { name ->
                viewModel.renameCategory(category.id, name)
                editingCategory = null
            },
        )
    }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text(text = stringResource(id = R.string.category_settings_delete_dialog_title)) },
            text = {
                Text(
                    text = stringResource(id = R.string.category_settings_delete_dialog_message, category.name),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category.id)
                        deletingCategory = null
                    },
                ) { Text(text = stringResource(id = R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) {
                    Text(text = stringResource(id = R.string.common_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    category: TransactionCategory,
    onEdit: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = if (category.isBuiltin) null else onDelete,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (category.isEnabled) {
                    stringResource(id = R.string.category_settings_enabled_tag)
                } else {
                    stringResource(id = R.string.category_settings_disabled_tag)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = category.isEnabled,
            onCheckedChange = onEnabledChange,
        )
        IconButton(onClick = onEdit) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.common_edit))
        }
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(20) },
                label = { Text(text = stringResource(id = R.string.category_settings_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }) {
                Text(text = stringResource(id = R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.common_cancel))
            }
        },
    )
}
