package com.moxmose.moxequiplog.ui.options

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OptionsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun username_isDisplayedCorrectly() {
        val testUsername = "JohnDoe"

        composeTestRule.setContent {
            OptionsScreenContent(
                username = testUsername,
                onUsernameChange = {},
                showAboutDialog = false,
                onShowAboutDialogChange = {}
            )
        }

        composeTestRule.onNodeWithText(testUsername).assertIsDisplayed()
    }

    @Test
    fun onUsernameChange_isCalled_whenTextIsEntered() {
        val changedUsername = AtomicReference<String>()
        val newUsername = "NewUser"

        composeTestRule.setContent {
            OptionsScreenContent(
                username = "",
                onUsernameChange = { changedUsername.set(it) },
                showAboutDialog = false,
                onShowAboutDialogChange = {}
            )
        }

        composeTestRule.onNodeWithText("Username").performTextInput(newUsername)

        assertEquals(newUsername, changedUsername.get())
    }

    @Test
    fun aboutButton_onClick_invokesOnShowAboutDialogChange() {
        val onShowAboutDialogChangeCalled = AtomicBoolean(false)

        composeTestRule.setContent {
            OptionsScreenContent(
                username = "",
                onUsernameChange = {},
                showAboutDialog = false,
                onShowAboutDialogChange = { onShowAboutDialogChangeCalled.set(it) }
            )
        }

        composeTestRule.onNodeWithText("About").performClick()

        assertTrue(onShowAboutDialogChangeCalled.get())
    }

    @Test
    fun aboutDialog_onDismiss_invokesOnShowAboutDialogChange() {
        val callbackValue = AtomicReference<Boolean>()

        composeTestRule.setContent {
            OptionsScreenContent(
                username = "",
                onUsernameChange = {},
                showAboutDialog = true, // Dialog is initially shown
                onShowAboutDialogChange = { callbackValue.set(it) }
            )
        }

        // Click the confirm button to dismiss
        composeTestRule.onNodeWithText("OK").performClick()

        // The callback should be called with `false`
        assertFalse(callbackValue.get())
    }
}
