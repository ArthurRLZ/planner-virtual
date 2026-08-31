package domain.usecase.relatorio

import domain.model.Relatorio
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

class GerarRelatorioSemanalUseCase(
    tarefaRepository: TarefaRepository,
    metaRepository: MetaRepository
) {
    private val gerador = GerarRelatorioUseCase(tarefaRepository, metaRepository)

    operator fun invoke(dataDeReferencia: LocalDate): Relatorio {
        val inicio = dataDeReferencia.minus(dataDeReferencia.dayOfWeek.ordinal, DateTimeUnit.DAY)
        return gerador.gerar(inicio, inicio.plus(6, DateTimeUnit.DAY), agruparPorMes = false)
    }
}