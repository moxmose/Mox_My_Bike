package com.moxmose.moxmybike.ui.maintenancelog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.moxmose.moxmybike.data.local.Bike
import com.moxmose.moxmybike.data.local.MaintenanceLog
import com.moxmose.moxmybike.data.local.MaintenanceLogDetails
import com.moxmose.moxmybike.data.local.OperationType
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceLogScreen(viewModel: MaintenanceLogViewModel = koinViewModel()) {
    val activeLogs by viewModel.activeLogsWithDetails.collectAsState()
    val allLogs by viewModel.allLogsWithDetails.collectAsState()
    val bikes by viewModel.allBikes.collectAsState()
    val operationTypes by viewModel.allOperationTypes.collectAsState()

    // FILTRA E ORDINA LE LISTE QUI
    val activeBikes = remember(bikes) { bikes.filter { !it.dismissed }.sortedBy { it.displayOrder } }
    val activeOperationTypes = remember(operationTypes) { operationTypes.filter { !it.dismissed }.sortedBy { it.displayOrder } }

    var showDismissed by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val logs by remember(activeLogs, allLogs, showDismissed, searchQuery) {
        derivedStateOf {
            val sourceList = if (showDismissed) allLogs else activeLogs
            if (searchQuery.isBlank()) {
                sourceList
            } else {
                sourceList.filter {
                    it.bikeDescription.contains(searchQuery, ignoreCase = true) ||
                            it.operationTypeDescription.contains(searchQuery, ignoreCase = true) ||
                            (it.log.notes?.contains(searchQuery, ignoreCase = true) ?: false)
                }
            }
        }
    }

    if (showAddDialog) {
        MaintenanceLogDialog(
            bikes = activeBikes,
            operationTypes = activeOperationTypes,
            onDismissRequest = { showAddDialog = false },
            onConfirm = {
                viewModel.addLog(it.bikeId, it.operationTypeId, it.notes, it.kilometers, it.date, it.color)
                showAddDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Log")
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
        Column(Modifier.padding(paddingValues)) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(8.dp)) {
                items(logs, key = { it.log.id }) { logDetail ->
                    MaintenanceLogCard(
                        logDetail = logDetail,
                        viewModel = viewModel,
                        bikes = activeBikes,
                        operationTypes = activeOperationTypes
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceLogDialog(
    bikes: List<Bike>,
    operationTypes: List<OperationType>,
    onDismissRequest: () -> Unit,
    onConfirm: (MaintenanceLog) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var notes by remember { mutableStateOf("") }
    var kilometers by remember { mutableStateOf("") }
    var selectedBike by remember { mutableStateOf<Bike?>(null) }
    var selectedOperationType by remember { mutableStateOf<OperationType?>(null) }
    var isBikeDropdownExpanded by remember { mutableStateOf(false) }
    var isOperationDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add New Maintenance Log") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = isBikeDropdownExpanded,
                    onExpandedChange = { isBikeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBike?.description?.takeIf { it.isNotBlank() } ?: selectedBike?.let { "id:${it.id} - no description" } ?: "Select a bike",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bike") },
                        leadingIcon = {
                            selectedBike?.photoUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                )
                            } ?: Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(24.dp))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBikeDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isBikeDropdownExpanded,
                        onDismissRequest = { isBikeDropdownExpanded = false }
                    ) {
                        bikes.forEach { bike ->
                            DropdownMenuItem(
                                text = { Text(bike.description.takeIf { it.isNotBlank() } ?: "id:${bike.id} - no description") },
                                leadingIcon = {
                                    bike.photoUri?.let {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                        )
                                    } ?: Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(24.dp))
                                },
                                onClick = {
                                    selectedBike = bike
                                    isBikeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = isOperationDropdownExpanded,
                    onExpandedChange = { isOperationDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOperationType?.description?.takeIf { it.isNotBlank() } ?: selectedOperationType?.let { "id:${it.id} - no description" } ?: "Select an operation",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Operation") },
                        leadingIcon = {
                            selectedOperationType?.photoUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                )
                            } ?: Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(24.dp))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isOperationDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isOperationDropdownExpanded,
                        onDismissRequest = { isOperationDropdownExpanded = false }
                    ) {
                        operationTypes.forEach { operation ->
                            DropdownMenuItem(
                                text = { Text(operation.description.takeIf { it.isNotBlank() } ?: "id:${operation.id} - no description") },
                                leadingIcon = {
                                    if (operation.photoUri != null) {
                                        AsyncImage(
                                            model = operation.photoUri,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                },
                                onClick = {
                                    selectedOperationType = operation
                                    isOperationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = kilometers,
                    onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) kilometers = it },
                    label = { Text("Kilometers (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 200) notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dateFormat.format(Date(selectedDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bike = selectedBike
                    val op = selectedOperationType
                    if (bike != null && op != null) {
                        onConfirm(
                            MaintenanceLog(
                                bikeId = bike.id,
                                operationTypeId = op.id,
                                notes = notes.takeIf { it.isNotBlank() },
                                kilometers = kilometers.toIntOrNull(),
                                date = selectedDate
                            )
                        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceLogCard(
    logDetail: MaintenanceLogDetails,
    viewModel: MaintenanceLogViewModel,
    bikes: List<Bike>,
    operationTypes: List<OperationType>,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedNotes by remember(logDetail, isEditing) { mutableStateOf(logDetail.log.notes ?: "") }
    var editedKilometers by remember(logDetail, isEditing) { mutableStateOf(logDetail.log.kilometers?.toString() ?: "") }
    var editedDate by remember(logDetail, isEditing) { mutableStateOf(logDetail.log.date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedBike by remember(logDetail, isEditing) { mutableStateOf(bikes.find { it.id == logDetail.log.bikeId }) }
    var selectedOperationType by remember(logDetail, isEditing) { mutableStateOf(operationTypes.find { it.id == logDetail.log.operationTypeId }) }
    var isBikeDropdownExpanded by remember { mutableStateOf(false) }
    var isOperationDropdownExpanded by remember { mutableStateOf(false) }

    var isExpanded by remember { mutableStateOf(false) }
    val cardAlpha = if (logDetail.log.dismissed) 0.5f else 1f
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { editedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer(alpha = cardAlpha)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isEditing) {
                    ExposedDropdownMenuBox(
                        expanded = isBikeDropdownExpanded,
                        onExpandedChange = { isBikeDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBike?.description?.takeIf { it.isNotBlank() } ?: selectedBike?.let { "id:${it.id} - no description" } ?: "Select a bike",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bike") },
                            leadingIcon = {
                                selectedBike?.photoUri?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                    )
                                } ?: Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(24.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBikeDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isBikeDropdownExpanded,
                            onDismissRequest = { isBikeDropdownExpanded = false }
                        ) {
                            bikes.forEach { bike ->
                                DropdownMenuItem(
                                    text = { Text(bike.description.takeIf { it.isNotBlank() } ?: "id:${bike.id} - no description") },
                                    leadingIcon = {
                                        bike.photoUri?.let {
                                            AsyncImage(
                                                model = it,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                            )
                                        } ?: Icon(Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(24.dp))
                                    },
                                    onClick = {
                                        selectedBike = bike
                                        isBikeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = isOperationDropdownExpanded,
                        onExpandedChange = { isOperationDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedOperationType?.description?.takeIf { it.isNotBlank() } ?: selectedOperationType?.let { "id:${it.id} - no description" } ?: "Select an operation",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Operation") },
                            leadingIcon = {
                                selectedOperationType?.photoUri?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                    )
                                } ?: Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(24.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isOperationDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isOperationDropdownExpanded,
                            onDismissRequest = { isOperationDropdownExpanded = false }
                        ) {
                            operationTypes.forEach { operation ->
                                DropdownMenuItem(
                                    text = { Text(operation.description.takeIf { it.isNotBlank() } ?: "id:${operation.id} - no description") },
                                    leadingIcon = {
                                        if (operation.photoUri != null) {
                                            AsyncImage(
                                                model = operation.photoUri,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                            )
                                        } else {
                                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(24.dp))
                                        }
                                    },
                                    onClick = {
                                        selectedOperationType = operation
                                        isOperationDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = editedKilometers,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) editedKilometers = it },
                        label = { Text("Kilometers (optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedNotes,
                        onValueChange = { if (it.length <= 200) editedNotes = it },
                        label = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = { showDatePicker = true }) {
                        Text(text = "Change Date: ${dateFormat.format(Date(editedDate))}")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val bikeTextAlpha = if (logDetail.bikeDismissed) 0.5f else 1f
                        logDetail.bikePhotoUri?.let {
                            AsyncImage(model = it, contentDescription = "Bike photo", modifier = Modifier.size(24.dp).clip(CircleShape).graphicsLayer(alpha = bikeTextAlpha))
                        } ?: Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsBike, contentDescription = "Bike Icon", modifier = Modifier.size(24.dp).graphicsLayer(alpha = bikeTextAlpha))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (logDetail.bikeDescription.takeIf { it.isNotBlank() } ?: "id:${logDetail.log.bikeId} - no description") + if (logDetail.bikeDismissed) " (dismissed)" else "",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer(alpha = bikeTextAlpha)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val operationTypeAlpha = if (logDetail.operationTypeDismissed) 0.5f else 1f
                        if (logDetail.operationTypePhotoUri != null) {
                            AsyncImage(model = logDetail.operationTypePhotoUri, contentDescription = "Operation photo", modifier = Modifier.size(24.dp).clip(CircleShape).graphicsLayer(alpha = operationTypeAlpha))
                        } else {
                            Icon(imageVector = Icons.Default.Build, contentDescription = "Operation icon", modifier = Modifier.size(24.dp).graphicsLayer(alpha = operationTypeAlpha))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = (logDetail.operationTypeDescription.takeIf { it.isNotBlank() } ?: "id:${logDetail.log.operationTypeId} - no description") + if (logDetail.operationTypeDismissed) " (dismissed)" else "",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer(alpha = operationTypeAlpha)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = logDetail.log.kilometers?.let { "Km: $it" } ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateFormat.format(Date(logDetail.log.date)),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    logDetail.log.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Column {
                IconButton(onClick = {
                    if (isEditing) {
                        val bike = selectedBike
                        val op = selectedOperationType
                        if (bike != null && op != null) {
                            viewModel.updateLog(logDetail.log.copy(notes = editedNotes, kilometers = editedKilometers.toIntOrNull(), date = editedDate, bikeId = bike.id, operationTypeId = op.id))
                        }
                    }
                    isEditing = !isEditing
                }) {
                    Icon(imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit, contentDescription = "Edit Log")
                }
                if (isEditing) {
                    IconButton(
                        onClick = {
                            if (logDetail.log.dismissed) {
                                viewModel.restoreLog(logDetail.log)
                            } else {
                                viewModel.dismissLog(logDetail.log)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (logDetail.log.dismissed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (logDetail.log.dismissed) "Restore Log" else "Dismiss Log"
                        )
                    }
                }
            }
        }
    }
}
