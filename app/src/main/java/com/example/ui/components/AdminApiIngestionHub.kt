package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.data.model.WholesalerDirectory
import com.example.data.remote.ApiSyncResult
import com.example.data.remote.ExtractedPicklistRow
import com.example.data.remote.OrderApiIngestionService
import com.example.data.remote.PicklistPdfParseResult
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApiIngestionHub(
    apiService: OrderApiIngestionService,
    isSyncing: Boolean,
    lastSyncResult: ApiSyncResult?,
    syncHistory: List<ApiSyncResult>,
    pdfParseResult: PicklistPdfParseResult?,
    onUploadPicklistPdf: (Uri, String) -> Unit,
    onGenerateSamplePicklistPdf: () -> Unit,
    onUpdatePdfRow: (ExtractedPicklistRow) -> Unit,
    onDispatchPdfRows: () -> Unit,
    onOpenManualOrderDialog: () -> Unit,
    onSyncRestEndpoint: (String) -> Unit,
    onIngestWebhookJson: (String, String) -> Unit,
    onIngestCsvBulk: (String) -> Unit,
    isPdfAllowed: Boolean = true,
    isApiAllowed: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // 0: Picklist PDF Import, 1: Channel APIs & REST Sync, 2: Webhook JSON, 3: Bulk CSV
    var selectedSection by remember { mutableStateOf(0) }
    var endpointUrl by remember { mutableStateOf("https://dummyjson.com/products?limit=4") }
    var webhookJsonText by remember { mutableStateOf(apiService.generateChannelWebhookPayload("Amazon")) }
    var csvText by remember { mutableStateOf(apiService.getSampleCsvTemplate()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    val configuredApiKey = BuildConfig.ORDER_SYNC_API_KEY
    val isSecretKeyConfigured = configuredApiKey.isNotBlank() && configuredApiKey != "ORDER_SYNC_API_KEY_DEFAULT_VALUE"

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var displayName = "Manager_Picklist.pdf"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex) ?: displayName
                }
            }
            onUploadPicklistPdf(uri, displayName)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Top Header Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MANAGER ORDER DISPATCH & PDF / API HUB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentTeal,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Picklist PDF, API Sync & Manual Orders",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = onOpenManualOrderDialog,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("manual_dispatch_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manual Form", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Upload a Picklist PDF (extracts Item Title, SKU, Image & Quantity into individual orders per wholesaler), sync Amazon/eBay/Webstore APIs, or import CSV sheets.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }

        // Status Feedback Banner if a sync or PDF dispatch just completed
        if (lastSyncResult != null) {
            item {
                Surface(
                    color = if (lastSyncResult.success) SuccessEmerald.copy(alpha = 0.14f) else DangerRose.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (lastSyncResult.success) SuccessEmerald else DangerRose
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = if (lastSyncResult.success) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (lastSyncResult.success) SuccessEmerald else DangerRose,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${lastSyncResult.sourceLabel} • ${timeFormat.format(Date(lastSyncResult.timestamp))}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = lastSyncResult.statusMessage,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Sub-navigation Tabs for PDF, APIs, Webhook JSON & CSV
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 4.dp,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("1. Picklist PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("2. Channel APIs", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    text = { Text("3. Webhook JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    text = { Text("4. Bulk CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        when (selectedSection) {
            0 -> {
                // SECTION 0: Direct Picklist PDF Importer
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = DangerRose,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Direct Picklist PDF Importer",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Extracts Item Title, SKU, Image & Qty into individual store orders",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!isPdfAllowed) {
                                Surface(
                                    color = WarningAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("PDF Upload feature disabled on current Super Admin plan.", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isPdfAllowed) {
                                            pdfPickerLauncher.launch(arrayOf("application/pdf"))
                                        }
                                    },
                                    enabled = isPdfAllowed,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("upload_pdf_button")
                                ) {
                                    Icon(if (isPdfAllowed) Icons.Default.UploadFile else Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isPdfAllowed) "Upload Picklist PDF" else "PDF (Locked)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (isPdfAllowed) {
                                            onGenerateSamplePicklistPdf()
                                        }
                                    },
                                    enabled = isPdfAllowed,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("sample_pdf_button")
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Load Sample PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            if (isSyncing) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Rendering PDF pages & extracting SKUs, images & quantities...", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // If a Picklist PDF has been parsed, display the Document Preview + Extracted Individual Orders staging table!
                if (pdfParseResult != null) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "PARSED PDF MANIFEST • ${pdfParseResult.fileName}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = pdfParseResult.statusSummary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        color = SuccessEmerald.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${pdfParseResult.extractedRows.count { it.selected }} SELECTED",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SuccessEmerald,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Rendered Page 1 Document Preview Strip
                                if (!pdfParseResult.firstPagePreviewPath.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Rendered PDF Page Preview (PdfRenderer):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate100)
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = File(pdfParseResult.firstPagePreviewPath),
                                            contentDescription = "Rendered Picklist PDF Page",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "EXTRACTED INDIVIDUAL ORDERS FOR WHOLESALE STORES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap any wholesaler chip on an item below to override its store destination before dispatching to pickers:",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                pdfParseResult.extractedRows.forEach { row ->
                                    ExtractedPdfRowItemCard(
                                        row = row,
                                        onUpdateRow = onUpdatePdfRow
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = onDispatchPdfRows,
                                    enabled = pdfParseResult.extractedRows.any { it.selected },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SuccessEmeraldDark,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("dispatch_pdf_orders_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dispatch ${pdfParseResult.extractedRows.count { it.selected }} Orders to Live Pick Queue",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // SECTION 1: One-Click Channel API Sync Cards (Amazon, eBay, Webstore)
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Direct E-Commerce Channel Connectors",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Pull unfulfilled orders from your connected marketplaces and auto-route SKUs to wholesaler depots:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (!isApiAllowed) {
                                Surface(
                                    color = WarningAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("REST API & Marketplace Connectors disabled on current Super Admin plan.", fontSize = 11.sp, color = WarningAmber, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val payload = apiService.generateChannelWebhookPayload("Amazon")
                                        onIngestWebhookJson(payload, "Amazon SP-API Connector")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmazonOrange,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Sync Amazon", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        Text("SP-API Pull", fontSize = 9.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val payload = apiService.generateChannelWebhookPayload("eBay")
                                        onIngestWebhookJson(payload, "eBay Fulfillment API")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0064D2),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Sync eBay", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        Text("REST Orders", fontSize = 9.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val payload = apiService.generateChannelWebhookPayload("Webstore")
                                        onIngestWebhookJson(payload, "Webstore Webhook API")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WebstoreEmerald,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Text("Webstore", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        Text("Direct API", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Live HTTP GET REST Endpoint Polling Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Http,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Custom REST API Endpoint Sync (OkHttp)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Surface(
                                    color = if (isSecretKeyConfigured) SuccessEmerald.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isSecretKeyConfigured) "API KEY ACTIVE" else "PUBLIC / SECRETS PANEL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSecretKeyConfigured) SuccessEmerald else WarningAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Connect any JSON order feed URL. Authentication headers automatically use ORDER_SYNC_API_KEY from the AI Studio Secrets panel.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = endpointUrl,
                                onValueChange = { endpointUrl = it },
                                label = { Text("Order Feed REST Endpoint URL (GET JSON)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onSyncRestEndpoint(endpointUrl) },
                                enabled = !isSyncing && endpointUrl.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fetching & Routing Orders...")
                                } else {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Fetch Orders from Live REST URL", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // SECTION 2: JSON Webhook Payload Inspector & Ingestion
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "JSON Webhook Order Payload",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Paste or edit a JSON webhook payload from Amazon SP-API, eBay, Linnworks, or Shopify. Missing store/team fields are automatically routed by SKU.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                AssistChip(
                                    onClick = { webhookJsonText = apiService.generateChannelWebhookPayload("Amazon") },
                                    label = { Text("Amazon Template", fontSize = 11.sp) }
                                )
                                AssistChip(
                                    onClick = { webhookJsonText = apiService.generateChannelWebhookPayload("eBay") },
                                    label = { Text("eBay Template", fontSize = 11.sp) }
                                )
                                AssistChip(
                                    onClick = { webhookJsonText = apiService.generateChannelWebhookPayload("Webstore") },
                                    label = { Text("Webstore Template", fontSize = 11.sp) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = webhookJsonText,
                                onValueChange = { webhookJsonText = it },
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                minLines = 8,
                                maxLines = 14,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onIngestWebhookJson(webhookJsonText, "Manager JSON Webhook") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Webhook, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ingest JSON Payload & Dispatch to Teams", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            3 -> {
                // SECTION 3: Bulk CSV Order Sheet Import
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Bulk CSV Order Sheet Import",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                TextButton(onClick = { csvText = apiService.getSampleCsvTemplate() }) {
                                    Text("Reset Sample CSV", fontSize = 11.sp)
                                }
                            }

                            Text(
                                text = "Paste comma-separated rows exported from Excel, Seller Central, or ERP:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = csvText,
                                onValueChange = { csvText = it },
                                textStyle = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                minLines = 6,
                                maxLines = 12,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onIngestCsvBulk(csvText) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import CSV Batch to Live Pick Queue", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Recent API & Batch Sync Logs
        if (syncHistory.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT PDF, API & BATCH INGESTION LOGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(syncHistory) { log ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = if (log.success) Icons.Default.TaskAlt else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = if (log.success) SuccessEmerald else DangerRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.sourceLabel} (${log.importedItems.size} items)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = log.statusMessage,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = timeFormat.format(Date(log.timestamp)),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExtractedPdfRowItemCard(
    row: ExtractedPicklistRow,
    onUpdateRow: (ExtractedPicklistRow) -> Unit
) {
    Surface(
        color = if (row.selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (row.selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = row.selected,
                    onCheckedChange = { checked -> onUpdateRow(row.copy(selected = checked)) },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Extracted Product Image Thumbnail
                ProductImageThumbnail(
                    imageResName = row.imageResName,
                    imageUri = row.imageUri,
                    productTitle = row.title,
                    storeName = row.routedStore,
                    isStriked = false,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SKU: ${row.sku} • #${row.orderNumber} (${row.salesChannel})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Team: ${row.assignedTeam} • ${row.aisleLocation}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Quantity Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (row.quantity > 1) {
                                onUpdateRow(row.copy(quantity = row.quantity - 1))
                            }
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease Qty", modifier = Modifier.size(16.dp))
                    }
                    Surface(
                        color = Slate900,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${row.quantity} ${row.unit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    IconButton(
                        onClick = { onUpdateRow(row.copy(quantity = row.quantity + 1)) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase Qty", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Wholesaler Destination Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Store:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                WholesalerDirectory.STORE_NAMES.forEach { store ->
                    val isCurrentStore = row.routedStore.equals(store, ignoreCase = true)
                    FilterChip(
                        selected = isCurrentStore,
                        onClick = { onUpdateRow(row.copy(routedStore = store)) },
                        label = {
                            Text(
                                text = store.substringBefore(" "),
                                fontSize = 10.sp,
                                fontWeight = if (isCurrentStore) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(26.dp)
                    )
                }
            }
        }
    }
}
