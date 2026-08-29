import androidx.compose.ui.window.application
import androidx.compose.ui.window.Window
import data.repository.TarefaRepositoryEmMemoria
import domain.usecase.tarefa.CriarTarefaUseCase
import ui.screens.TarefasScreen

fun main() = application {
    val tarefaRepository = TarefaRepositoryEmMemoria()
    val metaRepository = MetaRepositoryEmMemoria()
    val lembreteRepository = LembreteRepositoryEmMemoria()

    val criarTarefaUseCase = CriarTarefaUseCase(tarefaRepository)
    val criarMetaUseCase = CriarMetaUseCase(metaRepository)
    val criarLembreteUseCase = CriarLembreteUseCase(lembreteRepository)

    Window(onCloseRequest = ::exitApplication, title = "Planner Virtual") {
        TarefasScreen(criarTarefaUseCase)
    }
}