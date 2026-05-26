package com.yizuka17.dailylife.feature.assets.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountType
import com.yizuka17.dailylife.feature.assets.model.AssetAccountEditorState
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    viewModel: AssetsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var editorState by remember { mutableStateOf<AssetAccountEditorState?>(null) }
    var deleteAccount by remember { mutableStateOf<AssetAccountEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.asset_title)) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editorState = AssetAccountEditorState() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.asset_add_account))
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.error != null -> Text(
                    text = uiState.error.orEmpty(),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> AssetsContent(
                    totalBalance = uiState.totalBalance,
                    accounts = uiState.accounts,
                    onEditAccount = { editorState = AssetAccountEditorState.fromAccount(it) },
                    onDeleteAccount = { deleteAccount = it },
                    onAccountsReordered = viewModel::reorderAccounts,
                )
            }
        }
    }

    editorState?.let { state ->
        AccountEditorDialog(
            state = state,
            onStateChange = { editorState = it },
            onDismiss = { editorState = null },
            onSave = {
                viewModel.saveAccount(it)
                editorState = null
            },
        )
    }

    deleteAccount?.let { account ->
        AlertDialog(
            onDismissRequest = { deleteAccount = null },
            title = { Text(stringResource(R.string.asset_delete_dialog_title)) },
            text = { Text(stringResource(R.string.asset_delete_dialog_message, account.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAccount(account.id)
                    deleteAccount = null
                }) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteAccount = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssetsContent(
    totalBalance: Double,
    accounts: List<AssetAccountEntity>,
    onEditAccount: (AssetAccountEntity) -> Unit,
    onDeleteAccount: (AssetAccountEntity) -> Unit,
    onAccountsReordered: (List<Int>) -> Unit,
) {
    val orderedAccounts = remember { mutableStateListOf<AssetAccountEntity>() }
    val itemBounds = remember { mutableStateMapOf<Int, Rect>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var containerBounds by remember { mutableStateOf<Rect?>(null) }
    var draggingAccountId by remember { mutableStateOf<Int?>(null) }
    var dragStartAccountBounds by remember { mutableStateOf<Rect?>(null) }
    var dragStartOrder by remember { mutableStateOf<List<Int>>(emptyList()) }
    var hasDraggedAccountMoved by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(accounts) {
        if (draggingAccountId == null) {
            orderedAccounts.clear()
            orderedAccounts.addAll(accounts)
        }
    }

    LaunchedEffect(draggingAccountId, accounts) {
        if (draggingAccountId == null && orderedAccounts != accounts) {
            orderedAccounts.clear()
            orderedAccounts.addAll(accounts)
        }
    }

    fun moveAccount(draggingId: Int, targetId: Int) {
        if (draggingId == targetId) return
        val fromIndex = orderedAccounts.indexOfFirst { it.id == draggingId }
        val toIndex = orderedAccounts.indexOfFirst { it.id == targetId }
        if (fromIndex == -1 || toIndex == -1) return
        val item = orderedAccounts.removeAt(fromIndex)
        orderedAccounts.add(toIndex, item)
        hasDraggedAccountMoved = true
    }

    fun targetAccountIdFor(draggingId: Int, draggedCenter: Offset): Int? {
        return itemBounds.entries
            .filter { (id, _) -> id != draggingId }
            .mapNotNull { (id, bounds) ->
                val thresholdHeight = bounds.height * 0.72f
                val centerDistance = kotlin.math.abs(bounds.center.y - draggedCenter.y)
                val isInSwapArea = draggedCenter.y >= bounds.top &&
                    draggedCenter.y <= bounds.bottom &&
                    centerDistance <= thresholdHeight
                if (isInSwapArea) id to centerDistance else null
            }
            .minByOrNull { it.second }
            ?.first
    }

    fun autoScrollIfNeeded(pointerPositionInWindow: Offset) {
        val viewportBounds = listState.layoutInfo.viewportBoundsInWindow(itemBounds.values)
        if (viewportBounds == null) return
        val edgeSize = 72f
        val scrollDelta = when {
            pointerPositionInWindow.y < viewportBounds.top + edgeSize -> -18f
            pointerPositionInWindow.y > viewportBounds.bottom - edgeSize -> 18f
            else -> 0f
        }
        if (scrollDelta != 0f) {
            coroutineScope.launch { listState.scrollBy(scrollDelta) }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            containerBounds = coordinates.boundsInWindow()
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = draggingAccountId == null,
        ) {
        item {
            TotalAssetCard(totalBalance = totalBalance)
        }
        items(orderedAccounts, key = { it.id }) { account ->
            AssetAccountCard(
                account = account,
                onEditAccount = { onEditAccount(account) },
                onDeleteAccount = { onDeleteAccount(account) },
                modifier = Modifier
                    .animateItem()
                    .alpha(if (draggingAccountId == account.id) 0f else 1f)
                    .onGloballyPositioned { coordinates ->
                        itemBounds[account.id] = coordinates.boundsInWindow()
                    }
                    .pointerInput(account.id) {
                        var pointerPositionInWindow = Offset.Zero
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startOffset ->
                                draggingAccountId = account.id
                                dragStartAccountBounds = itemBounds[account.id]
                                dragStartOrder = orderedAccounts.map { it.id }
                                hasDraggedAccountMoved = false
                                dragOffset = Offset.Zero
                                pointerPositionInWindow = (itemBounds[account.id]?.topLeft ?: Offset.Zero) + startOffset
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragEnd = {
                                val reorderedIds = orderedAccounts.map { it.id }
                                val shouldPersist = hasDraggedAccountMoved && reorderedIds != dragStartOrder
                                draggingAccountId = null
                                dragStartAccountBounds = null
                                dragStartOrder = emptyList()
                                dragOffset = Offset.Zero
                                hasDraggedAccountMoved = false
                                if (shouldPersist) {
                                    onAccountsReordered(reorderedIds)
                                }
                            },
                            onDragCancel = {
                                val reorderedIds = orderedAccounts.map { it.id }
                                val shouldPersist = hasDraggedAccountMoved && reorderedIds != dragStartOrder
                                draggingAccountId = null
                                dragStartAccountBounds = null
                                dragStartOrder = emptyList()
                                dragOffset = Offset.Zero
                                hasDraggedAccountMoved = false
                                if (shouldPersist) {
                                    onAccountsReordered(reorderedIds)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset = dragOffset + dragAmount
                                pointerPositionInWindow = pointerPositionInWindow + dragAmount
                                autoScrollIfNeeded(pointerPositionInWindow)
                                val draggingId = draggingAccountId ?: account.id
                                val draggedCenter = (dragStartAccountBounds?.center ?: pointerPositionInWindow) + dragOffset
                                val targetId = targetAccountIdFor(draggingId, draggedCenter)
                                if (targetId != null) {
                                    moveAccount(draggingId, targetId)
                                }
                            },
                        )
                    },
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        val draggingAccount = draggingAccountId?.let { id -> orderedAccounts.firstOrNull { it.id == id } }
        val draggingItemBounds = dragStartAccountBounds
        val listBounds = containerBounds
        if (draggingAccount != null && draggingItemBounds != null && listBounds != null) {
            AssetAccountCard(
                account = draggingAccount,
                onEditAccount = {},
                onDeleteAccount = {},
                modifier = Modifier
                    .zIndex(10f)
                    .graphicsLayer {
                        translationX = draggingItemBounds.left - listBounds.left + dragOffset.x
                        translationY = draggingItemBounds.top - listBounds.top + dragOffset.y
                        scaleX = 1.02f
                        scaleY = 1.02f
                        shadowElevation = 0f
                        alpha = 0.98f
                    },
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListLayoutInfo.viewportBoundsInWindow(
    itemBounds: Collection<Rect>,
): Rect? {
    if (itemBounds.isEmpty()) return null
    val top = itemBounds.minOf { it.top }
    val bottom = top + (viewportEndOffset - viewportStartOffset)
    val left = itemBounds.minOf { it.left }
    val right = itemBounds.maxOf { it.right }
    return Rect(left, top, right, bottom)
}

@Composable
private fun TotalAssetCard(totalBalance: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.asset_total_balance),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(totalBalance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun AssetAccountCard(
    account: AssetAccountEntity,
    onEditAccount: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = accountTypeIcon(account.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = account.name, style = MaterialTheme.typography.titleMedium)
                    if (account.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.asset_default_tag)) },
                        )
                    }
                }
                Text(
                    text = accountTypeLabel(account.type),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatCurrency(account.balance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            IconButton(onClick = onEditAccount) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = stringResource(R.string.common_edit))
            }
            IconButton(onClick = onDeleteAccount) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete))
            }
        }
    }
}

@Composable
private fun AccountEditorDialog(
    state: AssetAccountEditorState,
    onStateChange: (AssetAccountEditorState) -> Unit,
    onDismiss: () -> Unit,
    onSave: (AssetAccountEditorState) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (state.id == null) stringResource(R.string.asset_add_account)
                else stringResource(R.string.asset_edit_account)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onStateChange(state.copy(name = it)) },
                    label = { Text(stringResource(R.string.asset_account_name)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.balance,
                    onValueChange = { onStateChange(state.copy(balance = it)) },
                    label = { Text(stringResource(R.string.asset_account_balance)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                AssetAccountType.entries.forEach { type ->
                    val selected = state.type == type
                    AssistChip(
                        onClick = { onStateChange(state.copy(type = type)) },
                        label = {
                            Text(
                                text = if (selected) "✓ ${accountTypeLabel(type)}" else accountTypeLabel(type),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = accountTypeIcon(type),
                                contentDescription = null,
                                tint = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            labelColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            borderWidth = if (selected) 1.5.dp else 1.dp,
                        ),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.asset_set_default), modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.isDefault,
                        onCheckedChange = { onStateChange(state.copy(isDefault = it)) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(state) }) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun accountTypeLabel(type: AssetAccountType): String {
    return when (type) {
        AssetAccountType.BANK_CARD -> stringResource(R.string.asset_type_bank_card)
        AssetAccountType.CASH -> stringResource(R.string.asset_type_cash)
        AssetAccountType.ALIPAY -> stringResource(R.string.asset_type_alipay)
        AssetAccountType.WECHAT -> stringResource(R.string.asset_type_wechat)
        AssetAccountType.OTHER -> stringResource(R.string.asset_type_other)
    }
}

private fun accountTypeIcon(type: AssetAccountType): ImageVector {
    return when (type) {
        AssetAccountType.BANK_CARD -> Icons.Default.AccountBalanceWallet
        AssetAccountType.CASH -> Icons.Default.Payments
        AssetAccountType.ALIPAY -> Icons.Default.QrCode
        AssetAccountType.WECHAT -> Icons.Default.Phone
        AssetAccountType.OTHER -> Icons.Default.MoreHoriz
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.CHINA).format(amount)
}
