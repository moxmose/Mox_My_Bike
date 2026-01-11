package com.moxmose.moxequiplog.ui.options

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.moxmose.moxequiplog.ui.equipments.EquipmentMediaSelector
import org.koin.androidx.compose.koinViewModel

@Composable
fun OptionsScreen(modifier: Modifier = Modifier, viewModel: OptionsViewModel = koinViewModel()) {
    val username by viewModel.username.collectAsState()
    val favoriteIcon by viewModel.favoriteIcon.collectAsState()
    val favoritePhotoUri by viewModel.favoritePhotoUri.collectAsState()
    val hiddenIcons by viewModel.hiddenIcons.collectAsState()
    val hiddenImages by viewModel.hiddenImages.collectAsState()
    val allMedia by viewModel.allMedia.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val allColors by viewModel.allColors.collectAsState()
    
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }

    OptionsScreenContent(
        modifier = modifier,
        username = username,
        favoriteIcon = favoriteIcon,
        favoritePhotoUri = favoritePhotoUri,
        hiddenIcons = hiddenIcons,
        hiddenImages = hiddenImages,
        allMedia = allMedia,
        allCategories = allCategories,
        allColors = allColors,
        onUsernameChange = { viewModel.setUsername(it) },
        onSetCategoryDefault = { catId, iconId, photoUri -> 
            viewModel.setCategoryDefault(catId, iconId, photoUri) 
        },
        onAddMedia = { uri, cat -> viewModel.addMedia(uri, cat) },
        onRemoveMedia = { uri, cat -> viewModel.removeMedia(uri, cat) },
        onUpdateMediaOrder = { viewModel.updateMediaOrder(it) },
        onToggleIconVisibility = { viewModel.toggleIconVisibility(it) },
        onToggleImageVisibility = { viewModel.toggleImageVisibility(it) },
        onUpdateCategoryColor = { catId, hex -> viewModel.updateCategoryColor(catId, hex) },
        isPhotoUsed = { viewModel.isPhotoUsed(it) },
        showAboutDialog = showAboutDialog,
        onShowAboutDialogChange = { showAboutDialog = it }
    )
}

@Composable
fun OptionsScreenContent(
    modifier: Modifier = Modifier,
    username: String,
    favoriteIcon: String?,
    favoritePhotoUri: String?,
    hiddenIcons: Set<String>,
    hiddenImages: Set<String>,
    allMedia: List<Media>,
    allCategories: List<Category>,
    allColors: List<AppColor>,
    onUsernameChange: (String) -> Unit,
    onSetCategoryDefault: (String, String?, String?) -> Unit,
    onAddMedia: (String, String) -> Unit,
    onRemoveMedia: (String, String) -> Unit,
    onUpdateMediaOrder: (List<Media>) -> Unit,
    onToggleIconVisibility: (String) -> Unit,
    onToggleImageVisibility: (String) -> Unit,
    onUpdateCategoryColor: (String, String) -> Unit,
    isPhotoUsed: suspend (String) -> Boolean,
    showAboutDialog: Boolean,
    onShowAboutDialogChange: (Boolean) -> Unit
) {
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { onShowAboutDialogChange(false) },
            title = { Text(stringResource(R.string.about_dialog_title)) },
            text = { Text(stringResource(R.string.about_dialog_content)) },
            confirmButton = {
                TextButton(onClick = { onShowAboutDialogChange(false) }) {
                    Text(stringResource(R.string.button_ok))
                }
            }
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

        // Card Profilo
        OptionsSectionCard(title = "Profilo") {
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("Nome Utente") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Card Categorie e Colori
        OptionsSectionCard(
            title = "Sezioni e Colori",
            description = "Personalizza i colori identificativi per ogni sezione dell'app."
        ) {
            allCategories.forEach { category ->
                CategoryColorRow(
                    category = category,
                    allColors = allColors,
                    onColorSelected = { onUpdateCategoryColor(category.id, it) }
                )
                if (category != allCategories.last()) Spacer(Modifier.height(12.dp))
            }
        }

        // Card Gestione Media Completa (Unica card per tutto)
        OptionsSectionCard(
            title = "Gestione Media e Default",
            description = "Punto unico per gestire i media. Seleziona una categoria specifica per impostare l'elemento di default per quella sezione."
        ) {
            EquipmentMediaSelector(
                photoUri = null,
                iconIdentifier = null,
                hiddenIcons = hiddenIcons,
                hiddenImages = hiddenImages,
                mediaLibrary = allMedia,
                categories = allCategories,
                onMediaSelected = { _, _ -> }, // Usiamo onSetDefaultInCategory per le Prefs
                onAddMedia = onAddMedia,
                onRemoveMedia = onRemoveMedia,
                onUpdateMediaOrder = onUpdateMediaOrder,
                onToggleIconVisibility = onToggleIconVisibility,
                onToggleImageVisibility = onToggleImageVisibility,
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
fun CategoryColorRow(
    category: Category,
    allColors: List<AppColor>,
    onColorSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(android.graphics.Color.parseColor(category.color)))
            )
            Spacer(Modifier.width(12.dp))
            Text(category.name, style = MaterialTheme.typography.titleMedium)
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allColors.take(10).forEach { color -> // Limitiamo a 10 per riga
                val isSelected = category.color.equals(color.hexValue, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(color.hexValue)))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color.hexValue) }
                )
            }
        }
    }
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
