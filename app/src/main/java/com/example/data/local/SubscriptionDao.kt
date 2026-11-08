package com.example.data.local

import androidx.room.*
import com.example.data.model.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions WHERE id = :id LIMIT 1")
    fun getSubscriptionById(id: String = "active_subscription"): Flow<SubscriptionEntity?>

    @Query("SELECT * FROM subscriptions WHERE id = :id LIMIT 1")
    suspend fun getSubscriptionByIdOnce(id: String = "active_subscription"): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteSubscriptionById(id: String)

    // Feature Flags Management
    @Query("""
        UPDATE subscriptions 
        SET allowCameraUse = :allowCamera,
            allowPdfUploads = :allowPdf,
            allowApiSync = :allowApi,
            allowLiveSimulation = :allowSim,
            allowAdvancedKpiExport = :allowKpi,
            allowBarcodeScanning = :allowBarcode,
            lastUpdatedTimestamp = :timestamp
        WHERE id = :id
    """)
    suspend fun updateFeatureFlags(
        id: String = "active_subscription",
        allowCamera: Boolean,
        allowPdf: Boolean,
        allowApi: Boolean,
        allowSim: Boolean,
        allowKpi: Boolean,
        allowBarcode: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE subscriptions SET allowCameraUse = :allowCamera, lastUpdatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateCameraPermission(
        id: String = "active_subscription",
        allowCamera: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE subscriptions SET allowPdfUploads = :allowPdf, lastUpdatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updatePdfPermission(
        id: String = "active_subscription",
        allowPdf: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    @Query("UPDATE subscriptions SET allowApiSync = :allowApi, lastUpdatedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateApiSyncPermission(
        id: String = "active_subscription",
        allowApi: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    // Trial Status & Expiration Management
    @Query("""
        UPDATE subscriptions 
        SET trialStatus = :trialStatus,
            trialDaysRemaining = :daysRemaining,
            expirationDate = :expirationDate,
            lastUpdatedTimestamp = :timestamp
        WHERE id = :id
    """)
    suspend fun updateTrialStatus(
        id: String = "active_subscription",
        trialStatus: String,
        daysRemaining: Int,
        expirationDate: Long,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    // Plan Level & Quotas Management
    @Query("""
        UPDATE subscriptions 
        SET planLevel = :planLevel,
            planName = :planName,
            isFreeSubscription = :isFree,
            maxItemsQuota = :maxItems,
            maxPickersQuota = :maxPickers,
            maxManagersQuota = :maxManagers,
            monthlyPriceGbp = :priceGbp,
            lastUpdatedTimestamp = :timestamp
        WHERE id = :id
    """)
    suspend fun updatePlanLevel(
        id: String = "active_subscription",
        planLevel: String,
        planName: String,
        isFree: Boolean,
        maxItems: Int,
        maxPickers: Int,
        maxManagers: Int,
        priceGbp: Double,
        timestamp: Long = System.currentTimeMillis()
    ): Int

    // Voucher Management
    @Query("""
        UPDATE subscriptions 
        SET activeVoucherCode = :voucherCode,
            voucherDiscountPercent = :discountPercent,
            lastUpdatedTimestamp = :timestamp
        WHERE id = :id
    """)
    suspend fun applyVoucher(
        id: String = "active_subscription",
        voucherCode: String,
        discountPercent: Int,
        timestamp: Long = System.currentTimeMillis()
    ): Int
}
