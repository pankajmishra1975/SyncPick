package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PickingAuditLog
import com.example.data.model.PickingItem
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminHistoryView(
    auditLogs: List<PickingAuditLog>,
    pickedItems: List<PickingItem>,
    onRedispatch: (PickingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss • dd MMM", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxWidth()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Audit Trail Logs (${auditLogs.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Completed Items (${pickedItems.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        if (selectedTab == 0) {
            if (auditLogs.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = "No picking history logs yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs, key = { it.id }) { log ->
                        AuditLogRow(log = log, timeFormat = timeFormat)
                    }
                }
            }
        } else {
            if (pickedItems.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = "No completed items yet. Active items appear in Pick Queue.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pickedItems, key = { it.id }) { item ->
                        CompletedItemRow(
                            item = item,
                            timeFormat = timeFormat,
                            onRedispatch = { onRedispatch(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuditLogRow(
    log: PickingAuditLog,
    timeFormat: SimpleDateFormat
) {
    val actionColor = when (log.actionType) {
        "PICKED" -> SuccessEmerald
        "CLAIMED" -> WarningAmber
        "DISPATCHED" -> PrimaryBlue
        "FIELD_REPORT" -> AccentTeal
        "OUT_OF_STOCK" -> DangerRose
        else -> Slate600
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(actionColor.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = when (log.actionType) {
                        "PICKED" -> Icons.Default.Check
                        "CLAIMED" -> Icons.Default.Handshake
                        "DISPATCHED" -> Icons.Default.LocalShipping
                        "FIELD_REPORT" -> Icons.Default.CameraAlt
                        "OUT_OF_STOCK" -> Icons.Default.Warning
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = actionColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${log.orderNumber} • ${log.itemTitle}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = actionColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = log.actionType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = actionColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${log.pickerName} (${log.teamName})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (log.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = log.note,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${log.sourceStore} • ${log.salesChannel}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = timeFormat.format(Date(log.timestamp)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (!log.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100)
                        .border(1.dp, actionColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = if (log.photoUri.startsWith("/")) File(log.photoUri) else log.photoUri,
                        contentDescription = "Field Report Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedItemRow(
    item: PickingItem,
    timeFormat: SimpleDateFormat,
    onRedispatch: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SuccessEmerald.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.quantityRequired} ${item.unit} '${item.title}'",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Picked by ${item.pickedByPickerName ?: "Team"} at ${item.sourceStore}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.hasPickerReport) {
                    Text(
                        text = "🏷️ ${item.pickerConditionTags.ifBlank { "Note" }}: ${item.pickerComment}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }
                if (item.pickedAt != null) {
                    Text(
                        text = "Struck off: ${timeFormat.format(Date(item.pickedAt))}",
                        fontSize = 10.sp,
                        color = SuccessEmerald
                    )
                }
            }

            if (!item.pickerPhotoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100)
                ) {
                    AsyncImage(
                        model = if (item.pickerPhotoUri.startsWith("/")) File(item.pickerPhotoUri) else item.pickerPhotoUri,
                        contentDescription = "Aisle Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            IconButton(onClick = onRedispatch) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Re-dispatch to queue",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
