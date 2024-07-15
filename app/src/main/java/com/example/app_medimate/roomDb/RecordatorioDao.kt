package com.example.app_medimate.roomDb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordatorioDao {
    @Insert
    suspend fun insertRecordatorio(recordatorio: Recordatorio)

    @Update
    suspend fun updateRecordatorio(recordatorio: Recordatorio)

    @Delete
    suspend fun deleteRecordatorio(recordatorio: Recordatorio)

    @Query("SELECT * FROM Recordatorio WHERE medicamentoId = :medicamentoId")
    fun getRecordatoriosForMedicamento(medicamentoId: Int): Flow<List<Recordatorio>>
}