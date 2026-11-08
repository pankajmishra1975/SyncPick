package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class PickingRepository(
    private val pickingDao: PickingDao,
    private val auditDao: PickingAuditDao,
    private val userDao: UserDao,
    private val orderDao: OrderDao,
    private val subscriptionDao: SubscriptionDao
) {
    val activeItems: Flow<List<PickingItem>> = pickingDao.getActivePickingItems()
    val allItems: Flow<List<PickingItem>> = pickingDao.getAllItems()
    val pickedItems: Flow<List<PickingItem>> = pickingDao.getPickedItems()
    val auditLogs: Flow<List<PickingAuditLog>> = auditDao.getAllLogs()

    val pendingCount: Flow<Int> = pickingDao.getPendingCount()
    val pickedCount: Flow<Int> = pickingDao.getPickedCount()
    val totalCount: Flow<Int> = pickingDao.getTotalCount()

    // Offline-first Users, Orders, and Subscription streams
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val activeOrders: Flow<List<OrderEntity>> = orderDao.getActiveOrders()
    val activeSubscription: Flow<SubscriptionEntity?> = subscriptionDao.getSubscriptionById()

    // --- Offline-First User CRUD Operations ---
    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)
    suspend fun deleteUserById(id: String) = userDao.deleteUserById(id)
    suspend fun recordUserLogin(userId: String) = userDao.recordUserLogin(userId)
    suspend fun findUserByCode(code: String): UserEntity? = userDao.findUserByCode(code)

    // --- Offline-First Order CRUD Operations ---
    suspend fun insertOrder(order: OrderEntity): Long = orderDao.insertOrder(order)
    suspend fun updateOrderStatus(orderNumber: String, status: String, completedAt: Long? = null) =
        orderDao.updateOrderStatus(orderNumber, status, completedAt)
    suspend fun updateOrderProgress(orderNumber: String, pickedUnits: Int) =
        orderDao.updateOrderProgress(orderNumber, pickedUnits)
    suspend fun deleteOrder(orderNumber: String) = orderDao.deleteOrderByNumber(orderNumber)

    // --- Offline-First Subscription CRUD & Feature Flag Operations ---
    suspend fun insertOrUpdateSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.insertSubscription(subscription)
    suspend fun updateSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.updateSubscription(subscription)
    suspend fun deleteSubscription(id: String) =
        subscriptionDao.deleteSubscriptionById(id)
    suspend fun updateSubscriptionFeatureFlags(
        id: String = "active_subscription",
        allowCamera: Boolean,
        allowPdf: Boolean,
        allowApi: Boolean,
        allowSim: Boolean,
        allowKpi: Boolean,
        allowBarcode: Boolean
    ) = subscriptionDao.updateFeatureFlags(id, allowCamera, allowPdf, allowApi, allowSim, allowKpi, allowBarcode)
    suspend fun updateCameraPermission(id: String = "active_subscription", allowCamera: Boolean) =
        subscriptionDao.updateCameraPermission(id, allowCamera)
    suspend fun updatePdfPermission(id: String = "active_subscription", allowPdf: Boolean) =
        subscriptionDao.updatePdfPermission(id, allowPdf)
    suspend fun updateApiSyncPermission(id: String = "active_subscription", allowApi: Boolean) =
        subscriptionDao.updateApiSyncPermission(id, allowApi)
    suspend fun updateSubscriptionTrialStatus(
        id: String = "active_subscription",
        status: String,
        daysRemaining: Int,
        expirationDate: Long
    ) = subscriptionDao.updateTrialStatus(id, status, daysRemaining, expirationDate)
    suspend fun updateSubscriptionPlanLevel(
        id: String = "active_subscription",
        planLevel: String,
        planName: String,
        isFree: Boolean,
        maxItems: Int,
        maxPickers: Int,
        maxManagers: Int,
        priceGbp: Double
    ) = subscriptionDao.updatePlanLevel(id, planLevel, planName, isFree, maxItems, maxPickers, maxManagers, priceGbp)
    suspend fun applySubscriptionVoucher(
        id: String = "active_subscription",
        voucherCode: String,
        discountPercent: Int
    ) = subscriptionDao.applyVoucher(id, voucherCode, discountPercent)

    suspend fun claimItem(item: PickingItem, pickerId: String, pickerName: String): Boolean {
        val now = System.currentTimeMillis()
        val rows = pickingDao.claimItem(item.id, pickerId, pickerName, now)
        if (rows > 0) {
            auditDao.insertLog(
                PickingAuditLog(
                    itemId = item.id,
                    orderNumber = item.orderNumber,
                    itemTitle = item.title,
                    pickerId = pickerId,
                    pickerName = pickerName,
                    teamName = item.assignedTeam,
                    sourceStore = item.sourceStore,
                    salesChannel = item.salesChannel,
                    actionType = "CLAIMED",
                    quantity = item.quantityRequired,
                    timestamp = now,
                    note = "Claimed for picking"
                )
            )
            return true
        }
        return false
    }

    suspend fun unclaimItem(item: PickingItem) {
        pickingDao.unclaimItem(item.id)
    }

    suspend fun completePick(
        item: PickingItem,
        pickerId: String,
        pickerName: String,
        teamName: String,
        durationSeconds: Int = 45
    ): Boolean {
        val now = System.currentTimeMillis()
        val rows = pickingDao.completePick(item.id, pickerId, pickerName, now)
        if (rows > 0) {
            auditDao.insertLog(
                PickingAuditLog(
                    itemId = item.id,
                    orderNumber = item.orderNumber,
                    itemTitle = item.title,
                    pickerId = pickerId,
                    pickerName = pickerName,
                    teamName = teamName,
                    sourceStore = item.sourceStore,
                    salesChannel = item.salesChannel,
                    actionType = "PICKED",
                    quantity = item.quantityRequired,
                    timestamp = now,
                    durationSeconds = durationSeconds,
                    note = "Completed store pick"
                )
            )
            return true
        }
        return false
    }

    suspend fun updatePartialPick(
        item: PickingItem,
        newQty: Int,
        pickerId: String,
        pickerName: String,
        teamName: String
    ) {
        val now = System.currentTimeMillis()
        pickingDao.updatePickQuantity(item.id, newQty, pickerId, pickerName, now)
        if (newQty >= item.quantityRequired) {
            auditDao.insertLog(
                PickingAuditLog(
                    itemId = item.id,
                    orderNumber = item.orderNumber,
                    itemTitle = item.title,
                    pickerId = pickerId,
                    pickerName = pickerName,
                    teamName = teamName,
                    sourceStore = item.sourceStore,
                    salesChannel = item.salesChannel,
                    actionType = "PICKED",
                    quantity = newQty,
                    timestamp = now,
                    durationSeconds = 60,
                    note = "Completed full pick in increments"
                )
            )
        }
    }

    suspend fun markOutOfStock(
        item: PickingItem,
        reason: String,
        pickerId: String,
        pickerName: String,
        teamName: String
    ) {
        val now = System.currentTimeMillis()
        pickingDao.markOutOfStock(item.id, reason)
        auditDao.insertLog(
            PickingAuditLog(
                itemId = item.id,
                orderNumber = item.orderNumber,
                itemTitle = item.title,
                pickerId = pickerId,
                pickerName = pickerName,
                teamName = teamName,
                sourceStore = item.sourceStore,
                salesChannel = item.salesChannel,
                actionType = "OUT_OF_STOCK",
                quantity = 0,
                timestamp = now,
                note = "Out of stock: $reason"
            )
        )
    }

    suspend fun submitPickerFieldReport(
        item: PickingItem,
        comment: String,
        conditionTags: List<String>,
        photoUri: String?,
        pickerId: String,
        pickerName: String,
        teamName: String
    ) {
        val now = System.currentTimeMillis()
        val tagsString = conditionTags.joinToString(", ")
        pickingDao.updatePickerFieldReport(
            id = item.id,
            comment = comment.trim(),
            conditionTags = tagsString,
            photoUri = photoUri,
            pickerName = pickerName,
            timestamp = now
        )
        val summaryNote = buildString {
            if (conditionTags.isNotEmpty()) append("[${tagsString}] ")
            if (comment.isNotBlank()) append(comment.trim())
            if (!photoUri.isNullOrBlank()) append(" (Aisle/Product photo attached)")
        }.trim()

        auditDao.insertLog(
            PickingAuditLog(
                itemId = item.id,
                orderNumber = item.orderNumber,
                itemTitle = item.title,
                pickerId = pickerId,
                pickerName = pickerName,
                teamName = teamName,
                sourceStore = item.sourceStore,
                salesChannel = item.salesChannel,
                actionType = "FIELD_REPORT",
                quantity = item.quantityRequired,
                timestamp = now,
                note = summaryNote.ifBlank { "Updated store condition report" },
                conditionTags = tagsString,
                photoUri = photoUri
            )
        )
    }

    suspend fun insertItem(item: PickingItem): Long {
        val now = System.currentTimeMillis()
        val id = pickingDao.insertItem(item)
        auditDao.insertLog(
            PickingAuditLog(
                itemId = id,
                orderNumber = item.orderNumber,
                itemTitle = item.title,
                pickerId = "admin",
                pickerName = "Admin Dispatcher",
                teamName = item.assignedTeam,
                sourceStore = item.sourceStore,
                salesChannel = item.salesChannel,
                actionType = "DISPATCHED",
                quantity = item.quantityRequired,
                timestamp = now,
                note = "Dispatched order to picking queue"
            )
        )
        return id
    }

    suspend fun insertBatch(items: List<PickingItem>) {
        pickingDao.insertAll(items)
    }

    suspend fun redispatchItem(item: PickingItem) {
        pickingDao.redispatchItem(item.id)
    }

    suspend fun deleteItem(item: PickingItem) {
        pickingDao.deleteItem(item)
    }

    suspend fun clearAll() {
        pickingDao.clearAll()
        auditDao.clearLogs()
    }
}
