package com.example.ui.screens

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SyncSummary
import com.example.ui.theme.*

@Composable
fun SyncAndSettingsScreen(
    currentSpreadsheetId: String,
    currentWebAppUrl: String,
    isSyncing: Boolean,
    syncSummary: SyncSummary?,
    onSaveSpreadsheetId: (String) -> Unit,
    onSaveWebAppUrl: (String) -> Unit,
    onManualSync: () -> Unit,
    onExportMn: () -> Unit,
    onImportMnPrompt: () -> Unit,
    onExportStockCsv: () -> Unit,
    onExportSalesCsv: () -> Unit
) {
    val context = LocalContext.current
    var sheetIdInput by remember(currentSpreadsheetId) { mutableStateOf(currentSpreadsheetId) }
    var webAppUrlInput by remember(currentWebAppUrl) { mutableStateOf(currentWebAppUrl) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Google Sheets Integration Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = DaniisaGreen)
                        Text(
                            text = "Servidor Central: Google Sheets (ver6funcionaljs)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Configuración del ID de la Hoja de Cálculo vinculada:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = sheetIdInput,
                        onValueChange = { sheetIdInput = it },
                        label = { Text("SPREADSHEET_ID") },
                        placeholder = { Text("1_isk9QDbGJYemT3eX0Si1ilspVpvjEhXa4CKEissb4g") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("spreadsheet_id_input")
                    )

                    OutlinedTextField(
                        value = webAppUrlInput,
                        onValueChange = { webAppUrlInput = it },
                        label = { Text("URL Google Apps Script (Web App)") },
                        placeholder = { Text("https://script.google.com/macros/s/.../exec") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onSaveSpreadsheetId(sheetIdInput)
                            onSaveWebAppUrl(webAppUrlInput)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DaniisaCyan),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Parámetros")
                    }
                }
            }
        }

        // Manual Sync Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = DaniisaCyan)
                        Text(
                            text = "Sincronización Manual",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Envía todas las ventas, cobros, devoluciones y gastos registrados en modo offline hacia el servidor central.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Button(
                        onClick = onManualSync,
                        enabled = !isSyncing,
                        colors = ButtonDefaults.buttonColors(containerColor = DaniisaGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_manual_sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizando...")
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ejecutar Sincronización Manual")
                        }
                    }

                    if (syncSummary != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DaniisaGreenLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Último reporte de sincronización:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DaniisaGreenDark)
                                Text("• Ventas sincronizadas: ${syncSummary.totalSyncedSales}", fontSize = 11.sp)
                                Text("• Devoluciones sincronizadas: ${syncSummary.totalSyncedReturns}", fontSize = 11.sp)
                                Text("• Gastos sincronizados: ${syncSummary.totalSyncedExpenses}", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Backup & Database Export (.mn files)
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = DaniisaOrange)
                        Text(
                            text = "Copias de Seguridad (.mn)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        text = "Exporta o importa la base de datos completa de productos, clientes y ventas en el formato estándar .mn de Daniisa.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExportMn,
                            colors = ButtonDefaults.buttonColors(containerColor = DaniisaOrange),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar .mn", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onImportMnPrompt,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Importar .mn", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // CSV Reports Export
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = DaniisaCyan)
                        Text(
                            text = "Exportación de Reportes en Formato CSV",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportStockCsv,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stock CSV", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onExportSalesCsv,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ventas CSV", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
