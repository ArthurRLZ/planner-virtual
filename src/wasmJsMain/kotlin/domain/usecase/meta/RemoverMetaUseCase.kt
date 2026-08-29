package domain.usecase.meta

import domain.repository.MetaRepository

class RemoverMetaUseCase(
    private val metaRepository: MetaRepository
) {
    operator fun invoke(idMeta: String) {
        metaRepository.remover(idMeta)
    }
}
