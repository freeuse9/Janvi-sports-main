package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = Repository(database)

    // ==========================================
    // GLOBAL APP STATE & DEVICE SIMULATOR
    // ==========================================
    val devices = listOf(
        "LAPTOP-01 (Admin Billing)",
        "TABLET-02 (Factory Floor)",
        "MOBILE-03 (Floor Scanner)",
        "MOBILE-04 (Field Collector)",
        "LAPTOP-05 (Management Portal)"
    )
    val selectedDeviceId = MutableStateFlow(devices[0])
    val currentTab = MutableStateFlow(0) // 0: Scanner, 1: Billing, 2: Return, 3: Collections, 4: Admin, 5: Live Sync Sync Broker

    // ==========================================
    // REACTIVE FLOW DATA STREAMS (FROM REPO)
    // ==========================================
    val accounts = repository.accountsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val products = repository.productsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val invoices = repository.invoicesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val returns = repository.returnsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val collections = repository.collectionsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val stockLedger = repository.ledgerFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val syncLogs = repository.syncLogsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // UI TOASTS & ALERTS (FEEDBACK MECHANISM)
    // ==========================================
    private val _uiMessage = MutableSharedFlow<Pair<String, Boolean>>() // Msg, IsSuccess
    val uiMessage = _uiMessage.asSharedFlow()

    fun showToast(msg: String, isSuccess: Boolean = true) {
        viewModelScope.launch {
            _uiMessage.emit(Pair(msg, isSuccess))
        }
    }

    // ==========================================
    // INITIALIZATION BLOCK
    // ==========================================
    init {
        viewModelScope.launch {
            repository.seedMockDataIfEmpty()
        }
    }

    // ==========================================
    // MODULE A: QR CONTROL STATE
    // ==========================================
    val qrQuantityInput = MutableStateFlow("10")
    val qrActionType = MutableStateFlow("ADD") // "ADD" or "REMOVE"

    fun adjustInventoryScan(qrCode: String) {
        viewModelScope.launch {
            val qty = qrQuantityInput.value.toIntOrNull() ?: 1
            val device = selectedDeviceId.value.split(" ")[0]
            val type = qrActionType.value
            repository.adjustInventoryViaQR(qrCode, type, qty, device)
                .onSuccess { prod ->
                    showToast("Secured stock update for ${prod.productName}. New total: ${prod.currentStock}", true)
                }
                .onFailure { err ->
                    showToast(err.message ?: "Inventory adjustment failed.", false)
                }
        }
    }

    // ==========================================
    // MODULE B: HYBRID DIGITAL BILLING STATE
    // ==========================================
    val billingAccountSelectionId = MutableStateFlow("")
    // Triple: Product, quantity, Price (including override)
    val billingCart = MutableStateFlow<List<Triple<ProductMaster, Int, Double>>>(emptyList())

    fun selectBillingAccount(id: String) {
        billingAccountSelectionId.value = id
    }

    fun addProductToBillingCart(product: ProductMaster, qty: Int = 1) {
        val current = billingCart.value.toMutableList()
        val index = current.indexOfFirst { it.first.productId == product.productId }
        if (index >= 0) {
            val existing = current[index]
            val newQty = existing.second + qty
            current[index] = Triple(product, newQty, existing.third)
        } else {
            current.add(Triple(product, qty, product.standardPrice))
        }
        billingCart.value = current
    }

    fun updateCartItemQuantity(productId: String, qty: Int) {
        if (qty <= 0) {
            removeCartItemInBilling(productId)
            return
        }
        billingCart.value = billingCart.value.map {
            if (it.first.productId == productId) Triple(it.first, qty, it.third) else it
        }
    }

    fun updateCartItemPriceOverride(productId: String, overriddenPrice: Double) {
        billingCart.value = billingCart.value.map {
            if (it.first.productId == productId) Triple(it.first, it.second, overriddenPrice) else it
        }
    }

    fun removeCartItemInBilling(productId: String) {
        billingCart.value = billingCart.value.filterNot { it.first.productId == productId }
    }

    fun submitBillingCart() {
        val accountId = billingAccountSelectionId.value
        val items = billingCart.value
        val device = selectedDeviceId.value.split(" ")[0]

        if (accountId.isEmpty()) {
            showToast("Transaction Aborted: Select customer account.", false)
            return
        }
        if (items.isEmpty()) {
            showToast("Transaction Aborted: Billing item list array is empty.", false)
            return
        }

        viewModelScope.launch {
            repository.executeInvoice(accountId, items, device)
                .onSuccess { invoice ->
                    showToast("Dispatched Invoice #${invoice.invoiceNumber}. Total value: ₹${invoice.totalAmount}", true)
                    // Clear cart
                    billingCart.value = emptyList()
                }
                .onFailure { err ->
                    showToast("ACID Locked Update Failed: " + (err.message ?: "Insufficient stock"), false)
                }
        }
    }

    // ==========================================
    // MODULE C: SALES RETURN STATE
    // ==========================================
    val returnAccountSelectionId = MutableStateFlow("")
    val returnCart = MutableStateFlow<List<Triple<ProductMaster, Int, Double>>>(emptyList())

    fun selectReturnAccount(id: String) {
        returnAccountSelectionId.value = id
    }

    fun addProductToReturnCart(product: ProductMaster, qty: Int = 1) {
        val current = returnCart.value.toMutableList()
        val index = current.indexOfFirst { it.first.productId == product.productId }
        if (index >= 0) {
            val existing = current[index]
            val newQty = existing.second + qty
            current[index] = Triple(product, newQty, existing.third)
        } else {
            current.add(Triple(product, qty, product.standardPrice))
        }
        returnCart.value = current
    }

    fun updateReturnQuantity(productId: String, qty: Int) {
        if (qty <= 0) {
            removeProductFromReturnCart(productId)
            return
        }
        returnCart.value = returnCart.value.map {
            if (it.first.productId == productId) Triple(it.first, qty, it.third) else it
        }
    }

    fun updateReturnCreditPrice(productId: String, price: Double) {
        returnCart.value = returnCart.value.map {
            if (it.first.productId == productId) Triple(it.first, it.second, price) else it
        }
    }

    fun removeProductFromReturnCart(productId: String) {
        returnCart.value = returnCart.value.filterNot { it.first.productId == productId }
    }

    fun submitSalesReturn() {
        val accountId = returnAccountSelectionId.value
        val items = returnCart.value
        val device = selectedDeviceId.value.split(" ")[0]

        if (accountId.isEmpty()) {
            showToast("Return Tracing Aborted: Assign customer directory profiling.", false)
            return
        }
        if (items.isEmpty()) {
            showToast("Return Tracing Aborted: Items card list is empty.", false)
            return
        }

        viewModelScope.launch {
            repository.executeSalesReturn(accountId, items, device)
                .onSuccess { ret ->
                    showToast("Sales Return #${ret.returnNumber} finalized. Account Credited: ₹${ret.totalCreditAmount}", true)
                    returnCart.value = emptyList()
                }
                .onFailure { err ->
                    showToast("Rejection processing error: " + (err.message ?: "Failed"), false)
                }
        }
    }

    // ==========================================
    // MODULE D: FIELD COLLECTIONS STATE
    // ==========================================
    val colAccountSelectionId = MutableStateFlow("")
    val colAmountInput = MutableStateFlow("")
    val colNotesInput = MutableStateFlow("")
    val colStatusSelection = MutableStateFlow("Success") // "Success", "Pending"
    val colDueDateInput = MutableStateFlow("")

    fun submitFieldCollection() {
        val accountId = colAccountSelectionId.value
        val amount = colAmountInput.value.toDoubleOrNull() ?: 0.0
        val notes = colNotesInput.value
        val status = colStatusSelection.value
        val due = colDueDateInput.value
        val device = selectedDeviceId.value.split(" ")[0]

        if (accountId.isEmpty()) {
            showToast("Voucher Aborted: Please select targeted customer profile.", false)
            return
        }
        if (amount <= 0.0) {
            showToast("Voucher Aborted: Collected numeric amount must exceed ₹0.00.", false)
            return
        }

        viewModelScope.launch {
            repository.executeFieldCollection(accountId, amount, status, due, notes, device)
                .onSuccess {
                    showToast("Field Recovery Voucher logged: Collected ₹$amount for client.", true)
                    // Reset field inputs
                    colAmountInput.value = ""
                    colNotesInput.value = ""
                    colDueDateInput.value = ""
                }
                .onFailure { err ->
                    showToast("Core Sync Processing issue occurred: " + (err.message ?: "Failed"), false)
                }
        }
    }

    // ==========================================
    // MASTER ADMIN CONTROLS: ADD NEW DIRECTORY entries
    // ==========================================
    val newAccountName = MutableStateFlow("")
    val newAccountPhone = MutableStateFlow("")
    val newAccountAddress = MutableStateFlow("")
    val newAccountBalance = MutableStateFlow("0.0")

    fun registerNewAccount() {
        val name = newAccountName.value.trim()
        val phone = newAccountPhone.value.trim()
        val addr = newAccountAddress.value.trim()
        val balance = newAccountBalance.value.toDoubleOrNull() ?: 0.0

        if (name.isEmpty()) {
            showToast("Directory generation failed: Name cannot be empty.", false)
            return
        }

        viewModelScope.launch {
            repository.insertAccount(
                AccountMaster(
                    accountName = name,
                    phoneNumber = phone,
                    billingAddress = addr,
                    openingBalance = balance
                )
            )
            showToast("Successfully registered profile for $name.", true)
            newAccountName.value = ""
            newAccountPhone.value = ""
            newAccountAddress.value = ""
            newAccountBalance.value = "0.0"
        }
    }

    val newProductName = MutableStateFlow("")
    val newProductSku = MutableStateFlow("")
    val newProductPrice = MutableStateFlow("500.0")
    val newProductStock = MutableStateFlow("50")
    val newProductQr = MutableStateFlow("")

    fun registerNewProduct() {
        val name = newProductName.value.trim()
        val sku = newProductSku.value.trim()
        val price = newProductPrice.value.toDoubleOrNull() ?: 0.0
        val stock = newProductStock.value.toIntOrNull() ?: 0
        var qr = newProductQr.value.trim()

        if (name.isEmpty() || sku.isEmpty()) {
            showToast("Catalog error: Name & Sku code are mandatory.", false)
            return
        }
        if (qr.isEmpty()) {
            qr = "QR-${sku.uppercase()}-${price.toInt()}"
        }

        viewModelScope.launch {
            repository.insertProduct(
                ProductMaster(
                    productName = name,
                    skuCode = sku,
                    standardPrice = price,
                    currentStock = stock,
                    qrCodePayload = qr
                )
            )
            showToast("Added $name to the master sports inventory directory catalog.", true)
            newProductName.value = ""
            newProductSku.value = ""
            newProductPrice.value = "500.0"
            newProductStock.value = "50"
            newProductQr.value = ""
        }
    }

    // ==========================================
    // PEER NETWORKS SYNC BROADCAST SIMULATION
    // ==========================================
    fun clearBrokerLogs() {
        viewModelScope.launch {
            repository.clearSyncLogs()
            showToast("Sync Broker message log queue flushed.", true)
        }
    }

    fun simulateOtherDeviceWrite() {
        // Simulates another device somewhere in the network committing a transaction that immediately refreshes our local view
        viewModelScope.launch {
            val otherDevices = devices.filterNot { it == selectedDeviceId.value }
            val chosenDevice = otherDevices.random().split(" ")[0]
            val clientProfiles = accounts.value
            val productCatalog = products.value

            if (clientProfiles.isEmpty() || productCatalog.isEmpty()) return@launch

            val isInvoice = Random().nextBoolean()
            if (isInvoice) {
                // Mock Invoicing on other device
                val randomClient = clientProfiles.random()
                val randomProduct = productCatalog.random()
                val qty = (1..5).random()
                val total = qty * randomProduct.standardPrice

                if (randomProduct.currentStock >= qty) {
                    val updatedStock = randomProduct.currentStock - qty
                    database.withTransaction {
                        repository.insertAccount(randomClient) // forces DB refresh/lock
                        database.appDao().updateProduct(randomProduct.copy(currentStock = updatedStock))
                        val referenceId = UUID.randomUUID().toString()
                        database.appDao().insertInvoice(
                            Invoice(
                                invoiceId = referenceId,
                                invoiceNumber = database.appDao().getMaxInvoiceNumber() + 1,
                                accountId = randomClient.accountId,
                                totalAmount = total
                            )
                        )
                        database.appDao().insertLedgerEntry(
                            StockLedger(
                                productId = randomProduct.productId,
                                transactionType = "INVOICE_OUT",
                                referenceId = referenceId,
                                quantityChanged = -qty,
                                runningStockBalance = updatedStock,
                                deviceId = chosenDevice
                            )
                        )
                        database.appDao().insertSyncLog(
                            SyncLog(
                                deviceId = chosenDevice,
                                eventDescription = "Remote Real-time Synchronized Inflow: Received WebSocket frame delta packet from [$chosenDevice] creating Invoice on parallel terminal node for ₹$total.",
                                deltaChange = "INVOICE_OUT",
                                targetRecord = referenceId
                            )
                        )
                    }
                    showToast("🔄 Synced: Remote terminal [$chosenDevice] posted transactional invoice update.", true)
                }
            } else {
                // Mock Stock Injection on tablet
                val randomProduct = productCatalog.random()
                val inputQty = (10..50).random()
                val updatedStock = randomProduct.currentStock + inputQty

                database.withTransaction {
                    database.appDao().updateProduct(randomProduct.copy(currentStock = updatedStock))
                    database.appDao().insertLedgerEntry(
                        StockLedger(
                            productId = randomProduct.productId,
                            transactionType = "INWARD",
                            referenceId = null,
                            quantityChanged = inputQty,
                            runningStockBalance = updatedStock,
                            deviceId = chosenDevice
                        )
                    )
                    database.appDao().insertSyncLog(
                        SyncLog(
                            deviceId = chosenDevice,
                            eventDescription = "Sync Packet Alert: Received WS Broadcaster inward log frame from [$chosenDevice] indicating factory production stock injection: Added +$inputQty units for ${randomProduct.productName}.",
                            deltaChange = "INWARD",
                            targetRecord = randomProduct.productId
                        )
                    )
                }
                showToast("🔄 Synced: Remote production terminal [$chosenDevice] uploaded live factory inward ledger.", true)
            }
        }
    }
}
