package domain.model

import kotlinx.datetime.LocalDate

data class PeriodoProdutivo(
    val inicio: LocalDate,
    val fim: LocalDate,
    val tarefasExecutadas: Int
)

data class Relatorio(
    val inicio: LocalDate,
    val fim: LocalDate,
    val percentualMetasCumpridas: Float,
    val percentualTarefasExecutadas: Float,
    val periodoMaisProdutivo: PeriodoProdutivo?,
    val turnoMaisProdutivo: Turno?,
    val categoriasTarefaMaisRealizadas: List<Categoria>,
    val categoriasMetaMaisRealizadas: List<Categoria>
)