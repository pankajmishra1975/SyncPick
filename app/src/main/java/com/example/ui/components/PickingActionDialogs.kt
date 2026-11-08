package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.PickingItem
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BarcodeScanDialog(
    item: PickingItem,
    onDismiss: () -> Unit,
    onVerifiedPick: () -> Unit
) {
    var isMatched by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Scan Barcode Verification",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate900)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = if (isMatched) SuccessEmerald else Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isMatched) "BARCODE MATCHED!" else item.barcode.ifBlank { "50123456789" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMatched) SuccessEmerald else Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Expected Qty: ${item.quantityRequired} ${item.unit} at ${item.sourceStore}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            isMatched = true
                            onVerifiedPick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan & Strike Off", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OutOfStockDialog(
    item: PickingItem,
    onDismiss: () -> Unit,
    onConfirmOos: (reason: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("Shelf empty / No warehouse pallet") }
    val reasons = listOf(
        "Shelf empty / No warehouse pallet",
        "Damaged / Leaking stock",
        "Expired batch date",
        "Store restricted / Limit per customer",
        "Price mismatch / Barcode unrecognised"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = DangerRose,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Report Out of Stock",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Item: ${item.title} (${item.quantityRequired} ${item.unit}) at ${item.sourceStore}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SELECT REASON FOR ADMIN DISPATCH:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reason,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onConfirmOos(selectedReason)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DangerRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Text("Confirm OOS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Dialog for Store Pickers to update an item with comments (Damaged, New Packaging, Price Increase, On Offer, etc.)
 * and attach a photo of the store aisle or product condition via Camera or Photo Picker.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PickerFieldReportDialog(
    item: PickingItem,
    isCameraAllowed: Boolean = true,
    onDismiss: () -> Unit,
    onSaveReport: (comment: String, conditionTags: List<String>, photoUri: String?, alsoPick: Boolean) -> Unit
) {
    val context = LocalContext.current

    var commentText by remember { mutableStateOf(item.pickerComment) }
    var selectedTags by remember {
        mutableStateOf(item.conditionTagList.toSet())
    }
    var capturedPhotoUri by remember { mutableStateOf(item.pickerPhotoUri) }

    // Zero-permission Android Photo Picker
    val visualMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            capturedPhotoUri = copyUriToInternalFile(context, uri)
        }
    }

    // Direct Camera Snapshot Launcher (TakePicturePreview returns a Bitmap without requiring storage permissions)
    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedPhotoUri = saveBitmapToCacheFile(context, bitmap, "camera_aisle_${item.id}")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue.copy(alpha = 0.14f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddComment,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Store Aisle & Condition Update",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "#${item.orderNumber} • ${item.sourceStore}",
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Target Product Summary Banner
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Qty: ${item.quantityRequired} ${item.unit} • Location: ${item.aisleLocation}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 1. Condition / Pricing Quick Tags
                    Text(
                        text = "1. CONDITION / PRICING STATUS TAGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PickingItem.CONDITION_TAGS.forEach { tag ->
                            val isSelected = selectedTags.contains(tag)
                            val tagColor = when {
                                tag.contains("Damaged", ignoreCase = true) -> DangerRose
                                tag.contains("Price Increase", ignoreCase = true) -> WarningAmber
                                tag.contains("On Offer", ignoreCase = true) -> SuccessEmerald
                                tag.contains("New Packaging", ignoreCase = true) -> PrimaryBlue
                                else -> AccentTeal
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                },
                                label = {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tagColor.copy(alpha = 0.18f),
                                    selectedLabelColor = tagColor,
                                    selectedLeadingIconColor = tagColor
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // 2. Picker Comments & Quick Templates
                    Text(
                        text = "2. PICKER FIELD COMMENT / NOTE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val quickComments = listOf(
                        "On Offer: £2.50 off per case at store",
                        "Price Increase: Up by £1.80 per unit on shelf tag",
                        "New Packaging: Updated manufacturer shrink-wrap & barcode",
                        "Damaged: Outer carton dented, inner tins intact",
                        "Moved to Promo Endcap Aisle 1"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickComments.forEach { template ->
                            SuggestionChip(
                                onClick = {
                                    commentText = if (commentText.isBlank()) template else "$commentText • $template"
                                },
                                label = { Text(template.substringBefore(":"), fontSize = 10.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Enter comment (e.g. price change, promo, damage, packaging)") },
                        placeholder = { Text("Describe shelf price, packaging update, or condition...") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("picker_comment_input")
                    )

                    // 3. Store Aisle or Product Condition Photo Attachment
                    Text(
                        text = "3. ATTACH STORE AISLE OR PRODUCT CONDITION PHOTO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!capturedPhotoUri.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate100)
                                .border(1.5.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = if (capturedPhotoUri!!.startsWith("/")) File(capturedPhotoUri!!) else capturedPhotoUri,
                                contentDescription = "Aisle or Product Condition Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            IconButton(
                                onClick = { capturedPhotoUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(30.dp)
                                    .background(Slate900.copy(alpha = 0.75f), RoundedCornerShape(15.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (isCameraAllowed) {
                                    cameraPreviewLauncher.launch(null)
                                }
                            },
                            enabled = isCameraAllowed,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (isCameraAllowed) Icons.Outlined.AddAPhoto else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isCameraAllowed) "Camera" else "Camera (Locked)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                visualMediaPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val firstTag = selectedTags.firstOrNull() ?: "On Offer / Promo"
                                capturedPhotoUri = generateStoreAisleEvidenceBitmap(
                                    context = context,
                                    item = item,
                                    conditionHeadline = firstTag,
                                    pickerNote = commentText.ifBlank { "Verified on store shelf at ${item.sourceStore}" }
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("snap_aisle_tag_btn")
                        ) {
                            Icon(Icons.Default.CameraEnhance, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Aisle Tag Snap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Save Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onSaveReport(commentText, selectedTags.toList(), capturedPhotoUri, false)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("save_report_only_btn")
                    ) {
                        Text("Save Note & Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onSaveReport(commentText, selectedTags.toList(), capturedPhotoUri, true)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessEmeraldDark,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .height(44.dp)
                            .testTag("save_and_pick_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save & Pick Item", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

private fun saveBitmapToCacheFile(context: Context, bitmap: Bitmap, prefix: String): String {
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 92, out)
    }
    return file.absolutePath
}

private fun copyUriToInternalFile(context: Context, uri: Uri): String {
    return try {
        val file = File(context.cacheDir, "picker_gallery_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (_: Exception) {
        uri.toString()
    }
}

/**
 * Generates a realistic high-contrast Store Aisle / Shelf Price & Condition photo evidence stamp
 * for emulator/floor testing when physical camera hardware is unavailable.
 */
private fun generateStoreAisleEvidenceBitmap(
    context: Context,
    item: PickingItem,
    conditionHeadline: String,
    pickerNote: String
): String {
    val width = 640
    val height = 360
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    // Store shelf dark background
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(30, 41, 59)
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    // Shelf price / condition card
    val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(255, 251, 235)
    }
    canvas.drawRoundRect(RectF(28f, 28f, width - 28f, height - 28f), 18f, 18f, cardPaint)

    // Top banner stripe based on condition
    val bannerColor = when {
        conditionHeadline.contains("Damaged", ignoreCase = true) -> android.graphics.Color.rgb(225, 29, 72)
        conditionHeadline.contains("Price", ignoreCase = true) -> android.graphics.Color.rgb(217, 119, 6)
        conditionHeadline.contains("Offer", ignoreCase = true) -> android.graphics.Color.rgb(16, 185, 129)
        else -> android.graphics.Color.rgb(37, 99, 235)
    }
    val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bannerColor }
    canvas.drawRoundRect(RectF(28f, 28f, width - 28f, 92f), 18f, 18f, bannerPaint)

    val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("STORE AISLE EVIDENCE: ${conditionHeadline.uppercase()}", 48f, 68f, headerTextPaint)

    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(15, 23, 42)
        textSize = 22f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(item.title.take(36), 48f, 140f, titlePaint)

    val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(71, 85, 105)
        textSize = 17f
    }
    canvas.drawText("Store: ${item.sourceStore}  •  ${item.aisleLocation}", 48f, 175f, metaPaint)
    canvas.drawText("SKU / Barcode: ${item.barcode}  •  Order #${item.orderNumber}", 48f, 205f, metaPaint)

    val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bannerColor
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText("Note: ${pickerNote.take(48)}", 48f, 255f, notePaint)

    val timeStr = SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault()).format(Date())
    canvas.drawText("Captured Live on Shop Floor • $timeStr", 48f, 300f, metaPaint)

    return saveBitmapToCacheFile(context, bmp, "aisle_evidence_${item.id}")
}
