package domain.usecase.meta

import domain.model.Meta
import domain.model.StatusMeta
import domain.repository.MetaRepository

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
