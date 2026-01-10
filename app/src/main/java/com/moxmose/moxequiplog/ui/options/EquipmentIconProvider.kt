package com.moxmose.moxequiplog.ui.options

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Moped
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

object EquipmentIconProvider {
    val icons = mapOf(
        "directions_bike" to Icons.Default.DirectionsBike,
        "electric_bike" to Icons.Default.ElectricBike,
        "moped" to Icons.Default.Moped,
        "directions_car" to Icons.Default.DirectionsCar,
        "electric_car" to Icons.Default.ElectricCar,
        "engine" to Icons.Default.Settings,
        "engineering" to Icons.Default.Engineering,
        "manufacturing" to Icons.Default.PrecisionManufacturing,
        "build" to Icons.Default.Build
    )

    fun getIcon(identifier: String?): ImageVector {
        return icons[identifier] ?: Icons.Default.DirectionsBike
    }
}
