package com.moxmose.moxequiplog.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moxmose.moxequiplog.BuildConfig
import com.moxmose.moxequiplog.R
import com.moxmose.moxequiplog.data.local.AppColor
import com.moxmose.moxequiplog.data.local.Category
import com.moxmose.moxequiplog.data.local.Media
import com.moxmose.moxequiplog.ui.components.DraggableLazyColumn
import com.moxmose.moxequiplog.ui.equipments.EquipmentMediaSelector
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun OptionsScreen(modifier: Modifier = Modifier, viewModel: OptionsViewModel = koinViewModel()) {
    val username by viewModel.username.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val allColors by viewModel.allColors.collectAsState()
    
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showColorPicker by rememberSaveable { mutableStateOf<String?>(null) }

    OptionsScreenContent(
        modifier = modifier,
        username = username,
        allMedia = allMedia,
        allCategories = allCategories,
        allColors = allColors,
        onUsernameChange = viewModel::setUsername,
        onSetCategoryDefault = viewModel::setCategoryDefault,
        onAddMedia = viewModel::addMedia,
        onRemoveMedia = viewModel::removeMedia,
        onUpdateMediaOrder = viewModel::updateMediaOrder,
        onToggleMediaVisibility = viewModel::toggleMediaVisibility,
        onUpdateCategoryColor = { catId, hex -> 
            viewModel.updateCategoryColor(catId, hex)
            showColorPicker = null
        },
        isPhotoUsed = { viewModel.isPhotoUsed(it) },
        showAboutDialog = showAboutDialog,
        onShowAboutDialogChange = { showAboutDialog = it },
        showColorPicker = showColorPicker,
        onShowColorPickerChange = { showColorPicker = it },
        onAddColor = viewModel::addColor,
        onUpdateColor = viewModel::updateColor,
        onUpdateColorsOrder = viewModel::updateColorsOrder,
        onToggleColorVisibility = viewModel::toggleColorVisibility
    )
}

@Composable
fun OptionsScreenContent(
    modifier: Modifier = Modifier,
    username: String,
    allMedia: List<Media>,
    allCategories: List<Category>,
    allColors: List<AppColor>,
    onUsernameChange: (String) -> Unit,
    onSetCategoryDefault: (String, String?, String?) -> Unit,
    onAddMedia: (String, String) -> Unit,
    onRemoveMedia: (String, String) -> Unit,
    onUpdateMediaOrder: (List<Media>) -> Unit,
    onToggleMediaVisibility: (String, String) -> Unit,
    onUpdateCategoryColor: (String, String) -> Unit,
    isPhotoUsed: suspend (String) -> Boolean,
    showAboutDialog: Boolean,
    onShowAboutDialogChange: (Boolean) -> Unit,
    showColorPicker: String?,
    onShowColorPickerChange: (String?) -> Unit,
    onAddColor: (String, String) -> Unit,
    onUpdateColor: (AppColor) -> Unit,
    onUpdateColorsOrder: (List<AppColor>) -> Unit,
    onToggleColorVisibility: (Long) -> Unit
) {
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { onShowAboutDialogChange(false) },
            title = { Text(stringResource(R.string.about_dialog_title)) },
            text = { Text(stringResource(R.string.about_dialog_content, BuildConfig.VERSION_NAME)) },
            confirmButton = {
                TextButton(onClick = { onShowAboutDialogChange(false) }) {
                    Text(stringResource(R.string.button_ok))
                }
            }
        )
    }

    showColorPicker?.let {
        val category = allCategories.find { it.id == showColorPicker }!!
        ColorPickerDialog(
            allColors = allColors,
            category = category,
            onDismiss = { onShowColorPickerChange(null) },
            onColorSelected = { onUpdateCategoryColor(category.id, it) },
            onAddColor = onAddColor,
            onUpdateColor = onUpdateColor,
            onUpdateColorsOrder = onUpdateColorsOrder,
            onToggleColorVisibility = onToggleColorVisibility
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.options_version_label, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        OptionsSectionCard(title = "Profilo") {
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Nome Utente") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        OptionsSectionCard(
            title = "Sezioni e Colori",
            description = "Personalizza i colori identificativi per ogni sezione dell'app."
        ) {
            allCategories.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(category.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(12.dp))
                     Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(category.color)))
                            .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onShowColorPickerChange(category.id) }
                    )
                }
                if (category != allCategories.last()) Divider(Modifier.padding(vertical = 12.dp))
            }
        }

        OptionsSectionCard(
            title = "Gestione Media e Default",
            description = "Punto unico per gestire i media. Seleziona una categoria specifica per impostare l'elemento di default per quella sezione."
        ) {
            EquipmentMediaSelector(
                photoUri = null,
                iconIdentifier = null,
                mediaLibrary = allMedia,
                categories = allCategories,
                onMediaSelected = { _, _ -> },
                onAddMedia = onAddMedia,
                onRemoveMedia = onRemoveMedia,
                onUpdateMediaOrder = onUpdateMediaOrder,
                onToggleMediaVisibility = onToggleMediaVisibility,
                onSetDefaultInCategory = onSetCategoryDefault,
                isPhotoUsed = isPhotoUsed,
                isPrefsMode = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onShowAboutDialogChange(true) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.button_about))
        }
    }
}

@Composable
fun ColorPickerDialog(
    allColors: List<AppColor>,
    category: Category,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit,
    onAddColor: (String, String) -> Unit,
    onUpdateColor: (AppColor) -> Unit,
    onUpdateColorsOrder: (List<AppColor>) -> Unit,
    onToggleColorVisibility: (Long) -> Unit
) {
    var showAddColorDialog by remember { mutableStateOf(false) }
    var editingColor by remember { mutableStateOf<AppColor?>(null) }
    var showHidden by remember { mutableStateOf(false) }
    val reorderableColors = remember(allColors, showHidden) {
        allColors.filter { !it.hidden || showHidden }.toMutableStateList()
    }
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(showHidden) {
        scope.launch {
            if (reorderableColors.isNotEmpty()) {
                lazyListState.scrollToItem(0)
            }
        }
    }

    if (showAddColorDialog) {
        AddColorDialog(
            onDismiss = { showAddColorDialog = false },
            onAddColor = onAddColor
        )
    }
    
    editingColor?.let {
        AddColorDialog(
            color = it,
            onDismiss = { editingColor = null },
            onAddColor = { hex, name -> onUpdateColor(it.copy(hexValue = hex, name = name)) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestione Colori") },
        text = {
            Column(Modifier.height(500.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trascina per ordinare", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showHidden = !showHidden }) {
                        Icon(if (showHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                    }
                }
                DraggableLazyColumn(
                    items = reorderableColors,
                    key = { _, color -> color.id },
                    onMove = { from, to ->
                        reorderableColors.add(to, reorderableColors.removeAt(from))
                    },
                    onDrop = {
                        val hiddenColors = allColors.filter { it.hidden && !showHidden }
                        val fullNewList = reorderableColors + hiddenColors
                        onUpdateColorsOrder(fullNewList.mapIndexed { index, appColor -> appColor.copy(displayOrder = index) })
                    },
                    itemContent = { _, color ->
                        ColorListItem(
                            color = color,
                            isSelected = category.color.equals(color.hexValue, ignoreCase = true),
                            onColorSelected = { onColorSelected(color.hexValue) },
                            onEdit = { editingColor = color },
                            onToggleVisibility = { onToggleColorVisibility(color.id) }
                        )
                    }
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showAddColorDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Aggiungi Nuovo Colore")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Chiudi") }
        }
    )
}

@Composable
fun ColorListItem(
    color: AppColor,
    isSelected: Boolean,
    onColorSelected: () -> Unit,
    onEdit: () -> Unit,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onColorSelected() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(color.hexValue)))
                .border(2.dp, if(isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
        )
        Spacer(Modifier.width(16.dp))
        Text(color.name.ifEmpty { color.hexValue }, modifier = Modifier.weight(1f), color = if(color.hidden) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface)
        IconButton(onClick = onToggleVisibility) {
            Icon(if (color.hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Visibility")
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Color")
        }
        Icon(Icons.Default.DragHandle, contentDescription = "Drag to Reorder")
    }
}

@Composable
fun AddColorDialog(onDismiss: () -> Unit, onAddColor: (String, String) -> Unit, color: AppColor? = null) {
    var hex by remember { mutableStateOf(color?.hexValue ?: "#") }
    var name by remember { mutableStateOf(color?.name ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (color == null) "Aggiungi un nuovo colore" else "Modifica colore") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = hex,
                    onValueChange = { 
                        if (it.startsWith("#") && it.length <= 7) hex = it
                    },
                    label = { Text("Codice Esadecimale (es. #RRGGBB)") },
                    isError = error != null,
                    enabled = color == null
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") }
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        Color(android.graphics.Color.parseColor(hex))
                        onAddColor(hex, name)
                        onDismiss()
                    } catch (e: Exception) {
                        error = "Codice colore non valido!"
                    }
                }
            ) { Text(if (color == null) "Aggiungi" else "Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}

@Composable
fun OptionsSectionCard(
    title: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}