package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.DaniisaRepository
import com.example.data.repository.SyncSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    ROUTE_START,
    SALES,
    SALES_HISTORY,
    REPORTS_GRID,
    REPORT_DETAIL,
    WEB_ADMIN,
    ROUTE_SUMMARY,
    INVENTORY,
    CLIENTS_EXPENSES,
    BUSINESS_INFO,
    SETTINGS_SYNC
}

data class DaniisaUiState(
    val currentScreen: AppScreen = AppScreen.ROUTE_START,
    val activeEmployee: String = "Juan Pérez",
    val activeEmployeeId: String = "EMP001",
    val isRouteActive: Boolean = false,
    val initialCash: Double = 0.0,
    val currentSession: RouteSessionEntity? = null,
    
    // Cart / Active Sale
    val cartItems: List<CartItem> = emptyList(),
    val selectedClient: ClientEntity = ClientEntity("00000000", "CLIENTE GENÉRICO", "Mostrador", "000000000"),
    val paymentStatus: String = "PAGADO", // PAGADO | POR_COBRAR
    val paymentMethod: String = "Efectivo",
    val tag: String = "",
    val additionalNotes: String = "",
    val cashReceivedStr: String = "",
    val productSearchQuery: String = "",
    
    // UI Dialogs
    val showChangeReturnDialog: Boolean = false
)

class DaniisaViewModel(
    private val repository: DaniisaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DaniisaUiState())
    val uiState: StateFlow<DaniisaUiState> = _uiState.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<ClientEntity>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SaleHeaderEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saleDetails: StateFlow<List<SaleDetailEntity>> = repository.allSaleDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val returns: StateFlow<List<ReturnEntity>> = repository.allReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSessionFlow: StateFlow<RouteSessionEntity?> = repository.currentSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // State for Dialogs and Reports
    private val _selectedSaleForTicket = MutableStateFlow<SaleHeaderEntity?>(null)
    val selectedSaleForTicket: StateFlow<SaleHeaderEntity?> = _selectedSaleForTicket.asStateFlow()

    private val _selectedReportId = MutableStateFlow("TRANSACCIONES_DIA")
    val selectedReportId: StateFlow<String> = _selectedReportId.asStateFlow()

    private val _syncSummary = MutableStateFlow<SyncSummary?>(null)
    val syncSummary: StateFlow<SyncSummary?> = _syncSummary.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _spreadsheetId = MutableStateFlow("1_isk9QDbGJYemT3eX0Si1ilspVpvjEhXa4CKEissb4g")
    val spreadsheetId: StateFlow<String> = _spreadsheetId.asStateFlow()

    private val _webAppUrl = MutableStateFlow("https://script.google.com/macros/s/AKfycbz_daniisa_endpoint/exec")
    val webAppUrl: StateFlow<String> = _webAppUrl.asStateFlow()

    private val _businessName = MutableStateFlow("DISTRIBUIDORA DANIISA")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    init {
        viewModelScope.launch {
            _spreadsheetId.value = repository.getSpreadsheetId()
            _webAppUrl.value = repository.getWebAppUrl()
            _businessName.value = repository.getConfig("NEGOCIO_NOMBRE", "DISTRIBUIDORA DANIISA")
            
            // Check if there is an active session
            currentSessionFlow.collect { session ->
                if (session != null && session.status == "ACTIVE") {
                    _uiState.update { 
                        it.copy(
                            isRouteActive = true,
                            activeEmployee = session.employeeName,
                            activeEmployeeId = session.employeeId,
                            initialCash = session.initialCash,
                            currentSession = session
                        )
                    }
                }
            }
        }
        
        // Add default product to cart on first load if empty to match screenshot
        viewModelScope.launch {
            products.collect { prods ->
                if (_uiState.value.cartItems.isEmpty() && prods.isNotEmpty()) {
                    val yogurt = prods.find { it.codigo == "PRD001" } ?: prods.first()
                    _uiState.update {
                        it.copy(cartItems = listOf(CartItem(product = yogurt, cantidad = 1.0, precioUnitario = yogurt.precioVenta)))
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // Route Session
    fun startRoute(employeeName: String, employeeId: String = "", initialCash: Double = 0.0) {
        viewModelScope.launch {
            val sessionId = repository.startRouteSession(employeeName, employeeId, initialCash)
            _uiState.update {
                it.copy(
                    activeEmployee = employeeName,
                    activeEmployeeId = employeeId,
                    isRouteActive = true,
                    initialCash = initialCash,
                    currentScreen = AppScreen.SALES
                )
            }
            showMessage("¡Jornada de ruta iniciada con éxito para $employeeName!")
        }
    }

    fun endRoute() {
        viewModelScope.launch {
            val session = _uiState.value.currentSession
            if (session != null) {
                repository.endRouteSession(session.id)
            }
            _uiState.update { 
                it.copy(
                    isRouteActive = false,
                    currentScreen = AppScreen.ROUTE_SUMMARY
                )
            }
            showMessage("Jornada finalizada. Por favor sincronice los datos con el servidor central.")
        }
    }

    // Cart Operations
    fun setProductSearchQuery(query: String) {
        _uiState.update { it.copy(productSearchQuery = query) }
    }

    fun addProductToCart(product: ProductEntity) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        val index = currentItems.indexOfFirst { it.product.codigo == product.codigo && !it.esDevolucion && !it.esCambio }
        if (index >= 0) {
            currentItems[index] = currentItems[index].copy(cantidad = currentItems[index].cantidad + 1)
        } else {
            currentItems.add(CartItem(product = product, cantidad = 1.0, precioUnitario = product.precioVenta))
        }
        _uiState.update { it.copy(cartItems = currentItems) }
    }

    fun updateCartItemQuantity(index: Int, newQuantity: Double) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        if (index in currentItems.indices) {
            if (newQuantity <= 0) {
                currentItems.removeAt(index)
            } else {
                currentItems[index] = currentItems[index].copy(cantidad = newQuantity)
            }
            _uiState.update { it.copy(cartItems = currentItems) }
        }
    }

    fun updateCartItemPrice(index: Int, newPrice: Double) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = currentItems[index].copy(precioUnitario = newPrice)
            _uiState.update { it.copy(cartItems = currentItems) }
        }
    }

    fun updateCartItemCambioFisico(index: Int, cfQty: Double) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        if (index in currentItems.indices) {
            currentItems[index] = currentItems[index].copy(cambioFisicoQty = maxOf(0.0, cfQty))
            _uiState.update { it.copy(cartItems = currentItems) }
        }
    }

    fun removeCartItem(index: Int) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _uiState.update { it.copy(cartItems = currentItems) }
        }
    }

    fun clearCart() {
        _uiState.update { 
            it.copy(
                cartItems = emptyList(),
                cashReceivedStr = "",
                tag = "",
                additionalNotes = ""
            ) 
        }
    }

    // Change & Return
    fun setItemAsReturn(index: Int, isReturn: Boolean, reason: String = "") {
        val currentItems = _uiState.value.cartItems.toMutableList()
        if (index in currentItems.indices) {
            val item = currentItems[index]
            currentItems[index] = item.copy(
                esDevolucion = isReturn,
                esCambio = false,
                motivoCambio = reason
            )
            _uiState.update { it.copy(cartItems = currentItems) }
        }
    }

    fun setItemAsExchange(
        index: Int,
        isExchange: Boolean,
        exchangeType: String = "MISMO", // MISMO | OTRO
        replacementProduct: ProductEntity? = null,
        reason: String = ""
    ) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        if (index in currentItems.indices) {
            val item = currentItems[index]
            currentItems[index] = item.copy(
                esCambio = isExchange,
                esDevolucion = false,
                cambioTipo = exchangeType,
                productoCambioSeleccionado = replacementProduct,
                motivoCambio = reason
            )
            _uiState.update { it.copy(cartItems = currentItems) }
        }
    }

    // Payment and Client
    fun selectClient(client: ClientEntity) {
        _uiState.update { it.copy(selectedClient = client) }
    }

    fun setPaymentStatus(status: String) { // PAGADO | POR_COBRAR
        _uiState.update { it.copy(paymentStatus = status) }
    }

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun setTag(tag: String) {
        _uiState.update { it.copy(tag = tag) }
    }

    fun setAdditionalNotes(notes: String) {
        _uiState.update { it.copy(additionalNotes = notes) }
    }

    fun setCashReceived(cash: String) {
        _uiState.update { it.copy(cashReceivedStr = cash) }
    }

    // Process Sale & Save
    fun completeSale() {
        val items = _uiState.value.cartItems
        if (items.isEmpty()) {
            showMessage("Agregue al menos un producto a la venta.")
            return
        }

        viewModelScope.launch {
            val received = _uiState.value.cashReceivedStr.toDoubleOrNull() ?: 0.0
            val res = repository.processSale(
                items = items,
                client = _uiState.value.selectedClient,
                employeeName = _uiState.value.activeEmployee,
                paymentStatus = _uiState.value.paymentStatus,
                paymentMethod = _uiState.value.paymentMethod,
                receivedCash = received,
                tag = _uiState.value.tag,
                notes = _uiState.value.additionalNotes
            )

            if (res.isSuccess) {
                val sale = res.getOrNull()
                _selectedSaleForTicket.value = sale
                clearCart()
                showMessage("¡Venta ${sale?.nDoc} guardada exitosamente!")
            } else {
                showMessage("Error al guardar venta: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    fun openTicket(sale: SaleHeaderEntity) {
        _selectedSaleForTicket.value = sale
    }

    fun closeTicket() {
        _selectedSaleForTicket.value = null
    }

    fun openReportDetail(reportId: String) {
        _selectedReportId.value = reportId
        navigateTo(AppScreen.REPORT_DETAIL)
    }

    // Sync Operations
    fun performManualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val summary = repository.syncAllPendingData()
                _syncSummary.value = summary
                if (summary.errors.isEmpty()) {
                    showMessage("Sincronización completada con éxito. ${summary.totalSyncedSales} ventas sincronizadas.")
                } else {
                    showMessage("Sincronización finalizada con algunas advertencias.")
                }
            } catch (e: Exception) {
                showMessage("Error durante la sincronización: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun closeDayAndSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val summary = repository.closeDayAndSyncBatch()
                _syncSummary.value = summary
                _uiState.update { it.copy(isRouteActive = false) }
                if (summary.errors.isEmpty()) {
                    showMessage("¡Cierre de jornada completado y sincronizado exitosamente con el servidor central!")
                } else {
                    showMessage("Cierre guardado localmente con éxito.")
                }
            } catch (e: Exception) {
                showMessage("Cierre completado localmente: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Settings
    fun updateSpreadsheetId(newId: String) {
        viewModelScope.launch {
            repository.setSpreadsheetId(newId)
            _spreadsheetId.value = newId
            showMessage("ID de Hoja de Cálculo actualizado.")
        }
    }

    fun updateWebAppUrl(newUrl: String) {
        viewModelScope.launch {
            repository.setWebAppUrl(newUrl)
            _webAppUrl.value = newUrl
            showMessage("URL del Servidor Central actualizada.")
        }
    }

    // Export / Import
    suspend fun getMnBackupContent(): String {
        return repository.exportMnBackup()
    }

    fun importMnBackupContent(content: String) {
        viewModelScope.launch {
            val res = repository.importMnBackup(content)
            if (res.isSuccess) {
                showMessage(res.getOrNull() ?: "Backup restaurado.")
            } else {
                showMessage("Error al importar: ${res.exceptionOrNull()?.message}")
            }
        }
    }

    suspend fun getStockCsvContent(): String {
        return repository.exportStockCsv()
    }

    suspend fun getSalesCsvContent(): String {
        return repository.exportSalesCsv()
    }

    fun registerExpense(categoria: String, monto: Double, descripcion: String) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertExpense(
                ExpenseEntity(
                    fecha = today,
                    categoria = categoria,
                    descripcion = descripcion,
                    monto = monto,
                    responsable = _uiState.value.activeEmployee
                )
            )
            showMessage("Gasto de $ $monto registrado correctamente.")
        }
    }

    fun registerClient(
        dniRuc: String,
        nombres: String,
        direccion: String,
        telefono: String,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        viewModelScope.launch {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            repository.insertClient(
                ClientEntity(
                    dniRuc = dniRuc,
                    nombres = nombres,
                    direccion = direccion,
                    telefono = telefono,
                    fechaRegistro = now,
                    latitud = latitud,
                    longitud = longitud
                )
            )
            showMessage("Cliente $nombres registrado con geolocalización.")
        }
    }

    fun addNewProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.insertProduct(product)
            showMessage("Producto ${product.nombre} añadido al inventario.")
        }
    }

    fun saveBusinessInfo(
        name: String,
        contact: String,
        email: String,
        phone: String,
        rfc: String,
        designType: String,
        formatType: String,
        addStatus: Boolean,
        addLogo: Boolean,
        addClientInfo: Boolean
    ) {
        viewModelScope.launch {
            repository.updateConfig("NEGOCIO_NOMBRE", name)
            repository.updateConfig("NEGOCIO_CONTACTO", contact)
            repository.updateConfig("NEGOCIO_EMAIL", email)
            repository.updateConfig("NEGOCIO_TELEFONO", phone)
            repository.updateConfig("NEGOCIO_RFC", rfc)
            repository.updateConfig("TICKET_DISENO", designType)
            repository.updateConfig("TICKET_FORMATO", formatType)
            repository.updateConfig("TICKET_STATUS", addStatus.toString())
            repository.updateConfig("TICKET_LOGO", addLogo.toString())
            repository.updateConfig("TICKET_CLIENTE_INFO", addClientInfo.toString())
            _businessName.value = name
            showMessage("Datos de 'Mi Negocio' actualizados correctamente.")
        }
    }
}

class DaniisaViewModelFactory(
    private val repository: DaniisaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DaniisaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DaniisaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
