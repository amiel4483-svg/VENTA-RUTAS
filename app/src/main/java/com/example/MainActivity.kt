package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DaniisaRepository
import com.example.ui.components.TopNavBar
import com.example.ui.screens.*
import com.example.ui.theme.DaniisaCyan
import com.example.ui.theme.DaniisaCyanDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.DaniisaViewModel
import com.example.ui.viewmodel.DaniisaViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DaniisaRepository(database)
        val factory = DaniisaViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                DaniisaApp(factory = factory)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaniisaApp(
    factory: DaniisaViewModelFactory,
    viewModel: DaniisaViewModel = viewModel(factory = factory)
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val clients by viewModel.clients.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val saleDetails by viewModel.saleDetails.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val returns by viewModel.returns.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSessionFlow.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncSummary by viewModel.syncSummary.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val spreadsheetId by viewModel.spreadsheetId.collectAsStateWithLifecycle()
    val webAppUrl by viewModel.webAppUrl.collectAsStateWithLifecycle()
    val selectedReportId by viewModel.selectedReportId.collectAsStateWithLifecycle()

    val pendingSyncCount = remember(sales, expenses, returns) {
        sales.count { it.syncStatus == "PENDIENTE" } +
                expenses.count { it.syncStatus == "PENDIENTE" } +
                returns.count { it.syncStatus == "PENDIENTE" }
    }

    // Handle user messages / alerts
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle back press gracefully
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = !drawerState.isOpen && uiState.currentScreen != AppScreen.SALES && uiState.currentScreen != AppScreen.ROUTE_START) {
        if (uiState.currentScreen == AppScreen.REPORT_DETAIL) {
            viewModel.navigateTo(AppScreen.REPORTS_GRID)
        } else {
            viewModel.navigateTo(AppScreen.SALES)
        }
    }

    // Modal Navigation Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DaniisaCyan)
                        .padding(20.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DISTRIBUIDORA DANIISA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Vendedor: ${uiState.activeEmployee}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                    label = { Text("Ventas POS (Ruta)") },
                    selected = uiState.currentScreen == AppScreen.SALES,
                    onClick = {
                        viewModel.navigateTo(AppScreen.SALES)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Historial de Ventas") },
                    selected = uiState.currentScreen == AppScreen.SALES_HISTORY,
                    onClick = {
                        viewModel.navigateTo(AppScreen.SALES_HISTORY)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = { Text("Reportes Daniisa") },
                    selected = uiState.currentScreen == AppScreen.REPORTS_GRID,
                    onClick = {
                        viewModel.navigateTo(AppScreen.REPORTS_GRID)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Panel Admin Web") },
                    selected = uiState.currentScreen == AppScreen.WEB_ADMIN,
                    onClick = {
                        viewModel.navigateTo(AppScreen.WEB_ADMIN)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("Control de Stock") },
                    selected = uiState.currentScreen == AppScreen.INVENTORY,
                    onClick = {
                        viewModel.navigateTo(AppScreen.INVENTORY)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Clientes & Gastos") },
                    selected = uiState.currentScreen == AppScreen.CLIENTS_EXPENSES,
                    onClick = {
                        viewModel.navigateTo(AppScreen.CLIENTS_EXPENSES)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Summarize, contentDescription = null) },
                    label = { Text("Cierre de Jornada / Resumen") },
                    selected = uiState.currentScreen == AppScreen.ROUTE_SUMMARY,
                    onClick = {
                        viewModel.navigateTo(AppScreen.ROUTE_SUMMARY)
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                    label = { Text("Información de Mi Negocio") },
                    selected = uiState.currentScreen == AppScreen.BUSINESS_INFO,
                    onClick = {
                        viewModel.navigateTo(AppScreen.BUSINESS_INFO)
                        scope.launch { drawerState.close() }
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Configuración & Sheets") },
                    selected = uiState.currentScreen == AppScreen.SETTINGS_SYNC,
                    onClick = {
                        viewModel.navigateTo(AppScreen.SETTINGS_SYNC)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (uiState.currentScreen != AppScreen.ROUTE_START) {
                    val subtitle = when (uiState.currentScreen) {
                        AppScreen.SALES -> "Ventas en Ruta"
                        AppScreen.SALES_HISTORY -> "Historial de Comprobantes"
                        AppScreen.REPORTS_GRID -> "Reportes y Estadísticas"
                        AppScreen.REPORT_DETAIL -> "Detalle de Reporte"
                        AppScreen.WEB_ADMIN -> "Panel Administrativo"
                        AppScreen.ROUTE_SUMMARY -> "Resumen de Cierre Diario"
                        AppScreen.INVENTORY -> "Inventario en Vehículo"
                        AppScreen.CLIENTS_EXPENSES -> "Clientes y Gastos"
                        AppScreen.BUSINESS_INFO -> "Información de Mi Negocio"
                        AppScreen.SETTINGS_SYNC -> "Configuración & Sincronización"
                        else -> "Ventas"
                    }

                    TopNavBar(
                        currentScreen = uiState.currentScreen,
                        subtitle = subtitle,
                        pendingSyncCount = pendingSyncCount,
                        onNavigate = { screen -> viewModel.navigateTo(screen) },
                        onOpenMenu = { scope.launch { drawerState.open() } },
                        onManualSync = { viewModel.performManualSync() },
                        onEndRoute = { viewModel.endRoute() }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentScreen) {
                    AppScreen.ROUTE_START -> {
                        RouteStartScreen(
                            employees = employees,
                            onStartRoute = { name, id, cash ->
                                viewModel.startRoute(name, id, cash)
                            }
                        )
                    }

                    AppScreen.SALES -> {
                        SalesScreen(
                            cartItems = uiState.cartItems,
                            allProducts = products,
                            allClients = clients,
                            selectedClient = uiState.selectedClient,
                            paymentStatus = uiState.paymentStatus,
                            paymentMethod = uiState.paymentMethod,
                            tag = uiState.tag,
                            notes = uiState.additionalNotes,
                            cashReceived = uiState.cashReceivedStr,
                            onAddProductToCart = { prod -> viewModel.addProductToCart(prod) },
                            onUpdateQuantity = { idx, qty -> viewModel.updateCartItemQuantity(idx, qty) },
                            onUpdatePrice = { idx, price -> viewModel.updateCartItemPrice(idx, price) },
                            onUpdateCambioFisico = { idx, cfQty -> viewModel.updateCartItemCambioFisico(idx, cfQty) },
                            onRemoveItem = { idx -> viewModel.removeCartItem(idx) },
                            onSetReturn = { idx, isReturn, reason -> viewModel.setItemAsReturn(idx, isReturn, reason) },
                            onSetExchange = { idx, isEx, type, repl, reason -> viewModel.setItemAsExchange(idx, isEx, type, repl, reason) },
                            onSelectClient = { c -> viewModel.selectClient(c) },
                            onSetPaymentStatus = { s -> viewModel.setPaymentStatus(s) },
                            onSetPaymentMethod = { m -> viewModel.setPaymentMethod(m) },
                            onSetTag = { t -> viewModel.setTag(t) },
                            onSetNotes = { n -> viewModel.setAdditionalNotes(n) },
                            onSetCashReceived = { c -> viewModel.setCashReceived(c) },
                            onClearCart = { viewModel.clearCart() },
                            onSaveSale = { viewModel.completeSale() },
                            onQuickAddClient = { dni, name -> viewModel.registerClient(dni, name, "Ruta", "") }
                        )
                    }

                    AppScreen.SALES_HISTORY -> {
                        SalesHistoryScreen(
                            sales = sales,
                            allDetails = saleDetails,
                            onOpenTicket = { sale -> viewModel.openTicket(sale) }
                        )
                    }

                    AppScreen.REPORTS_GRID -> {
                        ReportsGridScreen(
                            onSelectReport = { reportId -> viewModel.openReportDetail(reportId) },
                            onExportAllPdf = {
                                exportAllReportsSummary(context, sales, expenses, products)
                            }
                        )
                    }

                    AppScreen.REPORT_DETAIL -> {
                        ReportDetailScreen(
                            reportId = selectedReportId,
                            sales = sales,
                            saleDetails = saleDetails,
                            products = products,
                            clients = clients,
                            expenses = expenses,
                            returns = returns,
                            onBack = { viewModel.navigateTo(AppScreen.REPORTS_GRID) }
                        )
                    }

                    AppScreen.WEB_ADMIN -> {
                        WebAdminDashboardScreen(
                            sales = sales,
                            expenses = expenses,
                            products = products,
                            onManualSync = { viewModel.performManualSync() }
                        )
                    }

                    AppScreen.ROUTE_SUMMARY -> {
                        RouteSummaryEndScreen(
                            employeeName = uiState.activeEmployee,
                            initialCash = uiState.initialCash,
                            sales = sales,
                            expenses = expenses,
                            returns = returns,
                            currentSession = currentSession,
                            isSyncing = isSyncing,
                            syncSummary = syncSummary,
                            onManualSync = { viewModel.performManualSync() },
                            onCloseDayAndSync = { viewModel.closeDayAndSync() },
                            onExportPdf = {
                                exportAllReportsSummary(context, sales, expenses, products)
                            },
                            onStartNewRoute = {
                                viewModel.navigateTo(AppScreen.ROUTE_START)
                            }
                        )
                    }

                    AppScreen.INVENTORY -> {
                        InventoryStockScreen(
                            products = products,
                            onExportCsv = {
                                scope.launch {
                                    val csv = viewModel.getStockCsvContent()
                                    shareText(context, csv, "text/csv", "Stock_Daniisa.csv")
                                }
                            },
                            onAddNewProduct = { prod ->
                                viewModel.addNewProduct(prod)
                            }
                        )
                    }

                    AppScreen.CLIENTS_EXPENSES -> {
                        ClientsAndExpensesScreen(
                            clients = clients,
                            expenses = expenses,
                            onRegisterClient = { dni, name, addr, phone, lat, lng ->
                                viewModel.registerClient(dni, name, addr, phone, lat, lng)
                            },
                            onRegisterExpense = { cat, amt, desc ->
                                viewModel.registerExpense(cat, amt, desc)
                            }
                        )
                    }

                    AppScreen.BUSINESS_INFO -> {
                        BusinessInfoScreen(
                            onSaveBusinessInfo = { name, contact, email, phone, rfc, design, format, addStatus, addLogo, addClient ->
                                viewModel.saveBusinessInfo(name, contact, email, phone, rfc, design, format, addStatus, addLogo, addClient)
                            }
                        )
                    }

                    AppScreen.SETTINGS_SYNC -> {
                        SyncAndSettingsScreen(
                            currentSpreadsheetId = spreadsheetId,
                            currentWebAppUrl = webAppUrl,
                            isSyncing = isSyncing,
                            syncSummary = syncSummary,
                            onSaveSpreadsheetId = { id -> viewModel.updateSpreadsheetId(id) },
                            onSaveWebAppUrl = { url -> viewModel.updateWebAppUrl(url) },
                            onManualSync = { viewModel.performManualSync() },
                            onExportMn = {
                                scope.launch {
                                    val json = viewModel.getMnBackupContent()
                                    shareText(context, json, "application/json", "daniisa_backup.mn")
                                }
                            },
                            onImportMnPrompt = {
                                Toast.makeText(context, "Listo para importar archivo .mn", Toast.LENGTH_SHORT).show()
                            },
                            onExportStockCsv = {
                                scope.launch {
                                    val csv = viewModel.getStockCsvContent()
                                    shareText(context, csv, "text/csv", "Stock_Daniisa.csv")
                                }
                            },
                            onExportSalesCsv = {
                                scope.launch {
                                    val csv = viewModel.getSalesCsvContent()
                                    shareText(context, csv, "text/csv", "Ventas_Daniisa.csv")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun shareText(context: android.content.Context, content: String, mimeType: String, title: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar $title"))
}

private fun exportAllReportsSummary(
    context: android.content.Context,
    sales: List<com.example.data.model.SaleHeaderEntity>,
    expenses: List<com.example.data.model.ExpenseEntity>,
    products: List<com.example.data.model.ProductEntity>
) {
    try {
        val totalVentas = sales.sumOf { it.total }
        val totalGastos = expenses.sumOf { it.monto }
        val cobrado = sales.filter { it.estadoPago == "PAGADO" }.sumOf { it.total }
        val porCobrar = sales.filter { it.estadoPago == "POR_COBRAR" }.sumOf { it.total }

        val pdfFile = com.example.util.PdfRouteSummaryGenerator.generateRouteSummaryPdf(
            context = context,
            employeeName = "Oliverth / Vendedor Daniisa",
            initialCash = 500.0,
            sales = sales,
            expenses = expenses,
            returns = emptyList()
        )

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Abrir Reporte PDF"))
    } catch (e: Exception) {
        val totalVentas = sales.sumOf { it.total }
        val totalGastos = expenses.sumOf { it.monto }
        val neto = totalVentas - totalGastos

        val text = """
            ========================================
            DISTRIBUIDORA DANIISA - REPORTE CONSOLIDADO
            ========================================
            Total Ventas Brutas: $ ${String.format(java.util.Locale.getDefault(), "%.2f", totalVentas)}
            Total Gastos de Ruta: $ ${String.format(java.util.Locale.getDefault(), "%.2f", totalGastos)}
            Recaudación Neta: $ ${String.format(java.util.Locale.getDefault(), "%.2f", neto)}
            Comprobantes Emitidos: ${sales.size}
            Productos en Stock: ${products.size}
            ========================================
            Generado en Android App Daniisa QASO ERP.
        """.trimIndent()

        shareText(context, text, "text/plain", "Reporte_Consolidado_Daniisa.txt")
    }
}
