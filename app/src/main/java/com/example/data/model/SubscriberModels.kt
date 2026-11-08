package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

enum class SubscriberStatus(val label: String, val color: Color) {
    ACTIVE("Active License", SuccessEmerald),
    TRIAL("Free Trial", AccentTeal),
    GRACE_PERIOD("Grace Period", WarningAmber),
    PAUSED("Paused", Color(0xFF64748B)),
    CANCELLED("Cancelled", Color(0xFFEF4444))
}

enum class InvoiceStatus(val label: String, val color: Color) {
    PAID("Paid", SuccessEmerald),
    PENDING("Payment Due", WarningAmber),
    REFUNDED("Refunded", Color(0xFF64748B))
}

data class SubscriberCompany(
    val id: String,
    val companyName: String,
    val contactName: String,
    val email: String,
    val phone: String,
    val city: String,
    val joinedDate: String,
    val plan: SubscriptionPlan,
    val status: SubscriberStatus,
    val totalItemsProcessed: Int,
    val assignedPickersCount: Int,
    val activeManagersCount: Int,
    val lastPaymentDate: String,
    val totalSpendGbp: Double,
    val vatNumber: String = "GB 849 2041 88",
    val billingAddress: String = "14 Wholesale Boulevard, Unit 3B, $city, UK",
    val portalDomain: String = "apps.sellershub.io/stockpick",
    val autoRenew: Boolean = true
) {
    val isNearItemLimit: Boolean
        get() = !plan.isUnlimitedItems && totalItemsProcessed >= (plan.maxItemsQuota * 0.85)

    val isOverItemLimit: Boolean
        get() = !plan.isUnlimitedItems && totalItemsProcessed >= plan.maxItemsQuota

    val isFreemium: Boolean
        get() = plan.isFreeSubscription || plan.planId == "plan_freemium"
}

data class SubscriberInvoice(
    val invoiceId: String,
    val subscriberId: String,
    val subscriberName: String,
    val issueDate: String,
    val dueDate: String,
    val billingPeriod: String,
    val planTierName: String,
    val billingCycle: BillingCycle,
    val subtotalGbp: Double,
    val vatRatePercent: Int = 20,
    val status: InvoiceStatus,
    val paymentMethod: String,
    val transactionRef: String,
    val voucherCodeApplied: String? = null,
    val downloadUrl: String = "https://apps.sellershub.io/stockpick/invoices/download/"
) {
    val vatAmountGbp: Double get() = subtotalGbp * (vatRatePercent / 100.0)
    val totalAmountGbp: Double get() = subtotalGbp + vatAmountGbp
}

data class PaymentRecord(
    val transactionId: String,
    val subscriberId: String,
    val subscriberName: String,
    val timestamp: String,
    val amountGbp: Double,
    val paymentMethod: String,
    val status: String = "SUCCESS",
    val invoiceRef: String,
    val gatewayReference: String = "ch_live_${(100000..999999).random()}"
)

data class WebPortalConfig(
    val hostUrl: String = "https://apps.sellershub.io/stockpick",
    val apiBaseUrl: String = "https://apps.sellershub.io/stockpick/api/v1",
    val webhookStripeUrl: String = "https://apps.sellershub.io/stockpick/webhooks/stripe",
    val portalStatus: String = "LIVE & OPERATIONAL",
    val version: String = "v3.8.4-cloud",
    val activeSubscribersCount: Int = 6,
    val lastSyncTimestamp: String = "Just now",
    val isStripeConnected: Boolean = true,
    val isAutoSyncEnabled: Boolean = true
)

object SubscriberDirectory {
    val INITIAL_SUBSCRIBERS: List<SubscriberCompany> = listOf(
        SubscriberCompany(
            id = "SUB-84920",
            companyName = "Apex Wholesale Logistics Ltd.",
            contactName = "Alexander Vance",
            email = "billing@apexwholesale.co.uk",
            phone = "+44 20 7946 0912",
            city = "London",
            joinedDate = "01 Jan 2026",
            plan = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[1], // Wholesale Pro
            status = SubscriberStatus.ACTIVE,
            totalItemsProcessed = 642,
            assignedPickersCount = 8,
            activeManagersCount = 2,
            lastPaymentDate = "14 Oct 2026",
            totalSpendGbp = 390.00,
            vatNumber = "GB 948 2018 44"
        ),
        SubscriberCompany(
            id = "SUB-10294",
            companyName = "QuickPick Express Hub",
            contactName = "Liam Gallagher",
            email = "accounts@quickpickexpress.com",
            phone = "+44 161 496 0382",
            city = "Manchester",
            joinedDate = "10 Feb 2026",
            plan = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[0], // Public Freemium Starter
            status = SubscriberStatus.TRIAL,
            totalItemsProcessed = 188,
            assignedPickersCount = 3,
            activeManagersCount = 1,
            lastPaymentDate = "Free Trial Active",
            totalSpendGbp = 0.00,
            vatNumber = "GB 712 9013 11"
        ),
        SubscriberCompany(
            id = "SUB-39014",
            companyName = "Northern Depot Direct",
            contactName = "Beatrice Sterling",
            email = "finance@northerndepot.co.uk",
            phone = "+44 113 496 0291",
            city = "Leeds",
            joinedDate = "15 Mar 2026",
            plan = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[2], // Enterprise Multi-Depot
            status = SubscriberStatus.ACTIVE,
            totalItemsProcessed = 7810,
            assignedPickersCount = 34,
            activeManagersCount = 6,
            lastPaymentDate = "01 Oct 2026",
            totalSpendGbp = 948.00,
            vatNumber = "GB 394 8812 00"
        ),
        SubscriberCompany(
            id = "SUB-55921",
            companyName = "Metro Asian Foods Wholesale",
            contactName = "Rajesh Patel",
            email = "admin@metrofoods-wholesale.com",
            phone = "+44 121 496 0884",
            city = "Birmingham",
            joinedDate = "02 Apr 2026",
            plan = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[1].copy(
                billingCycle = BillingCycle.ANNUAL,
                voucherDiscountPercent = 20
            ),
            status = SubscriberStatus.ACTIVE,
            totalItemsProcessed = 890,
            assignedPickersCount = 10,
            activeManagersCount = 3,
            lastPaymentDate = "15 Sep 2026",
            totalSpendGbp = 449.28,
            vatNumber = "GB 559 2100 77"
        ),
        SubscriberCompany(
            id = "SUB-70102",
            companyName = "SellersHub Cloud Fulfillment",
            contactName = "David Thorne",
            email = "partner@sellershub.io",
            phone = "+44 117 496 0520",
            city = "Bristol",
            joinedDate = "12 Dec 2025",
            plan = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[2].copy(
                isFreeSubscription = true,
                name = "Enterprise VIP Partner Grant"
            ),
            status = SubscriberStatus.ACTIVE,
            totalItemsProcessed = 9200,
            assignedPickersCount = 45,
            activeManagersCount = 8,
            lastPaymentDate = "Super Admin Free Grant",
            totalSpendGbp = 0.00,
            vatNumber = "GB 701 0288 33"
        ),
        SubscriberCompany(
            id = "SUB-90312",
            companyName = "Highland Goods & Grocery",
            contactName = "Fiona MacLeod",
            email = "orders@highlandgoods.scot",
            phone = "+44 141 496 0773",
            city = "Glasgow",
            joinedDate = "28 Sep 2026",
            plan = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[0].copy(
                trialDaysRemaining = 3
            ),
            status = SubscriberStatus.TRIAL,
            totalItemsProcessed = 239,
            assignedPickersCount = 4,
            activeManagersCount = 2,
            lastPaymentDate = "Trial Expiring Soon",
            totalSpendGbp = 0.00,
            vatNumber = "GB 903 1245 99"
        )
    )

    val INITIAL_INVOICES: List<SubscriberInvoice> = listOf(
        SubscriberInvoice(
            invoiceId = "INV-2026-0849",
            subscriberId = "SUB-84920",
            subscriberName = "Apex Wholesale Logistics Ltd.",
            issueDate = "14 Oct 2026",
            dueDate = "14 Oct 2026",
            billingPeriod = "14 Oct 2026 – 13 Nov 2026",
            planTierName = "Wholesale Pro (Monthly)",
            billingCycle = BillingCycle.MONTHLY,
            subtotalGbp = 39.00,
            vatRatePercent = 20,
            status = InvoiceStatus.PAID,
            paymentMethod = "Stripe Card (Visa •••• 4242)",
            transactionRef = "TXN-2026-0849-ST"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0780",
            subscriberId = "SUB-84920",
            subscriberName = "Apex Wholesale Logistics Ltd.",
            issueDate = "14 Sep 2026",
            dueDate = "14 Sep 2026",
            billingPeriod = "14 Sep 2026 – 13 Oct 2026",
            planTierName = "Wholesale Pro (Monthly)",
            billingCycle = BillingCycle.MONTHLY,
            subtotalGbp = 39.00,
            vatRatePercent = 20,
            status = InvoiceStatus.PAID,
            paymentMethod = "Stripe Card (Visa •••• 4242)",
            transactionRef = "TXN-2026-0780-ST"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0710",
            subscriberId = "SUB-84920",
            subscriberName = "Apex Wholesale Logistics Ltd.",
            issueDate = "14 Aug 2026",
            dueDate = "14 Aug 2026",
            billingPeriod = "14 Aug 2026 – 13 Sep 2026",
            planTierName = "Wholesale Pro (Monthly)",
            billingCycle = BillingCycle.MONTHLY,
            subtotalGbp = 39.00,
            vatRatePercent = 20,
            status = InvoiceStatus.PAID,
            paymentMethod = "Stripe Card (Visa •••• 4242)",
            transactionRef = "TXN-2026-0710-ST"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0792",
            subscriberId = "SUB-39014",
            subscriberName = "Northern Depot Direct",
            issueDate = "01 Oct 2026",
            dueDate = "01 Oct 2026",
            billingPeriod = "01 Oct 2026 – 31 Oct 2026",
            planTierName = "Enterprise Multi-Depot",
            billingCycle = BillingCycle.MONTHLY,
            subtotalGbp = 79.00,
            vatRatePercent = 20,
            status = InvoiceStatus.PAID,
            paymentMethod = "Direct Debit (BACS UK)",
            transactionRef = "TXN-2026-0792-DD"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0715",
            subscriberId = "SUB-55921",
            subscriberName = "Metro Asian Foods Wholesale",
            issueDate = "15 Sep 2026",
            dueDate = "15 Sep 2026",
            billingPeriod = "15 Sep 2026 – 14 Sep 2027",
            planTierName = "Wholesale Pro Annual (20% Off)",
            billingCycle = BillingCycle.ANNUAL,
            subtotalGbp = 374.40,
            vatRatePercent = 20,
            status = InvoiceStatus.PAID,
            paymentMethod = "Stripe Card (Mastercard •••• 8821)",
            transactionRef = "TXN-2026-0715-ST",
            voucherCodeApplied = "ANNUAL20"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0688",
            subscriberId = "SUB-10294",
            subscriberName = "QuickPick Express Hub",
            issueDate = "10 Sep 2026",
            dueDate = "10 Oct 2026",
            billingPeriod = "10 Sep 2026 – 09 Oct 2026",
            planTierName = "Public Freemium Starter (Free Tier)",
            billingCycle = BillingCycle.FREE_TRIAL,
            subtotalGbp = 0.00,
            vatRatePercent = 0,
            status = InvoiceStatus.PAID,
            paymentMethod = "Public Freemium Tier (100% Free)",
            transactionRef = "TXN-2026-0688-FREE"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0641",
            subscriberId = "SUB-70102",
            subscriberName = "SellersHub Cloud Fulfillment",
            issueDate = "01 Sep 2026",
            dueDate = "01 Oct 2026",
            billingPeriod = "01 Sep 2026 – 31 Aug 2027",
            planTierName = "Enterprise VIP Partner Grant",
            billingCycle = BillingCycle.ANNUAL,
            subtotalGbp = 0.00,
            vatRatePercent = 0,
            status = InvoiceStatus.PAID,
            paymentMethod = "Super Admin VIP Grant (No Charge)",
            transactionRef = "TXN-2026-0641-GRANT",
            voucherCodeApplied = "VIP_PARTNER"
        ),
        SubscriberInvoice(
            invoiceId = "INV-2026-0901",
            subscriberId = "SUB-90312",
            subscriberName = "Highland Goods & Grocery",
            issueDate = "12 Oct 2026",
            dueDate = "19 Oct 2026",
            billingPeriod = "19 Oct 2026 – 18 Nov 2026",
            planTierName = "Wholesale Pro (Trial Upgrade)",
            billingCycle = BillingCycle.MONTHLY,
            subtotalGbp = 39.00,
            vatRatePercent = 20,
            status = InvoiceStatus.PENDING,
            paymentMethod = "Pending Payment (Invoice Sent)",
            transactionRef = "TXN-PENDING-90312"
        )
    )

    val INITIAL_TRANSACTIONS: List<PaymentRecord> = listOf(
        PaymentRecord(
            transactionId = "TXN-2026-0849-ST",
            subscriberId = "SUB-84920",
            subscriberName = "Apex Wholesale Logistics Ltd.",
            timestamp = "14 Oct 2026, 09:15",
            amountGbp = 46.80,
            paymentMethod = "Stripe CC (•••• 4242)",
            status = "SETTLED",
            invoiceRef = "INV-2026-0849"
        ),
        PaymentRecord(
            transactionId = "TXN-2026-0792-DD",
            subscriberId = "SUB-39014",
            subscriberName = "Northern Depot Direct",
            timestamp = "01 Oct 2026, 04:00",
            amountGbp = 94.80,
            paymentMethod = "BACS UK Direct Debit",
            status = "SETTLED",
            invoiceRef = "INV-2026-0792"
        ),
        PaymentRecord(
            transactionId = "TXN-2026-0715-ST",
            subscriberId = "SUB-55921",
            subscriberName = "Metro Asian Foods Wholesale",
            timestamp = "15 Sep 2026, 14:32",
            amountGbp = 449.28,
            paymentMethod = "Stripe CC (•••• 8821)",
            status = "SETTLED",
            invoiceRef = "INV-2026-0715"
        ),
        PaymentRecord(
            transactionId = "TXN-2026-0688-FREE",
            subscriberId = "SUB-10294",
            subscriberName = "QuickPick Express Hub",
            timestamp = "10 Sep 2026, 11:20",
            amountGbp = 0.00,
            paymentMethod = "Freemium License (Zero Charge)",
            status = "SETTLED",
            invoiceRef = "INV-2026-0688"
        )
    )
}
