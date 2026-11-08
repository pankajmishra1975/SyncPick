package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PickingItem::class,
        PickingAuditLog::class,
        UserEntity::class,
        OrderEntity::class,
        SubscriptionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PickingDatabase : RoomDatabase() {

    abstract fun pickingDao(): PickingDao
    abstract fun auditDao(): PickingAuditDao
    abstract fun userDao(): UserDao
    abstract fun orderDao(): OrderDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: PickingDatabase? = null

        fun getInstance(context: Context): PickingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PickingDatabase::class.java,
                    "store_picking_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial seed data on first database creation
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    populateInitialData(database)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(database: PickingDatabase) {
            val now = System.currentTimeMillis()
            val initialItems = listOf(
                PickingItem(
                    orderNumber = "AMZ-8841",
                    title = "Kirkland Signature Olive Oil 5L",
                    quantityRequired = 6,
                    quantityPicked = 0,
                    unit = "Tins",
                    sourceStore = "Costco Wholesale Ltd",
                    salesChannel = "Amazon",
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = "item_olive_oil_1787678888577",
                    barcode = "5012345678901",
                    aisleLocation = "Aisle 14, Pallet 02",
                    notes = "Check seal integrity. Handle with care.",
                    priority = PickingItem.PRIORITY_HIGH,
                    createdAt = now - 3600000
                ),
                PickingItem(
                    orderNumber = "WEB-9142",
                    title = "Lee Kum Kee Premium Soy Sauce 1.9L",
                    quantityRequired = 12,
                    quantityPicked = 0,
                    unit = "Bottles",
                    sourceStore = "Loon Fung",
                    salesChannel = "Webstore",
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = "item_soy_sauce_1787678907901",
                    barcode = "5098765432109",
                    aisleLocation = "Aisle 04, Row 12",
                    notes = "Fragile glass bottle pack. Secure packaging.",
                    priority = PickingItem.PRIORITY_RUSH,
                    createdAt = now - 3200000
                ),
                PickingItem(
                    orderNumber = "EB-5520",
                    title = "Chef's Larder Basmati Rice 20kg",
                    quantityRequired = 10,
                    quantityPicked = 0,
                    unit = "Sacks",
                    sourceStore = "Bookers Cash & Carry",
                    salesChannel = "eBay",
                    assignedTeam = "Team Alpha (North)",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = "item_basmati_rice_1787678922842",
                    barcode = "5023456789012",
                    aisleLocation = "Bay 08, Pallet 4",
                    notes = "Heavy sacks. Use flatbed warehouse trolley.",
                    priority = PickingItem.PRIORITY_NORMAL,
                    createdAt = now - 2800000
                ),
                PickingItem(
                    orderNumber = "AMZ-9915",
                    title = "Coca-Cola Original 24x330ml Can Pack",
                    quantityRequired = 20,
                    quantityPicked = 0,
                    unit = "Cases",
                    sourceStore = "Bestway Wholesale",
                    salesChannel = "Amazon",
                    assignedTeam = "Team Beta (South)",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = null,
                    barcode = "5449000000996",
                    aisleLocation = "Section 03, Shelf A",
                    notes = "Bulk canned drinks pallet.",
                    priority = PickingItem.PRIORITY_NORMAL,
                    createdAt = now - 2400000
                ),
                PickingItem(
                    orderNumber = "WEB-7734",
                    title = "PG Tips Catering Pyramid Bags 1100s",
                    quantityRequired = 8,
                    quantityPicked = 0,
                    unit = "Boxes",
                    sourceStore = "Bookers Cash & Carry",
                    salesChannel = "Webstore",
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = null,
                    barcode = "8712566089201",
                    aisleLocation = "Bay 11, Rack 02",
                    notes = "Catering carton pack for cafes.",
                    priority = PickingItem.PRIORITY_HIGH,
                    createdAt = now - 2000000
                ),
                PickingItem(
                    orderNumber = "AMZ-6602",
                    title = "Fairy Original Washing Up Liquid 5L",
                    quantityRequired = 15,
                    quantityPicked = 0,
                    unit = "Bottles",
                    sourceStore = "Costco Wholesale Ltd",
                    salesChannel = "Amazon",
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = null,
                    barcode = "8001090123456",
                    aisleLocation = "Aisle 08, Pallet 16",
                    notes = "Cleaning chemical bay.",
                    priority = PickingItem.PRIORITY_NORMAL,
                    createdAt = now - 1600000
                ),
                PickingItem(
                    orderNumber = "EB-8821",
                    title = "Heinz Tomato Ketchup Squeezy 570g (x10)",
                    quantityRequired = 10,
                    quantityPicked = 0,
                    unit = "Packs",
                    sourceStore = "Tesco's",
                    salesChannel = "eBay",
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = null,
                    barcode = "5000157024671",
                    aisleLocation = "Aisle 06, Mod 03",
                    notes = "Condiments shelf.",
                    priority = PickingItem.PRIORITY_NORMAL,
                    createdAt = now - 1200000
                ),
                PickingItem(
                    orderNumber = "WEB-3390",
                    title = "Cadbury Dairy Milk 110g (Box of 24)",
                    quantityRequired = 14,
                    quantityPicked = 0,
                    unit = "Boxes",
                    sourceStore = "Sainsbury's",
                    salesChannel = "Webstore",
                    assignedTeam = "All Teams",
                    status = PickingItem.STATUS_PENDING,
                    imageResName = null,
                    barcode = "7622210286741",
                    aisleLocation = "Aisle 09, Shelf 04",
                    notes = "Keep out of direct sunlight.",
                    priority = PickingItem.PRIORITY_RUSH,
                    createdAt = now - 800000
                ),
                // Pre-populate some already picked items to show history and stats right away
                PickingItem(
                    orderNumber = "AMZ-4412",
                    title = "Nescafe Gold Blend Coffee 750g Tin",
                    quantityRequired = 8,
                    quantityPicked = 8,
                    unit = "Tins",
                    sourceStore = "Costco Wholesale Ltd",
                    salesChannel = "Amazon",
                    assignedTeam = "Team Alpha (North)",
                    status = PickingItem.STATUS_PICKED,
                    claimedByPickerId = "p_alex",
                    claimedByPickerName = "Alex Morgan",
                    pickedByPickerId = "p_alex",
                    pickedByPickerName = "Alex Morgan",
                    imageResName = null,
                    barcode = "7613035882901",
                    aisleLocation = "Aisle 02, Pallet 09",
                    notes = "Fully picked and packed.",
                    priority = PickingItem.PRIORITY_NORMAL,
                    createdAt = now - 7200000,
                    claimedAt = now - 7000000,
                    pickedAt = now - 6800000
                ),
                PickingItem(
                    orderNumber = "EB-3310",
                    title = "Sriracha Hot Chilli Sauce 455ml (x6)",
                    quantityRequired = 6,
                    quantityPicked = 6,
                    unit = "Packs",
                    sourceStore = "Loon Fung",
                    salesChannel = "eBay",
                    assignedTeam = "Team Beta (South)",
                    status = PickingItem.STATUS_PICKED,
                    claimedByPickerId = "p_sarah",
                    claimedByPickerName = "Sarah Chen",
                    pickedByPickerId = "p_sarah",
                    pickedByPickerName = "Sarah Chen",
                    imageResName = null,
                    barcode = "8853662056214",
                    aisleLocation = "Aisle 05, Row 03",
                    notes = "Checked expiry date 2027.",
                    priority = PickingItem.PRIORITY_HIGH,
                    createdAt = now - 6200000,
                    claimedAt = now - 6000000,
                    pickedAt = now - 5800000
                )
            )

            database.pickingDao().insertAll(initialItems)

            // Initial logs
            val initialLogs = listOf(
                PickingAuditLog(
                    itemId = 9,
                    orderNumber = "AMZ-4412",
                    itemTitle = "Nescafe Gold Blend Coffee 750g Tin",
                    pickerId = "p_alex",
                    pickerName = "Alex Morgan",
                    teamName = "Team Alpha (North)",
                    sourceStore = "Costco Wholesale Ltd",
                    salesChannel = "Amazon",
                    actionType = "PICKED",
                    quantity = 8,
                    timestamp = now - 6800000,
                    durationSeconds = 120,
                    note = "Picked complete order"
                ),
                PickingAuditLog(
                    itemId = 10,
                    orderNumber = "EB-3310",
                    itemTitle = "Sriracha Hot Chilli Sauce 455ml (x6)",
                    pickerId = "p_sarah",
                    pickerName = "Sarah Chen",
                    teamName = "Team Beta (South)",
                    sourceStore = "Loon Fung",
                    salesChannel = "eBay",
                    actionType = "PICKED",
                    quantity = 6,
                    timestamp = now - 5800000,
                    durationSeconds = 95,
                    note = "Picked complete order"
                )
            )
            database.auditDao().insertAll(initialLogs)

            // Seed initial users for offline-first authentication
            val allDirectoryProfiles = TeamDirectory.PICKERS + TeamDirectory.ADMINS + TeamDirectory.SUPER_ADMINS
            val initialUsers = allDirectoryProfiles.map { UserEntity.fromPickerProfile(it) }
            database.userDao().insertAllUsers(initialUsers)

            // Seed initial orders for offline-first order tracking
            val initialOrders = listOf(
                OrderEntity(
                    orderNumber = "AMZ-8841",
                    salesChannel = "Amazon",
                    customerName = "Apex Wholesale Logistics Ltd.",
                    destinationCity = "London, UK",
                    sourceStore = "Costco Wholesale Ltd",
                    assignedTeam = "All Teams",
                    status = OrderEntity.STATUS_PENDING,
                    itemCount = 1,
                    totalUnitsRequired = 6,
                    totalUnitsPicked = 0,
                    priority = OrderEntity.PRIORITY_HIGH,
                    notes = "Express Amazon pallet fulfillment. Handle olive oil tins with care."
                ),
                OrderEntity(
                    orderNumber = "WEB-9142",
                    salesChannel = "Webstore",
                    customerName = "Direct Asian Food Mart",
                    destinationCity = "Manchester, UK",
                    sourceStore = "Loon Fung",
                    assignedTeam = "All Teams",
                    status = OrderEntity.STATUS_PENDING,
                    itemCount = 1,
                    totalUnitsRequired = 12,
                    totalUnitsPicked = 0,
                    priority = OrderEntity.PRIORITY_RUSH,
                    notes = "Fragile glass bottle pack. Use bubblewrap."
                ),
                OrderEntity(
                    orderNumber = "EB-5520",
                    salesChannel = "eBay",
                    customerName = "Catering Supplies Express",
                    destinationCity = "Leeds, UK",
                    sourceStore = "Bookers Cash & Carry",
                    assignedTeam = "Team Alpha (North)",
                    status = OrderEntity.STATUS_PENDING,
                    itemCount = 1,
                    totalUnitsRequired = 10,
                    totalUnitsPicked = 0,
                    priority = OrderEntity.PRIORITY_NORMAL,
                    notes = "Heavy sacks of Basmati rice. Use warehouse flatbed trolley."
                ),
                OrderEntity(
                    orderNumber = "AMZ-9915",
                    salesChannel = "Amazon",
                    customerName = "Midland Superstore Depot",
                    destinationCity = "Birmingham, UK",
                    sourceStore = "Bestway Wholesale",
                    assignedTeam = "Team Beta (South)",
                    status = OrderEntity.STATUS_PENDING,
                    itemCount = 1,
                    totalUnitsRequired = 20,
                    totalUnitsPicked = 0,
                    priority = OrderEntity.PRIORITY_NORMAL,
                    notes = "Bulk canned soft drinks cases."
                )
            )
            database.orderDao().insertAllOrders(initialOrders)

            // Seed initial subscription status locally for offline-first validation
            val initialSubscription = SubscriptionEntity(
                id = "active_subscription",
                subscriberId = "SUB-84920",
                subscriberName = "Apex Wholesale Logistics Ltd.",
                planLevel = "FREEMIUM",
                planName = "Public Freemium Starter",
                planDescription = "Entry-level cloud fulfillment for small merchant wholesalers with offline local Room persistence.",
                billingCycle = "MONTHLY",
                isFreeSubscription = true,
                trialStatus = "ACTIVE_TRIAL",
                trialDaysRemaining = 30,
                maxItemsQuota = 250,
                maxManagersQuota = 2,
                maxPickersQuota = 4,
                monthlyPriceGbp = 0.0,
                allowCameraUse = true,
                allowPdfUploads = true,
                allowApiSync = true,
                allowLiveSimulation = true,
                allowAdvancedKpiExport = true,
                allowBarcodeScanning = true,
                supportTier = "STANDARD"
            )
            database.subscriptionDao().insertSubscription(initialSubscription)
        }
    }
}
