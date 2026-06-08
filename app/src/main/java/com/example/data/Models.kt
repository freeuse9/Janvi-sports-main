package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "account_master")
data class AccountMaster(
    @PrimaryKey val accountId: String = UUID.randomUUID().toString(),
    val accountName: String,
    val phoneNumber: String,
    val billingAddress: String,
    val openingBalance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "product_master")
data class ProductMaster(
    @PrimaryKey val productId: String = UUID.randomUUID().toString(),
    val qrCodePayload: String, // String scanned with camera OR typed
    val productName: String,
    val skuCode: String,
    val standardPrice: Double = 0.0,
    val currentStock: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val invoiceId: String = UUID.randomUUID().toString(),
    val invoiceNumber: Int, // safe auto-increment counter computed in repository
    val accountId: String,
    val invoiceDate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey val invoiceItemId: String = UUID.randomUUID().toString(),
    val invoiceId: String,
    val productId: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

@Entity(tableName = "sales_returns")
data class SalesReturn(
    @PrimaryKey val returnId: String = UUID.randomUUID().toString(),
    val returnNumber: Int, // auto computed counter
    val accountId: String,
    val returnDate: Long = System.currentTimeMillis(),
    val totalCreditAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sales_return_items")
data class SalesReturnItem(
    @PrimaryKey val returnItemId: String = UUID.randomUUID().toString(),
    val returnId: String,
    val productId: String,
    val quantity: Int,
    val creditUnitPrice: Double,
    val lineCreditTotal: Double
)

@Entity(tableName = "field_collections")
data class FieldCollection(
    @PrimaryKey val collectionId: String = UUID.randomUUID().toString(),
    val accountId: String,
    val amountCollected: Double,
    val collectionStatus: String = "Success", // 'Success', 'Pending'
    val dueDate: String? = null,
    val notes: String = "",
    val deviceId: String,
    val collectionDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "stock_ledger")
data class StockLedger(
    @PrimaryKey val ledgerEntryId: String = UUID.randomUUID().toString(),
    val productId: String,
    val transactionType: String, // 'INWARD', 'INVOICE_OUT', 'RETURN_IN', 'MANUAL_OUT'
    val referenceId: String?, // Points to invoiceId, returnId, etc.
    val quantityChanged: Int, // Positive for inward, Negative for outward
    val runningStockBalance: Int,
    val deviceId: String,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_log")
data class SyncLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String,
    val eventDescription: String,
    val deltaChange: String,
    val targetRecord: String
)
