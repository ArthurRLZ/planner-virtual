package domain.usecase.relatorio

import domain.model.Relatorio
import domain.repository.MetaRepository
import domain.repository.TarefaRepository
import kotlinx.datetime.LocalDate

class GerarRelatorioAnualUseCase(
    tarefaRepository: TarefaRepository,
    metaRepository: MetaRepository
) {
    private val gerador = GerarRelatorioUseCase(tarefaRepository, metaRepository)

    operator fun invoke(ano: Int): Relatorio = gerador.gerar(
        inicio = LocalDate(ano, 1, 1),
        fim = LocalDate(ano, 12, 31),
        agruparPorMes = true
    )
}