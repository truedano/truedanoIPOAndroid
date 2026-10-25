package com.example.di

import android.content.Context
import com.example.data.local.IpoDatabase
import com.example.data.local.SpoDatabase
import com.example.data.remote.TwseAuctionApiService
import com.example.data.remote.TwseSubscriptionApiService
import com.example.data.repository.IpoAuctionRepositoryImpl
import com.example.data.repository.SpoLotteryRepositoryImpl
import com.example.domain.repository.IpoAuctionRepository
import com.example.domain.repository.SpoLotteryRepository

class AppContainer(context: Context) {
    val database: IpoDatabase by lazy {
        IpoDatabase.getInstance(context)
    }

    val spoDatabase: SpoDatabase by lazy {
        SpoDatabase.getInstance(context)
    }

    val twseApiService: TwseAuctionApiService by lazy {
        TwseAuctionApiService.create()
    }

    val twseSubscriptionApiService: TwseSubscriptionApiService by lazy {
        TwseSubscriptionApiService.create()
    }

    val repository: IpoAuctionRepository by lazy {
        IpoAuctionRepositoryImpl(
            dao = database.ipoAuctionDao(),
            apiService = twseApiService,
            subscriptionApiService = twseSubscriptionApiService
        )
    }

    val spoRepository: SpoLotteryRepository by lazy {
        SpoLotteryRepositoryImpl(
            dao = spoDatabase.spoLotteryDao(),
            apiService = twseSubscriptionApiService
        )
    }
}
