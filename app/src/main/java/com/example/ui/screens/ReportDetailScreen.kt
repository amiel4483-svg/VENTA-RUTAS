package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    reportId: String,
    sales: List<SaleHeaderEntity>,
    saleDetails: List<SaleDetailEntity>,
    products: List<ProductEntity>,
    clients: List<ClientEntity>,
    expenses: List<ExpenseEntity>,
    returns: List<ReturnEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val title = when (reportId) {
        "TRANSACCIONES_DIA" -> "Transacciones por Día"
        "TOTAL_TRANSACCIONES" -> "Total de Transacciones"
        "FORMAS_PAGO" -> "Formas de Pago"
        "GASTOS_EXTRAS" -> "Gastos Extras de Ruta"
        "TRANSACCIONES_PRODUCTO" -> "Transacciones por Producto"
        "TOP_PRODUCTOS" -> "Top Productos Más Vendidos"
        "PRODUCTOS_DETALLE" -> "Productos a Detalle"
        "REPORTE_CATEGORIA" -> "Reporte por Categoría"
        "REPORTE_INVENTARIO" -> "Reporte de Inventario & Stock"
        "REPORTE_EMPLEADOS" -> "Reporte de Empleados / Vendedor"
        "VENTA_POR_DIA" -> "Venta por Día / Recaudación"
        "INGRESOS_GASTOS" -> "Ingresos y Gastos Extra"
        "REPORTE_CLIENTES" -> "Reporte de Clientes"
        "TOP_CLIENTES" -> "Top Clientes Frecuentes"
        else -> "Reporte Detallado"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // High-Contrast Sub-Header with Back and Share
        Surface(
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("report_back_button")
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Regresar al panel de reportes",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Distribuidora Daniisa",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = {
                            shareReportSummary(context, title, sales, saleDetails, expenses, products)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DaniisaCyanLight,
                            contentColor = DaniisaCyanDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DaniisaCyanDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Exportar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DaniisaCyanDark
                        )
                    }
                }
            }
        }

        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (reportId) {
                "TRANSACCIONES_DIA", "TOTAL_TRANSACCIONES", "VENTA_POR_DIA" -> {
                    // Detailed transactions with full product breakdown per sale
                    val totalVentas = sales.sumOf { it.total }
                    val totalItemsVendidos = saleDetails.sumOf { it.cantidad }
                    val ticketPromedio = if (sales.isNotEmpty()) totalVentas / sales.size else 0.0

                    // Summary KPI Card
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Resumen General de Transacciones",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Ventas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                        Text(
                                            "$ ${String.format(Locale.getDefault(), "%.2f", totalVentas)}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DaniisaGreenDark
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Tickets", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                        Text(
                                            "${sales.size}",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = BorderLight)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Promedio por Ticket: $ ${String.format(Locale.getDefault(), "%.2f", ticketPromedio)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Unidades: ${String.format(Locale.getDefault(), "%.0f", totalItemsVendidos)} pzs",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DaniisaCyanDark
                                    )
                                }
                            }
                        }
                    }

                    if (sales.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "No hay transacciones registradas hoy",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Las ventas que realices en el POS aparecerán aquí con el detalle de productos.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                "Detalle de Ventas & Productos Vendidos (${sales.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Group by date if multiple days, or show sales list
                        val salesByDate = sales.groupBy { it.fecha }
                        salesByDate.forEach { (fecha, salesList) ->
                            item {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DaniisaCyanLight,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "📅 Fecha: $fecha",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DaniisaCyanDark
                                        )
                                        Text(
                                            "Subtotal Día: $ ${String.format(Locale.getDefault(), "%.2f", salesList.sumOf { it.total })}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = DaniisaCyanDark
                                        )
                                    }
                                }
                            }

                            items(salesList) { sale ->
                                val details = saleDetails.filter { it.nDoc == sale.nDoc }
                                DetailedSaleCard(sale = sale, details = details)
                            }
                        }
                    }
                }

                "TOP_PRODUCTOS", "TRANSACCIONES_PRODUCTO", "PRODUCTOS_DETALLE" -> {
                    val productSales = saleDetails.groupBy { it.codigo }
                        .map { (code, items) ->
                            val prod = products.find { it.codigo == code }
                            val totalQty = items.sumOf { it.cantidad }
                            val totalAmount = items.sumOf { it.subtotal }
                            val avgPrice = if (totalQty > 0) totalAmount / totalQty else 0.0
                            TupleProductSale(
                                code = code,
                                name = prod?.nombre ?: items.firstOrNull()?.nombre ?: code,
                                group = prod?.grupo ?: "General",
                                quantity = totalQty,
                                totalAmount = totalAmount,
                                avgPrice = avgPrice,
                                currentStock = prod?.stockActual ?: 0.0
                            )
                        }
                        .sortedByDescending { it.quantity }

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Ranking y Detalle de Productos Vendidos",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    "Muestra cantidades totales, precios de venta e ingresos generados",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                if (productSales.isEmpty()) {
                                    Text("Aún no se registran ventas de productos.", fontSize = 12.sp, color = TextSecondary)
                                } else {
                                    val maxQty = productSales.firstOrNull()?.quantity ?: 1.0
                                    productSales.forEachIndexed { idx, item ->
                                        val progress = if (maxQty > 0) (item.quantity / maxQty).toFloat() else 0f
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        "${idx + 1}. ${item.name}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        "Cód: ${item.code} | Cat: ${item.group} | Precio Prom: $ ${String.format(Locale.getDefault(), "%.2f", item.avgPrice)}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TextSecondary
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        "${String.format(Locale.getDefault(), "%.1f", item.quantity)} un.",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        "$ ${String.format(Locale.getDefault(), "%.2f", item.totalAmount)}",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DaniisaGreenDark
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp),
                                                color = DaniisaCyan,
                                                trackColor = BorderLight
                                            )
                                            Divider(color = BorderLight, modifier = Modifier.padding(top = 8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "FORMAS_PAGO" -> {
                    val payments = sales.groupBy { it.formaPago }
                    val totalRecaudado = sales.sumOf { it.total }

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Distribución de Formas de Pago",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                payments.forEach { (method, sList) ->
                                    val total = sList.sumOf { it.total }
                                    val pct = if (totalRecaudado > 0) (total / totalRecaudado * 100) else 0.0
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                "$method (${sList.size} tickets)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                "$ ${String.format(Locale.getDefault(), "%.2f", total)} (${String.format(Locale.getDefault(), "%.1f", pct)}%)",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = DaniisaGreenDark,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (pct / 100.0).toFloat() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            color = if (method == "Efectivo") DaniisaGreen else DaniisaCyan,
                                            trackColor = BorderLight
                                        )
                                        Divider(color = BorderLight, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                "GASTOS_EXTRAS", "INGRESOS_GASTOS" -> {
                    val totalGastos = expenses.sumOf { it.monto }
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Gastos Registrados en Ruta",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    "Total Gastos: $ ${String.format(Locale.getDefault(), "%.2f", totalGastos)}",
                                    color = DaniisaRedDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                if (expenses.isEmpty()) {
                                    Text("No hay gastos registrados hoy.", color = TextSecondary, fontSize = 12.sp)
                                } else {
                                    expenses.forEach { exp ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    exp.categoria,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    "${exp.descripcion} (${exp.fecha})",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                                Text(
                                                    "Responsable: ${exp.responsable.ifBlank { "Vendedor" }}",
                                                    fontSize = 11.sp,
                                                    color = TextMuted
                                                )
                                            }
                                            Text(
                                                "$ ${String.format(Locale.getDefault(), "%.2f", exp.monto)}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = DaniisaRedDark
                                            )
                                        }
                                        Divider(color = BorderLight)
                                    }
                                }
                            }
                        }
                    }
                }

                "REPORTE_CLIENTES", "TOP_CLIENTES" -> {
                    val clientSales = sales.groupBy { it.nombreTercero }
                        .map { (name, list) ->
                            Triple(name, list.size, list.sumOf { it.total })
                        }
                        .sortedByDescending { it.third }

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Ventas por Cliente / Clientes Frecuentes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                clientSales.forEach { (name, count, total) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("$count compras registradas", fontSize = 11.sp, color = TextSecondary)
                                        }
                                        Text(
                                            "$ ${String.format(Locale.getDefault(), "%.2f", total)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = DaniisaCyanDark
                                        )
                                    }
                                    Divider(color = BorderLight)
                                }
                            }
                        }
                    }
                }

                "REPORTE_INVENTARIO" -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Inventario & Stock en Vehículo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                products.forEach { prod ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(prod.nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                            Text("Cód: ${prod.codigo} | Cat: ${prod.grupo} | Precio: $ ${String.format(Locale.getDefault(), "%.2f", prod.precioVenta)}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                        Text(
                                            "${String.format(Locale.getDefault(), "%.1f", prod.stockActual)} ${prod.unidad.take(3)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = if (prod.stockActual > 0) DaniisaGreenDark else DaniisaRedDark
                                        )
                                    }
                                    Divider(color = BorderLight)
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Fallback
                    val totalVentas = sales.sumOf { it.total }
                    val totalGastos = expenses.sumOf { it.monto }
                    val neto = totalVentas - totalGastos

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Resumen de Recaudación y Liquidación", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Ventas Brutas:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("$ ${String.format(Locale.getDefault(), "%.2f", totalVentas)}", fontWeight = FontWeight.Bold, color = DaniisaGreenDark, fontSize = 14.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Gastos en Ruta:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("- $ ${String.format(Locale.getDefault(), "%.2f", totalGastos)}", fontWeight = FontWeight.Bold, color = DaniisaRedDark, fontSize = 14.sp)
                                }
                                Divider(color = BorderLight)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Recaudación Neta:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("$ ${String.format(Locale.getDefault(), "%.2f", neto)}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DaniisaCyanDark)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailedSaleCard(
    sale: SaleHeaderEntity,
    details: List<SaleDetailEntity>
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row with doc number and total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = DaniisaCyanLight
                    ) {
                        Text(
                            text = sale.nDoc,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DaniisaCyanDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${sale.fecha} ${sale.timestamp.takeLast(8)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$ ${String.format(Locale.getDefault(), "%.2f", sale.total)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DaniisaGreenDark
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expandir detalle",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cliente: ${sale.nombreTercero}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pago: ${sale.formaPago} (${sale.estadoPago})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (sale.estadoPago == "PAGADO") DaniisaGreenDark else DaniisaOrange
                )
                Text(
                    text = "Atendió: ${sale.empleado.ifEmpty { "Vendedor Daniisa" }}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Divider(color = BorderLight)
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        "ÍTEMS VENDIDOS EN ESTA VENTA (${details.size}):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Table Header
                    Surface(
                        color = SurfaceLight,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Producto / Clave", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(2f))
                            Text("Cant.", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.7f))
                            Text("P. Unit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.9f))
                            Text("Importe", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(0.9f))
                        }
                    }

                    details.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text(
                                    text = item.nombre,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Cód: ${item.codigo}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", item.cantidad)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(0.7f)
                            )
                            Text(
                                text = "$ ${String.format(Locale.getDefault(), "%.2f", item.precioUnit)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                modifier = Modifier.weight(0.9f)
                            )
                            Text(
                                text = "$ ${String.format(Locale.getDefault(), "%.2f", item.subtotal)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DaniisaGreenDark,
                                modifier = Modifier.weight(0.9f)
                            )
                        }
                        Divider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

private data class TupleProductSale(
    val code: String,
    val name: String,
    val group: String,
    val quantity: Double,
    val totalAmount: Double,
    val avgPrice: Double,
    val currentStock: Double
)

private fun shareReportSummary(
    context: Context,
    reportName: String,
    sales: List<SaleHeaderEntity>,
    saleDetails: List<SaleDetailEntity>,
    expenses: List<ExpenseEntity>,
    products: List<ProductEntity>
) {
    val totalVentas = sales.sumOf { it.total }
    val totalGastos = expenses.sumOf { it.monto }
    val itemsSummary = saleDetails.groupBy { it.nombre }
        .map { "${it.key}: ${it.value.sumOf { d -> d.cantidad }} un." }
        .take(10)
        .joinToString("\n- ")

    val text = """
        *DISTRIBUIDORA DANIISA - REPORTE DE VENTAS*
        Reporte: $reportName
        Total Ventas: $ ${String.format(Locale.getDefault(), "%.2f", totalVentas)}
        Total Gastos: $ ${String.format(Locale.getDefault(), "%.2f", totalGastos)}
        Recaudación Neta: $ ${String.format(Locale.getDefault(), "%.2f", totalVentas - totalGastos)}
        Total Comprobantes: ${sales.size}

        *Productos Vendidos:*
        - $itemsSummary

        Generado desde la App Móvil Daniisa QASO ERP.
    """.trimIndent()

    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Exportar Reporte Daniisa"))
}
