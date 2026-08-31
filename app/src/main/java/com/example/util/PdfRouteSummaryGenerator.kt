package com.example.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ReturnEntity
import com.example.data.model.SaleHeaderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfRouteSummaryGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateRouteSummaryPdf(
        context: Context,
        employeeName: String,
        initialCash: Double,
        sales: List<SaleHeaderEntity>,
        expenses: List<ExpenseEntity>,
        returns: List<ReturnEntity>,
        businessName: String = "DISTRIBUIDORA DANIISA"
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val totalVentas = sales.sumOf { it.total }
        val totalCobrado = sales.filter { it.estadoPago == "PAGADO" }.sumOf { it.total }
        val totalPorCobrar = sales.filter { it.estadoPago == "POR_COBRAR" }.sumOf { it.total }
        val totalGastos = expenses.sumOf { it.monto }
        val efectivoTeorico = initialCash + totalCobrado - totalGastos

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(Color.WHITE)

        // Top Header Banner (Daniisa Navy & Cyan)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B2A4A") }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, headerPaint)

        val cyanStripe = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00A3E0") }
        canvas.drawRect(0f, 110f, PAGE_WIDTH.toFloat(), 116f, cyanStripe)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(businessName, 30f, 48f, paint)

        paint.textSize = 14f
        paint.color = Color.parseColor("#80D0F5")
        canvas.drawText("CIERRE DE RUTA Y LIQUIDACIÓN DIARIA", 30f, 72f, paint)

        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        paint.textSize = 11f
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Fecha: $dateStr", PAGE_WIDTH - 30f, 48f, paint)
        canvas.drawText("Vendedor: $employeeName", PAGE_WIDTH - 30f, 72f, paint)

        var y = 150f

        // Financial Summary Box
        val boxRect = RectF(30f, y, PAGE_WIDTH - 30f, y + 175f)
        paint.color = Color.parseColor("#F8FAFC")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(boxRect, 8f, 8f, paint)

        paint.color = Color.parseColor("#CBD5E1")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        canvas.drawRoundRect(boxRect, 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#1B2A4A")
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("RESUMEN FINANCIERO DE LA JORNADA", 45f, y + 28f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val rowYStart = y + 55f

        drawSummaryRow(canvas, "Fondo de Caja Inicial:", String.format(Locale.getDefault(), "$ %.2f", initialCash), 45f, rowYStart, PAGE_WIDTH - 45f)
        drawSummaryRow(canvas, "Total Ventas Brutas (${sales.size} comprobantes):", String.format(Locale.getDefault(), "$ %.2f", totalVentas), 45f, rowYStart + 22f, PAGE_WIDTH - 45f)
        drawSummaryRow(canvas, "Cobrado de Contado / Transferencias:", String.format(Locale.getDefault(), "$ %.2f", totalCobrado), 45f, rowYStart + 44f, PAGE_WIDTH - 45f)
        drawSummaryRow(canvas, "Créditos por Cobrar:", String.format(Locale.getDefault(), "$ %.2f", totalPorCobrar), 45f, rowYStart + 66f, PAGE_WIDTH - 45f)
        drawSummaryRow(canvas, "Gastos de Ruta Pagados (${expenses.size}):", String.format(Locale.getDefault(), "- $ %.2f", totalGastos), 45f, rowYStart + 88f, PAGE_WIDTH - 45f)

        // Highlight Cash to Liquidate
        paint.color = Color.parseColor("#00A3E0")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EFECTIVO A ENTREGAR (ARQUEO):", 45f, rowYStart + 112f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(String.format(Locale.getDefault(), "$ %.2f", efectivoTeorico), PAGE_WIDTH - 45f, rowYStart + 112f, paint)

        y += 205f

        // Table of Sales Header
        paint.color = Color.parseColor("#1B2A4A")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("ÚLTIMOS COMPROBANTES DE VENTA", 30f, y, paint)

        y += 15f
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E2E8F0") }
        canvas.drawRect(30f, y, PAGE_WIDTH - 30f, y + 24f, tableHeaderPaint)

        paint.color = Color.parseColor("#334155")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("N° DOC", 36f, y + 16f, paint)
        canvas.drawText("CLIENTE", 110f, y + 16f, paint)
        canvas.drawText("ESTADO", 320f, y + 16f, paint)
        canvas.drawText("FORMA PAGO", 400f, y + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", PAGE_WIDTH - 36f, y + 16f, paint)

        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (sale in sales.take(12)) {
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.parseColor("#1E293B")
            canvas.drawText(sale.nDoc, 36f, y + 15f, paint)
            val clientName = if (sale.nombreTercero.length > 28) sale.nombreTercero.take(28) + "..." else sale.nombreTercero
            canvas.drawText(clientName, 110f, y + 15f, paint)
            canvas.drawText(sale.estadoPago, 320f, y + 15f, paint)
            canvas.drawText(sale.formaPago, 400f, y + 15f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(String.format(Locale.getDefault(), "$ %.2f", sale.total), PAGE_WIDTH - 36f, y + 15f, paint)

            y += 20f
        }

        // Signatures area at bottom
        val sigY = PAGE_HEIGHT - 90f
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 1f
        }
        canvas.drawLine(60f, sigY, 220f, sigY, linePaint)
        canvas.drawLine(PAGE_WIDTH - 220f, sigY, PAGE_WIDTH - 60f, sigY, linePaint)

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 10f
        paint.color = Color.parseColor("#64748B")
        canvas.drawText("Firma del Vendedor", 140f, sigY + 16f, paint)
        canvas.drawText("Firma de Administración / Caja", PAGE_WIDTH - 140f, sigY + 16f, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Cierre_Ruta_Daniisa.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()

        return file
    }

    private fun drawSummaryRow(canvas: Canvas, label: String, value: String, xStart: Float, y: Float, xEnd: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 11.5f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(label, xStart, y, paint)
        paint.textAlign = Paint.Align.RIGHT
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, xEnd, y, paint)
    }
}
