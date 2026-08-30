package domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Categoria {
    FACULDADE, TRABALHO, SAUDE, LAZER, PROJETOS_PESSOAIS, ESTUDOS
}

@Serializable
enum class StatusMeta {
    CUMPRIDA, PARCIALMENTE_CUMPRIDA, NAO_CUMPRIDA
}

@Serializable
enum class PeriodoMeta {
    SEMANAL, MENSAL, ANUAL
}

@Serializable
enum class StatusTarefa {
    PENDENTE, EXECUTADA, PARCIALMENTE_EXECUTADA, CANCELADA, ADIADA
}

@Serializable
enum class Prioridade {
    BAIXA, MEDIA, ALTA
}

@Serializable
enum class TipoLembrete {
    REUNIAO, LIGACAO, COMPRA, ESTUDO, EXERCICIO, ENTREGA_TRABALHO
}

@Serializable
enum class Recorrencia {
    UNICO, SEMANAL
}

@Serializable
enum class Turno {
    MANHA, TARDE, NOITE
}

@Serializable
enum class DiaSemana {
    SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO
}

@Serializable
enum class BlocoTempo(val minutos: Int?) {
    MEIA_HORA(30),
    UMA_HORA(60),
    TURNO(null)
}
