package domain.usecase.classroom

import domain.model.*
import domain.repository.TarefaRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json
import util.novoId

class SincronizarClassroomUseCase(
    private val tarefaRepository: TarefaRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun processarCursos(jsonCursos: String): List<ClassroomCourseDto> =
        json.decodeFromString<ClassroomCoursesResponse>(jsonCursos).courses

    fun processarESalvarTarefas(jsonCourseWork: String) {
        val itens = json.decodeFromString<ClassroomCourseWorkResponse>(jsonCourseWork).courseWork

        itens.forEach { item ->
            val data = item.dueDate?.let { LocalDate(it.year, it.month, it.day) }
                ?: return@forEach // sem data de entrega: pula por enquanto

            val hora = item.dueTime?.let { LocalTime(it.hours, it.minutes) } ?: LocalTime(23, 59)

            tarefaRepository.salvar(
                Tarefa(
                    id = "classroom-${item.id}",
                    descricao = item.title,
                    categoria = Categoria.FACULDADE,
                    data = data,
                    horarioInicio = hora,
                    duracaoMinutos = BlocoTempo.MEIA_HORA.minutos,
                    blocoTempo = BlocoTempo.MEIA_HORA,
                    turno = null,
                    status = StatusTarefa.PENDENTE,
                    prioridade = Prioridade.MEDIA,
                    linkExterno = item.alternateLink
                )
            )
        }
    }
}