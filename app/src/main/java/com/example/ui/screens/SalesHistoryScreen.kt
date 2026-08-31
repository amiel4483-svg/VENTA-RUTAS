package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleDetailEntity
import com.example.data.model.SaleHeaderEntity
import com.example.ui.components.TicketDialog
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    sales: List<SaleHeaderEntity>,
    allDetails: List<SaleDetailEntity>,
    onOpenTicket: (SaleHeaderEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("TODAS") } // TODAS | PAGADO | POR_COBRAR | PENDIENTE_SYNC
    var selectedSaleForTicket by remember { mutableStateOf<SaleHeaderEntity?>(null) }

    val filteredSales = remember(sales, searchQuery, filterStatus) {
        sales.filter { sale ->
            val matchesQuery = searchQuery.isBlank() ||
                    sale.nDoc.contains(searchQuery, ignoreCase = true) ||
                    sale.nombreTercero.contains(searchQuery, ignoreCase = true) ||
                    sale.fecha.contains(searchQuery, ignoreCase = true) ||
                    sale.empleado.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterStatus) {
                "PAGADO" -> sale.estadoPago == "PAGADO"
                "POR_COBRAR" -> sale.estadoPago == "POR_COBRAR"
                "PENDIENTE_SYNC" -> sale.syncStatus == "PENDIENTE"
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Quick Search Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por N° Ticket, Cliente o Fecha...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("history_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterStatus == "TODAS",
                        onClick = { filterStatus = "TODAS" },
                        label = { Text("Todas (${sales.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = filterStatus == "PAGADO",
                        onClick = { filterStatus = "PAGADO" },
                        label = { Text("Pagadas", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = filterStatus == "POR_COBRAR",
                        onClick = { filterStatus = "POR_COBRAR" },
                        label = { Text("Por Cobrar", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = filterStatus == "PENDIENTE_SYNC",
                        onClick = { filterStatus = "PENDIENTE_SYNC" },
                        label = { Text("Offline", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Summary Bar
        Surface(
            color = DaniisaCyanLight.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registros: ${filteredSales.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DaniisaCyanDark
                )
                Text(
                    text = "Total: S/ ${String.format(Locale.getDefault(), "%.2f", filteredSales.sumOf { it.total })}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DaniisaCyanDark
                )
            }
        }

        // Sales List
        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No se encontraron registros de ventas", fontWeight = FontWeight.Bold)
                    Text("Las ventas realizadas en ruta aparecerán aquí.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSales) { sale ->
                    val saleDetails = remember(sale.nDoc, allDetails) {
                        allDetails.filter { it.nDoc == sale.nDoc }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSaleForTicket = sale }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = DaniisaCyanLight,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(
                                            text = sale.nDoc,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = DaniisaCyanDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${sale.fecha} ${sale.timestamp.takeLast(8)}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                // Sync status badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (sale.syncStatus == "SINCRONIZADO") DaniisaGreenLight else DaniisaOrangeLight
                                ) {
                                    Text(
                                        text = if (sale.syncStatus == "SINCRONIZADO") "Sincronizado" else "Offline",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sale.syncStatus == "SINCRONIZADO") DaniisaGreenDark else DaniisaOrange,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = sale.nombreTercero,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )

                            Text(
                                text = "Atendió: ${sale.empleado.ifEmpty { "Vendedor Daniisa" }} | Pago: ${sale.formaPago} (${sale.estadoPago})",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )

                            if (sale.etiqueta.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "🏷️ ${sale.etiqueta}",
                                        fontSize = 10.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        tint = DaniisaCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${saleDetails.size} ítems vendidos",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "S/ ${String.format(Locale.getDefault(), "%.2f", sale.total)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DaniisaGreenDark
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { selectedSaleForTicket = sale },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Print,
                                            contentDescription = "Imprimir ticket",
                                            tint = DaniisaCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Thermal Ticket Dialog
    selectedSaleForTicket?.let { sale ->
        val details = remember(sale.nDoc, allDetails) {
            allDetails.filter { it.nDoc == sale.nDoc }
        }
        TicketDialog(
            sale = sale,
            details = details,
            onDismiss = { selectedSaleForTicket = null }
        )
    }
}
