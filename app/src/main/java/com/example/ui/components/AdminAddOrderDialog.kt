package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddOrderDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        quantity: Int,
        unit: String,
        store: String,
        channel: String,
        team: String,
        imageUri: String?,
        imageResName: String?,
        barcode: String,
        aisle: String,
        priority: String,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("6") }
    var selectedUnit by remember { mutableStateOf("Units") }
    var selectedStore by remember { mutableStateOf(WholesalerDirectory.STORE_NAMES.first()) }
    var selectedChannel by remember { mutableStateOf("Amazon") }
    var selectedTeam by remember { mutableStateOf("All Teams") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var selectedImageResName by remember { mutableStateOf<String?>(null) }
    var barcode by remember { mutableStateOf("") }
    var aisleLocation by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(PickingItem.PRIORITY_NORMAL) }
    var notes by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
            selectedImageResName = null
        }
    }

    val units = listOf("Units", "Cases", "Tins", "Bottles", "Sacks", "Boxes", "Packs", "Crates")
    val channels = listOf("Amazon", "eBay", "Webstore")

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
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Dispatch New Order",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Add online store picking item for teams",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Preset Template Bar
                    Text(
                        text = "QUICK TEMPLATES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PresetCatalog.PRESETS.forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    title = preset.title
                                    quantityText = preset.defaultQty.toString()
                                    selectedUnit = preset.unit
                                    selectedStore = preset.defaultStore
                                    selectedChannel = preset.defaultChannel
                                    selectedImageResName = preset.imageResName
                                    selectedImageUri = null
                                    aisleLocation = preset.defaultAisle
                                    barcode = preset.barcode
                                },
                                label = {
                                    Text(
                                        text = preset.title.take(18) + if (preset.title.length > 18) "..." else "",
                                        fontSize = 11.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Picture Selector & Preview
                    Text(
                        text = "PRODUCT PICTURE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Image Preview Box
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate100)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        ) {
                            if (selectedImageResName != null) {
                                val resId = context.resources.getIdentifier(
                                    selectedImageResName,
                                    "drawable",
                                    context.packageName
                                )
                                if (resId != 0) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            } else if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoCamera,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Photo", fontSize = 12.sp)
                            }
                            if (selectedImageUri != null || selectedImageResName != null) {
                                TextButton(
                                    onClick = {
                                        selectedImageUri = null
                                        selectedImageResName = null
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Clear image", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    // Small Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Small Title / Product Name *") },
                        placeholder = { Text("e.g. Kirkland Olive Oil 5L") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quantity and Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) quantityText = it },
                            label = { Text("Qty *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(0.45f)
                        )

                        // Unit selector chips/dropdown
                        Column(modifier = Modifier.weight(0.55f)) {
                            Text(
                                text = "Unit",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                units.forEach { unit ->
                                    FilterChip(
                                        selected = selectedUnit == unit,
                                        onClick = { selectedUnit = unit },
                                        label = { Text(unit, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Wholesaler / Shop Selector
                    Text(
                        text = "WHOLESALER / SHOP DESTINATION *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WholesalerDirectory.STORE_NAMES.forEach { store ->
                            FilterChip(
                                selected = selectedStore == store,
                                onClick = { selectedStore = store },
                                label = {
                                    Text(
                                        text = store,
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedStore == store) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Sales Channel Selector
                    Text(
                        text = "SALES CHANNEL *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        channels.forEach { channel ->
                            FilterChip(
                                selected = selectedChannel == channel,
                                onClick = { selectedChannel = channel },
                                label = { Text(channel, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Target Team Assignment
                    Text(
                        text = "ASSIGN TO TEAM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TeamDirectory.TEAMS.forEach { team ->
                            FilterChip(
                                selected = selectedTeam == team,
                                onClick = { selectedTeam = team },
                                label = { Text(team, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Aisle Location & Barcode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = aisleLocation,
                            onValueChange = { aisleLocation = it },
                            label = { Text("Aisle / Bay") },
                            placeholder = { Text("Aisle 14, Pallet 02") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode / SKU") },
                            placeholder = { Text("50123456789") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Priority
                    Text(
                        text = "PRIORITY LEVEL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(PickingItem.PRIORITY_NORMAL, PickingItem.PRIORITY_HIGH, PickingItem.PRIORITY_RUSH).forEach { p ->
                            FilterChip(
                                selected = priority == p,
                                onClick = { priority = p },
                                label = {
                                    Text(
                                        text = when (p) {
                                            PickingItem.PRIORITY_RUSH -> "🔥 Rush"
                                            PickingItem.PRIORITY_HIGH -> "⚡ High"
                                            else -> "Normal"
                                        },
                                        fontSize = 11.sp
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Picker Instructions / Notes") },
                        placeholder = { Text("e.g. Check seal, handle with care") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.4f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val qty = quantityText.toIntOrNull() ?: 1
                            if (title.isNotBlank()) {
                                onSubmit(
                                    title,
                                    qty,
                                    selectedUnit,
                                    selectedStore,
                                    selectedChannel,
                                    selectedTeam,
                                    selectedImageUri,
                                    selectedImageResName,
                                    barcode,
                                    aisleLocation,
                                    priority,
                                    notes
                                )
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch Order", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
