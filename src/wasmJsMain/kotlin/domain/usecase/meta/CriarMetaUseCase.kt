package domain.usecase.meta

import domain.model.Meta
import domain.repository.MetaRepository

class CriarMetaUseCase(
    private val metaRepository: MetaRepository
) {
    operator fun invoke(meta: Meta): Result<Meta> {
        if (meta.descricao.isBlank()) {
            return Result.failure(IllegalArgumentException("A descrição da meta não pode ser vazia"))
        }
        if (meta.dataFim < meta.dataInicio) {
            return Result.failure(IllegalArgumentException("A data fim não pode ser anterior à data início"))
        }

        metaRepository.salvar(meta)
        return Result.success(meta)
    }
}
