package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SalesChannelDirectory
import com.example.data.model.WholesalerDirectory
import com.example.ui.theme.*

@Composable
fun StoreBadge(
    storeName: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, shortTitle) = when {
        storeName.contains("Costco", ignoreCase = true) -> Triple(CostcoRed, Color.White, "Costco")
        storeName.contains("Bookers", ignoreCase = true) -> Triple(BookersGreen, Color.White, "Bookers")
        storeName.contains("Bestway", ignoreCase = true) -> Triple(BestwayOrange, Color.White, "Bestway")
        storeName.contains("Loon", ignoreCase = true) || storeName.contains("Fung", ignoreCase = true) -> Triple(LoonFungGold, Color.White, "Loon Fung")
        storeName.contains("Sainsbury", ignoreCase = true) -> Triple(SainsburysOrange, Color.White, "Sainsbury's")
        storeName.contains("Tesco", ignoreCase = true) -> Triple(TescoBlue, Color.White, "Tesco")
        else -> Triple(Slate700, Color.White, storeName.take(10))
    }

    Surface(
        color = bgColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(bgColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = shortTitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = bgColor
            )
        }
    }
}

@Composable
fun ChannelBadge(
    channelName: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (channelName.lowercase()) {
        "amazon" -> Pair(AmazonOrange, Color(0xFF1E293B))
        "ebay" -> Pair(Color(0xFF0064D2), Color.White)
        "webstore" -> Pair(WebstoreEmerald, Color.White)
        else -> Pair(Slate600, Color.White)
    }

    Surface(
        color = bgColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = channelName.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (channelName.equals("amazon", ignoreCase = true)) Color(0xFFD97706) else bgColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp),
            letterSpacing = 0.5.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WholesalerChipRow(
    selectedStore: String,
    onSelectStore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allOptions = listOf("All Stores") + WholesalerDirectory.STORE_NAMES

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        allOptions.forEach { store ->
            val isSelected = selectedStore == store
            val label = if (store == "All Stores") "All Stores" else {
                when {
                    store.contains("Costco") -> "Costco"
                    store.contains("Bookers") -> "Bookers"
                    store.contains("Bestway") -> "Bestway"
                    store.contains("Loon Fung") -> "Loon Fung"
                    store.contains("Sainsbury") -> "Sainsbury's"
                    store.contains("Tesco") -> "Tesco's"
                    else -> store
                }
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelectStore(store) },
                label = {
                    Text(
                        text = label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = if (store != "All Stores") {
                    {
                        val dotColor = when {
                            store.contains("Costco") -> CostcoRed
                            store.contains("Bookers") -> BookersGreen
                            store.contains("Bestway") -> BestwayOrange
                            store.contains("Loon Fung") -> LoonFungGold
                            store.contains("Sainsbury") -> SainsburysOrange
                            store.contains("Tesco") -> TescoBlue
                            else -> Slate600
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(dotColor)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelChipRow(
    selectedChannel: String,
    onSelectChannel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SalesChannelDirectory.CHANNELS.forEach { channel ->
            val isSelected = selectedChannel == channel
            FilterChip(
                selected = isSelected,
                onClick = { onSelectChannel(channel) },
                label = {
                    Text(
                        text = channel,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Slate800,
                    selectedLabelColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
