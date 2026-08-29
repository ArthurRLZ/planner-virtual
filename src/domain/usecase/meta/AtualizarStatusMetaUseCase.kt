package br.edu.ufapetro.planner.domain.usecase.meta

import br.edu.ufapetro.planner.domain.model.Meta
import br.edu.ufapetro.planner.domain.model.StatusMeta
import br.edu.ufapetro.planner.domain.repository.MetaRepository

class AtualizarStatusMetaUseCase(
    private val metaRepository: MetaRepository
) {
    operator fun invoke(idMeta: String, novoStatus: StatusMeta): Result<Meta> {
        val meta = metaRepository.buscarTodas().find { it.id == idMeta }
            ?: return Result.failure(NoSuchElementException("Meta não encontrada"))

        val metaAtualizada = meta.copy(status = novoStatus)
        metaRepository.salvar(metaAtualizada)
        return Result.success(metaAtualizada)
    }
}