package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun getProductByCode(codigo: String): ProductEntity?

    @Query("SELECT * FROM productos WHERE codigo LIKE '%' || :query || '%' OR nombre LIKE '%' || :query || '%' ORDER BY nombre ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM productos WHERE grupo = :grupo ORDER BY nombre ASC")
    fun getProductsByGroup(grupo: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE productos SET stockActual = :stock WHERE codigo = :codigo")
    suspend fun updateStock(codigo: String, stock: Double)

    @Query("DELETE FROM productos WHERE codigo = :codigo")
    suspend fun deleteProduct(codigo: String)

    @Query("DELETE FROM productos")
    suspend fun deleteAllProducts()
}

@Dao
interface ClientDao {
    @Query("SELECT * FROM clientes ORDER BY nombres ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clientes WHERE dniRuc LIKE '%' || :query || '%' OR nombres LIKE '%' || :query || '%'")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Query("DELETE FROM clientes WHERE dniRuc = :dniRuc")
    suspend fun deleteClient(dniRuc: String)
}

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM empleados ORDER BY nombre ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM empleados WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM cabecera_doc ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleHeaderEntity>>

    @Query("SELECT * FROM cabecera_doc WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun getSalesByDate(fecha: String): Flow<List<SaleHeaderEntity>>

    @Query("SELECT * FROM cabecera_doc WHERE nDoc = :nDoc LIMIT 1")
    suspend fun getSaleByDoc(nDoc: String): SaleHeaderEntity?

    @Query("SELECT * FROM cabecera_doc WHERE syncStatus = 'PENDIENTE'")
    suspend fun getPendingSales(): List<SaleHeaderEntity>

    @Query("SELECT COUNT(*) FROM cabecera_doc WHERE tipoOp = 'VENTA'")
    suspend fun getSalesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleHeader(sale: SaleHeaderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleDetails(details: List<SaleDetailEntity>)

    @Query("SELECT * FROM detalle_doc WHERE nDoc = :nDoc")
    suspend fun getDetailsForSale(nDoc: String): List<SaleDetailEntity>

    @Query("SELECT * FROM detalle_doc WHERE nDoc = :nDoc")
    fun getDetailsForSaleFlow(nDoc: String): Flow<List<SaleDetailEntity>>

    @Query("SELECT * FROM detalle_doc")
    fun getAllSaleDetailsFlow(): Flow<List<SaleDetailEntity>>

    @Query("UPDATE cabecera_doc SET syncStatus = 'SINCRONIZADO' WHERE nDoc = :nDoc")
    suspend fun markSaleAsSynced(nDoc: String)
}

@Dao
interface MovementDao {
    @Query("SELECT * FROM movimientos ORDER BY timestamp DESC")
    fun getAllMovements(): Flow<List<MovementEntity>>

    @Query("SELECT * FROM movimientos WHERE syncStatus = 'PENDIENTE'")
    suspend fun getPendingMovements(): List<MovementEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: MovementEntity)

    @Query("UPDATE movimientos SET syncStatus = 'SINCRONIZADO' WHERE id = :id")
    suspend fun markMovementAsSynced(id: Long)
}

@Dao
interface ReturnDao {
    @Query("SELECT * FROM devoluciones ORDER BY id DESC")
    fun getAllReturns(): Flow<List<ReturnEntity>>

    @Query("SELECT * FROM devoluciones WHERE syncStatus = 'PENDIENTE'")
    suspend fun getPendingReturns(): List<ReturnEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnEntity: ReturnEntity)

    @Query("UPDATE devoluciones SET syncStatus = 'SINCRONIZADO' WHERE id = :id")
    suspend fun markReturnAsSynced(id: Long)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE fecha = :fecha")
    fun getExpensesByDate(fecha: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM gastos WHERE syncStatus = 'PENDIENTE'")
    suspend fun getPendingExpenses(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("UPDATE gastos SET syncStatus = 'SINCRONIZADO' WHERE id = :id")
    suspend fun markExpenseAsSynced(id: Long)
}

@Dao
interface RouteSessionDao {
    @Query("SELECT * FROM route_sessions ORDER BY id DESC LIMIT 1")
    fun getCurrentSession(): Flow<RouteSessionEntity?>

    @Query("SELECT * FROM route_sessions WHERE status = 'ACTIVE' ORDER BY id DESC LIMIT 1")
    suspend fun getActiveSession(): RouteSessionEntity?

    @Query("SELECT * FROM route_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<RouteSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: RouteSessionEntity): Long

    @Update
    suspend fun updateSession(session: RouteSessionEntity)
}

@Dao
interface ConfigDao {
    @Query("SELECT * FROM app_config")
    fun getAllConfig(): Flow<List<AppConfigEntity>>

    @Query("SELECT value FROM app_config WHERE key = :key LIMIT 1")
    suspend fun getConfigValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setConfigValue(config: AppConfigEntity)
}
