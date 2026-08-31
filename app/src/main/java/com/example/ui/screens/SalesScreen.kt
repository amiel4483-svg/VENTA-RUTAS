package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.ClientEntity
import com.example.data.model.ProductEntity
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.ReturnExchangeDialog
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    cartItems: List<CartItem>,
    allProducts: List<ProductEntity>,
    allClients: List<ClientEntity>,
    selectedClient: ClientEntity,
    paymentStatus: String,
    paymentMethod: String,
    tag: String,
    notes: String,
    cashReceived: String,
    onAddProductToCart: (ProductEntity) -> Unit,
    onUpdateQuantity: (index: Int, qty: Double) -> Unit,
    onUpdatePrice: (index: Int, price: Double) -> Unit,
    onRemoveItem: (index: Int) -> Unit,
    onSetReturn: (index: Int, isReturn: Boolean, reason: String) -> Unit,
    onSetExchange: (index: Int, isExchange: Boolean, type: String, replacement: ProductEntity?, reason: String) -> Unit,
    onSelectClient: (ClientEntity) -> Unit,
    onSetPaymentStatus: (String) -> Unit,
    onSetPaymentMethod: (String) -> Unit,
    onSetTag: (String) -> Unit,
    onSetNotes: (String) -> Unit,
    onSetCashReceived: (String) -> Unit,
    onClearCart: () -> Unit,
    onSaveSale: () -> Unit,
    onQuickAddClient: (dni: String, name: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showBarcodeDialog by remember { mutableStateOf(false) }
    var showCategoryFilterDialog by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var showPaymentMethodDialog by remember { mutableStateOf(false) }
    var selectedItemForReturnExchangeIndex by remember { mutableStateOf<Int?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    // Client Add dialog fields
    var newClientDni by remember { mutableStateOf("") }
    var newClientName by remember { mutableStateOf("") }

    // Compute totals
    val subtotal = remember(cartItems) {
        cartItems.sumOf { it.subtotal }
    }
    val taxRate = 0.18 // 18% IGV
    val taxAmount = subtotal * taxRate
    val grandTotal = subtotal + taxAmount

    val categories = remember(allProducts) {
        allProducts.map { it.grupo }.distinct().filter { it.isNotBlank() }
    }

    // Filtered products dropdown when typing in search
    val searchResults = remember(searchQuery, selectedCategoryFilter, allProducts) {
        if (searchQuery.isBlank() && selectedCategoryFilter == null) emptyList()
        else {
            allProducts.filter { prod ->
                val matchesQuery = searchQuery.isBlank() ||
                        prod.nombre.contains(searchQuery, ignoreCase = true) ||
                        prod.codigo.contains(searchQuery, ignoreCase = true)
                val matchesCat = selectedCategoryFilter == null || prod.grupo == selectedCategoryFilter
                matchesQuery && matchesCat
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Search & Action Row (Matching screenshot top search section)
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Search text field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Nombre/Clave", fontSize = 14.sp, color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = BorderLight,
                            focusedBorderColor = DaniisaCyan
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("product_search_input")
                    )

                    // Category Filter Icon Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedCategoryFilter != null) DaniisaCyanLight else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { showCategoryFilterDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Filtrar categoría",
                                tint = if (selectedCategoryFilter != null) DaniisaCyan else TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Client Selector Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { showAddClientDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Seleccionar cliente",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Barcode Scanner Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { showBarcodeDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Escanear código",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Dropdown suggestions when typing in search
                if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            searchResults.take(6).forEach { prod ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onAddProductToCart(prod)
                                            searchQuery = ""
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(prod.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Stock: ${prod.stockActual} ${prod.unidad} | ${prod.grupo}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                    Text(
                                        "S/ ${String.format(Locale.getDefault(), "%.2f", prod.precioVenta)}",
                                        fontWeight = FontWeight.Bold,
                                        color = DaniisaCyan
                                    )
                                }
                                Divider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }
        }

        // Active Cart List and Inputs
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp)
        ) {
            // Cart Items
            if (cartItems.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay artículos en la venta actual", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Busque productos arriba o escanee un código para agregarlos.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                itemsIndexed(cartItems) { index, item ->
                    ProductCartCard(
                        item = item,
                        index = index,
                        onUpdateQuantity = { qty -> onUpdateQuantity(index, qty) },
                        onUpdatePrice = { price -> onUpdatePrice(index, price) },
                        onRemove = { onRemoveItem(index) },
                        onOpenReturnExchange = { selectedItemForReturnExchangeIndex = index }
                    )
                }
            }

            // Payment Status (Pagado vs Por Cobrar) - Matching screenshot
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onSetPaymentStatus("PAGADO") }
                            .padding(end = 24.dp)
                    ) {
                        RadioButton(
                            selected = paymentStatus == "PAGADO",
                            onClick = { onSetPaymentStatus("PAGADO") },
                            colors = RadioButtonDefaults.colors(selectedColor = DaniisaCyan)
                        )
                        Text("Pagado", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onSetPaymentStatus("POR_COBRAR") }
                    ) {
                        RadioButton(
                            selected = paymentStatus == "POR_COBRAR",
                            onClick = { onSetPaymentStatus("POR_COBRAR") },
                            colors = RadioButtonDefaults.colors(selectedColor = DaniisaOrange)
                        )
                        Text("Por Cobrar", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Green Total Badge Card - Matching screenshot "%+ Total $ ..."
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DaniisaGreen,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "+% Total $",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "S/ ${String.format(Locale.getDefault(), "%.2f", grandTotal)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DaniisaGreenDark
                        )
                    }
                }
            }

            // Client Field (Optional)
            item {
                OutlinedTextField(
                    value = selectedClient.nombres,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cliente (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                onSelectClient(ClientEntity("00000000", "CLIENTE GENÉRICO", "Mostrador", "000000000"))
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar cliente", tint = TextSecondary)
                            }
                            IconButton(onClick = { showAddClientDialog = true }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Cambiar/Agregar cliente", tint = DaniisaCyan)
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Payment Method Field (Optional)
            item {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Forma de pago (opcional)") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onSetPaymentMethod("Efectivo") }) {
                                Icon(Icons.Default.Close, contentDescription = "Por defecto", tint = TextSecondary)
                            }
                            IconButton(onClick = { showPaymentMethodDialog = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Elegir forma de pago")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPaymentMethodDialog = true }
                )
            }

            // Cash Received Input (If cash payment)
            if (paymentMethod == "Efectivo") {
                item {
                    OutlinedTextField(
                        value = cashReceived,
                        onValueChange = onSetCashReceived,
                        label = { Text("Efectivo recibido (S/)") },
                        placeholder = { Text("Ej. 50.00") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = DaniisaGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tag Field (Optional)
            item {
                OutlinedTextField(
                    value = tag,
                    onValueChange = onSetTag,
                    label = { Text("Etiqueta (opcional)") },
                    placeholder = { Text("Ej. Ruta Norte, Mayorista, Urgente...") },
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (tag.isNotEmpty()) {
                            IconButton(onClick = { onSetTag("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar")
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Additional Notes Field
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = onSetNotes,
                    label = { Text("Información adicional de la venta (opcional)") },
                    placeholder = { Text("Observaciones del pedido o entrega...") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary) },
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Bottom Action Buttons: Red Cancel (X) & Green Save (✓ Guardar) - Matching screenshot
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Red Circular Cancel Button
                    Surface(
                        shape = CircleShape,
                        color = DaniisaRed,
                        shadowElevation = 3.dp,
                        modifier = Modifier
                            .size(54.dp)
                            .clickable { onClearCart() }
                            .testTag("cancel_sale_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar Venta",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Green Pill Guardar Button
                    Button(
                        onClick = onSaveSale,
                        colors = ButtonDefaults.buttonColors(containerColor = DaniisaGreen),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .padding(horizontal = 4.dp)
                            .testTag("save_sale_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Guardar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showBarcodeDialog) {
        BarcodeScannerDialog(
            products = allProducts,
            onDismiss = { showBarcodeDialog = false },
            onProductSelected = { prod ->
                onAddProductToCart(prod)
            }
        )
    }

    if (showCategoryFilterDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryFilterDialog = false },
            title = { Text("Filtrar por Categoría") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategoryFilter = null
                                showCategoryFilterDialog = false
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Todas las categorías", fontWeight = if (selectedCategoryFilter == null) FontWeight.Bold else FontWeight.Normal)
                    }
                    Divider()
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryFilter = cat
                                    showCategoryFilterDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(cat, fontWeight = if (selectedCategoryFilter == cat) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryFilterDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showAddClientDialog) {
        AlertDialog(
            onDismissRequest = { showAddClientDialog = false },
            title = { Text("Seleccionar o Registrar Cliente") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Clientes existentes:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyColumn(modifier = Modifier.heightIn(max = 140.dp)) {
                        itemsIndexed(allClients) { _, c ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectClient(c)
                                        showAddClientDialog = false
                                    }
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(c.nombres, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(c.dniRuc, fontSize = 11.sp, color = TextSecondary)
                            }
                            Divider()
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Nuevo cliente rápido:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newClientDni,
                        onValueChange = { newClientDni = it },
                        label = { Text("DNI / RUC") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newClientName,
                        onValueChange = { newClientName = it },
                        label = { Text("Nombre / Razón Social") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newClientName.isNotBlank()) {
                            onQuickAddClient(newClientDni.ifBlank { "00000000" }, newClientName)
                            showAddClientDialog = false
                            newClientDni = ""
                            newClientName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DaniisaCyan)
                ) {
                    Text("Guardar y Usar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClientDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPaymentMethodDialog) {
        val methods = listOf("Efectivo", "Yape / Plin", "Tarjeta Débito/Crédito", "Transferencia Bancaria", "Crédito a 7 días", "Crédito a 15 días")
        AlertDialog(
            onDismissRequest = { showPaymentMethodDialog = false },
            title = { Text("Seleccionar Forma de Pago") },
            text = {
                Column {
                    methods.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetPaymentMethod(m)
                                    showPaymentMethodDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(m, fontWeight = if (paymentMethod == m) FontWeight.Bold else FontWeight.Normal)
                        }
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaymentMethodDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Return / Exchange Dialog
    selectedItemForReturnExchangeIndex?.let { idx ->
        if (idx in cartItems.indices) {
            ReturnExchangeDialog(
                item = cartItems[idx],
                allProducts = allProducts,
                onDismiss = { selectedItemForReturnExchangeIndex = null },
                onApply = { isReturn, isExchange, exchangeType, replacementProduct, reason ->
                    if (isReturn) {
                        onSetReturn(idx, true, reason)
                    } else if (isExchange) {
                        onSetExchange(idx, true, exchangeType, replacementProduct, reason)
                    } else {
                        onSetReturn(idx, false, "")
                    }
                    selectedItemForReturnExchangeIndex = null
                }
            )
        }
    }
}

@Composable
fun ProductCartCard(
    item: CartItem,
    index: Int,
    onUpdateQuantity: (Double) -> Unit,
    onUpdatePrice: (Double) -> Unit,
    onRemove: () -> Unit,
    onOpenReturnExchange: () -> Unit
) {
    var quantityText by remember(item.cantidad) { mutableStateOf(item.cantidad.toString().removeSuffix(".0")) }
    var priceText by remember(item.precioUnitario) { mutableStateOf(item.precioUnitario.toString()) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header with box icon and product name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.product.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )
            }

            // Return / Exchange Badge indicator
            if (item.esDevolucion) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DaniisaRedLight,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "DEVOLUCIÓN (-S/ ${String.format(Locale.getDefault(), "%.2f", item.cantidad * item.precioUnitario)}) ${if (item.motivoCambio.isNotBlank()) "- ${item.motivoCambio}" else ""}",
                        color = DaniisaRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else if (item.esCambio) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DaniisaCyanLight,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "CAMBIO: ${if (item.cambioTipo == "MISMO") "Mismo producto" else "Por ${item.productoCambioSeleccionado?.nombre ?: "Otro"}"}",
                        color = DaniisaCyanDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subheaders: Cantidad | Venta $ | Subtotal $
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cantidad",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1.3f)
                )
                Text(
                    text = "Venta $",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Subtotal $",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Input Row: (-) [Qty] (+) | [Price] | [Subtotal]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quantity Steppers Box
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1.3f)
                        .border(1.5.dp, DaniisaOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newQ = maxOf(0.0, item.cantidad - 1)
                            onUpdateQuantity(newQ)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Disminuir",
                            tint = Color.Gray
                        )
                    }

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it
                            it.toDoubleOrNull()?.let { q -> onUpdateQuantity(q) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    )

                    IconButton(
                        onClick = {
                            onUpdateQuantity(item.cantidad + 1)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Aumentar",
                            tint = Color.Gray
                        )
                    }
                }

                // Price Input Field
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        it.toDoubleOrNull()?.let { p -> onUpdatePrice(p) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderLight,
                        focusedBorderColor = DaniisaCyan
                    ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                )

                // Subtotal text display
                Text(
                    text = "S/ ${String.format(Locale.getDefault(), "%.2f", item.subtotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End,
                    color = if (item.subtotal < 0) DaniisaRed else DaniisaCyanDark,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Option Row: (i) Info | Cambio & Devolución Selector Button | (+%) | (X) Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Info Icon (i) - Matching screenshot
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF29B6F6),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { showInfoDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("i", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    // Cambio y Devolución Button (between Venta$ and Subtotal$ logic)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (item.esDevolucion) DaniisaRedLight else if (item.esCambio) DaniisaCyanLight else Color(0xFFF1F5F9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.esDevolucion) DaniisaRed else if (item.esCambio) DaniisaCyan else BorderLight),
                        modifier = Modifier.clickable { onOpenReturnExchange() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Cambio o Devolución",
                                tint = if (item.esDevolucion) DaniisaRed else if (item.esCambio) DaniisaCyan else TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.esDevolucion) "Devolución" else if (item.esCambio) "Cambio" else "Cambio/Dev",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.esDevolucion) DaniisaRed else if (item.esCambio) DaniisaCyan else TextPrimary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Discount / Tax (+%) Icon - Matching screenshot
                    Surface(
                        shape = CircleShape,
                        color = DaniisaOrange,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                // Quick 10% discount toggle
                                val discounted = item.product.precioVenta * 0.9
                                onUpdatePrice(discounted)
                                priceText = String.format(Locale.getDefault(), "%.2f", discounted)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Red Square Delete (X) Icon - Matching screenshot
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DaniisaRed),
                        color = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onRemove() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar artículo",
                                tint = DaniisaRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(item.product.nombre) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Código: ${item.product.codigo}")
                    Text("Categoría: ${item.product.grupo}")
                    Text("Unidad: ${item.product.unidad}")
                    Text("Stock actual en vehículo: ${item.product.stockActual} ${item.product.unidad}")
                    Text("Precio Lista: S/ ${String.format(Locale.getDefault(), "%.2f", item.product.precioVenta)}")
                    Text("Precio Compra Base: S/ ${String.format(Locale.getDefault(), "%.2f", item.product.precioCompra)}")
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
