package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey
    val orderNumber: String,
    val salesChannel: String, // Amazon, eBay, Webstore
    val customerName: String = "Wholesale Trade Merchant",
    val destinationCity: String = "London, UK",
    val sourceStore: String, // Costco Wholesale Ltd, Bookers Cash & Carry, etc.
    val assignedTeam: String = "All Teams",
    val status: String = STATUS_PENDING, // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    val itemCount: Int = 1,
    val totalUnitsRequired: Int = 1,
    val totalUnitsPicked: Int = 0,
    val priority: String = PRIORITY_NORMAL, // NORMAL, HIGH, RUSH
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val isOfflineCreated: Boolean = true
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"

        const val PRIORITY_NORMAL = "NORMAL"
        const val PRIORITY_HIGH = "HIGH"
        const val PRIORITY_RUSH = "RUSH"
    }

    val isCompleted: Boolean get() = status == STATUS_COMPLETED
    val progressFraction: Float get() = if (totalUnitsRequired > 0) totalUnitsPicked.toFloat() / totalUnitsRequired else 0f
}
