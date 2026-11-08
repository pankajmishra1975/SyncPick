package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class SuperAdminRole(val title: String, val badgeColor: Color) {
    PLATFORM_OWNER("Platform Owner", Color(0xFF7C3AED)),
    BILLING_DIRECTOR("Billing & Subscription Director", Color(0xFF2563EB)),
    LICENSE_OPERATOR("License & Voucher Manager", Color(0xFF059669)),
    TIER_AUDITOR("Tier & Quota Auditor", Color(0xFFD97706))
}

enum class SupportTier(val title: String, val slaDescription: String) {
    STANDARD("Standard Support", "Email/Admin ticket resolved in 3–5 business days"),
    PREMIUM_CHAT("Premium Real-time Chat", "24/7 Priority Live Chat Support (< 15 mins)")
}

enum class BillingCycle(val title: String, val months: Int, val discountPercent: Int) {
    FREE_TRIAL("Free Trial", 0, 0),
    MONTHLY("Monthly Plan", 1, 0),
    ANNUAL("Annual Plan (Save 20%)", 12, 20)
}

data class PlanFeatures(
    val allowCameraUse: Boolean = true,
    val allowPdfUploads: Boolean = true,
    val allowApiSync: Boolean = true,
    val allowLiveSimulation: Boolean = true,
    val allowAdvancedKpiExport: Boolean = true,
    val allowBarcodeScanning: Boolean = true
)

data class SubscriptionPlan(
    val planId: String,
    val name: String,
    val description: String,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val isFreeSubscription: Boolean = false,
    val trialDaysRemaining: Int = 14,
    val maxItemsQuota: Int = 500, // 0 = unlimited or capped count
    val maxManagersQuota: Int = 3,
    val maxPickersQuota: Int = 10,
    val supportTier: SupportTier = SupportTier.STANDARD,
    val features: PlanFeatures = PlanFeatures(),
    val monthlyPriceGbp: Double = 49.00,
    val activeVoucherCode: String? = null,
    val voucherDiscountPercent: Int = 0,
    val grantedBySuperAdminId: String = "super_alex",
    val grantedBySuperAdminName: String = "Alexander Vance",
    val status: String = "ACTIVE" // ACTIVE, TRIAL, EXPIRED, PAUSED
) {
    val isUnlimitedItems: Boolean get() = maxItemsQuota <= 0 || maxItemsQuota >= 99999
    val isUnlimitedManagers: Boolean get() = maxManagersQuota <= 0 || maxManagersQuota >= 999
    val isUnlimitedPickers: Boolean get() = maxPickersQuota <= 0 || maxPickersQuota >= 999

    val effectivePriceGbp: Double
        get() {
            if (isFreeSubscription) return 0.0
            val base = if (billingCycle == BillingCycle.ANNUAL) monthlyPriceGbp * 12 * 0.80 else monthlyPriceGbp
            val discountFactor = (100 - voucherDiscountPercent).coerceAtLeast(0) / 100.0
            return base * discountFactor
        }
}

data class SuperAdminMember(
    val id: String,
    val name: String,
    val email: String,
    val role: SuperAdminRole,
    val pinCode: String = "7777",
    val avatarInitials: String = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
)

object SuperAdminDirectory {
    val SUPER_ADMINS = listOf(
        SuperAdminMember(
            id = "super_alex",
            name = "Alexander Vance",
            email = "alex.vance@wholesalesync.com",
            role = SuperAdminRole.PLATFORM_OWNER,
            pinCode = "7777"
        ),
        SuperAdminMember(
            id = "super_beatrice",
            name = "Beatrice Sterling",
            email = "beatrice.s@wholesalesync.com",
            role = SuperAdminRole.BILLING_DIRECTOR,
            pinCode = "7778"
        ),
        SuperAdminMember(
            id = "super_clara",
            name = "Clara Dupont",
            email = "clara.d@wholesalesync.com",
            role = SuperAdminRole.LICENSE_OPERATOR,
            pinCode = "7779"
        ),
        SuperAdminMember(
            id = "super_devon",
            name = "Devon Miller",
            email = "devon.m@wholesalesync.com",
            role = SuperAdminRole.TIER_AUDITOR,
            pinCode = "7780"
        )
    )

    val PRESET_VOUCHERS = mapOf(
        "WHOLESALE100" to 100, // 100% free
        "SUPER50" to 50,       // 50% discount
        "DISPATCH25" to 25,    // 25% discount
        "FREETRIAL30" to 100,  // 30 days free voucher
        "FREEMIUM_PRO" to 100, // Freemium upgrade voucher
        "LAUNCH50" to 50       // 50% off launch discount
    )

    val PUBLIC_FREEMIUM_TIERS = listOf(
        SubscriptionPlan(
            planId = "plan_freemium",
            name = "Public Freemium Starter",
            description = "Entry-level cloud fulfillment for small merchant wholesalers & trial users.",
            billingCycle = BillingCycle.MONTHLY,
            isFreeSubscription = true,
            trialDaysRemaining = 30,
            maxItemsQuota = 250,
            maxManagersQuota = 2,
            maxPickersQuota = 4,
            supportTier = SupportTier.STANDARD,
            features = PlanFeatures(
                allowCameraUse = true,
                allowPdfUploads = true,
                allowApiSync = false,
                allowLiveSimulation = true,
                allowAdvancedKpiExport = false,
                allowBarcodeScanning = true
            ),
            monthlyPriceGbp = 0.00,
            status = "ACTIVE_FREEMIUM"
        ),
        SubscriptionPlan(
            planId = "plan_starter_pro",
            name = "Wholesale Pro",
            description = "Essential picking for multi-store wholesale hubs with PDF extraction & camera evidence.",
            billingCycle = BillingCycle.MONTHLY,
            isFreeSubscription = false,
            trialDaysRemaining = 14,
            maxItemsQuota = 1000,
            maxManagersQuota = 4,
            maxPickersQuota = 12,
            supportTier = SupportTier.STANDARD,
            features = PlanFeatures(
                allowCameraUse = true,
                allowPdfUploads = true,
                allowApiSync = true,
                allowLiveSimulation = true,
                allowAdvancedKpiExport = true,
                allowBarcodeScanning = true
            ),
            monthlyPriceGbp = 39.00
        ),
        SubscriptionPlan(
            planId = "plan_enterprise",
            name = "Enterprise Multi-Depot",
            description = "High-velocity distribution across Amazon, eBay, webstores with 24/7 live priority support.",
            billingCycle = BillingCycle.MONTHLY,
            isFreeSubscription = false,
            trialDaysRemaining = 14,
            maxItemsQuota = 10000,
            maxManagersQuota = 10,
            maxPickersQuota = 50,
            supportTier = SupportTier.PREMIUM_CHAT,
            features = PlanFeatures(
                allowCameraUse = true,
                allowPdfUploads = true,
                allowApiSync = true,
                allowLiveSimulation = true,
                allowAdvancedKpiExport = true,
                allowBarcodeScanning = true
            ),
            monthlyPriceGbp = 79.00
        )
    )

    fun createDefaultPlan(): SubscriptionPlan {
        // Default to the Public Freemium Starter plan with active free subscription
        return PUBLIC_FREEMIUM_TIERS.first()
    }
}
