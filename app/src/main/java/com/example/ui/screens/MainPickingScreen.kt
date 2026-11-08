package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PickingItem
import com.example.data.model.SupportTier
import com.example.data.model.TeamDirectory
import com.example.ui.components.*
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessEmerald
import com.example.ui.viewmodel.PickingViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPickingScreen(
    viewModel: PickingViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeQueue by viewModel.filteredPickingQueue.collectAsStateWithLifecycle()
    val allItems by viewModel.allItems.collectAsStateWithLifecycle()
    val pickedItems by viewModel.pickedItems.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val leaderboard by viewModel.pickerLeaderboard.collectAsStateWithLifecycle()
    val storeStats by viewModel.storeStats.collectAsStateWithLifecycle()
    val channelStats by viewModel.channelStats.collectAsStateWithLifecycle()

    var showSuperAdminDialog by remember { mutableStateOf(false) }
    var activeSuperAdminProfile by remember {
        mutableStateOf(TeamDirectory.SUPER_ADMINS.first())
    }

    // Dedicated Clean Login Gate (Manager / Admin vs Picker, Developer Portal, Theme Toggle)
    if (!uiState.isAuthenticated) {
        RoleLoginScreen(
            loginError = uiState.loginError,
            onClearError = { viewModel.clearLoginError() },
            onLoginByCredentials = { roleType, identifier, pin ->
                viewModel.loginByRoleAndCredentials(roleType, identifier, pin)
            },
            onDeveloperLogin = { devPin ->
                val ok = viewModel.loginAsDeveloper(devPin)
                if (ok) {
                    viewModel.openDeveloperPortal(true)
                }
                ok
            },
            onOpenDeveloperConsole = {
                viewModel.openDeveloperPortal(true)
            },
            isDarkMode = uiState.isDarkMode,
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            modifier = modifier
        )
        return
    }

    // Dedicated Full Developer & Super Admin Cloud Portal (apps.sellershub.io/stockpick)
    if (uiState.isDeveloperPortalOpen) {
        DeveloperSuperAdminPortalView(
            subscribers = uiState.subscribers,
            invoices = uiState.invoices,
            transactions = uiState.paymentTransactions,
            webPortalConfig = uiState.webPortalConfig,
            activeSuperAdmin = uiState.activePicker,
            onClosePortal = { viewModel.openDeveloperPortal(false) },
            onUpdateSubscriberPlan = { subId, plan -> viewModel.updateSubscriberPlan(subId, plan) },
            onUpdateSubscriberStatus = { subId, status -> viewModel.updateSubscriberStatus(subId, status) },
            onExtendSubscriberTrial = { subId, days -> viewModel.extendSubscriberTrial(subId, days) },
            onGrantSubscriberFreePlan = { subId, days, items, pickers, managers ->
                viewModel.grantSubscriberFreePlan(subId, days, items, pickers, managers)
            },
            onApplyVoucherToSubscriber = { subId, code -> viewModel.applyVoucherToSubscriber(subId, code) },
            onMarkInvoicePaid = { invId -> viewModel.markInvoiceAsPaid(invId) },
            onSyncWebPortal = { viewModel.syncWebPortal() },
            modifier = modifier
        )
        return
    }

    // 0: Pick Queue, 1: Order Placement & API Sync (Admin), 2: Productivity Monitor (Admin), 3: History (Admin)
    var currentTab by remember(uiState.isAdminMode) {
        mutableStateOf(if (uiState.isAdminMode) 1 else 0)
    }
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var scanBarcodeTargetItem by remember { mutableStateOf<PickingItem?>(null) }
    var outOfStockTargetItem by remember { mutableStateOf<PickingItem?>(null) }
    var fieldReportTargetItem by remember { mutableStateOf<PickingItem?>(null) }

    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (uiState.isAdminMode) PrimaryBlue else SuccessEmerald,
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (uiState.isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.ShoppingCartCheckout,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (uiState.isAdminMode) "SellersHub Dispatch Hub" else "SellersHub StockPickup",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = (-0.2).sp
                                )
                                Text(
                                    text = if (uiState.isAdminMode)
                                        "apps.sellershub.io • Manager Dispatch & KPIs"
                                    else
                                        "Live Queue • ${uiState.activePicker.teamName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (uiState.isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (uiState.isSuperAdminMode) {
                            IconButton(onClick = { viewModel.openDeveloperPortal(true) }) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = "Developer & Super Admin Portal (apps.sellershub.io)",
                                    tint = Color(0xFF7C3AED)
                                )
                            }
                        }
                        if (uiState.isAdminMode) {
                            IconButton(onClick = { showAddOrderDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "Add Order",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Active Identity & Session Control Bar
                PickerSelectorBar(
                    activePicker = uiState.activePicker,
                    onSelectPicker = { viewModel.switchActivePicker(it) },
                    isSimulationActive = uiState.isSimulationActive,
                    onToggleSimulation = { viewModel.toggleSimulation(it) },
                    isAdminMode = uiState.isAdminMode,
                    onLogout = { viewModel.logout() }
                )

                // Real-Time Broadcast Notification Banner
                LiveNotificationBanner(
                    event = uiState.latestBroadcast,
                    onDismiss = { viewModel.dismissBroadcast() }
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Only Managers/Admins get the multi-tab command navigation bar;
                // Store Pickers stay focused strictly on the live Pick Queue!
                if (uiState.isAdminMode) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == 0,
                            onClick = { currentTab = 0 },
                            icon = {
                                BadgedBox(badge = {
                                    if (activeQueue.isNotEmpty()) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                            Text("${activeQueue.size}", fontSize = 10.sp)
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (currentTab == 0) Icons.Default.Inventory else Icons.Outlined.Inventory,
                                        contentDescription = "Pick Queue"
                                    )
                                }
                            },
                            label = { Text("Pick Queue", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )

                        NavigationBarItem(
                            selected = currentTab == 1,
                            onClick = { currentTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 1) Icons.Default.CloudSync else Icons.Outlined.CloudSync,
                                    contentDescription = "Place Orders & API"
                                )
                            },
                            label = { Text("Order & API", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )

                        NavigationBarItem(
                            selected = currentTab == 2,
                            onClick = { currentTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 2) Icons.Default.Insights else Icons.Outlined.Insights,
                                    contentDescription = "Team KPIs"
                                )
                            },
                            label = { Text("Team KPIs", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )

                        NavigationBarItem(
                            selected = currentTab == 3,
                            onClick = { currentTab = 3 },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 3) Icons.Default.History else Icons.Outlined.History,
                                    contentDescription = "Audit History"
                                )
                            },
                            label = { Text("History", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )

                        NavigationBarItem(
                            selected = currentTab == 4,
                            onClick = { currentTab = 4 },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == 4) Icons.Default.AccountBalance else Icons.Outlined.AccountBalance,
                                    contentDescription = "Subscriber Portfolio & Freemium Plan"
                                )
                            },
                            label = { Text("Portfolio", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                    }
                }

                // Super Admin Subscription Module (strictly for Developers / Super Admins only)
                if (uiState.isSuperAdminMode) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openDeveloperPortal(true) }
                            .testTag("super_admin_subscription_footer")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val plan = uiState.subscriptionPlan
                            val priceOrFree = if (plan.isFreeSubscription) {
                                "Free Grant (${plan.trialDaysRemaining}d remaining)"
                            } else {
                                "${plan.billingCycle.title} • £${String.format(Locale.US, "%.2f", plan.effectivePriceGbp)}"
                            }
                            val supportName = if (plan.supportTier == SupportTier.PREMIUM_CHAT) "24/7 Premium Chat" else "Standard (3–5d)"
                            val itemsText = if (plan.isUnlimitedItems) "Unlimited items" else "${plan.maxItemsQuota} items"
                            Text(
                                text = "Super Admin: ${plan.name} • $priceOrFree • $itemsText • ${plan.maxPickersQuota} Pickers • Support: $supportName • [Manage Subscription]",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (uiState.isAdminMode && (currentTab == 0 || currentTab == 1)) {
                ExtendedFloatingActionButton(
                    onClick = { showAddOrderDialog = true },
                    icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                    text = { Text("Dispatch Order", fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            val effectiveTab = if (uiState.isAdminMode) currentTab else 0
            when (effectiveTab) {
                0 -> {
                    // TAB 0: Pick Queue (Strictly shows items that still need picking)
                    Column(modifier = Modifier.fillMaxSize()) {
                        WholesalerChipRow(
                            selectedStore = uiState.selectedStore,
                            onSelectStore = { viewModel.setSelectedStore(it) }
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search product, barcode or #ID...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )
                        }

                        ChannelChipRow(
                            selectedChannel = uiState.selectedChannel,
                            onSelectChannel = { viewModel.setSelectedChannel(it) }
                        )

                        if (activeQueue.isEmpty()) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(72.dp)
                                            .background(SuccessEmerald.copy(alpha = 0.15f), RoundedCornerShape(36.dp))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = SuccessEmerald,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "All Items Picked!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (uiState.selectedStore != "All Stores" || uiState.selectedChannel != "All Channels")
                                            "No pending items matching active filters."
                                        else
                                            "Great job! All team orders have been fulfilled and struck off.",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (uiState.isAdminMode) {
                                            Button(
                                                onClick = { currentTab = 1 },
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Open API Sync")
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                viewModel.setSelectedStore("All Stores")
                                                viewModel.setSelectedChannel("All Channels")
                                                viewModel.setSearchQuery("")
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Reset Filters")
                                        }
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "ITEMS STILL NEEDING PICKING (${activeQueue.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Tap Pick to strike off live",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                items(activeQueue, key = { it.id }) { item ->
                                    val isStriking = uiState.strikingItemIds.contains(item.id)
                                    PickerItemCard(
                                        item = item,
                                        currentPickerId = uiState.activePicker.id,
                                        isStriking = isStriking,
                                        onPick = { viewModel.pickItem(item) },
                                        onPartialPickIncrement = { viewModel.partialPick(item, 1) },
                                        onClaim = { viewModel.claimItem(item) },
                                        onUnclaim = { viewModel.unclaimItem(item) },
                                        onOutOfStock = { outOfStockTargetItem = item },
                                        onScanBarcode = { scanBarcodeTargetItem = item },
                                        onOpenFieldReport = { fieldReportTargetItem = item }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Manager Order Placement & API Ingestion Hub
                    AdminApiIngestionHub(
                        apiService = viewModel.apiIngestionService,
                        isSyncing = uiState.isSyncingApi,
                        lastSyncResult = uiState.lastApiSyncResult,
                        syncHistory = uiState.apiSyncHistory,
                        pdfParseResult = uiState.currentPdfParseResult,
                        onUploadPicklistPdf = { uri, fileName -> viewModel.parseUploadedPicklistPdf(uri, fileName) },
                        onGenerateSamplePicklistPdf = { viewModel.generateSamplePicklistPdf() },
                        onUpdatePdfRow = { row -> viewModel.updateExtractedPdfRow(row) },
                        onDispatchPdfRows = { viewModel.dispatchExtractedPdfRows() },
                        onOpenManualOrderDialog = { showAddOrderDialog = true },
                        onSyncRestEndpoint = { url -> viewModel.syncOrdersFromRestApi(url) },
                        onIngestWebhookJson = { json, label -> viewModel.ingestWebhookJsonPayload(json, label) },
                        onIngestCsvBulk = { csv -> viewModel.ingestCsvBulkPayload(csv) },
                        isPdfAllowed = uiState.subscriptionPlan.features.allowPdfUploads,
                        isApiAllowed = uiState.subscriptionPlan.features.allowApiSync
                    )
                }

                2 -> {
                    // TAB 2: Admin Productivity Dashboard
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = "Admin Productivity Monitor",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = "Real-time picker metrics, speed & fulfillment",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Button(
                                    onClick = { viewModel.generateDemoBatch() },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Quick Batch", fontSize = 11.sp)
                                }
                            }
                        }

                        item {
                            AdminKpiGrid(allItems = allItems)
                        }

                        item {
                            PickerLeaderboardSection(leaderboard = leaderboard)
                        }

                        item {
                            WholesalerFulfillmentSection(storeStats = storeStats)
                        }

                        item {
                            ChannelDistributionSection(channelStats = channelStats)
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Admin Queue Controls",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { showAddOrderDialog = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Manual Order Form", fontSize = 12.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.resetDatabase() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Reset Sample Data", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Order Assignment History & Completed Items
                    AdminHistoryView(
                        auditLogs = auditLogs,
                        pickedItems = pickedItems,
                        onRedispatch = { viewModel.redispatchItem(it) }
                    )
                }

                4 -> {
                    // TAB 4: Subscriber Portfolio & Freemium Subscription Management & Useful Settings
                    ManagerPortfolioSettingsView(
                        plan = uiState.subscriptionPlan,
                        activeManager = uiState.activePicker,
                        totalItemsCount = allItems.size,
                        isDarkMode = uiState.isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                        onSelectPlanTier = { viewModel.selectPlanTier(it) },
                        onToggleBillingCycle = { viewModel.toggleBillingCycle() },
                        onApplyVoucher = { viewModel.applyVoucherCode(it) },
                        onCancelSubscription = { viewModel.cancelSubscription() },
                        onPauseSubscription = { viewModel.pauseSubscription(it) },
                        onResumeSubscription = { viewModel.resumeSubscription() },
                        onResetSampleData = { viewModel.resetDatabase() },
                        onGenerateDemoBatch = { viewModel.generateDemoBatch() },
                        invoices = uiState.invoices.filter { it.subscriberId == "SUB-84920" || it.subscriberName.contains("Apex") }
                    )
                }
            }
        }
    }

    // Add Order Dialog
    if (showAddOrderDialog) {
        AdminAddOrderDialog(
            onDismiss = { showAddOrderDialog = false },
            onSubmit = { title, qty, unit, store, channel, team, imageUri, imageResName, barcode, aisle, priority, notes ->
                viewModel.createNewItem(
                    title = title,
                    quantity = qty,
                    unit = unit,
                    sourceStore = store,
                    salesChannel = channel,
                    assignedTeam = team,
                    imageUri = imageUri,
                    imageResName = imageResName,
                    barcode = barcode,
                    aisleLocation = aisle,
                    priority = priority,
                    notes = notes
                )
            }
        )
    }

    // Barcode Scan Verification Dialog
    scanBarcodeTargetItem?.let { item ->
        BarcodeScanDialog(
            item = item,
            onDismiss = { scanBarcodeTargetItem = null },
            onVerifiedPick = { viewModel.pickItem(item) }
        )
    }

    // Out of Stock Dialog
    outOfStockTargetItem?.let { item ->
        OutOfStockDialog(
            item = item,
            onDismiss = { outOfStockTargetItem = null },
            onConfirmOos = { reason -> viewModel.markOutOfStock(item, reason) }
        )
    }

    // Picker Store Aisle Photo & Condition Report Dialog
    fieldReportTargetItem?.let { item ->
        PickerFieldReportDialog(
            item = item,
            isCameraAllowed = uiState.subscriptionPlan.features.allowCameraUse,
            onDismiss = { fieldReportTargetItem = null },
            onSaveReport = { comment, conditionTags, photoUri, alsoPick ->
                viewModel.submitPickerFieldReport(
                    item = item,
                    comment = comment,
                    conditionTags = conditionTags,
                    photoUri = photoUri,
                    alsoMarkPicked = alsoPick
                )
            }
        )
    }

    // Super Admin Subscription & License Console Dialog
    if (showSuperAdminDialog) {
        SuperAdminSubscriptionDialog(
            plan = uiState.subscriptionPlan,
            activeSuperAdmin = activeSuperAdminProfile,
            onDismiss = { showSuperAdminDialog = false },
            onSavePlan = { updated -> viewModel.updateSubscriptionPlan(updated) },
            onGrantFree = { days, items, managers, pickers ->
                viewModel.grantFreeSubscription(days, items, managers, pickers)
            },
            onApplyVoucher = { code -> viewModel.applyVoucherCode(code) },
            onToggleFeature = { camera, pdf, api, sim, kpi, barcode ->
                viewModel.togglePlanFeature(camera, pdf, api, sim, kpi, barcode)
            },
            onSuperAdminSelected = { member ->
                activeSuperAdminProfile = TeamDirectory.SUPER_ADMINS.find { it.id == member.id } ?: activeSuperAdminProfile
            }
        )
    }
}
