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
import kotlinx.datetime.LocalDate

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
        }.maxBy { it.value.size }
        val inicio = grupo.key
        val fim = if (agruparPorMes) {
            val proximoMes = if (inicio.month == 12) {
                LocalDate(inicio.year + 1, 1, 1)
            } else {
                LocalDate(inicio.year, inicio.month + 1, 1)
            }
            proximoMes.minusUmDia()
        } else {
            inicio
        }
        return PeriodoProdutivo(inicio, fim, grupo.value.size)
    }

    private fun LocalDate.minusUmDia(): LocalDate {
        val diasNoMes = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
            else -> error("Mês inválido")
        }
        return if (dayOfMonth > 1) {
            LocalDate(year, month, dayOfMonth - 1)
        } else if (month > 1) {
            LocalDate(year, month - 1, when (month - 1) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
                else -> diasNoMes
            })
        } else {
            LocalDate(year - 1, 12, 31)
        }
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