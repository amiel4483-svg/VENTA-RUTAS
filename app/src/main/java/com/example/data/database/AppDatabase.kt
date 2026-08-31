package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        ProductEntity::class,
        ClientEntity::class,
        EmployeeEntity::class,
        SaleHeaderEntity::class,
        SaleDetailEntity::class,
        MovementEntity::class,
        ReturnEntity::class,
        ExpenseEntity::class,
        RouteSessionEntity::class,
        AppConfigEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun saleDao(): SaleDao
    abstract fun movementDao(): MovementDao
    abstract fun returnDao(): ReturnDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun routeSessionDao(): RouteSessionDao
    abstract fun configDao(): ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daniisa_sales_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Default Config matching ver6funcionaljs and screenshots
            val configs = listOf(
                AppConfigEntity("SPREADSHEET_ID", "1_isk9QDbGJYemT3eX0Si1ilspVpvjEhXa4CKEissb4g"),
                AppConfigEntity("WEB_APP_URL", "https://script.google.com/macros/s/AKfycbz_daniisa_endpoint/exec"),
                AppConfigEntity("NEGOCIO_NOMBRE", "DISTRIBUIDORA DANIISA"),
                AppConfigEntity("NEGOCIO_CONTACTO", "Oliverth"),
                AppConfigEntity("NEGOCIO_EMAIL", "eddyzun18@gmail.com"),
                AppConfigEntity("NEGOCIO_TELEFONO", "9512494964"),
                AppConfigEntity("NEGOCIO_RFC", "RFC: ZUCE931118BT4"),
                AppConfigEntity("TICKET_DISENO", "SIMPLE"),
                AppConfigEntity("TICKET_FORMATO", "TICKET"),
                AppConfigEntity("TICKET_STATUS", "true"),
                AppConfigEntity("TICKET_LOGO", "true"),
                AppConfigEntity("TICKET_CLIENTE_INFO", "false"),
                AppConfigEntity("MONEDA", "$"),
                AppConfigEntity("IMPUESTO_NOMBRE", "IVA"),
                AppConfigEntity("IMPUESTO_PORCENTAJE", "16"),
                AppConfigEntity("ZONA_HORARIA", "America/Mexico_City"),
                AppConfigEntity("DISTRIBUCION_EMPLEADOS", "20"),
                AppConfigEntity("DISTRIBUCION_COMPRA_PRODUCTO", "30"),
                AppConfigEntity("DISTRIBUCION_GASOLINA", "10"),
                AppConfigEntity("DISTRIBUCION_GASTOS_IMPRESORAS", "10"),
                AppConfigEntity("DISTRIBUCION_UTILIDAD_DUENO", "30")
            )
            configs.forEach { database.configDao().setConfigValue(it) }

            // Employees matching screenshot
            val employees = listOf(
                EmployeeEntity("020539_1103", "AMIEL GUADALUPE ZUÑIGA", "Vendedor de Ruta", "9512494964", 1500.0, 5.0, now),
                EmployeeEntity("EMP002", "Oliverth Zuñiga", "Supervisor de Ruta", "9512494964", 1800.0, 5.0, now),
                EmployeeEntity("EMP003", "Juan Carlos Pérez", "Vendedor de Ruta", "951333444", 1400.0, 5.0, now)
            )
            database.employeeDao().insertEmployees(employees)

            // Clients
            val clients = listOf(
                ClientEntity("00000000", "CLIENTE GENÉRICO", "Mostrador", "000000000", now),
                ClientEntity("ZUCE931118BT4", "TIENDA DOÑA ROSA", "Av. Benito Juárez 302, Centro", "9511112233", now),
                ClientEntity("RFC987654321", "MINISUPER EL ROBLE", "Calle Independencia 14", "9512223344", now),
                ClientEntity("ABCC12345678", "ABARROTES LA PROVIDENCIA", "Calzada de la República 88", "9513334455", now)
            )
            database.clientDao().insertClients(clients)

            // Full Products list matching screenshots and 7-page PDF catalog
            val products = listOf(
                ProductEntity("0283", "CAJA BONICE", "Caja = 10 pz", "Congelados", 5, 20.0, 34.0, 94.0, now),
                ProductEntity("8236", "PIEZA ALPURA DESLACTOSADA", "1 Litro", "Lácteos", 5, 18.0, 26.0, -48.0, now),
                ProductEntity("15010407", "PIEZA BARRA JAMON FUD 3.5KG", "Pieza 3.5kg", "Embutidos", 2, 290.0, 385.0, 2.0, now),
                ProductEntity("7501040091230", "PIEZA BEBIBLE YOPLAIT", "220g", "Lácteos", 10, 9.0, 14.0, 38.0, now),
                ProductEntity("7501040092770", "PIEZA DISFRUTA YOPLAIT BATIDO 442G", "442g", "Lácteos", 5, 19.0, 28.50, 0.0, now),
                ProductEntity("220", "PIEZA YOGURTH BEBIBLE", "Pieza", "Lácteos", 10, 8.50, 13.50, -223.0, now),
                ProductEntity("12345", "PIEZA YOGURTH CEREALERO", "125g", "Lácteos", 5, 9.0, 13.75, 1.0, now),
                ProductEntity("1234", "PIEZA YOGURTH VASO", "125g", "Lácteos", 10, 5.0, 8.0, 40.0, now),
                ProductEntity("400", "PIEZA CHIMEX SALCHICHA", "400g", "Embutidos", 5, 18.0, 27.0, 5.0, now),
                ProductEntity("0000", "PIEZA CN HUEVO", "Cono 30 pz", "Abarrotes", 5, 32.0, 45.0, 18.0, now),
                
                // Additional products from PDF catalog pages
                ProductEntity("ALP001", "Media crema 250g", "1 paquete = 3 pz", "Lácteos", 5, 11.0, 17.0, 50.0, now),
                ProductEntity("ALP002", "Media crema alpura 450g", "450g", "Lácteos", 5, 21.0, 31.50, 42.0, now),
                ProductEntity("ALP003", "Crema Alpura 426 ml", "426 ml", "Lácteos", 5, 30.0, 43.50, 35.0, now),
                ProductEntity("ALP004", "Crema Alpura 190g", "190g", "Lácteos", 5, 14.0, 21.75, 60.0, now),
                ProductEntity("PHI001", "Queso Philadelphia 120g", "120g", "Lácteos", 5, 19.0, 27.95, 45.0, now),
                ProductEntity("PHI002", "Queso Philadelphia 180g", "180g", "Lácteos", 5, 31.0, 44.0, 38.0, now),
                ProductEntity("MAR001", "Margarina la villita 90g", "90g", "Lácteos", 10, 9.5, 15.0, 80.0, now),
                ProductEntity("MAR002", "Margarina iberia 90g", "90g", "Lácteos", 10, 7.5, 12.0, 75.0, now),
                ProductEntity("MAN001", "Mantequilla Noche Buena 90g", "90g", "Lácteos", 10, 14.0, 20.80, 55.0, now),
                ProductEntity("YAK001", "yakult 3 pack 80 ml/cu", "3 pack", "Lácteos", 10, 5.5, 8.0, 90.0, now),
                ProductEntity("YOP001", "yoghut bebible Alpura 220g", "220g", "Lácteos", 10, 9.0, 13.50, 65.0, now),
                ProductEntity("ALP005", "lechitas Alpura Sabores 200 ml", "200 ml", "Lácteos", 10, 6.5, 10.0, 110.0, now),
                ProductEntity("YOP002", "Yoplait Chocogalleta 307g", "307g", "Lácteos", 5, 16.0, 23.0, 40.0, now),
                ProductEntity("YOP003", "Yoplait DISFRUTA 307g", "307g", "Lácteos", 5, 14.0, 20.50, 48.0, now),
                ProductEntity("YOP004", "Yoplait bebible 470g", "470g", "Lácteos", 5, 16.5, 24.0, 52.0, now),
                ProductEntity("YOP005", "Yoplait Licuado 470g", "470g", "Lácteos", 5, 17.0, 25.0, 44.0, now),
                ProductEntity("YOP006", "Yoplait Kids 100g", "100g", "Lácteos", 10, 6.0, 9.0, 85.0, now),
                ProductEntity("FUT001", "Manchego Americano FUT 180g", "180g", "Lácteos", 5, 25.0, 36.50, 30.0, now),
                ProductEntity("CAS001", "Queso Amarillo Le Castell 180g", "180g", "Lácteos", 5, 11.5, 17.0, 50.0, now),
                ProductEntity("VIL001", "Manchego La Villita 180g", "180g", "Lácteos", 5, 20.0, 29.50, 36.0, now),
                ProductEntity("CAS002", "Manchego Le Castell 180g", "180g", "Lácteos", 5, 10.5, 15.29, 45.0, now),
                ProductEntity("VIL002", "Queso Amarillo la villita 175g", "175g", "Lácteos", 5, 19.5, 28.50, 40.0, now),
                ProductEntity("FUT002", "Jamón Fut Virginia 290g", "290g", "Embutidos", 5, 42.0, 60.0, 28.0, now),
                ProductEntity("FUT003", "salchicha Fut 266g 1 Red=8 pz", "266g", "Embutidos", 5, 15.5, 23.0, 35.0, now),
                ProductEntity("FUD001", "Jamón Fud Americano 196g", "196g", "Embutidos", 5, 25.0, 36.50, 32.0, now),
                ProductEntity("CHX001", "Jamón chimex 162g", "162g", "Embutidos", 5, 17.0, 25.0, 40.0, now),
                ProductEntity("ANY001", "Salchijocho Any 3kg", "3kg", "Embutidos", 2, 95.0, 138.0, 12.0, now),
                ProductEntity("MAZ001", "Maiz Pozolero Morelos 1kg", "1kg", "Abarrotes", 5, 23.0, 34.0, 50.0, now),
                ProductEntity("MAZ002", "Maiz pozolero Michoacano 1kg", "1kg", "Abarrotes", 5, 18.0, 27.0, 50.0, now),
                ProductEntity("BON001", "Bonicessote 4pz", "4pz", "Congelados", 10, 22.0, 34.0, 60.0, now),
                ProductEntity("VUA001", "Vuala Sabores Tira = 6 pz", "Tira = 6 pz", "Snacks", 5, 68.0, 96.0, 25.0, now),
                ProductEntity("ELE001", "Electrolit sabores 625 ml", "625 ml", "Bebidas", 10, 16.5, 24.0, 120.0, now)
            )
            database.productDao().insertProducts(products)
        }
    }
}
