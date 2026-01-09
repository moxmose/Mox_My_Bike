package com.moxmose.moxequiplog.ui.equipments

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.moxmose.moxequiplog.data.local.Equipment
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals


class EquipmentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun equipmentsScreenContent_whenEquipmentsListIsNotEmpty_displaysEquipments() {
        val equipments = listOf(
            Equipment(id = 1, description = "Road Equipment"),
            Equipment(id = 2, description = "Mountain Equipment")
        )

        composeTestRule.setContent {
            EquipmentsScreenContent(
                equipments = equipments,
                showDismissed = false,
                onToggleShowDismissed = {},
                showAddDialog = false,
                onShowAddDialogChange = {},
                onAddEquipment = { _, _ -> },
                onUpdateEquipments = {},
                onUpdateEquipment = {},
                onDismissEquipment = {},
                onRestoreEquipment = {}
            )
        }

        composeTestRule.onNodeWithText("Road Equipment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mountain Equipment").assertIsDisplayed()
    }

    @Test
    fun addEquipmentFab_onClick_invokesOnShowAddDialogChange() {
        val onShowAddDialogChangeCalled = AtomicBoolean(false)

        composeTestRule.setContent {
            EquipmentsScreenContent(
                equipments = emptyList(),
                showDismissed = false,
                onToggleShowDismissed = {},
                showAddDialog = false,
                onShowAddDialogChange = { onShowAddDialogChangeCalled.set(it) },
                onAddEquipment = { _, _ -> },
                onUpdateEquipments = {},
                onUpdateEquipment = {},
                onDismissEquipment = {},
                onRestoreEquipment = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Add Equipment").performClick()

        assertTrue(onShowAddDialogChangeCalled.get())
    }

    @Test
    fun addEquipmentDialog_onConfirm_callsOnAddEquipment() {
        val newEquipmentDescription = "New Gravel Equipment"
        val addedEquipmentInfo = AtomicReference<Pair<String, String?>>()

        composeTestRule.setContent {
            AddEquipmentDialog(
                onDismissRequest = {},
                onConfirm = { addedEquipmentInfo.set(it) }
            )
        }

        // 1. Enter text into the description field
        composeTestRule.onNodeWithText("Equipment description").performTextInput(newEquipmentDescription)

        // 2. Click the confirm button
        composeTestRule.onNodeWithText("Add").performClick()

        // 3. Verify the callback was invoked with the correct data
        assertEquals(newEquipmentDescription, addedEquipmentInfo.get().first)
    }
}
