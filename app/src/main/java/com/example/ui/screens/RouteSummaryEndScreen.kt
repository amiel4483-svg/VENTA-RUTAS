package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.ReturnEntity
import com.example.data.model.RouteSessionEntity
import com.example.data.model.SaleHeaderEntity
import com.example.data.repository.SyncSummary
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun RouteSummaryEndScreen(
    employeeName: String,
    initialCash: Double,
    sales: List<SaleHeaderEntity>,
    expenses: List<ExpenseEntity>,
    returns: List<ReturnEntity>,
    currentSession: RouteSessionEntity?,
    isSyncing: Boolean,
    syncSummary: SyncSummary?,
    onManualSync: () -> Unit,
    onExportPdf: () -> Unit,
    onStartNewRoute: () -> Unit
) {
    val totalVentas = remember(sales) { sales.sumOf { it.total } }
    val totalCobrado = remember(sales) { sales.filter { it.estadoPago == "PAGADO" }.sumOf { it.total } }
    val totalPorCobrar = remember(sales) { sales.filter { it.estadoPago == "POR_COBRAR" }.sumOf { it.total } }
    val totalGastos = remember(expenses) { expenses.sumOf { it.monto } }
    val efectivoTeoricoEnMano = initialCash + totalCobrado - totalGastos
    val pendingSync = sales.count { it.syncStatus == "PENDIENTE" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DaniisaGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FactCheck,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reporte Consolidado de Cierre de Ruta",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vendedor: $employeeName",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Manual Server Sync Alert Box (Requested by user)
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pendingSync > 0) DaniisaOrangeLight else DaniisaGreenLight
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (pendingSync > 0) Icons.Default.CloudUpload else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (pendingSync > 0) DaniisaOrange else DaniisaGreenDark
                        )
                        Text(
                            text = if (pendingSync > 0) "Sincronización Manual Requerida" else "Datos Sincronizados con el Servidor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (pendingSync > 0) DaniisaOrange else DaniisaGreenDark
                        )
                    }

                    Text(
                        text = if (pendingSync > 0)
                            "Tiene $pendingSync comprobantes pendientes de transferir a la Hoja de Cálculo Central (Google Sheets)."
                        else
                            "Todas las transacciones de la jornada han sido guardadas en la base central.",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )

                    Button(
                        onClick = onManualSync,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pendingSync > 0) DaniisaOrange else DaniisaCyan
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizando...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar Manualmente con Servidor", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (syncSummary != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Resultado Sincronización:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("• Ventas enviadas: ${syncSummary.totalSyncedSales}", fontSize = 11.sp)
                                Text("• Devoluciones enviadas: ${syncSummary.totalSyncedReturns}", fontSize = 11.sp)
                                Text("• Gastos enviados: ${syncSummary.totalSyncedExpenses}", fontSize = 11.sp)
                                if (syncSummary.errors.isNotEmpty()) {
                                    Text("⚠️ ${syncSummary.errors.first()}", fontSize = 10.sp, color = DaniisaRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Consolidated Financial Breakdown
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Arqueo de Ventas y Liquidación",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    SummaryItem(label = "Fondo Inicial de Caja:", value = "$ ${String.format(Locale.getDefault(), "%.2f", initialCash)}")
                    SummaryItem(label = "Ventas Totales Brutas (${sales.size} tickets):", value = "$ ${String.format(Locale.getDefault(), "%.2f", totalVentas)}", isBold = true)
                    SummaryItem(label = "Cobrado al Contado / Yape:", value = "$ ${String.format(Locale.getDefault(), "%.2f", totalCobrado)}", color = DaniisaGreenDark)
                    SummaryItem(label = "Créditos por Cobrar:", value = "$ ${String.format(Locale.getDefault(), "%.2f", totalPorCobrar)}", color = DaniisaOrange)
                    SummaryItem(label = "Devoluciones / Cambios (${returns.size}):", value = "- ${returns.size} artículos", color = DaniisaRed)
                    SummaryItem(label = "Gastos en Ruta Pagados:", value = "- $ ${String.format(Locale.getDefault(), "%.2f", totalGastos)}", color = DaniisaRed)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Efectivo Total a Liquidar:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$ ${String.format(Locale.getDefault(), "%.2f", efectivoTeoricoEnMano)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DaniisaGreenDark
                        )
                    }
                }
            }
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportPdf,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = DaniisaRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exportar PDF")
                }

                Button(
                    onClick = onStartNewRoute,
                    colors = ButtonDefaults.buttonColors(containerColor = DaniisaCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nueva Ruta")
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextPrimary)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}
