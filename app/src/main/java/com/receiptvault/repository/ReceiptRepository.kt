package com.receiptvault.repository

import com.receiptvault.data.local.dao.CategoryTotal
import com.receiptvault.data.local.dao.ReceiptDao
import com.receiptvault.data.local.entities.Receipt
import com.receiptvault.data.remote.FirestoreDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReceiptRepository @Inject constructor(
    private val receiptDao: ReceiptDao,
    private val firestoreDataSource: FirestoreDataSource
) {

    fun getAllReceipts(): Flow<List<Receipt>> = receiptDao.getAllReceipts()

    fun getFilteredReceipts(
        merchant: String? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<List<Receipt>> = receiptDao.getFilteredReceipts(
        merchant?.takeIf { it.isNotBlank() },
        category?.takeIf { it.isNotBlank() && it != "All" },
        startDate?.takeIf { it.isNotBlank() },
        endDate?.takeIf { it.isNotBlank() }
    )

    fun getReceiptsByMonth(monthPrefix: String): Flow<List<Receipt>> =
        receiptDao.getReceiptsByMonth(monthPrefix)

    fun getMonthlyTotal(monthPrefix: String): Flow<Double?> =
        receiptDao.getMonthlyTotal(monthPrefix)

    fun getCategoryTotalsForMonth(monthPrefix: String): Flow<List<CategoryTotal>> =
        receiptDao.getCategoryTotalsForMonth(monthPrefix)

    fun getAllCategories(): Flow<List<String>> = receiptDao.getAllCategories()

    suspend fun getReceiptById(id: Int): Receipt? = receiptDao.getReceiptById(id)

    suspend fun insertReceipt(receipt: Receipt): Long {
        val id = receiptDao.insertReceipt(receipt)
        if (firestoreDataSource.isUserLoggedIn()) {
            syncReceiptToCloud(receipt.copy(id = id.toInt()))
        }
        return id
    }

    suspend fun updateReceipt(receipt: Receipt) {
        receiptDao.updateReceipt(receipt)
        if (firestoreDataSource.isUserLoggedIn()) {
            syncReceiptToCloud(receipt)
        }
    }

    suspend fun deleteReceipt(receipt: Receipt) {
        receiptDao.deleteReceipt(receipt)
        if (firestoreDataSource.isUserLoggedIn()) {
            runCatching { firestoreDataSource.deleteReceipt(receipt.id) }
        }
    }

    suspend fun syncAllToCloud() {
        if (!firestoreDataSource.isUserLoggedIn()) return
        val unsynced = receiptDao.getUnsyncedReceipts()
        unsynced.forEach { receipt ->
            runCatching {
                syncReceiptToCloud(receipt)
            }
        }
    }

    private suspend fun syncReceiptToCloud(receipt: Receipt) {
        runCatching {
            firestoreDataSource.uploadReceipt(receipt)
            receiptDao.markAsSynced(receipt.id)
        }
    }

    suspend fun getUnsyncedReceipts(): List<Receipt> = receiptDao.getUnsyncedReceipts()
}
