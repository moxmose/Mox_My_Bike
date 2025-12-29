package com.moxmose.moxmybike.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
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
@Config(manifest = Config.NONE)
class MaintenanceLogDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var maintenanceLogDao: MaintenanceLogDao
    private lateinit var bikeDao: BikeDao
    private lateinit var operationTypeDao: OperationTypeDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        maintenanceLogDao = database.maintenanceLogDao()
        bikeDao = database.bikeDao()
        operationTypeDao = database.operationTypeDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertLog_whenDependenciesExist_retrievesLogWithDetails() = runTest {
        // 1. Setup dependencies
        val bike = Bike(id = 1, description = "Mountain Bike")
        val operationType = OperationType(id = 1, description = "Clean Chain")
        bikeDao.insertBike(bike)
        operationTypeDao.insertOperationType(operationType)

        // 2. Insert the log
        val log = MaintenanceLog(id = 1, bikeId = 1, operationTypeId = 1, date = System.currentTimeMillis())
        maintenanceLogDao.insertLog(log)

        // 3. Build a simple query to get all logs
        val query = SimpleSQLiteQuery(
            """
            SELECT 
                l.*,
                b.description as bikeDescription,
                ot.description as operationTypeDescription,
                b.photoUri as bikePhotoUri,
                ot.photoUri as operationTypePhotoUri,
                ot.iconIdentifier as operationTypeIconIdentifier,
                b.dismissed as bikeDismissed,
                ot.dismissed as operationTypeDismissed
            FROM maintenance_logs as l
            JOIN bikes as b ON l.bikeId = b.id
            JOIN operation_types as ot ON l.operationTypeId = ot.id
            WHERE l.dismissed = 0
            """
        )

        // 4. Test the flow
        maintenanceLogDao.getLogsWithDetails(query).test {
            val logDetailsList = awaitItem()
            assertEquals(1, logDetailsList.size)
            val logDetails = logDetailsList[0]

            assertEquals(1, logDetails.log.id)
            assertEquals("Mountain Bike", logDetails.bikeDescription)
            assertEquals("Clean Chain", logDetails.operationTypeDescription)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getLogsWithDetails_withSearchQuery_returnsMatchingLogs() = runTest {
        // 1. Setup dependencies and logs
        val bike1 = Bike(id = 1, description = "Road Bike")
        val bike2 = Bike(id = 2, description = "Mountain Bike")
        val op1 = OperationType(id = 1, description = "Fix Brakes")
        val op2 = OperationType(id = 2, description = "Clean Chain")
        bikeDao.insertBike(bike1)
        bikeDao.insertBike(bike2)
        operationTypeDao.insertOperationType(op1)
        operationTypeDao.insertOperationType(op2)
        maintenanceLogDao.insertLog(MaintenanceLog(bikeId = 1, operationTypeId = 1, date = System.currentTimeMillis()))
        maintenanceLogDao.insertLog(MaintenanceLog(bikeId = 2, operationTypeId = 2, notes = "Used a specific chain cleaner", date = System.currentTimeMillis()))

        // 2. Build query with a search term
        val searchTerm = "%Chain%"
        val query = SimpleSQLiteQuery(
            """
            SELECT 
                l.*,
                b.description as bikeDescription,
                ot.description as operationTypeDescription,
                b.photoUri as bikePhotoUri,
                ot.photoUri as operationTypePhotoUri,
                ot.iconIdentifier as operationTypeIconIdentifier,
                b.dismissed as bikeDismissed,
                ot.dismissed as operationTypeDismissed
            FROM maintenance_logs as l
            JOIN bikes as b ON l.bikeId = b.id
            JOIN operation_types as ot ON l.operationTypeId = ot.id
            WHERE l.dismissed = 0 AND (ot.description LIKE ? OR l.notes LIKE ?)
            """,
            arrayOf(searchTerm, searchTerm)
        )

        // 3. Test the flow
        maintenanceLogDao.getLogsWithDetails(query).test {
            val logDetailsList = awaitItem()
            assertEquals(1, logDetailsList.size)
            assertEquals("Clean Chain", logDetailsList[0].operationTypeDescription)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
