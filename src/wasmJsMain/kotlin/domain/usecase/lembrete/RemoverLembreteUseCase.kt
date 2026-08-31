package domain.usecase.lembrete

import domain.repository.LembreteRepository

class RemoverLembreteUseCase(
    private val lembreteRepository: LembreteRepository
) {
    operator fun invoke(idLembrete: String) {
        lembreteRepository.remover(idLembrete)
    }
}

