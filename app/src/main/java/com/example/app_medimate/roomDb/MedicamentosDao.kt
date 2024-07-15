package com.example.app_medimate.roomDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentosDao {
    @Upsert
    suspend fun upsertMedicamento(medicamento: Medicamentos)
    @Delete
    suspend fun deleteMedicamento(medicamento: Medicamentos)
    @Query("SELECT * FROM medicamentos")
    fun getAllMedicamentos(): Flow<List<Medicamentos>>
    @Query("SELECT * FROM medicamentos WHERE id = :id LIMIT 1")
    fun getMedicamentoById(id: Int): Flow<Medicamentos?>
//Recordatorio operations
    @Upsert
    suspend fun upsertRecordatorio(recordatorio: Recordatorio)

    @Delete
    suspend fun deleteRecordatorio(recordatorio: Recordatorio)

    @Query("SELECT * FROM recordatorio WHERE medicamentoId = :medicamentoId")
    fun getRecordatoriosByMedicamentoId(medicamentoId: Int): Flow<List<Recordatorio>>

}
