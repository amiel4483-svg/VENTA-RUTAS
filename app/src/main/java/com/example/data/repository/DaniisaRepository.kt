package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.network.GoogleSheetsSyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DaniisaRepository(
    private val database: AppDatabase,
    private val syncService: GoogleSheetsSyncService = GoogleSheetsSyncService()
) {
    val allProducts: Flow<List<ProductEntity>> = database.productDao().getAllProducts()
    val allClients: Flow<List<ClientEntity>> = database.clientDao().getAllClients()
    val allEmployees: Flow<List<EmployeeEntity>> = database.employeeDao().getAllEmployees()
    val allSales: Flow<List<SaleHeaderEntity>> = database.saleDao().getAllSales()
    val allSaleDetails: Flow<List<SaleDetailEntity>> = database.saleDao().getAllSaleDetailsFlow()
    val allExpenses: Flow<List<ExpenseEntity>> = database.expenseDao().getAllExpenses()
    val allReturns: Flow<List<ReturnEntity>> = database.returnDao().getAllReturns()
    val allMovements: Flow<List<MovementEntity>> = database.movementDao().getAllMovements()
    val currentSession: Flow<RouteSessionEntity?> = database.routeSessionDao().getCurrentSession()
    val appConfig: Flow<List<AppConfigEntity>> = database.configDao().getAllConfig()

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getNowTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    suspend fun getSpreadsheetId(): String {
        return database.configDao().getConfigValue("SPREADSHEET_ID") ?: "1_isk9QDbGJYemT3eX0Si1ilspVpvjEhXa4CKEissb4g"
    }

    suspend fun setSpreadsheetId(id: String) {
        database.configDao().setConfigValue(AppConfigEntity("SPREADSHEET_ID", id.trim()))
    }

    suspend fun getWebAppUrl(): String {
        return database.configDao().getConfigValue("WEB_APP_URL") ?: "https://script.google.com/macros/s/AKfycbz_daniisa_endpoint/exec"
    }

    suspend fun setWebAppUrl(url: String) {
        database.configDao().setConfigValue(AppConfigEntity("WEB_APP_URL", url.trim()))
    }

    suspend fun updateConfig(key: String, value: String) {
        database.configDao().setConfigValue(AppConfigEntity(key, value))
    }

    suspend fun getConfig(key: String, default: String = ""): String {
        return database.configDao().getConfigValue(key) ?: default
    }

    suspend fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return database.productDao().searchProducts(query)
    }

    suspend fun insertProduct(product: ProductEntity) {
        database.productDao().insertProduct(product)
    }

    suspend fun insertClient(client: ClientEntity) {
        database.clientDao().insertClient(client)
    }

    suspend fun insertExpense(expense: ExpenseEntity) {
        database.expenseDao().insertExpense(expense)
    }

    suspend fun startRouteSession(employeeName: String, employeeId: String = "", initialCash: Double = 0.0): Long {
        val now = getNowTimestamp()
        val session = RouteSessionEntity(
            employeeName = employeeName,
            employeeId = employeeId,
            startTime = now,
            initialCash = initialCash,
            status = "ACTIVE"
        )
        return database.routeSessionDao().insertSession(session)
    }

    suspend fun endRouteSession(sessionId: Long): RouteSessionEntity? {
        val active = database.routeSessionDao().getActiveSession() ?: return null
        val sales = database.saleDao().getAllSales().first()
        val totalAmount = sales.filter { it.empleado == active.employeeName || active.employeeName.isEmpty() }
            .sumOf { it.total }
        val updated = active.copy(
            endTime = getNowTimestamp(),
            status = "COMPLETED",
            totalSales = totalAmount,
            totalTransactions = sales.size
        )
        database.routeSessionDao().updateSession(updated)
        return updated
    }

    suspend fun getNextDocNumber(): String {
        val count = database.saleDao().getSalesCount()
        return "T-" + String.format(Locale.getDefault(), "%03d", count + 1)
    }

    suspend fun processSale(
        items: List<CartItem>,
        client: ClientEntity,
        employeeName: String,
        paymentStatus: String, // PAGADO | POR_COBRAR
        paymentMethod: String,
        receivedCash: Double,
        tag: String = "",
        notes: String = ""
    ): Result<SaleHeaderEntity> = withContext(Dispatchers.IO) {
        try {
            val nDoc = getNextDocNumber()
            val today = getTodayDate()
            val now = getNowTimestamp()
            val taxRate = (database.configDao().getConfigValue("IMPUESTO_PORCENTAJE")?.toDoubleOrNull() ?: 18.0) / 100.0

            var subtotalSum = 0.0
            val detailsList = mutableListOf<SaleDetailEntity>()

            // Process each item
            for (item in items) {
                val itemSubtotal = item.subtotal
                subtotalSum += itemSubtotal

                detailsList.add(
                    SaleDetailEntity(
                        nDoc = nDoc,
                        codigo = item.product.codigo,
                        nombre = item.product.nombre + if (item.esDevolucion) " (DEVOLUCIÓN)" else if (item.esCambio) " (CAMBIO)" else "",
                        cantidad = item.cantidad,
                        precioUnit = item.precioUnitario,
                        subtotal = itemSubtotal
                    )
                )

                // Stock management
                val currentProduct = database.productDao().getProductByCode(item.product.codigo)
                val currentStock = currentProduct?.stockActual ?: 0.0

                if (item.esDevolucion) {
                    // Returned item increases stock
                    val newStock = currentStock + item.cantidad
                    database.productDao().updateStock(item.product.codigo, newStock)
                    database.movementDao().insertMovement(
                        MovementEntity(
                            codigo = item.product.codigo,
                            fecha = today,
                            tipo = "DEVOLUCION",
                            cantidad = item.cantidad,
                            usuario = employeeName,
                            timestamp = now,
                            observaciones = "Devolución en venta $nDoc: ${item.motivoCambio.ifEmpty { "Cliente devolvió producto" }}",
                            stockResultante = newStock,
                            docRef = nDoc
                        )
                    )
                    database.returnDao().insertReturn(
                        ReturnEntity(
                            fecha = today,
                            nDocOrig = nDoc,
                            cliente = client.nombres,
                            productoDevuelto = item.product.codigo,
                            cantDevuelta = item.cantidad,
                            motivo = item.motivoCambio,
                            usuario = employeeName
                        )
                    )
                } else if (item.esCambio) {
                    // Exchange: return current item + dispense replacement item
                    val newStockReturned = currentStock + item.cantidad
                    database.productDao().updateStock(item.product.codigo, newStockReturned)
                    database.movementDao().insertMovement(
                        MovementEntity(
                            codigo = item.product.codigo,
                            fecha = today,
                            tipo = "DEVOLUCION",
                            cantidad = item.cantidad,
                            usuario = employeeName,
                            timestamp = now,
                            observaciones = "Cambio devuelto $nDoc",
                            stockResultante = newStockReturned,
                            docRef = nDoc
                        )
                    )

                    val exchangeProduct = item.productoCambioSeleccionado ?: item.product
                    val exchangeCurrent = database.productDao().getProductByCode(exchangeProduct.codigo)
                    val newExchangeStock = (exchangeCurrent?.stockActual ?: 0.0) - item.cantidad
                    database.productDao().updateStock(exchangeProduct.codigo, maxOf(0.0, newExchangeStock))
                    database.movementDao().insertMovement(
                        MovementEntity(
                            codigo = exchangeProduct.codigo,
                            fecha = today,
                            tipo = "VENTA_CAMBIO",
                            cantidad = item.cantidad,
                            usuario = employeeName,
                            timestamp = now,
                            observaciones = "Producto entregado por cambio en $nDoc",
                            stockResultante = maxOf(0.0, newExchangeStock),
                            docRef = nDoc
                        )
                    )

                    database.returnDao().insertReturn(
                        ReturnEntity(
                            fecha = today,
                            nDocOrig = nDoc,
                            cliente = client.nombres,
                            productoDevuelto = item.product.codigo,
                            cantDevuelta = item.cantidad,
                            motivo = "Cambio por ${exchangeProduct.nombre} (${item.motivoCambio})",
                            productoCambio = exchangeProduct.codigo,
                            cantCambio = item.cantidad,
                            usuario = employeeName
                        )
                    )
                } else {
                    // Regular sale: reduce stock
                    val newStock = maxOf(0.0, currentStock - item.cantidad)
                    database.productDao().updateStock(item.product.codigo, newStock)
                    database.movementDao().insertMovement(
                        MovementEntity(
                            codigo = item.product.codigo,
                            fecha = today,
                            tipo = "VENTA",
                            cantidad = item.cantidad,
                            usuario = employeeName,
                            timestamp = now,
                            observaciones = "Venta $nDoc a ${client.nombres}",
                            stockResultante = newStock,
                            docRef = nDoc
                        )
                    )
                }
            }

            val impuesto = subtotalSum * taxRate
            val total = subtotalSum + impuesto
            val cambio = if (receivedCash > total) receivedCash - total else 0.0

            val saleHeader = SaleHeaderEntity(
                nDoc = nDoc,
                tipoDoc = "TICKET",
                tipoOp = "VENTA",
                fecha = today,
                idTercero = client.dniRuc,
                nombreTercero = client.nombres,
                subtotal = subtotalSum,
                impuesto = impuesto,
                total = total,
                usuario = employeeName,
                timestamp = now,
                empleado = employeeName,
                efectivoRecibido = receivedCash,
                cambio = cambio,
                estadoPago = paymentStatus,
                formaPago = paymentMethod,
                etiqueta = tag,
                infoAdicional = notes,
                syncStatus = "PENDIENTE"
            )

            database.saleDao().insertSaleHeader(saleHeader)
            database.saleDao().insertSaleDetails(detailsList)

            // Attempt online sync in background
            val webAppUrl = getWebAppUrl()
            if (webAppUrl.isNotEmpty() && !webAppUrl.contains("endpoint")) {
                try {
                    val syncRes = syncService.syncSaleToServer(webAppUrl, saleHeader, detailsList)
                    if (syncRes.isSuccess) {
                        database.saleDao().markSaleAsSynced(nDoc)
                    }
                } catch (_: Exception) {
                    // Stays PENDIENTE for manual sync
                }
            }

            Result.success(saleHeader)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllPendingData(): SyncSummary = withContext(Dispatchers.IO) {
        val pendingSales = database.saleDao().getPendingSales()
        val pendingReturns = database.returnDao().getPendingReturns()
        val pendingExpenses = database.expenseDao().getPendingExpenses()
        val webAppUrl = getWebAppUrl()

        var syncedSales = 0
        var syncedReturns = 0
        var syncedExpenses = 0
        val errors = mutableListOf<String>()

        for (sale in pendingSales) {
            val details = database.saleDao().getDetailsForSale(sale.nDoc)
            val res = syncService.syncSaleToServer(webAppUrl, sale, details)
            if (res.isSuccess) {
                database.saleDao().markSaleAsSynced(sale.nDoc)
                syncedSales++
            } else {
                errors.add("Error venta ${sale.nDoc}: ${res.exceptionOrNull()?.message ?: "Error"}")
            }
        }

        for (ret in pendingReturns) {
            val res = syncService.syncReturnToServer(webAppUrl, ret)
            if (res.isSuccess) {
                database.returnDao().markReturnAsSynced(ret.id)
                syncedReturns++
            }
        }

        for (exp in pendingExpenses) {
            val res = syncService.syncExpenseToServer(webAppUrl, exp)
            if (res.isSuccess) {
                database.expenseDao().markExpenseAsSynced(exp.id)
                syncedExpenses++
            }
        }

        SyncSummary(
            totalSyncedSales = syncedSales,
            totalSyncedReturns = syncedReturns,
            totalSyncedExpenses = syncedExpenses,
            pendingSalesCount = pendingSales.size - syncedSales,
            errors = errors
        )
    }

    // Export .mn Database Backup (JSON schema)
    suspend fun exportMnBackup(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("format", "DANIISA_MN_BACKUP")
        root.put("version", "6.0_funcional")
        root.put("timestamp", getNowTimestamp())
        root.put("spreadsheetId", getSpreadsheetId())

        val products = database.productDao().getAllProducts().first()
        val prodArray = JSONArray()
        products.forEach { p ->
            prodArray.put(JSONObject().apply {
                put("codigo", p.codigo)
                put("nombre", p.nombre)
                put("unidad", p.unidad)
                put("grupo", p.grupo)
                put("stockMin", p.stockMin)
                put("precioCompra", p.precioCompra)
                put("precioVenta", p.precioVenta)
                put("stockActual", p.stockActual)
            })
        }
        root.put("productos", prodArray)

        val clients = database.clientDao().getAllClients().first()
        val clientArray = JSONArray()
        clients.forEach { c ->
            clientArray.put(JSONObject().apply {
                put("dniRuc", c.dniRuc)
                put("nombres", c.nombres)
                put("direccion", c.direccion)
                put("telefono", c.telefono)
            })
        }
        root.put("clientes", clientArray)

        val sales = database.saleDao().getAllSales().first()
        val salesArray = JSONArray()
        sales.forEach { s ->
            val details = database.saleDao().getDetailsForSale(s.nDoc)
            val sObj = JSONObject().apply {
                put("nDoc", s.nDoc)
                put("fecha", s.fecha)
                put("total", s.total)
                put("subtotal", s.subtotal)
                put("cliente", s.nombreTercero)
                put("empleado", s.empleado)
                put("estadoPago", s.estadoPago)
                val detArray = JSONArray()
                details.forEach { d ->
                    detArray.put(JSONObject().apply {
                        put("codigo", d.codigo)
                        put("nombre", d.nombre)
                        put("cantidad", d.cantidad)
                        put("precioUnit", d.precioUnit)
                        put("subtotal", d.subtotal)
                    })
                }
                put("detalles", detArray)
            }
            salesArray.put(sObj)
        }
        root.put("ventas", salesArray)

        root.toString(2)
    }

    suspend fun importMnBackup(mnJsonContent: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(mnJsonContent)
            if (root.has("spreadsheetId")) {
                setSpreadsheetId(root.getString("spreadsheetId"))
            }

            var prodCount = 0
            if (root.has("productos")) {
                val prodArray = root.getJSONArray("productos")
                for (i in 0 until prodArray.length()) {
                    val p = prodArray.getJSONObject(i)
                    database.productDao().insertProduct(
                        ProductEntity(
                            codigo = p.getString("codigo"),
                            nombre = p.getString("nombre"),
                            unidad = p.optString("unidad", "Unidades"),
                            grupo = p.optString("grupo", "General"),
                            stockMin = p.optInt("stockMin", 5),
                            precioCompra = p.optDouble("precioCompra", 0.0),
                            precioVenta = p.optDouble("precioVenta", 0.0),
                            stockActual = p.optDouble("stockActual", 0.0),
                            fechaCreacion = getNowTimestamp()
                        )
                    )
                    prodCount++
                }
            }

            var clientCount = 0
            if (root.has("clientes")) {
                val clientArray = root.getJSONArray("clientes")
                for (i in 0 until clientArray.length()) {
                    val c = clientArray.getJSONObject(i)
                    database.clientDao().insertClient(
                        ClientEntity(
                            dniRuc = c.getString("dniRuc"),
                            nombres = c.getString("nombres"),
                            direccion = c.optString("direccion", ""),
                            telefono = c.optString("telefono", ""),
                            fechaRegistro = getNowTimestamp()
                        )
                    )
                    clientCount++
                }
            }

            Result.success("Importación exitosa: $prodCount productos, $clientCount clientes.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportStockCsv(): String = withContext(Dispatchers.IO) {
        val products = database.productDao().getAllProducts().first()
        val sb = java.lang.StringBuilder()
        sb.append("Código,Nombre,Unidad,Grupo,Stock Mínimo,Precio Compra,Precio Venta,Stock Actual,Estado\n")
        products.forEach { p ->
            val estado = if (p.stockActual <= 0) "Sin Stock" else if (p.stockActual <= p.stockMin) "Stock Bajo" else "Normal"
            sb.append("\"${p.codigo}\",\"${p.nombre}\",\"${p.unidad}\",\"${p.grupo}\",${p.stockMin},${p.precioCompra},${p.precioVenta},${p.stockActual},\"$estado\"\n")
        }
        sb.toString()
    }

    suspend fun exportSalesCsv(): String = withContext(Dispatchers.IO) {
        val sales = database.saleDao().getAllSales().first()
        val sb = java.lang.StringBuilder()
        sb.append("N° Doc,Fecha,Cliente,Documento,Empleado,Subtotal,Impuesto,Total,Estado Pago,Forma Pago,Sincronizado\n")
        sales.forEach { s ->
            sb.append("\"${s.nDoc}\",\"${s.fecha}\",\"${s.nombreTercero}\",\"${s.idTercero}\",\"${s.empleado}\",${s.subtotal},${s.impuesto},${s.total},\"${s.estadoPago}\",\"${s.formaPago}\",\"${s.syncStatus}\"\n")
        }
        sb.toString()
    }
}

data class SyncSummary(
    val totalSyncedSales: Int,
    val totalSyncedReturns: Int,
    val totalSyncedExpenses: Int,
    val pendingSalesCount: Int,
    val errors: List<String>
)
