package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.core.content.FileProvider
import com.example.data.model.ProductEntity
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.theme.*
import com.example.util.PdfCatalogGenerator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryStockScreen(
    products: List<ProductEntity>,
    onExportCsv: () -> Unit,
    onAddNewProduct: (ProductEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var filterReserva by remember { mutableStateOf(false) }
    var filterMerma by remember { mutableStateOf(false) }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Dialog state for adding product
    var newCode by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Lácteos") }
    var newPrice by remember { mutableStateOf("") }
    var newStock by remember { mutableStateOf("10") }

    val filteredProducts = remember(products, searchQuery, selectedCategory, filterReserva, filterMerma) {
        products.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                    p.nombre.contains(searchQuery, ignoreCase = true) ||
                    p.codigo.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategory == null || p.grupo == selectedCategory
            val matchesReserva = !filterReserva || p.stockActual > 0
            val matchesMerma = !filterMerma || p.stockActual <= 0
            matchesQuery && matchesCat && matchesReserva && matchesMerma
        }
    }

    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            products = products,
            onDismiss = { showBarcodeDialog = false },
            onProductSelected = { prod ->
                searchQuery = prod.nombre
                showBarcodeDialog = false
            }
        )
    }

    if (showAddProductDialog) {
        AlertDialog(
            onDismissRequest = { showAddProductDialog = false },
            title = { Text("Nuevo Producto en Inventario", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCode,
                        onValueChange = { newCode = it },
                        label = { Text("Código de barras / Clave") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre del Producto") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Precio de Venta ($)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStock,
                        onValueChange = { newStock = it },
                        label = { Text("Stock Inicial") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val code = if (newCode.isNotBlank()) newCode else "PRD_${System.currentTimeMillis() % 10000}"
                            val priceVal = newPrice.toDoubleOrNull() ?: 10.0
                            val stockVal = newStock.toDoubleOrNull() ?: 10.0
                            onAddNewProduct(
                                ProductEntity(
                                    codigo = code,
                                    nombre = newName.uppercase(),
                                    unidad = "Piezas",
                                    grupo = newCategory,
                                    precioVenta = priceVal,
                                    precioCompra = priceVal * 0.7,
                                    stockActual = stockVal
                                )
                            )
                            showAddProductDialog = false
                            newCode = ""
                            newName = ""
                            newPrice = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DaniisaGreen)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddProductDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddProductDialog = true },
                containerColor = DaniisaGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF1F5F9))
        ) {
            // Search Row matching screenshot 3
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Q Nombre/Clave", fontSize = 13.sp, color = Color.Gray) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = DaniisaCyan, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DaniisaCyan,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("inventory_search_input")
                        )

                        // Category Icon Filter Button
                        IconButton(
                            onClick = { selectedCategory = if (selectedCategory == null) "Lácteos" else null },
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (selectedCategory != null) DaniisaCyanLight else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Category, contentDescription = "Categorías", tint = DaniisaCyan)
                        }

                        // Barcode Scan Icon Button
                        IconButton(
                            onClick = { showBarcodeDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear", tint = Color(0xFF1E293B))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter Action Buttons matching Image 3: "Productos Reserva [X]", "Merma [X]", "[PDF] Catálogo", "[CSV] Importar / Exportar"
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = filterReserva,
                                onClick = { filterReserva = !filterReserva },
                                label = { Text("Productos Reserva", fontSize = 11.sp) },
                                trailingIcon = {
                                    if (filterReserva) Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DaniisaCyanLight,
                                    selectedLabelColor = DaniisaCyanDark
                                )
                            )
                        }

                        item {
                            FilterChip(
                                selected = filterMerma,
                                onClick = { filterMerma = !filterMerma },
                                label = { Text("Merma", fontSize = 11.sp) },
                                trailingIcon = {
                                    if (filterMerma) Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp))
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DaniisaRedLight,
                                    selectedLabelColor = DaniisaRed
                                )
                            )
                        }

                        // [PDF] Catálogo Button
                        item {
                            Button(
                                onClick = {
                                    try {
                                        val pdfFile = PdfCatalogGenerator.generateCatalogPdf(context, products)
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            pdfFile
                                        )
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Abrir Catálogo PDF"))
                                    } catch (e: Exception) {
                                        // Fallback direct share
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Catálogo PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // [CSV] Button
                        item {
                            OutlinedButton(
                                onClick = onExportCsv,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp), tint = DaniisaGreenDark)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DaniisaGreenDark)
                            }
                        }
                    }
                }
            }

            // Products List matching screenshot 3
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredProducts) { prod ->
                    val isPositiveStock = prod.stockActual > 0
                    val isZeroOrNegative = prod.stockActual <= 0

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Product Image / Photo Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DaniisaCyanLight)
                                    .border(1.dp, BorderLight, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (prod.imagenUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = prod.imagenUrl,
                                        contentDescription = "Foto de ${prod.nombre}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Category-based high-contrast visual icon/photo placeholder
                                    val (catIcon, catColor) = when {
                                        prod.grupo.contains("Lácteo", ignoreCase = true) || prod.nombre.contains("LECHE", ignoreCase = true) || prod.nombre.contains("ALPURA", ignoreCase = true) -> Pair(Icons.Default.LocalDrink, DaniisaCyanDark)
                                        prod.grupo.contains("Congelado", ignoreCase = true) || prod.nombre.contains("BONICE", ignoreCase = true) -> Pair(Icons.Default.AcUnit, DaniisaCyan)
                                        prod.grupo.contains("Embutido", ignoreCase = true) || prod.nombre.contains("JAMON", ignoreCase = true) || prod.nombre.contains("FUD", ignoreCase = true) -> Pair(Icons.Default.Restaurant, DaniisaOrange)
                                        prod.nombre.contains("YOPLAIT", ignoreCase = true) || prod.nombre.contains("BEBIBLE", ignoreCase = true) -> Pair(Icons.Default.Egg, DaniisaGold)
                                        else -> Pair(Icons.Default.Fastfood, DaniisaCyanDark)
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = catIcon,
                                            contentDescription = "Foto ${prod.nombre}",
                                            tint = catColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = prod.grupo.take(6),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = catColor
                                        )
                                    }
                                }
                            }

                            // Middle product details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${prod.codigo} | ${prod.nombre}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isPositiveStock) DaniisaGreenLight else DaniisaRedLight
                                    ) {
                                        Text(
                                            text = "Stock: ${String.format(Locale.getDefault(), "%.0f", prod.stockActual)} ${prod.unidad.take(3)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isPositiveStock) DaniisaGreenDark else DaniisaRedDark,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }

                                    Text(
                                        text = "• ${prod.grupo}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Precio de Venta: $ ${if (prod.precioVenta % 1.0 == 0.0) prod.precioVenta.toInt() else String.format(Locale.getDefault(), "%.2f", prod.precioVenta)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                            }

                            // Right Status Icon matching screenshot 3
                            if (isPositiveStock) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFDCFCE7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "En existencia",
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color(0xFFFEE2E2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = "Stock negativo o agotado",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
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
