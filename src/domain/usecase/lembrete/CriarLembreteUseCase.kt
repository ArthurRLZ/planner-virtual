package br.edu.ufapetro.planner.domain.usecase.lembrete

import br.edu.ufapetro.planner.domain.model.Lembrete
import br.edu.ufapetro.planner.domain.repository.LembreteRepository

class CriarLembreteUseCase(
    private val lembreteRepository: LembreteRepository
) {
    operator fun invoke(lembrete: Lembrete): Result<Lembrete> {
        lembreteRepository.salvar(lembrete)
        return Result.success(lembrete)
    }
}