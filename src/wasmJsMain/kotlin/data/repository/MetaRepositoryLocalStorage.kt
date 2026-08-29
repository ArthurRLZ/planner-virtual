package data.repository

import domain.model.Meta
import domain.model.PeriodoMeta
import domain.repository.MetaRepository
import kotlinx.browser.localStorage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementação de [MetaRepository] que persiste os dados em JSON
 * no localStorage do navegador, sob a chave "planner_metas".
 */
class MetaRepositoryLocalStorage : MetaRepository {

    private val chave = "planner_metas"
    private val json = Json { ignoreUnknownKeys = true }

    private fun carregar(): MutableList<Meta> {
        val conteudo = localStorage.getItem(chave) ?: return mutableListOf()
        return json.decodeFromString<List<Meta>>(conteudo).toMutableList()
    }

    private fun persistir(lista: List<Meta>) {
        localStorage.setItem(chave, json.encodeToString(lista))
    }

    override fun salvar(meta: Meta) {
        val lista = carregar()
        lista.removeAll { it.id == meta.id }
        lista.add(meta)
        persistir(lista)
    }

    override fun buscarPorPeriodo(periodo: PeriodoMeta): List<Meta> =
        carregar().filter { it.periodo == periodo }

    override fun buscarTodas(): List<Meta> = carregar()

    override fun remover(id: String) {
        persistir(carregar().filter { it.id != id })
    }
}
