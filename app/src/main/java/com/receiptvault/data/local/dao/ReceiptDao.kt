package com.receiptvault.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.receiptvault.data.local.entities.Receipt
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {

    @Query("SELECT * FROM receipts ORDER BY date DESC")
    fun getAllReceipts(): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE id = :id")
    suspend fun getReceiptById(id: Int): Receipt?

    @Query("SELECT * FROM receipts WHERE syncedToCloud = 0")
    suspend fun getUnsyncedReceipts(): List<Receipt>

    @Query("""
        SELECT * FROM receipts 
        WHERE (:merchant IS NULL OR merchant LIKE '%' || :merchant || '%')
        AND (:category IS NULL OR category = :category)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC
    """)
    fun getFilteredReceipts(
        merchant: String? = null,
        category: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<List<Receipt>>

    @Query("SELECT * FROM receipts WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getReceiptsByMonth(monthPrefix: String): Flow<List<Receipt>>

    @Query("SELECT SUM(amount) FROM receipts WHERE date LIKE :monthPrefix || '%'")
    fun getMonthlyTotal(monthPrefix: String): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM receipts WHERE date LIKE :monthPrefix || '%' GROUP BY category")
    fun getCategoryTotalsForMonth(monthPrefix: String): Flow<List<CategoryTotal>>

    @Query("SELECT DISTINCT category FROM receipts ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: Receipt): Long

    @Update
    suspend fun updateReceipt(receipt: Receipt)

    @Delete
    suspend fun deleteReceipt(receipt: Receipt)

    @Query("UPDATE receipts SET syncedToCloud = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
