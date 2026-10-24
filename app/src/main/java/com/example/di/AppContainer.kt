package com.example.di

import android.content.Context
import com.example.data.local.IpoDatabase
import com.example.data.remote.TwseAuctionApiService
import com.example.data.repository.IpoAuctionRepositoryImpl
import com.example.domain.repository.IpoAuctionRepository

class AppContainer(context: Context) {
    val database: IpoDatabase by lazy {
        IpoDatabase.getInstance(context)
    }

    val twseApiService: TwseAuctionApiService by lazy {
        TwseAuctionApiService.create()
    }

    val repository: IpoAuctionRepository by lazy {
        IpoAuctionRepositoryImpl(
            dao = database.ipoAuctionDao(),
            apiService = twseApiService
        )
    }
}
