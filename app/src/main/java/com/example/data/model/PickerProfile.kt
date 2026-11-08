package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

data class PickerProfile(
    val id: String,
    val name: String,
    val teamName: String,
    val color: Color,
    val role: String = "Store Picker",
    val pinCode: String = "1234",
    val employeeCode: String = "PK-101",
    val isAdmin: Boolean = false,
    val isSuperAdmin: Boolean = false,
    val superAdminRole: SuperAdminRole? = null,
    val avatarInitials: String = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
)

object TeamDirectory {
    val TEAMS = listOf(
        "All Teams",
        "Team Alpha (North)",
        "Team Beta (South)",
        "Team Central",
        "Team Express"
    )

    val PICKERS = listOf(
        PickerProfile("p_alex", "Alex Morgan", "Team Alpha (North)", TeamAlphaBlue, "Lead Picker", pinCode = "1111", employeeCode = "PK-101"),
        PickerProfile("p_sarah", "Sarah Chen", "Team Beta (South)", TeamBetaPurple, "Fast Picker", pinCode = "2222", employeeCode = "PK-102"),
        PickerProfile("p_david", "David Miller", "Team Alpha (North)", TeamGammaAmber, "Store Picker", pinCode = "3333", employeeCode = "PK-103"),
        PickerProfile("p_priya", "Priya Patel", "Team Central", TeamDeltaTeal, "Senior Picker", pinCode = "4444", employeeCode = "PK-104"),
        PickerProfile("p_liam", "Liam Walker", "Team Beta (South)", TeamAlphaBlue, "Store Picker", pinCode = "5555", employeeCode = "PK-105")
    )

    val ADMINS = listOf(
        PickerProfile(
            id = "admin_marcus",
            name = "Marcus Vance",
            teamName = "All Teams",
            color = PrimaryBlue,
            role = "Operations Manager",
            pinCode = "9999",
            employeeCode = "ADM-01",
            isAdmin = true
        ),
        PickerProfile(
            id = "admin_elena",
            name = "Elena Rostova",
            teamName = "All Teams",
            color = AccentTeal,
            role = "Dispatch Director",
            pinCode = "8888",
            employeeCode = "ADM-02",
            isAdmin = true
        )
    )

    val SUPER_ADMINS = listOf(
        PickerProfile(
            id = "super_alex",
            name = "Alexander Vance",
            teamName = "Executive / Super Admin",
            color = Color(0xFF7C3AED),
            role = "Platform Owner",
            pinCode = "7777",
            employeeCode = "SUP-01",
            isAdmin = true,
            isSuperAdmin = true,
            superAdminRole = SuperAdminRole.PLATFORM_OWNER
        ),
        PickerProfile(
            id = "super_beatrice",
            name = "Beatrice Sterling",
            teamName = "Executive / Super Admin",
            color = Color(0xFF2563EB),
            role = "Billing & Subscription Director",
            pinCode = "7778",
            employeeCode = "SUP-02",
            isAdmin = true,
            isSuperAdmin = true,
            superAdminRole = SuperAdminRole.BILLING_DIRECTOR
        ),
        PickerProfile(
            id = "super_clara",
            name = "Clara Dupont",
            teamName = "Executive / Super Admin",
            color = Color(0xFF059669),
            role = "License & Voucher Operator",
            pinCode = "7779",
            employeeCode = "SUP-03",
            isAdmin = true,
            isSuperAdmin = true,
            superAdminRole = SuperAdminRole.LICENSE_OPERATOR
        ),
        PickerProfile(
            id = "super_devon",
            name = "Devon Miller",
            teamName = "Executive / Super Admin",
            color = Color(0xFFD97706),
            role = "Tier & Quota Auditor",
            pinCode = "7780",
            employeeCode = "SUP-04",
            isAdmin = true,
            isSuperAdmin = true,
            superAdminRole = SuperAdminRole.TIER_AUDITOR
        )
    )
}

data class WholesalerStoreInfo(
    val id: String,
    val name: String,
    val shortName: String,
    val color: Color,
    val type: String, // Cash & Carry, Superstore, Asian Wholesaler, Supermarket
    val defaultAisleFormat: String
)

object WholesalerDirectory {
    val STORES = listOf(
        WholesalerStoreInfo("costco", "Costco Wholesale Ltd", "Costco", CostcoRed, "Wholesale Club", "Aisle %s, Pallet %s"),
        WholesalerStoreInfo("bookers", "Bookers Cash & Carry", "Bookers", BookersGreen, "Cash & Carry", "Bay %s, Rack %s"),
        WholesalerStoreInfo("bestway", "Bestway Wholesale", "Bestway", BestwayOrange, "Wholesale Depot", "Section %s, Shelf %s"),
        WholesalerStoreInfo("loonfung", "Loon Fung", "Loon Fung", LoonFungGold, "Oriental Wholesaler", "Aisle %s, Row %s"),
        WholesalerStoreInfo("sainsburys", "Sainsbury's", "Sainsbury's", SainsburysOrange, "Supermarket", "Aisle %s"),
        WholesalerStoreInfo("tesco", "Tesco's", "Tesco", TescoBlue, "Supermarket", "Aisle %s, Mod %s")
    )

    val STORE_NAMES = STORES.map { it.name }
}

object SalesChannelDirectory {
    val CHANNELS = listOf(
        "All Channels",
        "Amazon",
        "eBay",
        "Webstore"
    )
}

data class ProductPreset(
    val title: String,
    val defaultQty: Int,
    val unit: String,
    val defaultStore: String,
    val defaultChannel: String,
    val imageResName: String?,
    val defaultAisle: String,
    val barcode: String
)

object PresetCatalog {
    val PRESETS = listOf(
        ProductPreset(
            title = "Kirkland Extra Virgin Olive Oil 5L",
            defaultQty = 6,
            unit = "Tins",
            defaultStore = "Costco Wholesale Ltd",
            defaultChannel = "Amazon",
            imageResName = "item_olive_oil_1787678888577",
            defaultAisle = "Aisle 14, Pallet 02",
            barcode = "5012345678901"
        ),
        ProductPreset(
            title = "Lee Kum Kee Premium Dark Soy Sauce 1.9L",
            defaultQty = 12,
            unit = "Bottles",
            defaultStore = "Loon Fung",
            defaultChannel = "Webstore",
            imageResName = "item_soy_sauce_1787678907901",
            defaultAisle = "Aisle 04, Row 12",
            barcode = "5098765432109"
        ),
        ProductPreset(
            title = "Chef's Larder Premium Basmati Rice 20kg",
            defaultQty = 10,
            unit = "Sacks",
            defaultStore = "Bookers Cash & Carry",
            defaultChannel = "eBay",
            imageResName = "item_basmati_rice_1787678922842",
            defaultAisle = "Bay 08, Pallet 4",
            barcode = "5023456789012"
        ),
        ProductPreset(
            title = "Coca-Cola Original 24x330ml Can Pack",
            defaultQty = 20,
            unit = "Cases",
            defaultStore = "Bestway Wholesale",
            defaultChannel = "Amazon",
            imageResName = null,
            defaultAisle = "Section 03, Shelf A",
            barcode = "5449000000996"
        ),
        ProductPreset(
            title = "PG Tips Pyramid Catering Tea Bags 1100s",
            defaultQty = 8,
            unit = "Boxes",
            defaultStore = "Bookers Cash & Carry",
            defaultChannel = "Webstore",
            imageResName = null,
            defaultAisle = "Bay 11, Rack 02",
            barcode = "8712566089201"
        ),
        ProductPreset(
            title = "Fairy Original Washing Up Liquid 5L",
            defaultQty = 15,
            unit = "Bottles",
            defaultStore = "Costco Wholesale Ltd",
            defaultChannel = "Amazon",
            imageResName = null,
            defaultAisle = "Aisle 08, Pallet 16",
            barcode = "8001090123456"
        ),
        ProductPreset(
            title = "Heinz Tomato Ketchup Squeezy 570g (Pack of 10)",
            defaultQty = 12,
            unit = "Packs",
            defaultStore = "Tesco's",
            defaultChannel = "eBay",
            imageResName = null,
            defaultAisle = "Aisle 06, Mod 03",
            barcode = "5000157024671"
        ),
        ProductPreset(
            title = "Cadbury Dairy Milk Chocolate Bar 110g (Box of 24)",
            defaultQty = 14,
            unit = "Boxes",
            defaultStore = "Sainsbury's",
            defaultChannel = "Webstore",
            imageResName = null,
            defaultAisle = "Aisle 09, Shelf 04",
            barcode = "7622210286741"
        )
    )
}
