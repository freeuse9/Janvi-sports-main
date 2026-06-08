package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountMaster
import com.example.data.ProductMaster
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BillingScreen(viewModel: MainViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val products by viewModel.products.collectAsState()
    val cart by viewModel.billingCart.collectAsState()
    val selectedAccountId by viewModel.billingAccountSelectionId.collectAsState()
    val invoices by viewModel.invoices.collectAsState()

    var productSearchText by remember { mutableStateOf("") }
    var accountSearchText by remember { mutableStateOf("") }
    var customInvoiceDate by remember { mutableStateOf("") }

    val nextInvoiceNumber = (invoices.firstOrNull()?.invoiceNumber ?: 0) + 1

    LaunchedEffect(Unit) {
        if (customInvoiceDate.isEmpty()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            customInvoiceDate = sdf.format(Date())
        }
    }

    // Filtered accounts & products based on search queries
    val filteredAccounts = accounts.filter {
        it.accountName.contains(accountSearchText, ignoreCase = true) ||
                it.phoneNumber.contains(accountSearchText)
    }

    val filteredProducts = products.filter {
        it.productName.contains(productSearchText, ignoreCase = true) ||
                it.skuCode.contains(productSearchText, ignoreCase = true) ||
                it.qrCodePayload.contains(productSearchText, ignoreCase = true)
    }

    // Dynamic running totals
    val subtotal = cart.sumOf { it.second * it.third }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        AppHeader(
            title = "Invoicing Hub Workspace",
            subtitle = "Digital Billing Terminal & ACID Registry Lock Desk",
            imageVector = Icons.Default.ReceiptLong
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DevicePersonaSelector(viewModel = viewModel)
            }

            // HEADER DETAILS: Customer Selector, Date, Auto-No
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "METADATA REASSESSMENT HEADER",
                                color = SportsGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SportsGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "INVOICE #$nextInvoiceNumber",
                                        color = TextDarkBlack,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Customer Selection Drawer
                        Text("1. Purchaser Account Directory Profile", color = TextIvoryWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        var accountDropdownExpanded by remember { mutableStateOf(false) }
                        val activeAccount = accounts.find { it.accountId == selectedAccountId }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = accountSearchText.ifEmpty { activeAccount?.accountName ?: "" },
                                onValueChange = {
                                    accountSearchText = it
                                    accountDropdownExpanded = true
                                },
                                placeholder = { Text("Query customer name or phone...", color = TextMutedGray) },
                                leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SportsGold) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { accountDropdownExpanded = !accountDropdownExpanded },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Toggle dropdown", tint = SportsGold)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("billing_account_search_input")
                            )

                            DropdownMenu(
                                expanded = accountDropdownExpanded && filteredAccounts.isNotEmpty(),
                                onDismissRequest = { accountDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(SurfaceCard)
                            ) {
                                filteredAccounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(acc.accountName, color = TextIvoryWhite, fontWeight = FontWeight.Bold)
                                                    Text(acc.phoneNumber, color = TextMutedGray, fontSize = 11.sp)
                                                }
                                                Text(
                                                    text = "Bal: ₹${acc.openingBalance}",
                                                    color = if (acc.openingBalance < 0) CrimsonError else EmeraldSuccess,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectBillingAccount(acc.accountId)
                                            accountSearchText = acc.accountName
                                            accountDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("billing_account_item_${acc.accountName.take(5)}")
                                    )
                                }
                            }
                        }

                        // Date Picker (Editable text calendar backdate simulation)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("2. Billing Date Element (Backdating Allowed)", color = TextIvoryWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customInvoiceDate,
                            onValueChange = { customInvoiceDate = it },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = SportsGold) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportsGold,
                                unfocusedBorderColor = BorderAccent,
                                focusedTextColor = TextIvoryWhite,
                                unfocusedTextColor = TextIvoryWhite
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("billing_date_element")
                        )
                    }
                }
            }

            // DIRECT LINE-ITEM ENTRY DRAWER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "HYBRID LINE ITEMS ENTRY CONTROLS",
                            color = SportsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var productDropdownExpanded by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = productSearchText,
                                    onValueChange = {
                                        productSearchText = it
                                        productDropdownExpanded = true
                                    },
                                    placeholder = { Text("Query sports standard SKUs...", color = TextMutedGray) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SportsGold) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { productDropdownExpanded = !productDropdownExpanded },
                                            modifier = Modifier.minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = SportsGold)
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SportsGold,
                                        unfocusedBorderColor = BorderAccent,
                                        focusedTextColor = TextIvoryWhite,
                                        unfocusedTextColor = TextIvoryWhite
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("billing_product_search_input")
                                )

                                DropdownMenu(
                                    expanded = productDropdownExpanded && filteredProducts.isNotEmpty(),
                                    onDismissRequest = { productDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(SurfaceCard)
                                ) {
                                    filteredProducts.forEach { prod ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(prod.productName, color = TextIvoryWhite, fontWeight = FontWeight.Bold)
                                                        Text("SKU: ${prod.skuCode} • Stock: ${prod.currentStock}", color = TextMutedGray, fontSize = 11.sp)
                                                    }
                                                    Text(
                                                        "₹${prod.standardPrice}",
                                                        color = SportsGold,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            },
                                            onClick = {
                                                viewModel.addProductToBillingCart(prod)
                                                productSearchText = ""
                                                productDropdownExpanded = false
                                                viewModel.showToast("${prod.productName} loaded to basket.", true)
                                            },
                                            modifier = Modifier.testTag("billing_product_option_${prod.skuCode}")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // INTERACTIVE ACTIVE LINE ITEM ARRAY SHEET
            item {
                Text(
                    text = "ACTIVE BILLING BASKET LINEITEMS",
                    color = SportsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            if (cart.isEmpty()) {
                item {
                    EmptyPlaceholderCard(
                        title = "Billing Basket is Empty",
                        tip = "Select a customer profile above and query available sports gear to compile a dynamic ledger invoice.",
                        icon = Icons.Default.AddShoppingCart
                    )
                }
            } else {
                items(cart) { item ->
                    val prod = item.first
                    val qty = item.second
                    val overriddenPrice = item.third
                    val lineTotal = qty * overriddenPrice

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("billing_cart_item_${prod.productId}"),
                        colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                        border = BorderStroke(1.dp, BorderAccent)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.productName, color = TextIvoryWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("SKU: ${prod.skuCode} • Base Rate: ₹${prod.standardPrice}", color = TextMutedGray, fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.removeCartItemInBilling(prod.productId) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("remove_cart_item_${prod.productId}")
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete from basket", tint = CrimsonError, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderAccent)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive controls: Quantity & Live Rate override inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Qty modifier
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, BorderAccent, RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(prod.productId, qty - 1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("decrement_qty_${prod.productId}")
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Less", tint = SportsGold, modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "$qty",
                                        color = TextIvoryWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.updateCartItemQuantity(prod.productId, qty + 1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("increment_qty_${prod.productId}")
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "More", tint = SportsGold, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // LINE-ITEM RATE OVERRIDE ENGINE: Direct number modification textfield
                                OutlinedTextField(
                                    value = if (overriddenPrice == 0.0) "" else "$overriddenPrice",
                                    onValueChange = { text ->
                                        val price = text.toDoubleOrNull() ?: 0.0
                                        viewModel.updateCartItemPriceOverride(prod.productId, price)
                                    },
                                    label = { Text("Override Unit Rate (₹)", fontSize = 10.sp, color = TextMutedGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SportsGold,
                                        unfocusedBorderColor = BorderAccent,
                                        focusedTextColor = TextIvoryWhite,
                                        unfocusedTextColor = TextIvoryWhite
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp) // Maintain accessible heights
                                        .testTag("override_price_input_${prod.skuCode}")
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // Line Total Display
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Line Total", color = TextMutedGray, fontSize = 10.sp)
                                    Text("₹$lineTotal", color = SportsGold, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            // MASTER TRANSACTION SUMMARY & SUBMISSION
            if (cart.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                        border = BorderStroke(1.dp, SportsGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("BASKET ITEM SUBCOUNT:", color = TextMutedGray, fontSize = 12.sp)
                                Text("${cart.sumOf { it.second }} Units", color = TextIvoryWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("NET CALCULATED BILL AMOUNT:", color = TextIvoryWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("₹$subtotal", color = SportsGold, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.submitBillingCart() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SportsGold,
                                    contentColor = TextDarkBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("checkout_dispatch_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Payment, contentDescription = "Execute CheckOut")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("DISPATCH INVOICE (ACID LOCK RECORD)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
