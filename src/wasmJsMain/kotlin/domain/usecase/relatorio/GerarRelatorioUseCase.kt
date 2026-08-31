package domain.usecase.relatorio

import domain.model.Categoria
import domain.model.Meta
import domain.model.PeriodoProdutivo
import domain.model.Relatorio
import domain.model.StatusMeta
import domain.model.StatusTarefa
import domain.model.Tarefa
import domain.model.Turno
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.number

internal class GerarRelatorioUseCase(
    private val tarefaRepository: TarefaRepository,
    private val metaRepository: MetaRepository
) {
    fun gerar(inicio: LocalDate, fim: LocalDate, agruparPorMes: Boolean): Relatorio {
        val tarefas = tarefaRepository.buscarTodas().filter { it.data in inicio..fim }
        val metas = metaRepository.buscarTodas().filter { it.sobrepoe(inicio, fim) }
        val tarefasExecutadas = tarefas.filter { it.status == StatusTarefa.EXECUTADA }
        val metasCumpridas = metas.filter { it.status == StatusMeta.CUMPRIDA }

        return Relatorio(
            inicio = inicio,
            fim = fim,
            percentualMetasCumpridas = percentual(metasCumpridas.size, metas.size),
            percentualTarefasExecutadas = percentual(tarefasExecutadas.size, tarefas.size),
            periodoMaisProdutivo = tarefasExecutadas.periodoMaisProdutivo(agruparPorMes),
            turnoMaisProdutivo = tarefasExecutadas.turnoMaisProdutivo(),
            categoriasTarefaMaisRealizadas = tarefasExecutadas.categoriasMaisRealizadas { it.categoria },
            categoriasMetaMaisRealizadas = metasCumpridas.categoriasMaisRealizadas { it.categoria }
        )
    }

    private fun Meta.sobrepoe(inicio: LocalDate, fim: LocalDate): Boolean =
        dataInicio <= fim && dataFim >= inicio

    private fun percentual(parte: Int, total: Int): Float =
        if (total == 0) 0f else parte.toFloat() / total * 100f

    private fun List<Tarefa>.periodoMaisProdutivo(agruparPorMes: Boolean): PeriodoProdutivo? {
        if (isEmpty()) return null

        val grupo = groupBy {
            if (agruparPorMes) LocalDate(it.data.year, it.data.month, 1) else it.data
        }.maxByOrNull { it.value.size } ?: return null

        val inicio = grupo.key
        val fim = if (agruparPorMes) {
            val proximoMes = if (inicio.month.number == 12) {
                LocalDate(inicio.year + 1, 1, 1)
            } else {
                LocalDate(inicio.year, inicio.month.number + 1, 1)
            }
            proximoMes.minus(1, DateTimeUnit.DAY)
        } else {
            inicio
        }
        return PeriodoProdutivo(inicio, fim, grupo.value.size)
    }

    private fun List<Tarefa>.turnoMaisProdutivo(): Turno? =
        mapNotNull { it.turno }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

    private fun <Item> List<Item>.categoriasMaisRealizadas(
        categoria: (Item) -> Categoria
    ): List<Categoria> {
        val contagens = groupingBy(categoria).eachCount()
        val maiorQuantidade = contagens.values.maxOrNull() ?: return emptyList()
        return contagens.filterValues { it == maiorQuantidade }.keys.sortedBy { it.ordinal }
    }
}