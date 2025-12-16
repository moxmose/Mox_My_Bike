package com.moxmose.moxmybike.ui.bikes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moxmose.moxmybike.data.local.Bike
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikesScreen(viewModel: BikesViewModel = koinViewModel()) {
    val bikes by viewModel.allBikes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

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

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            items(bikes) { bike ->
                BikeCard(bike = bike, viewModel = viewModel)
            }
        }
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
fun BikeCard(bike: Bike, viewModel: BikesViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var editedDescription by remember { mutableStateOf(bike.description) }

    // Update the edited description if the bike from the database changes
    LaunchedEffect(bike.description) {
        if (!isEditing) {
            editedDescription = bike.description
        }
    }

    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()) {
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
