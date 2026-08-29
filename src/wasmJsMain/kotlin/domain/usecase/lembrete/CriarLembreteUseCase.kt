package domain.usecase.lembrete

import domain.model.Lembrete
import domain.repository.LembreteRepository

class CriarLembreteUseCase(
    private val lembreteRepository: LembreteRepository
) {
    operator fun invoke(lembrete: Lembrete): Result<Lembrete> {
        lembreteRepository.salvar(lembrete)
        return Result.success(lembrete)
    }
}
