package data.repository

import domain.model.DiaSemana
import domain.model.Lembrete
import domain.repository.LembreteRepository
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementação de [LembreteRepository] que persiste os dados em JSON
 * no localStorage do navegador, sob a chave "planner_lembretes".
 */
class LembreteRepositoryLocalStorage : LembreteRepository {

    private val chave = "planner_lembretes"
    private val json = Json { ignoreUnknownKeys = true }

    private fun carregar(): MutableList<Lembrete> {
        val conteudo = localStorage.getItem(chave) ?: return mutableListOf()
        return json.decodeFromString<List<Lembrete>>(conteudo).toMutableList()
    }

    private fun persistir(lista: List<Lembrete>) {
        localStorage.setItem(chave, json.encodeToString(lista))
    }

    override fun salvar(lembrete: Lembrete) {
        val lista = carregar()
        lista.removeAll { it.id == lembrete.id }
        lista.add(lembrete)
        persistir(lista)
    }

    override fun buscarPorDia(dia: DiaSemana): List<Lembrete> =
        carregar().filter { it.diaSemana == dia }

    override fun buscarTodos(): List<Lembrete> = carregar()

    override fun remover(id: String) {
        persistir(carregar().filter { it.id != id })
    }
}
