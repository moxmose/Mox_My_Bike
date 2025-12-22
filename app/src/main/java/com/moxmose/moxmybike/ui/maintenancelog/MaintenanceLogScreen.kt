package com.moxmose.moxmybike.ui.maintenancelog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.moxmose.moxmybike.data.local.Bike
import com.moxmose.moxmybike.data.local.MaintenanceLogDetails
import com.moxmose.moxmybike.data.local.OperationType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    var showDismissed by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddLogDialog by remember { mutableStateOf(false) }

    var logs by remember { mutableStateOf<List<MaintenanceLogDetails>>(emptyList()) }

    LaunchedEffect(activeLogs, allLogs, showDismissed, searchQuery) {
        val sourceList = if (showDismissed) allLogs else activeLogs
        logs = sourceList.filter {
            it.bikeDescription.contains(searchQuery, ignoreCase = true) ||
            it.operationTypeDescription.contains(searchQuery, ignoreCase = true) ||
            it.log.notes?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    if (showAddLogDialog) {
        AddMaintenanceLogDialog(
            bikes = bikes,
            operationTypes = operationTypes,
            onDismissRequest = { showAddLogDialog = false },
            onConfirm = {
                viewModel.addLog(it.bikeId, it.operationTypeId, it.notes, it.kilometers, it.date, it.color)
                showAddLogDialog = false
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(onClick = { showAddLogDialog = true }) {
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
                items(logs) { logDetail ->
                    MaintenanceLogCard(
                        logDetail = logDetail,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaintenanceLogDialog(
    bikes: List<Bike>,
    operationTypes: List<OperationType>,
    onDismissRequest: () -> Unit,
    onConfirm: (com.moxmose.moxmybike.data.local.MaintenanceLog) -> Unit
) {
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
                        value = selectedBike?.description ?: "Select a bike",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            selectedBike?.photoUri?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Bike photo",
                                    modifier = Modifier.size(24.dp).clip(CircleShape)
                                )
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBikeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Bike") }
                    )
                    ExposedDropdownMenu(
                        expanded = isBikeDropdownExpanded,
                        onDismissRequest = { isBikeDropdownExpanded = false }
                    ) {
                        bikes.forEach { bike ->
                            DropdownMenuItem(
                                text = { Text(bike.description) },
                                leadingIcon = {
                                    bike.photoUri?.let {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = "Bike photo",
                                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                        )
                                    }
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
                        value = selectedOperationType?.description ?: "Select an operation",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                             selectedOperationType?.let { 
                                if (it.photoUri != null) {
                                    AsyncImage(
                                        model = it.photoUri,
                                        contentDescription = "Operation photo",
                                        modifier = Modifier.size(24.dp).clip(CircleShape)
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Build, contentDescription = "Operation icon", modifier = Modifier.size(24.dp))
                                }
                            }
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isOperationDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("Operation") }
                    )
                    ExposedDropdownMenu(
                        expanded = isOperationDropdownExpanded,
                        onDismissRequest = { isOperationDropdownExpanded = false }
                    ) {
                        operationTypes.forEach { operation ->
                            DropdownMenuItem(
                                text = { Text(operation.description) },
                                leadingIcon = {
                                    if (operation.photoUri != null) {
                                        AsyncImage(
                                            model = operation.photoUri,
                                            contentDescription = "Operation photo",
                                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Icon(imageVector = Icons.Default.Build, contentDescription = "Operation icon", modifier = Modifier.size(24.dp))
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
                    onValueChange = { kilometers = it },
                    label = { Text("Kilometers") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { showDatePicker = true }) {
                    Text(text = "Select Date: ${SimpleDateFormat.getDateInstance().format(Date(selectedDate))}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bike = selectedBike
                    val op = selectedOperationType
                    val km = kilometers.toIntOrNull()
                    if (bike != null && op != null && km != null) {
                        onConfirm(
                            com.moxmose.moxmybike.data.local.MaintenanceLog(
                                bikeId = bike.id,
                                operationTypeId = op.id,
                                notes = notes.takeIf { it.isNotBlank() },
                                kilometers = km,
                                date = selectedDate,
                                color = null // TODO: Implement color picker
                            )
                        )
                    }
                }
            ) {
                Text("Add Log")
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
fun MaintenanceLogCard(logDetail: MaintenanceLogDetails, viewModel: MaintenanceLogViewModel, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }
    var editedNotes by remember { mutableStateOf(logDetail.log.notes ?: "") }
    var editedDate by remember { mutableStateOf(logDetail.log.date) }
    var showDatePicker by remember { mutableStateOf(false) }

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
            .graphicsLayer(alpha = cardAlpha),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedNotes,
                    onValueChange = { if (it.length <= 200) editedNotes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { showDatePicker = true }) {
                    Text(text = "Change Date: ${dateFormat.format(Date(editedDate))}")
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    logDetail.bikePhotoUri?.let {
                        AsyncImage(model = it, contentDescription = "Bike photo", modifier = Modifier.size(24.dp).clip(CircleShape))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(logDetail.bikeDescription, style = MaterialTheme.typography.titleSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (logDetail.operationTypePhotoUri != null) {
                        AsyncImage(model = logDetail.operationTypePhotoUri, contentDescription = "Operation photo", modifier = Modifier.size(24.dp).clip(CircleShape))
                    } else {
                        Icon(imageVector = Icons.Default.Build, contentDescription = "Operation icon", modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(logDetail.operationTypeDescription, style = MaterialTheme.typography.bodyLarge)
                }
                Text("Km: ${logDetail.log.kilometers}", style = MaterialTheme.typography.bodyMedium)
                logDetail.log.notes?.let {
                    Text("Notes: $it", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    text = dateFormat.format(Date(logDetail.log.date)),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                IconButton(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateLog(logDetail.log.copy(notes = editedNotes, date = editedDate))
                        }
                        isEditing = !isEditing
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Filled.Done else Icons.Filled.Edit,
                        contentDescription = if (isEditing) "Save" else "Edit Log"
                    )
                }
            }
        }
    }
}
