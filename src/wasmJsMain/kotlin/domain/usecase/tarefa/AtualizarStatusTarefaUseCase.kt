package domain.usecase.tarefa

import domain.model.StatusTarefa
import domain.model.Tarefa
import domain.repository.TarefaRepository

/**
 * Caso de uso responsável por atualizar o status de execução de uma Tarefa (Issue #8).
 */
class AtualizarStatusTarefaUseCase(
    private val tarefaRepository: TarefaRepository
) {
    operator fun invoke(idTarefa: String, novoStatus: StatusTarefa): Result<Tarefa> {
        val tarefa = tarefaRepository.buscarTodas().find { it.id == idTarefa }
            ?: return Result.failure(NoSuchElementException("Tarefa com ID '$idTarefa' não foi encontrada."))

        val tarefaAtualizada = tarefa.copy(status = novoStatus)
        tarefaRepository.salvar(tarefaAtualizada)
        return Result.success(tarefaAtualizada)
    }
}
