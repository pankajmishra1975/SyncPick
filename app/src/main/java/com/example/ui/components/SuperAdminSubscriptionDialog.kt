package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminSubscriptionDialog(
    plan: SubscriptionPlan,
    activeSuperAdmin: PickerProfile,
    onDismiss: () -> Unit,
    onSavePlan: (SubscriptionPlan) -> Unit,
    onGrantFree: (days: Int, items: Int, managers: Int, pickers: Int) -> Unit,
    onApplyVoucher: (String) -> Boolean,
    onToggleFeature: (camera: Boolean?, pdf: Boolean?, api: Boolean?, sim: Boolean?, kpi: Boolean?, barcode: Boolean?) -> Unit,
    onSuperAdminSelected: ((SuperAdminMember) -> Unit)? = null
) {
    var currentSuperAdmin by remember(activeSuperAdmin) {
        mutableStateOf(SuperAdminDirectory.SUPER_ADMINS.find { it.id == activeSuperAdmin.id } ?: SuperAdminDirectory.SUPER_ADMINS.first())
    }
    var isFreeSubscription by remember { mutableStateOf(plan.isFreeSubscription) }
    var selectedCycle by remember { mutableStateOf(plan.billingCycle) }
    var trialDays by remember { mutableStateOf(plan.trialDaysRemaining.toString()) }
    var maxItemsQuota by remember { mutableStateOf(if (plan.maxItemsQuota <= 0) "Unlimited" else plan.maxItemsQuota.toString()) }
    var maxManagersQuota by remember { mutableStateOf(plan.maxManagersQuota.toString()) }
    var maxPickersQuota by remember { mutableStateOf(plan.maxPickersQuota.toString()) }
    var selectedSupportTier by remember { mutableStateOf(plan.supportTier) }
    var voucherInput by remember { mutableStateOf("") }
    var voucherMessage by remember { mutableStateOf<String?>(null) }

    var cameraEnabled by remember { mutableStateOf(plan.features.allowCameraUse) }
    var pdfEnabled by remember { mutableStateOf(plan.features.allowPdfUploads) }
    var apiEnabled by remember { mutableStateOf(plan.features.allowApiSync) }
    var simEnabled by remember { mutableStateOf(plan.features.allowLiveSimulation) }
    var kpiEnabled by remember { mutableStateOf(plan.features.allowAdvancedKpiExport) }
    var barcodeEnabled by remember { mutableStateOf(plan.features.allowBarcodeScanning) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF7C3AED))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Diamond,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Super Admin Plan & License Console",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Logged in as ${currentSuperAdmin.name} (${currentSuperAdmin.role.title})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Plan Controls
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Super Admin Team Members & Defined Roles Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SUPER ADMIN TEAM DIRECTORY & DEFINED ROLES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap any Super Admin to switch acting authority & credentials",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            SuperAdminDirectory.SUPER_ADMINS.forEach { admin ->
                                val isCurrent = admin.id == currentSuperAdmin.id
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            currentSuperAdmin = admin
                                            onSuperAdminSelected?.invoke(admin)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(admin.role.badgeColor)
                                    ) {
                                        Text(
                                            text = admin.name.take(2).uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = admin.name + if (isCurrent) " (Active Signatory)" else "",
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = admin.role.title,
                                            fontSize = 10.sp,
                                            color = admin.role.badgeColor,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = admin.email,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1. Free Subscription vs Paid Plan Toggle
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFreeSubscription) SuccessEmerald.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, if (isFreeSubscription) SuccessEmerald else MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Free Subscription Grant",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Waives all billing charges (complimentary enterprise grant by Super Admin)",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isFreeSubscription,
                                    onCheckedChange = { isFreeSubscription = it },
                                    modifier = Modifier.testTag("switch_free_subscription")
                                )
                            }
                        }
                    }

                    // 2. Billing Cycle: Monthly vs Annual vs Free Trial
                    Text(
                        text = "BILLING CYCLE & TRIAL PERIOD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BillingCycle.values().forEach { cycle ->
                            FilterChip(
                                selected = selectedCycle == cycle,
                                onClick = { selectedCycle = cycle },
                                label = {
                                    Text(
                                        text = cycle.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedCycle == cycle) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Trial Days Counter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trial / Validity Days Remaining",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Extend or grant specific access window",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(7, 14, 30, 60, 90, 365).forEach { d ->
                                SuggestionChip(
                                    onClick = { trialDays = d.toString() },
                                    label = { Text("${d}d", fontSize = 10.sp) },
                                    shape = RoundedCornerShape(6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = trialDays,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) trialDays = it },
                        label = { Text("Number of Days") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. Quotas: Items, Managers, Pickers
                    Text(
                        text = "CAPACITIES & SEAT ALLOCATION QUOTAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = maxItemsQuota,
                            onValueChange = { maxItemsQuota = it },
                            label = { Text("Max Items") },
                            placeholder = { Text("e.g. 500, 2500, Unlimited") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxManagersQuota,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) maxManagersQuota = it },
                            label = { Text("Max Managers") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxPickersQuota,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) maxPickersQuota = it },
                            label = { Text("Max Pickers") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 4. Special Features Ticking / Unticking
                    Text(
                        text = "FEATURE ENTITLEMENTS (ENABLE / RESTRICT)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            FeatureCheckboxRow(
                                title = "Camera & Photo Evidence",
                                description = "Allows pickers to capture store aisle photos, price stickers & damaged packaging",
                                checked = cameraEnabled,
                                onCheckedChange = { cameraEnabled = it }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            FeatureCheckboxRow(
                                title = "PDF Picklist Import (PdfRenderer)",
                                description = "Extracts product titles, SKUs, and embedded photos into separate store orders",
                                checked = pdfEnabled,
                                onCheckedChange = { pdfEnabled = it }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            FeatureCheckboxRow(
                                title = "REST API & Webhook Connectors",
                                description = "Live syncing from Amazon SP-API, eBay, Linnworks, and custom HTTP endpoints",
                                checked = apiEnabled,
                                onCheckedChange = { apiEnabled = it }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            FeatureCheckboxRow(
                                title = "Live Multi-Team Sync Simulation",
                                description = "Simulates concurrent picking field actions in real time across wholesaler depots",
                                checked = simEnabled,
                                onCheckedChange = { simEnabled = it }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            FeatureCheckboxRow(
                                title = "Barcode / SKU Scanner",
                                description = "Verifies EAN/UPC barcodes in the store aisle before striking off",
                                checked = barcodeEnabled,
                                onCheckedChange = { barcodeEnabled = it }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            FeatureCheckboxRow(
                                title = "Advanced Admin KPI Metrics",
                                description = "Pick speed benchmarks, fulfillment rates, and auditor logs",
                                checked = kpiEnabled,
                                onCheckedChange = { kpiEnabled = it }
                            )
                        }
                    }

                    // 5. Support SLA Tier
                    Text(
                        text = "SUPPORT LEVEL & SLA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SupportTier.values().forEach { tier ->
                            val isSelected = selectedSupportTier == tier
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSupportTier = tier }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = tier.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tier.slaDescription,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // 6. Voucher Codes
                    Text(
                        text = "VOUCHER CODE & SPECIAL DISCOUNTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = voucherInput,
                            onValueChange = { voucherInput = it },
                            placeholder = { Text("e.g. WHOLESALE100, SUPER50") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val success = onApplyVoucher(voucherInput)
                                voucherMessage = if (success) "Voucher applied successfully!" else "Invalid code."
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Apply")
                        }
                    }

                    // Preset quick vouchers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuperAdminDirectory.PRESET_VOUCHERS.forEach { (code, discount) ->
                            AssistChip(
                                onClick = {
                                    voucherInput = code
                                    onApplyVoucher(code)
                                    voucherMessage = "Applied $code ($discount% off)"
                                    if (discount >= 100) isFreeSubscription = true
                                },
                                label = { Text("$code (-$discount%)", fontSize = 10.sp) }
                            )
                        }
                    }

                    if (voucherMessage != null) {
                        Text(
                            text = voucherMessage!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (voucherMessage!!.contains("success", ignoreCase = true) || voucherMessage!!.contains("Applied", ignoreCase = true))
                                SuccessEmerald else DangerRose
                        )
                    }

                    // Live Pricing Summary
                    val itemsQuotaParsed = maxItemsQuota.toIntOrNull() ?: if (maxItemsQuota.contains("unlimited", ignoreCase = true)) 99999 else 2500
                    val managersQuotaParsed = maxManagersQuota.toIntOrNull() ?: 5
                    val pickersQuotaParsed = maxPickersQuota.toIntOrNull() ?: 25
                    val daysParsed = trialDays.toIntOrNull() ?: 14

                    val previewPlan = plan.copy(
                        isFreeSubscription = isFreeSubscription,
                        billingCycle = selectedCycle,
                        trialDaysRemaining = daysParsed,
                        maxItemsQuota = itemsQuotaParsed,
                        maxManagersQuota = managersQuotaParsed,
                        maxPickersQuota = pickersQuotaParsed,
                        supportTier = selectedSupportTier,
                        features = PlanFeatures(
                            allowCameraUse = cameraEnabled,
                            allowPdfUploads = pdfEnabled,
                            allowApiSync = apiEnabled,
                            allowLiveSimulation = simEnabled,
                            allowAdvancedKpiExport = kpiEnabled,
                            allowBarcodeScanning = barcodeEnabled
                        ),
                        grantedBySuperAdminId = currentSuperAdmin.id,
                        grantedBySuperAdminName = currentSuperAdmin.name
                    )

                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "EFFECTIVE PLAN CHARGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = if (previewPlan.isFreeSubscription) "FREE (100% Waived)" else "£${String.format(Locale.US, "%.2f", previewPlan.effectivePriceGbp)} / ${selectedCycle.title}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (previewPlan.isFreeSubscription) SuccessEmerald else Color.White
                                )
                                Text(
                                    text = "${previewPlan.maxPickersQuota} Pickers • ${previewPlan.maxManagersQuota} Managers • ${if (previewPlan.isUnlimitedItems) "Unlimited" else previewPlan.maxItemsQuota} Items",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Button(
                                onClick = {
                                    onSavePlan(previewPlan)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save & Apply Plan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckboxRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
