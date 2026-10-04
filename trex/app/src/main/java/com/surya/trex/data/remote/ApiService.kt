package com.surya.trex.data.remote

import com.surya.trex.data.model.Category
import com.surya.trex.data.model.CategoryCreateRequest
import com.surya.trex.data.model.CategoryUpdateRequest
import com.surya.trex.data.model.CategorySummary
import com.surya.trex.data.model.DashboardSummary
import com.surya.trex.data.model.PendingTransaction
import com.surya.trex.data.model.PendingTransactionCreateRequest
import com.surya.trex.data.model.RecentTransaction
import com.surya.trex.data.model.SettingsUpdateRequest
import com.surya.trex.data.model.Transaction
import com.surya.trex.data.model.TransactionCreateRequest
import com.surya.trex.data.model.TransactionSource
import com.surya.trex.data.model.TransactionSourceCreateRequest
import com.surya.trex.data.model.TransactionSourceUpdateRequest
import com.surya.trex.data.model.UserProfile
import com.surya.trex.data.model.UserProfileUpdate
import com.surya.trex.data.model.UserSettings

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {

    // ============================================================
    // TRANSACTIONS
    // ============================================================

    @POST("transactions/")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionCreateRequest
    ): Response<Transaction>


    @GET("transactions/")
    suspend fun getTransactions(
        @Header("Authorization") token: String
    ): Response<List<Transaction>>


    // ============================================================
    // UPDATE TRANSACTION
    // ============================================================

    @PUT("transactions/{transactionId}")
    suspend fun updateTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: Int,
        @Body request: TransactionCreateRequest
    ): Response<Transaction>


    // ============================================================
    // DELETE TRANSACTION
    // ============================================================

    @DELETE("transactions/{transactionId}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("transactionId") transactionId: Int
    ): Response<Unit>



// ============================================================
// CATEGORIES
// ============================================================

    // GET ALL CATEGORIES
    @GET("categories/")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): Response<List<Category>>


    // CREATE CATEGORY
    @POST("categories/")
    suspend fun createCategory(
        @Header("Authorization") token: String,
        @Body request: CategoryCreateRequest
    ): Response<Category>


    // GET SINGLE CATEGORY
    @GET("categories/{categoryId}")
    suspend fun getCategory(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Int
    ): Response<Category>


    // UPDATE CATEGORY
    @PUT("categories/{categoryId}")
    suspend fun updateCategory(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Int,
        @Body request: CategoryUpdateRequest
    ): Response<Category>


    // DELETE CATEGORY
    @DELETE("categories/{categoryId}")
    suspend fun deleteCategory(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Int
    ): Response<Unit>

    // ============================================================
    // DASHBOARD
    // ============================================================

    @GET("dashboard/summary")
    suspend fun getDashboardSummary(
        @Header("Authorization") token: String
    ): Response<DashboardSummary>


    @GET("dashboard/categories")
    suspend fun getDashboardCategories(
        @Header("Authorization") token: String
    ): Response<List<CategorySummary>>


    @GET("dashboard/recent")
    suspend fun getRecentTransactions(
        @Header("Authorization") token: String
    ): Response<List<RecentTransaction>>


    // ============================================================
    // PROFILE
    // ============================================================

    @GET("users/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): UserProfile


    @PATCH("users/me")
    suspend fun updateMyProfile(
        @Header("Authorization") token: String,
        @Body request: UserProfileUpdate
    ): UserProfile


    // ============================================================
    // SETTINGS
    // ============================================================

    @GET("settings")
    suspend fun getSettings(
        @Header("Authorization") token: String
    ): Response<UserSettings>


    @PUT("settings")
    suspend fun updateSettings(
        @Header("Authorization") token: String,
        @Body request: SettingsUpdateRequest
    ): Response<UserSettings>


    // ============================================================
    // TRANSACTION SOURCES
    // ============================================================

    @GET("settings/sources")
    suspend fun getTransactionSources(
        @Header("Authorization") token: String
    ): Response<List<TransactionSource>>


    @POST("settings/sources")
    suspend fun createTransactionSource(
        @Header("Authorization") token: String,
        @Body request: TransactionSourceCreateRequest
    ): Response<TransactionSource>


    @PUT("settings/sources/{sourceId}")
    suspend fun updateTransactionSource(
        @Header("Authorization") token: String,
        @Path("sourceId") sourceId: Int,
        @Body request: TransactionSourceUpdateRequest
    ): Response<TransactionSource>


    @DELETE("settings/sources/{sourceId}")
    suspend fun deleteTransactionSource(
        @Header("Authorization") token: String,
        @Path("sourceId") sourceId: Int
    ): Response<Unit>


    // ============================================================
    // PENDING TRANSACTIONS
    // ============================================================

    @GET("pending-transactions")
    suspend fun getPendingTransactions(
        @Header("Authorization") token: String
    ): Response<List<PendingTransaction>>


    @POST("pending-transactions")
    suspend fun createPendingTransaction(
        @Header("Authorization") token: String,
        @Body request: PendingTransactionCreateRequest
    ): Response<PendingTransaction>


    // ============================================================
    // UPDATE PENDING TRANSACTION CATEGORY
    // ============================================================

    @PATCH("pending-transactions/{pendingId}/category")
    suspend fun updatePendingTransactionCategory(
        @Header("Authorization") token: String,
        @Path("pendingId") pendingId: Int,
        @Query("category_id") categoryId: Int
    ): Response<PendingTransaction>


    // ============================================================
    // APPROVE PENDING TRANSACTION
    // ============================================================

    @POST("pending-transactions/{pendingId}/approve")
    suspend fun approvePendingTransaction(
        @Header("Authorization") token: String,
        @Path("pendingId") pendingId: Int
    ): Response<Map<String, Any>>


    // ============================================================
    // DENY PENDING TRANSACTION
    // ============================================================

    @POST("pending-transactions/{pendingId}/deny")
    suspend fun denyPendingTransaction(
        @Header("Authorization") token: String,
        @Path("pendingId") pendingId: Int
    ): Response<Map<String, Any>>


    // ============================================================
    // DELETE PENDING TRANSACTION
    // ============================================================

    @DELETE("pending-transactions/{pendingId}")
    suspend fun deletePendingTransaction(
        @Header("Authorization") token: String,
        @Path("pendingId") pendingId: Int
    ): Response<Map<String, Any>>
}
