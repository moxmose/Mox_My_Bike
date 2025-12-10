package com.moxmose.moxmybike.ui.maintenancelog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moxmose.moxmybike.data.local.Bike
import com.moxmose.moxmybike.data.local.OperationType
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceLogScreen(viewModel: MaintenanceLogViewModel = koinViewModel()) {
    val logs by viewModel.allLogsWithDetails.collectAsState()
    val bikes by viewModel.allBikes.collectAsState()
    val operationTypes by viewModel.allOperationTypes.collectAsState()

    // State for the input form
    var notes by remember { mutableStateOf("") }
    var kilometers by remember { mutableStateOf("") }
    var selectedBike by remember { mutableStateOf<Bike?>(null) }
    var selectedOperationType by remember { mutableStateOf<OperationType?>(null) }
    var isBikeDropdownExpanded by remember { mutableStateOf(false) }
    var isOperationDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- Input Form ---
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Add New Maintenance", style = MaterialTheme.typography.titleMedium)

                // Bike Selector
                ExposedDropdownMenuBox(
                    expanded = isBikeDropdownExpanded,
                    onExpandedChange = { isBikeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBike?.description ?: "Select a bike",
                        onValueChange = {},
                        readOnly = true,
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
                                onClick = {
                                    selectedBike = bike
                                    isBikeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Operation Type Selector
                ExposedDropdownMenuBox(
                    expanded = isOperationDropdownExpanded,
                    onExpandedChange = { isOperationDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedOperationType?.description ?: "Select an operation",
                        onValueChange = {},
                        readOnly = true,
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

                Button(
                    onClick = {
                        val bike = selectedBike
                        val op = selectedOperationType
                        val km = kilometers.toIntOrNull()
                        if (bike != null && op != null && km != null) {
                            viewModel.addLog(
                                bikeId = bike.id,
                                operationTypeId = op.id,
                                notes = notes.takeIf { it.isNotBlank() },
                                kilometers = km,
                                date = System.currentTimeMillis()
                            )
                            // Reset fields
                            notes = ""
                            kilometers = ""
                            selectedBike = null
                            selectedOperationType = null
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add Log")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Log List ---
        LazyColumn {
            items(logs) { logDetail ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(logDetail.bikeDescription, style = MaterialTheme.typography.titleSmall)
                        Text(logDetail.operationTypeDescription, style = MaterialTheme.typography.bodyLarge)
                        Text("Km: ${logDetail.log.kilometers}", style = MaterialTheme.typography.bodyMedium)
                        logDetail.log.notes?.let {
                            Text("Notes: $it", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = SimpleDateFormat.getDateInstance().format(Date(logDetail.log.date)),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
