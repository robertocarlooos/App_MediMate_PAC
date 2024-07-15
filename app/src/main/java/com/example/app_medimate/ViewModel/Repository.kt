import com.example.app_medimate.roomDb.MedicamentoDataBase
import com.example.app_medimate.roomDb.Medicamentos
import com.example.app_medimate.roomDb.Recordatorio
import kotlinx.coroutines.flow.Flow

class Repository(private val db: MedicamentoDataBase) {
    suspend fun upsertMedicamentos(medicamento: Medicamentos) {
        db.dao.upsertMedicamento(medicamento)
    }

    suspend fun deleteMedicamentos(medicamento: Medicamentos) {
        db.dao.deleteMedicamento(medicamento)
    }

    fun getAllMedicamentos(): Flow<List<Medicamentos>> {
        return db.dao.getAllMedicamentos()
    }

//recordatorio operations
    suspend fun insertRecordatorio(recordatorio: Recordatorio) {
        db.recordatorioDao.insertRecordatorio(recordatorio)
    }

    suspend fun deleteRecordatorio(recordatorio: Recordatorio) {
        db.recordatorioDao.deleteRecordatorio(recordatorio)
    }

    fun getRecordatoriosForMedicamento(medicamentoId: Int): Flow<List<Recordatorio>> {
        return db.recordatorioDao.getRecordatoriosForMedicamento(medicamentoId)
    }

}