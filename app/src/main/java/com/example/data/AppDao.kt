package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // ==========================================
    // 1. ACCOUNT MASTER ACTIONS
    // ==========================================
    @Query("SELECT * FROM account_master ORDER BY accountName ASC")
    fun getAllAccountsFlow(): Flow<List<AccountMaster>>

    @Query("SELECT * FROM account_master")
    suspend fun getAllAccounts(): List<AccountMaster>

    @Query("SELECT * FROM account_master WHERE accountId = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: String): AccountMaster?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountMaster)

    @Update
    suspend fun updateAccount(account: AccountMaster)

    // ==========================================
    // 2. PRODUCT MASTER ACTIONS
    // ==========================================
    @Query("SELECT * FROM product_master ORDER BY productName ASC")
    fun getAllProductsFlow(): Flow<List<ProductMaster>>

    @Query("SELECT * FROM product_master")
    suspend fun getAllProducts(): List<ProductMaster>

    @Query("SELECT * FROM product_master WHERE productId = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductMaster?

    @Query("SELECT * FROM product_master WHERE qrCodePayload = :qrCode LIMIT 1")
    suspend fun getProductByQrCode(qrCode: String): ProductMaster?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductMaster)

    @Update
    suspend fun updateProduct(product: ProductMaster)

    // ==========================================
    // 3. INVOICE ACTIONS
    // ==========================================
    @Query("SELECT * FROM invoices ORDER BY invoiceNumber DESC")
    fun getAllInvoicesFlow(): Flow<List<Invoice>>

    @Query("SELECT COALESCE(MAX(invoiceNumber), 0) FROM invoices")
    suspend fun getMaxInvoiceNumber(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoiceFlow(invoiceId: String): Flow<List<InvoiceItem>>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoice(invoiceId: String): List<InvoiceItem>

    // ==========================================
    // 4. SALES RETURN ACTIONS
    // ==========================================
    @Query("SELECT * FROM sales_returns ORDER BY returnNumber DESC")
    fun getAllReturnsFlow(): Flow<List<SalesReturn>>

    @Query("SELECT COALESCE(MAX(returnNumber), 0) FROM sales_returns")
    suspend fun getMaxReturnNumber(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(salesReturn: SalesReturn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturnItems(items: List<SalesReturnItem>)

    @Query("SELECT * FROM sales_return_items WHERE returnId = :returnId")
    fun getItemsForReturnFlow(returnId: String): Flow<List<SalesReturnItem>>

    @Query("SELECT * FROM sales_return_items WHERE returnId = :returnId")
    suspend fun getItemsForReturn(returnId: String): List<SalesReturnItem>

    // ==========================================
    // 5. FIELD COLLECTION ACTIONS
    // ==========================================
    @Query("SELECT * FROM field_collections ORDER BY collectionDate DESC")
    fun getAllCollectionsFlow(): Flow<List<FieldCollection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: FieldCollection)

    // ==========================================
    // 6. STOCK LEDGER LOG ACTIONS
    // ==========================================
    @Query("SELECT * FROM stock_ledger ORDER BY recordedAt DESC")
    fun getAllLedgerEntriesFlow(): Flow<List<StockLedger>>

    @Query("SELECT * FROM stock_ledger ORDER BY recordedAt DESC")
    suspend fun getAllLedgerEntries(): List<StockLedger>

    @Query("SELECT * FROM stock_ledger WHERE productId = :productId ORDER BY recordedAt DESC")
    fun getLedgerForProductFlow(productId: String): Flow<List<StockLedger>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: StockLedger)

    // ==========================================
    // 7. SYNC EVENT LOG ACTIONS
    // ==========================================
    @Query("SELECT * FROM sync_log ORDER BY timestamp DESC")
    fun getAllSyncLogsFlow(): Flow<List<SyncLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncLog(log: SyncLog)

    @Query("DELETE FROM sync_log")
    suspend fun clearSyncLogs()
}
