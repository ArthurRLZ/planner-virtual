package br.edu.ufapetro.planner.domain.usecase.painel

import br.edu.ufapetro.planner.domain.model.*
import domain.repository.LembreteRepository
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import domain.model.Tarefa
import domain.model.StatusTarefa
import domain.model.StatusMeta
import kotlinx.datetime.LocalDate

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
                .filter {it.status == StatusMeta.CUMPRIDA},
            proximosLembretes = lembreteRepository.buscarTodos().take(5),
            indicadorProdutividade = calcularProdutividade(tarefasDoDia)
        )
    }

    private fun calcularProdutividade(tarefas: List<Tarefa>): Float {
        if (tarefas.isEmpty()) return 0f
        val concluidas = tarefas.count { it.status == StatusTarefa.EXECUTADA }
        return concluidas.toFloat() / tarefas.size
    }
}