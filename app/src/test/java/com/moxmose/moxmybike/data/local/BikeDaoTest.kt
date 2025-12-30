package com.moxmose.moxmybike.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class BikeDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var bikeDao: BikeDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // For simplicity in tests
            .build()
        bikeDao = database.bikeDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertBike_whenNewBikeIsAdded_updatesActiveBikesFlow() = runTest {
        val bike = Bike(id = 1, description = "Test Bike", displayOrder = 0)

        bikeDao.insertBike(bike)

        bikeDao.getActiveBikes().test {
            val bikeList = awaitItem()
            assertEquals(1, bikeList.size)
            assertEquals("Test Bike", bikeList[0].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateBike_withValidData_reflectsChangesInFlow() = runTest {
        val initialBike = Bike(id = 1, description = "Initial Name", displayOrder = 0)
        bikeDao.insertBike(initialBike)

        // First, verify the initial state
        bikeDao.getActiveBikes().test {
            assertEquals("Initial Name", awaitItem().first().description)
            cancelAndIgnoreRemainingEvents()
        }

        val updatedBike = initialBike.copy(description = "Updated Name")
        bikeDao.updateBike(updatedBike)

        // Then, verify the updated state
        bikeDao.getActiveBikes().test {
            val bikeList = awaitItem()
            assertEquals(1, bikeList.size)
            assertEquals("Updated Name", bikeList[0].description)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun dismissBike_whenBikeIsDismissed_isRemovedFromActiveBikesFlow() = runTest {
        val bike = Bike(id = 1, description = "Test Bike", dismissed = false, displayOrder = 0)
        bikeDao.insertBike(bike)

        // 1. Verify it's initially active
        bikeDao.getActiveBikes().test {
            assertEquals(1, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // 2. Dismiss the bike
        val dismissedBike = bike.copy(dismissed = true)
        bikeDao.updateBike(dismissedBike)

        // 3. Verify it's no longer in the active list
        bikeDao.getActiveBikes().test {
            assertEquals(0, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }

        // 4. Verify it's still in the "all bikes" list
        bikeDao.getAllBikes().test {
            val allBikes = awaitItem()
            assertEquals(1, allBikes.size)
            assertTrue(allBikes.first().dismissed)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
