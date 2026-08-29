package domain.repository

import domain.model.Tarefa
import kotlinx.datetime.LocalDate

interface TarefaRepository {
    fun salvar(tarefa: Tarefa)
    fun buscarPorData(data: LocalDate): List<Tarefa>
    fun buscarTodas(): List<Tarefa>
    fun remover(id: String)
}
