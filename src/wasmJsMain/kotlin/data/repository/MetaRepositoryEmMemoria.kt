package data.repository

import domain.model.Meta
import domain.model.PeriodoMeta
import domain.repository.MetaRepository

class MetaRepositoryEmMemoria : MetaRepository {
    private val metas = mutableListOf<Meta>()

    override fun salvar(meta: Meta) {
        metas.removeAll { it.id == meta.id }
        metas.add(meta)
    }

    override fun buscarPorPeriodo(periodo: PeriodoMeta): List<Meta> =
        metas.filter { it.periodo == periodo }

    override fun buscarTodas(): List<Meta> = metas.toList()

    override fun remover(id: String) {
        metas.removeAll { it.id == id }
    }
}
