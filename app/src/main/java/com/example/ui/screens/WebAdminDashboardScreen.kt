package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseEntity
import com.example.data.model.ProductEntity
import com.example.data.model.SaleHeaderEntity
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun WebAdminDashboardScreen(
    sales: List<SaleHeaderEntity>,
    expenses: List<ExpenseEntity>,
    products: List<ProductEntity>,
    onManualSync: () -> Unit
) {
    val totalRevenue = remember(sales) { sales.sumOf { it.total } }
    val totalExpenses = remember(expenses) { expenses.sumOf { it.monto } }
    val netProfit = totalRevenue - totalExpenses
    val totalInventoryValue = remember(products) { products.sumOf { it.stockActual * it.precioVenta } }

    // 5-way profit distribution breakdown based on standard Daniisa logistics:
    // Empleados/Comisiones: 20%
    // Reposición / Compras: 30%
    // Combustible / Gasolina: 10%
    // Gastos Operativos: 10%
    // Ganancia Neta Dueño: 30%
    val distEmpleados = maxOf(0.0, netProfit * 0.20)
    val distCompras = maxOf(0.0, netProfit * 0.30)
    val distGasolina = maxOf(0.0, netProfit * 0.10)
    val distGastosOp = maxOf(0.0, netProfit * 0.10)
    val distDueno = maxOf(0.0, netProfit * 0.30)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Admin Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DaniisaCyan),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Panel de Administración Daniisa",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Métricas en tiempo real & Distribución de Utilidades",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onManualSync,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("admin_sync_button")
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = DaniisaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sincronizar", color = DaniisaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4 KPI Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "Ventas Totales",
                    value = "$ ${String.format(Locale.getDefault(), "%.2f", totalRevenue)}",
                    subtitle = "${sales.size} comprobantes",
                    icon = Icons.Default.TrendingUp,
                    color = DaniisaGreen,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Utilidad Neta",
                    value = "$ ${String.format(Locale.getDefault(), "%.2f", netProfit)}",
                    subtitle = "Margen operativo",
                    icon = Icons.Default.MonetizationOn,
                    color = DaniisaCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "Gastos de Ruta",
                    value = "$ ${String.format(Locale.getDefault(), "%.2f", totalExpenses)}",
                    subtitle = "${expenses.size} registros",
                    icon = Icons.Default.MoneyOff,
                    color = DaniisaRed,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Valor Inventario",
                    value = "$ ${String.format(Locale.getDefault(), "%.2f", totalInventoryValue)}",
                    subtitle = "${products.size} productos",
                    icon = Icons.Default.Inventory2,
                    color = DaniisaOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Profit Distribution Card (Distribución de Utilidades)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Distribución Automática de Utilidad ($ ${String.format(Locale.getDefault(), "%.2f", netProfit)})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )

                    DistributionRow(label = "Comisión Empleados (20%)", amount = distEmpleados, color = Color(0xFF4CAF50))
                    DistributionRow(label = "Fondo Recompra Stock (30%)", amount = distCompras, color = Color(0xFF00A3E0))
                    DistributionRow(label = "Combustible & Movilidad (10%)", amount = distGasolina, color = Color(0xFFFF9800))
                    DistributionRow(label = "Gastos Operativos & Mantenimiento (10%)", amount = distGastosOp, color = Color(0xFF9C27B0))
                    DistributionRow(label = "Utilidad Neta Titular / Dueño (30%)", amount = distDueno, color = Color(0xFF2E7D32))
                }
            }
        }

        // Payment status breakdown
        item {
            val paid = sales.filter { it.estadoPago == "PAGADO" }.sumOf { it.total }
            val pending = sales.filter { it.estadoPago == "POR_COBRAR" }.sumOf { it.total }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Estado de Cobranza en Ruta", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cobrado en Efectivo / Digital:")
                        Text("$ ${String.format(Locale.getDefault(), "%.2f", paid)}", fontWeight = FontWeight.Bold, color = DaniisaGreenDark)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Crédito / Cuentas por Cobrar:")
                        Text("$ ${String.format(Locale.getDefault(), "%.2f", pending)}", fontWeight = FontWeight.Bold, color = DaniisaOrange)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(26.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(subtitle, fontSize = 10.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun DistributionRow(label: String, amount: Double, color: Color) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp)
            Text("$ ${String.format(Locale.getDefault(), "%.2f", amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { 1f },
            color = color,
            trackColor = BorderLight,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
    }
}
