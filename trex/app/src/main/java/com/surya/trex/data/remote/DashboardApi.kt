package com.surya.trex.data.remote

import com.surya.trex.data.model.CategorySummary
import com.surya.trex.data.model.DashboardSummary
import com.surya.trex.data.model.RecentTransaction
import retrofit2.http.GET
import retrofit2.http.Header

interface DashboardApi {

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") token: String
    ): DashboardSummary

    @GET("dashboard/categories")
    suspend fun getCategorySummary(
        @Header("Authorization") token: String
    ): List<CategorySummary>

    @GET("dashboard/recent")
    suspend fun getRecentTransactions(
        @Header("Authorization") token: String
    ): List<RecentTransaction>
}