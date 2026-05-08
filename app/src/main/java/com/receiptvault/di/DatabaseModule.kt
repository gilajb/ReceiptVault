package com.receiptvault.di

import android.content.Context
import androidx.room.Room
import com.receiptvault.data.local.ReceiptDatabase
import com.receiptvault.data.local.dao.ReceiptDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideReceiptDatabase(@ApplicationContext context: Context): ReceiptDatabase =
        Room.databaseBuilder(
            context,
            ReceiptDatabase::class.java,
            "receipt_vault_db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    @Singleton
    fun provideReceiptDao(database: ReceiptDatabase): ReceiptDao =
        database.receiptDao()
}
