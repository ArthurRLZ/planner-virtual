package planner.domain.model

data class AtividadesDoDia(
    val temTarefa: Boolean = false,
    val temMeta: Boolean = false,
    val temLembrete: Boolean = false
) {
    val temAlgumaAtividade: Boolean
        get() = temTarefa || temMeta || temLembrete
}