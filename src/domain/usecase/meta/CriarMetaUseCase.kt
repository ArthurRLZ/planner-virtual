package br.edu.ufapetro.planner.domain.usecase.meta

import br.edu.ufapetro.planner.domain.model.Meta
import br.edu.ufapetro.planner.domain.repository.MetaRepository

class CriarMetaUseCase(
    private val metaRepository: MetaRepository
) {
    operator fun invoke(meta: Meta): Result<Meta> {
        metaRepository.salvar(meta)
        return Result.success(meta)
    }
}