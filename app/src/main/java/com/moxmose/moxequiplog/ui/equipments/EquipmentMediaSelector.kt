package com.moxmose.moxequiplog.ui.equipments

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.moxmose.moxequiplog.R
import com.moxmose.moxequiplog.ui.options.EquipmentIconProvider

@Composable
fun EquipmentMediaSelector(
    photoUri: String?,
    iconIdentifier: String?,
    onMediaSelected: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showIconPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(it, flags)
                onMediaSelected(null, it.toString())
            } catch (e: SecurityException) {
                Log.e("MediaSelector", "Failed to take persistable URI permission", e)
            }
        }
    }

    if (showIconPicker) {
        AlertDialog(
            onDismissRequest = { showIconPicker = false },
            title = { Text("Seleziona Icona") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 64.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(EquipmentIconProvider.icons.toList()) { (id, icon) ->
                        IconButton(
                            onClick = {
                                onMediaSelected(id, null)
                                showIconPicker = false
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .background(
                                    if (iconIdentifier == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    CircleShape
                                )
                        ) {
                            Icon(icon, contentDescription = null)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showIconPicker = false }) { Text("Chiudi") }
            }
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Photo Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(
                    width = if (photoUri != null) 3.dp else 1.dp,
                    color = if (photoUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable { imagePickerLauncher.launch(arrayOf("image/*")) }
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = stringResource(R.string.add_image),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text("O", style = MaterialTheme.typography.titleMedium)

        // Icon Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(
                    width = if (iconIdentifier != null) 3.dp else 1.dp,
                    color = if (iconIdentifier != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable { showIconPicker = true }
        ) {
            if (iconIdentifier != null) {
                Icon(
                    imageVector = EquipmentIconProvider.getIcon(iconIdentifier),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Scegli Icona",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
