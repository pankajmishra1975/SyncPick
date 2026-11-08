package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picking_items")
data class PickingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val title: String,
    val quantityRequired: Int,
    val quantityPicked: Int = 0,
    val unit: String = "Units",
    val sourceStore: String, // Costco Wholesale Ltd, Bookers Cash & Carry, Bestway Wholesale, Loon Fung, Sainsbury's, Tesco's
    val salesChannel: String, // Amazon, eBay, Webstore
    val assignedTeam: String = "All Teams", // All Teams, Team Alpha (North), Team Beta (South), Team Central
    val status: String = STATUS_PENDING, // PENDING, IN_PROGRESS, PICKED, OUT_OF_STOCK
    val claimedByPickerId: String? = null,
    val claimedByPickerName: String? = null,
    val pickedByPickerId: String? = null,
    val pickedByPickerName: String? = null,
    val imageResName: String? = null,
    val imageUri: String? = null,
    val barcode: String = "",
    val aisleLocation: String = "",
    val notes: String = "",
    val priority: String = PRIORITY_NORMAL, // NORMAL, HIGH, RUSH
    val pickerComment: String = "",
    val pickerConditionTags: String = "", // Comma-separated tags: Damaged, New Packaging, Price Increase, On Offer / Promo, Low Shelf Stock
    val pickerPhotoUri: String? = null, // Photo of store aisle, price tag, or product condition
    val pickerReportedBy: String? = null,
    val pickerReportedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val claimedAt: Long? = null,
    val pickedAt: Long? = null
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_PICKED = "PICKED"
        const val STATUS_OUT_OF_STOCK = "OUT_OF_STOCK"

        const val PRIORITY_NORMAL = "NORMAL"
        const val PRIORITY_HIGH = "HIGH"
        const val PRIORITY_RUSH = "RUSH"

        val CONDITION_TAGS = listOf(
            "On Offer / Promo",
            "Price Increase",
            "New Packaging",
            "Damaged Packaging",
            "Short Expiry Date",
            "Low Shelf Stock",
            "Moved Aisle / Endcap"
        )
    }

    val isPending: Boolean
        get() = status == STATUS_PENDING

    val isClaimed: Boolean
        get() = status == STATUS_IN_PROGRESS

    val isPicked: Boolean
        get() = status == STATUS_PICKED

    val isOutOfStock: Boolean
        get() = status == STATUS_OUT_OF_STOCK

    val remainingQty: Int
        get() = (quantityRequired - quantityPicked).coerceAtLeast(0)

    val hasPickerReport: Boolean
        get() = pickerComment.isNotBlank() || pickerConditionTags.isNotBlank() || !pickerPhotoUri.isNullOrBlank()

    val conditionTagList: List<String>
        get() = pickerConditionTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
}
