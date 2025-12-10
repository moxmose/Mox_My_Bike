package com.moxmose.moxmybike.ui.operations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperationTypeScreen(viewModel: OperationTypeViewModel = koinViewModel()) {
    val operationTypes by viewModel.allOperationTypes.collectAsState()
    var newOperationTypeDescription by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newOperationTypeDescription,
                onValueChange = { newOperationTypeDescription = it },
                label = { Text("New operation type") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newOperationTypeDescription.isNotBlank()) {
                        viewModel.addOperationType(newOperationTypeDescription)
                        newOperationTypeDescription = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(operationTypes) { operationType ->
                Text(text = operationType.description, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
