package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.PickingItem
import com.example.data.model.PresetCatalog
import com.example.data.model.WholesalerDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class ApiSyncResult(
    val success: Boolean,
    val sourceLabel: String,
    val importedItems: List<PickingItem>,
    val statusMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)

class OrderApiIngestionService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    /**
     * Fetches orders from a live REST API endpoint (GET returning JSON array or object with "orders" array)
     * using optional BuildConfig.ORDER_SYNC_API_KEY authorization header.
     */
    suspend fun fetchOrdersFromRestEndpoint(endpointUrl: String): ApiSyncResult = withContext(Dispatchers.IO) {
        try {
            val cleanUrl = endpointUrl.trim()
            if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
                return@withContext ApiSyncResult(
                    success = false,
                    sourceLabel = "REST API",
                    importedItems = emptyList(),
                    statusMessage = "Invalid endpoint URL. Must start with https:// or http://"
                )
            }

            val apiKey = BuildConfig.ORDER_SYNC_API_KEY
            val requestBuilder = Request.Builder()
                .url(cleanUrl)
                .header("Accept", "application/json")

            if (apiKey.isNotBlank() && apiKey != "ORDER_SYNC_API_KEY_DEFAULT_VALUE") {
                requestBuilder.header("Authorization", "Bearer $apiKey")
                requestBuilder.header("X-API-Key", apiKey)
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val bodyString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext ApiSyncResult(
                    success = false,
                    sourceLabel = cleanUrl,
                    importedItems = emptyList(),
                    statusMessage = "HTTP ${response.code}: ${response.message}"
                )
            }

            val parsed = parseJsonPayload(bodyString, sourceTag = "REST API ($cleanUrl)")
            if (parsed.isEmpty()) {
                return@withContext ApiSyncResult(
                    success = false,
                    sourceLabel = cleanUrl,
                    importedItems = emptyList(),
                    statusMessage = "Connected to endpoint (HTTP 200), but no valid order items were found in response."
                )
            }

            ApiSyncResult(
                success = true,
                sourceLabel = "REST API Endpoint",
                importedItems = parsed,
                statusMessage = "Synced ${parsed.size} orders from $cleanUrl"
            )
        } catch (e: Exception) {
            ApiSyncResult(
                success = false,
                sourceLabel = "REST API",
                importedItems = emptyList(),
                statusMessage = "Network / Parse Error: ${e.localizedMessage ?: "Failed to connect"}"
            )
        }
    }

    /**
     * Parses a raw JSON payload (either a JSONArray of orders or a JSONObject containing "orders" / "items")
     * or a flexible generic JSON list (e.g. dummyjson / custom webhook payload) and auto-routes each SKU
     * to the appropriate wholesaler and team.
     */
    fun parseJsonPayload(rawJson: String, sourceTag: String = "JSON Webhook"): List<PickingItem> {
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return emptyList()

        val items = mutableListOf<PickingItem>()
        val now = System.currentTimeMillis()

        val jsonArray: JSONArray = when {
            trimmed.startsWith("[") -> JSONArray(trimmed)
            trimmed.startsWith("{") -> {
                val obj = JSONObject(trimmed)
                when {
                    obj.has("orders") -> obj.getJSONArray("orders")
                    obj.has("items") -> obj.getJSONArray("items")
                    obj.has("products") -> obj.getJSONArray("products")
                    obj.has("data") -> obj.getJSONArray("data")
                    else -> JSONArray().put(obj)
                }
            }
            else -> return emptyList()
        }

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val title = obj.optString("title")
                .ifBlank { obj.optString("product_name") }
                .ifBlank { obj.optString("name") }
                .ifBlank { "Order Item #${i + 1}" }

            val sku = obj.optString("sku")
                .ifBlank { obj.optString("barcode") }
                .ifBlank { "50${Random.nextLong(10000000000L, 99999999999L)}" }

            val qty = obj.optInt("quantity", obj.optInt("qty", Random.nextInt(4, 16))).coerceAtLeast(1)
            val unit = obj.optString("unit", autoDetectUnit(title))
            val channel = normalizeChannel(obj.optString("channel", obj.optString("salesChannel", "Amazon")))
            val explicitStore = obj.optString("store", obj.optString("sourceStore", ""))
            val routedStore = if (explicitStore.isNotBlank()) normalizeStore(explicitStore) else autoRouteStoreByTitleOrSku(title, sku)

            val orderNumber = obj.optString("orderNumber")
                .ifBlank { obj.optString("order_id") }
                .ifBlank {
                    val prefix = when (channel) {
                        "Amazon" -> "AMZ"
                        "eBay" -> "EB"
                        else -> "WEB"
                    }
                    "$prefix-${Random.nextInt(1000, 9999)}"
                }

            val priority = normalizePriority(obj.optString("priority", "NORMAL"))
            val team = obj.optString("assignedTeam", obj.optString("team", autoRouteTeamByStore(routedStore)))
            val aisle = obj.optString("aisleLocation", obj.optString("aisle", defaultAisleForStore(routedStore)))
            val notes = obj.optString("notes", "Synced via $sourceTag")
            val matchedPreset = PresetCatalog.PRESETS.firstOrNull {
                title.contains(it.title.take(10), ignoreCase = true) || it.barcode == sku
            }

            items.add(
                PickingItem(
                    orderNumber = orderNumber,
                    title = title,
                    quantityRequired = qty,
                    quantityPicked = 0,
                    unit = unit,
                    sourceStore = routedStore,
                    salesChannel = channel,
                    assignedTeam = team,
                    status = PickingItem.STATUS_PENDING,
                    imageResName = matchedPreset?.imageResName,
                    barcode = sku,
                    aisleLocation = aisle,
                    notes = notes,
                    priority = priority,
                    createdAt = now - (i * 1000L)
                )
            )
        }
        return items
    }

    /**
     * Parses CSV lines into PickingItem list.
     * Expected columns (header optional):
     * OrderNumber, Title, Quantity, Unit, Store, Channel, Team, Priority, Barcode, Aisle
     */
    fun parseCsvPayload(rawCsv: String): List<PickingItem> {
        val lines = rawCsv.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<PickingItem>()
        val now = System.currentTimeMillis()

        lines.forEachIndexed { index, line ->
            // Skip header row if present
            if (index == 0 && (line.lowercase().contains("title") || line.lowercase().contains("order"))) {
                return@forEachIndexed
            }

            val cols = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (cols.size >= 2) {
                val hasOrderCol = cols[0].contains("-") || cols[0].startsWith("#")
                val orderNumRaw = if (hasOrderCol) cols[0].removePrefix("#") else ""
                val title = if (hasOrderCol) cols.getOrElse(1) { "Bulk Item" } else cols[0]
                val qty = (if (hasOrderCol) cols.getOrElse(2) { "6" } else cols.getOrElse(1) { "6" }).toIntOrNull() ?: 6
                val unit = (if (hasOrderCol) cols.getOrElse(3) { "" } else cols.getOrElse(2) { "" }).ifBlank { autoDetectUnit(title) }
                val rawStore = if (hasOrderCol) cols.getOrElse(4) { "" } else cols.getOrElse(3) { "" }
                val rawChannel = if (hasOrderCol) cols.getOrElse(5) { "Amazon" } else cols.getOrElse(4) { "Amazon" }
                val rawTeam = if (hasOrderCol) cols.getOrElse(6) { "" } else cols.getOrElse(5) { "" }
                val rawPriority = if (hasOrderCol) cols.getOrElse(7) { "NORMAL" } else cols.getOrElse(6) { "NORMAL" }
                val barcode = (if (hasOrderCol) cols.getOrElse(8) { "" } else cols.getOrElse(7) { "" })
                    .ifBlank { "50${Random.nextLong(10000000000L, 99999999999L)}" }

                val channel = normalizeChannel(rawChannel)
                val store = if (rawStore.isNotBlank()) normalizeStore(rawStore) else autoRouteStoreByTitleOrSku(title, barcode)
                val team = rawTeam.ifBlank { autoRouteTeamByStore(store) }
                val orderNumber = orderNumRaw.ifBlank {
                    val prefix = when (channel) {
                        "Amazon" -> "AMZ"
                        "eBay" -> "EB"
                        else -> "WEB"
                    }
                    "$prefix-${Random.nextInt(1000, 9999)}"
                }
                val matchedPreset = PresetCatalog.PRESETS.firstOrNull {
                    title.contains(it.title.take(10), ignoreCase = true)
                }

                result.add(
                    PickingItem(
                        orderNumber = orderNumber,
                        title = title,
                        quantityRequired = qty.coerceAtLeast(1),
                        quantityPicked = 0,
                        unit = unit,
                        sourceStore = store,
                        salesChannel = channel,
                        assignedTeam = team,
                        status = PickingItem.STATUS_PENDING,
                        imageResName = matchedPreset?.imageResName,
                        barcode = barcode,
                        aisleLocation = defaultAisleForStore(store),
                        notes = "Imported via Manager CSV Bulk Upload",
                        priority = normalizePriority(rawPriority),
                        createdAt = now - (index * 500L)
                    )
                )
            }
        }
        return result
    }

    /**
     * Generates realistic channel-specific API webhook payloads (Amazon SP-API, eBay Fulfillment API, Shopify/Webstore Webhook)
     * so Managers can test channel integrations immediately or inspect sample JSON/CSV templates.
     */
    fun generateChannelWebhookPayload(channel: String): String {
        return when (channel.lowercase()) {
            "amazon" -> """
                {
                  "orders": [
                    {
                      "orderNumber": "AMZ-${Random.nextInt(2000, 8999)}",
                      "title": "Kirkland Signature Extra Virgin Olive Oil 5L",
                      "sku": "5012345678901",
                      "quantity": 8,
                      "unit": "Tins",
                      "store": "Costco Wholesale Ltd",
                      "channel": "Amazon",
                      "team": "Team Alpha (North)",
                      "priority": "RUSH",
                      "aisle": "Aisle 14, Pallet 02",
                      "notes": "Amazon Prime Same-Day Dispatch"
                    },
                    {
                      "orderNumber": "AMZ-${Random.nextInt(2000, 8999)}",
                      "title": "Coca-Cola Original 24x330ml Can Pack",
                      "sku": "5449000000996",
                      "quantity": 15,
                      "unit": "Cases",
                      "store": "Bestway Wholesale",
                      "channel": "Amazon",
                      "team": "Team Beta (South)",
                      "priority": "HIGH",
                      "aisle": "Section 03, Shelf A",
                      "notes": "Amazon FBM Multi-Case Order"
                    }
                  ]
                }
            """.trimIndent()

            "ebay" -> """
                {
                  "orders": [
                    {
                      "orderNumber": "EB-${Random.nextInt(2000, 8999)}",
                      "title": "Chef's Larder Premium Basmati Rice 20kg",
                      "sku": "5023456789012",
                      "quantity": 10,
                      "unit": "Sacks",
                      "store": "Bookers Cash & Carry",
                      "channel": "eBay",
                      "team": "Team Alpha (North)",
                      "priority": "HIGH",
                      "aisle": "Bay 08, Pallet 4",
                      "notes": "eBay Fast & Free Next Day"
                    },
                    {
                      "orderNumber": "EB-${Random.nextInt(2000, 8999)}",
                      "title": "Heinz Tomato Ketchup Squeezy 570g (x10)",
                      "sku": "5000157024671",
                      "quantity": 12,
                      "unit": "Packs",
                      "store": "Tesco's",
                      "channel": "eBay",
                      "team": "All Teams",
                      "priority": "NORMAL",
                      "aisle": "Aisle 06, Mod 03",
                      "notes": "eBay Catering Bundle"
                    }
                  ]
                }
            """.trimIndent()

            else -> """
                {
                  "orders": [
                    {
                      "orderNumber": "WEB-${Random.nextInt(2000, 8999)}",
                      "title": "Lee Kum Kee Premium Soy Sauce 1.9L",
                      "sku": "5098765432109",
                      "quantity": 14,
                      "unit": "Bottles",
                      "store": "Loon Fung",
                      "channel": "Webstore",
                      "team": "Team Central",
                      "priority": "RUSH",
                      "aisle": "Aisle 04, Row 12",
                      "notes": "Webstore Restaurant Express Order"
                    },
                    {
                      "orderNumber": "WEB-${Random.nextInt(2000, 8999)}",
                      "title": "Cadbury Dairy Milk 110g (Box of 24)",
                      "sku": "7622210286741",
                      "quantity": 16,
                      "unit": "Boxes",
                      "store": "Sainsbury's",
                      "channel": "Webstore",
                      "team": "All Teams",
                      "priority": "NORMAL",
                      "aisle": "Aisle 09, Shelf 04",
                      "notes": "Webstore Direct Wholesale"
                    }
                  ]
                }
            """.trimIndent()
        }
    }

    fun getSampleCsvTemplate(): String {
        return """
            OrderNumber,Title,Quantity,Unit,Store,Channel,Team,Priority,Barcode
            AMZ-7104,Kirkland Olive Oil 5L,6,Tins,Costco Wholesale Ltd,Amazon,Team Alpha (North),RUSH,5012345678901
            EB-8209,Chef's Larder Basmati Rice 20kg,12,Sacks,Bookers Cash & Carry,eBay,Team Beta (South),HIGH,5023456789012
            WEB-9315,Lee Kum Kee Soy Sauce 1.9L,10,Bottles,Loon Fung,Webstore,Team Central,NORMAL,5098765432109
            AMZ-6420,Red Bull Energy Drink 24x250ml,18,Cases,Bestway Wholesale,Amazon,All Teams,HIGH,9002490100070
        """.trimIndent()
    }

    private fun autoRouteStoreByTitleOrSku(title: String, sku: String): String {
        val lower = title.lowercase()
        return when {
            lower.contains("kirkland") || lower.contains("olive oil") || lower.contains("fairy") || lower.contains("nescafe") -> "Costco Wholesale Ltd"
            lower.contains("lee kum kee") || lower.contains("soy") || lower.contains("sriracha") || lower.contains("noodle") || lower.contains("oriental") -> "Loon Fung"
            lower.contains("chef's larder") || lower.contains("basmati") || lower.contains("pg tips") || lower.contains("mayonnaise") -> "Bookers Cash & Carry"
            lower.contains("coca-cola") || lower.contains("red bull") || lower.contains("pepsi") || lower.contains("monster") -> "Bestway Wholesale"
            lower.contains("cadbury") || lower.contains("innocent") || lower.contains("sainsbury") -> "Sainsbury's"
            lower.contains("heinz") || lower.contains("lurpak") || lower.contains("tesco") -> "Tesco's"
            else -> WholesalerDirectory.STORE_NAMES.random()
        }
    }

    private fun autoRouteTeamByStore(store: String): String {
        return when {
            store.contains("Costco", ignoreCase = true) || store.contains("Bookers", ignoreCase = true) -> "Team Alpha (North)"
            store.contains("Bestway", ignoreCase = true) || store.contains("Tesco", ignoreCase = true) -> "Team Beta (South)"
            store.contains("Loon Fung", ignoreCase = true) -> "Team Central"
            else -> "All Teams"
        }
    }

    private fun autoDetectUnit(title: String): String {
        val lower = title.lowercase()
        return when {
            lower.contains("5l") || lower.contains("tin") -> "Tins"
            lower.contains("20kg") || lower.contains("sack") || lower.contains("rice") -> "Sacks"
            lower.contains("24x") || lower.contains("can") || lower.contains("case") -> "Cases"
            lower.contains("bottle") || lower.contains("1.9l") || lower.contains("liquid") -> "Bottles"
            lower.contains("box") || lower.contains("1100s") -> "Boxes"
            else -> "Units"
        }
    }

    private fun defaultAisleForStore(store: String): String {
        return when {
            store.contains("Costco", ignoreCase = true) -> "Aisle ${Random.nextInt(1, 18)}, Pallet 0${Random.nextInt(1, 9)}"
            store.contains("Bookers", ignoreCase = true) -> "Bay 0${Random.nextInt(1, 9)}, Rack 0${Random.nextInt(1, 6)}"
            store.contains("Bestway", ignoreCase = true) -> "Section 0${Random.nextInt(1, 8)}, Shelf B"
            store.contains("Loon Fung", ignoreCase = true) -> "Aisle 0${Random.nextInt(1, 6)}, Row ${Random.nextInt(1, 14)}"
            else -> "Aisle 0${Random.nextInt(1, 12)}, Mod 0${Random.nextInt(1, 5)}"
        }
    }

    private fun normalizeChannel(raw: String): String {
        return when {
            raw.contains("amazon", ignoreCase = true) || raw.equals("amz", ignoreCase = true) -> "Amazon"
            raw.contains("ebay", ignoreCase = true) || raw.equals("eb", ignoreCase = true) -> "eBay"
            else -> "Webstore"
        }
    }

    private fun normalizeStore(raw: String): String {
        return when {
            raw.contains("costco", ignoreCase = true) -> "Costco Wholesale Ltd"
            raw.contains("booker", ignoreCase = true) -> "Bookers Cash & Carry"
            raw.contains("bestway", ignoreCase = true) -> "Bestway Wholesale"
            raw.contains("loon", ignoreCase = true) || raw.contains("fung", ignoreCase = true) -> "Loon Fung"
            raw.contains("sainsbury", ignoreCase = true) -> "Sainsbury's"
            raw.contains("tesco", ignoreCase = true) -> "Tesco's"
            else -> raw
        }
    }

    private fun normalizePriority(raw: String): String {
        return when {
            raw.contains("rush", ignoreCase = true) || raw.contains("urgent", ignoreCase = true) -> PickingItem.PRIORITY_RUSH
            raw.contains("high", ignoreCase = true) -> PickingItem.PRIORITY_HIGH
            else -> PickingItem.PRIORITY_NORMAL
        }
    }
}
