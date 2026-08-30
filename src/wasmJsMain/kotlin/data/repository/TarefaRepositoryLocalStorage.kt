package data.repository

import domain.model.Tarefa
import domain.repository.TarefaRepository
import kotlinx.browser.localStorage
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementação de [TarefaRepository] que persiste os dados em JSON
 * no localStorage do navegador, sob a chave "planner_tarefas".
 */
class TarefaRepositoryLocalStorage : TarefaRepository {

    private val chave = "planner_tarefas"
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    private fun carregar(): MutableList<Tarefa> {
        val conteudo = localStorage.getItem(chave) ?: return mutableListOf()
        return try {
            json.decodeFromString<List<Tarefa>>(conteudo).toMutableList()
        } catch (e: Throwable) {
            mutableListOf()
        }
    }

    private fun persistir(lista: List<Tarefa>) {
        try {
            localStorage.setItem(chave, json.encodeToString(lista))
        } catch (e: Throwable) {}
    }

    override fun salvar(tarefa: Tarefa) {
        val lista = carregar()
        lista.removeAll { it.id == tarefa.id }
        lista.add(tarefa)
        persistir(lista)
    }

    override fun buscarPorData(data: LocalDate): List<Tarefa> =
        carregar().filter { it.data == data }

    override fun buscarTodas(): List<Tarefa> = carregar()

    override fun remover(id: String) {
        val lista = carregar()
        lista.removeAll { it.id == id }
        persistir(lista)
    }
}
