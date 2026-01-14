package com.moxmose.moxequiplog.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> DraggableLazyColumn(
    modifier: Modifier = Modifier,
    items: List<T>,
    key: (index: Int, item: T) -> Any,
    onMove: (from: Int, to: Int) -> Unit,
    onDrop: () -> Unit,
    itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }

    val spacing = 16.dp
    val spacingInPx = with(LocalDensity.current) { spacing.toPx() }

    val dragDropState = remember { DragDropState(spacing = spacingInPx) }
    dragDropState.onMove = onMove
    dragDropState.onDrop = onDrop

    LaunchedEffect(items) {
        dragDropState.reset()
    }

    LazyColumn(
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragDropState.onDrag(dragAmount, lazyListState)

                    if (overscrollJob?.isActive != true) {
                        val overscroll = dragDropState.checkForOverscroll(lazyListState)
                        if (overscroll != 0f) {
                            overscrollJob = scope.launch {
                                lazyListState.scrollBy(overscroll)
                            }
                        } else {
                            overscrollJob?.cancel()
                        }
                    }
                },
                onDragStart = { offset -> dragDropState.onDragStart(offset, lazyListState) },
                onDragEnd = { dragDropState.onDragEnd() },
                onDragCancel = { dragDropState.onDragEnd() }
            )
        },
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        itemsIndexed(items, key) { index, item ->
            val currentKey = key(index, item)
            val isDragging = dragDropState.isDragging(currentKey)
            val offset by dragDropState.offsetOf(currentKey)

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

private class DragDropState(
    private val spacing: Float
) {
    var onMove: (from: Int, to: Int) -> Unit = { _, _ -> }
    var onDrop: () -> Unit = {}

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
                val draggedItemSize = initiallyDraggedElement?.size ?: 0
                draggedDistance += if (from < to) {
                    -(draggedItemSize.toFloat() + spacing)
                } else {
                    (draggedItemSize.toFloat() + spacing)
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

    fun checkForOverscroll(lazyListState: LazyListState): Float {
        val initial = initiallyDraggedElement ?: return 0f
        val startOffset = initial.offset + draggedDistance
        val endOffset = initial.offset + initial.size + draggedDistance

        return when {
            draggedDistance > 0 && endOffset > lazyListState.layoutInfo.viewportEndOffset -> draggedDistance * 0.05f
            draggedDistance < 0 && startOffset < lazyListState.layoutInfo.viewportStartOffset -> draggedDistance * 0.05f
            else -> 0f
        }
    }

    fun reset() {
        draggedDistance = 0f
        initiallyDraggedElement = null
        currentIndexOfDraggedItem = null
        draggedItemKey = null
    }
}