package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessInfoScreen(
    businessName: String = "DISTRIBUIDORA DANIISA",
    contactName: String = "Oliverth",
    email: String = "eddyzun18@gmail.com",
    phone: String = "9512494964",
    rfcInfo: String = "RFC: ZUCE931118BT4",
    onSaveBusinessInfo: (name: String, contact: String, email: String, phone: String, rfc: String, designType: String, formatType: String, addStatus: Boolean, addLogo: Boolean, addClientInfo: Boolean) -> Unit
) {
    var nameState by remember { mutableStateOf(businessName) }
    var contactState by remember { mutableStateOf(contactName) }
    var emailState by remember { mutableStateOf(email) }
    var phoneState by remember { mutableStateOf(phone) }
    var rfcState by remember { mutableStateOf(rfcInfo) }

    // Ticket Settings matching Image 6
    var ticketDesign by remember { mutableStateOf("SIMPLE") } // SIMPLE | EXTRA
    var ticketFormat by remember { mutableStateOf("TICKET") } // NORMAL | TICKET
    var addStatusInSale by remember { mutableStateOf(true) }
    var addLogoInTicket by remember { mutableStateOf(true) }
    var addClientInfoInReceipt by remember { mutableStateOf(false) }

    // Dialog state for editing fields
    var editingField by remember { mutableStateOf<String?>(null) }
    var tempFieldValue by remember { mutableStateOf("") }

    if (editingField != null) {
        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text("Editar $editingField", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempFieldValue,
                    onValueChange = { tempFieldValue = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        when (editingField) {
                            "Nombre del negocio" -> nameState = tempFieldValue
                            "Nombre de contacto" -> contactState = tempFieldValue
                            "Email" -> emailState = tempFieldValue
                            "Teléfono" -> phoneState = tempFieldValue
                            "Información Adicional" -> rfcState = tempFieldValue
                        }
                        editingField = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DaniisaCyan)
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        // Orange top disclaimer banner matching screenshot 6
        item {
            Surface(
                color = Color(0xFFF9A825),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "*La información sólo se utiliza para personalizar la aplicación y los recibos.",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        // Business Profile Form
        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre del negocio
                    EditableBusinessField(
                        label = "Nombre del negocio",
                        value = nameState,
                        onEdit = {
                            tempFieldValue = nameState
                            editingField = "Nombre del negocio"
                        }
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // Seleccione logo Row
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Seleccione logo:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Circle with question mark
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFFE8EEF5), CircleShape)
                                    .border(1.5.dp, Color(0xFFCBD5E1), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "?",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Action buttons: Gallery, Camera, Delete
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedIconButton(
                                    onClick = { /* Pick logo */ },
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería", tint = DaniisaCyan)
                                }

                                OutlinedIconButton(
                                    onClick = { /* Camera */ },
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = "Cámara", tint = DaniisaCyan)
                                }

                                OutlinedIconButton(
                                    onClick = { /* Remove */ },
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFEEEEEE))

                    // Nombre de contacto
                    EditableBusinessField(
                        label = "Nombre de contacto",
                        value = contactState,
                        onEdit = {
                            tempFieldValue = contactState
                            editingField = "Nombre de contacto"
                        }
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // Email
                    EditableBusinessField(
                        label = "Email",
                        value = emailState,
                        onEdit = {
                            tempFieldValue = emailState
                            editingField = "Email"
                        }
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // Teléfono
                    EditableBusinessField(
                        label = "Teléfono",
                        value = phoneState,
                        onEdit = {
                            tempFieldValue = phoneState
                            editingField = "Teléfono"
                        }
                    )

                    Divider(color = Color(0xFFEEEEEE))

                    // Información Adicional (RFC)
                    EditableBusinessField(
                        label = "Información Adicional",
                        value = rfcState,
                        onEdit = {
                            tempFieldValue = rfcState
                            editingField = "Información Adicional"
                        }
                    )
                }
            }
        }

        // Section Title: Configuración
        item {
            Surface(
                color = Color(0xFFE2E8F0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Configuración",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Configuration Options Card matching Image 6
        item {
            Card(
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Configuración recibo / ticket de venta:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    // Radio Group: Diseño Simple vs Diseño Extra
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { ticketDesign = "SIMPLE" }
                        ) {
                            RadioButton(
                                selected = (ticketDesign == "SIMPLE"),
                                onClick = { ticketDesign = "SIMPLE" },
                                colors = RadioButtonDefaults.colors(selectedColor = DaniisaCyan)
                            )
                            Text("Diseño Simple", fontSize = 13.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { ticketDesign = "EXTRA" }
                        ) {
                            RadioButton(
                                selected = (ticketDesign == "EXTRA"),
                                onClick = { ticketDesign = "EXTRA" },
                                colors = RadioButtonDefaults.colors(selectedColor = DaniisaCyan)
                            )
                            Text("Diseño Extra", fontSize = 13.sp)
                        }
                    }

                    // Checkbox: Agregar status en la venta
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { addStatusInSale = !addStatusInSale }
                    ) {
                        Checkbox(
                            checked = addStatusInSale,
                            onCheckedChange = { addStatusInSale = it },
                            colors = CheckboxDefaults.colors(checkedColor = DaniisaCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar status en la venta", fontSize = 13.sp)
                    }

                    // Radio Group: Formato Normal vs Formato Ticket
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { ticketFormat = "NORMAL" }
                        ) {
                            RadioButton(
                                selected = (ticketFormat == "NORMAL"),
                                onClick = { ticketFormat = "NORMAL" },
                                colors = RadioButtonDefaults.colors(selectedColor = DaniisaCyan)
                            )
                            Text("Formato Normal", fontSize = 13.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { ticketFormat = "TICKET" }
                        ) {
                            RadioButton(
                                selected = (ticketFormat == "TICKET"),
                                onClick = { ticketFormat = "TICKET" },
                                colors = RadioButtonDefaults.colors(selectedColor = DaniisaCyan)
                            )
                            Text("Formato Ticket", fontSize = 13.sp)
                        }
                    }

                    // Checkbox: Agregar logo en el ticket
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { addLogoInTicket = !addLogoInTicket }
                    ) {
                        Checkbox(
                            checked = addLogoInTicket,
                            onCheckedChange = { addLogoInTicket = it },
                            colors = CheckboxDefaults.colors(checkedColor = DaniisaCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar logo en el ticket", fontSize = 13.sp)
                    }

                    // Checkbox: Agregar Información del cliente en el recibo.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { addClientInfoInReceipt = !addClientInfoInReceipt }
                    ) {
                        Checkbox(
                            checked = addClientInfoInReceipt,
                            onCheckedChange = { addClientInfoInReceipt = it },
                            colors = CheckboxDefaults.colors(checkedColor = DaniisaCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar Información del cliente en el recibo.", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save Button
                    Button(
                        onClick = {
                            onSaveBusinessInfo(
                                nameState,
                                contactState,
                                emailState,
                                phoneState,
                                rfcState,
                                ticketDesign,
                                ticketFormat,
                                addStatusInSale,
                                addLogoInTicket,
                                addClientInfoInReceipt
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DaniisaGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_business_info_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Cambios de Mi Negocio", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditableBusinessField(
    label: String,
    value: String,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            Text(
                text = value.ifEmpty { "Sin especificar" },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (value.isNotEmpty()) Color(0xFF0F172A) else Color(0xFF94A3B8)
            )
        }

        IconButton(onClick = onEdit) {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Editar",
                tint = DaniisaCyan,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
