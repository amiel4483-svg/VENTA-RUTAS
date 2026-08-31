package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SaleDetailEntity
import com.example.data.model.SaleHeaderEntity
import com.example.ui.theme.DaniisaCyan
import com.example.ui.theme.DaniisaGreen
import java.util.Locale

@Composable
fun TicketDialog(
    sale: SaleHeaderEntity,
    details: List<SaleDetailEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var format by remember { mutableStateOf("80mm") } // 58mm | 80mm | A4

    val htmlTicket = remember(sale, details, format) {
        generateTicketHtml(sale, details, format)
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = DaniisaCyan)
                        Text(
                            text = "Comprobante de Venta",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                // Format selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = format == "58mm",
                        onClick = { format = "58mm" },
                        label = { Text("58 mm") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = format == "80mm",
                        onClick = { format = "80mm" },
                        label = { Text("80 mm") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = format == "A4",
                        onClick = { format = "A4" },
                        label = { Text("A4 Factura") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Ticket Thermal Preview Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFAFAFA),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "DISTRIBUIDORA DANIISA",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Venta en Ruta - QASO ERP",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "${sale.tipoDoc} N° ${sale.nDoc}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = "Fecha: ${sale.fecha} ${sale.timestamp.takeLast(8)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Cliente: ${sale.nombreTercero}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        if (sale.idTercero.isNotEmpty() && sale.idTercero != "00000000") {
                            Text(
                                text = "Doc: ${sale.idTercero}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "Vendedor: ${sale.empleado.ifEmpty { "Ruta" }}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Forma Pago: ${sale.formaPago} (${sale.estadoPago})",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )

                        // Items list
                        details.forEach { det ->
                            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text(
                                    text = det.nombre,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f", det.cantidad)} x $ ${String.format(Locale.getDefault(), "%.2f", det.precioUnit)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "$ ${String.format(Locale.getDefault(), "%.2f", det.subtotal)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "--------------------------------",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("$ ${String.format(Locale.getDefault(), "%.2f", sale.subtotal)}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IGV (18%):", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            Text("$ ${String.format(Locale.getDefault(), "%.2f", sale.impuesto)}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TOTAL:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("$ ${String.format(Locale.getDefault(), "%.2f", sale.total)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        if (sale.efectivoRecibido > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Efectivo:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("$ ${String.format(Locale.getDefault(), "%.2f", sale.efectivoRecibido)}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Cambio:", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                Text("$ ${String.format(Locale.getDefault(), "%.2f", sale.cambio)}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "¡Gracias por su preferencia!",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                // Action Buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Ticket ${sale.nDoc} - DISTRIBUIDORA DANIISA\nCliente: ${sale.nombreTercero}\nTotal: $ ${String.format(Locale.getDefault(), "%.2f", sale.total)}\nGracias por su compra."
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir ticket"))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compartir", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            printHtmlTicket(context, htmlTicket, "Ticket_${sale.nDoc}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DaniisaGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("print_ticket_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Imprimir", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

private fun printHtmlTicket(context: Context, html: String, jobName: String) {
    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("id", "thermal", 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager?.print(jobName, printAdapter, printAttributes)
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    } catch (_: Exception) {
    }
}

fun generateTicketHtml(sale: SaleHeaderEntity, details: List<SaleDetailEntity>, format: String): String {
    val itemsHtml = details.joinToString("") {
        """
        <tr><td colspan="2" style="font-weight:600;padding-bottom:1px">${it.nombre}</td></tr>
        <tr>
          <td style="color:#555">${String.format(Locale.getDefault(), "%.2f", it.cantidad)} x $ ${String.format(Locale.getDefault(), "%.2f", it.precioUnit)}</td>
          <td style="text-align:right;font-weight:700">$ ${String.format(Locale.getDefault(), "%.2f", it.subtotal)}</td>
        </tr>
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <style>
        body { width: 80mm; font-family: 'Courier New', monospace; font-size: 11px; padding: 4mm; margin: 0 auto; color: #000; }
        .c { text-align: center; }
        .b { font-weight: bold; }
        .lin { border-top: 1px dashed #000; margin: 4px 0; }
        .lin2 { border-top: 2px solid #000; margin: 4px 0; }
        table { width: 100%; border-collapse: collapse; }
        td { padding: 2px 0; }
      </style>
    </head>
    <body>
      <div class="c b" style="font-size:1.2em">DISTRIBUIDORA DANIISA</div>
      <div class="c" style="font-size:0.85em">Venta en Ruta - QASO ERP</div>
      <div class="lin2"></div>
      <div class="c b">${sale.tipoDoc} N° ${sale.nDoc}</div>
      <div class="lin"></div>
      <div>Fecha : ${sale.fecha}</div>
      <div>Cliente : ${sale.nombreTercero}</div>
      <div>Vendedor: ${sale.empleado}</div>
      <div>Pago : ${sale.formaPago} (${sale.estadoPago})</div>
      <div class="lin2"></div>
      <table>$itemsHtml</table>
      <div class="lin"></div>
      <table>
        <tr><td>Subtotal :</td><td style="text-align:right">$ ${String.format(Locale.getDefault(), "%.2f", sale.subtotal)}</td></tr>
        <tr><td>IGV (18%) :</td><td style="text-align:right">$ ${String.format(Locale.getDefault(), "%.2f", sale.impuesto)}</td></tr>
        <tr style="font-weight:bold; font-size:1.2em; border-top:1px solid #000;">
          <td>TOTAL :</td><td style="text-align:right">$ ${String.format(Locale.getDefault(), "%.2f", sale.total)}</td>
        </tr>
      </table>
      <div class="lin"></div>
      <div class="c" style="margin-top:4px">¡Gracias por su preferencia!</div>
    </body>
    </html>
    """.trimIndent()
}
