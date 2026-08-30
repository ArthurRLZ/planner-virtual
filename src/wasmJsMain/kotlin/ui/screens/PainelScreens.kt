package br.edu.ufapetro.planner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.edu.ufapetro.planner.domain.model.ResumoDoDia
import br.edu.ufapetro.planner.domain.usecase.painel.GerarResumoDoDiaUseCase
import kotlinx.datetime.LocalDate

@Composable
fun PainelScreen(gerarResumoDoDiaUseCase: GerarResumoDoDiaUseCase, now: LocalDate.Companion.() -> LocalDate) {
    var resumo by remember { mutableStateOf<ResumoDoDia?>(null) }

    LaunchedEffect(Unit) {
        resumo = gerarResumoDoDiaUseCase(LocalDate.now())
    }

    resumo?.let { r ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Resumo do dia", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { r.indicadorProdutividade },
                modifier = Modifier.fillMaxWidth()
            )
            Text("Produtividade: ${(r.indicadorProdutividade * 100).toInt()}%")

            Spacer(Modifier.height(16.dp))
            Text("Tarefas pendentes (${r.tarefasPendentes.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(r.tarefasPendentes) { tarefa ->
                    Text("• ${tarefa.descricao}")
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Tarefas concluídas (${r.tarefasConcluidas.size})", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(16.dp))
            Text("Metas em andamento (${r.metasEmAndamento.size})", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(16.dp))
            Text("Metas cumpridas(${r.metasCumpridas.size})", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(16.dp))
            Text("Próximos lembretes (${r.proximosLembretes.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(r.proximosLembretes) { lembrete ->
                    Text("• ${lembrete.descricao}")
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}