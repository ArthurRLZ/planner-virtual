package domain.model

import java.time.DayOfWeek

data class Lembrete(
    val id: String,
    val descricao: String,
    val tipo: TipoLembrete,
    val recorrencia: Recorrencia,
    val diaSemana: DayOfWeek?  // usado quando recorrência é SEMANAL
)