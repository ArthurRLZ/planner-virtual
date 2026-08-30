package domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * Entidade de Domínio representando uma Tarefa no Planner Virtual (Issue #6).
 *
 * Suporta agendamentos com horários pontuais e duração em blocos (30min ou 1h),
 * ou alocações por turno (Manhã, Tarde, Noite).
 */
@Serializable
data class Tarefa(
    val id: String,
    val descricao: String,
    val categoria: Categoria,
    val data: LocalDate,
    val horarioInicio: LocalTime? = null,
    val duracaoMinutos: Int? = null,
    val blocoTempo: BlocoTempo? = null,
    val turno: Turno? = null,
    val status: StatusTarefa = StatusTarefa.PENDENTE,
    val prioridade: Prioridade = Prioridade.MEDIA
) {
    init {
        require(descricao.isNotBlank()) {
            "A descrição da tarefa não pode ser vazia"
        }
        require((horarioInicio != null && (duracaoMinutos != null || blocoTempo != null)) || turno != null) {
            "A tarefa deve ter um horário de início com duração (30min ou 1h) ou um turno definido"
        }
        if (horarioInicio != null) {
            val duracao = duracaoEfetivaMinutos()
            require(duracao == 30 || duracao == 60) {
                "Para tarefas com horário específico, a duração deve ser de 30 ou 60 minutos"
            }
        }
    }

    /**
     * Retorna a duração efetiva da tarefa em minutos (30 ou 60) quando alocada por horário.
     */
    fun duracaoEfetivaMinutos(): Int? = duracaoMinutos ?: blocoTempo?.minutos

    /**
     * Calcula o horário de término previsto da tarefa (horarioInicio + duracao).
     */
    fun horarioFim(): LocalTime? {
        val inicio = horarioInicio ?: return null
        val duracao = duracaoEfetivaMinutos() ?: return null
        val totalMinutosFim = inicio.hour * 60 + inicio.minute + duracao
        val horaFim = (totalMinutosFim / 60) % 24
        val minutoFim = totalMinutosFim % 60
        return LocalTime(horaFim, minutoFim)
    }

    /**
     * Converte o horário de início em minutos desde o início do dia (00:00).
     */
    fun inicioEmMinutos(): Int? {
        val inicio = horarioInicio ?: return null
        return inicio.hour * 60 + inicio.minute
    }

    /**
     * Converte o horário de término em minutos desde o início do dia (00:00).
     */
    fun fimEmMinutos(): Int? {
        val inicioMinutos = inicioEmMinutos() ?: return null
        val duracao = duracaoEfetivaMinutos() ?: return null
        return inicioMinutos + duracao
    }
}
