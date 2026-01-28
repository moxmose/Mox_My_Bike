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
import com.moxmose.moxequiplog.data.local.AppColor
import com.moxmose.moxequiplog.data.local.Category
import com.moxmose.moxequiplog.data.local.EquipmentDao
import io.mockk.coEvery
import io.mockk.coVerify
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun allCategories_onInit_isEmpty() = runTest {
        viewModel.allCategories.test {
            assertEquals(emptyList<Category>(), awaitItem())
        }
    }

    @Test
    fun allColors_onInit_isEmpty() = runTest {
        viewModel.allColors.test {
            assertEquals(emptyList<AppColor>(), awaitItem())
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

    @Test
    fun setCategoryDefault_withValidData_callsRepository() = runTest {
        val categoryId = "test_category"
        val iconId = "test_icon"
        val photoUri = "test_uri"

        // Esegui la funzione sul ViewModel
        viewModel.setCategoryDefault(categoryId, iconId, photoUri)

        // Esegui le coroutine in sospeso
        testDispatcher.scheduler.advanceUntilIdle()

        // Verifica che il metodo corrispondente sul repository sia stato chiamato con i parametri corretti
        coVerify { mediaRepository.setCategoryDefault(categoryId, iconId, photoUri) }
    }

    @Test
    fun isPhotoUsed_whenPhotoIsInUse_returnsTrue() = runTest {
        val uri = "used_uri"
        // Istruisci il mock: quando il DAO viene chiamato, restituisci 1
        coEvery { equipmentDao.countEquipmentsUsingPhoto(uri) } returns 1

        // Chiama la funzione e verifica il risultato
        val isUsed = viewModel.isPhotoUsed(uri)
        assertTrue(isUsed)
    }

    @Test
    fun isPhotoUsed_whenPhotoIsNotInUse_returnsFalse() = runTest {
        val uri = "unused_uri"
        // Istruisci il mock: quando il DAO viene chiamato, restituisci 0
        coEvery { equipmentDao.countEquipmentsUsingPhoto(uri) } returns 0

        // Chiama la funzione e verifica il risultato
        val isUsed = viewModel.isPhotoUsed(uri)
        assertFalse(isUsed)
    }

    @Test
    fun addMedia_withValidData_callsRepository() = runTest {
        val uri = "test_uri"
        val category = "test_category"

        // Chiama la funzione sul ViewModel
        viewModel.addMedia(uri, category)

        // Fai avanzare lo scheduler per eseguire la coroutine in sospeso
        testDispatcher.scheduler.advanceUntilIdle()

        // Verifica che il metodo del repository sia stato chiamato con i parametri corretti
        coVerify { mediaRepository.addMedia(uri, category) }
    }

}
