package com.moxmose.moxmybike.ui.bikes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.moxmose.moxmybike.data.local.Bike
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreen(viewModel: BikesViewModel = koinViewModel()) {
    val bikesFromDb by viewModel.allBikes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var bikes by remember { mutableStateOf<List<Bike>>(emptyList()) }
    LaunchedEffect(bikesFromDb) {
        bikes = bikesFromDb
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Bike")
            }
        }
    ) { paddingValues ->
        if (showDialog) {
            AddBikeDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = { description ->
                    viewModel.addBike(description)
                    showDialog = false
                }
            )
        }

        DraggableLazyColumn(
            items = bikes,
            key = { _, bike -> bike.id },
            onMove = { from, to ->
                bikes = bikes.toMutableList().apply {
                    add(to, removeAt(from))
                }
            },
            onDrop = {
                val reorderedBikes = bikes.mapIndexed { index, bike ->
                    bike.copy(displayOrder = index)
                }
                viewModel.updateBikes(reorderedBikes)
            },
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            itemContent = { _, bike ->
                BikeCard(
                    bike = bike,
                    viewModel = viewModel
                )
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : Any> DraggableLazyColumn(
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

    val dragDropState = remember(spacingInPx) { DragDropState(onMove = onMove, onDrop = onDrop, spacing = spacingInPx) }

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
    private val onMove: (from: Int, to: Int) -> Unit,
    private val onDrop: () -> Unit,
    private val spacing: Float
) {
    var draggedDistance by mutableStateOf(0f)
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
            // All'interno della funzione onDrag
                val from = currentDraggedIndex
                val to = targetItem.index
                if (from != to) {
                    // 1. Esegui lo scambio dei dati
                    onMove(from, to)

                    // 2. Calcola e applica la compensazione basandoti sulle dimensioni
                    val draggedItemSize = initiallyDraggedElement?.size ?: 0
                    draggedDistance += if (from < to) {
                        // Spostamento verso il basso: compensa all'indietro
                        -(draggedItemSize.toFloat() + spacing)
                    } else {
                        // Spostamento verso l'alto: compensa in avanti
                        (draggedItemSize.toFloat() + spacing)
                    }

                    // 3. AGGIORNA IL PUNTO DI RIFERIMENTO (LA CHIAVE DELLA SOLUZIONE)
                    // Troviamo l'informazione aggiornata dell'elemento che stiamo trascinando
                    // (che ora si trova all'indice 'to') e la salviamo come nuovo
                    // punto di riferimento per il prossimo calcolo.
                    initiallyDraggedElement = lazyListState.layoutInfo.visibleItemsInfo
                        .find { it.index == to }

                    // 4. Aggiorna l'indice corrente
                    currentIndexOfDraggedItem = to

            /*            val from = currentDraggedIndex
                        val to = targetItem.index
                        if (from != to) {
                            // Esegui lo scambio dei dati
                            onMove(from, to)
            //                draggedDistance += if (from < to) -(targetItem.size + spacing) else (targetItem.size + spacing)

                            // Ora compensa la distanza di trascinamento.
                            // Usiamo la dimensione dell'elemento che abbiamo iniziato a trascinare,
                            // che è un valore più stabile e affidabile.
                            // Lo spazio tra gli elementi (spacing) va aggiunto per un calcolo preciso.
                            val draggedItemSize = initiallyDraggedElement?.size ?: 0
                            draggedDistance += if (from < to) {
                                // Se mi sono spostato verso il basso (es: da indice 2 a 3), l'elemento "salta" in avanti.
                                // Devo compensare la draggedDistance all'indietro (negativamente).
                                -(draggedItemSize.toFloat() + spacing)
                            } else {
                                // Se mi sono spostato verso l'alto (es: da indice 3 a 2), l'elemento "salta" indietro.
                                // Devo compensare la draggedDistance in avanti (positivamente).
                                (draggedItemSize.toFloat() + spacing)
                            }

                            // Aggiorna l'indice dell'elemento trascinato
                            currentIndexOfDraggedItem = to*/
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

    private fun reset() {
        draggedDistance = 0f
        initiallyDraggedElement = null
        currentIndexOfDraggedItem = null
        draggedItemKey = null
    }
}

@Composable
fun AddBikeDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit) {
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add a new bike") },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Bike description") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (description.isNotBlank()) {
                        onConfirm(description)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BikeCard(bike: Bike, viewModel: BikesViewModel, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(bike.description) }

    LaunchedEffect(bike.description) {
        if (!isEditing) {
            editedDescription = bike.description
        }
    }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { editedDescription = it },
                    label = { Text("Bike Description") },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = bike.description,
                    modifier = Modifier.weight(1f)
                )
            }
            IconButton(
                onClick = {
                    if (isEditing) {
                        viewModel.updateBike(bike.copy(description = editedDescription))
                    }
                    isEditing = !isEditing
                }
            ) {
                Icon(
                    imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                    contentDescription = if (isEditing) "Save" else "Edit Bike"
                )
            }
        }
    }
}
