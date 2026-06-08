package com.example.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class Repository(private val database: AppDatabase) {
    private val dao = database.appDao()

    // Expose data streams reactively
    val accountsFlow: Flow<List<AccountMaster>> = dao.getAllAccountsFlow()
    val productsFlow: Flow<List<ProductMaster>> = dao.getAllProductsFlow()
    val invoicesFlow: Flow<List<Invoice>> = dao.getAllInvoicesFlow()
    val returnsFlow: Flow<List<SalesReturn>> = dao.getAllReturnsFlow()
    val collectionsFlow: Flow<List<FieldCollection>> = dao.getAllCollectionsFlow()
    val ledgerFlow: Flow<List<StockLedger>> = dao.getAllLedgerEntriesFlow()
    val syncLogsFlow: Flow<List<SyncLog>> = dao.getAllSyncLogsFlow()

    // Fetch lists
    suspend fun getAllAccounts(): List<AccountMaster> = dao.getAllAccounts()
    suspend fun getAllProducts(): List<ProductMaster> = dao.getAllProducts()
    suspend fun getProductByQr(qrCode: String): ProductMaster? = dao.getProductByQrCode(qrCode)

    suspend fun getItemsForInvoice(invoiceId: String): List<InvoiceItem> = dao.getItemsForInvoice(invoiceId)
    suspend fun getItemsForReturn(returnId: String): List<SalesReturnItem> = dao.getItemsForReturn(returnId)

    // Clear sync logs helper
    suspend fun clearSyncLogs() = dao.clearSyncLogs()

    // ==========================================
    // SEEDING AND INITIALIZATION
    // ==========================================
    suspend fun seedMockDataIfEmpty() {
        database.withTransaction {
            val accounts = dao.getAllAccounts()
            if (accounts.isEmpty()) {
                // Seed Accounts
                val accountSeeds = listOf(
                    AccountMaster(
                        accountName = "Aman Sports & Co.",
                        phoneNumber = "+91 98765 43210",
                        billingAddress = "102, Sports Market, Jalandhar, Punjab",
                        openingBalance = 15200.0
                    ),
                    AccountMaster(
                        accountName = "Delhi Cricket Academy",
                        phoneNumber = "+91 99991 11122",
                        billingAddress = "F-Block, Saket Crossing, New Delhi",
                        openingBalance = -2300.0
                    ),
                    AccountMaster(
                        accountName = "Aryan Athletics Academy",
                        phoneNumber = "+91 88882 22233",
                        billingAddress = "Sports Hub Complex, Meerut, UP",
                        openingBalance = 45000.0
                    ),
                    AccountMaster(
                        accountName = "Super Soccer Wholesalers",
                        phoneNumber = "+91 77773 33344",
                        billingAddress = "Lane 4, Sector 15B, Rohini, Delhi",
                        openingBalance = 0.0
                    ),
                    AccountMaster(
                        accountName = "Janvi Premium Club Dealer",
                        phoneNumber = "+91 91114 44455",
                        billingAddress = "Janvi Sports Building, Central Mall, Gurgaon",
                        openingBalance = 8750.0
                    )
                )
                accountSeeds.forEach { dao.insertAccount(it) }

                // Seed Products
                val productSeeds = listOf(
                    ProductMaster(
                        qrCodePayload = "QR-JSPROTSHIRT-750",
                        productName = "Janvi Sports Pro T-Shirt",
                        skuCode = "JS-TS-PRO-M",
                        standardPrice = 750.0,
                        currentStock = 120
                    ),
                    ProductMaster(
                        qrCodePayload = "QR-VECTORLEAGUE-350",
                        productName = "Vector Premium League Leather Ball",
                        skuCode = "JS-CB-LEAGUE-R",
                        standardPrice = 350.0,
                        currentStock = 250
                    ),
                    ProductMaster(
                        qrCodePayload = "QR-AEROPRORACKET-1850",
                        productName = "Aero Titanium Pro Badminton Racket",
                        skuCode = "JS-BR-AEPRO-S",
                        standardPrice = 1850.0,
                        currentStock = 45
                    ),
                    ProductMaster(
                        qrCodePayload = "QR-GOLDFOOTBALL-1200",
                        productName = "Gold Match Series Professional Football",
                        skuCode = "JS-FB-GOLDMS-5",
                        standardPrice = 1200.0,
                        currentStock = 85
                    ),
                    ProductMaster(
                        qrCodePayload = "QR-ELITESLEEVES-220",
                        productName = "Elite Compression Fitness Sleeves",
                        skuCode = "JS-SL-ELITE-C",
                        standardPrice = 220.0,
                        currentStock = 300
                    )
                )
                productSeeds.forEach {
                    dao.insertProduct(it)
                    // Write initial stock ledger
                    dao.insertLedgerEntry(
                        StockLedger(
                            productId = it.productId,
                            transactionType = "INWARD",
                            referenceId = null,
                            quantityChanged = it.currentStock,
                            runningStockBalance = it.currentStock,
                            deviceId = "SYSTEM-INIT"
                        )
                    )
                }

                // Add initial Sync action
                dao.insertSyncLog(
                    SyncLog(
                        deviceId = "SYSTEM-INIT",
                        eventDescription = "Database initial seeding completes. 5 Accounts & 5 Products master registries logged.",
                        deltaChange = "SEED",
                        targetRecord = "ALL"
                    )
                )
            }
        }
    }

    // ==========================================
    // MODULE A: QR-DRIVEN INVENTORY CONTROL
    // ==========================================
    suspend fun adjustInventoryViaQR(
        qrCode: String,
        actionType: String, // "ADD" or "REMOVE"
        quantity: Int,
        deviceId: String
    ): Result<ProductMaster> {
        return runCatching {
            database.withTransaction {
                val product = dao.getProductByQrCode(qrCode) 
                    ?: throw IllegalArgumentException("Product not found with scanned QR Code payload: $qrCode")

                var newStock = product.currentStock
                if (actionType == "ADD") {
                    newStock += quantity
                } else if (actionType == "REMOVE") {
                    if (product.currentStock < quantity) {
                        throw IllegalStateException("Underflow Stock Exception: Scanned quantity ($quantity) exceeds available stock ($newStock)")
                    }
                    newStock -= quantity
                }

                // Update product stock count
                val updatedProduct = product.copy(currentStock = newStock)
                dao.updateProduct(updatedProduct)

                // Write immutable stock ledger trace
                val ledger = StockLedger(
                    productId = product.productId,
                    transactionType = if (actionType == "ADD") "INWARD" else "MANUAL_OUT",
                    referenceId = null,
                    quantityChanged = if (actionType == "ADD") quantity else -quantity,
                    runningStockBalance = newStock,
                    deviceId = deviceId
                )
                dao.insertLedgerEntry(ledger)

                // Trigger Simulated Network Broadcast
                val description = if (actionType == "ADD") {
                    "Dispatched inward ledger adjustment: Added $quantity units to ${product.productName} via QR scan."
                } else {
                    "Dispatched manual adjustment: Deducted $quantity units from ${product.productName} via QR scan."
                }
                dao.insertSyncLog(
                    SyncLog(
                        deviceId = deviceId,
                        eventDescription = description,
                        deltaChange = if (actionType == "ADD") "+$quantity" else "-$quantity",
                        targetRecord = product.productId
                    )
                )

                updatedProduct
            }
        }
    }

    // ==========================================
    // MODULE B: HYBRID DIGITAL BILLING TERMINAL
    // ==========================================
    suspend fun executeInvoice(
        accountId: String,
        selectedProducts: List<Triple<ProductMaster, Int, Double>>, // Product, Quantity, CustomUnitPrice
        deviceId: String
    ): Result<Invoice> {
        return runCatching {
            database.withTransaction {
                if (selectedProducts.isEmpty()) {
                    throw IllegalArgumentException("Invoice line items array is empty.")
                }

                val account = dao.getAccountById(accountId)
                    ?: throw IllegalArgumentException("Selected account does not exist.")

                // Calculate numeric totals securely
                var totalAmount = 0.0
                val invoiceItems = mutableListOf<InvoiceItem>()
                val productUpdates = mutableListOf<ProductMaster>()
                val ledgerEntries = mutableListOf<StockLedger>()

                val invoiceId = UUID.randomUUID().toString()

                for (triple in selectedProducts) {
                    val prod = dao.getProductById(triple.first.productId)
                        ?: throw IllegalArgumentException("Product ${triple.first.productName} missing in ledger.")

                    val qty = triple.second
                    val customPrice = triple.third
                    val lineTotal = qty * customPrice

                    if (prod.currentStock < qty) {
                        throw IllegalStateException("Insufficient stock for product ${prod.productName}: Requested $qty, available ${prod.currentStock}")
                    }

                    totalAmount += lineTotal

                    // Build line item
                    invoiceItems.add(
                        InvoiceItem(
                            invoiceId = invoiceId,
                            productId = prod.productId,
                            quantity = qty,
                            unitPrice = customPrice,
                            lineTotal = lineTotal
                        )
                    )

                    // Compute stock mutation
                    val newStock = prod.currentStock - qty
                    productUpdates.add(prod.copy(currentStock = newStock))

                    // Build Stock ledger record
                    ledgerEntries.add(
                        StockLedger(
                            productId = prod.productId,
                            transactionType = "INVOICE_OUT",
                            referenceId = invoiceId,
                            quantityChanged = -qty,
                            runningStockBalance = newStock,
                            deviceId = deviceId
                        )
                    )
                }

                // Generate new serial Invoice ID / number safely
                val maxInvNo = dao.getMaxInvoiceNumber()
                val nextInvNo = maxInvNo + 1

                val invoice = Invoice(
                    invoiceId = invoiceId,
                    invoiceNumber = nextInvNo,
                    accountId = accountId,
                    totalAmount = totalAmount,
                    invoiceDate = System.currentTimeMillis()
                )

                // Write header
                dao.insertInvoice(invoice)

                // Write line items
                dao.insertInvoiceItems(invoiceItems)

                // Perform ACID inventory reductions & write ledger log rows
                productUpdates.forEach { dao.updateProduct(it) }
                ledgerEntries.forEach { dao.insertLedgerEntry(it) }

                // Broadcast Simulated WebSocket Sync Packet
                dao.insertSyncLog(
                    SyncLog(
                        deviceId = deviceId,
                        eventDescription = "Broadcast: Invoice #$nextInvNo generated for ${account.accountName}. Invoice total: ₹$totalAmount. Inventory modified and locked.",
                        deltaChange = "INVOICE_CREATED",
                        targetRecord = invoiceId
                    )
                )

                invoice
            }
        }
    }

    // ==========================================
    // MODULE C: SALES RETURN MECHANISM
    // ==========================================
    suspend fun executeSalesReturn(
        accountId: String,
        returnedProducts: List<Triple<ProductMaster, Int, Double>>, // Product, Quantity, CreditUnitPrice
        deviceId: String
    ): Result<SalesReturn> {
        return runCatching {
            database.withTransaction {
                if (returnedProducts.isEmpty()) {
                    throw IllegalArgumentException("Returned items list cannot be empty.")
                }

                val account = dao.getAccountById(accountId)
                    ?: throw IllegalArgumentException("Selected account does not exist.")

                var totalCreditAmount = 0.0
                val returnItems = mutableListOf<SalesReturnItem>()
                val productUpdates = mutableListOf<ProductMaster>()
                val ledgerEntries = mutableListOf<StockLedger>()

                val returnId = UUID.randomUUID().toString()

                for (triple in returnedProducts) {
                    val prod = dao.getProductById(triple.first.productId)
                        ?: throw IllegalArgumentException("Product ${triple.first.productName} does not exist.")

                    val qty = triple.second
                    val creditPrice = triple.third
                    val lineTotal = qty * creditPrice

                    totalCreditAmount += lineTotal

                    // Build sales return row
                    returnItems.add(
                        SalesReturnItem(
                            returnId = returnId,
                            productId = prod.productId,
                            quantity = qty,
                            creditUnitPrice = creditPrice,
                            lineCreditTotal = lineTotal
                        )
                    )

                    // Reverse stock update: Increment stock since item is returned
                    val newStock = prod.currentStock + qty
                    productUpdates.add(prod.copy(currentStock = newStock))

                    // Ledger tracking timeline INJECTION
                    ledgerEntries.add(
                        StockLedger(
                            productId = prod.productId,
                            transactionType = "RETURN_IN",
                            referenceId = returnId,
                            quantityChanged = qty,
                            runningStockBalance = newStock,
                            deviceId = deviceId
                        )
                    )
                }

                val maxReturnNo = dao.getMaxReturnNumber()
                val nextReturnNo = maxReturnNo + 1

                val salesReturn = SalesReturn(
                    returnId = returnId,
                    returnNumber = nextReturnNo,
                    accountId = accountId,
                    totalCreditAmount = totalCreditAmount,
                    returnDate = System.currentTimeMillis()
                )

                // Write return records
                dao.insertReturn(salesReturn)
                dao.insertReturnItems(returnItems)

                // Write stock modifications and ledger rows
                productUpdates.forEach { dao.updateProduct(it) }
                ledgerEntries.forEach { dao.insertLedgerEntry(it) }

                // Web socket sync notification simulation
                dao.insertSyncLog(
                    SyncLog(
                        deviceId = deviceId,
                        eventDescription = "Broadcast: Sales Return Record #$nextReturnNo verified. Credited ₹$totalCreditAmount to ${account.accountName}. Inventory replenished.",
                        deltaChange = "RETURN_ACCEPTED",
                        targetRecord = returnId
                    )
                )

                salesReturn
            }
        }
    }

    // ==========================================
    // MODULE D: FIELD COLLECTION APPLICATION
    // ==========================================
    suspend fun executeFieldCollection(
        accountId: String,
        amount: Double,
        status: String,
        dueDate: String?,
        notes: String,
        deviceId: String
    ): Result<FieldCollection> {
        return runCatching {
            database.withTransaction {
                val account = dao.getAccountById(accountId)
                    ?: throw IllegalArgumentException("Account does not exist.")

                if (amount <= 0) {
                    throw IllegalArgumentException("Collected amount must be positive.")
                }

                val collection = FieldCollection(
                    accountId = accountId,
                    amountCollected = amount,
                    collectionStatus = status,
                    dueDate = dueDate,
                    notes = notes,
                    deviceId = deviceId,
                    collectionDate = System.currentTimeMillis()
                )

                dao.insertCollection(collection)

                // Broadcast Simulated WebSocket Sync Packet immediately
                dao.insertSyncLog(
                    SyncLog(
                        deviceId = deviceId,
                        eventDescription = "Broadcast: LEDGER_MODIFIED for ${account.accountName}. Field Agent recovery collected value ₹$amount. Status: $status.",
                        deltaChange = "LEDGER_MODIFIED",
                        targetRecord = accountId
                    )
                )

                collection
            }
        }
    }

    // Add manual account or product for admin control
    suspend fun insertAccount(account: AccountMaster) {
        dao.insertAccount(account)
        dao.insertSyncLog(
            SyncLog(
                deviceId = "ADMIN",
                eventDescription = "New Account registered in central system: ${account.accountName}",
                deltaChange = "ACCOUNT_CREATED",
                targetRecord = account.accountId
            )
        )
    }

    suspend fun insertProduct(product: ProductMaster) {
        dao.insertProduct(product)
        // Add initial inward ledger entry
        dao.insertLedgerEntry(
            StockLedger(
                productId = product.productId,
                transactionType = "INWARD",
                referenceId = null,
                quantityChanged = product.currentStock,
                runningStockBalance = product.currentStock,
                deviceId = "ADMIN"
            )
        )
        dao.insertSyncLog(
            SyncLog(
                deviceId = "ADMIN",
                eventDescription = "New SKU product catalog code recorded: ${product.productName}",
                deltaChange = "PRODUCT_CREATED",
                targetRecord = product.productId
            )
        )
    }
}
