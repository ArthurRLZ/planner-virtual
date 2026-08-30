package domain.usecase.tarefa

import domain.repository.TarefaRepository

/**
 * Caso de uso responsável pela exclusão de uma Tarefa do repositório.
 */
class RemoverTarefaUseCase(
    private val tarefaRepository: TarefaRepository
) {
    operator fun invoke(idTarefa: String) {
        tarefaRepository.remover(idTarefa)
    }
}
