

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app_medimate.roomDb.Medicamentos
import com.example.app_medimate.roomDb.Recordatorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicamentoViewModel(private val repository: Repository) : ViewModel() {
    val medicamentosFlow: StateFlow<List<Medicamentos>> = repository.getAllMedicamentos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())




    fun upsertMedicamento(medicamentos: Medicamentos) {
        viewModelScope.launch {
            repository.upsertMedicamentos(medicamentos)
        }
    }

    fun deleteMedicamento(medicamentos: Medicamentos) {
        viewModelScope.launch {
            repository.deleteMedicamentos(medicamentos)
        }
    }
    fun getMedicamentoById(id: Int): Flow<Medicamentos?> {
        return medicamentosFlow.map { medicamentos -> medicamentos.find { it.id == id } }
    }
   // Recordatorio operations
   fun insertRecordatorio(recordatorio: Recordatorio) {
       viewModelScope.launch {
           repository.insertRecordatorio(recordatorio)
       }
   }

    fun deleteRecordatorio(recordatorio: Recordatorio) {
        viewModelScope.launch {
            repository.deleteRecordatorio(recordatorio)
        }
    }

    fun getRecordatoriosForMedicamento(medicamentoId: Int): Flow<List<Recordatorio>> {
        return repository.getRecordatoriosForMedicamento(medicamentoId)
    }
}