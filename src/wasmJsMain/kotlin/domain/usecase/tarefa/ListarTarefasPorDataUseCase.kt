package domain.usecase.tarefa

import domain.model.Tarefa
import domain.repository.TarefaRepository
import kotlinx.datetime.LocalDate

/**
 * Caso de uso para listar e ordenar as tarefas de um dia específico (Issue #7).
 */
class ListarTarefasPorDataUseCase(
    private val tarefaRepository: TarefaRepository
) {
    operator fun invoke(data: LocalDate): List<Tarefa> {
        return tarefaRepository.buscarPorData(data)
            .sortedWith(
                compareBy<Tarefa> { it.horarioInicio == null }
                    .thenBy { it.horarioInicio?.hour ?: 0 }
                    .thenBy { it.horarioInicio?.minute ?: 0 }
                    .thenBy { it.turno?.ordinal ?: 0 }
            )
    }
}
