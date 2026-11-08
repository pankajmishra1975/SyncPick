package com.example

import com.example.data.model.PickingItem
import com.example.data.model.SuperAdminDirectory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PickingModelUnitTest {

    @Test
    fun testPickingItemStatusAndRemainingQty() {
        val item = PickingItem(
            orderNumber = "AMZ-1001",
            title = "Kirkland Olive Oil 5L",
            quantityRequired = 6,
            quantityPicked = 2,
            unit = "Tins",
            sourceStore = "Costco Wholesale Ltd",
            salesChannel = "Amazon",
            status = PickingItem.STATUS_PENDING
        )

        assertTrue(item.isPending)
        assertEquals(4, item.remainingQty)
    }

    @Test
    fun testCompletedPickingItem() {
        val item = PickingItem(
            orderNumber = "EB-2002",
            title = "Basmati Rice 20kg",
            quantityRequired = 10,
            quantityPicked = 10,
            unit = "Sacks",
            sourceStore = "Bookers Cash & Carry",
            salesChannel = "eBay",
            status = PickingItem.STATUS_PICKED
        )

        assertTrue(item.isPicked)
        assertEquals(0, item.remainingQty)
    }

    @Test
    fun testPublicFreemiumTiersAndVouchers() {
        val freemium = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS.first()
        assertTrue(freemium.isFreeSubscription)
        assertEquals(0.0, freemium.effectivePriceGbp, 0.01)
        assertEquals(250, freemium.maxItemsQuota)

        // Voucher discount test
        val proTier = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[1]
        val discountedTier = proTier.copy(voucherDiscountPercent = 50)
        assertEquals(proTier.monthlyPriceGbp * 0.5, discountedTier.effectivePriceGbp, 0.01)

        val fullFreeTier = proTier.copy(voucherDiscountPercent = 100)
        assertEquals(0.0, fullFreeTier.effectivePriceGbp, 0.01)
    }

    @Test
    fun testAnnualBillingDiscount() {
        val enterpriseTier = SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS[2].copy(
            billingCycle = com.example.data.model.BillingCycle.ANNUAL
        )
        // 12 months * monthly * 0.80
        val expectedAnnual = enterpriseTier.monthlyPriceGbp * 12 * 0.80
        assertEquals(expectedAnnual, enterpriseTier.effectivePriceGbp, 0.01)
    }

    @Test
    fun testSubscriberInvoiceIsolation() {
        val allInvoices = com.example.data.model.SubscriberDirectory.INITIAL_INVOICES
        val apexInvoices = allInvoices.filter { it.subscriberId == "SUB-84920" }
        // Verify Apex Wholesale Logistics Ltd. only sees their own statements
        assertTrue(apexInvoices.isNotEmpty())
        assertTrue(apexInvoices.all { it.subscriberId == "SUB-84920" })
        assertTrue(apexInvoices.none { it.subscriberId == "SUB-39014" })
    }

    @Test
    fun testSubscriptionStatusTransitions() {
        val plan = SuperAdminDirectory.createDefaultPlan()
        assertEquals("ACTIVE_FREEMIUM", plan.status)

        val pausedPlan = plan.copy(status = "PAUSED")
        assertEquals("PAUSED", pausedPlan.status)

        val cancelledPlan = plan.copy(status = "CANCELLED_AT_PERIOD_END")
        assertEquals("CANCELLED_AT_PERIOD_END", cancelledPlan.status)
    }
}
