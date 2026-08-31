package domain.usecase.lembrete

import domain.model.Lembrete
import domain.repository.LembreteRepository

class ListarLembretesUseCase(
    private val lembreteRepository: LembreteRepository
) {
    operator fun invoke(): List<Lembrete> = lembreteRepository.buscarTodos()
}

