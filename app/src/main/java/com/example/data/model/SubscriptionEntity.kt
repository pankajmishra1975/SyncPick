package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val id: String = "active_subscription",
    val subscriberId: String = "SUB-84920",
    val subscriberName: String = "Apex Wholesale Logistics Ltd.",
    val planLevel: String = "FREEMIUM", // FREEMIUM, STARTER_PRO, ENTERPRISE, VIP_GRANT
    val planName: String = "Public Freemium Starter",
    val planDescription: String = "Entry-level cloud fulfillment for small merchant wholesalers with offline local Room persistence.",
    val billingCycle: String = "MONTHLY", // FREE_TRIAL, MONTHLY, ANNUAL
    val isFreeSubscription: Boolean = true,
    val trialStatus: String = "ACTIVE_TRIAL", // ACTIVE_TRIAL, TRIAL_EXPIRED, REGULAR_ACTIVE, GRACE_PERIOD, PAUSED
    val trialDaysRemaining: Int = 30,
    val expirationDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Expiration timestamp in ms
    val maxItemsQuota: Int = 250,
    val maxManagersQuota: Int = 2,
    val maxPickersQuota: Int = 4,
    val monthlyPriceGbp: Double = 0.0,
    val activeVoucherCode: String? = null,
    val voucherDiscountPercent: Int = 0,
    val grantedBySuperAdminId: String = "super_alex",
    val grantedBySuperAdminName: String = "Alexander Vance",
    
    // Feature flags & access permissions
    val allowCameraUse: Boolean = true,
    val allowPdfUploads: Boolean = true,
    val allowApiSync: Boolean = true,
    val allowLiveSimulation: Boolean = true,
    val allowAdvancedKpiExport: Boolean = true,
    val allowBarcodeScanning: Boolean = true,
    
    val supportTier: String = "STANDARD", // STANDARD, PREMIUM_CHAT
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val isOfflineCached: Boolean = true
) {
    fun toSubscriptionPlan(): SubscriptionPlan {
        return SubscriptionPlan(
            planId = when (planLevel) {
                "STARTER_PRO" -> "plan_starter_pro"
                "ENTERPRISE" -> "plan_enterprise"
                else -> "plan_freemium"
            },
            name = planName,
            description = planDescription,
            billingCycle = when (billingCycle) {
                "ANNUAL" -> BillingCycle.ANNUAL
                "FREE_TRIAL" -> BillingCycle.FREE_TRIAL
                else -> BillingCycle.MONTHLY
            },
            isFreeSubscription = isFreeSubscription,
            trialDaysRemaining = trialDaysRemaining,
            maxItemsQuota = maxItemsQuota,
            maxManagersQuota = maxManagersQuota,
            maxPickersQuota = maxPickersQuota,
            supportTier = if (supportTier == "PREMIUM_CHAT") SupportTier.PREMIUM_CHAT else SupportTier.STANDARD,
            features = PlanFeatures(
                allowCameraUse = allowCameraUse,
                allowPdfUploads = allowPdfUploads,
                allowApiSync = allowApiSync,
                allowLiveSimulation = allowLiveSimulation,
                allowAdvancedKpiExport = allowAdvancedKpiExport,
                allowBarcodeScanning = allowBarcodeScanning
            ),
            monthlyPriceGbp = monthlyPriceGbp,
            activeVoucherCode = activeVoucherCode,
            voucherDiscountPercent = voucherDiscountPercent,
            grantedBySuperAdminId = grantedBySuperAdminId,
            grantedBySuperAdminName = grantedBySuperAdminName,
            status = trialStatus
        )
    }

    companion object {
        fun fromSubscriptionPlan(
            plan: SubscriptionPlan,
            subscriberId: String = "SUB-84920",
            subscriberName: String = "Apex Wholesale Logistics Ltd."
        ): SubscriptionEntity {
            return SubscriptionEntity(
                id = "active_subscription",
                subscriberId = subscriberId,
                subscriberName = subscriberName,
                planLevel = when (plan.planId) {
                    "plan_starter_pro" -> "STARTER_PRO"
                    "plan_enterprise" -> "ENTERPRISE"
                    else -> if (plan.isFreeSubscription) "FREEMIUM" else "STARTER_PRO"
                },
                planName = plan.name,
                planDescription = plan.description,
                billingCycle = plan.billingCycle.name,
                isFreeSubscription = plan.isFreeSubscription,
                trialStatus = plan.status,
                trialDaysRemaining = plan.trialDaysRemaining,
                expirationDate = System.currentTimeMillis() + (plan.trialDaysRemaining.toLong() * 24 * 60 * 60 * 1000),
                maxItemsQuota = plan.maxItemsQuota,
                maxManagersQuota = plan.maxManagersQuota,
                maxPickersQuota = plan.maxPickersQuota,
                monthlyPriceGbp = plan.monthlyPriceGbp,
                activeVoucherCode = plan.activeVoucherCode,
                voucherDiscountPercent = plan.voucherDiscountPercent,
                grantedBySuperAdminId = plan.grantedBySuperAdminId,
                grantedBySuperAdminName = plan.grantedBySuperAdminName,
                allowCameraUse = plan.features.allowCameraUse,
                allowPdfUploads = plan.features.allowPdfUploads,
                allowApiSync = plan.features.allowApiSync,
                allowLiveSimulation = plan.features.allowLiveSimulation,
                allowAdvancedKpiExport = plan.features.allowAdvancedKpiExport,
                allowBarcodeScanning = plan.features.allowBarcodeScanning,
                supportTier = plan.supportTier.name,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
        }
    }
}
