package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.PickingItem
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PickerItemCard(
    item: PickingItem,
    currentPickerId: String,
    isStriking: Boolean,
    onPick: () -> Unit,
    onPartialPickIncrement: () -> Unit,
    onClaim: () -> Unit,
    onUnclaim: () -> Unit,
    onOutOfStock: () -> Unit,
    onScanBarcode: () -> Unit,
    onOpenFieldReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isClaimedByMe = item.claimedByPickerId == currentPickerId
    val isClaimedByOther = item.isClaimed && !isClaimedByMe
    val isFullyPicked = item.isPicked || item.quantityPicked >= item.quantityRequired || isStriking

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyPicked) SuccessEmerald.copy(alpha = 0.08f)
            else if (isClaimedByMe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isClaimedByMe) 3.dp else 1.5.dp),
        border = BorderStroke(
            width = if (isClaimedByMe) 1.5.dp else if (isFullyPicked) 1.5.dp else 1.dp,
            color = if (isFullyPicked) SuccessEmerald
            else if (isClaimedByMe) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Order ID, Channel Badge, Priority Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${item.orderNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    ChannelBadge(channelName = item.salesChannel)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.priority == PickingItem.PRIORITY_RUSH) {
                        Surface(
                            color = DangerRose.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🔥 RUSH ORDER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DangerRose,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (item.priority == PickingItem.PRIORITY_HIGH) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⚡ HIGH PRIORITY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    StoreBadge(storeName = item.sourceStore)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Product Photo, Title, Quantity, Aisle Location
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProductImageThumbnail(
                    imageResName = item.imageResName,
                    imageUri = item.imageUri,
                    productTitle = item.title,
                    storeName = item.sourceStore,
                    isStriked = isFullyPicked,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isFullyPicked) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Quantity to pick badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isFullyPicked) SuccessEmerald else Slate900,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "QTY: ${item.quantityRequired} ${item.unit.uppercase()}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                if (item.quantityPicked > 0 && !isFullyPicked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${item.quantityPicked}/${item.quantityRequired} picked)",
                                        fontSize = 10.sp,
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (item.assignedTeam != "All Teams") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.assignedTeam,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Warehouse Location (Aisle & Bay)
                    if (item.aisleLocation.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.aisleLocation,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Barcode Info
                    if (item.barcode.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.barcode,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Admin Dispatch Notes
            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 ${item.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Picker Field Report Banner (Condition Tags, Comments & Attached Aisle/Product Photo)
            if (item.hasPickerReport) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.09f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenFieldReport() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "STORE FIELD UPDATE (${item.pickerReportedBy ?: "Picker"})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PrimaryBlue
                                )
                            }

                            if (item.conditionTagList.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    item.conditionTagList.forEach { tag ->
                                        val badgeColor = when {
                                            tag.contains("Damaged", ignoreCase = true) -> DangerRose
                                            tag.contains("Price", ignoreCase = true) -> WarningAmber
                                            tag.contains("Offer", ignoreCase = true) -> SuccessEmerald
                                            else -> PrimaryBlue
                                        }
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (item.pickerComment.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "💬 \"${item.pickerComment}\"",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Attached Store Aisle / Product Condition Photo Thumbnail
                        if (!item.pickerPhotoUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Slate100)
                                    .border(1.dp, PrimaryBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = if (item.pickerPhotoUri.startsWith("/")) File(item.pickerPhotoUri) else item.pickerPhotoUri,
                                    contentDescription = "Attached Store Aisle Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // Claim Status Banner if another team member claimed it
            if (isClaimedByOther) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = WarningAmber.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.claimedByPickerName} is currently picking this at store",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = WarningAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Out of stock report button
                OutlinedButton(
                    onClick = onOutOfStock,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = "Out of Stock",
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Store Aisle Photo & Condition Note Button (Damaged / New Packaging / Price Increase / On Offer)
                OutlinedButton(
                    onClick = onOpenFieldReport,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (item.hasPickerReport) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (item.hasPickerReport) PrimaryBlue else MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("field_report_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddAPhoto,
                        contentDescription = "Add Aisle Photo or Condition Note",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.hasPickerReport) "Edit Note" else "Note/Photo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Barcode Scan / Check helper button
                OutlinedButton(
                    onClick = onScanBarcode,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = "Scan Barcode",
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Claim toggle (if not yet claimed by anyone)
                if (!item.isClaimed) {
                    OutlinedButton(
                        onClick = onClaim,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Claim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isClaimedByMe) {
                    OutlinedButton(
                        onClick = onUnclaim,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Release", fontSize = 11.sp)
                    }
                }

                // Primary Action Button: "Pick Item" / "Strike Off"
                Button(
                    onClick = onPick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFullyPicked) SuccessEmerald else SuccessEmeraldDark,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isFullyPicked) "Picked!" else "Pick & Strike",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProductImageThumbnail(
    imageResName: String?,
    imageUri: String?,
    productTitle: String,
    storeName: String,
    isStriked: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(Slate100)
    ) {
        val resId = remember(imageResName) {
            if (imageResName != null) {
                context.resources.getIdentifier(imageResName, "drawable", context.packageName)
            } else 0
        }

        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = productTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!imageUri.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (imageUri.startsWith("/")) File(imageUri) else imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = productTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val iconBg = when {
                storeName.contains("Costco") -> CostcoRed
                storeName.contains("Bookers") -> BookersGreen
                storeName.contains("Bestway") -> BestwayOrange
                storeName.contains("Loon Fung") -> LoonFungGold
                storeName.contains("Sainsbury") -> SainsburysOrange
                storeName.contains("Tesco") -> TescoBlue
                else -> Slate700
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(iconBg.copy(alpha = 0.15f))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = iconBg,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = productTitle.take(6).uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconBg
                    )
                }
            }
        }

        if (isStriked) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(SuccessEmerald.copy(alpha = 0.75f))
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Picked",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
