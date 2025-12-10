package com.moxmose.moxmybike.ui.bikes

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
fun BikesScreen(viewModel: BikesViewModel = koinViewModel()) {
    val bikes by viewModel.allBikes.collectAsState()
    var newBikeDescription by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newBikeDescription,
                onValueChange = { newBikeDescription = it },
                label = { Text("New bike description") },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (newBikeDescription.isNotBlank()) {
                        viewModel.addBike(newBikeDescription)
                        newBikeDescription = ""
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add")
            }
        }

        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(bikes) { bike ->
                Text(text = bike.description, modifier = Modifier.padding(8.dp))
            }
        }
    }
}
