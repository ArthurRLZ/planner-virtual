package domain.usecase.painel

import planner.domain.model.AtividadesDoDia
import domain.model.DiaSemana
import domain.model.Recorrencia
import domain.repository.LembreteRepository
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * Para cada dia do mês informado, indica se existe tarefa, meta ou lembrete
 * (semanal) associado — usado para pintar as bordas do calendário do Painel.
 */
class ListarAtividadesDoMesUseCase(
    private val tarefaRepository: TarefaRepository,
    private val metaRepository: MetaRepository,
    private val lembreteRepository: LembreteRepository
) {
    operator fun invoke(ano: Int, mes: Int): Map<LocalDate, AtividadesDoDia> {
        val primeiroDia = LocalDate(ano, mes, 1)
        val ultimoDia = primeiroDia.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

        val diasComTarefa = tarefaRepository.buscarTodas()
            .map { it.data }
            .filter { it in primeiroDia..ultimoDia }
            .toSet()

        val metasDoPeriodo = metaRepository.buscarTodas()
            .filter { it.dataInicio <= ultimoDia && it.dataFim >= primeiroDia }

        // Só lembretes SEMANAIS "caem" em dias específicos do calendário —
        // lembrete ÚNICO ainda não tem data no modelo (ver observação no chat).
        val diasSemanaComLembrete = lembreteRepository.buscarTodos()
            .filter { it.recorrencia == Recorrencia.SEMANAL }
            .mapNotNull { it.diaSemana }
            .toSet()

        val resultado = mutableMapOf<LocalDate, AtividadesDoDia>()
        var dia = primeiroDia
        while (dia <= ultimoDia) {
            val temTarefa = dia in diasComTarefa
            val temMeta = metasDoPeriodo.any { dia >= it.dataInicio && dia <= it.dataFim }
            val temLembrete = dia.dayOfWeek.paraDiaSemana() in diasSemanaComLembrete

            if (temTarefa || temMeta || temLembrete) {
                resultado[dia] = AtividadesDoDia(temTarefa, temMeta, temLembrete)
            }
            dia = dia.plus(1, DateTimeUnit.DAY)
        }
        return resultado
    }

    private fun DayOfWeek.paraDiaSemana(): DiaSemana = when (this) {
        DayOfWeek.MONDAY -> DiaSemana.SEGUNDA
        DayOfWeek.TUESDAY -> DiaSemana.TERCA
        DayOfWeek.WEDNESDAY -> DiaSemana.QUARTA
        DayOfWeek.THURSDAY -> DiaSemana.QUINTA
        DayOfWeek.FRIDAY -> DiaSemana.SEXTA
        DayOfWeek.SATURDAY -> DiaSemana.SABADO
        DayOfWeek.SUNDAY -> DiaSemana.DOMINGO
        else -> DiaSemana.SEGUNDA
    }
}