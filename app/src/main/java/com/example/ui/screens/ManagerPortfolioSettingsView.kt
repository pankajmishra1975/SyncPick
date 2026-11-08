package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerPortfolioSettingsView(
    plan: SubscriptionPlan,
    activeManager: PickerProfile,
    totalItemsCount: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onSelectPlanTier: (SubscriptionPlan) -> Unit,
    onToggleBillingCycle: () -> Unit,
    onApplyVoucher: (String) -> Boolean,
    onCancelSubscription: () -> Unit,
    onPauseSubscription: (Int) -> Unit,
    onResumeSubscription: () -> Unit,
    onResetSampleData: () -> Unit,
    onGenerateDemoBatch: () -> Unit,
    invoices: List<SubscriberInvoice> = emptyList(),
    modifier: Modifier = Modifier
) {
    var voucherInput by remember { mutableStateOf("") }
    var voucherStatusMessage by remember { mutableStateOf<String?>(null) }
    var voucherIsSuccess by remember { mutableStateOf(false) }

    // Settings state
    var soundFeedbackEnabled by remember { mutableStateOf(true) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var showSupportChatDemo by remember { mutableStateOf(false) }
    var showShowAndTellModal by remember { mutableStateOf(false) }

    // Dialog state for Cancel / Pause
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showPauseConfirmDialog by remember { mutableStateOf(false) }
    var selectedPauseDurationDays by remember { mutableIntStateOf(30) }

    // Selected Invoice for detail / receipt modal
    var viewingInvoiceModal by remember { mutableStateOf<SubscriberInvoice?>(null) }
    var invoiceDownloadFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Section 1: Subscriber Account Profile Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manager_subscriber_account_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Apex Wholesale Logistics Ltd.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Subscriber Account #SUB-84920 • VAT GB 948 2018 44",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Account Plan Status Badge
                        val (statusBadgeText, statusBadgeColor) = when {
                            plan.status == "PAUSED" -> "PAUSED" to WarningAmber
                            plan.status == "CANCELLED_AT_PERIOD_END" -> "CANCELLING" to MaterialTheme.colorScheme.error
                            plan.isFreeSubscription -> "FREEMIUM ACTIVE" to SuccessEmerald
                            else -> "PAID LICENSE" to PrimaryBlue
                        }
                        Surface(
                            color = statusBadgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = statusBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = statusBadgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Manager Info & Current Tier Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LOGGED-IN PORTFOLIO MANAGER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${activeManager.name} (${activeManager.employeeCode})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CURRENT PLAN TIER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = plan.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Notification if subscription is paused or cancelling
                    if (plan.status == "PAUSED") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = WarningAmber.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PauseCircle, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Subscription is paused. Billing is on hold.", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = WarningAmber)
                                }
                                TextButton(onClick = onResumeSubscription, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                    Text("Resume Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                            }
                        }
                    } else if (plan.status == "CANCELLED_AT_PERIOD_END") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scheduled to downgrade to Freemium at period end.", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.error)
                                }
                                TextButton(onClick = onResumeSubscription, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                    Text("Keep Plan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Quota & Metering Tracking
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Account Quota & Metering",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = if (plan.isFreeSubscription) "${plan.trialDaysRemaining} days remaining in trial" else "Monthly Auto-Renewal",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (plan.isFreeSubscription) SuccessEmerald else PrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Items Quota Meter
                    val itemLimit = if (plan.isUnlimitedItems) 10000 else plan.maxItemsQuota
                    val itemProgress = (totalItemsCount.toFloat() / itemLimit.toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Orders / Items Processed",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$totalItemsCount / ${if (plan.isUnlimitedItems) "Unlimited" else plan.maxItemsQuota}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { itemProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (itemProgress > 0.85f) WarningAmber else SuccessEmerald,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pickers & Managers Allowance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Pickers Allowed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "5 active / ${if (plan.isUnlimitedPickers) "∞" else plan.maxPickersQuota} max",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Managers Allowed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "2 active / ${if (plan.isUnlimitedManagers) "∞" else plan.maxManagersQuota} max",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feature Entitlements Checklist
                    Text(
                        text = "FEATURES INCLUDED IN YOUR CURRENT PLAN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureEntitlementChip(
                            label = "Camera Floor Notes",
                            isAllowed = plan.features.allowCameraUse,
                            modifier = Modifier.weight(1f)
                        )
                        FeatureEntitlementChip(
                            label = "PDF Picklist Parser",
                            isAllowed = plan.features.allowPdfUploads,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureEntitlementChip(
                            label = "REST API & Webhooks",
                            isAllowed = plan.features.allowApiSync,
                            modifier = Modifier.weight(1f)
                        )
                        FeatureEntitlementChip(
                            label = "Live Sync Telemetry",
                            isAllowed = plan.features.allowLiveSimulation,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FeatureEntitlementChip(
                            label = "Barcode Scanning",
                            isAllowed = plan.features.allowBarcodeScanning,
                            modifier = Modifier.weight(1f)
                        )
                        FeatureEntitlementChip(
                            label = "Audit History & KPIs",
                            isAllowed = plan.features.allowAdvancedKpiExport,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section 3: Upgrade & Downgrade Subscription Plans
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Upgrade,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subscription Plans & Upgrades",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        // Billing cycle toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onToggleBillingCycle() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("billing_cycle_toggle")
                        ) {
                            Text(
                                text = if (plan.billingCycle == BillingCycle.ANNUAL) "Annual (Save 20%)" else "Monthly",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Toggle cycle",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Public Freemium Tiers
                    SuperAdminDirectory.PUBLIC_FREEMIUM_TIERS.forEach { tier ->
                        val isCurrent = tier.planId == plan.planId
                        val isHigherTier = tier.monthlyPriceGbp > plan.monthlyPriceGbp
                        Surface(
                            color = if (isCurrent)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                if (isCurrent) 1.5.dp else 1.dp,
                                if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = tier.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (tier.isFreeSubscription) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = SuccessEmerald.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "FREE FOREVER",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = SuccessEmerald,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${tier.maxItemsQuota} items • ${tier.maxPickersQuota} pickers • ${tier.description}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val priceText = if (tier.monthlyPriceGbp == 0.0) {
                                        "£0 / mo"
                                    } else if (plan.billingCycle == BillingCycle.ANNUAL) {
                                        "£${String.format(Locale.US, "%.1f", tier.monthlyPriceGbp * 0.80)} / mo"
                                    } else {
                                        "£${String.format(Locale.US, "%.0f", tier.monthlyPriceGbp)} / mo"
                                    }
                                    Text(
                                        text = priceText,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isCurrent) {
                                        Surface(
                                            color = SuccessEmerald.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "✓ ACTIVE PLAN",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                color = SuccessEmerald,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Button(
                                            onClick = { onSelectPlanTier(tier) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isHigherTier) PrimaryBlue else MaterialTheme.colorScheme.secondary
                                            ),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(
                                                text = if (isHigherTier) "Upgrade" else "Downgrade",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voucher Redemption Box
                    Text(
                        text = "REDEEM PROMO / DISCOUNT CODE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = voucherInput,
                            onValueChange = { voucherInput = it },
                            placeholder = { Text("e.g. WHOLESALE100 or LAUNCH50", fontSize = 11.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("voucher_input_field")
                        )
                        Button(
                            onClick = {
                                if (voucherInput.isNotBlank()) {
                                    val success = onApplyVoucher(voucherInput)
                                    voucherIsSuccess = success
                                    voucherStatusMessage = if (success)
                                        "Voucher applied! Discount is active on your portfolio."
                                    else
                                        "Invalid voucher code. Try WHOLESALE100 or LAUNCH50."
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("apply_voucher_button")
                        ) {
                            Text("Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = voucherStatusMessage != null) {
                        voucherStatusMessage?.let { msg ->
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (voucherIsSuccess) SuccessEmerald else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Cancel or Pause Subscription
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PauseCircleOutline,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pause or Cancel Subscription",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Need to adjust your warehouse cycle? You can pause your subscription temporarily to freeze billing while keeping all historical orders, or cancel to downgrade to the Starter Freemium tier.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPauseConfirmDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, WarningAmber),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pause_subscription_button")
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (plan.status == "PAUSED") "Adjust Pause" else "Pause Plan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }

                        OutlinedButton(
                            onClick = { showCancelConfirmDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_subscription_button")
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (plan.status == "CANCELLED_AT_PERIOD_END") "Re-activate" else "Cancel Plan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Subscriber Invoices & Payment History
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscriber_invoices_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Invoices & Payment History",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${invoices.size} Statements",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Official statements and VAT receipts for Apex Wholesale Logistics Ltd. (SUB-84920).",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (invoices.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No past invoices found on this account.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        invoices.forEach { invoice ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewingInvoiceModal = invoice }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = invoice.invoiceId,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = if (invoice.status == InvoiceStatus.PAID) SuccessEmerald.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = invoice.status.name,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (invoice.status == InvoiceStatus.PAID) SuccessEmerald else WarningAmber,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${invoice.issueDate} • ${invoice.planTierName}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "£${String.format(Locale.US, "%.2f", invoice.totalAmountGbp)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "View receipt",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 6: Support Tier Assistance & Help Guide
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = SuccessEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subscriber Support & SLA",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            color = if (plan.supportTier == SupportTier.PREMIUM_CHAT) SuccessEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (plan.supportTier == SupportTier.PREMIUM_CHAT) "24/7 PRIORITY CHAT" else "STANDARD TICKET",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (plan.supportTier == SupportTier.PREMIUM_CHAT) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = plan.supportTier.slaDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSupportChatDemo = !showSupportChatDemo },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (plan.supportTier == SupportTier.PREMIUM_CHAT) SuccessEmerald else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_support_button")
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (plan.supportTier == SupportTier.PREMIUM_CHAT) "24/7 Live Chat" else "Log Ticket",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showShowAndTellModal = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.HelpCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tutorials & FAQ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = showSupportChatDemo) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (plan.supportTier == SupportTier.PREMIUM_CHAT)
                                        "🟢 24/7 Live Support Connected (Response time: 42s). Dedicated account engineer assigned to Apex Wholesale Logistics Ltd."
                                    else
                                        "📩 Support ticket logged for Account #SUB-84920. Standard SLA is 3–5 business days. For faster turnaround, upgrade to Wholesale Pro.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 7: Useful Settings Section (Light/Dark mode toggle, sounds, demo data)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Useful Settings",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Light & Dark Mode Toggle (Default: Light Mode)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Theme Appearance",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkMode) "Dark Mode active" else "Light Mode active (Default)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleDarkMode(it) },
                            modifier = Modifier.testTag("settings_theme_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio / Haptic Feedback Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Sound & Vibration Feedback",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Audible beep on item scan and strike-off",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = soundFeedbackEnabled,
                            onCheckedChange = { soundFeedbackEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-time Auto-Sync Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Sync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Live Floor Queue Telemetry",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Auto-pull pick events across multiple pickers",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = autoSyncEnabled,
                            onCheckedChange = { autoSyncEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Demo Data Helpers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onGenerateDemoBatch,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate Batch", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onResetSampleData,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Data", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal: Invoice & Tax Receipt Detail
    viewingInvoiceModal?.let { inv ->
        AlertDialog(
            onDismissRequest = {
                viewingInvoiceModal = null
                invoiceDownloadFeedback = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(text = "Official Tax Invoice", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(text = inv.invoiceId, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Surface(
                        color = SuccessEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "PAID",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = SuccessEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "BILLED TO:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Apex Wholesale Logistics Ltd.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Account: #SUB-84920 • VAT: GB 948 2018 44", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Contact: Marcus Vance • billing@apexwholesale.co.uk", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "BILLING DETAILS:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Issue Date: ${inv.issueDate} • Period: ${inv.billingPeriod}", fontSize = 11.sp)
                    Text(text = "Payment Method: ${inv.paymentMethod}", fontSize = 11.sp)
                    Text(text = "Transaction Ref: ${inv.transactionRef}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subscription: ${inv.planTierName}", fontSize = 11.sp)
                                Text("£${String.format(Locale.US, "%.2f", inv.subtotalGbp)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("UK VAT (20%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("£${String.format(Locale.US, "%.2f", inv.vatAmountGbp)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Paid", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                Text("£${String.format(Locale.US, "%.2f", inv.totalAmountGbp)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                            }
                        }
                    }

                    AnimatedVisibility(visible = invoiceDownloadFeedback != null) {
                        invoiceDownloadFeedback?.let { fb ->
                            Surface(
                                color = SuccessEmerald.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = fb,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SuccessEmerald,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        invoiceDownloadFeedback = "✓ PDF receipt for ${inv.invoiceId} downloaded to device."
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewingInvoiceModal = null
                        invoiceDownloadFeedback = null
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Modal: Cancel Subscription Confirmation Dialog
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = {
                Text(
                    text = if (plan.status == "CANCELLED_AT_PERIOD_END") "Re-activate Subscription?" else "Cancel Subscription?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column {
                    if (plan.status == "CANCELLED_AT_PERIOD_END") {
                        Text(
                            text = "Your subscription was scheduled to cancel. Would you like to resume your active plan (${plan.name})?",
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "Are you sure you want to cancel your ${plan.name} subscription? Your warehouse will continue to operate normally until the end of your billing cycle, after which it will safely downgrade to the Starter Freemium plan with zero data loss.",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• Picklists, barcode history, and order logs remain saved.\n• You can re-upgrade at any time without reconfiguration.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (plan.status == "CANCELLED_AT_PERIOD_END") {
                            onResumeSubscription()
                        } else {
                            onCancelSubscription()
                        }
                        showCancelConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (plan.status == "CANCELLED_AT_PERIOD_END") PrimaryBlue else MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (plan.status == "CANCELLED_AT_PERIOD_END") "Keep Subscription" else "Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("Keep Active")
                }
            }
        )
    }

    // Modal: Pause Subscription Dialog
    if (showPauseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPauseConfirmDialog = false },
            title = {
                Text(text = "Pause Portfolio Subscription", fontSize = 16.sp, fontWeight = FontWeight.Black)
            },
            text = {
                Column {
                    Text(
                        text = "Freeze recurring billing temporarily during seasonal inventory slowdowns. All your data and staff profiles are safely stored.",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "SELECT PAUSE DURATION:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 90).forEach { days ->
                            val selected = selectedPauseDurationDays == days
                            Surface(
                                color = if (selected) WarningAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (selected) WarningAmber else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPauseDurationDays = days }
                            ) {
                                Text(
                                    text = "$days Days",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) WarningAmber else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPauseSubscription(selectedPauseDurationDays)
                        showPauseConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pause for $selectedPauseDurationDays Days", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPauseConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Show and Tell / FAQ Tutorials Guide
    if (showShowAndTellModal) {
        AlertDialog(
            onDismissRequest = { showShowAndTellModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "WholesaleSync Features & FAQ", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Published at apps.sellershub.io/stockpick for subscriber managers and staff.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "• Barcode Scanning: Tap 'Scan Barcode' to verify against wholesaler EAN tags.", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Camera Floor Notes: Capture pallet pictures or shelf tags directly onto pick tickets.", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Picklist PDF Parser: Import supplier invoices or PDF picking lists without manual typing.", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "• Invoices: Download full VAT tax statements directly from your Portfolio tab.", fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showShowAndTellModal = false }, shape = RoundedCornerShape(8.dp)) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
private fun FeatureEntitlementChip(
    label: String,
    isAllowed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isAllowed) SuccessEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isAllowed) SuccessEmerald.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        ),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (isAllowed) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isAllowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
