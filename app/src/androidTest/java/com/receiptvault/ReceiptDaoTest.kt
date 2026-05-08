package com.receiptvault

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.receiptvault.data.local.ReceiptDatabase
import com.receiptvault.data.local.dao.ReceiptDao
import com.receiptvault.data.local.entities.Receipt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiptDaoTest {

    private lateinit var database: ReceiptDatabase
    private lateinit var dao: ReceiptDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ReceiptDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.receiptDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun sampleReceipt(
        merchant: String = "Starbucks",
        amount: Double = 12.50,
        date: String = "2024-03-15",
        category: String = "Food & Dining"
    ) = Receipt(
        merchant = merchant,
        amount = amount,
        date = date,
        category = category,
        imageUri = "/fake/path.jpg",
        notes = null,
        syncedToCloud = false
    )

    @Test
    fun insertAndRetrieveReceipt() = runBlocking {
        val receipt = sampleReceipt()
        val id = dao.insertReceipt(receipt)
        assertTrue(id > 0)

        val retrieved = dao.getReceiptById(id.toInt())
        assertNotNull(retrieved)
        assertEquals("Starbucks", retrieved?.merchant)
        assertEquals(12.50, retrieved?.amount ?: 0.0, 0.001)
    }

    @Test
    fun getAllReceiptsReturnsAllInserted() = runBlocking {
        dao.insertReceipt(sampleReceipt(merchant = "Merchant A"))
        dao.insertReceipt(sampleReceipt(merchant = "Merchant B"))
        dao.insertReceipt(sampleReceipt(merchant = "Merchant C"))

        val all = dao.getAllReceipts().first()
        assertEquals(3, all.size)
    }

    @Test
    fun deleteReceiptRemovesIt() = runBlocking {
        val id = dao.insertReceipt(sampleReceipt()).toInt()
        val receipt = dao.getReceiptById(id)!!
        dao.deleteReceipt(receipt)

        val retrieved = dao.getReceiptById(id)
        assertNull(retrieved)
    }

    @Test
    fun updateReceiptPersistsChanges() = runBlocking {
        val id = dao.insertReceipt(sampleReceipt(amount = 10.00)).toInt()
        val receipt = dao.getReceiptById(id)!!
        dao.updateReceipt(receipt.copy(amount = 99.99))

        val updated = dao.getReceiptById(id)
        assertEquals(99.99, updated?.amount ?: 0.0, 0.001)
    }

    @Test
    fun markAsSyncedUpdatesFlag() = runBlocking {
        val id = dao.insertReceipt(sampleReceipt()).toInt()
        dao.markAsSynced(id)

        val receipt = dao.getReceiptById(id)
        assertTrue(receipt?.syncedToCloud == true)
    }

    @Test
    fun getUnsyncedReturnsOnlyUnsynced() = runBlocking {
        dao.insertReceipt(sampleReceipt())
        dao.insertReceipt(sampleReceipt())
        val syncedId = dao.insertReceipt(sampleReceipt()).toInt()
        dao.markAsSynced(syncedId)

        val unsynced = dao.getUnsyncedReceipts()
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.none { it.syncedToCloud })
    }

    @Test
    fun getFilteredReceiptsByMerchant() = runBlocking {
        dao.insertReceipt(sampleReceipt(merchant = "Starbucks"))
        dao.insertReceipt(sampleReceipt(merchant = "McDonald's"))
        dao.insertReceipt(sampleReceipt(merchant = "Starbucks Reserve"))

        val results = dao.getFilteredReceipts(merchant = "Starbucks").first()
        assertEquals(2, results.size)
        assertTrue(results.all { it.merchant.contains("Starbucks") })
    }

    @Test
    fun getFilteredReceiptsByCategory() = runBlocking {
        dao.insertReceipt(sampleReceipt(category = "Food & Dining"))
        dao.insertReceipt(sampleReceipt(category = "Transport"))
        dao.insertReceipt(sampleReceipt(category = "Food & Dining"))

        val results = dao.getFilteredReceipts(category = "Food & Dining").first()
        assertEquals(2, results.size)
    }

    @Test
    fun getReceiptsByMonthFiltersCorrectly() = runBlocking {
        dao.insertReceipt(sampleReceipt(date = "2024-03-10"))
        dao.insertReceipt(sampleReceipt(date = "2024-03-20"))
        dao.insertReceipt(sampleReceipt(date = "2024-04-05"))

        val results = dao.getReceiptsByMonth("2024-03").first()
        assertEquals(2, results.size)
    }

    @Test
    fun getMonthlyTotalSumsCorrectly() = runBlocking {
        dao.insertReceipt(sampleReceipt(amount = 10.00, date = "2024-03-01"))
        dao.insertReceipt(sampleReceipt(amount = 20.50, date = "2024-03-15"))
        dao.insertReceipt(sampleReceipt(amount = 5.00, date = "2024-04-01"))

        val total = dao.getMonthlyTotal("2024-03").first()
        assertEquals(30.50, total ?: 0.0, 0.001)
    }

    @Test
    fun getCategoryTotalsGroupsCorrectly() = runBlocking {
        dao.insertReceipt(sampleReceipt(amount = 10.0, category = "Food & Dining", date = "2024-03-01"))
        dao.insertReceipt(sampleReceipt(amount = 15.0, category = "Food & Dining", date = "2024-03-02"))
        dao.insertReceipt(sampleReceipt(amount = 50.0, category = "Transport", date = "2024-03-03"))

        val totals = dao.getCategoryTotalsForMonth("2024-03").first()
        assertEquals(2, totals.size)

        val foodTotal = totals.firstOrNull { it.category == "Food & Dining" }
        assertNotNull(foodTotal)
        assertEquals(25.0, foodTotal?.total ?: 0.0, 0.001)
    }

    @Test
    fun deleteAllReceiptsClearsDatabase() = runBlocking {
        dao.insertReceipt(sampleReceipt())
        dao.insertReceipt(sampleReceipt())
        dao.deleteAllReceipts()

        val all = dao.getAllReceipts().first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun getAllCategoriesReturnsDistinctSorted() = runBlocking {
        dao.insertReceipt(sampleReceipt(category = "Transport"))
        dao.insertReceipt(sampleReceipt(category = "Food & Dining"))
        dao.insertReceipt(sampleReceipt(category = "Transport"))

        val categories = dao.getAllCategories().first()
        assertEquals(2, categories.size)
        assertEquals(categories.sorted(), categories)
    }
}
