package domain.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Meta(
    val id: String,
    val descricao: String,
    val categoria: Categoria,
    val status: StatusMeta,
    val periodo: PeriodoMeta,
    val dataInicio: LocalDate,
    val dataFim: LocalDate
)
