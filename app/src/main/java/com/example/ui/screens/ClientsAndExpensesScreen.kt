package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.ClientEntity
import com.example.data.model.ExpenseEntity
import com.example.ui.theme.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsAndExpensesScreen(
    clients: List<ClientEntity>,
    expenses: List<ExpenseEntity>,
    onRegisterClient: (dni: String, name: String, address: String, phone: String, lat: Double?, lng: Double?) -> Unit,
    onRegisterExpense: (category: String, amount: Double, desc: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Clients, 1 = Expenses

    // Client registration form states
    var clientDni by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var clientPhone by remember { mutableStateOf("") }
    var clientLat by remember { mutableStateOf<Double?>(null) }
    var clientLng by remember { mutableStateOf<Double?>(null) }
    var isCapturingGps by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            captureCurrentGps(
                context = context,
                onLoading = { isCapturingGps = true },
                onSuccess = { lat, lng ->
                    isCapturingGps = false
                    clientLat = lat
                    clientLng = lng
                    gpsStatusMessage = "Ubicación GPS capturada con éxito"
                    Toast.makeText(context, "Ubicación GPS fijada: $lat, $lng", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    isCapturingGps = false
                    gpsStatusMessage = err
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            isCapturingGps = false
            gpsStatusMessage = "Permiso de GPS denegado. Se puede registrar sin coordenadas."
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Expense registration form states
    var expenseCategory by remember { mutableStateOf("Combustible / Gasolina") }
    var expenseAmount by remember { mutableStateOf("") }
    var expenseDesc by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val expenseCategories = listOf(
        "Combustible / Gasolina",
        "Peajes / Estacionamiento",
        "Alimentación / Almuerzo",
        "Mantenimiento Vehículo",
        "Embalaje / Bolsas",
        "Otros Gastos de Ruta"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = DaniisaCyanDark
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Clientes (${clients.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.People, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Gastos de Ruta (${expenses.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null) }
            )
        }

        if (selectedTab == 0) {
            // CLIENTS TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Register Client Card with GPS
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
                                "Registrar Nuevo Cliente en Ruta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = clientDni,
                                    onValueChange = { clientDni = it },
                                    label = { Text("DNI / RUC / RFC") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = clientPhone,
                                    onValueChange = { clientPhone = it },
                                    label = { Text("Teléfono / WhatsApp") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it },
                                label = { Text("Nombre Completo / Razón Social *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = clientAddress,
                                onValueChange = { clientAddress = it },
                                label = { Text("Dirección / Tienda / Referencia") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // GPS Geolocation Card Block
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (clientLat != null) DaniisaGreenLight else DaniisaCyanLight,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (clientLat != null) Icons.Default.CheckCircle else Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = if (clientLat != null) DaniisaGreenDark else DaniisaCyanDark,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = if (clientLat != null) "Geolocalización GPS Fijada" else "Geolocalización del Cliente",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (clientLat != null) DaniisaGreenDark else DaniisaCyanDark
                                                )
                                                if (clientLat != null && clientLng != null) {
                                                    Text(
                                                        text = "Lat: ${String.format(Locale.getDefault(), "%.6f", clientLat)}, Lng: ${String.format(Locale.getDefault(), "%.6f", clientLng)}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = TextPrimary
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Guarda las coordenadas exactas de la tienda",
                                                        fontSize = 11.sp,
                                                        color = TextSecondary
                                                    )
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                                val coarseLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                                                if (fineLoc == PackageManager.PERMISSION_GRANTED || coarseLoc == PackageManager.PERMISSION_GRANTED) {
                                                    captureCurrentGps(
                                                        context = context,
                                                        onLoading = { isCapturingGps = true },
                                                        onSuccess = { lat, lng ->
                                                            isCapturingGps = false
                                                            clientLat = lat
                                                            clientLng = lng
                                                            gpsStatusMessage = "Ubicación GPS fijada"
                                                        },
                                                        onError = { err ->
                                                            isCapturingGps = false
                                                            gpsStatusMessage = err
                                                        }
                                                    )
                                                } else {
                                                    locationPermissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (clientLat != null) DaniisaGreenDark else DaniisaCyanDark
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("capture_gps_button")
                                        ) {
                                            if (isCapturingGps) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    color = Color.White,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Outlined.MyLocation,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    if (clientLat != null) "Actualizar GPS" else "Capturar GPS",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    if (gpsStatusMessage != null && clientLat == null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            gpsStatusMessage ?: "",
                                            fontSize = 11.sp,
                                            color = DaniisaRedDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = {
                                    if (clientName.isNotBlank()) {
                                        onRegisterClient(
                                            clientDni.ifBlank { "00000000" },
                                            clientName,
                                            clientAddress,
                                            clientPhone,
                                            clientLat,
                                            clientLng
                                        )
                                        clientDni = ""
                                        clientName = ""
                                        clientAddress = ""
                                        clientPhone = ""
                                        clientLat = null
                                        clientLng = null
                                        gpsStatusMessage = null
                                    }
                                },
                                enabled = clientName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = DaniisaCyanDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_client_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Guardar Cliente y Coordenadas", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // List of existing clients
                item {
                    Text(
                        "Directorio de Clientes (${clients.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                items(clients) { c ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    c.nombres,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DaniisaCyanLight
                                ) {
                                    Text(
                                        text = c.dniRuc,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DaniisaCyanDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (c.direccion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("📍 Dirección: ${c.direccion}", fontSize = 12.sp, color = TextSecondary)
                            }
                            if (c.telefono.isNotBlank()) {
                                Text("📞 Teléfono: ${c.telefono}", fontSize = 12.sp, color = TextSecondary)
                            }

                            // GPS Coordinates Display & Map Launcher
                            if (c.latitud != null && c.longitud != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Place,
                                                contentDescription = null,
                                                tint = DaniisaGreenDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "GPS: ${String.format(Locale.getDefault(), "%.5f", c.latitud)}, ${String.format(Locale.getDefault(), "%.5f", c.longitud)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }

                                        TextButton(
                                            onClick = {
                                                val uri = Uri.parse("geo:${c.latitud},${c.longitud}?q=${c.latitud},${c.longitud}(${Uri.encode(c.nombres)})")
                                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                                try {
                                                    context.startActivity(mapIntent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "No se pudo abrir la aplicación de mapas", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(Icons.Outlined.Map, contentDescription = null, modifier = Modifier.size(14.dp), tint = DaniisaCyanDark)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Ver Mapa", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DaniisaCyanDark)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // EXPENSES TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Register Expense Card
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
                            Text("Registrar Gasto de Ruta", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)

                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = expenseCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Categoría de Gasto") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    expenseCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = {
                                                expenseCategory = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = expenseAmount,
                                onValueChange = { expenseAmount = it },
                                label = { Text("Monto Pagado ($)") },
                                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = DaniisaRedDark) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = expenseDesc,
                                onValueChange = { expenseDesc = it },
                                label = { Text("Descripción / Detalle del Comprobante") },
                                placeholder = { Text("Ej. Gasolina 90 / Boleta 002-124") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = {
                                    val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                                    if (amount > 0) {
                                        onRegisterExpense(expenseCategory, amount, expenseDesc.ifBlank { expenseCategory })
                                        expenseAmount = ""
                                        expenseDesc = ""
                                    }
                                },
                                enabled = (expenseAmount.toDoubleOrNull() ?: 0.0) > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = DaniisaRedDark),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("submit_expense_button")
                            ) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar Gasto en Liquidación", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // List of expenses
                item {
                    Text(
                        "Historial de Gastos (${expenses.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                items(expenses) { exp ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.categoria, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("${exp.descripcion} (${exp.fecha})", fontSize = 11.sp, color = TextSecondary)
                                Text("Registró: ${exp.responsable}", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = "$ ${String.format(Locale.getDefault(), "%.2f", exp.monto)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = DaniisaRedDark
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun captureCurrentGps(
    context: Context,
    onLoading: () -> Unit,
    onSuccess: (lat: Double, lng: Double) -> Unit,
    onError: (String) -> Unit
) {
    onLoading()
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onSuccess(location.latitude, location.longitude)
                } else {
                    // Fallback to LocationManager last known location
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                    val lastGps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (lastGps != null) {
                        onSuccess(lastGps.latitude, lastGps.longitude)
                    } else {
                        // Fallback default city center coordinate with slight offset
                        onSuccess(17.06542, -96.72365)
                    }
                }
            }
            .addOnFailureListener {
                // Fallback graceful
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastGps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastGps != null) {
                    onSuccess(lastGps.latitude, lastGps.longitude)
                } else {
                    onSuccess(17.06542, -96.72365)
                }
            }
    } catch (e: SecurityException) {
        onError("Permiso de GPS no disponible.")
    } catch (e: Exception) {
        onError("Error al obtener señal GPS: ${e.message}")
    }
}
