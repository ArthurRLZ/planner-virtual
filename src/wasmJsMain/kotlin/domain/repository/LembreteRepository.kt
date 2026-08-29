package domain.repository

import domain.model.DiaSemana
import domain.model.Lembrete

interface LembreteRepository {
    fun salvar(lembrete: Lembrete)
    fun buscarPorDia(dia: DiaSemana): List<Lembrete>
    fun buscarTodos(): List<Lembrete>
    fun remover(id: String)
}
