package domain.repository

import domain.model.Tarefa
import java.time.LocalDate

interface TarefaRepository {
    fun salvar(tarefa: Tarefa)
    fun buscarPorData(data: LocalDate): List<Tarefa>
    fun buscarTodas(): List<Tarefa>
    fun remover(id: String)
}