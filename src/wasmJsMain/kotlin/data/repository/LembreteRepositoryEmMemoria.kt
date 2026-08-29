package data.repository

import domain.model.DiaSemana
import domain.model.Lembrete
import domain.repository.LembreteRepository

class LembreteRepositoryEmMemoria : LembreteRepository {
    private val lembretes = mutableListOf<Lembrete>()

    override fun salvar(lembrete: Lembrete) {
        lembretes.removeAll { it.id == lembrete.id }
        lembretes.add(lembrete)
    }

    override fun buscarPorDia(dia: DiaSemana): List<Lembrete> =
        lembretes.filter { it.diaSemana == dia }

    override fun buscarTodos(): List<Lembrete> = lembretes.toList()

    override fun remover(id: String) {
        lembretes.removeAll { it.id == id }
    }
}
