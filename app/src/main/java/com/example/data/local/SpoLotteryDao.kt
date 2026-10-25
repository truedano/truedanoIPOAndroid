package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpoLotteryDao {

    @Query("SELECT * FROM spo_lotteries ORDER BY lotteryDate DESC")
    fun getAllRecords(): Flow<List<SpoLotteryEntity>>

    @Query("SELECT * FROM spo_lotteries WHERE stockCode = :code LIMIT 1")
    suspend fun getByStockCode(code: String): SpoLotteryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<SpoLotteryEntity>)

    @Query("DELETE FROM spo_lotteries WHERE isPreset = 0")
    suspend fun clearNonPresets()
}
