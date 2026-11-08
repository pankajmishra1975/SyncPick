package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picking_audit_logs")
data class PickingAuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemId: Long,
    val orderNumber: String,
    val itemTitle: String,
    val pickerId: String,
    val pickerName: String,
    val teamName: String,
    val sourceStore: String,
    val salesChannel: String,
    val actionType: String, // PICKED, CLAIMED, PARTIAL_PICK, OUT_OF_STOCK, DISPATCHED, FIELD_REPORT
    val quantity: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val note: String = "",
    val conditionTags: String = "",
    val photoUri: String? = null
)
