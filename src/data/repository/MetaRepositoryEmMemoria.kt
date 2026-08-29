package data.repository

import domain.model.Meta
import domain.repository.MetaRepository
import java.time.LocalDate


class MetaRepositoryEmMemoria : MetaRepository {
    private val metas = mutableListOf<Meta>()

    override fun salvar(meta: Meta) {
        metas.removeIf { it.id == meta.id }
        metas.add(meta)
    }

    override fun buscarPorPeriodo(periodo: PeriodoMeta) =
        metas.filter { it.periodo == periodo }

    override fun buscarTodas() = metas.toList()

    override fun remover(id: String) {
        metas.removeIf { it.id == id }
    }
}