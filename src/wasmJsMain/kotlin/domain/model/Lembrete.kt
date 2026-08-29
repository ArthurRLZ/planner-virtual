package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Lembrete(
    val id: String,
    val descricao: String,
    val tipo: TipoLembrete,
    val recorrencia: Recorrencia,
    val diaSemana: DiaSemana?  // usado quando recorrência é SEMANAL
)
