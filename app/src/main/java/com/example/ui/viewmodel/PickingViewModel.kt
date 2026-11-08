package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PickingDatabase
import com.example.data.model.*
import com.example.data.remote.ApiSyncResult
import com.example.data.remote.ExtractedPicklistRow
import com.example.data.remote.OrderApiIngestionService
import com.example.data.remote.PicklistPdfParseResult
import com.example.data.remote.PicklistPdfParserService
import com.example.data.repository.PickingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LiveBroadcastEvent(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val pickerName: String,
    val storeName: String,
    val itemTitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PickerProductivityStat(
    val picker: PickerProfile,
    val itemsPickedCount: Int,
    val totalUnitsPicked: Int,
    val averageSpeedSecs: Int,
    val activeClaimsCount: Int,
    val lastActiveTimestamp: Long
)

data class StoreProductivityStat(
    val storeName: String,
    val shortName: String,
    val totalItems: Int,
    val pickedItems: Int,
    val pendingItems: Int,
    val completionPercent: Float
)

data class ChannelProductivityStat(
    val channelName: String,
    val totalItems: Int,
    val pickedItems: Int,
    val percentOfTotal: Float
)

data class PickingUiState(
    val isAuthenticated: Boolean = false,
    val loginError: String? = null,
    val activePicker: PickerProfile = TeamDirectory.PICKERS[0],
    val selectedStore: String = "All Stores",
    val selectedChannel: String = "All Channels",
    val selectedTeam: String = "All Teams",
    val searchQuery: String = "",
    val isAdminMode: Boolean = false,
    val isSuperAdminMode: Boolean = false,
    val isSimulationActive: Boolean = false,
    val isSyncingApi: Boolean = false,
    val lastApiSyncResult: ApiSyncResult? = null,
    val apiSyncHistory: List<ApiSyncResult> = emptyList(),
    val currentPdfParseResult: PicklistPdfParseResult? = null,
    val subscriptionPlan: SubscriptionPlan = SuperAdminDirectory.createDefaultPlan(),
    val latestBroadcast: LiveBroadcastEvent? = null,
    val recentBroadcasts: List<LiveBroadcastEvent> = emptyList(),
    // Animation trigger tracking: set of item IDs that were recently picked to display strike-off animation
    val strikingItemIds: Set<Long> = emptySet(),
    val isDarkMode: Boolean = false, // Default Light Mode as requested
    val subscribers: List<SubscriberCompany> = SubscriberDirectory.INITIAL_SUBSCRIBERS,
    val invoices: List<SubscriberInvoice> = SubscriberDirectory.INITIAL_INVOICES,
    val paymentTransactions: List<PaymentRecord> = SubscriberDirectory.INITIAL_TRANSACTIONS,
    val webPortalConfig: WebPortalConfig = WebPortalConfig(),
    val selectedSubscriberDetail: SubscriberCompany? = null,
    val viewingInvoiceDetail: SubscriberInvoice? = null,
    val isDeveloperPortalOpen: Boolean = false
)

class PickingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PickingDatabase.getInstance(application)
    val repository = PickingRepository(
        database.pickingDao(),
        database.auditDao(),
        database.userDao(),
        database.orderDao(),
        database.subscriptionDao()
    )
    val apiIngestionService = OrderApiIngestionService()
    val pdfParserService = PicklistPdfParserService()

    private val _uiState = MutableStateFlow(PickingUiState())
    val uiState: StateFlow<PickingUiState> = _uiState.asStateFlow()

    val activeItems: StateFlow<List<PickingItem>> = repository.activeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allItems: StateFlow<List<PickingItem>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pickedItems: StateFlow<List<PickingItem>> = repository.pickedItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<PickingAuditLog>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered items shown to the pickers (strictly items that still need picking)
    val filteredPickingQueue: StateFlow<List<PickingItem>> = combine(
        activeItems,
        _uiState
    ) { items, state ->
        items.filter { item ->
            val matchesStore = state.selectedStore == "All Stores" || item.sourceStore.equals(state.selectedStore, ignoreCase = true)
            val matchesChannel = state.selectedChannel == "All Channels" || item.salesChannel.equals(state.selectedChannel, ignoreCase = true)
            val matchesTeam = state.selectedTeam == "All Teams" || item.assignedTeam == "All Teams" || item.assignedTeam.equals(state.selectedTeam, ignoreCase = true)
            val matchesSearch = state.searchQuery.isBlank() ||
                    item.title.contains(state.searchQuery, ignoreCase = true) ||
                    item.orderNumber.contains(state.searchQuery, ignoreCase = true) ||
                    item.sourceStore.contains(state.searchQuery, ignoreCase = true) ||
                    item.barcode.contains(state.searchQuery, ignoreCase = true)

            matchesStore && matchesChannel && matchesTeam && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Productivity metrics for admin dashboard
    val pickerLeaderboard: StateFlow<List<PickerProductivityStat>> = combine(
        allItems,
        auditLogs
    ) { items, logs ->
        TeamDirectory.PICKERS.map { picker ->
            val pickerLogs = logs.filter { it.pickerId == picker.id && it.actionType == "PICKED" }
            val itemsPicked = pickerLogs.size
            val unitsPicked = pickerLogs.sumOf { it.quantity }
            val avgSpeed = if (pickerLogs.isNotEmpty()) {
                (pickerLogs.map { it.durationSeconds }.filter { it > 0 }.average().takeIf { !it.isNaN() } ?: 45.0).toInt()
            } else {
                0
            }
            val activeClaims = items.count { it.claimedByPickerId == picker.id && it.status == PickingItem.STATUS_IN_PROGRESS }
            val lastActive = pickerLogs.maxOfOrNull { it.timestamp } ?: 0L

            PickerProductivityStat(
                picker = picker,
                itemsPickedCount = itemsPicked,
                totalUnitsPicked = unitsPicked,
                averageSpeedSecs = avgSpeed,
                activeClaimsCount = activeClaims,
                lastActiveTimestamp = lastActive
            )
        }.sortedByDescending { it.itemsPickedCount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeStats: StateFlow<List<StoreProductivityStat>> = allItems.map { items ->
        WholesalerDirectory.STORES.map { storeInfo ->
            val storeItems = items.filter { it.sourceStore.equals(storeInfo.name, ignoreCase = true) }
            val total = storeItems.size
            val picked = storeItems.count { it.status == PickingItem.STATUS_PICKED }
            val pending = total - picked
            val percent = if (total > 0) (picked.toFloat() / total.toFloat()) * 100f else 0f

            StoreProductivityStat(
                storeName = storeInfo.name,
                shortName = storeInfo.shortName,
                totalItems = total,
                pickedItems = picked,
                pendingItems = pending,
                completionPercent = percent
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channelStats: StateFlow<List<ChannelProductivityStat>> = allItems.map { items ->
        val channels = listOf("Amazon", "eBay", "Webstore")
        val totalAll = items.size.coerceAtLeast(1)
        channels.map { channel ->
            val channelItems = items.filter { it.salesChannel.equals(channel, ignoreCase = true) }
            val picked = channelItems.count { it.status == PickingItem.STATUS_PICKED }
            ChannelProductivityStat(
                channelName = channel,
                totalItems = channelItems.size,
                pickedItems = picked,
                percentOfTotal = (channelItems.size.toFloat() / totalAll.toFloat()) * 100f
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var simulationJob: Job? = null

    init {
        // Ensure database initial items are seeded if empty
        viewModelScope.launch {
            val count = database.pickingDao().getItemById(1)
            if (count == null) {
                PickingDatabase.populateInitialData(database)
            }
        }
    }

    fun setSelectedStore(store: String) {
        _uiState.update { it.copy(selectedStore = store) }
    }

    fun setSelectedChannel(channel: String) {
        _uiState.update { it.copy(selectedChannel = channel) }
    }

    fun setSelectedTeam(team: String) {
        _uiState.update { it.copy(selectedTeam = team) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun switchActivePicker(picker: PickerProfile) {
        _uiState.update {
            it.copy(
                activePicker = picker,
                isAdminMode = picker.isAdmin || picker.isSuperAdmin,
                isSuperAdminMode = picker.isSuperAdmin,
                selectedTeam = if (picker.isAdmin || picker.isSuperAdmin) "All Teams" else picker.teamName
            )
        }
    }

    fun setAdminMode(enabled: Boolean) {
        _uiState.update { it.copy(isAdminMode = enabled) }
    }

    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun loginWithCredentials(profile: PickerProfile, enteredPin: String): Boolean {
        if (enteredPin.trim() != profile.pinCode) {
            _uiState.update {
                it.copy(loginError = "Invalid PIN for ${profile.name}. Demo PIN is ${profile.pinCode}")
            }
            return false
        }
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                loginError = null,
                activePicker = profile,
                isAdminMode = profile.isAdmin || profile.isSuperAdmin,
                isSuperAdminMode = profile.isSuperAdmin,
                selectedTeam = if (profile.isAdmin || profile.isSuperAdmin) "All Teams" else profile.teamName
            )
        }
        return true
    }

    fun toggleDarkMode(enableDark: Boolean? = null) {
        _uiState.update {
            it.copy(isDarkMode = enableDark ?: !it.isDarkMode)
        }
    }

    fun loginByRoleAndCredentials(roleType: Int, identifier: String, pin: String): Boolean {
        val trimmedId = identifier.trim()
        val trimmedPin = pin.trim()

        if (trimmedId.isBlank()) {
            _uiState.update {
                it.copy(loginError = if (roleType == 0) "Please enter your Picker Badge or Employee ID" else "Please enter your Manager ID or Username")
            }
            return false
        }

        val profile = when (roleType) {
            0 -> {
                // Picker pool
                TeamDirectory.PICKERS.find {
                    it.employeeCode.equals(trimmedId, ignoreCase = true) ||
                    it.id.equals(trimmedId, ignoreCase = true) ||
                    it.name.equals(trimmedId, ignoreCase = true) ||
                    it.name.contains(trimmedId, ignoreCase = true)
                }
            }
            1 -> {
                // Manager / Admin pool
                TeamDirectory.ADMINS.find {
                    it.employeeCode.equals(trimmedId, ignoreCase = true) ||
                    it.id.equals(trimmedId, ignoreCase = true) ||
                    it.name.equals(trimmedId, ignoreCase = true) ||
                    it.name.contains(trimmedId, ignoreCase = true)
                } ?: TeamDirectory.SUPER_ADMINS.find {
                    it.employeeCode.equals(trimmedId, ignoreCase = true) ||
                    it.id.equals(trimmedId, ignoreCase = true) ||
                    it.name.equals(trimmedId, ignoreCase = true)
                }
            }
            else -> {
                // Developer / Super Admin pool
                TeamDirectory.SUPER_ADMINS.find {
                    it.employeeCode.equals(trimmedId, ignoreCase = true) ||
                    it.id.equals(trimmedId, ignoreCase = true) ||
                    it.name.equals(trimmedId, ignoreCase = true)
                } ?: TeamDirectory.SUPER_ADMINS.first()
            }
        }

        if (profile == null) {
            _uiState.update {
                it.copy(
                    loginError = if (roleType == 0)
                        "Picker ID '$trimmedId' not found. Try demo ID: PK-101 (PIN: 1111)"
                    else
                        "Manager account '$trimmedId' not found. Try demo ID: ADM-01 (PIN: 9999)"
                )
            }
            return false
        }

        if (trimmedPin != profile.pinCode) {
            _uiState.update {
                it.copy(loginError = "Incorrect PIN for ${profile.name}. (Demo PIN: ${profile.pinCode})")
            }
            return false
        }

        _uiState.update {
            it.copy(
                isAuthenticated = true,
                loginError = null,
                activePicker = profile,
                isAdminMode = profile.isAdmin || profile.isSuperAdmin,
                isSuperAdminMode = profile.isSuperAdmin,
                selectedTeam = if (profile.isAdmin || profile.isSuperAdmin) "All Teams" else profile.teamName
            )
        }
        return true
    }

    fun loginAsDeveloper(devPin: String): Boolean {
        val trimmed = devPin.trim()
        val developerProfile = TeamDirectory.SUPER_ADMINS.first()
        if (trimmed == "7777" || trimmed == "0000" || trimmed == developerProfile.pinCode || trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    isAuthenticated = true,
                    loginError = null,
                    activePicker = developerProfile,
                    isAdminMode = true,
                    isSuperAdminMode = true,
                    isDeveloperPortalOpen = true,
                    selectedTeam = "All Teams"
                )
            }
            return true
        } else {
            _uiState.update {
                it.copy(loginError = "Invalid Developer Passcode. Use 7777 or 0000.")
            }
            return false
        }
    }

    fun openDeveloperPortal(open: Boolean) {
        if (!open || _uiState.value.isSuperAdminMode) {
            _uiState.update { it.copy(isDeveloperPortalOpen = open) }
        }
    }

    fun selectSubscriberDetail(subscriber: SubscriberCompany?) {
        _uiState.update { it.copy(selectedSubscriberDetail = subscriber) }
    }

    fun selectInvoiceDetail(invoice: SubscriberInvoice?) {
        _uiState.update { it.copy(viewingInvoiceDetail = invoice) }
    }

    fun updateSubscriberPlan(subscriberId: String, newPlan: SubscriptionPlan) {
        _uiState.update { state ->
            val updatedList = state.subscribers.map { sub ->
                if (sub.id == subscriberId) {
                    sub.copy(
                        plan = newPlan,
                        status = if (newPlan.isFreeSubscription) SubscriberStatus.ACTIVE else sub.status
                    )
                } else sub
            }
            state.copy(
                subscribers = updatedList,
                selectedSubscriberDetail = if (state.selectedSubscriberDetail?.id == subscriberId) {
                    state.selectedSubscriberDetail.copy(plan = newPlan)
                } else state.selectedSubscriberDetail
            )
        }
        val author = _uiState.value.activePicker.name
        broadcastNotification(
            message = "💎 Super Admin $author updated plan for $subscriberId: ${newPlan.name}",
            pickerName = author,
            storeName = "SellersHub Cloud",
            itemTitle = "Plan Modified"
        )
    }

    fun updateSubscriberStatus(subscriberId: String, newStatus: SubscriberStatus) {
        _uiState.update { state ->
            val updatedList = state.subscribers.map { sub ->
                if (sub.id == subscriberId) sub.copy(status = newStatus) else sub
            }
            state.copy(
                subscribers = updatedList,
                selectedSubscriberDetail = if (state.selectedSubscriberDetail?.id == subscriberId) {
                    state.selectedSubscriberDetail.copy(status = newStatus)
                } else state.selectedSubscriberDetail
            )
        }
    }

    fun extendSubscriberTrial(subscriberId: String, extraDays: Int) {
        _uiState.update { state ->
            val updatedList = state.subscribers.map { sub ->
                if (sub.id == subscriberId) {
                    val currentDays = sub.plan.trialDaysRemaining
                    val updatedPlan = sub.plan.copy(trialDaysRemaining = currentDays + extraDays)
                    sub.copy(plan = updatedPlan, status = SubscriberStatus.TRIAL)
                } else sub
            }
            state.copy(
                subscribers = updatedList,
                selectedSubscriberDetail = if (state.selectedSubscriberDetail?.id == subscriberId) {
                    val updatedPlan = state.selectedSubscriberDetail.plan.copy(
                        trialDaysRemaining = state.selectedSubscriberDetail.plan.trialDaysRemaining + extraDays
                    )
                    state.selectedSubscriberDetail.copy(plan = updatedPlan, status = SubscriberStatus.TRIAL)
                } else state.selectedSubscriberDetail
            )
        }
    }

    fun grantSubscriberFreePlan(subscriberId: String, days: Int, maxItems: Int, maxPickers: Int, maxManagers: Int) {
        _uiState.update { state ->
            val updatedList = state.subscribers.map { sub ->
                if (sub.id == subscriberId) {
                    val updatedPlan = sub.plan.copy(
                        isFreeSubscription = true,
                        trialDaysRemaining = days,
                        maxItemsQuota = maxItems,
                        maxPickersQuota = maxPickers,
                        maxManagersQuota = maxManagers,
                        status = "ACTIVE_FREE_GRANT",
                        grantedBySuperAdminId = state.activePicker.id,
                        grantedBySuperAdminName = state.activePicker.name
                    )
                    sub.copy(plan = updatedPlan, status = SubscriberStatus.ACTIVE)
                } else sub
            }
            state.copy(
                subscribers = updatedList,
                selectedSubscriberDetail = if (state.selectedSubscriberDetail?.id == subscriberId) {
                    val updatedPlan = state.selectedSubscriberDetail.plan.copy(
                        isFreeSubscription = true,
                        trialDaysRemaining = days,
                        maxItemsQuota = maxItems,
                        maxPickersQuota = maxPickers,
                        maxManagersQuota = maxManagers
                    )
                    state.selectedSubscriberDetail.copy(plan = updatedPlan, status = SubscriberStatus.ACTIVE)
                } else state.selectedSubscriberDetail
            )
        }
    }

    fun applyVoucherToSubscriber(subscriberId: String, voucherCode: String): Boolean {
        val discount = SuperAdminDirectory.PRESET_VOUCHERS[voucherCode.trim().uppercase()] ?: return false
        _uiState.update { state ->
            val updatedList = state.subscribers.map { sub ->
                if (sub.id == subscriberId) {
                    val updatedPlan = sub.plan.copy(
                        activeVoucherCode = voucherCode.trim().uppercase(),
                        voucherDiscountPercent = discount,
                        isFreeSubscription = discount >= 100 || sub.plan.isFreeSubscription
                    )
                    sub.copy(plan = updatedPlan)
                } else sub
            }
            state.copy(
                subscribers = updatedList,
                selectedSubscriberDetail = if (state.selectedSubscriberDetail?.id == subscriberId) {
                    val updatedPlan = state.selectedSubscriberDetail.plan.copy(
                        activeVoucherCode = voucherCode.trim().uppercase(),
                        voucherDiscountPercent = discount,
                        isFreeSubscription = discount >= 100 || state.selectedSubscriberDetail.plan.isFreeSubscription
                    )
                    state.selectedSubscriberDetail.copy(plan = updatedPlan)
                } else state.selectedSubscriberDetail
            )
        }
        return true
    }

    fun markInvoiceAsPaid(invoiceId: String) {
        _uiState.update { state ->
            val updatedInvoices = state.invoices.map { inv ->
                if (inv.invoiceId == invoiceId) inv.copy(status = InvoiceStatus.PAID) else inv
            }
            val targetInvoice = state.invoices.find { it.invoiceId == invoiceId }
            val newTxn = targetInvoice?.let { inv ->
                PaymentRecord(
                    transactionId = "TXN-${System.currentTimeMillis() % 100000}-ST",
                    subscriberId = inv.subscriberId,
                    subscriberName = inv.subscriberName,
                    timestamp = "Today (Super Admin Settlement)",
                    amountGbp = inv.totalAmountGbp,
                    paymentMethod = "Super Admin Manual Settlement",
                    status = "SETTLED",
                    invoiceRef = inv.invoiceId
                )
            }
            state.copy(
                invoices = updatedInvoices,
                paymentTransactions = if (newTxn != null) listOf(newTxn) + state.paymentTransactions else state.paymentTransactions,
                viewingInvoiceDetail = if (state.viewingInvoiceDetail?.invoiceId == invoiceId) {
                    state.viewingInvoiceDetail.copy(status = InvoiceStatus.PAID)
                } else state.viewingInvoiceDetail
            )
        }
    }

    fun syncWebPortal() {
        _uiState.update { state ->
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
            state.copy(
                webPortalConfig = state.webPortalConfig.copy(
                    lastSyncTimestamp = "Just now at $timestamp",
                    activeSubscribersCount = state.subscribers.size
                )
            )
        }
        val author = _uiState.value.activePicker.name
        broadcastNotification(
            message = "🌐 SellersHub Cloud Sync completed with apps.sellershub.io/stockpick (${_uiState.value.subscribers.size} subscribers, ${_uiState.value.invoices.size} invoices)",
            pickerName = author,
            storeName = "Cloud API",
            itemTitle = "Sync Completed"
        )
    }

    fun selectPlanTier(plan: SubscriptionPlan) {
        val current = _uiState.value.subscriptionPlan
        val updated = plan.copy(
            billingCycle = current.billingCycle,
            activeVoucherCode = current.activeVoucherCode,
            voucherDiscountPercent = current.voucherDiscountPercent
        )
        updateSubscriptionPlan(updated)
    }

    fun toggleBillingCycle() {
        val current = _uiState.value.subscriptionPlan
        val newCycle = if (current.billingCycle == BillingCycle.MONTHLY) BillingCycle.ANNUAL else BillingCycle.MONTHLY
        updateSubscriptionPlan(current.copy(billingCycle = newCycle))
    }

    fun cancelSubscription() {
        val current = _uiState.value.subscriptionPlan
        val updated = current.copy(status = "CANCELLED_AT_PERIOD_END")
        updateSubscriptionPlan(updated)
        val author = _uiState.value.activePicker.name
        broadcastNotification(
            message = "⚠️ $author scheduled subscription cancellation at period end. Portfolio will transition to Starter Freemium.",
            pickerName = author,
            storeName = "Portfolio Settings",
            itemTitle = "Cancellation Scheduled"
        )
    }

    fun pauseSubscription(days: Int = 30) {
        val current = _uiState.value.subscriptionPlan
        val updated = current.copy(status = "PAUSED")
        updateSubscriptionPlan(updated)
        val author = _uiState.value.activePicker.name
        broadcastNotification(
            message = "⏸️ $author paused portfolio subscription for $days days. All picklist history and records are preserved.",
            pickerName = author,
            storeName = "Portfolio Settings",
            itemTitle = "Subscription Paused"
        )
    }

    fun resumeSubscription() {
        val current = _uiState.value.subscriptionPlan
        val updated = current.copy(status = "ACTIVE")
        updateSubscriptionPlan(updated)
        val author = _uiState.value.activePicker.name
        broadcastNotification(
            message = "✅ $author resumed active subscription on ${current.name}.",
            pickerName = author,
            storeName = "Portfolio Settings",
            itemTitle = "Subscription Resumed"
        )
    }

    fun logout() {
        toggleSimulation(false)
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                loginError = null,
                isAdminMode = false,
                isSuperAdminMode = false,
                isDeveloperPortalOpen = false,
                selectedSubscriberDetail = null,
                viewingInvoiceDetail = null
            )
        }
    }

    // --- Super Admin Subscription Management Methods ---

    fun updateSubscriptionPlan(updatedPlan: SubscriptionPlan) {
        _uiState.update {
            it.copy(subscriptionPlan = updatedPlan)
        }
        val author = _uiState.value.activePicker.name
        broadcastNotification(
            message = "💎 Super Admin $author updated subscription: ${updatedPlan.name} (${if (updatedPlan.isFreeSubscription) "FREE" else "${updatedPlan.billingCycle.title} £${String.format(java.util.Locale.US, "%.2f", updatedPlan.effectivePriceGbp)}"}",
            pickerName = author,
            storeName = "Super Admin Console",
            itemTitle = "Subscription Updated"
        )
    }

    fun grantFreeSubscription(days: Int, maxItems: Int, maxManagers: Int, maxPickers: Int) {
        val current = _uiState.value.subscriptionPlan
        val updated = current.copy(
            isFreeSubscription = true,
            trialDaysRemaining = days,
            maxItemsQuota = maxItems,
            maxManagersQuota = maxManagers,
            maxPickersQuota = maxPickers,
            status = "ACTIVE_FREE_GRANT",
            grantedBySuperAdminId = _uiState.value.activePicker.id,
            grantedBySuperAdminName = _uiState.value.activePicker.name
        )
        updateSubscriptionPlan(updated)
    }

    fun applyVoucherCode(code: String): Boolean {
        val discount = SuperAdminDirectory.PRESET_VOUCHERS[code.trim().uppercase()]
        if (discount != null) {
            val current = _uiState.value.subscriptionPlan
            val isFreeNow = discount >= 100
            val updated = current.copy(
                activeVoucherCode = code.trim().uppercase(),
                voucherDiscountPercent = discount,
                isFreeSubscription = isFreeNow || current.isFreeSubscription
            )
            updateSubscriptionPlan(updated)
            return true
        }
        return false
    }

    fun togglePlanFeature(
        camera: Boolean? = null,
        pdf: Boolean? = null,
        api: Boolean? = null,
        sim: Boolean? = null,
        kpi: Boolean? = null,
        barcode: Boolean? = null
    ) {
        val current = _uiState.value.subscriptionPlan
        val curFeatures = current.features
        val updatedFeatures = curFeatures.copy(
            allowCameraUse = camera ?: curFeatures.allowCameraUse,
            allowPdfUploads = pdf ?: curFeatures.allowPdfUploads,
            allowApiSync = api ?: curFeatures.allowApiSync,
            allowLiveSimulation = sim ?: curFeatures.allowLiveSimulation,
            allowAdvancedKpiExport = kpi ?: curFeatures.allowAdvancedKpiExport,
            allowBarcodeScanning = barcode ?: curFeatures.allowBarcodeScanning
        )
        updateSubscriptionPlan(current.copy(features = updatedFeatures))
    }

    fun syncOrdersFromRestApi(endpointUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingApi = true) }
            val result = apiIngestionService.fetchOrdersFromRestEndpoint(endpointUrl)
            if (result.success && result.importedItems.isNotEmpty()) {
                result.importedItems.forEach { item ->
                    repository.insertItem(item)
                }
                broadcastNotification(
                    message = "🌐 API Sync imported ${result.importedItems.size} orders from ${result.sourceLabel}",
                    pickerName = _uiState.value.activePicker.name,
                    storeName = "API Gateway",
                    itemTitle = "REST API Sync"
                )
            }
            _uiState.update {
                it.copy(
                    isSyncingApi = false,
                    lastApiSyncResult = result,
                    apiSyncHistory = (listOf(result) + it.apiSyncHistory).take(10)
                )
            }
        }
    }

    fun ingestWebhookJsonPayload(rawJson: String, sourceLabel: String = "Channel Webhook API") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingApi = true) }
            val items = try {
                apiIngestionService.parseJsonPayload(rawJson, sourceTag = sourceLabel)
            } catch (e: Exception) {
                emptyList()
            }

            val result = if (items.isNotEmpty()) {
                items.forEach { item ->
                    repository.insertItem(item)
                }
                broadcastNotification(
                    message = "⚡ $sourceLabel dispatched ${items.size} auto-routed orders to picking teams!",
                    pickerName = _uiState.value.activePicker.name,
                    storeName = "Multi-Store Webhook",
                    itemTitle = sourceLabel
                )
                ApiSyncResult(
                    success = true,
                    sourceLabel = sourceLabel,
                    importedItems = items,
                    statusMessage = "Successfully ingested & routed ${items.size} orders via $sourceLabel"
                )
            } else {
                ApiSyncResult(
                    success = false,
                    sourceLabel = sourceLabel,
                    importedItems = emptyList(),
                    statusMessage = "Invalid JSON format or empty orders list."
                )
            }

            _uiState.update {
                it.copy(
                    isSyncingApi = false,
                    lastApiSyncResult = result,
                    apiSyncHistory = (listOf(result) + it.apiSyncHistory).take(10)
                )
            }
        }
    }

    fun ingestCsvBulkPayload(rawCsv: String) {
        viewModelScope.launch {
            val items = apiIngestionService.parseCsvPayload(rawCsv)
            val result = if (items.isNotEmpty()) {
                items.forEach { item ->
                    repository.insertItem(item)
                }
                broadcastNotification(
                    message = "📄 Manager Bulk CSV imported ${items.size} orders across wholesalers!",
                    pickerName = _uiState.value.activePicker.name,
                    storeName = "CSV Batch",
                    itemTitle = "Bulk Import"
                )
                ApiSyncResult(
                    success = true,
                    sourceLabel = "Manager CSV Upload",
                    importedItems = items,
                    statusMessage = "Imported ${items.size} orders from CSV sheet."
                )
            } else {
                ApiSyncResult(
                    success = false,
                    sourceLabel = "Manager CSV Upload",
                    importedItems = emptyList(),
                    statusMessage = "Could not parse CSV rows. Ensure comma-separated columns."
                )
            }

            _uiState.update {
                it.copy(
                    lastApiSyncResult = result,
                    apiSyncHistory = (listOf(result) + it.apiSyncHistory).take(10)
                )
            }
        }
    }

    fun parseUploadedPicklistPdf(uri: Uri, fileName: String = "Uploaded_Picklist.pdf") {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingApi = true) }
            val parsed = pdfParserService.parseUploadedPdf(getApplication(), uri, fileName)
            _uiState.update {
                it.copy(
                    isSyncingApi = false,
                    currentPdfParseResult = parsed
                )
            }
        }
    }

    fun generateSamplePicklistPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncingApi = true) }
            val parsed = pdfParserService.generateAndParseSamplePicklistPdf(
                getApplication(),
                "Wholesale_Picklist_Manifest_${Random.nextInt(100, 999)}.pdf"
            )
            _uiState.update {
                it.copy(
                    isSyncingApi = false,
                    currentPdfParseResult = parsed
                )
            }
        }
    }

    fun updateExtractedPdfRow(updatedRow: ExtractedPicklistRow) {
        val current = _uiState.value.currentPdfParseResult ?: return
        val updatedList = current.extractedRows.map { if (it.id == updatedRow.id) updatedRow else it }
        _uiState.update {
            it.copy(currentPdfParseResult = current.copy(extractedRows = updatedList))
        }
    }

    fun dispatchExtractedPdfRows() {
        val current = _uiState.value.currentPdfParseResult ?: return
        val selectedRows = current.extractedRows.filter { it.selected }
        if (selectedRows.isEmpty()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val createdItems = selectedRows.mapIndexed { idx, row ->
                PickingItem(
                    orderNumber = row.orderNumber,
                    title = row.title,
                    quantityRequired = row.quantity.coerceAtLeast(1),
                    quantityPicked = 0,
                    unit = row.unit,
                    sourceStore = row.routedStore,
                    salesChannel = row.salesChannel,
                    assignedTeam = row.assignedTeam,
                    status = PickingItem.STATUS_PENDING,
                    imageUri = row.imageUri,
                    imageResName = row.imageResName,
                    barcode = row.sku,
                    aisleLocation = row.aisleLocation,
                    notes = "Imported from Picklist PDF (${current.fileName})",
                    priority = row.priority,
                    createdAt = now - (idx * 500L)
                )
            }

            createdItems.forEach { item ->
                repository.insertItem(item)
            }

            val syncResult = ApiSyncResult(
                success = true,
                sourceLabel = "Picklist PDF (${current.fileName})",
                importedItems = createdItems,
                statusMessage = "Dispatched ${createdItems.size} individual orders with images & SKUs across wholesalers."
            )

            broadcastNotification(
                message = "📑 Manager imported Picklist PDF '${current.fileName}' → ${createdItems.size} orders live for pickers!",
                pickerName = _uiState.value.activePicker.name,
                storeName = "Multi-Store PDF",
                itemTitle = current.fileName
            )

            _uiState.update {
                it.copy(
                    currentPdfParseResult = null,
                    lastApiSyncResult = syncResult,
                    apiSyncHistory = (listOf(syncResult) + it.apiSyncHistory).take(10)
                )
            }
        }
    }

    // Pick & Strike off item
    fun pickItem(item: PickingItem, durationSecs: Int = Random.nextInt(35, 90)) {
        val currentPicker = _uiState.value.activePicker
        viewModelScope.launch {
            // Animate strike-off briefly before removal from list
            _uiState.update { it.copy(strikingItemIds = it.strikingItemIds + item.id) }

            val success = repository.completePick(
                item = item,
                pickerId = currentPicker.id,
                pickerName = currentPicker.name,
                teamName = currentPicker.teamName,
                durationSeconds = durationSecs
            )

            if (success) {
                broadcastPick(
                    pickerName = currentPicker.name,
                    storeName = item.sourceStore,
                    itemTitle = item.title,
                    orderNumber = item.orderNumber,
                    qty = item.quantityRequired,
                    unit = item.unit
                )
            }

            // Remove from striking tracking after brief animation duration
            delay(400)
            _uiState.update { it.copy(strikingItemIds = it.strikingItemIds - item.id) }
        }
    }

    fun claimItem(item: PickingItem) {
        val currentPicker = _uiState.value.activePicker
        viewModelScope.launch {
            repository.claimItem(item, currentPicker.id, currentPicker.name)
            broadcastNotification(
                message = "${currentPicker.name} claimed #${item.orderNumber} (${item.title}) at ${item.sourceStore}",
                pickerName = currentPicker.name,
                storeName = item.sourceStore,
                itemTitle = item.title
            )
        }
    }

    fun unclaimItem(item: PickingItem) {
        viewModelScope.launch {
            repository.unclaimItem(item)
        }
    }

    fun partialPick(item: PickingItem, delta: Int) {
        val currentPicker = _uiState.value.activePicker
        viewModelScope.launch {
            val newQty = (item.quantityPicked + delta).coerceIn(0, item.quantityRequired)
            repository.updatePartialPick(
                item = item,
                newQty = newQty,
                pickerId = currentPicker.id,
                pickerName = currentPicker.name,
                teamName = currentPicker.teamName
            )
            if (newQty >= item.quantityRequired) {
                broadcastPick(
                    pickerName = currentPicker.name,
                    storeName = item.sourceStore,
                    itemTitle = item.title,
                    orderNumber = item.orderNumber,
                    qty = item.quantityRequired,
                    unit = item.unit
                )
            }
        }
    }

    fun markOutOfStock(item: PickingItem, reason: String) {
        val currentPicker = _uiState.value.activePicker
        viewModelScope.launch {
            repository.markOutOfStock(
                item = item,
                reason = reason,
                pickerId = currentPicker.id,
                pickerName = currentPicker.name,
                teamName = currentPicker.teamName
            )
            broadcastNotification(
                message = "⚠️ ${currentPicker.name} reported '${item.title}' OUT OF STOCK at ${item.sourceStore}",
                pickerName = currentPicker.name,
                storeName = item.sourceStore,
                itemTitle = item.title
            )
        }
    }

    fun submitPickerFieldReport(
        item: PickingItem,
        comment: String,
        conditionTags: List<String>,
        photoUri: String?,
        alsoMarkPicked: Boolean = false
    ) {
        val currentPicker = _uiState.value.activePicker
        viewModelScope.launch {
            repository.submitPickerFieldReport(
                item = item,
                comment = comment,
                conditionTags = conditionTags,
                photoUri = photoUri,
                pickerId = currentPicker.id,
                pickerName = currentPicker.name,
                teamName = currentPicker.teamName
            )

            val badgeSummary = if (conditionTags.isNotEmpty()) "[${conditionTags.joinToString(", ")}] " else ""
            val photoNote = if (!photoUri.isNullOrBlank()) " 📷 +Aisle Photo" else ""
            broadcastNotification(
                message = "🏷️ ${currentPicker.name} updated #${item.orderNumber} (${item.title}): $badgeSummary${comment.take(40)}$photoNote",
                pickerName = currentPicker.name,
                storeName = item.sourceStore,
                itemTitle = item.title
            )

            if (alsoMarkPicked) {
                pickItem(
                    item.copy(
                        pickerComment = comment,
                        pickerConditionTags = conditionTags.joinToString(", "),
                        pickerPhotoUri = photoUri,
                        pickerReportedBy = currentPicker.name,
                        pickerReportedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun redispatchItem(item: PickingItem) {
        viewModelScope.launch {
            repository.redispatchItem(item)
            broadcastNotification(
                message = "🔄 Order #${item.orderNumber} re-dispatched to picking queue by Admin",
                pickerName = "Admin",
                storeName = item.sourceStore,
                itemTitle = item.title
            )
        }
    }

    fun deleteItem(item: PickingItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun createNewItem(
        title: String,
        quantity: Int,
        unit: String,
        sourceStore: String,
        salesChannel: String,
        assignedTeam: String,
        imageUri: String?,
        imageResName: String?,
        barcode: String,
        aisleLocation: String,
        priority: String,
        notes: String
    ) {
        viewModelScope.launch {
            val prefix = when (salesChannel) {
                "Amazon" -> "AMZ"
                "eBay" -> "EB"
                else -> "WEB"
            }
            val randomOrderNum = "$prefix-${Random.nextInt(1000, 9999)}"

            val newItem = PickingItem(
                orderNumber = randomOrderNum,
                title = title.trim(),
                quantityRequired = quantity.coerceAtLeast(1),
                quantityPicked = 0,
                unit = unit,
                sourceStore = sourceStore,
                salesChannel = salesChannel,
                assignedTeam = assignedTeam,
                status = PickingItem.STATUS_PENDING,
                imageUri = imageUri,
                imageResName = imageResName,
                barcode = barcode.ifBlank { "50${Random.nextLong(10000000000L, 99999999999L)}" },
                aisleLocation = aisleLocation.ifBlank { "Aisle ${Random.nextInt(1, 20)}, Bay ${Random.nextInt(1, 10)}" },
                priority = priority,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )

            repository.insertItem(newItem)

            broadcastNotification(
                message = "📦 Admin dispatched new order #${newItem.orderNumber} (${newItem.quantityRequired} ${newItem.unit} '${newItem.title}') to ${newItem.sourceStore} for ${newItem.assignedTeam}",
                pickerName = "Admin",
                storeName = newItem.sourceStore,
                itemTitle = newItem.title
            )
        }
    }

    fun generateDemoBatch() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val sampleTitles = listOf(
                Triple("Ferrero Rocher Box of 30", "Costco Wholesale Ltd", "Amazon"),
                Triple("Walkers Ready Salted Crisps 32x32.5g Box", "Bookers Cash & Carry", "eBay"),
                Triple("Kikkoman Soy Sauce 1L Dispenser", "Loon Fung", "Webstore"),
                Triple("Red Bull Energy Drink 24x250ml Pack", "Bestway Wholesale", "Amazon"),
                Triple("Hellmann's Real Mayonnaise 5L Catering Tub", "Bookers Cash & Carry", "Webstore"),
                Triple("Lurpak Slightly Salted Butter 500g (x6)", "Tesco's", "Amazon"),
                Triple("Innocent Orange Juice 1.35L (x4)", "Sainsbury's", "Webstore")
            )

            val newItems = sampleTitles.map { (title, store, channel) ->
                val prefix = when (channel) {
                    "Amazon" -> "AMZ"
                    "eBay" -> "EB"
                    else -> "WEB"
                }
                PickingItem(
                    orderNumber = "$prefix-${Random.nextInt(1000, 9999)}",
                    title = title,
                    quantityRequired = Random.nextInt(4, 20),
                    quantityPicked = 0,
                    unit = "Units",
                    sourceStore = store,
                    salesChannel = channel,
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    barcode = "50${Random.nextLong(10000000000L, 99999999999L)}",
                    aisleLocation = "Aisle ${Random.nextInt(1, 15)}, Pallet ${Random.nextInt(1, 8)}",
                    priority = if (Random.nextBoolean()) PickingItem.PRIORITY_HIGH else PickingItem.PRIORITY_NORMAL,
                    createdAt = now
                )
            }

            repository.insertBatch(newItems)

            broadcastNotification(
                message = "🚀 Admin imported batch of ${newItems.size} online orders from Amazon, eBay & Webstores!",
                pickerName = "Admin",
                storeName = "Multiple Wholesalers",
                itemTitle = "Batch Import"
            )
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.clearAll()
            PickingDatabase.populateInitialData(database)
            _uiState.update { it.copy(latestBroadcast = null, recentBroadcasts = emptyList()) }
        }
    }

    // Toggle multi-team simulation: other pickers in the field pick items in real-time
    fun toggleSimulation(enable: Boolean) {
        _uiState.update { it.copy(isSimulationActive = enable) }
        if (enable) {
            startSimulation()
        } else {
            simulationJob?.cancel()
            simulationJob = null
        }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(Random.nextLong(7000, 14000))
                val pending = activeItems.value.filter { it.status == PickingItem.STATUS_PENDING }
                if (pending.isNotEmpty()) {
                    val randomItem = pending.random()
                    // Pick a different team member than current active user
                    val otherPickers = TeamDirectory.PICKERS.filter { it.id != _uiState.value.activePicker.id }
                    val simPicker = otherPickers.random()

                    // Simulate pick
                    val speed = Random.nextInt(30, 85)
                    repository.completePick(
                        item = randomItem,
                        pickerId = simPicker.id,
                        pickerName = simPicker.name,
                        teamName = simPicker.teamName,
                        durationSeconds = speed
                    )

                    broadcastPick(
                        pickerName = simPicker.name,
                        storeName = randomItem.sourceStore,
                        itemTitle = randomItem.title,
                        orderNumber = randomItem.orderNumber,
                        qty = randomItem.quantityRequired,
                        unit = randomItem.unit
                    )
                }
            }
        }
    }

    private fun broadcastPick(
        pickerName: String,
        storeName: String,
        itemTitle: String,
        orderNumber: String,
        qty: Int,
        unit: String
    ) {
        val event = LiveBroadcastEvent(
            message = "⚡ $pickerName picked #$orderNumber ($qty $unit '$itemTitle') at $storeName! Struck off list.",
            pickerName = pickerName,
            storeName = storeName,
            itemTitle = itemTitle
        )
        _uiState.update {
            it.copy(
                latestBroadcast = event,
                recentBroadcasts = (listOf(event) + it.recentBroadcasts).take(15)
            )
        }
    }

    private fun broadcastNotification(
        message: String,
        pickerName: String,
        storeName: String,
        itemTitle: String
    ) {
        val event = LiveBroadcastEvent(
            message = message,
            pickerName = pickerName,
            storeName = storeName,
            itemTitle = itemTitle
        )
        _uiState.update {
            it.copy(
                latestBroadcast = event,
                recentBroadcasts = (listOf(event) + it.recentBroadcasts).take(15)
            )
        }
    }

    fun dismissBroadcast() {
        _uiState.update { it.copy(latestBroadcast = null) }
    }
}
