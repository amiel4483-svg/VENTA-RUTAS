package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductEntity(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val unidad: String = "Unidades",
    val grupo: String = "General",
    val stockMin: Int = 5,
    val precioCompra: Double = 0.0,
    val precioVenta: Double = 0.0,
    val stockActual: Double = 0.0,
    val fechaCreacion: String = "",
    val imagenUrl: String = ""
)

@Entity(tableName = "clientes")
data class ClientEntity(
    @PrimaryKey val dniRuc: String,
    val nombres: String,
    val direccion: String = "",
    val telefono: String = "",
    val fechaRegistro: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null
)

@Entity(tableName = "empleados")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val cargo: String = "Vendedor de Ruta",
    val telefono: String = "",
    val sueldoBase: Double = 0.0,
    val comision: Double = 5.0,
    val fechaRegistro: String = ""
)

@Entity(tableName = "cabecera_doc")
data class SaleHeaderEntity(
    @PrimaryKey val nDoc: String,
    val tipoDoc: String = "TICKET",
    val tipoOp: String = "VENTA",
    val fecha: String,
    val idTercero: String = "",
    val nombreTercero: String = "CLIENTE GENÉRICO",
    val subtotal: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val usuario: String = "Sistema",
    val timestamp: String = "",
    val empleado: String = "",
    val efectivoRecibido: Double = 0.0,
    val cambio: Double = 0.0,
    val estadoPago: String = "PAGADO", // PAGADO | POR_COBRAR
    val formaPago: String = "Efectivo",
    val etiqueta: String = "",
    val infoAdicional: String = "",
    val syncStatus: String = "PENDIENTE" // PENDIENTE | SINCRONIZADO
)

@Entity(tableName = "detalle_doc")
data class SaleDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nDoc: String,
    val codigo: String,
    val nombre: String,
    val cantidad: Double,
    val precioUnit: Double,
    val subtotal: Double,
    val descuento: Double = 0.0
)

@Entity(tableName = "movimientos")
data class MovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val fecha: String,
    val tipo: String, // INGRESO, SALIDA, VENTA, COMPRA, DEVOLUCION, VENTA_CAMBIO, AJUSTE
    val cantidad: Double,
    val usuario: String = "Sistema",
    val timestamp: String,
    val observaciones: String = "",
    val stockResultante: Double = 0.0,
    val docRef: String = "",
    val syncStatus: String = "PENDIENTE"
)

@Entity(tableName = "devoluciones")
data class ReturnEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val nDocOrig: String,
    val cliente: String = "CLIENTE GENÉRICO",
    val productoDevuelto: String,
    val cantDevuelta: Double,
    val motivo: String = "",
    val productoCambio: String = "",
    val cantCambio: Double = 0.0,
    val usuario: String = "Sistema",
    val syncStatus: String = "PENDIENTE"
)

@Entity(tableName = "gastos")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fecha: String,
    val categoria: String, // Gasolina, Viáticos, Mantenimiento, etc.
    val descripcion: String = "",
    val monto: Double,
    val responsable: String = "",
    val syncStatus: String = "PENDIENTE"
)

@Entity(tableName = "route_sessions")
data class RouteSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeName: String,
    val employeeId: String = "",
    val startTime: String,
    val endTime: String? = null,
    val initialCash: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE | COMPLETED
    val totalSales: Double = 0.0,
    val totalTransactions: Int = 0,
    val isSynced: Boolean = false
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val key: String,
    val value: String
)

data class CartItem(
    val product: ProductEntity,
    var cantidad: Double = 1.0,
    var precioUnitario: Double = product.precioVenta,
    var esDevolucion: Boolean = false,
    var esCambio: Boolean = false,
    var cambioTipo: String = "MISMO", // MISMO | OTRO
    var productoCambioSeleccionado: ProductEntity? = null,
    var motivoCambio: String = ""
) {
    val subtotal: Double
        get() {
            return if (esDevolucion) {
                - (cantidad * precioUnitario)
            } else if (esCambio && cambioTipo == "OTRO" && productoCambioSeleccionado != null) {
                // Adjustment: difference between selected new product and returned item
                (cantidad * productoCambioSeleccionado!!.precioVenta) - (cantidad * precioUnitario)
            } else {
                cantidad * precioUnitario
            }
        }
}
