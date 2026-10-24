package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [IpoAuctionEntity::class], version = 1, exportSchema = false)
abstract class IpoDatabase : RoomDatabase() {

    abstract fun ipoAuctionDao(): IpoAuctionDao

    companion object {
        @Volatile
        private var INSTANCE: IpoDatabase? = null

        fun getInstance(context: Context): IpoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IpoDatabase::class.java,
                    "ipo_auction_engine.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
