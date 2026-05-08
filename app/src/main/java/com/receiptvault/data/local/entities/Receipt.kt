package com.receiptvault.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class Receipt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val merchant: String,
    val amount: Double,
    val date: String,
    val category: String,
    val imageUri: String,
    val notes: String? = null,
    val syncedToCloud: Boolean = false
)
