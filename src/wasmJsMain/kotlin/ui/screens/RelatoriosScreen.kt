package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domain.model.PeriodoMeta
import domain.model.Relatorio
import domain.usecase.relatorio.GerarRelatorioAnualUseCase
import domain.usecase.relatorio.GerarRelatorioMensalUseCase
import domain.usecase.relatorio.GerarRelatorioSemanalUseCase
import util.hoje

/**
 * Tela de Relatórios (Issue #13).
 *
 * Permite ao usuário escolher o período (semana/mês/ano) e exibe os dados
 * calculados pelos casos de uso de relatório (Issue #12).
 */
@Composable
fun RelatoriosScreen(
    gerarRelatorioSemanalUseCase: GerarRelatorioSemanalUseCase,
    gerarRelatorioMensalUseCase: GerarRelatorioMensalUseCase,
    gerarRelatorioAnualUseCase: GerarRelatorioAnualUseCase
) {
    val hoje = hoje()
    var periodoSelecionado by remember { mutableStateOf(PeriodoMeta.MENSAL) }
    var relatorio by remember { mutableStateOf<Relatorio?>(null) }

    LaunchedEffect(periodoSelecionado) {
        relatorio = when (periodoSelecionado) {
            PeriodoMeta.SEMANAL -> gerarRelatorioSemanalUseCase(hoje)
            PeriodoMeta.MENSAL -> gerarRelatorioMensalUseCase(hoje)
            PeriodoMeta.ANUAL -> gerarRelatorioAnualUseCase(hoje.year)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                "Relatórios",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Escolha o período e veja seu desempenho.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            PeriodDropdownSelector(
                label = "Período",
                opcoes = PeriodoMeta.entries,
                selecionado = periodoSelecionado,
                onSelecionar = { periodoSelecionado = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            relatorio?.let { r ->
                RelatorioCard(r)
            } ?: Box(
                Modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }
    }
}

@Composable
private fun RelatorioCard(relatorio: Relatorio) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "${formatarDataCompleta(relatorio.inicio)} — ${formatarDataCompleta(relatorio.fim)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IndicadorPercentual("Metas cumpridas", relatorio.percentualMetasCumpridas)
            IndicadorPercentual("Tarefas executadas", relatorio.percentualTarefasExecutadas)

            relatorio.periodoMaisProdutivo?.let { periodo ->
                Text(
                    "Período mais produtivo: ${formatarDataCompleta(periodo.inicio)} " +
                            "(${periodo.tarefasExecutadas} tarefa(s) executada(s))",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            relatorio.turnoMaisProdutivo?.let { turno ->
                Text(
                    "Turno mais produtivo: ${turno.nomeAmigavel()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (relatorio.categoriasTarefaMaisRealizadas.isNotEmpty()) {
                Text(
                    "Categorias de tarefas em destaque: " +
                            relatorio.categoriasTarefaMaisRealizadas.joinToString { it.nomeAmigavel() },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (relatorio.categoriasMetaMaisRealizadas.isNotEmpty()) {
                Text(
                    "Categorias de metas em destaque: " +
                            relatorio.categoriasMetaMaisRealizadas.joinToString { it.nomeAmigavel() },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun IndicadorPercentual(rotulo: String, percentual: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(rotulo, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${percentual.toInt()}%",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        LinearProgressIndicator(
            progress = { (percentual / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}