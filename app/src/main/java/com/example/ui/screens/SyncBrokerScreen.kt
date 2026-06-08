package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncBrokerScreen(viewModel: MainViewModel) {
    val syncLogs by viewModel.syncLogs.collectAsState()
    val activeDevice by viewModel.selectedDeviceId.collectAsState()

    val currentTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        AppHeader(
            title = "WebSocket Sync Broker",
            subtitle = "Distributed 5-Node WebSockets Stream Core Diagnostic",
            imageVector = Icons.Default.NetworkPing
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selected active simulated node alert
            item {
                DevicePersonaSelector(viewModel = viewModel)
            }

            // CENTRAL BROKER STATUS MATRIX
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, SportsGold.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WS REAL-TIME CONNECTION SERVER",
                                color = SportsGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SERVER ONLINE",
                                    color = EmeraldSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Status nodes grid
                        Text("Active Peer Networks Connected Terminals:", color = TextIvoryWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        val terminalDevices = listOf(
                            "LAPTOP-01" to "Billing Terminal 1",
                            "TABLET-02" to "Factory Floor Inflow",
                            "MOBILE-03" to "Floor Scanning Unit",
                            "MOBILE-04" to "Field Collectors Recovery",
                            "LAPTOP-05" to "Management Dashboard"
                        )

                        terminalDevices.forEach { (code, desc) ->
                            val isSelf = activeDevice.startsWith(code)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelf) SportsGold.copy(alpha = 0.08f) else Color.Transparent)
                                    .border(if (isSelf) BorderStroke(1.dp, SportsGold.copy(alpha = 0.2f)) else BorderStroke(0.dp, Color.Transparent))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$code ($desc)",
                                        color = if (isSelf) SportsGold else TextIvoryWhite,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Normal
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelf) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(SportsGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE SELF",
                                                color = SportsGold,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Text(
                                        text = "SYNCED",
                                        color = EmeraldSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // NETWORK SIMULATOR OPTIONS
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                    border = BorderStroke(1.dp, BorderAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "NETWORK INTERCEPT SIMULATION SUITE",
                            color = SportsGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Simulate writing standard transactions on standard parallel node terminals over WebSockets in real time.",
                            color = TextMutedGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.simulateOtherDeviceWrite() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SportsGold,
                                    contentColor = TextDarkBlack
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("simulate_peer_write_button")
                            ) {
                                Icon(Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Post Peer Sync Write", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = { viewModel.clearBrokerLogs() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CrimsonError
                                ),
                                border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clear_broker_queue_button")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Flush Logs Console", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // WEBSOCKET LOGS CONSOLE OUTPUT
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WS TRANSACTION BROADCAST QUEUES",
                        color = SportsGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LAST UPDATE: $currentTimestamp",
                        color = TextMutedGray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (syncLogs.isEmpty()) {
                item {
                    EmptyPlaceholderCard(
                        title = "Logs console queue empty",
                        tip = "Perform billing transactions or trigger peer socket write simulations to capture delta packet headers.",
                        icon = Icons.Default.Notes
                    )
                }
            } else {
                items(syncLogs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_log_item_${log.id}"),
                        colors = CardDefaults.cardColors(containerColor = SecondaryPanel),
                        border = BorderStroke(1.dp, BorderAccent)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SportsGold)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = log.deviceId,
                                            color = TextDarkBlack,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Frame Delta: [${log.deltaChange}]",
                                        color = SportsGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                val logTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                                Text(
                                    text = logTime,
                                    color = TextMutedGray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = log.eventDescription,
                                color = TextIvoryWhite,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.SansSerif,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
