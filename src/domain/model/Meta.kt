package domain.model

import java.time.LocalDate

data class Meta(
    val id: String,
    val descricao: String,
    val categoria: Categoria,
    val status: StatusMeta,
    val periodo: PeriodoMeta,
    val dataInicio: LocalDate,
    val dataFim: LocalDate
)