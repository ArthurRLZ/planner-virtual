package br.edu.ufapetro.planner.domain.usecase.painel

import br.edu.ufapetro.planner.domain.model.*
import domain.repository.LembreteRepository
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import domain.model.Tarefa
import domain.model.Lembrete
import domain.model.Recorrencia
import domain.model.StatusTarefa
import domain.model.StatusMeta
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

class GerarResumoDoDiaUseCase(
    private val tarefaRepository: TarefaRepository,
    private val metaRepository: MetaRepository,
    private val lembreteRepository: LembreteRepository
) {
    operator fun invoke(data: LocalDate): ResumoDoDia {
        val tarefasDoDia = tarefaRepository.buscarPorData(data)

        return ResumoDoDia(
            tarefasPendentes = tarefasDoDia.filter { it.status == StatusTarefa.PENDENTE },
            tarefasConcluidas = tarefasDoDia.filter { it.status == StatusTarefa.EXECUTADA },
            metasEmAndamento = metaRepository.buscarTodas()
                .filter { it.status != StatusMeta.CUMPRIDA && it.status != StatusMeta.NAO_CUMPRIDA },
            metasCumpridas = metaRepository.buscarTodas()
                .filter { it.status == StatusMeta.CUMPRIDA },
            proximosLembretes = proximosLembretes(lembreteRepository.buscarTodos(), data),
            indicadorProdutividade = calcularProdutividade(tarefasDoDia)
        )
    }

    private fun calcularProdutividade(tarefas: List<Tarefa>): Float {
        if (tarefas.isEmpty()) return 0f
        val concluidas = tarefas.count { it.status == StatusTarefa.EXECUTADA }
        return concluidas.toFloat() / tarefas.size
    }

    /**
     * Ordena os lembretes pela proximidade em relação a [data] e retorna os [limite] mais próximos.
     *
     * - Lembretes ÚNICO não têm data própria no modelo atual, então são tratados como
     *   prioridade máxima (equivalente a "hoje").
     * - Lembretes SEMANAL são ordenados pela quantidade de dias até a próxima ocorrência
     *   do diaSemana configurado, a partir de [data] (0 = hoje, 6 = daqui a uma semana).
     */
    private fun proximosLembretes(
        lembretes: List<Lembrete>,
        data: LocalDate,
        limite: Int = 5
    ): List<Lembrete> = lembretes
        .sortedBy { diasAteProximaOcorrencia(it, data) }
        .take(limite)

    private fun diasAteProximaOcorrencia(lembrete: Lembrete, data: LocalDate): Int {
        val diaSemana = lembrete.diaSemana
        if (lembrete.recorrencia == Recorrencia.UNICO || diaSemana == null) return 0

        val diaReferencia = data.dayOfWeek.isoDayNumber - 1 // Segunda=0 .. Domingo=6
        val diaAlvo = diaSemana.ordinal
        return (diaAlvo - diaReferencia + 7) % 7
    }
}