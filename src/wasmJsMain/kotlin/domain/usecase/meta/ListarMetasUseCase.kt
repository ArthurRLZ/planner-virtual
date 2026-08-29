package domain.usecase.meta

import domain.model.Meta
import domain.model.PeriodoMeta
import domain.repository.MetaRepository

class ListarMetasUseCase(
    private val metaRepository: MetaRepository
) {
    /** Sem período informado, retorna todas as metas. */
    operator fun invoke(periodo: PeriodoMeta? = null): List<Meta> =
        if (periodo == null) metaRepository.buscarTodas()
        else metaRepository.buscarPorPeriodo(periodo)
}
