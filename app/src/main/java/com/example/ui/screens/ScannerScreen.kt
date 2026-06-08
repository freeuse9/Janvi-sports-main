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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProductMaster
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun ScannerScreen(viewModel: MainViewModel) {
    val products by viewModel.products.collectAsState()
    val ledgerList by viewModel.stockLedger.collectAsState()
    val quantityInput by viewModel.qrQuantityInput.collectAsState()
    val actionType by viewModel.qrActionType.collectAsState()

    var scannedQrCode by remember { mutableStateOf("") }
    var selectedProductForScan by remember { mutableStateOf<ProductMaster?>(null) }
    var laserQueryTyped by remember { mutableStateOf("") }

    // On startup, choose first product as scanned default
    LaunchedEffect(products) {
        if (products.isNotEmpty() && selectedProductForScan == null) {
            val first = products.first()
            selectedProductForScan = first
            scannedQrCode = first.qrCodePayload
        }
    }

    LaunchedEffect(scannedQrCode) {
        if (scannedQrCode.isNotEmpty()) {
            selectedProductForScan = products.find { it.qrCodePayload == scannedQrCode }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        AppHeader(
            title = "QR Scanner Terminal",
            subtitle = "Active Warehouse Floor & Catalog Sync Unit",
            imageVector = Icons.Default.QrCodeScanner
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Simulated Active Device Persona Alert
            item {
                DevicePersonaSelector(viewModel = viewModel)
            }

            // High Resolution Camera Array Simulator Screen
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIVE CAMERA FEED & SCANNING DECK",
                            color = SportsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Camera Viewport Frame
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black)
                                .border(2.dp, SportsGold.copy(alpha = 0.8f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // High intensity laser sight corners
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp)
                                    .border(1.dp, Color.Red.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            )

                            // Animated Laser Line
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(Color.Red)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Simulated feed",
                                    tint = SportsGold.copy(alpha = 0.4f),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (scannedQrCode.isEmpty()) "AIM AT SKU ATTACHMENT" else "SKU IDENTIFIED: $scannedQrCode",
                                    color = SportsGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hardware Input / Scan Emulator Trigger",
                            color = TextMutedGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Selectable stream streams representing QR scanners
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var dropdownExpanded by remember { mutableStateOf(false) }

                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { dropdownExpanded = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SurfaceCard,
                                        contentColor = TextIvoryWhite
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderAccent),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("qr_stream_select")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedProductForScan?.productName ?: "Select Registered QR Code",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Dropdown"
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.background(SurfaceCard)
                                ) {
                                    products.forEach { item ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(item.productName, color = TextIvoryWhite, fontWeight = FontWeight.Bold)
                                                    Text("Payload: ${item.qrCodePayload}", color = TextMutedGray, fontSize = 11.sp)
                                                }
                                            },
                                            onClick = {
                                                scannedQrCode = item.qrCodePayload
                                                selectedProductForScan = item
                                                dropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("qr_option_${item.skuCode}")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Manual Laser input
                            OutlinedTextField(
                                value = laserQueryTyped,
                                onValueChange = {
                                    laserQueryTyped = it
                                    if (products.any { p -> p.qrCodePayload == it }) {
                                        scannedQrCode = it
                                        laserQueryTyped = ""
                                        viewModel.showToast("Laser peripheral payload matched SKU!", true)
                                    }
                                },
                                label = { Text("Or Type Raw QR", fontSize = 11.sp, color = TextMutedGray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SportsGold,
                                    unfocusedBorderColor = BorderAccent,
                                    focusedTextColor = TextIvoryWhite,
                                    unfocusedTextColor = TextIvoryWhite
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                singleLine = true,
                                modifier = Modifier
                                    .width(130.dp)
                                    .testTag("manual_laser_input")
                            )
                        }
                    }
                }
            }

            // Interactive payload details card representing SKU
            item {
                selectedProductForScan?.let { prod ->
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
                                    text = "SCAN IDENTIFIED SKU DIRECTORY INFO",
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
                                        text = prod.skuCode,
                                        color = SportsGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = prod.productName,
                                color = TextIvoryWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Current Live Stock", color = TextMutedGray, fontSize = 11.sp)
                                    Text(
                                        text = "${prod.currentStock} Units",
                                        color = if (prod.currentStock <= 10) CrimsonError else EmeraldSuccess,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Standard SKU Rate", color = TextMutedGray, fontSize = 11.sp)
                                    Text(
                                        text = "₹${prod.standardPrice}",
                                        color = TextIvoryWhite,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = BorderAccent)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Mutable adjust controller parameters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = quantityInput,
                                    onValueChange = { viewModel.qrQuantityInput.value = it },
                                    label = { Text("Delta Adjust Qty", color = TextMutedGray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SportsGold,
                                        unfocusedBorderColor = BorderAccent,
                                        focusedTextColor = TextIvoryWhite,
                                        unfocusedTextColor = TextIvoryWhite
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("qr_quantity_input")
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // ADD/REMOVE toggles
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceCard)
                                        .border(1.dp, BorderAccent, RoundedCornerShape(8.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clickable { viewModel.qrActionType.value = "ADD" }
                                            .background(if (actionType == "ADD") EmeraldSuccess.copy(alpha = 0.2f) else Color.Transparent)
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("action_add_toggle"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "ADD INWARD",
                                            color = if (actionType == "ADD") EmeraldSuccess else TextMutedGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clickable { viewModel.qrActionType.value = "REMOVE" }
                                            .background(if (actionType == "REMOVE") CrimsonError.copy(alpha = 0.2f) else Color.Transparent)
                                            .padding(horizontal = 16.dp, vertical = 12.dp)
                                            .testTag("action_remove_toggle"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "REMOVE OUT",
                                            color = if (actionType == "REMOVE") CrimsonError else TextMutedGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large Tactile Execute Button
                            Button(
                                onClick = { viewModel.adjustInventoryScan(scannedQrCode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (actionType == "ADD") EmeraldSuccess else CrimsonError,
                                    contentColor = TextIvoryWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("qr_execute_adjust_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (actionType == "ADD") Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                                        contentDescription = "Execute"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (actionType == "ADD") "EXECUTE PORTAL STOCK ADDITION (+${quantityInput})" else "EXECUTE SECURE DEDUCTION REDUCTION (-${quantityInput})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Local floor timeline log tracing
            item {
                Text(
                    text = "CHRONOLOGICAL STOCK ORGANIZER TRACE",
                    color = SportsGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            if (ledgerList.isEmpty()) {
                item {
                    EmptyPlaceholderCard(
                        title = "No timeline events recorded",
                        tip = "Perform inventory alterations above to inspect running asset ledgers."
                    )
                }
            } else {
                items(ledgerList.take(6)) { log ->
                    val prod = products.find { it.productId == log.productId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_trace_item_${log.ledgerEntryId}"),
                        colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                        border = BorderStroke(1.dp, BorderAccent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (log.transactionType) {
                                            "INWARD", "RETURN_IN" -> EmeraldSuccess.copy(alpha = 0.15f)
                                            else -> CrimsonError.copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (log.transactionType) {
                                        "INWARD", "RETURN_IN" -> Icons.Default.TrendingUp
                                        else -> Icons.Default.TrendingDown
                                    },
                                    contentDescription = log.transactionType,
                                    tint = when (log.transactionType) {
                                        "INWARD", "RETURN_IN" -> EmeraldSuccess
                                        else -> CrimsonError
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod?.productName ?: "Unknown Product",
                                    color = TextIvoryWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Node: ${log.deviceId} • Event: ${log.transactionType}",
                                    color = TextMutedGray,
                                    fontSize = 11.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (log.quantityChanged >= 0) "+${log.quantityChanged}" else "${log.quantityChanged}",
                                    color = if (log.quantityChanged >= 0) EmeraldSuccess else CrimsonError,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bal: ${log.runningStockBalance}",
                                    color = TextMutedGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
