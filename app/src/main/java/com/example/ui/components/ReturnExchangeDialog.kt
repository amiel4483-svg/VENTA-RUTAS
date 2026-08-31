package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.CartItem
import com.example.data.model.ProductEntity
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.abs

@Composable
fun ReturnExchangeDialog(
    item: CartItem,
    allProducts: List<ProductEntity>,
    onDismiss: () -> Unit,
    onApply: (
        isReturn: Boolean,
        isExchange: Boolean,
        exchangeType: String,
        replacementProduct: ProductEntity?,
        reason: String
    ) -> Unit
) {
    var mode by remember {
        mutableStateOf(
            if (item.esDevolucion) "DEVOLUCION"
            else if (item.esCambio) "CAMBIO"
            else "NORMAL"
        )
    }

    var exchangeType by remember { mutableStateOf(item.cambioTipo) } // MISMO | OTRO
    var selectedReplacement by remember { mutableStateOf<ProductEntity?>(item.productoCambioSeleccionado) }
    var reasonText by remember { mutableStateOf(item.motivoCambio) }

    // Find similar priced products (sorted by closest price difference)
    val similarProducts = remember(allProducts, item.product) {
        allProducts.filter { it.codigo != item.product.codigo }
            .sortedBy { abs(it.precioVenta - item.product.precioVenta) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHorizontalCircle,
                            contentDescription = null,
                            tint = DaniisaCyan
                        )
                        Text(
                            text = "Cambio o Devolución",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = "${item.product.nombre} (P. Venta: $ ${String.format(Locale.getDefault(), "%.2f", item.precioUnitario)})",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Option selector pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == "NORMAL",
                        onClick = { mode = "NORMAL" },
                        label = { Text("Venta Normal") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mode == "CAMBIO",
                        onClick = { mode = "CAMBIO" },
                        label = { Text("Cambio") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = mode == "DEVOLUCION",
                        onClick = { mode = "DEVOLUCION" },
                        label = { Text("Devolución") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (mode == "CAMBIO") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DaniisaCyanLight.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "¿Tipo de cambio de producto?",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = DaniisaCyanDark
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = exchangeType == "MISMO",
                                    onClick = {
                                        exchangeType = "MISMO"
                                        selectedReplacement = null
                                    }
                                )
                                Text("Mismo producto (Garantía)", fontSize = 13.sp)

                                Spacer(modifier = Modifier.width(8.dp))

                                RadioButton(
                                    selected = exchangeType == "OTRO",
                                    onClick = {
                                        exchangeType = "OTRO"
                                        if (selectedReplacement == null && similarProducts.isNotEmpty()) {
                                            selectedReplacement = similarProducts.first()
                                        }
                                    }
                                )
                                Text("Por otro producto", fontSize = 13.sp)
                            }
                        }
                    }

                    if (exchangeType == "OTRO") {
                        Text(
                            text = "Productos recomendados (Precio Similar):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DaniisaCyanDark,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 160.dp)
                        ) {
                            items(similarProducts.take(6)) { p ->
                                val isSelected = selectedReplacement?.codigo == p.codigo
                                val priceDiff = p.precioVenta - item.precioUnitario
                                val isSimilar = abs(priceDiff) <= 3.0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .background(
                                            if (isSelected) DaniisaCyanLight else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) DaniisaCyan else BorderLight,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedReplacement = p }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = p.nombre,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            )
                                            if (isSimilar) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Badge(containerColor = DaniisaOrange) {
                                                    Text("Recomendado", fontSize = 9.sp, color = Color.White)
                                                }
                                            }
                                        }
                                        Text(
                                            text = "Precio: $ ${String.format(Locale.getDefault(), "%.2f", p.precioVenta)} (Stock: ${p.stockActual})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        text = if (priceDiff == 0.0) "Mismo precio"
                                        else if (priceDiff > 0) "+$ ${String.format(Locale.getDefault(), "%.2f", priceDiff)}"
                                        else "-$ ${String.format(Locale.getDefault(), "%.2f", abs(priceDiff))}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (priceDiff > 0) DaniisaGreenDark else if (priceDiff < 0) DaniisaRed else Color.Gray
                                    )
                                }
                            }
                        }

                        if (selectedReplacement != null) {
                            val adjustment = (selectedReplacement!!.precioVenta - item.precioUnitario) * item.cantidad
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DaniisaGreenLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ajuste automático:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = if (adjustment >= 0) "+$ ${String.format(Locale.getDefault(), "%.2f", adjustment)}"
                                        else "-$ ${String.format(Locale.getDefault(), "%.2f", abs(adjustment))}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (adjustment >= 0) DaniisaGreenDark else DaniisaRed
                                    )
                                }
                            }
                        }
                    }
                } else if (mode == "DEVOLUCION") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DaniisaRedLight),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Devolución de Producto",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DaniisaRed
                            )
                            Text(
                                text = "El producto ingresará nuevamente al stock y se restará $ ${String.format(Locale.getDefault(), "%.2f", item.cantidad * item.precioUnitario)} del total de la venta.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Reason input
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Motivo del cambio/devolución") },
                    placeholder = { Text("Ej. Vencimiento, empaque roto, gusto...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            when (mode) {
                                "NORMAL" -> onApply(false, false, "MISMO", null, "")
                                "DEVOLUCION" -> onApply(true, false, "MISMO", null, reasonText.ifEmpty { "Devolución cliente" })
                                "CAMBIO" -> onApply(
                                    false,
                                    true,
                                    exchangeType,
                                    if (exchangeType == "OTRO") selectedReplacement else null,
                                    reasonText.ifEmpty { "Cambio de producto" }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DaniisaCyan),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_return_exchange_button")
                    ) {
                        Text("Aplicar", color = Color.White)
                    }
                }
            }
        }
    }
}
