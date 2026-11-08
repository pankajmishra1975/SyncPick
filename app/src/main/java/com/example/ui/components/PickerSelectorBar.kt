package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.example.data.model.PickerProfile
import com.example.data.model.TeamDirectory
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerSelectorBar(
    activePicker: PickerProfile,
    onSelectPicker: (PickerProfile) -> Unit,
    isSimulationActive: Boolean,
    onToggleSimulation: (Boolean) -> Unit,
    isAdminMode: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPickerMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Active User / Admin Profile Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { showPickerMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(activePicker.color)
                    ) {
                        Text(
                            text = activePicker.avatarInitials,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activePicker.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                color = if (isAdminMode) PrimaryBlue else SuccessEmerald,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isAdminMode) "ADMIN" else "PICKER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${activePicker.employeeCode} • ${activePicker.teamName}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Controls: Live Sync Simulation & Switch Role / Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = isSimulationActive,
                        onClick = { onToggleSimulation(!isSimulationActive) },
                        label = {
                            Text(
                                text = if (isSimulationActive) "⚡ Live Sync ON" else "⚡ Sim Sync",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessEmerald,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(30.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Logout / Switch Role Button
                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sign Out", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Profile Switcher / Session Info Modal
            if (showPickerMenu) {
                val profilesForCurrentRole = if (isAdminMode) TeamDirectory.ADMINS else TeamDirectory.PICKERS
                AlertDialog(
                    onDismissRequest = { showPickerMenu = false },
                    title = {
                        Text(
                            text = if (isAdminMode) "Active Manager Session" else "Switch Team Picker Terminal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isAdminMode)
                                    "Signed in with Manager/Admin privileges. Switch between Admin accounts or sign out to log in as a Store Picker:"
                                else
                                    "Simulate another picker's handheld device on the shop floor, or sign out to access Manager/Admin login:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            profilesForCurrentRole.forEach { profile ->
                                val isSelected = profile.id == activePicker.id
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            onSelectPicker(profile)
                                            showPickerMenu = false
                                        }
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
                                                .background(profile.color)
                                        ) {
                                            Text(
                                                text = profile.avatarInitials,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${profile.name} (${profile.employeeCode})",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "${profile.role} • ${profile.teamName}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Active",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showPickerMenu = false
                                onLogout()
                            }
                        ) {
                            Text("Switch Role / Sign Out", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPickerMenu = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}
