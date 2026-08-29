package data.repository

import domain.model.Lembrete
import domain.repository.LembreteRepository
import java.time.LocalDate

class LembreteRepositoryEmMemoria : LembreteRepository {
    private val lembretes = mutableListOf<Lembrete>()

    override fun salvar(lembrete: Lembrete) {
        lembretes.removeIf { it.id == lembrete.id }
        lembretes.add(lembrete)
    }

    override fun buscarPorPeriodo(periodo: PeriodoLembrete) =
        lembretes.filter { it.periodo == periodo }

    override fun buscarTodas() = lembretes.toList()

    override fun remover(id: String) {
        lembretes.removeIf { it.id == id }
    }
}