package br.edu.ufapetro.planner.domain.repository

import br.edu.ufapetro.planner.domain.model.Meta
import br.edu.ufapetro.planner.domain.model.PeriodoMeta

interface MetaRepository {
    fun salvar(meta: Meta)
    fun buscarPorPeriodo(periodo: PeriodoMeta): List<Meta>
    fun buscarTodas(): List<Meta>
    fun remover(id: String)
}