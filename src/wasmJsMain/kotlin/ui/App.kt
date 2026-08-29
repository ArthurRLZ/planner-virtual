package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import data.repository.LembreteRepositoryLocalStorage
import data.repository.MetaRepositoryLocalStorage
import data.repository.TarefaRepositoryLocalStorage
import domain.usecase.lembrete.CriarLembreteUseCase
import domain.usecase.meta.AtualizarStatusMetaUseCase
import domain.usecase.meta.CriarMetaUseCase
import domain.usecase.meta.ListarMetasUseCase
import domain.usecase.tarefa.CriarTarefaUseCase
import ui.screens.MetasScreen
import ui.screens.TarefasScreen

@Composable
fun App() {
    // Persistência real no navegador (Issue #2). Para testes unitários, usem as
    // implementações *EmMemoria em vez destas.
    val tarefaRepository = remember { TarefaRepositoryLocalStorage() }
    val metaRepository = remember { MetaRepositoryLocalStorage() }
    val lembreteRepository = remember { LembreteRepositoryLocalStorage() }

    val criarTarefaUseCase = remember { CriarTarefaUseCase(tarefaRepository) }
    val criarMetaUseCase = remember { CriarMetaUseCase(metaRepository) }
    val listarMetasUseCase = remember { ListarMetasUseCase(metaRepository) }
    val atualizarStatusMetaUseCase = remember { AtualizarStatusMetaUseCase(metaRepository) }
    @Suppress("UNUSED_VARIABLE")
    val criarLembreteUseCase = remember { CriarLembreteUseCase(lembreteRepository) }

    MaterialTheme {
        // TODO(#16): trocar esse toggle manual pela navegação real quando a issue for concluída.
        var telaAtual by remember { mutableStateOf("metas") }

        Column {
            Button(onClick = { telaAtual = if (telaAtual == "metas") "tarefas" else "metas" }) {
                Text(if (telaAtual == "metas") "Ir para Tarefas" else "Ir para Metas")
            }

            if (telaAtual == "metas") {
                MetasScreen(criarMetaUseCase, listarMetasUseCase, atualizarStatusMetaUseCase)
            } else {
                TarefasScreen(criarTarefaUseCase)
            }
        }
    }
}
