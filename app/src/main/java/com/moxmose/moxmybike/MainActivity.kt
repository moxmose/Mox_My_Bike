package com.moxmose.moxmybike

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.moxmose.moxmybike.ui.bikes.BikesScreen
import com.moxmose.moxmybike.ui.maintenancelog.MaintenanceLogScreen
import com.moxmose.moxmybike.ui.operations.OperationTypeScreen
import com.moxmose.moxmybike.ui.options.OptionsScreen
import com.moxmose.moxmybike.ui.theme.MoxMyBikeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoxMyBikeTheme {
                MoxMyBikeApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun MoxMyBikeApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.LOGS) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { if (it.enabled) currentDestination = it },
                    enabled = it.enabled
                )
            }
        }
    ) { 
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.LOGS -> MaintenanceLogScreen()
                AppDestinations.BIKES -> BikesScreen()
                AppDestinations.OPERATIONS -> OperationTypeScreen()
                AppDestinations.REPORTS -> Greeting(name = "Reports", modifier = Modifier.padding(innerPadding))
                AppDestinations.OPTIONS -> OptionsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true
) {
    LOGS("Logs", Icons.Default.Home),
    BIKES("Bikes", Icons.Default.List),
    OPERATIONS("Operations", Icons.Default.Build),
    REPORTS("Reports", Icons.Default.Assessment, false),
    OPTIONS("Options", Icons.Default.Settings),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoxMyBikeTheme {
        Greeting("Android")
    }
}
