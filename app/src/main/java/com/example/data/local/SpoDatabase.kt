package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SpoLotteryEntity::class], version = 1, exportSchema = false)
abstract class SpoDatabase : RoomDatabase() {

    abstract fun spoLotteryDao(): SpoLotteryDao

    companion object {
        @Volatile
        private var INSTANCE: SpoDatabase? = null

        fun getInstance(context: Context): SpoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpoDatabase::class.java,
                    "spo_lottery_engine.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
