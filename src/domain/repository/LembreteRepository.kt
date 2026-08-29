package br.edu.ufapetro.planner.domain.repository

import br.edu.ufapetro.planner.domain.model.Lembrete
import java.time.DayOfWeek

interface LembreteRepository {
    fun salvar(lembrete: Lembrete)
    fun buscarPorDia(dia: DayOfWeek): List<Lembrete>
    fun buscarTodos(): List<Lembrete>
    fun remover(id: String)
}