package com.moxmose.moxequiplog.ui.options

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.moxmose.moxequiplog.data.AppSettingsManager
import com.moxmose.moxequiplog.data.MediaRepository
import com.moxmose.moxequiplog.data.local.EquipmentDao
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class OptionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var appSettingsManager: AppSettingsManager
    private lateinit var equipmentDao: EquipmentDao
    private lateinit var mediaRepository: MediaRepository
    private lateinit var viewModel: OptionsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val testContext: Context = ApplicationProvider.getApplicationContext()
        testDataStore = PreferenceDataStoreFactory.create(
            produceFile = { testContext.preferencesDataStoreFile("test_settings") }
        )
        appSettingsManager = AppSettingsManager(testContext)
        equipmentDao = mockk(relaxed = true)
        mediaRepository = mockk(relaxed = true)
        viewModel = OptionsViewModel(appSettingsManager, equipmentDao, mediaRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun username_onInit_isEmpty() = runTest {
        viewModel.username.test {
            assertEquals("", awaitItem())
        }
    }

    @Test
    fun setUsername_withValidUsername_updatesUsernameFlow() = runTest {
        val newUsername = "testuser"
        viewModel.username.test {
            assertEquals("", awaitItem()) // Initial value
            viewModel.setUsername(newUsername)
            assertEquals(newUsername, awaitItem())
        }
    }
}
