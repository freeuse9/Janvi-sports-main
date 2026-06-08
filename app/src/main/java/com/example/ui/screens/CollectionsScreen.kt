package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun CollectionsScreen(viewModel: MainViewModel) {
    val accounts by viewModel.accounts.collectAsState()
    val activeAccountId by viewModel.colAccountSelectionId.collectAsState()
    val amountCollectedInput by viewModel.colAmountInput.collectAsState()
    val notesInput by viewModel.colNotesInput.collectAsState()
    val statusSelection by viewModel.colStatusSelection.collectAsState()
    val dueDateInput by viewModel.colDueDateInput.collectAsState()
    val deviceId by viewModel.selectedDeviceId.collectAsState()

    var accountSearchQuery by remember { mutableStateOf("") }
    val filteredAccounts = accounts.filter {
        it.accountName.contains(accountSearchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        AppHeader(
            title = "Field Collections App",
            subtitle = "Field Agent Recovery & Inflow ledger Voucher",
            imageVector = Icons.Default.Paid
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DevicePersonaSelector(viewModel = viewModel)
            }

            // Central field collection form container
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "FIELD RECOVERY INFLOW VOUCHER",
                            color = SportsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Target client selector search
                        Text("1. Targeted Customer Profile Portfolio", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        var accountDropdownExpanded by remember { mutableStateOf(false) }
                        val activeAccount = accounts.find { it.accountId == activeAccountId }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = accountSearchQuery.ifEmpty { activeAccount?.accountName ?: "" },
                                onValueChange = {
                                    accountSearchQuery = it
                                    accountDropdownExpanded = true
                                },
                                placeholder = { Text("Query customer record...", color = TextMutedGray) },
                                leadingIcon = { Icon(Icons.Default.ManageSearch, contentDescription = null, tint = SportsGold) },
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
                                    .testTag("col_account_search_input")
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
                                                Text(acc.accountName, color = TextIvoryWhite, fontWeight = FontWeight.Bold)
                                                Text("Prev Bal: ₹${acc.openingBalance}", color = TextMutedGray, fontSize = 12.sp)
                                            }
                                        },
                                        onClick = {
                                            viewModel.colAccountSelectionId.value = acc.accountId
                                            accountSearchQuery = acc.accountName
                                            accountDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("col_account_item_${acc.accountName.take(5)}")
                                    )
                                }
                            }
                        }

                        // Collection Amount Input
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("2. Cold Cash / Cheque Numeric Amount Received (₹)", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = amountCollectedInput,
                            onValueChange = { viewModel.colAmountInput.value = it },
                            placeholder = { Text("e.g. ₹5000", color = TextMutedGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportsGold,
                                unfocusedBorderColor = BorderAccent,
                                focusedTextColor = TextIvoryWhite,
                                unfocusedTextColor = TextIvoryWhite
                            ),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("col_amount_input")
                        )

                        // Status toggle check
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("3. Collection Clearing Status Tracker", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderAccent, RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.colStatusSelection.value = "Success" }
                                    .background(if (statusSelection == "Success") EmeraldSuccess.copy(alpha = 0.15f) else Color.Transparent)
                                    .padding(vertical = 12.dp)
                                    .testTag("col_status_success_toggle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (statusSelection == "Success") EmeraldSuccess else TextMutedGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Success (Cleared)", color = if (statusSelection == "Success") EmeraldSuccess else TextMutedGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.colStatusSelection.value = "Pending" }
                                    .background(if (statusSelection == "Pending") GoldenWarning.copy(alpha = 0.15f) else Color.Transparent)
                                    .padding(vertical = 12.dp)
                                    .testTag("col_status_pending_toggle"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pending, contentDescription = null, tint = if (statusSelection == "Pending") GoldenWarning else TextMutedGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pending Float", color = if (statusSelection == "Pending") GoldenWarning else TextMutedGray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Due date if pending
                        if (statusSelection == "Pending") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Scheduled Settlement Clearance Date", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = dueDateInput,
                                onValueChange = { viewModel.colDueDateInput.value = it },
                                placeholder = { Text("YYYY-MM-DD", color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("col_due_date_input")
                            )
                        }

                        // Notes field
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("4. Field Notes & Settlement References", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { viewModel.colNotesInput.value = it },
                            placeholder = { Text("e.g. Received via GPay / Cheque Ref No.", color = TextMutedGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SportsGold,
                                unfocusedBorderColor = BorderAccent,
                                focusedTextColor = TextIvoryWhite,
                                unfocusedTextColor = TextIvoryWhite
                            ),
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("col_notes_input")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.submitFieldCollection() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SportsGold,
                                contentColor = TextDarkBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("col_submit_and_sync_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("SUBMIT DISPATCH VOUCHER (PEER NODE SYNC)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
