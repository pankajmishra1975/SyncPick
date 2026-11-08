package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSuperAdminPortalView(
    subscribers: List<SubscriberCompany>,
    invoices: List<SubscriberInvoice>,
    transactions: List<PaymentRecord>,
    webPortalConfig: WebPortalConfig,
    activeSuperAdmin: PickerProfile,
    onClosePortal: () -> Unit,
    onUpdateSubscriberPlan: (subscriberId: String, newPlan: SubscriptionPlan) -> Unit,
    onUpdateSubscriberStatus: (subscriberId: String, newStatus: SubscriberStatus) -> Unit,
    onExtendSubscriberTrial: (subscriberId: String, days: Int) -> Unit,
    onGrantSubscriberFreePlan: (subscriberId: String, days: Int, maxItems: Int, maxPickers: Int, maxManagers: Int) -> Unit,
    onApplyVoucherToSubscriber: (subscriberId: String, voucherCode: String) -> Boolean,
    onMarkInvoicePaid: (invoiceId: String) -> Unit,
    onSyncWebPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPortalTab by remember { mutableStateOf(0) } // 0: Subscribers, 1: Invoices & Payments, 2: Logic & Controls, 3: Web Portal (apps.sellershub.io/stockpick)
    var subscriberSearchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf("All") }
    var selectedSubscriberForEdit by remember { mutableStateOf<SubscriberCompany?>(null) }
    var selectedInvoiceForView by remember { mutableStateOf<SubscriberInvoice?>(null) }
    var showSuperAdminRoleSwitcher by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Metrics calculations
    val totalSubscribersCount = subscribers.size
    val freemiumCount = subscribers.count { it.isFreemium }
    val paidCount = totalSubscribersCount - freemiumCount
    val totalRevenueGbp = invoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.totalAmountGbp }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Developer & Super Admin App Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7C3AED))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Developer Console",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SellersHub StockPick",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF7C3AED).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SUPER ADMIN CONSOLE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF7C3AED),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Secure Web Portal: apps.sellershub.io/stockpick/manage • Logged in as ${activeSuperAdmin.name}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onClosePortal,
                        modifier = Modifier.testTag("close_developer_portal_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Developer Console")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Fast Super Admin Switcher & Web Portal Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { showSuperAdminRoleSwitcher = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Role: ${activeSuperAdmin.superAdminRole?.title ?: "Platform Owner"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Surface(
                        color = SuccessEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { onSyncWebPortal() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SuccessEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sync: ${webPortalConfig.lastSyncTimestamp}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessEmerald
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = SuccessEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Multi-Metric Dashboard Banner ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DeveloperMetricCard(
                title = "Total Subscribers",
                value = "$totalSubscribersCount",
                subtext = "$freemiumCount Free • $paidCount Paid",
                icon = Icons.Default.Groups,
                accentColor = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )

            DeveloperMetricCard(
                title = "Total Revenue",
                value = "£${String.format(Locale.US, "%.0f", totalRevenueGbp)}",
                subtext = "${invoices.size} Invoices Generated",
                icon = Icons.Default.ReceiptLong,
                accentColor = SuccessEmerald,
                modifier = Modifier.weight(1f)
            )

            DeveloperMetricCard(
                title = "Cloud Portal",
                value = "apps.sellershub.io",
                subtext = "Live Web Console",
                icon = Icons.Default.Language,
                accentColor = Color(0xFF7C3AED),
                modifier = Modifier.weight(1.1f)
            )
        }

        // --- Secondary Navigation Tabs ---
        ScrollableTabRow(
            selectedTabIndex = selectedPortalTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedPortalTab == 0,
                onClick = { selectedPortalTab = 0 },
                text = { Text("Subscribers (${subscribers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedPortalTab == 1,
                onClick = { selectedPortalTab = 1 },
                text = { Text("Invoices & Payments (${invoices.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedPortalTab == 2,
                onClick = { selectedPortalTab = 2 },
                text = { Text("Subscription Logic & Quotas", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedPortalTab == 3,
                onClick = { selectedPortalTab = 3 },
                text = { Text("Web Console (/manage)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // --- Main Tab Content ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedPortalTab) {
                0 -> {
                    // TAB 0: All Subscribers & Levels
                    SubscribersDirectoryTab(
                        subscribers = subscribers,
                        searchQuery = subscriberSearchQuery,
                        onSearchChange = { subscriberSearchQuery = it },
                        tierFilter = selectedTierFilter,
                        onTierFilterChange = { selectedTierFilter = it },
                        onSelectSubscriber = { selectedSubscriberForEdit = it },
                        onViewInvoicesForSubscriber = { sub ->
                            selectedPortalTab = 1
                        }
                    )
                }

                1 -> {
                    // TAB 1: Payment History & Invoices
                    InvoicesAndPaymentsTab(
                        invoices = invoices,
                        transactions = transactions,
                        onSelectInvoice = { selectedInvoiceForView = it },
                        onMarkPaid = onMarkInvoicePaid
                    )
                }

                2 -> {
                    // TAB 2: Subscription Architecture & Logic
                    SubscriptionManagementLogicTab(
                        subscribers = subscribers,
                        onOpenManageSubscriber = { sub -> selectedSubscriberForEdit = sub }
                    )
                }

                3 -> {
                    // TAB 3: Web Portal (apps.sellershub.io/stockpick)
                    WebPortalHostingTab(
                        config = webPortalConfig,
                        subscribersCount = subscribers.size,
                        invoicesCount = invoices.size,
                        onSyncNow = onSyncWebPortal,
                        onOpenExternalBrowser = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webPortalConfig.hostUrl))
                            runCatching { context.startActivity(intent) }
                        }
                    )
                }
            }
        }
    }

    // Modal: Edit Subscriber Plan & Quotas
    selectedSubscriberForEdit?.let { subscriber ->
        SubscriberManagementModal(
            subscriber = subscriber,
            onDismiss = { selectedSubscriberForEdit = null },
            onSavePlan = { updatedPlan ->
                onUpdateSubscriberPlan(subscriber.id, updatedPlan)
                selectedSubscriberForEdit = null
            },
            onUpdateStatus = { newStatus ->
                onUpdateSubscriberStatus(subscriber.id, newStatus)
                selectedSubscriberForEdit = null
            },
            onExtendTrial = { days ->
                onExtendSubscriberTrial(subscriber.id, days)
                selectedSubscriberForEdit = null
            },
            onGrantFree = { days, items, pickers, managers ->
                onGrantSubscriberFreePlan(subscriber.id, days, items, pickers, managers)
                selectedSubscriberForEdit = null
            },
            onApplyVoucher = { code ->
                onApplyVoucherToSubscriber(subscriber.id, code)
            }
        )
    }

    // Modal: Detailed Tax Invoice Viewer
    selectedInvoiceForView?.let { invoice ->
        InvoiceDetailModal(
            invoice = invoice,
            onDismiss = { selectedInvoiceForView = null },
            onMarkPaid = {
                onMarkInvoicePaid(invoice.invoiceId)
                selectedInvoiceForView = null
            }
        )
    }

    // Modal: Super Admin Role Switcher
    if (showSuperAdminRoleSwitcher) {
        SuperAdminRoleSwitcherDialog(
            activeAdmin = activeSuperAdmin,
            onDismiss = { showSuperAdminRoleSwitcher = false }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: ALL SUBSCRIBERS DIRECTORY & LEVELS
// -------------------------------------------------------------

@Composable
private fun SubscribersDirectoryTab(
    subscribers: List<SubscriberCompany>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    tierFilter: String,
    onTierFilterChange: (String) -> Unit,
    onSelectSubscriber: (SubscriberCompany) -> Unit,
    onViewInvoicesForSubscriber: (SubscriberCompany) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredSubscribers = remember(subscribers, searchQuery, tierFilter) {
        subscribers.filter { sub ->
            val matchesSearch = searchQuery.isBlank() ||
                    sub.companyName.contains(searchQuery, ignoreCase = true) ||
                    sub.id.contains(searchQuery, ignoreCase = true) ||
                    sub.city.contains(searchQuery, ignoreCase = true) ||
                    sub.contactName.contains(searchQuery, ignoreCase = true)

            val matchesTier = when (tierFilter) {
                "Freemium" -> sub.isFreemium
                "Pro" -> sub.plan.planId.contains("starter_pro") || sub.plan.name.contains("Pro")
                "Enterprise" -> sub.plan.planId.contains("enterprise") || sub.plan.name.contains("Enterprise")
                "Active" -> sub.status == SubscriberStatus.ACTIVE
                else -> true
            }

            matchesSearch && matchesTier
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            // Search Bar & Filter Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search subscriber company, ID (#SUB-...), or city...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Freemium", "Pro", "Enterprise", "Active").forEach { filter ->
                        FilterChip(
                            selected = tierFilter == filter,
                            onClick = { onTierFilterChange(filter) },
                            label = { Text(filter, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        items(filteredSubscribers, key = { it.id }) { subscriber ->
            SubscriberCardItem(
                subscriber = subscriber,
                onManagePlan = { onSelectSubscriber(subscriber) },
                onViewInvoices = { onViewInvoicesForSubscriber(subscriber) }
            )
        }
    }
}

@Composable
private fun SubscriberCardItem(
    subscriber: SubscriberCompany,
    onManagePlan: () -> Unit,
    onViewInvoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Company & Status
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (subscriber.isFreemium) SuccessEmerald.copy(alpha = 0.15f)
                                else PrimaryBlue.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = if (subscriber.isFreemium) Icons.Default.Spa else Icons.Default.Storefront,
                            contentDescription = null,
                            tint = if (subscriber.isFreemium) SuccessEmerald else PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = subscriber.companyName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${subscriber.id} • ${subscriber.city}, UK • Joined ${subscriber.joinedDate}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = subscriber.status.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = subscriber.status.label.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = subscriber.status.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Plan Tier & Billing Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = subscriber.plan.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (subscriber.plan.isFreeSubscription) "Freemium / Free Grant • £0.00"
                                else "${subscriber.plan.billingCycle.title} • £${String.format(Locale.US, "%.2f", subscriber.plan.effectivePriceGbp)}/mo",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (subscriber.plan.trialDaysRemaining > 0 && subscriber.isFreemium) {
                        Surface(
                            color = AccentTeal.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${subscriber.plan.trialDaysRemaining}d trial left",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quotas: Items processed, Pickers, Managers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuotaPill(
                    label = "Items Processed",
                    value = "${subscriber.totalItemsProcessed} / ${if (subscriber.plan.isUnlimitedItems) "∞" else subscriber.plan.maxItemsQuota}",
                    fraction = if (subscriber.plan.isUnlimitedItems) 0.3f else (subscriber.totalItemsProcessed.toFloat() / subscriber.plan.maxItemsQuota).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f)
                )

                QuotaPill(
                    label = "Pickers Assigned",
                    value = "${subscriber.assignedPickersCount} / ${subscriber.plan.maxPickersQuota}",
                    fraction = (subscriber.assignedPickersCount.toFloat() / subscriber.plan.maxPickersQuota).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f)
                )

                QuotaPill(
                    label = "Managers",
                    value = "${subscriber.activeManagersCount} / ${subscriber.plan.maxManagersQuota}",
                    fraction = (subscriber.activeManagersCount.toFloat() / subscriber.plan.maxManagersQuota).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewInvoices,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Invoices", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onManagePlan,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuotaPill(
    label: String,
    value: String,
    fraction: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (fraction >= 0.9f) Color(0xFFEF4444) else PrimaryBlue,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 1: INVOICES & PAYMENT HISTORY
// -------------------------------------------------------------

@Composable
private fun InvoicesAndPaymentsTab(
    invoices: List<SubscriberInvoice>,
    transactions: List<PaymentRecord>,
    onSelectInvoice: (SubscriberInvoice) -> Unit,
    onMarkPaid: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var invoiceSearchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val filteredInvoices = remember(invoices, invoiceSearchQuery, selectedStatusFilter) {
        invoices.filter { inv ->
            val matchesSearch = invoiceSearchQuery.isBlank() ||
                    inv.invoiceId.contains(invoiceSearchQuery, ignoreCase = true) ||
                    inv.subscriberName.contains(invoiceSearchQuery, ignoreCase = true) ||
                    inv.transactionRef.contains(invoiceSearchQuery, ignoreCase = true)

            val matchesStatus = when (selectedStatusFilter) {
                "Paid" -> inv.status == InvoiceStatus.PAID
                "Pending" -> inv.status == InvoiceStatus.PENDING
                else -> true
            }

            matchesSearch && matchesStatus
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            // Search & Filter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = invoiceSearchQuery,
                    onValueChange = { invoiceSearchQuery = it },
                    placeholder = { Text("Search invoice #, subscriber name, or transaction...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Paid", "Pending").forEach { filter ->
                        FilterChip(
                            selected = selectedStatusFilter == filter,
                            onClick = { selectedStatusFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "TAX INVOICES & BILLING RECEIPTS (${filteredInvoices.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }

        items(filteredInvoices, key = { it.invoiceId }) { invoice ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectInvoice(invoice) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(invoice.status.color.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = if (invoice.status == InvoiceStatus.PAID) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                                contentDescription = null,
                                tint = invoice.status.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = invoice.invoiceId,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = invoice.status.color.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = invoice.status.label.uppercase(),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = invoice.status.color,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = invoice.subscriberName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${invoice.issueDate} • ${invoice.paymentMethod}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "£${String.format(Locale.US, "%.2f", invoice.totalAmountGbp)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "inc. £${String.format(Locale.US, "%.2f", invoice.vatAmountGbp)} VAT",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "View Invoice →",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GATEWAY PAYMENT TRANSACTIONS (STRIPE & BACS LEDGER)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }

        items(transactions, key = { it.transactionId }) { txn ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${txn.transactionId} • ${txn.subscriberName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${txn.timestamp} via ${txn.paymentMethod}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "£${String.format(Locale.US, "%.2f", txn.amountGbp)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessEmerald
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SuccessEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = txn.status,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = SuccessEmerald,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: SUBSCRIPTION LOGIC & QUOTAS
// -------------------------------------------------------------

@Composable
private fun SubscriptionManagementLogicTab(
    subscribers: List<SubscriberCompany>,
    onOpenManageSubscriber: (SubscriberCompany) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "How Subscriptions Are Managed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Freemium auto-throttling, cloud syncing & super admin override",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LogicStepItem(
                        step = "1",
                        title = "Public Freemium Onboarding",
                        description = "Subscribers start on 'Public Freemium Starter' with 250 monthly items, 4 pickers, 2 managers, and full barcode scanning at £0.00."
                    )

                    LogicStepItem(
                        step = "2",
                        title = "Grace Period & Quota Throttling",
                        description = "When a depot exceeds 85% quota (212 items), the app warns the manager. At 100% (250 items), scanning requires upgrade or Super Admin trial extension."
                    )

                    LogicStepItem(
                        step = "3",
                        title = "Instant Upgrades & Invoicing",
                        description = "Managers upgrade to Wholesale Pro (£39/mo) or Enterprise (£79/mo). Invoices are immediately generated with 20% VAT and synced to apps.sellershub.io/stockpick."
                    )

                    LogicStepItem(
                        step = "4",
                        title = "Super Admin / Developer VIP Override",
                        description = "Developers and Super Admins have zero-restriction access to issue free trial extensions, apply 100% vouchers, and adjust quotas manually."
                    )
                }
            }
        }

        item {
            Text(
                text = "QUICK TIER PRESETS (MANAGED ON APPS.SELLERSHUB.IO)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }

        items(SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS, key = { it.planId }) { tier ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tier.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (tier.isFreeSubscription) "FREE (£0.00)" else "£${String.format(Locale.US, "%.2f", tier.monthlyPriceGbp)}/mo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (tier.isFreeSubscription) SuccessEmerald else PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tier.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "• Items: ${if (tier.isUnlimitedItems) "Unlimited" else tier.maxItemsQuota}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Pickers: ${tier.maxPickersQuota}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Managers: ${tier.maxManagersQuota}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogicStepItem(step: String, title: String, description: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.15f))
        ) {
            Text(text = step, fontSize = 10.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// -------------------------------------------------------------
// TAB 3: WEB PORTAL HOSTING (apps.sellershub.io/stockpick)
// -------------------------------------------------------------

@Composable
private fun WebPortalHostingTab(
    config: WebPortalConfig,
    subscribersCount: Int,
    invoicesCount: Int,
    onSyncNow: () -> Unit,
    onOpenExternalBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var simulatedBrowserUrl by remember { mutableStateOf("https://apps.sellershub.io/stockpick/manage") }
    var activeWebTab by remember { mutableStateOf("Subscribers") }
    val context = LocalContext.current

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Direct User Answer Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Color(0xFF7C3AED).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF7C3AED))
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Dedicated Developer Management Portal",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Secure URL: apps.sellershub.io/stockpick/manage",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "The dedicated webpage for developers to log in and manage all subscribers, payments, invoices, and freemium quotas is apps.sellershub.io/stockpick/manage with full 2FA authentication.\n\nThe base URL apps.sellershub.io/stockpick is reserved strictly for Show & Tell features, manager FAQs, and depot training tutorials.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.sellershub.io/stockpick/manage"))
                                runCatching { context.startActivity(intent) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open /manage URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.sellershub.io/stockpick"))
                                runCatching { context.startActivity(intent) }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Show & Tell URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onSyncNow,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger Cloud Sync with /manage API", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Cloud API Endpoints Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "LIVE CLOUD REST API & WEBHOOK STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ApiEndpointRow("GET", "/api/v1/subscribers", "200 OK • $subscribersCount Registered")
                    ApiEndpointRow("POST", "/api/v1/subscribers/upgrade", "200 OK • Auto Invoicing")
                    ApiEndpointRow("GET", "/api/v1/invoices", "200 OK • $invoicesCount Invoices")
                    ApiEndpointRow("POST", "/webhooks/stripe-events", "200 OK • Live Listener")
                }
            }
        }

        // Embedded Interactive Web Portal Preview Simulator
        item {
            Text(
                text = "INTERACTIVE WEB CONSOLE PREVIEW (apps.sellershub.io/stockpick)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Browser Window Header
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "SSL Secure",
                                            tint = SuccessEmerald,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = simulatedBrowserUrl,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Web Navbar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Subscribers", "Invoices", "Stripe Billing", "Webhooks", "API Keys").forEach { tab ->
                                    Surface(
                                        color = if (activeWebTab == tab) Color(0xFF7C3AED) else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable { activeWebTab = tab }
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (activeWebTab == tab) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Web Console Inner Content
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SellersHub Cloud • $activeWebTab Management",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            Surface(
                                color = SuccessEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LIVE CLOUD CONNECTED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SuccessEmerald,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        when (activeWebTab) {
                            "Subscribers" -> {
                                Text(
                                    text = "All active merchants registered under apps.sellershub.io/stockpick. Real-time webhook listeners automatically propagate changes to mobile picker devices.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "• Apex Wholesale Logistics (#SUB-84920) - Wholesale Pro\n• QuickPick Express (#SUB-10294) - Public Freemium\n• Northern Depot Direct (#SUB-39014) - Enterprise Multi-Depot",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            "Invoices" -> {
                                Text(
                                    text = "Automated VAT invoices dispatched on subscription renewal. Supports PDF export, credit notes, and Stripe receipts.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            "Stripe Billing" -> {
                                Text(
                                    text = "Stripe Connect API Key: sh_live_9942a77f01c89be4\nWebhook Secret: whsec_stockpick_live_8492",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            else -> {
                                Text(
                                    text = "Endpoints active at https://apps.sellershub.io/stockpick/api/v1",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApiEndpointRow(method: String, endpoint: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (method == "GET") PrimaryBlue.copy(alpha = 0.15f) else Color(0xFF7C3AED).copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = method,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (method == "GET") PrimaryBlue else Color(0xFF7C3AED),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = endpoint,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = status,
            fontSize = 10.sp,
            color = SuccessEmerald,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun DeveloperMetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = subtext,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// -------------------------------------------------------------
// MODAL: SUBSCRIBER MANAGEMENT & QUOTA CONTROLS
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriberManagementModal(
    subscriber: SubscriberCompany,
    onDismiss: () -> Unit,
    onSavePlan: (SubscriptionPlan) -> Unit,
    onUpdateStatus: (SubscriberStatus) -> Unit,
    onExtendTrial: (Int) -> Unit,
    onGrantFree: (days: Int, items: Int, pickers: Int, managers: Int) -> Unit,
    onApplyVoucher: (String) -> Boolean
) {
    var selectedTier by remember { mutableStateOf(subscriber.plan) }
    var voucherCodeInput by remember { mutableStateOf("") }
    var voucherStatusMsg by remember { mutableStateOf<String?>(null) }
    var extraTrialDays by remember { mutableStateOf("14") }
    var customItemLimit by remember { mutableStateOf(subscriber.plan.maxItemsQuota.toString()) }
    var customPickerLimit by remember { mutableStateOf(subscriber.plan.maxPickersQuota.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Manage Subscriber Portfolio",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${subscriber.companyName} (#${subscriber.id})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Plan Selection
                    Text(
                        text = "SELECT SUBSCRIPTION PLAN TIER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS.forEach { tier ->
                        val isSelected = selectedTier.planId == tier.planId
                        Surface(
                            color = if (isSelected) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            border = if (isSelected) BorderStroke(1.5.dp, PrimaryBlue) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTier = tier }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tier.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${tier.maxItemsQuota} items • ${tier.maxPickersQuota} pickers • ${tier.maxManagersQuota} managers",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = if (tier.isFreeSubscription) "FREE" else "£${String.format(Locale.US, "%.2f", tier.monthlyPriceGbp)}/mo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (tier.isFreeSubscription) SuccessEmerald else PrimaryBlue
                                )
                            }
                        }
                    }

                    // Super Admin Free Grant & Trial Extension
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SUPER ADMIN VIP GRANT & TRIAL EXTENSION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF7C3AED)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = extraTrialDays,
                                    onValueChange = { extraTrialDays = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("Add Trial Days") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                Button(
                                    onClick = {
                                        val days = extraTrialDays.toIntOrNull() ?: 14
                                        onExtendTrial(days)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text("Extend Trial", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onGrantFree(365, 10000, 50, 10)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Grant 1-Year VIP Free Subscription (10,000 items)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Voucher Application
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = voucherCodeInput,
                            onValueChange = { voucherCodeInput = it.uppercase() },
                            placeholder = { Text("Code: SUPER50, WHOLESALE100") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                val success = onApplyVoucher(voucherCodeInput)
                                voucherStatusMsg = if (success) "Voucher applied!" else "Invalid code"
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text("Apply", fontSize = 11.sp)
                        }
                    }

                    voucherStatusMsg?.let {
                        Text(text = it, fontSize = 10.sp, color = SuccessEmerald, fontWeight = FontWeight.Bold)
                    }

                    // Account Status Actions
                    Text(
                        text = "ACCOUNT STATUS ACTIONS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onUpdateStatus(SubscriberStatus.ACTIVE) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Activate", fontSize = 10.sp, color = SuccessEmerald)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus(SubscriberStatus.PAUSED) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Pause", fontSize = 10.sp, color = WarningAmber)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus(SubscriberStatus.CANCELLED) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 10.sp, color = Color(0xFFEF4444))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onSavePlan(selectedTier) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Subscription Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODAL: TAX INVOICE DETAIL SHEET
// -------------------------------------------------------------

@Composable
fun InvoiceDetailModal(
    invoice: SubscriberInvoice,
    onDismiss: () -> Unit,
    onMarkPaid: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Invoice Branding Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SELLERSHUB STOCKPICK",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF7C3AED)
                        )
                        Text(
                            text = "Cloud Fulfillment Billing • apps.sellershub.io",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "VAT ID: GB 928 4719 22",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Invoice Numbers & Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "INVOICE NUMBER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = invoice.invoiceId, fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "BILLING PERIOD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = invoice.billingPeriod, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = invoice.status.color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = invoice.status.label.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = invoice.status.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(text = "DUE DATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = invoice.dueDate, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Billed To
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "BILLED TO SUBSCRIBER", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text(text = invoice.subscriberName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(text = "Subscriber ID: ${invoice.subscriberId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Payment Method: ${invoice.paymentMethod}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Transaction Ref: ${invoice.transactionRef}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Line Items
                Text(text = "LINE ITEMS", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = invoice.planTierName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "£${String.format(Locale.US, "%.2f", invoice.subtotalGbp)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Subtotal, VAT, Total
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Subtotal (excl. VAT)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "£${String.format(Locale.US, "%.2f", invoice.subtotalGbp)}", fontSize = 11.sp)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "UK VAT (${invoice.vatRatePercent}%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "£${String.format(Locale.US, "%.2f", invoice.vatAmountGbp)}", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Total Amount Due", fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text(text = "£${String.format(Locale.US, "%.2f", invoice.totalAmountGbp)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                if (invoice.status == InvoiceStatus.PENDING) {
                    Button(
                        onClick = onMarkPaid,
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Invoice as Paid", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Invoice View")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODAL: SUPER ADMIN ROLE SWITCHER
// -------------------------------------------------------------

@Composable
fun SuperAdminRoleSwitcherDialog(
    activeAdmin: PickerProfile,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "SUPER ADMIN DIRECTORY & ROLES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF7C3AED)
                )

                Spacer(modifier = Modifier.height(10.dp))

                SuperAdminDirectory.SUPER_ADMINS.forEach { member ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(member.role.badgeColor.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = member.avatarInitials,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = member.role.badgeColor
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(text = member.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = member.role.title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}
