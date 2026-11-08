package com.example.data.local

import androidx.room.*
import com.example.data.model.PickingAuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface PickingAuditDao {

    @Query("SELECT * FROM picking_audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PickingAuditLog>>

    @Query("SELECT * FROM picking_audit_logs WHERE pickerId = :pickerId ORDER BY timestamp DESC")
    fun getLogsByPicker(pickerId: String): Flow<List<PickingAuditLog>>

    @Query("SELECT * FROM picking_audit_logs WHERE sourceStore = :storeName ORDER BY timestamp DESC")
    fun getLogsByStore(storeName: String): Flow<List<PickingAuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PickingAuditLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<PickingAuditLog>)

    @Query("DELETE FROM picking_audit_logs")
    suspend fun clearLogs()
}
