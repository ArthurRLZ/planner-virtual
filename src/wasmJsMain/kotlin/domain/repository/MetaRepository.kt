package domain.repository

import domain.model.Meta
import domain.model.PeriodoMeta

interface MetaRepository {
    fun salvar(meta: Meta)
    fun buscarPorPeriodo(periodo: PeriodoMeta): List<Meta>
    fun buscarTodas(): List<Meta>
    fun remover(id: String)
}
