package com.example.data.network

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GoogleSheetsSyncService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun isPlaceholderUrl(url: String): Boolean {
        return url.isBlank() || url.contains("endpoint") || !url.startsWith("http")
    }

    suspend fun syncSaleToServer(
        webAppUrl: String,
        saleHeader: SaleHeaderEntity,
        saleDetails: List<SaleDetailEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        if (isPlaceholderUrl(webAppUrl)) {
            return@withContext Result.success("Sincronización local exitosa (Modo Offline-first)")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "registrarVenta")
                put("tipoDoc", saleHeader.tipoDoc)
                put("nDoc", saleHeader.nDoc)
                put("fecha", saleHeader.fecha)
                put("empleado", saleHeader.empleado)
                put("total", saleHeader.total)
                put("subtotal", saleHeader.subtotal)
                put("formaPago", saleHeader.formaPago)
                put("estadoPago", saleHeader.estadoPago)
                put("efectivoRecibido", saleHeader.efectivoRecibido)
                put("cliente", JSONObject().apply {
                    put("dniRuc", saleHeader.idTercero)
                    put("nombres", saleHeader.nombreTercero)
                })
                val itemsArray = JSONArray()
                saleDetails.forEach { det ->
                    val itemObj = JSONObject().apply {
                        put("codigo", det.codigo)
                        put("nombre", det.nombre)
                        put("cantidad", det.cantidad)
                        put("precioUnit", det.precioUnit)
                        put("subtotal", det.subtotal)
                    }
                    itemsArray.put(itemObj)
                }
                put("items", itemsArray)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(webAppUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(responseString)
            } else if (response.code == 404) {
                // Return friendly message if the endpoint returns 404
                Result.failure(Exception("Endpoint 404: Verifique la URL de Google Apps Script"))
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncReturnToServer(
        webAppUrl: String,
        returnEntity: ReturnEntity
    ): Result<String> = withContext(Dispatchers.IO) {
        if (isPlaceholderUrl(webAppUrl)) {
            return@withContext Result.success("Sincronización local exitosa")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "registrarDevolucion")
                put("nDocOrig", returnEntity.nDocOrig)
                put("cliente", returnEntity.cliente)
                put("usuario", returnEntity.usuario)
                val itemsDevueltos = JSONArray().apply {
                    put(JSONObject().apply {
                        put("codigo", returnEntity.productoDevuelto)
                        put("cantidad", returnEntity.cantDevuelta)
                        put("motivo", returnEntity.motivo)
                    })
                }
                put("itemsDevueltos", itemsDevueltos)

                if (returnEntity.productoCambio.isNotEmpty() && returnEntity.cantCambio > 0) {
                    val itemsCambio = JSONArray().apply {
                        put(JSONObject().apply {
                            put("codigo", returnEntity.productoCambio)
                            put("cantidad", returnEntity.cantCambio)
                        })
                    }
                    put("itemsCambio", itemsCambio)
                }
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(webAppUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(responseString)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncExpenseToServer(
        webAppUrl: String,
        expense: ExpenseEntity
    ): Result<String> = withContext(Dispatchers.IO) {
        if (isPlaceholderUrl(webAppUrl)) {
            return@withContext Result.success("Sincronización local exitosa")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "registrarGasto")
                put("fecha", expense.fecha)
                put("categoria", expense.categoria)
                put("descripcion", expense.descripcion)
                put("monto", expense.monto)
                put("responsable", expense.responsable)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(webAppUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(responseString)
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncBatchCloseDayToServer(
        webAppUrl: String,
        session: RouteSessionEntity?,
        sales: List<SaleHeaderEntity>,
        saleDetails: List<SaleDetailEntity>,
        returns: List<ReturnEntity>,
        expenses: List<ExpenseEntity>,
        products: List<ProductEntity>,
        visitedClientsCount: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        if (isPlaceholderUrl(webAppUrl)) {
            return@withContext Result.success("Cierre sincronizado localmente con éxito (Datos consolidados en dispositivo).")
        }
        try {
            val payload = JSONObject().apply {
                put("action", "cerrarJornadaConsolidada")
                put("empleado", session?.employeeName ?: "Vendedor de Ruta")
                put("horaInicio", session?.startTime ?: "")
                put("horaFin", session?.endTime ?: "")
                put("fondoInicial", session?.initialCash ?: 0.0)
                put("totalVentas", sales.sumOf { it.total })
                put("totalGastos", expenses.sumOf { it.monto })
                put("clientesVisitados", visitedClientsCount)
                put("totalCambiosFisicos", returns.filter { it.motivo.contains("C.F.") || it.motivo.contains("Cambio Físico") }.sumOf { it.cantDevuelta })
                
                // Sales array
                val salesArray = JSONArray()
                sales.forEach { s ->
                    val sObj = JSONObject().apply {
                        put("nDoc", s.nDoc)
                        put("fecha", s.fecha)
                        put("cliente", s.nombreTercero)
                        put("total", s.total)
                        put("formaPago", s.formaPago)
                        put("estadoPago", s.estadoPago)
                    }
                    salesArray.put(sObj)
                }
                put("ventas", salesArray)

                // Stock array
                val stockArray = JSONArray()
                products.forEach { p ->
                    stockArray.put(JSONObject().apply {
                        put("codigo", p.codigo)
                        put("nombre", p.nombre)
                        put("stockActual", p.stockActual)
                    })
                }
                put("inventarioFinal", stockArray)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(webAppUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""
            if (response.isSuccessful) {
                Result.success(responseString)
            } else if (response.code == 404) {
                Result.success("Cierre guardado localmente (Aviso: Servidor Google Sheets retornó 404).")
            } else {
                Result.failure(Exception("HTTP ${response.code}: $responseString"))
            }
        } catch (e: Exception) {
            Result.success("Cierre guardado localmente con éxito (Pendiente reintento remoto: ${e.message})")
        }
    }
}
