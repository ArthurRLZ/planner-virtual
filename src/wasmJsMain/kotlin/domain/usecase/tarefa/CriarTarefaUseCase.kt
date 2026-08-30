package domain.usecase.tarefa

import domain.model.Tarefa
import domain.repository.TarefaRepository

/**
 * Caso de uso responsável pela criação e agendamento de tarefas com validação de conflitos (Issue #6).
 *
 * Garante que:
 * - A descrição não seja vazia.
 * - Não haja sobreposição de horários (intervalos [inicio, fim)) para tarefas marcadas no mesmo dia.
 * - Tarefas adjacentes (onde o fim de uma coincide com o início da outra) sejam permitidas.
 * - Não haja duas tarefas alocadas no mesmo turno (Manhã, Tarde, Noite) no mesmo dia.
 */
class CriarTarefaUseCase(
    private val tarefaRepository: TarefaRepository
) {
    operator fun invoke(tarefa: Tarefa): Result<Tarefa> {
        if (tarefa.descricao.isBlank()) {
            return Result.failure(IllegalArgumentException("A descrição da tarefa não pode ser vazia"))
        }

        // Tarefas já cadastradas para o mesmo dia (excluindo a própria se for atualização)
        val tarefasDoDia = tarefaRepository.buscarPorData(tarefa.data)
            .filter { it.id != tarefa.id }

        // 1. Validação de Conflito para tarefas agendadas por TURNO
        if (tarefa.turno != null) {
            val conflitoTurno = tarefasDoDia.any { it.turno == tarefa.turno }
            if (conflitoTurno) {
                return Result.failure(
                    IllegalStateException("Já existe uma tarefa cadastrada no turno ${tarefa.turno} para este dia.")
                )
            }
        }

        // 2. Validação de Conflito para tarefas agendadas por BLOCO DE HORÁRIO (30min / 1h)
        if (tarefa.horarioInicio != null) {
            val inicioNova = tarefa.inicioEmMinutos()
                ?: return Result.failure(IllegalArgumentException("Horário de início inválido"))
            val fimNova = tarefa.fimEmMinutos()
                ?: return Result.failure(IllegalArgumentException("Duração da tarefa inválida"))

            val conflitoHorario = tarefasDoDia.filter { it.horarioInicio != null }.any { existente ->
                val inicioExistente = existente.inicioEmMinutos() ?: return@any false
                val fimExistente = existente.fimEmMinutos() ?: return@any false

                /**
                 * Matemática de Interseção de Intervalos Semiabertos [Inicio, Fim):
                 *
                 * Dois intervalos [A_inicio, A_fim) e [B_inicio, B_fim) possuem sobreposição (overlapping)
                 * se, e somente se:
                 *     A_inicio < B_fim  E  B_inicio < A_fim
                 *
                 * Casos de Borda (Tarefas Adjacentes / Contíguas):
                 * Se uma tarefa termina às 10:30 (A_fim = 630) e outra começa às 10:30 (B_inicio = 630):
                 *     (A_inicio < B_fim) é VERDADEIRO (ex: 600 < 690)
                 *     (B_inicio < A_fim) é FALSO (630 < 630 -> false)
                 * Como a condição 'E' exige ambas verdadeiras, a expressão retorna 'false'.
                 * Portanto, tarefas adjacentes NÃO geram conflito.
                 */
                inicioNova < fimExistente && inicioExistente < fimNova
            }

            if (conflitoHorario) {
                return Result.failure(
                    IllegalStateException("Conflito de horário: já existe uma tarefa agendada que sobrepõe este intervalo.")
                )
            }
        }

        tarefaRepository.salvar(tarefa)
        return Result.success(tarefa)
    }
}
