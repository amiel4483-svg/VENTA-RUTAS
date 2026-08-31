package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(
    currentScreen: AppScreen,
    subtitle: String = "Ventas",
    pendingSyncCount: Int = 0,
    onNavigate: (AppScreen) -> Unit,
    onOpenMenu: () -> Unit = {},
    onManualSync: () -> Unit = {},
    onEndRoute: () -> Unit = {}
) {
    var showOptionsMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DaniisaCyan)
    ) {
        // Main Blue Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier.testTag("menu_drawer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú principal",
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "DISTRIBUIDORA DANIISA",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }

            // Direct prominent Cierre de Jornada action in Top Bar
            Surface(
                onClick = { onNavigate(AppScreen.ROUTE_SUMMARY) },
                shape = RoundedCornerShape(8.dp),
                color = if (currentScreen == AppScreen.ROUTE_SUMMARY) Color.White else Color(0xFFFFD54F),
                contentColor = if (currentScreen == AppScreen.ROUTE_SUMMARY) DaniisaCyanDark else Color(0xFF5D4037),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .testTag("top_cierre_jornada_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Summarize,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cierre",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Sync Notification badge or bell
            IconButton(
                onClick = onManualSync,
                modifier = Modifier.testTag("sync_bell_button")
            ) {
                BadgedBox(
                    badge = {
                        if (pendingSyncCount > 0) {
                            Badge(containerColor = Color(0xFFFF5252)) {
                                Text("$pendingSyncCount", color = Color.White)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (pendingSyncCount > 0) Icons.Default.SyncProblem else Icons.Default.Notifications,
                        contentDescription = "Sincronización y Notificaciones",
                        tint = Color.White
                    )
                }
            }

            // Overflow 3-dots Menu
            Box {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier.testTag("overflow_options_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Panel Admin Web") },
                        leadingIcon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        onClick = {
                            showOptionsMenu = false
                            onNavigate(AppScreen.WEB_ADMIN)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Control de Stock") },
                        leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                        onClick = {
                            showOptionsMenu = false
                            onNavigate(AppScreen.INVENTORY)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clientes & Gastos") },
                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                        onClick = {
                            showOptionsMenu = false
                            onNavigate(AppScreen.CLIENTS_EXPENSES)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Configuración / SPREADSHEET_ID") },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        onClick = {
                            showOptionsMenu = false
                            onNavigate(AppScreen.SETTINGS_SYNC)
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Cerrar Ruta y Consolidar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showOptionsMenu = false
                            onEndRoute()
                        }
                    )
                }
            }
        }

        // Horizontal Action Pill Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PillButton(
                title = "Venta POS",
                icon = Icons.Default.ShoppingCart,
                isSelected = currentScreen == AppScreen.SALES,
                onClick = { onNavigate(AppScreen.SALES) }
            )
            PillButton(
                title = "Historial de Ventas",
                icon = Icons.Default.History,
                isSelected = currentScreen == AppScreen.SALES_HISTORY,
                onClick = { onNavigate(AppScreen.SALES_HISTORY) }
            )
            PillButton(
                title = "Reportes Daniisa",
                icon = Icons.Default.BarChart,
                isSelected = currentScreen == AppScreen.REPORTS_GRID || currentScreen == AppScreen.REPORT_DETAIL,
                onClick = { onNavigate(AppScreen.REPORTS_GRID) }
            )
            PillButton(
                title = "Admin Web",
                icon = Icons.Default.Insights,
                isSelected = currentScreen == AppScreen.WEB_ADMIN,
                onClick = { onNavigate(AppScreen.WEB_ADMIN) }
            )
            PillButton(
                title = "Stock Ruta",
                icon = Icons.Default.LocalShipping,
                isSelected = currentScreen == AppScreen.INVENTORY,
                onClick = { onNavigate(AppScreen.INVENTORY) }
            )
            PillButton(
                title = "Cierre de Jornada",
                icon = Icons.Default.Summarize,
                isSelected = currentScreen == AppScreen.ROUTE_SUMMARY,
                isHighlight = true,
                onClick = { onNavigate(AppScreen.ROUTE_SUMMARY) }
            )
        }
    }
}

@Composable
private fun PillButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) {
            Color.White
        } else if (isHighlight) {
            Color(0xFFFFD54F)
        } else {
            Color.White.copy(alpha = 0.25f)
        },
        contentColor = if (isSelected) {
            DaniisaCyan
        } else if (isHighlight) {
            Color(0xFF5D4037)
        } else {
            Color.White
        },
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
