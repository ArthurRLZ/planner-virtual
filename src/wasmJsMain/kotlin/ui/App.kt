package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.edu.ufapetro.planner.domain.usecase.painel.GerarResumoDoDiaUseCase
import br.edu.ufapetro.planner.ui.screens.PainelScreen
import data.repository.LembreteRepositoryLocalStorage
import data.repository.MetaRepositoryLocalStorage
import data.repository.TarefaRepositoryLocalStorage
import domain.usecase.lembrete.CriarLembreteUseCase
import domain.usecase.meta.AtualizarStatusMetaUseCase
import domain.usecase.meta.CriarMetaUseCase
import domain.usecase.meta.ListarMetasUseCase
import domain.usecase.meta.RemoverMetaUseCase
import domain.usecase.tarefa.AtualizarStatusTarefaUseCase
import domain.usecase.tarefa.CriarTarefaUseCase
import domain.usecase.tarefa.ListarTarefasPorDataUseCase
import domain.usecase.tarefa.RemoverTarefaUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ui.screens.LembretesScreen
import ui.screens.MetasScreen
import ui.screens.TarefasScreen

@Composable
fun App() {
    // Persistência real no navegador (localStorage)
    val tarefaRepository = remember { TarefaRepositoryLocalStorage() }
    val metaRepository = remember { MetaRepositoryLocalStorage() }
    val lembreteRepository = remember { LembreteRepositoryLocalStorage() }

    // UseCases de Tarefas
    val criarTarefaUseCase = remember { CriarTarefaUseCase(tarefaRepository) }
    val listarTarefasPorDataUseCase = remember { ListarTarefasPorDataUseCase(tarefaRepository) }
    val atualizarStatusTarefaUseCase = remember { AtualizarStatusTarefaUseCase(tarefaRepository) }
    val removerTarefaUseCase = remember { RemoverTarefaUseCase(tarefaRepository) }

    // UseCases de Metas
    val criarMetaUseCase = remember { CriarMetaUseCase(metaRepository) }
    val listarMetasUseCase = remember { ListarMetasUseCase(metaRepository) }
    val atualizarStatusMetaUseCase = remember { AtualizarStatusMetaUseCase(metaRepository) }
    val removerMetaUseCase = remember { RemoverMetaUseCase(metaRepository) }

    // UseCases de Lembretes
    val criarLembreteUseCase = remember { CriarLembreteUseCase(lembreteRepository) }

    // UseCase para o Painel Analítico
    val gerarResumoDoDiaUseCase = remember {
        GerarResumoDoDiaUseCase(
            tarefaRepository = tarefaRepository,
            metaRepository = metaRepository,
            lembreteRepository = lembreteRepository
        )
    }

    MaterialTheme {
        var telaAtual by remember { mutableStateOf("painel") }

        Column(modifier = Modifier.fillMaxSize()) {
            // Barra superior de navegação
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Button(
                    onClick = { telaAtual = "painel" },
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Painel") }

                Button(
                    onClick = { telaAtual = "metas" },
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Metas") }

                Button(
                    onClick = { telaAtual = "tarefas" },
                    modifier = Modifier.padding(end = 8.dp)
                ) { Text("Tarefas") }

                Button(
                    onClick = { telaAtual = "lembretes" }
                ) { Text("Lembretes") }
            }

            when (telaAtual) {
                "painel" -> PainelScreen(
                    gerarResumoDoDiaUseCase = gerarResumoDoDiaUseCase,
                    now = { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
                )
                "metas" -> MetasScreen(
                    criarMetaUseCase = criarMetaUseCase,
                    listarMetasUseCase = listarMetasUseCase,
                    atualizarStatusMetaUseCase = atualizarStatusMetaUseCase,
                    removerMetaUseCase = removerMetaUseCase
                )
                "tarefas" -> TarefasScreen(
                    criarTarefaUseCase = criarTarefaUseCase,
                    listarTarefasPorDataUseCase = listarTarefasPorDataUseCase,
                    atualizarStatusTarefaUseCase = atualizarStatusTarefaUseCase,
                    removerTarefaUseCase = removerTarefaUseCase
                )
                "lembretes" -> LembretesScreen(criarLembreteUseCase)
            }
        }
    }
}