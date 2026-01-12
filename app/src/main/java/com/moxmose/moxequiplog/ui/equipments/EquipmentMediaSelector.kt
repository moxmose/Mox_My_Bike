package com.moxmose.moxequiplog.ui.equipments

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.moxmose.moxequiplog.data.local.Category
import com.moxmose.moxequiplog.data.local.Media
import com.moxmose.moxequiplog.ui.options.EquipmentIconProvider

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EquipmentMediaSelector(
    photoUri: String?,
    iconIdentifier: String?,
    onMediaSelected: (String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    mediaLibrary: List<Media> = emptyList(),
    categories: List<Category> = emptyList(),
    onAddMedia: ((String, String) -> Unit)? = null,
    onRemoveMedia: ((String, String) -> Unit)? = null,
    onUpdateMediaOrder: ((List<Media>) -> Unit)? = null,
    onToggleMediaVisibility: ((String, String) -> Unit)? = null,
    onSetDefaultInCategory: ((String, String?, String?) -> Unit)? = null,
    isPhotoUsed: (suspend (String) -> Boolean)? = null,
    isPrefsMode: Boolean = false,
    forcedCategory: String? = null
) {
    var showPicker by remember { mutableStateOf(false) }
    var imageToDelete by remember { mutableStateOf<Media?>(null) }
    var showHidden by remember { mutableStateOf(isPrefsMode) }
    var currentFilterCategory by remember { mutableStateOf(forcedCategory ?: "ALL") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
                val uriString = it.toString()
                val targetCategory = if (currentFilterCategory == "ALL") "EQUIPMENT" else currentFilterCategory
                onAddMedia?.invoke(uriString, targetCategory)
                onMediaSelected(null, uriString)
            } catch (e: SecurityException) {
                Log.e("MediaSelector", "Failed to take permission", e)
            }
        }
    }

    // Confirm Delete Dialog
    imageToDelete?.let { media ->
        var isUsed by remember { mutableStateOf(false) }
        LaunchedEffect(media.uri) { isUsed = isPhotoUsed?.invoke(media.uri) ?: false }
        val categoryName = categories.find { it.id == media.category }?.name ?: media.category

        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("Elimina immagine") },
            text = {
                Column {
                    Text("Eliminare definitivamente dalla libreria di '$categoryName'?")
                    if (isUsed) {
                        Spacer(Modifier.height(8.dp))
                        Text("ATTENZIONE: In uso!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveMedia?.invoke(media.uri, media.category)
                        if (photoUri == media.uri) onMediaSelected(null, null)
                        imageToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Elimina") }
            },
            dismissButton = { TextButton(onClick = { imageToDelete = null }) { Text("Annulla") } }
        )
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { 
                Text(
                    text = if (isPrefsMode) "Gestione Libreria" else "Seleziona Media",
                    modifier = Modifier.fillMaxWidth(), 
                    textAlign = TextAlign.Center 
                ) 
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(550.dp)) {
                    if (forcedCategory == null || isPrefsMode) {
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (currentFilterCategory == "ALL") "Tutte le categorie" else categories.find { it.id == currentFilterCategory }?.name ?: currentFilterCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filtra per Categoria") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Tutte le categorie") },
                                    onClick = { currentFilterCategory = "ALL"; dropdownExpanded = false }
                                )
                                categories.sortedBy { it.name }.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.name) },
                                        onClick = { currentFilterCategory = cat.id; dropdownExpanded = false }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { imagePickerLauncher.launch(arrayOf("image/*")) }, 
                            modifier = Modifier.weight(1f),
                            enabled = currentFilterCategory != "ALL" || forcedCategory != null
                        ) {
                            Icon(Icons.Default.AddAPhoto, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Importa")
                        }
                        
                        IconButton(
                            onClick = { showHidden = !showHidden },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (showHidden) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = if (showHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = if (showHidden) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(8.dp))

                    val isMixedMode = currentFilterCategory == "ALL"
                    if (isMixedMode && isPrefsMode) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(8.dp))
                                Text("Clicca un elemento per impostarlo come default.", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(Modifier.width(8.dp))
                                Text("Drag & drop e importazione non disponibili. Seleziona una categoria per abilitarli.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(8.dp))
                                val text = if (isPrefsMode) "Clicca per default, trascina per ordinare, importa nuove immagini." else "Tocca per selezionare, trascina per ordinare."
                                Text(text, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))

                    val filteredAndSortedMedia = mediaLibrary
                        .filter { media ->
                            val matchesCategory = currentFilterCategory == "ALL" || media.category == currentFilterCategory
                            matchesCategory && (showHidden || !media.hidden)
                        }
                        .sortedWith(compareBy({ it.category }, { it.displayOrder }))

                    DraggableMediaGrid(
                        items = filteredAndSortedMedia,
                        onMove = { from, to ->
                            if (onUpdateMediaOrder != null && currentFilterCategory != "ALL") {
                                val newList = filteredAndSortedMedia.toMutableList().apply { add(to, removeAt(from)) }
                                onUpdateMediaOrder(newList.mapIndexed { index, m -> m.copy(displayOrder = index) })
                            }
                        },
                        itemContent = { media ->
                            val uriKey = media.uri.removePrefix("icon:")
                            val cat = categories.find { it.id == media.category }
                            val catColorHex = cat?.color ?: "#808080"
                            val catColor = Color(android.graphics.Color.parseColor(catColorHex))
                            
                            val isSelected = if (isPrefsMode) {
                                if (uriKey == "none") {
                                    cat?.defaultIconIdentifier == null && cat?.defaultPhotoUri == null
                                } else {
                                    (media.mediaType == "ICON" && uriKey == cat?.defaultIconIdentifier) ||
                                    (media.mediaType == "IMAGE" && media.uri == cat?.defaultPhotoUri)
                                }
                            } else {
                                if (uriKey == "none") {
                                    photoUri == null && iconIdentifier == null
                                } else {
                                    (media.mediaType == "ICON" && iconIdentifier == uriKey) ||
                                    (media.mediaType == "IMAGE" && photoUri == media.uri)
                                }
                            }

                            MediaGridItem(
                                media = media,
                                categoryColor = catColor,
                                isSelected = isSelected,
                                isHidden = media.hidden,
                                isManagementMode = isPrefsMode || forcedCategory == null,
                                onSelect = {
                                    if (isPrefsMode) {
                                        if (media.mediaType == "ICON") {
                                            onSetDefaultInCategory?.invoke(media.category, if(uriKey == "none") null else uriKey, null)
                                        } else {
                                            onSetDefaultInCategory?.invoke(media.category, null, media.uri)
                                        }
                                    } else {
                                        if (media.mediaType == "ICON") {
                                            if (uriKey == "none") onMediaSelected(null, null)
                                            else onMediaSelected(uriKey, null)
                                        } else {
                                            onMediaSelected(null, media.uri)
                                        }
                                        showPicker = false
                                    }
                                },
                                onToggleVisibility = {
                                    onToggleMediaVisibility?.invoke(media.uri, media.category)
                                },
                                onDelete = { if (media.mediaType == "IMAGE") imageToDelete = media }
                            )
                        }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text("Chiudi") } }
        )
    }

    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable { showPicker = true },
        contentAlignment = Alignment.Center
    ) {
        when {
            photoUri != null -> {
                AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            }
            iconIdentifier != null -> {
                Icon(imageVector = EquipmentIconProvider.getIcon(iconIdentifier, forcedCategory ?: "EQUIPMENT"), contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            }
            else -> {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.NotInterested, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Nothing", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun MediaGridItem(
    media: Media,
    categoryColor: Color,
    isSelected: Boolean,
    isHidden: Boolean,
    isManagementMode: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit
) {
    Box(contentAlignment = Alignment.TopEnd) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 5.dp else 2.dp,
                    color = if (isSelected) categoryColor else categoryColor.copy(alpha = 0.4f),
                    shape = CircleShape
                )
                .clickable { onSelect() }
                .graphicsLayer(alpha = if (isHidden) 0.5f else 1f),
            contentAlignment = Alignment.Center
        ) {
            val uriKey = media.uri.removePrefix("icon:")
            if (media.mediaType == "ICON") {
                if (uriKey == "none") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.NotInterested, null, modifier = Modifier.size(24.dp), tint = if (isHidden) MaterialTheme.colorScheme.onSurface.copy(0.3f) else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Nothing", style = MaterialTheme.typography.labelSmall, color = if (isHidden) MaterialTheme.colorScheme.onSurface.copy(0.3f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Icon(
                        imageVector = EquipmentIconProvider.getIcon(uriKey, media.category),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = if (isHidden) MaterialTheme.colorScheme.onSurface.copy(0.3f) else MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                AsyncImage(model = media.uri, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
            }
        }

        if (isManagementMode) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(0.8f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                ) {
                    IconButton(onClick = onToggleVisibility, modifier = Modifier.size(22.dp)) {
                        Icon(if (isHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, Modifier.size(12.dp))
                    }
                    if (media.mediaType == "IMAGE") {
                        IconButton(onClick = onDelete, modifier = Modifier.size(22.dp)) {
                            Icon(Icons.Default.Delete, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DraggableMediaGrid(
    items: List<Media>,
    onMove: (Int, Int) -> Unit,
    itemContent: @Composable (Media) -> Unit
) {
    val gridState = rememberLazyGridState()
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingOffset by remember { mutableStateOf(Offset.Zero) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.pointerInput(items) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset ->
                    gridState.layoutInfo.visibleItemsInfo
                        .firstOrNull { offset.y.toInt() in it.offset.y..(it.offset.y + it.size.height) && 
                                      offset.x.toInt() in it.offset.x..(it.offset.x + it.size.width) }
                        ?.let { draggingItemIndex = it.index }
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    draggingOffset += dragAmount
                    val currentIndex = draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                    
                    gridState.layoutInfo.visibleItemsInfo
                        .firstOrNull { item ->
                            val center = Offset(item.offset.x + item.size.width / 2f, item.offset.y + item.size.height / 2f)
                            (change.position - center).getDistance() < item.size.width / 2f
                        }?.let { target ->
                            if (currentIndex != target.index) {
                                onMove(currentIndex, target.index)
                                draggingItemIndex = target.index
                            }
                        }
                },
                onDragEnd = { draggingItemIndex = null; draggingOffset = Offset.Zero },
                onDragCancel = { draggingItemIndex = null; draggingOffset = Offset.Zero }
            )
        }
    ) {
        itemsIndexed(items, key = { _, m -> "${m.category}:${m.uri}" }) { index, media ->
            Box(modifier = Modifier.zIndex(if (draggingItemIndex == index) 1f else 0f)) {
                itemContent(media)
            }
        }
    }
}
