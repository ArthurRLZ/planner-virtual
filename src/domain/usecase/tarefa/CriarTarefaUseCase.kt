package domain.usecase.tarefa

import domain.model.Tarefa
import domain.repository.TarefaRepository

class CriarTarefaUseCase(
    private val tarefaRepository: TarefaRepository
) {
    operator fun invoke(tarefa: Tarefa): Result<Tarefa> {
        // regra de negócio: não permitir tarefa duplicada no mesmo horário
        val conflito = tarefaRepository.buscarPorData(tarefa.data)
            .any { it.horarioInicio == tarefa.horarioInicio && it.id != tarefa.id }

        if (conflito) {
            return Result.failure(IllegalStateException("Já existe tarefa nesse horário"))
        }

        tarefaRepository.salvar(tarefa)
        return Result.success(tarefa)
    }
}