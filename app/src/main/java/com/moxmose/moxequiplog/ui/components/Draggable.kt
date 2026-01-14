package com.moxmose.moxequiplog.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun <T : Any> DraggableLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    items: List<T>,
    key: (index: Int, item: T) -> Any = { _, item -> item },
    onMove: (from: Int, to: Int) -> Unit,
    onDrop: () -> Unit,
    itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val spacing = 8.dp
    val spacingInPx = with(density) { spacing.toPx() }

    val dragDropState = remember(items) { DragDropState(onMove = onMove, onDrop = onDrop, spacing = spacingInPx) }

    LazyColumn(
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragDropState.onDrag(dragAmount, state)
                },
                onDragStart = { offset -> dragDropState.onDragStart(offset, state) },
                onDragEnd = { dragDropState.onDragEnd() },
                onDragCancel = { dragDropState.onDragEnd() }
            )
        },
        state = state,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        itemsIndexed(items, key = key) { index, item ->
            val offset by dragDropState.offsetOf(key(index, item))
            val isDragging = dragDropState.isDragging(key(index, item))
            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer { translationY = offset }
            ) {
                itemContent(index, item)
            }
        }
    }
}

class DragDropState(
    private val onMove: (from: Int, to: Int) -> Unit,
    private val onDrop: () -> Unit,
    private val spacing: Float
) {
    var draggedDistance by mutableFloatStateOf(0f)
        private set
    var draggedItemKey by mutableStateOf<Any?>(null)
        private set

    private var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)

    fun isDragging(itemKey: Any): Boolean = itemKey == draggedItemKey

    fun offsetOf(itemKey: Any): State<Float> = derivedStateOf {
        if (itemKey == draggedItemKey) {
            draggedDistance
        } else {
            0f
        }
    }

    fun onDragStart(offset: Offset, lazyListState: LazyListState) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { offset.y.toInt() in it.offset..(it.offset + it.size) }
            ?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
                draggedItemKey = it.key
            }
    }

    fun onDrag(dragAmount: Offset, lazyListState: LazyListState) {
        draggedDistance += dragAmount.y
        val initial = initiallyDraggedElement ?: return
        val currentDraggedIndex = currentIndexOfDraggedItem ?: return

        val currentOffset = initial.offset + draggedDistance

        val layoutInfo = lazyListState.layoutInfo
        val visibleItemsMap = layoutInfo.visibleItemsInfo.associateBy { it.index }

        val targetItem = when {
            dragAmount.y > 0 -> (currentDraggedIndex + 1 until layoutInfo.totalItemsCount)
                .asSequence()
                .mapNotNull { visibleItemsMap[it] }
                .firstOrNull { item -> currentOffset + initial.size > item.offset }

            dragAmount.y < 0 -> (0 until currentDraggedIndex).reversed()
                .asSequence()
                .mapNotNull { visibleItemsMap[it] }
                .firstOrNull { item -> currentOffset < item.offset + item.size }

            else -> null
        }

        if (targetItem != null) {
            val from = currentDraggedIndex
            val to = targetItem.index
            if (from != to) {
                onMove(from, to)
                val draggedItemSize = initial.size.toFloat()
                draggedDistance += if (from < to) {
                    - (draggedItemSize + spacing)
                } else {
                    draggedItemSize + spacing
                }
                initiallyDraggedElement = lazyListState.layoutInfo.visibleItemsInfo.find { it.index == to }
                currentIndexOfDraggedItem = to
            }
        }
    }

    fun onDragEnd() {
        onDrop()
        reset()
    }

    private fun reset() {
        draggedDistance = 0f
        initiallyDraggedElement = null
        currentIndexOfDraggedItem = null
        draggedItemKey = null
    }
}