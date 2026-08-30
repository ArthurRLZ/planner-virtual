package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import planner.domain.model.AtividadesDoDia
import br.edu.ufapetro.planner.domain.model.ResumoDoDia
import br.edu.ufapetro.planner.domain.usecase.painel.GerarResumoDoDiaUseCase
import domain.usecase.painel.ListarAtividadesDoMesUseCase
import kotlinx.datetime.*
import util.hoje

@Composable
fun PainelScreen(
    gerarResumoDoDiaUseCase: GerarResumoDoDiaUseCase,
    listarAtividadesDoMesUseCase: ListarAtividadesDoMesUseCase
) {
    val hoje = hoje()
    var mesExibido by remember { mutableStateOf(LocalDate(hoje.year, hoje.monthNumber, 1)) }
    var diaSelecionado by remember { mutableStateOf(hoje) }
    var resumo by remember { mutableStateOf<ResumoDoDia?>(null) }
    var atividadesDoMes by remember { mutableStateOf<Map<LocalDate, AtividadesDoDia>>(emptyMap()) }

    LaunchedEffect(mesExibido) {
        atividadesDoMes = listarAtividadesDoMesUseCase(mesExibido.year, mesExibido.monthNumber)
    }

    LaunchedEffect(diaSelecionado) {
        resumo = gerarResumoDoDiaUseCase(diaSelecionado)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                "Painel Analítico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Acompanhe seu mês e veja o resumo de qualquer dia.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            CalendarioMensal(
                mesExibido = mesExibido,
                atividades = atividadesDoMes,
                diaSelecionado = diaSelecionado,
                hoje = hoje,
                onMesAnterior = { mesExibido = mesExibido.minus(1, DateTimeUnit.MONTH) },
                onMesProximo = { mesExibido = mesExibido.plus(1, DateTimeUnit.MONTH) },
                onDiaSelecionado = { diaSelecionado = it }
            )
        }

        item { LegendaCalendario() }

        item {
            resumo?.let { r ->
                ResumoDoDiaCard(dia = diaSelecionado, resumo = r)
            } ?: Box(
                Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }

        item { RelatorioMensalPlaceholder(mesExibido) }
    }
}

// ============================================================================
// CALENDÁRIO MENSAL
// ============================================================================

@Composable
fun CalendarioMensal(
    mesExibido: LocalDate,
    atividades: Map<LocalDate, AtividadesDoDia>,
    diaSelecionado: LocalDate,
    hoje: LocalDate,
    onMesAnterior: () -> Unit,
    onMesProximo: () -> Unit,
    onDiaSelecionado: (LocalDate) -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho: mês/ano + navegação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${mesCompleto(mesExibido.monthNumber)} ${mesExibido.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onMesAnterior,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("◀") }
                    FilledTonalButton(
                        onClick = onMesProximo,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("▶") }
                }
            }

            // Cabeçalho dos dias da semana
            Row(Modifier.fillMaxWidth()) {
                listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                ).forEach { dia ->
                    Text(
                        diaSemanaAbreviado(dia),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid de dias
            val primeiroDiaMes = LocalDate(mesExibido.year, mesExibido.monthNumber, 1)
            val ultimoDiaMes = primeiroDiaMes.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            val offsetInicial = primeiroDiaMes.dayOfWeek.isoDayNumber - 1 // Segunda=0 .. Domingo=6

            val celulas = buildList {
                repeat(offsetInicial) { add(null) }
                var d = primeiroDiaMes
                while (d <= ultimoDiaMes) {
                    add(d)
                    d = d.plus(1, DateTimeUnit.DAY)
                }
            }

            celulas.chunked(7).forEach { semana ->
                Row(Modifier.fillMaxWidth()) {
                    semana.forEach { dia ->
                        if (dia == null) {
                            Box(Modifier.weight(1f))
                        } else {
                            CelulaDia(
                                dia = dia,
                                atividades = atividades[dia],
                                isHoje = dia == hoje,
                                isSelecionado = dia == diaSelecionado,
                                onClick = { onDiaSelecionado(dia) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    repeat(7 - semana.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun CelulaDia(
    dia: LocalDate,
    atividades: AtividadesDoDia?,
    isHoje: Boolean,
    isSelecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val temAtividade = atividades?.temAlgumaAtividade == true

    Box(
        modifier = modifier.padding(3.dp).aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            color = when {
                isHoje -> MaterialTheme.colorScheme.primary
                isSelecionado -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else -> Color.Transparent
            },
            border = BorderStroke(
                width = if (temAtividade || isSelecionado) 1.5.dp else 1.dp,
                color = when {
                    isSelecionado && !isHoje -> MaterialTheme.colorScheme.primary
                    temAtividade -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                }
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    dia.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHoje || isSelecionado) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHoje) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
                if (temAtividade) {
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (atividades!!.temTarefa) PontoAtividade(MaterialTheme.colorScheme.primary, isHoje)
                        if (atividades.temMeta) PontoAtividade(Color(0xFF7C3AED), isHoje)
                        if (atividades.temLembrete) PontoAtividade(Color(0xFFD97706), isHoje)
                    }
                }
            }
        }
    }
}

@Composable
fun PontoAtividade(cor: Color, isHoje: Boolean) {
    Box(
        Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(if (isHoje) Color.White else cor)
    )
}

@Composable
fun LegendaCalendario() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ItemLegenda("Tarefa", MaterialTheme.colorScheme.primary)
        ItemLegenda("Meta", Color(0xFF7C3AED))
        ItemLegenda("Lembrete", Color(0xFFD97706))
    }
}

@Composable
fun ItemLegenda(rotulo: String, cor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
        Text(rotulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ============================================================================
// RESUMO DO DIA SELECIONADO
// ============================================================================

@Composable
fun ResumoDoDiaCard(dia: LocalDate, resumo: ResumoDoDia) {
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
                "Resumo de ${formatarDataCompleta(dia)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { resumo.indicadorProdutividade },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Produtividade: ${(resumo.indicadorProdutividade * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IndicadorResumo("Tarefas pendentes", resumo.tarefasPendentes.size, MaterialTheme.colorScheme.primary)
            IndicadorResumo("Tarefas concluídas", resumo.tarefasConcluidas.size, Color(0xFF166534))
            IndicadorResumo("Metas em andamento", resumo.metasEmAndamento.size, Color(0xFF7C3AED))
            IndicadorResumo("Metas cumpridas", resumo.metasCumpridas.size, Color(0xFF166534))
            IndicadorResumo("Próximos lembretes", resumo.proximosLembretes.size, Color(0xFFD97706))
        }
    }
}

@Composable
fun IndicadorResumo(rotulo: String, quantidade: Int, cor: Color) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
            Text(rotulo, style = MaterialTheme.typography.bodyMedium)
        }
        Text(quantidade.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

// ============================================================================
// RELATÓRIO MENSAL (placeholder — Issue #12/#13)
// ============================================================================

@Composable
fun RelatorioMensalPlaceholder(mesExibido: LocalDate) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Relatório de ${mesCompleto(mesExibido.monthNumber)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "🚧 Em construção",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}