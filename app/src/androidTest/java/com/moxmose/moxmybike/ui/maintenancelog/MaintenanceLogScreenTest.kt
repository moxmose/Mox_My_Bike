package com.moxmose.moxmybike.ui.maintenancelog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.moxmose.moxmybike.data.local.Bike
import com.moxmose.moxmybike.data.local.MaintenanceLog
import com.moxmose.moxmybike.data.local.MaintenanceLogDetails
import com.moxmose.moxmybike.data.local.OperationType
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals

class MaintenanceLogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyBikes = listOf(Bike(id = 1, description = "Road Bike"))
    private val dummyOps = listOf(OperationType(id = 1, description = "Oil Change"))

    @Test
    fun maintenanceLogScreen_whenLogsArePresent_displaysLogs() {
        val logs = listOf(
            MaintenanceLogDetails(
                log = MaintenanceLog(id = 1, bikeId = 1, operationTypeId = 1, date = 0L),
                bikeDescription = "Road Bike",
                operationTypeDescription = "Oil Change",
                bikePhotoUri = null,
                operationTypePhotoUri = null,
                operationTypeIconIdentifier = null,
                bikeDismissed = false,
                operationTypeDismissed = false
            )
        )

        composeTestRule.setContent {
            MaintenanceLogScreenContent(
                logs = logs,
                bikes = dummyBikes,
                operationTypes = dummyOps,
                searchQuery = "",
                onSearchQueryChange = {},
                sortProperty = SortProperty.DATE,
                onSortPropertyChange = {},
                sortDirection = SortDirection.DESCENDING,
                onSortDirectionChange = {},
                showDismissed = false,
                onShowDismissedToggle = {},
                showAddDialog = false,
                onShowAddDialogChange = {},
                onAddLog = { _, _, _, _, _, _ -> },
                expandedCardId = null,
                onCardExpanded = {},
                editingCardId = null,
                onEditLog = {},
                onUpdateLog = {},
                onDismissLog = {},
                onRestoreLog = {}
            )
        }

        composeTestRule.onNodeWithText("Road Bike").assertIsDisplayed()
        composeTestRule.onNodeWithText("Oil Change").assertIsDisplayed()
    }

    @Test
    fun addLogFab_onClick_invokesOnShowAddDialogChange() {
        val onShowAddDialogChangeCalled = AtomicBoolean(false)

        composeTestRule.setContent {
            MaintenanceLogScreenContent(
                logs = emptyList(),
                bikes = dummyBikes,
                operationTypes = dummyOps,
                searchQuery = "",
                onSearchQueryChange = {},
                sortProperty = SortProperty.DATE,
                onSortPropertyChange = {},
                sortDirection = SortDirection.DESCENDING,
                onSortDirectionChange = {},
                showDismissed = false,
                onShowDismissedToggle = {},
                showAddDialog = false,
                onShowAddDialogChange = { onShowAddDialogChangeCalled.set(it) },
                onAddLog = { _, _, _, _, _, _ -> },
                expandedCardId = null,
                onCardExpanded = {},
                editingCardId = null,
                onEditLog = {},
                onUpdateLog = {},
                onDismissLog = {},
                onRestoreLog = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Add Log").performClick()

        assertTrue(onShowAddDialogChangeCalled.get())
    }

    @Test
    fun maintenanceLogDialog_onConfirm_callsOnConfirm() {
        val confirmedLog = AtomicReference<MaintenanceLog>()

        composeTestRule.setContent {
            MaintenanceLogDialog(
                bikes = dummyBikes,
                operationTypes = dummyOps,
                onDismissRequest = {},
                onConfirm = { confirmedLog.set(it) }
            )
        }

        // Simulate user selection
        composeTestRule.onNodeWithText("Select a bike").performClick()
        composeTestRule.onNodeWithText("Road Bike").performClick()
        composeTestRule.onNodeWithText("Select an operation").performClick()
        composeTestRule.onNodeWithText("Oil Change").performClick()

        // Click confirm
        composeTestRule.onNodeWithText("Add").performClick()

        // Verify callback
        assertEquals(1, confirmedLog.get().bikeId)
        assertEquals(1, confirmedLog.get().operationTypeId)
    }

    @Test
    fun maintenanceLogCard_onClick_invokesOnExpand() {
        val onCardExpandedCalled = AtomicBoolean(false)
        val log = MaintenanceLogDetails(
            log = MaintenanceLog(id = 1, bikeId = 1, operationTypeId = 1, date = 0L),
            bikeDescription = "Road Bike",
            operationTypeDescription = "Oil Change",
            bikePhotoUri = null, operationTypePhotoUri = null, operationTypeIconIdentifier = null, bikeDismissed = false, operationTypeDismissed = false
        )

        composeTestRule.setContent {
            MaintenanceLogCard(logDetail = log, bikes = dummyBikes, operationTypes = dummyOps, isExpanded = false, isEditing = false, onExpand = { onCardExpandedCalled.set(true) }, onEdit = {}, onSave = {}, onDismiss = {}, onRestore = {})
        }

        composeTestRule.onNodeWithText("Road Bike").performClick()

        assertTrue(onCardExpandedCalled.get())
    }

    @Test
    fun editButton_onClick_invokesOnEdit() {
        val onEditCalled = AtomicBoolean(false)
        val log = MaintenanceLogDetails(
            log = MaintenanceLog(id = 1, bikeId = 1, operationTypeId = 1, date = 0L),
            bikeDescription = "Road Bike",
            operationTypeDescription = "Oil Change",
            bikePhotoUri = null, operationTypePhotoUri = null, operationTypeIconIdentifier = null, bikeDismissed = false, operationTypeDismissed = false
        )

        composeTestRule.setContent {
            MaintenanceLogCard(logDetail = log, bikes = dummyBikes, operationTypes = dummyOps, isExpanded = false, isEditing = false, onExpand = {}, onEdit = { onEditCalled.set(true) }, onSave = {}, onDismiss = {}, onRestore = {})
        }

        composeTestRule.onNodeWithContentDescription("Edit Log").performClick()

        assertTrue(onEditCalled.get())
    }
}
