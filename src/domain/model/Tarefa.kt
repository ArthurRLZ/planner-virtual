package domain.model

import java.time.LocalDate
import java.time.LocalTime

data class Tarefa(
    val id: String,
    val descricao: String,
    val categoria: Categoria,
    val data: LocalDate,
    val horarioInicio: LocalTime?,   // null se for por turno
    val turno: Turno?,               // null se for por horário
    val status: StatusTarefa,
    val prioridade: Prioridade
) {
    init {
        require(horarioInicio != null || turno != null) {
            "Tarefa deve ter horário ou turno definido"
        }
    }
}