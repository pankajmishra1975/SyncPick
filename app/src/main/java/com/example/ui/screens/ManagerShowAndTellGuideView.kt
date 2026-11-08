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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessEmerald
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerShowAndTellGuideView(
    onDismiss: () -> Unit,
    onOpenDeveloperConsole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(0) } // 0: Features Showcase, 1: Manager FAQ, 2: Tutorials, 3: Web Replicas
    var expandedFaqIndex by remember { mutableStateOf<Int?>(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
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
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryBlue.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Manager Knowledge Hub",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = PrimaryBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "SHOW & TELL",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = PrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "apps.sellershub.io/stockpick • Features, FAQ & Depot Tutorials",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_guide_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close Guide")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedSection,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = PrimaryBlue,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedSection == 0,
                        onClick = { selectedSection = 0 },
                        text = { Text("Show & Tell Features", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedSection == 1,
                        onClick = { selectedSection = 1 },
                        text = { Text("Manager FAQ (10)", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedSection == 2,
                        onClick = { selectedSection = 2 },
                        text = { Text("Depot Tutorials", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                    Tab(
                        selected = selectedSection == 3,
                        onClick = { selectedSection = 3 },
                        text = { Text("Web & Dev Login URL", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }
        }

        // --- Main Body ---
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (selectedSection) {
                0 -> {
                    // SECTION 0: SHOW & TELL FEATURES
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Public Show & Tell Information",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "This exact feature showcase is replicated on the public web portal at apps.sellershub.io/stockpick to demonstrate the system capabilities to clients and subscribers.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        FeatureShowcaseCard(
                            title = "1. Live Strike-Off Engine & Collision Prevention",
                            badge = "CORE PICKING",
                            badgeColor = PrimaryBlue,
                            icon = Icons.Default.CheckCircle,
                            description = "When a picker scans or picks an item in a warehouse aisle, it is struck off with animated feedback across all picker screens simultaneously. This prevents duplicate picking, reduces walking steps, and guarantees zero collision between team pickers.",
                            highlights = listOf(
                                "Instant strike-through visual feedback on item cards",
                                "Claim lock: other pickers see 'In Progress by Alex' in real-time",
                                "Haptic vibration and audio confirmation on pick completion"
                            )
                        )
                    }

                    item {
                        FeatureShowcaseCard(
                            title = "2. Multi-Store Cash & Carry Routing",
                            badge = "DEPOT LOGISTICS",
                            badgeColor = AccentTeal,
                            icon = Icons.Default.Storefront,
                            description = "Dedicated store filters optimize routes across major wholesale depots including Costco Wholesale Ltd, Bookers Cash & Carry, Bestway Wholesale, Loon Fung Asian Superstore, and supermarkets.",
                            highlights = listOf(
                                "Auto-sorts items by aisle, bay, and pallet location",
                                "Stores map with specific color badges and short-codes",
                                "Separate pallets for ambient, chilled, and heavy bulk sacks"
                            )
                        )
                    }

                    item {
                        FeatureShowcaseCard(
                            title = "3. Barcode Verification & Zero-Error Scanning",
                            badge = "QUALITY CONTROL",
                            badgeColor = SuccessEmerald,
                            icon = Icons.Default.QrCodeScanner,
                            description = "Pickers scan standard EAN-13, UPC, and code-128 barcodes using the device camera. The system confirms an exact match with the picklist before allowing strike-off, eliminating wrong-item dispatches.",
                            highlights = listOf(
                                "Instant visual match verification dialog",
                                "Auto-checks quantity increments per barcode scan",
                                "Fallback manual entry with manager audit trail"
                            )
                        )
                    }

                    item {
                        FeatureShowcaseCard(
                            title = "4. Field Reports & Camera Condition Evidence",
                            badge = "DAMAGE & PROMOS",
                            badgeColor = WarningAmber,
                            icon = Icons.Default.PhotoCamera,
                            description = "Pickers encounter damaged packaging, price increases, promo offers, or empty shelves. With one tap, they snap photo proof with condition tags and notes for managers to review instantly.",
                            highlights = listOf(
                                "Condition tags: Damaged, New Packaging, Price Increase, On Offer",
                                "Stores photo URI and picker timestamp in Room database",
                                "Option to mark Out of Stock or proceed with partial pick"
                            )
                        )
                    }

                    item {
                        FeatureShowcaseCard(
                            title = "5. Offline-First Room Database Architecture",
                            badge = "OFFLINE CAPABILITY",
                            badgeColor = Color(0xFF7C3AED),
                            icon = Icons.Default.Storage,
                            description = "Depot internet drops frequently inside metal-clad warehouse bays. The built-in Room SQLite database keeps Users, Orders, PickItems, and Subscription status permanently cached locally for offline-first operations.",
                            highlights = listOf(
                                "Users, Orders, and Subscriptions stored in Room SQLite",
                                "Zero latency when scanning barcodes or marking picks offline",
                                "Auto-syncs with cloud APIs when Wi-Fi connectivity recovers"
                            )
                        )
                    }

                    item {
                        FeatureShowcaseCard(
                            title = "6. PDF Picklists & Cloud API Sync",
                            badge = "AUTOMATION",
                            badgeColor = Color(0xFF0284C7),
                            icon = Icons.Default.CloudSync,
                            description = "Ingest daily dispatch batches automatically from Amazon Seller Central, eBay, and WooCommerce, or drop PDF invoices to auto-parse item quantities, SKUs, and delivery addresses in seconds.",
                            highlights = listOf(
                                "Regex-powered PDF line extraction for trade invoices",
                                "Sales channels split: Amazon, eBay, and Webstore",
                                "Batch dispatching with priority tagging (Rush, High, Normal)"
                            )
                        )
                    }
                }

                1 -> {
                    // SECTION 1: MANAGER FAQ
                    item {
                        Text(
                            text = "FREQUENTLY ASKED QUESTIONS FOR DEPOT MANAGERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    val faqs = listOf(
                        FaqItem(
                            question = "1. How does offline-first mode work when pickers lose Wi-Fi in warehouse aisles?",
                            answer = "The app utilizes a local Room SQLite database running on the Android device. All PickItems, Orders, User rosters, and Subscription quotas are cached locally. Pickers can continue scanning barcodes, completing strike-offs, and logging field reports with zero lag. When network connectivity is restored, events sync back automatically."
                        ),
                        FaqItem(
                            question = "2. How do freemium quotas work and when are pickers throttled?",
                            answer = "Subscribers on the Public Freemium plan have a quota of 250 items processed per month, 4 pickers, and 2 managers. When a depot reaches 85% of its monthly limit (212 items), the manager receives a warning banner. At 100% (250 items), scanning requires an upgrade to Wholesale Pro (£39/mo) or a free trial extension by a Super Admin."
                        ),
                        FaqItem(
                            question = "3. Can multiple pickers work on the same order simultaneously without collision?",
                            answer = "Yes! PickItems are claimed individually. When Alex claims the 5L Olive Oil tin on Order AMZ-8841, Sarah sees it marked 'In Progress by Alex'. When Alex finishes picking, the strike-off animation triggers across all connected screens in real-time."
                        ),
                        FaqItem(
                            question = "4. How do I assign orders to specific teams (e.g. Team Alpha vs Team Beta)?",
                            answer = "When creating an order or parsing a PDF, select the Assigned Team dropdown (e.g., Team Alpha North or Team Beta South). Pickers filter their queue to show only items assigned to their team, keeping depot zones neatly separated."
                        ),
                        FaqItem(
                            question = "5. How are damaged or out-of-stock items reported by pickers?",
                            answer = "Pickers tap the 'Field Report' button on any item card. They can snap a camera photo of the damaged package or shelf tag, select condition tags (Damaged Packaging, Short Expiry, Moved Aisle), add comments, and choose whether to mark it Out of Stock or proceed with a partial pick."
                        ),
                        FaqItem(
                            question = "6. How do managers upgrade subscriptions or redeem voucher promo codes?",
                            answer = "Go to the 'Portfolio' tab in the navigation bar. Under 'Subscription & Licensing', select any plan tier (Wholesale Pro or Enterprise Multi-Depot), toggle Monthly or Annual billing (20% discount), and enter voucher codes like 'SUPER50' (50% off) or 'FREEMIUM_PRO' (100% free)."
                        ),
                        FaqItem(
                            question = "7. How does the PDF picklist parser extract order items?",
                            answer = "In the 'Order & API' tab, tap 'Upload PDF Picklist'. The app scans invoice headers, item descriptions, quantities, and customer details, converting them into structured PickItems ready for floor assignment with one tap."
                        ),
                        FaqItem(
                            question = "8. What happens when an item is marked Out of Stock?",
                            answer = "The item is moved from the active picking queue to the 'Audit History' tab under 'Out of Stock' status with the picker's notes and photo evidence. Managers can redispatch the item to an alternative wholesaler store when stock arrives."
                        ),
                        FaqItem(
                            question = "9. Where do developers and super admins manage the whole system?",
                            answer = "Developers and Super Admins manage all subscribers, billing, invoices, quotas, and feature flags through the dedicated, fully secured web portal at apps.sellershub.io/stockpick/manage. The main URL apps.sellershub.io/stockpick is reserved for show & tell features, FAQ, and tutorials."
                        ),
                        FaqItem(
                            question = "10. How is privacy protected on the login screen?",
                            answer = "The login screen features clean logo branding and two distinct pathways: Manager/Admin Login and Picker Login. Individual employee names and rosters are never displayed on the screen. Pickers enter their Badge ID (e.g., PK-101) and PIN, while managers use their Admin Code (ADM-01)."
                        )
                    )

                    items(faqs.indices.toList()) { index ->
                        val faq = faqs[index]
                        val isExpanded = expandedFaqIndex == index
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedFaqIndex = if (isExpanded) null else index }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = faq.question,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 10.dp)) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = faq.answer,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // SECTION 2: DEPOT TUTORIALS
                    item {
                        Text(
                            text = "STEP-BY-STEP DEPOT MANAGER TUTORIALS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    item {
                        TutorialStepCard(
                            stepNumber = "Tutorial 1",
                            title = "Onboarding Pickers & Assigning Teams",
                            summary = "How to configure your depot workforce for multi-zone picking.",
                            steps = listOf(
                                "Provide each floor picker with their 5-digit Employee Badge (e.g. PK-101) and 4-digit PIN (1111).",
                                "Assign pickers to specific warehouse zones (Team Alpha North for ambient, Team Beta South for drinks & bulk).",
                                "Pickers log in with their Badge ID on their handheld terminal without exposing other members' names.",
                                "Their terminal defaults to their team's assigned queue for zero distraction."
                            )
                        )
                    }

                    item {
                        TutorialStepCard(
                            stepNumber = "Tutorial 2",
                            title = "Creating & Ingesting Orders via API or PDF",
                            summary = "How to feed daily fulfillment orders into the live picking queue.",
                            steps = listOf(
                                "Navigate to Tab 1: 'Order & API'.",
                                "For manual order entry: Tap 'Dispatch Order' and enter Store, Channel, Item Title, Quantity, and Priority.",
                                "For automated PDF entry: Tap 'Upload PDF Picklist' and choose your trade invoice. The system extracts rows into PickItems.",
                                "For REST API Sync: Tap 'Sync Orders via API' to pull orders from your cloud store endpoints."
                            )
                        )
                    }

                    item {
                        TutorialStepCard(
                            stepNumber = "Tutorial 3",
                            title = "Monitoring Real-Time Team KPIs & Speed",
                            summary = "How to supervise floor speed and spot bottlenecks.",
                            steps = listOf(
                                "Navigate to Tab 2: 'Team KPIs'.",
                                "Review the Picker Leaderboard showing items picked, total units, and average speed (seconds per item).",
                                "Monitor channel completion rates (Amazon, eBay, Webstore) to hit carrier cut-off times.",
                                "Review Wholesaler Store completion meters to see which cash & carry pallets are ready for loading."
                            )
                        )
                    }

                    item {
                        TutorialStepCard(
                            stepNumber = "Tutorial 4",
                            title = "Managing Subscriptions & Voucher Redemptions",
                            summary = "How to keep your depot within quotas and unlock enterprise features.",
                            steps = listOf(
                                "Navigate to Tab 4: 'Portfolio'.",
                                "Check your monthly items processed progress bar against your plan quota.",
                                "To upgrade from Freemium, tap 'Select Wholesale Pro' or 'Enterprise Multi-Depot'.",
                                "Enter promotional vouchers (e.g. SUPER50 for 50% discount) to apply immediate credits."
                            )
                        )
                    }
                }

                3 -> {
                    // SECTION 3: WEBPAGE SPLIT & DEVELOPER MANAGEMENT URL
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, PrimaryBlue)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Public, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "apps.sellershub.io/stockpick",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = "Public Show & Tell Features, FAQ & Tutorials Portal",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "This public web page is dedicated strictly to Show & Tell features, manager FAQs, and training tutorials for clients and onboarding depots.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.sellershub.io/stockpick"))
                                        runCatching { context.startActivity(intent) }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Showcase Webpage (apps.sellershub.io/stockpick)")
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.5.dp, Color(0xFF7C3AED))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "apps.sellershub.io/stockpick/manage",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF7C3AED)
                                        )
                                        Text(
                                            text = "Dedicated Secured Developer & Super Admin Login Portal",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "To manage all subscribers, payment history, tax invoices, freemium tier quotas, and super admin overrides, developers log in securely at apps.sellershub.io/stockpick/manage with full 2FA & passcode protection.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onOpenDeveloperConsole,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open In-App Console", fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://apps.sellershub.io/stockpick/manage"))
                                            runCatching { context.startActivity(intent) }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Web Manage URL", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class FaqItem(val question: String, val answer: String)

@Composable
private fun FeatureShowcaseCard(
    title: String,
    badge: String,
    badgeColor: Color,
    icon: ImageVector,
    description: String,
    highlights: List<String>
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                highlights.forEach { highlight ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(badgeColor))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = highlight, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialStepCard(
    stepNumber: String,
    title: String,
    summary: String,
    steps: List<String>
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = PrimaryBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stepNumber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = summary, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                steps.forEachIndexed { idx, stp ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${idx + 1}.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.width(18.dp)
                        )
                        Text(
                            text = stp,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
