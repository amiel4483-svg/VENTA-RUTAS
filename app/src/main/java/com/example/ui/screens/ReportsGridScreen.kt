package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class ReportCardItem(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: Color
)

@Composable
fun ReportsGridScreen(
    onSelectReport: (String) -> Unit,
    onExportAllPdf: () -> Unit
) {
    var includeCharts by remember { mutableStateOf(true) }
    var reportCount by remember { mutableStateOf(14) }

    // Grid items matching user screenshot 2
    val reportCards = listOf(
        ReportCardItem("TRANSACCIONES_DIA", "Transacciones por Día", Icons.Default.EventNote, Color(0xFF4CAF50)),
        ReportCardItem("TOTAL_TRANSACCIONES", "Total de Transacciones", Icons.Default.ReceiptLong, Color(0xFF4CAF50)),
        ReportCardItem("FORMAS_PAGO", "Formas de Pago", Icons.Default.CreditCard, Color(0xFF4CAF50)),
        ReportCardItem("GASTOS_EXTRAS", "Gastos Extras de Ruta", Icons.Default.MoneyOff, Color(0xFFFF9800)),
        ReportCardItem("TRANSACCIONES_PRODUCTO", "Transacciones por Producto", Icons.Default.ViewList, Color(0xFF4CAF50)),
        ReportCardItem("TOP_PRODUCTOS", "Top Productos Más Vendidos", Icons.Default.Star, Color(0xFF4CAF50)),
        ReportCardItem("PRODUCTOS_DETALLE", "Productos a Detalle", Icons.Default.Category, Color(0xFF4CAF50)),
        ReportCardItem("REPORTE_CATEGORIA", "Reporte Categoría", Icons.Default.PieChart, Color(0xFF4CAF50)),
        ReportCardItem("REPORTE_INVENTARIO", "Reporte Inventario & Stock", Icons.Default.Inventory, Color(0xFF4CAF50)),
        ReportCardItem("REPORTE_EMPLEADOS", "Reporte Empleados / Vendedor", Icons.Default.Badge, Color(0xFF4CAF50)),
        ReportCardItem("VENTA_POR_DIA", "Venta por Día / Recaudación", Icons.Default.TrendingUp, Color(0xFFFF7043)),
        ReportCardItem("INGRESOS_GASTOS", "Ingresos y Gastos Extra", Icons.Default.AccountBalanceWallet, Color(0xFFFF7043)),
        ReportCardItem("REPORTE_CLIENTES", "Reporte Clientes", Icons.Default.People, Color(0xFF00A3E0)),
        ReportCardItem("TOP_CLIENTES", "Top Clientes Frecuentes", Icons.Default.WorkspacePremium, Color(0xFF00A3E0))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Subheader Panel - Matching screenshot 2 top
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vistas de reportes:",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$reportCount módulos activos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(
                            checked = includeCharts,
                            onCheckedChange = { includeCharts = it },
                            colors = CheckboxDefaults.colors(checkedColor = DaniisaCyan)
                        )
                        Text(
                            text = "Incluir Gráficas",
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }

                // Yellow/Gold "OBTENER VISTAS DE REPORTES" Button - Matching screenshot
                Button(
                    onClick = onExportAllPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4B106)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("get_reports_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EXPORTAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "REPORTES PDF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Grid of Report Cards (Matching screenshot 2)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(reportCards) { report ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = report.backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .height(115.dp)
                        .clickable { onSelectReport(report.id) }
                        .testTag("report_card_${report.id.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = report.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = report.title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}
