package com.yizuka17.dailylife.feature.transaction.editor.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.yizuka17.dailylife.core.ui.model.TransactionCategory
import kotlinx.coroutines.launch

/**
 * 支出/收入切换标签。
 */
@Composable
fun TransactionTypeTabs(
    isExpense: Boolean,
    expenseLabel: String,
    incomeLabel:String,
    onTransactionTypeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SecondaryTabRow (
        modifier = modifier,
        selectedTabIndex = if (isExpense) 0 else 1
    ) {
        Tab(
            selected = isExpense,
            onClick = { onTransactionTypeChange(true) },
            text = { Text(expenseLabel) },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Tab(
            selected = !isExpense,
            onClick = { onTransactionTypeChange(false) },
            text = { Text(incomeLabel) },
            selectedContentColor = MaterialTheme.colorScheme.primary,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 分类选择网格。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionCategoryGrid(
    categories: List<TransactionCategory>,
    selectedCategoryId: String,
    onCategorySelected: (TransactionCategory) -> Unit,
    onManageCategory: () -> Unit,
    manageLabel: String,
    modifier: Modifier = Modifier,
    manageIcon: ImageVector = Icons.Filled.Settings,
    onCategoriesReordered: (List<String>) -> Unit = {},
) {
    val orderedCategories = remember { mutableStateListOf<TransactionCategory>() }
    val itemBounds = remember { mutableStateMapOf<String, Rect>() }
    var containerBounds by remember { mutableStateOf<Rect?>(null) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var draggingCategoryId by remember { mutableStateOf<String?>(null) }
    var dragStartCategoryBounds by remember { mutableStateOf<Rect?>(null) }
    var dragStartOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasDraggedCategoryMoved by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(categories.map { it.id }) {
        if (draggingCategoryId == null) {
            orderedCategories.clear()
            orderedCategories.addAll(categories)
        }
    }

    fun moveCategory(draggingId: String, targetId: String) {
        if (draggingId == targetId) return
        val fromIndex = orderedCategories.indexOfFirst { it.id == draggingId }
        val toIndex = orderedCategories.indexOfFirst { it.id == targetId }
        if (fromIndex == -1 || toIndex == -1) return
        val item = orderedCategories.removeAt(fromIndex)
        orderedCategories.add(toIndex, item)
        hasDraggedCategoryMoved = true
    }

    fun targetCategoryIdFor(draggingId: String, draggedCenter: Offset): String? {
        return itemBounds.entries
            .filter { (id, _) -> id != draggingId }
            .mapNotNull { (id, bounds) ->
                val horizontalThreshold = bounds.width * 0.62f
                val verticalThreshold = bounds.height * 0.62f
                val centerDistance = (bounds.center - draggedCenter).getDistance()
                val isInSwapArea = kotlin.math.abs(bounds.center.x - draggedCenter.x) <= horizontalThreshold &&
                    kotlin.math.abs(bounds.center.y - draggedCenter.y) <= verticalThreshold
                if (isInSwapArea) id to centerDistance else null
            }
            .minByOrNull { it.second }
            ?.first
    }

    fun autoScrollIfNeeded(pointerPositionInWindow: Offset) {
        val viewportBounds = gridState.layoutInfo.viewportBoundsInWindow(itemBounds.values)
        if (viewportBounds == null) return
        val edgeSize = 72f
        val scrollDelta = when {
            pointerPositionInWindow.y < viewportBounds.top + edgeSize -> -18f
            pointerPositionInWindow.y > viewportBounds.bottom - edgeSize -> 18f
            else -> 0f
        }
        if (scrollDelta != 0f) {
            coroutineScope.launch { gridState.scrollBy(scrollDelta) }
        }
    }

    Box(modifier = modifier.onGloballyPositioned { coordinates ->
        containerBounds = coordinates.boundsInWindow()
    }) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = draggingCategoryId == null,
            modifier = Modifier.fillMaxSize()
        ) {
        items(
            items = orderedCategories,
            key = { it.id }
        ) { category ->
            CategoryItem(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category) },
                modifier = Modifier
                    .animateItem()
                    .alpha(if (draggingCategoryId == category.id) 0f else 1f)
                    .onGloballyPositioned { coordinates ->
                        itemBounds[category.id] = coordinates.boundsInWindow()
                    }
                    .pointerInput(category.id) {
                        var pointerPositionInWindow = Offset.Zero
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startOffset ->
                                draggingCategoryId = category.id
                                dragStartCategoryBounds = itemBounds[category.id]
                                dragStartOrder = orderedCategories.map { it.id }
                                hasDraggedCategoryMoved = false
                                dragOffset = Offset.Zero
                                pointerPositionInWindow = (itemBounds[category.id]?.topLeft ?: Offset.Zero) + startOffset
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDragEnd = {
                                val reorderedIds = orderedCategories.map { it.id }
                                val shouldPersist = hasDraggedCategoryMoved && reorderedIds != dragStartOrder
                                draggingCategoryId = null
                                dragStartCategoryBounds = null
                                dragStartOrder = emptyList()
                                dragOffset = Offset.Zero
                                hasDraggedCategoryMoved = false
                                if (shouldPersist) {
                                    onCategoriesReordered(reorderedIds)
                                }
                            },
                            onDragCancel = {
                                val reorderedIds = orderedCategories.map { it.id }
                                val shouldPersist = hasDraggedCategoryMoved && reorderedIds != dragStartOrder
                                draggingCategoryId = null
                                dragStartCategoryBounds = null
                                dragStartOrder = emptyList()
                                dragOffset = Offset.Zero
                                hasDraggedCategoryMoved = false
                                if (shouldPersist) {
                                    onCategoriesReordered(reorderedIds)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset = dragOffset + dragAmount
                                pointerPositionInWindow = pointerPositionInWindow + dragAmount
                                autoScrollIfNeeded(pointerPositionInWindow)
                                val draggingId = draggingCategoryId ?: category.id
                                val draggedCenter = (dragStartCategoryBounds?.center ?: pointerPositionInWindow) + dragOffset
                                val targetId = targetCategoryIdFor(draggingId, draggedCenter)
                                if (targetId != null) {
                                    moveCategory(draggingId, targetId)
                                }
                            },
                        )
                    },
            )
        }

        item {
            CategoryItem(
                category = TransactionCategory(
                    id = "__manage__",
                    name = manageLabel,
                    icon = manageIcon
                ),
                isSelected = false,
                onClick = onManageCategory
            )
        }
    }

        val draggingCategory = draggingCategoryId?.let { id -> orderedCategories.firstOrNull { it.id == id } }
        val draggingItemBounds = dragStartCategoryBounds
        val gridBounds = containerBounds
        if (draggingCategory != null && draggingItemBounds != null && gridBounds != null) {
            CategoryItem(
                category = draggingCategory,
                isSelected = draggingCategory.id == selectedCategoryId,
                onClick = {},
                modifier = Modifier
                    .zIndex(10f)
                    .graphicsLayer {
                        translationX = draggingItemBounds.left - gridBounds.left + dragOffset.x
                        translationY = draggingItemBounds.top - gridBounds.top + dragOffset.y
                        scaleX = 1.04f
                        scaleY = 1.04f
                        shadowElevation = 0f
                        alpha = 0.98f
                    },
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridLayoutInfo.viewportBoundsInWindow(
    itemBounds: Collection<Rect>,
): Rect? {
    if (itemBounds.isEmpty()) return null
    val top = itemBounds.minOf { it.top }
    val bottom = top + (viewportEndOffset - viewportStartOffset)
    val left = itemBounds.minOf { it.left }
    val right = itemBounds.maxOf { it.right }
    return Rect(left, top, right, bottom)
}

/**
 * 备注与金额展示模块。
 */
@Composable
fun RemarkAmountCard(
    description: String,
    onDescriptionChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onRequestFocus: () -> Unit,
    placeholderText: String,
    currencySymbol: String,
    amountText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = 120.dp)
                    .clickableWithoutRipple(onClick = onRequestFocus)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = LocalContentColor.current
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (description.isEmpty()) {
                                Text(
                                    text = placeholderText,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    maxLines = 1
                                )
                            }
                            innerField()
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencySymbol,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = amountText,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurface,
                    softWrap = false,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
