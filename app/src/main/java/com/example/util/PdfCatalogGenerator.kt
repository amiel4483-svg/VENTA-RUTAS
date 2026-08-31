package com.example.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.ProductEntity
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfCatalogGenerator {

    private const val PAGE_WIDTH = 595 // A4 Standard width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 Standard height in points

    fun generateCatalogPdf(
        context: Context,
        products: List<ProductEntity>,
        businessName: String = "DISTRIBUIDORA DANIISA",
        phone: String = "(951) 249-4964",
        email: String = "eddyzun18@gmail.com"
    ): File {
        val pdfDocument = PdfDocument()
        var pageNumber = 1

        // ---------------- PAGE 1: COVER ----------------
        val coverPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val coverPage = pdfDocument.startPage(coverPageInfo)
        drawCoverPage(coverPage.canvas, businessName)
        pdfDocument.finishPage(coverPage)
        pageNumber++

        // ---------------- PAGES 2..N: PRODUCTS (3x3 Grid, 9 per page) ----------------
        val catalogProducts = if (products.isEmpty()) getSampleProducts() else products
        val chunks = catalogProducts.chunked(9)

        for (chunk in chunks) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            drawProductGridPage(page.canvas, chunk, pageNumber)
            pdfDocument.finishPage(page)
            pageNumber++
        }

        // ---------------- LAST PAGE: BACK COVER ----------------
        val backPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val backPage = pdfDocument.startPage(backPageInfo)
        drawBackCoverPage(backPage.canvas, phone, email)
        pdfDocument.finishPage(backPage)

        // Save PDF to cache directory
        val file = File(context.cacheDir, "Catalogo_Productos_Daniisa.pdf")
        val outputStream = FileOutputStream(file)
        pdfDocument.writeTo(outputStream)
        outputStream.close()
        pdfDocument.close()

        return file
    }

    private fun drawCoverPage(canvas: Canvas, businessName: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Geometric Top Shapes (Navy and Cyan)
        val navyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B2A4A") }
        val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00A3E0") }
        val lightBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#80D0F5") }

        // Top right polygons
        val path1 = Path().apply {
            moveTo(PAGE_WIDTH * 0.7f, 0f)
            lineTo(PAGE_WIDTH.toFloat(), 0f)
            lineTo(PAGE_WIDTH.toFloat(), 130f)
            lineTo(PAGE_WIDTH * 0.7f, 130f)
            close()
        }
        canvas.drawPath(path1, lightBluePaint)

        val path2 = Path().apply {
            moveTo(PAGE_WIDTH * 0.75f, 0f)
            lineTo(PAGE_WIDTH.toFloat(), 0f)
            lineTo(PAGE_WIDTH.toFloat(), 340f)
            lineTo(PAGE_WIDTH * 0.75f, 340f)
            close()
        }
        canvas.drawPath(path2, cyanPaint)

        // Top Left 20/30 Badge Box
        paint.color = Color.parseColor("#1B2A4A")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRect(70f, 90f, 160f, 220f, paint)

        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 52f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("20", 115f, 150f, paint)
        canvas.drawText("30", 115f, 205f, paint)

        // Center Shopping Cart in Navy Circle
        canvas.drawCircle(PAGE_WIDTH / 2f, 320f, 50f, navyPaint)
        // Shopping cart icon representation
        val cartPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(PAGE_WIDTH / 2f - 24f, 305f, PAGE_WIDTH / 2f + 24f, 328f, cartPaint)
        canvas.drawCircle(PAGE_WIDTH / 2f - 14f, 340f, 6f, cartPaint)
        canvas.drawCircle(PAGE_WIDTH / 2f + 14f, 340f, 6f, cartPaint)

        // Main Title: CATÁLOGO
        paint.color = Color.parseColor("#1B2A4A")
        paint.textSize = 58f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = 0.05f
        canvas.drawText("CATÁLOGO", PAGE_WIDTH / 2f, 490f, paint)

        // Subtitle: DE PRODUCTOS
        paint.textSize = 24f
        paint.letterSpacing = 0.2f
        canvas.drawText("DE PRODUCTOS", PAGE_WIDTH / 2f, 535f, paint)

        // Round Vintage Stamp: 100% CALIDAD - DANISA - 100% CALIDAD
        drawQualityStamp(canvas, PAGE_WIDTH * 0.72f, 740f, 110f)

        // Bottom Left Geometric Shapes
        val bottomPath1 = Path().apply {
            moveTo(0f, PAGE_HEIGHT - 80f)
            lineTo(140f, PAGE_HEIGHT - 80f)
            lineTo(140f, PAGE_HEIGHT.toFloat())
            lineTo(0f, PAGE_HEIGHT.toFloat())
            close()
        }
        canvas.drawPath(bottomPath1, navyPaint)

        val bottomPath2 = Path().apply {
            moveTo(0f, PAGE_HEIGHT - 170f)
            lineTo(260f, PAGE_HEIGHT - 170f)
            lineTo(260f, PAGE_HEIGHT - 80f)
            lineTo(0f, PAGE_HEIGHT - 80f)
            close()
        }
        canvas.drawPath(bottomPath2, cyanPaint)
    }

    private fun drawProductGridPage(canvas: Canvas, products: List<ProductEntity>, pageNum: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // 3x3 Grid Layout parameters
        val cols = 3
        val rows = 3
        val marginLeft = 40f
        val marginRight = 40f
        val marginTop = 60f
        val marginBottom = 80f

        val gridWidth = PAGE_WIDTH - marginLeft - marginRight
        val gridHeight = PAGE_HEIGHT - marginTop - marginBottom

        val cellWidth = gridWidth / cols
        val cellHeight = gridHeight / rows

        for (i in products.indices) {
            val product = products[i]
            val row = i / cols
            val col = i % cols

            val cellX = marginLeft + (col * cellWidth)
            val cellY = marginTop + (row * cellHeight)

            drawProductCell(canvas, cellX, cellY, cellWidth, cellHeight, product)
        }

        // Bottom Geometric Footer (Cyan and Navy stripes)
        val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00A3E0") }
        val navyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B2A4A") }
        val lightBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#B3E5FC") }

        canvas.drawRect(0f, PAGE_HEIGHT - 45f, PAGE_WIDTH * 0.45f, PAGE_HEIGHT - 25f, lightBluePaint)
        canvas.drawRect(0f, PAGE_HEIGHT - 25f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), cyanPaint)
        canvas.drawRect(0f, PAGE_HEIGHT - 45f, 180f, PAGE_HEIGHT.toFloat(), navyPaint)
    }

    private fun drawProductCell(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        product: ProductEntity
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val centerX = x + width / 2f

        // Product Mock Thumbnail Card with subtle shadow/border
        val imageRect = RectF(centerX - 50f, y + 10f, centerX + 50f, y + 110f)
        paint.color = Color.parseColor("#F5F7FA")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(imageRect, 8f, 8f, paint)

        paint.color = Color.parseColor("#E0E6ED")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(imageRect, 8f, 8f, paint)

        // Draw illustrative icon or initial inside product image box
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#00A3E0")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val initial = if (product.nombre.isNotBlank()) product.nombre.take(2).uppercase() else "PR"
        canvas.drawText(initial, centerX, y + 68f, paint)

        // Price Badge (Cyan rectangle matching PDF)
        val priceBadgeRect = RectF(centerX - 55f, y + 120f, centerX + 55f, y + 145f)
        val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00A3E0")
            style = Paint.Style.FILL
        }
        canvas.drawRect(priceBadgeRect, cyanPaint)

        // Price Text
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 15f
        paint.textAlign = Paint.Align.CENTER
        val priceStr = if (product.precioVenta % 1.0 == 0.0) {
            "$${product.precioVenta.toInt()}"
        } else {
            String.format(Locale.getDefault(), "$%.2f", product.precioVenta)
        }
        canvas.drawText(priceStr, centerX, y + 138f, paint)

        // Product Title
        paint.color = Color.parseColor("#2C3E50")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 11f
        paint.textAlign = Paint.Align.CENTER

        val formattedTitle = product.nombre.lowercase().replaceFirstChar { it.uppercase() }
        val titleLines = wrapText(formattedTitle, 20)
        var lineY = y + 162f
        for (line in titleLines.take(2)) {
            canvas.drawText(line, centerX, lineY, paint)
            lineY += 13f
        }

        // Subtitle / Weight / Package
        paint.color = Color.parseColor("#7F8C8D")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f
        val detailStr = "${product.unidad} • Código: ${product.codigo}"
        canvas.drawText(detailStr, centerX, lineY + 3f, paint)
    }

    private fun drawBackCoverPage(canvas: Canvas, phone: String, email: String) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        canvas.drawColor(Color.WHITE)

        // Vintage Stamp on top
        drawQualityStamp(canvas, PAGE_WIDTH / 2f, 210f, 115f)

        // Main Title: COTIZA CON NOSOTROS
        paint.color = Color.parseColor("#1B2A4A")
        paint.textSize = 48f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        paint.letterSpacing = 0.05f
        canvas.drawText("COTIZA CON", PAGE_WIDTH / 2f, 380f, paint)
        canvas.drawText("NOSOTROS", PAGE_WIDTH / 2f, 435f, paint)

        // Contact Info Icons and texts
        paint.color = Color.parseColor("#1B2A4A")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.letterSpacing = 0.02f

        // Phone: (951) 249-4964
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("📞  $phone", 70f, 520f, paint)

        // Email: eddyzun18@gmail.com
        canvas.drawText("✉️  $email", 310f, 520f, paint)

        // Bottom slogan
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.parseColor("#1B2A4A")
        canvas.drawText("La mejor calidad del mercado la encuentras aquí.", PAGE_WIDTH / 2f, 610f, paint)

        // Bottom geometric cyan & navy shapes
        val navyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1B2A4A") }
        val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#00A3E0") }
        val lightBluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#80D0F5") }

        val triPath = Path().apply {
            moveTo(60f, PAGE_HEIGHT - 170f)
            lineTo(130f, PAGE_HEIGHT - 60f)
            lineTo(10f, PAGE_HEIGHT - 60f)
            close()
        }
        canvas.drawPath(triPath, cyanPaint)

        canvas.drawRect(0f, PAGE_HEIGHT - 190f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT - 150f, lightBluePaint)
        canvas.drawRect(18f, PAGE_HEIGHT - 150f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT - 90f, cyanPaint)
        canvas.drawRect(18f, PAGE_HEIGHT - 90f, 260f, PAGE_HEIGHT - 35f, navyPaint)
    }

    private fun drawQualityStamp(canvas: Canvas, cx: Float, cy: Float, radius: Float) {
        val stampPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#991B1B") // Burgundy / Rust Red
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }

        // Outer concentric circles
        canvas.drawCircle(cx, cy, radius, stampPaint)
        canvas.drawCircle(cx, cy, radius - 8f, stampPaint)
        canvas.drawCircle(cx, cy, radius - 30f, stampPaint)

        // Top arc text: 100 % CALIDAD
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#991B1B")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val topArcPath = Path().apply {
            addArc(RectF(cx - radius + 15f, cy - radius + 15f, cx + radius - 15f, cy + radius - 15f), 190f, 160f)
        }
        canvas.drawTextOnPath("★ 100 % CALIDAD ★", topArcPath, 0f, 0f, textPaint)

        // Bottom arc text: 100 % CALIDAD
        val bottomArcPath = Path().apply {
            addArc(RectF(cx - radius + 15f, cy - radius + 15f, cx + radius - 15f, cy + radius - 15f), 10f, 160f)
        }
        canvas.drawTextOnPath("★ 100 % CALIDAD ★", bottomArcPath, 0f, 0f, textPaint)

        // Center Brand Text: DANISA
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#991B1B")
            textSize = 34f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("DANISA", cx, cy + 10f, brandPaint)
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            if ((currentLine + word).length <= maxCharsPerLine) {
                currentLine += (if (currentLine.isEmpty()) "" else " ") + word
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return lines
    }

    private fun getSampleProducts(): List<ProductEntity> {
        return listOf(
            ProductEntity("0283", "CAJA BONICE", "Cajas", "Congelados", 5, 20.0, 34.0, 94.0),
            ProductEntity("8236", "PIEZA ALPURA DESLACTOSADA", "Piezas", "Lácteos", 5, 18.0, 26.0, 48.0),
            ProductEntity("15010407", "PIEZA BARRA JAMON FUD 3.5KG", "Piezas", "Embutidos", 2, 290.0, 385.0, 2.0),
            ProductEntity("7501040091230", "PIEZA BEBIBLE YOPLAIT", "Piezas", "Lácteos", 10, 9.0, 14.0, 38.0),
            ProductEntity("400", "PIEZA CHIMEX SALCHICHA", "Piezas", "Embutidos", 5, 18.0, 27.0, 5.0),
            ProductEntity("220", "PIEZA YOGURTH BEBIBLE", "Piezas", "Lácteos", 10, 8.5, 13.5, 50.0),
            ProductEntity("12345", "PIEZA YOGURTH CEREALERO", "Piezas", "Lácteos", 10, 9.0, 13.75, 30.0),
            ProductEntity("1234", "PIEZA YOGURTH VASO", "Piezas", "Lácteos", 10, 5.0, 8.0, 40.0),
            ProductEntity("0000", "PIEZA CN HUEVO", "Piezas", "Abarrotes", 5, 30.0, 45.0, 18.0)
        )
    }
}
