package domain.model

enum class Categoria {
    FACULDADE, TRABALHO, SAUDE, LAZER, PROJETOS_PESSOAIS, ESTUDOS
}

enum class StatusMeta {
    CUMPRIDA, PARCIALMENTE_CUMPRIDA, NAO_CUMPRIDA
}

enum class PeriodoMeta {
    SEMANAL, MENSAL, ANUAL
}

enum class StatusTarefa {
    PENDENTE, EXECUTADA, PARCIALMENTE_EXECUTADA, CANCELADA, ADIADA
}

enum class Prioridade {
    BAIXA, MEDIA, ALTA
}

enum class TipoLembrete {
    REUNIAO, LIGACAO, COMPRA, ESTUDO, EXERCICIO, ENTREGA_TRABALHO
}

enum class Recorrencia {
    UNICO, SEMANAL
}

enum class Turno {
    MANHA, TARDE, NOITE
}