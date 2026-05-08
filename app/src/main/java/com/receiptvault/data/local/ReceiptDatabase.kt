package com.receiptvault.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.receiptvault.data.local.dao.ReceiptDao
import com.receiptvault.data.local.entities.Receipt

@Database(
    entities = [Receipt::class],
    version = 1,
    exportSchema = false
)
abstract class ReceiptDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}
