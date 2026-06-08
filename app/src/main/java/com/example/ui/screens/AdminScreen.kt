package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AdminScreen(viewModel: MainViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val products by viewModel.products.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val returns by viewModel.returns.collectAsState()
    val collections by viewModel.collections.collectAsState()

    // Administrative state
    var showAddAccountForm by remember { mutableStateOf(false) }
    var showAddProductForm by remember { mutableStateOf(false) }

    // Forms Inputs Link
    val newAccName by viewModel.newAccountName.collectAsState()
    val newAccPhone by viewModel.newAccountPhone.collectAsState()
    val newAccAddr by viewModel.newAccountAddress.collectAsState()
    val newAccBal by viewModel.newAccountBalance.collectAsState()

    val newProdName by viewModel.newProductName.collectAsState()
    val newProdSku by viewModel.newProductSku.collectAsState()
    val newProdPrice by viewModel.newProductPrice.collectAsState()
    val newProdStock by viewModel.newProductStock.collectAsState()
    val newProdQr by viewModel.newProductQr.collectAsState()

    // Compute live relational metrics
    val totalInvoicedSales = invoices.sumOf { it.totalAmount }
    val totalSalesReturns = returns.sumOf { it.totalCreditAmount }
    val totalFieldCollections = collections.filter { it.collectionStatus == "Success" }.sumOf { it.amountCollected }

    // Accounts Ledger aggregation mapping
    val accountsLedger = accounts.map { acc ->
        val customerInvoices = invoices.filter { it.accountId == acc.accountId }.sumOf { it.totalAmount }
        val customerReturns = returns.filter { it.accountId == acc.accountId }.sumOf { it.totalCreditAmount }
        val customerCollections = collections.filter { it.accountId == acc.accountId && it.collectionStatus == "Success" }.sumOf { it.amountCollected }

        val closingBalance = acc.openingBalance + customerInvoices - (customerCollections + customerReturns)
        Triple(acc, customerInvoices, closingBalance)
    }

    val totalActiveDebtsExposed = accountsLedger.sumOf { it.third }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        AppHeader(
            title = "Admin Ledger Console",
            subtitle = "Enterprise Control Dashboard & Profit/Loss Audits",
            imageVector = Icons.Default.AdminPanelSettings
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selected active simulated node alert
            item {
                DevicePersonaSelector(viewModel = viewModel)
            }

            // PROFIT & LOSS BALANCES SHEET CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "REAL-TIME ENTERPRISE P&L STATEMENT",
                            color = SportsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            PLMetricCell(
                                title = "Gross Invoiced",
                                amount = "₹$totalInvoicedSales",
                                color = TextIvoryWhite,
                                icon = Icons.Default.TrendingUp,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            PLMetricCell(
                                title = "Sales Deductions",
                                amount = "₹$totalSalesReturns",
                                color = CrimsonError,
                                icon = Icons.Default.AssignmentReturn,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            PLMetricCell(
                                title = "Cash Recovered",
                                amount = "₹$totalFieldCollections",
                                color = EmeraldSuccess,
                                icon = Icons.Default.CheckCircle,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            PLMetricCell(
                                title = "Exposed Debts",
                                amount = "₹$totalActiveDebtsExposed",
                                color = SportsGold,
                                icon = Icons.Default.Warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // DIRECTORY OPERATIONS PANELS
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            showAddAccountForm = !showAddAccountForm
                            showAddProductForm = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAddAccountForm) SportsGold else SurfaceCard,
                            contentColor = if (showAddAccountForm) TextDarkBlack else TextIvoryWhite
                        ),
                        border = BorderStroke(1.dp, BorderAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_toggle_account_add")
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Account", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            showAddProductForm = !showAddProductForm
                            showAddAccountForm = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showAddProductForm) SportsGold else SurfaceCard,
                            contentColor = if (showAddProductForm) TextDarkBlack else TextIvoryWhite
                        ),
                        border = BorderStroke(1.dp, BorderAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_toggle_product_add")
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Product", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SUB-FORM: REGISTER NEW ACCOUNT
            if (showAddAccountForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                        border = BorderStroke(1.dp, SportsGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Create Account Registry Entry", color = SportsGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newAccName,
                                onValueChange = { viewModel.newAccountName.value = it },
                                label = { Text("Client/Wholesaler Name *", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_acc_name")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newAccPhone,
                                onValueChange = { viewModel.newAccountPhone.value = it },
                                label = { Text("Phone Number", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_acc_phone")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newAccAddr,
                                onValueChange = { viewModel.newAccountAddress.value = it },
                                label = { Text("Billing Address", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                maxLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_acc_addr")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newAccBal,
                                onValueChange = { viewModel.newAccountBalance.value = it },
                                label = { Text("Opening Balance Outstanding (₹)", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_acc_bal")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.registerNewAccount()
                                    showAddAccountForm = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SportsGold,
                                    contentColor = TextDarkBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_new_account")
                            ) {
                                Text("WRITE RECORD TO CENTRAL REGISTRY", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // SUB-FORM: REGISTER NEW PRODUCT
            if (showAddProductForm) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                        border = BorderStroke(1.dp, SportsGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Append Sports SKU Code to Catalog", color = SportsGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = newProdName,
                                onValueChange = { viewModel.newProductName.value = it },
                                label = { Text("Product Display Title *", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_prod_name")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newProdSku,
                                onValueChange = { viewModel.newProductSku.value = it },
                                label = { Text("SKU Standard Reference Code *", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_prod_sku")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                OutlinedTextField(
                                    value = newProdPrice,
                                    onValueChange = { viewModel.newProductPrice.value = it },
                                    label = { Text("Standard Unit Price (₹)", color = TextMutedGray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SportsGold,
                                        unfocusedBorderColor = BorderAccent,
                                        focusedTextColor = TextIvoryWhite,
                                        unfocusedTextColor = TextIvoryWhite
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("form_prod_price")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = newProdStock,
                                    onValueChange = { viewModel.newProductStock.value = it },
                                    label = { Text("Inward Init Stock", color = TextMutedGray, fontSize = 11.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SportsGold,
                                        unfocusedBorderColor = BorderAccent,
                                        focusedTextColor = TextIvoryWhite,
                                        unfocusedTextColor = TextIvoryWhite
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("form_prod_stock")
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newProdQr,
                                onValueChange = { viewModel.newProductQr.value = it },
                                label = { Text("QR Code Scanner Payload (Optional)", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_prod_qr")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.registerNewProduct()
                                    showAddProductForm = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SportsGold,
                                    contentColor = TextDarkBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_new_product")
                            ) {
                                Text("WRITE SKU RECORD TO CATALOG", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ACCOUNT MASTER TRACKING RELATION TABLE LIST
            item {
                Text(
                    text = "MASTER PURCHASER ACCOUNT LEDGER ENTRIES",
                    color = SportsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            items(accountsLedger) { triple ->
                val acc = triple.first
                val invoicedSales = triple.second
                val closingBalance = triple.third

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("master_ledger_acc_${acc.accountId}"),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = acc.accountName,
                                    color = TextIvoryWhite,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Phone: ${acc.phoneNumber}",
                                    color = TextMutedGray,
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Closing Bal",
                                    color = TextMutedGray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "₹$closingBalance",
                                    color = if (closingBalance < 0.0) CrimsonError else EmeraldSuccess,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = BorderAccent)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Multi-Column Ledger calculation trace row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Opening Balance", color = TextMutedGray, fontSize = 10.sp)
                                Text("₹${acc.openingBalance}", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Invoiced", color = TextMutedGray, fontSize = 10.sp)
                                Text("₹$invoicedSales", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Recoveries", color = TextMutedGray, fontSize = 10.sp)
                                // Fetch recoveries + returns
                                val customerCollections = collections.filter { it.accountId == acc.accountId && it.collectionStatus == "Success" }.sumOf { it.amountCollected }
                                val customerReturns = returns.filter { it.accountId == acc.accountId }.sumOf { it.totalCreditAmount }
                                val totalDeductions = customerCollections + customerReturns
                                Text("₹$totalDeductions", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PLMetricCell(
    title: String,
    amount: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, BorderAccent),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, color = TextMutedGray, fontSize = 10.sp)
                Text(text = amount, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}
