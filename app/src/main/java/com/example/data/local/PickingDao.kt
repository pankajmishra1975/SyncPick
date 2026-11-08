package com.example.data.local

import androidx.room.*
import com.example.data.model.PickingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PickingDao {

    // Fetch items that still need picking (PENDING or IN_PROGRESS)
    @Query("SELECT * FROM picking_items WHERE status IN ('PENDING', 'IN_PROGRESS') ORDER BY CASE priority WHEN 'RUSH' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END, createdAt DESC")
    fun getActivePickingItems(): Flow<List<PickingItem>>

    // Fetch all items for admin monitoring and logs
    @Query("SELECT * FROM picking_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<PickingItem>>

    @Query("SELECT * FROM picking_items WHERE id = :id")
    suspend fun getItemById(id: Long): PickingItem?

    @Query("SELECT * FROM picking_items WHERE status = 'PICKED' ORDER BY pickedAt DESC")
    fun getPickedItems(): Flow<List<PickingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PickingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PickingItem>)

    @Update
    suspend fun updateItem(item: PickingItem)

    @Delete
    suspend fun deleteItem(item: PickingItem)

    @Query("DELETE FROM picking_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM picking_items")
    suspend fun clearAll()

    // Claim item
    @Query("UPDATE picking_items SET status = 'IN_PROGRESS', claimedByPickerId = :pickerId, claimedByPickerName = :pickerName, claimedAt = :timestamp WHERE id = :id AND status = 'PENDING'")
    suspend fun claimItem(id: Long, pickerId: String, pickerName: String, timestamp: Long): Int

    // Complete pick & strike off
    @Query("UPDATE picking_items SET status = 'PICKED', quantityPicked = quantityRequired, pickedByPickerId = :pickerId, pickedByPickerName = :pickerName, pickedAt = :timestamp WHERE id = :id")
    suspend fun completePick(id: Long, pickerId: String, pickerName: String, timestamp: Long): Int

    // Partial pick
    @Query("UPDATE picking_items SET quantityPicked = :qtyPicked, status = CASE WHEN :qtyPicked >= quantityRequired THEN 'PICKED' ELSE status END, pickedByPickerId = :pickerId, pickedByPickerName = :pickerName, pickedAt = CASE WHEN :qtyPicked >= quantityRequired THEN :timestamp ELSE pickedAt END WHERE id = :id")
    suspend fun updatePickQuantity(id: Long, qtyPicked: Int, pickerId: String, pickerName: String, timestamp: Long): Int

    // Mark out of stock
    @Query("UPDATE picking_items SET status = 'OUT_OF_STOCK', notes = :reason WHERE id = :id")
    suspend fun markOutOfStock(id: Long, reason: String): Int

    // Update picker field report (comments, condition tags like Damaged / New Packaging / Price Increase / On Offer, and aisle/product photo)
    @Query("UPDATE picking_items SET pickerComment = :comment, pickerConditionTags = :conditionTags, pickerPhotoUri = :photoUri, pickerReportedBy = :pickerName, pickerReportedAt = :timestamp WHERE id = :id")
    suspend fun updatePickerFieldReport(
        id: Long,
        comment: String,
        conditionTags: String,
        photoUri: String?,
        pickerName: String,
        timestamp: Long
    ): Int

    // Re-queue / un-claim
    @Query("UPDATE picking_items SET status = 'PENDING', claimedByPickerId = null, claimedByPickerName = null, claimedAt = null WHERE id = :id")
    suspend fun unclaimItem(id: Long): Int

    // Re-dispatch picked item back to queue
    @Query("UPDATE picking_items SET status = 'PENDING', quantityPicked = 0, claimedByPickerId = null, claimedByPickerName = null, pickedByPickerId = null, pickedByPickerName = null, pickedAt = null, claimedAt = null WHERE id = :id")
    suspend fun redispatchItem(id: Long): Int

    @Query("SELECT COUNT(*) FROM picking_items WHERE status IN ('PENDING', 'IN_PROGRESS')")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM picking_items WHERE status = 'PICKED'")
    fun getPickedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM picking_items")
    fun getTotalCount(): Flow<Int>
}
