package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import br.edu.ufapetro.planner.domain.usecase.painel.GerarResumoDoDiaUseCase
import br.edu.ufapetro.planner.ui.screens.PainelScreen
import ui.screens.LembretesScreen
import data.repository.LembreteRepositoryLocalStorage
import data.repository.MetaRepositoryLocalStorage
import data.repository.TarefaRepositoryLocalStorage
import domain.usecase.lembrete.CriarLembreteUseCase
import domain.usecase.meta.AtualizarStatusMetaUseCase
import domain.usecase.meta.CriarMetaUseCase
import domain.usecase.meta.ListarMetasUseCase
import domain.usecase.tarefa.CriarTarefaUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ui.screens.MetasScreen
import ui.screens.TarefasScreen

@Composable
fun App() {
    // Persistência real no navegador
    val tarefaRepository = remember { TarefaRepositoryLocalStorage() }
    val metaRepository = remember { MetaRepositoryLocalStorage() }
    val lembreteRepository = remember { LembreteRepositoryLocalStorage() }

    // UseCases
    val criarTarefaUseCase = remember { CriarTarefaUseCase(tarefaRepository) }
    val criarMetaUseCase = remember { CriarMetaUseCase(metaRepository) }
    val listarMetasUseCase = remember { ListarMetasUseCase(metaRepository) }
    val atualizarStatusMetaUseCase = remember { AtualizarStatusMetaUseCase(metaRepository) }
    val criarLembreteUseCase = remember { CriarLembreteUseCase(lembreteRepository) }

    // Novo UseCase para o Painel
    val gerarResumoDoDiaUseCase = remember {
        GerarResumoDoDiaUseCase(
            tarefaRepository = tarefaRepository,
            metaRepository = metaRepository,
            lembreteRepository = lembreteRepository
        )
    }

    MaterialTheme {
        var telaAtual by remember { mutableStateOf("painel") }

        Column {
            // Botões temporários de alternância de tela
            Row {
                Button(onClick = { telaAtual = "painel" }) { Text("Painel") }
                Button(onClick = { telaAtual = "metas" }) { Text("Metas") }
                Button(onClick = { telaAtual = "tarefas" }) { Text("Tarefas") }
                Button(onClick = { telaAtual = "lembretes" }) { Text("Lembretes") }
            }

            when (telaAtual) {
                "painel" -> PainelScreen(
                    gerarResumoDoDiaUseCase = gerarResumoDoDiaUseCase,
                    now = { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
                )
                "metas" -> MetasScreen(criarMetaUseCase, listarMetasUseCase, atualizarStatusMetaUseCase)
                "tarefas" -> TarefasScreen(criarTarefaUseCase)
                "lembretes" -> LembretesScreen(criarLembreteUseCase)
            }
        }
    }
}