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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountMaster
import com.example.data.ProductMaster
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ReturnScreen(viewModel: MainViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val products by viewModel.products.collectAsState()
    val returnCart by viewModel.returnCart.collectAsState()
    val activeAccountId by viewModel.returnAccountSelectionId.collectAsState()
    val returnsList by viewModel.returns.collectAsState()

    var productSearchText by remember { mutableStateOf("") }
    var accountSearchText by remember { mutableStateOf("") }

    val nextReturnNo = (returnsList.firstOrNull()?.returnNumber ?: 0) + 1

    // Search filters
    val filteredAccounts = accounts.filter {
        it.accountName.contains(accountSearchText, ignoreCase = true)
    }

    val filteredProducts = products.filter {
        it.productName.contains(productSearchText, ignoreCase = true) ||
                it.skuCode.contains(productSearchText, ignoreCase = true)
    }

    val netCreditTotal = returnCart.sumOf { it.second * it.third }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        AppHeader(
            title = "Sales Return Dashboard",
            subtitle = "Rejection Registry & Inventory Replenishment Desk",
            imageVector = Icons.Default.KeyboardReturn
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DevicePersonaSelector(viewModel = viewModel)
            }

            // Customer selection and header configuration
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
                                text = "RETURN PROFILE LINK",
                                color = SportsGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SportsGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "RETURN VOUCHER #$nextReturnNo",
                                    color = SportsGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Customer directory dropdown
                        Text("1. Originating Purchaser Directory Profile", color = TextIvoryWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        var accountDropdownExpanded by remember { mutableStateOf(false) }
                        val selectedAccount = accounts.find { it.accountId == activeAccountId }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = accountSearchText.ifEmpty { selectedAccount?.accountName ?: "" },
                                onValueChange = {
                                    accountSearchText = it
                                    accountDropdownExpanded = true
                                },
                                placeholder = { Text("Search client record...", color = TextMutedGray) },
                                leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SportsGold) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { accountDropdownExpanded = !accountDropdownExpanded },
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
                                    .testTag("return_account_search_input")
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
                                            Text(acc.accountName, color = TextIvoryWhite, fontWeight = FontWeight.Bold)
                                        },
                                        onClick = {
                                            viewModel.selectReturnAccount(acc.accountId)
                                            accountSearchText = acc.accountName
                                            accountDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("return_account_item_${acc.accountName.take(5)}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Products to choose from catalog
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CATALOG REJECTIONS ENTRY CONTROLS",
                            color = SportsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        var productDropdownExpanded by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = productSearchText,
                                onValueChange = {
                                    productSearchText = it
                                    productDropdownExpanded = true
                                },
                                placeholder = { Text("Query sports product catalog code...", color = TextMutedGray) },
                                leadingIcon = { Icon(Icons.Default.ManageSearch, contentDescription = null, tint = SportsGold) },
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
                                    .testTag("return_product_search_input")
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
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(prod.productName, color = TextIvoryWhite, fontWeight = FontWeight.Bold)
                                                    Text("SKU: ${prod.skuCode} • Stock: ${prod.currentStock}", color = TextMutedGray, fontSize = 11.sp)
                                                }
                                                Text("₹${prod.standardPrice}", color = SportsGold, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            viewModel.addProductToReturnCart(prod)
                                            productSearchText = ""
                                            productDropdownExpanded = false
                                            viewModel.showToast("${prod.productName} ready for restocking feedback.", true)
                                        },
                                        modifier = Modifier.testTag("return_product_option_${prod.skuCode}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive array sheet
            item {
                Text(
                    text = "RESTOCKED REJECTED ITEMS ARRAY",
                    color = SportsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            if (returnCart.isEmpty()) {
                item {
                    EmptyPlaceholderCard(
                        title = "Return Docket is Empty",
                        tip = "Select customer profile above and search and select returnable items to restock inventory.",
                        icon = Icons.Default.RemoveShoppingCart
                    )
                }
            } else {
                items(returnCart) { item ->
                    val prod = item.first
                    val qty = item.second
                    val creditPrice = item.third
                    val lineCreditTotal = qty * creditPrice

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("return_cart_item_${prod.productId}"),
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
                                    Text("SKU: ${prod.skuCode} • Base Rate: ₹${prod.standardPrice} • Stock +$qty units", color = TextMutedGray, fontSize = 11.sp)
                                }
                                IconButton(
                                    onClick = { viewModel.removeProductFromReturnCart(prod.productId) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("remove_return_item_${prod.productId}")
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonError, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = BorderAccent)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Qty increment/decrement
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, BorderAccent, RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.updateReturnQuantity(prod.productId, qty - 1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("decrement_return_${prod.productId}")
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
                                        onClick = { viewModel.updateReturnQuantity(prod.productId, qty + 1) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("increment_return_${prod.productId}")
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "More", tint = SportsGold, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Pricing adjustments inputs
                                OutlinedTextField(
                                    value = if (creditPrice == 0.0) "" else "$creditPrice",
                                    onValueChange = { text ->
                                        val price = text.toDoubleOrNull() ?: 0.0
                                        viewModel.updateReturnCreditPrice(prod.productId, price)
                                    },
                                    label = { Text("Credit Unit Rate (₹)", fontSize = 10.sp, color = TextMutedGray) },
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
                                        .height(52.dp)
                                        .testTag("override_credit_price_${prod.skuCode}")
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Credit Total", color = TextMutedGray, fontSize = 10.sp)
                                    Text("₹$lineCreditTotal", color = EmeraldSuccess, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Execute sales return credit notes
            if (returnCart.isNotEmpty()) {
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TOTAL CREDIT SLIP OFFSET VALUE:", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("₹$netCreditTotal", color = EmeraldSuccess, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.submitSalesReturn() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldSuccess,
                                    contentColor = TextIvoryWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_return_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Backup, contentDescription = "Post Slip")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SUBMIT CREDITING RETURN (REPLENISH INVENTORY)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
