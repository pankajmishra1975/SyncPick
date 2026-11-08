package com.example.data.local

import androidx.room.*
import com.example.data.model.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status IN ('PENDING', 'IN_PROGRESS') ORDER BY CASE priority WHEN 'RUSH' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END, createdAt DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE salesChannel = :channel ORDER BY createdAt DESC")
    fun getOrdersByChannel(channel: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    fun getOrderByNumber(orderNumber: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByNumberOnce(orderNumber: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Query("DELETE FROM orders WHERE orderNumber = :orderNumber")
    suspend fun deleteOrderByNumber(orderNumber: String)

    @Query("UPDATE orders SET status = :status, completedAt = :completedAt WHERE orderNumber = :orderNumber")
    suspend fun updateOrderStatus(orderNumber: String, status: String, completedAt: Long? = null): Int

    @Query("UPDATE orders SET totalUnitsPicked = :pickedUnits, status = CASE WHEN :pickedUnits >= totalUnitsRequired THEN 'COMPLETED' ELSE status END, completedAt = CASE WHEN :pickedUnits >= totalUnitsRequired THEN :timestamp ELSE completedAt END WHERE orderNumber = :orderNumber")
    suspend fun updateOrderProgress(orderNumber: String, pickedUnits: Int, timestamp: Long = System.currentTimeMillis()): Int
}
