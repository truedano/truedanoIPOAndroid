package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IpoAuctionDao {

    @Query("SELECT * FROM ipo_auctions ORDER BY auctionDate DESC, stockCode ASC")
    fun getAll(): Flow<List<IpoAuctionEntity>>

    @Query("SELECT * FROM ipo_auctions WHERE stockCode = :stockCode LIMIT 1")
    suspend fun getByCode(stockCode: String): IpoAuctionEntity?

    @Query("SELECT COUNT(*) FROM ipo_auctions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: IpoAuctionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<IpoAuctionEntity>)

    @Query("DELETE FROM ipo_auctions WHERE stockCode = :stockCode")
    suspend fun deleteByCode(stockCode: String)

    @Query("DELETE FROM ipo_auctions WHERE isPreset = 0")
    suspend fun clearNonPresets()

    @Query("DELETE FROM ipo_auctions")
    suspend fun clearAll()
}
