package com.moxmose.moxmybike.ui.bikes

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.moxmose.moxmybike.data.local.Bike
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreen(viewModel: BikesViewModel = koinViewModel()) {
    val activeBikes by viewModel.activeBikes.collectAsState()
    val allBikes by viewModel.allBikes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDismissed by remember { mutableStateOf(false) }

    var bikes by remember { mutableStateOf<List<Bike>>(emptyList()) }
    LaunchedEffect(activeBikes, allBikes, showDismissed) {
        bikes = if (showDismissed) allBikes else activeBikes
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Bike")
                }
                Spacer(modifier = Modifier.padding(8.dp))
                FloatingActionButton(
                    onClick = { showDismissed = !showDismissed },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = if (showDismissed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showDismissed) "Hide Dismissed" else "Show Dismissed"
                    )
                }
            }
        }
    ) { paddingValues ->
        if (showDialog) {
            AddBikeDialog(
                onDismissRequest = { showDialog = false },
                onConfirm = { description, photoUri ->
                    viewModel.addBike(description, photoUri)
                    showDialog = false
                }
            )
        }

        Column(Modifier.padding(paddingValues)) {
            Text(
                text = "Hold and drag items to change order",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
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
                modifier = Modifier.fillMaxSize(),
                itemContent = { _, bike ->
                    BikeCard(
                        bike = bike,
                        viewModel = viewModel
                    )
                }
            )
        }
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

    private fun reset() {
        draggedDistance = 0f
        initiallyDraggedElement = null
        currentIndexOfDraggedItem = null
        draggedItemKey = null
    }
}

@Composable
fun AddBikeDialog(onDismissRequest: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var description by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
                photoUri = it.toString()
            } catch (e: SecurityException) {
                Log.e("AddBikeDialog", "Failed to take persistable URI permission", e)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Add a new bike",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 50) description = it },
                    label = { Text("Bike description") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.padding(4.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { imagePickerLauncher.launch(arrayOf("image/*")) }
                ) {
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Add Image",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(description, photoUri)
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
fun FullImageDialog(photoUri: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        modifier = Modifier.padding(16.dp),
        text = {
            AsyncImage(
                model = photoUri,
                contentDescription = "Full-size bike photo",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        }
    )
}

@Composable
fun BikeCard(bike: Bike, viewModel: BikesViewModel, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(bike.description) }
    val context = LocalContext.current
    var showFullImageDialog by remember { mutableStateOf<String?>(null) }
    var showNoPictureDialog by remember { mutableStateOf(false) }

    val cardAlpha = if (bike.dismissed) 0.5f else 1f

    if (showNoPictureDialog) {
        AlertDialog(
            onDismissRequest = { showNoPictureDialog = false },
            title = { Text("No Image") },
            text = { Text("No picture loaded, add one.") },
            confirmButton = {
                TextButton(onClick = { showNoPictureDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    showFullImageDialog?.let { uri ->
        FullImageDialog(photoUri = uri, onDismiss = { showFullImageDialog = null })
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (e: SecurityException) {
                Log.e("BikeCard", "Failed to take persistable URI permission", e)
            }
            viewModel.updateBike(bike.copy(photoUri = it.toString()))
        }
    }

    LaunchedEffect(bike.description) {
        if (!isEditing) {
            editedDescription = bike.description
        }
    }

    val imageRequest = ImageRequest.Builder(context)
        .data(bike.photoUri)
        .crossfade(true)
        .build()

    val placeholderPainter = rememberVectorPainter(image = Icons.Filled.DirectionsBike)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .graphicsLayer(alpha = cardAlpha),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .then(if (isEditing) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                    .clickable {
                        if (isEditing) {
                            imagePickerLauncher.launch(arrayOf("image/*"))
                        } else {
                            if (bike.photoUri != null) {
                                showFullImageDialog = bike.photoUri
                            } else {
                                showNoPictureDialog = true
                            }
                        }
                    }
            ) {
                if (bike.photoUri != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = "Bike Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = placeholderPainter,
                        error = placeholderPainter
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsBike,
                            contentDescription = "Bike Photo",
                            modifier = Modifier.size(36.dp), // 10% smaller
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = editedDescription,
                    onValueChange = { if (it.length <= 50) editedDescription = it },
                    label = { Text("Bike Description") },
                    placeholder = {
                        Text(
                            text = "id:${bike.id} - no description",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            } else {
                Text(
                    text = if (editedDescription.isNotBlank()) editedDescription else "id:${bike.id} - no description",
                    color = if (editedDescription.isNotBlank()) LocalContentColor.current else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    IconButton(
                        onClick = { 
                            if (bike.dismissed) {
                                viewModel.restoreBike(bike)
                            } else {
                                viewModel.dismissBike(bike)
                            }
                         }
                    ) {
                        Icon(
                            imageVector = if (bike.dismissed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (bike.dismissed) "Restore Bike" else "Dismiss Bike"
                        )
                    }
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
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = "Drag to reorder"
                    )
                }
            }
        }
    }
}
