package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val context = LocalContext.current

                // Handle global alert notifications reactively
                LaunchedEffect(Unit) {
                    viewModel.uiMessage.collect { (msg, isSuccess) ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("main_scaffold"),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_navigation_bar"),
                            containerColor = SecondaryPanel,
                            tonalElevation = 8.dp
                        ) {
                            val items = listOf(
                                Triple("Scanner", Icons.Default.QrCodeScanner, "nav_tab_scanner"),
                                Triple("Billing", Icons.Default.ReceiptLong, "nav_tab_billing"),
                                Triple("Return", Icons.Default.KeyboardReturn, "nav_tab_return"),
                                Triple("Collections", Icons.Default.Paid, "nav_tab_collections"),
                                Triple("Admin", Icons.Default.AdminPanelSettings, "nav_tab_admin"),
                                Triple("Broker", Icons.Default.SettingsEthernet, "nav_tab_broker")
                            )

                            items.forEachIndexed { index, (label, icon, tag) ->
                                val selected = currentTab == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.currentTab.value = index },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (selected) SportsGold else TextMutedGray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 9.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) SportsGold else TextMutedGray,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = SurfaceCard
                                    ),
                                    modifier = Modifier.testTag(tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = 12.dp,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                    ) {
                        // Simulated central Websocket sync ticker tray
                        RealtimeWebSocketNotificationTray(viewModel = viewModel)

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTab) {
                                0 -> ScannerScreen(viewModel = viewModel)
                                1 -> BillingScreen(viewModel = viewModel)
                                2 -> ReturnScreen(viewModel = viewModel)
                                3 -> CollectionsScreen(viewModel = viewModel)
                                4 -> AdminScreen(viewModel = viewModel)
                                5 -> SyncBrokerScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
