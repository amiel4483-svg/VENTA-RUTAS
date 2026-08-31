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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun syncSaleToServer(
        webAppUrl: String,
        saleHeader: SaleHeaderEntity,
        saleDetails: List<SaleDetailEntity>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("action", "registrarVenta")
                put("tipoDoc", saleHeader.tipoDoc)
                put("fecha", saleHeader.fecha)
                put("empleado", saleHeader.empleado)
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
        try {
            val payload = JSONObject().apply {
                put("action", "registrarDevolucion")
                put("nDocOrig", returnEntity.nDocOrig)
                put("cliente", returnEntity.cliente)
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
            Result.success(responseString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncExpenseToServer(
        webAppUrl: String,
        expense: ExpenseEntity
    ): Result<String> = withContext(Dispatchers.IO) {
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
            Result.success(responseString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
