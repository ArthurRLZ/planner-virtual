package domain.usecase.classroom

import domain.model.*
import domain.repository.TarefaRepository
import kotlinx.datetime.*
import kotlinx.serialization.json.Json

class SincronizarClassroomUseCase(
    private val tarefaRepository: TarefaRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun processarCursos(jsonCursos: String): List<ClassroomCourseDto> =
        json.decodeFromString<ClassroomCoursesResponse>(jsonCursos).courses

    fun processarESalvarTarefas(jsonCourseWork: String) {
        val itens = json.decodeFromString<ClassroomCourseWorkResponse>(jsonCourseWork).courseWork
        val fusoLocal = TimeZone.currentSystemDefault()

        itens.forEach { item ->
            val dataUtc = item.dueDate ?: return@forEach

            val horaUtc = item.dueTime ?: ClassroomTimeDto(hours = 23, minutes = 59)

            // Monta o instante em UTC e converte pro fuso local
            val dataHoraUtc = LocalDateTime(
                year = dataUtc.year,
                monthNumber = dataUtc.month,
                dayOfMonth = dataUtc.day,
                hour = horaUtc.hours,
                minute = horaUtc.minutes
            ).toInstant(TimeZone.UTC)

            val dataHoraLocal = dataHoraUtc.toLocalDateTime(fusoLocal)

            tarefaRepository.salvar(
                Tarefa(
                    id = "classroom-${item.id}",
                    descricao = item.title,
                    categoria = Categoria.FACULDADE,
                    data = dataHoraLocal.date,
                    horarioInicio = LocalTime(dataHoraLocal.hour, dataHoraLocal.minute),
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