package data.repository

import domain.model.Tarefa
import domain.repository.TarefaRepository
import kotlinx.datetime.LocalDate

class TarefaRepositoryEmMemoria : TarefaRepository {
    private val tarefas = mutableListOf<Tarefa>()

    override fun salvar(tarefa: Tarefa) {
        tarefas.removeAll { it.id == tarefa.id } // evita duplicar em caso de edição
        tarefas.add(tarefa)
    }

    override fun buscarPorData(data: LocalDate): List<Tarefa> =
        tarefas.filter { it.data == data }

    override fun buscarTodas(): List<Tarefa> = tarefas.toList()

    override fun remover(id: String) {
        tarefas.removeAll { it.id == id }
    }
}
