package domain.usecase.relatorio

import domain.model.Relatorio
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class GerarRelatorioMensalUseCase(
    tarefaRepository: TarefaRepository,
    metaRepository: MetaRepository
) {
    private val gerador = GerarRelatorioUseCase(tarefaRepository, metaRepository)

    operator fun invoke(dataDeReferencia: LocalDate): Relatorio {
        val inicio = LocalDate(dataDeReferencia.year, dataDeReferencia.month, 1)
        val fim = inicio.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
        return gerador.gerar(inicio, fim, agruparPorMes = false)
    }
}