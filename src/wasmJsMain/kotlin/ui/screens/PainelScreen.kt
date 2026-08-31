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
    var mostrarModalDia by remember { mutableStateOf(false) }

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
                "Veja o resumo do dia e acompanhe seu mês.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            resumo?.let { r ->
                ResumoDoDiaCard(dia = diaSelecionado, resumo = r)
            } ?: Box(
                Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        }

        item {
            CalendarioMensal(
                mesExibido = mesExibido,
                atividades = atividadesDoMes,
                diaSelecionado = diaSelecionado,
                hoje = hoje,
                onMesAnterior = { mesExibido = mesExibido.minus(1, DateTimeUnit.MONTH) },
                onMesProximo = { mesExibido = mesExibido.plus(1, DateTimeUnit.MONTH) },
                onDiaSelecionado = { 
                    diaSelecionado = it
                    mostrarModalDia = true
                }
            )
        }

        item { LegendaCalendario() }

        item { RelatorioMensalPlaceholder(mesExibido) }
    }

    if (mostrarModalDia && resumo != null) {
        AlertDialog(
            onDismissRequest = { mostrarModalDia = false },
            confirmButton = {
                TextButton(onClick = { mostrarModalDia = false }) { Text("Fechar") }
            },
            title = {
                Text("Detalhes de ${formatarDataCompleta(diaSelecionado)}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (resumo!!.tarefasPendentes.isNotEmpty()) {
                        Text("Tarefas Pendentes:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                        resumo!!.tarefasPendentes.forEach { Text("• ${it.descricao}", style = MaterialTheme.typography.bodySmall) }
                    }
                    if (resumo!!.metasEmAndamento.isNotEmpty()) {
                        Text("Metas em Andamento:", fontWeight = FontWeight.SemiBold, color = Color(0xFF7C3AED))
                        resumo!!.metasEmAndamento.forEach { Text("• ${it.descricao}", style = MaterialTheme.typography.bodySmall) }
                    }
                    if (resumo!!.proximosLembretes.isNotEmpty()) {
                        Text("Lembretes:", fontWeight = FontWeight.SemiBold, color = Color(0xFFD97706))
                        resumo!!.proximosLembretes.forEach { Text("• ${it.descricao}", style = MaterialTheme.typography.bodySmall) }
                    }
                    
                    if (resumo!!.tarefasPendentes.isEmpty() && resumo!!.metasEmAndamento.isEmpty() && resumo!!.proximosLembretes.isEmpty()) {
                        Text("Nenhuma atividade pendente para este dia.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        )
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
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                ).forEach { dia ->
                    Text(
                        diaSemanaAbreviado(dia),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

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

    Surface(
        onClick = onClick,
        color = if (isSelecionado && !isHoje) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(2.dp).height(72.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isHoje) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dia.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isHoje || isSelecionado) FontWeight.Bold else FontWeight.Medium,
                    color = if (isHoje) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
            if (temAtividade) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    if (atividades!!.temTarefa) PontoAtividade(MaterialTheme.colorScheme.primary)
                    if (atividades.temMeta) PontoAtividade(Color(0xFF7C3AED))
                    if (atividades.temLembrete) PontoAtividade(Color(0xFFD97706))
                }
            }
        }
    }
}

@Composable
fun PontoAtividade(cor: Color) {
    Box(Modifier.size(5.dp).clip(CircleShape).background(cor))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Resumo de ${formatarDataCompleta(dia)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Indicador Geral de Produtividade
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Produtividade Geral",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${(resumo.indicadorProdutividade * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                LinearProgressIndicator(
                    progress = { resumo.indicadorProdutividade },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(4.dp))

            // Grid para os outros 4 itens
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MétricaCard(
                        "Tarefas Pendentes", 
                        resumo.tarefasPendentes.size, 
                        MaterialTheme.colorScheme.primary
                    )
                    MétricaCard(
                        "Metas em Andamento", 
                        resumo.metasEmAndamento.size, 
                        Color(0xFF7C3AED)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MétricaCard(
                        "Tarefas Concluídas", 
                        resumo.tarefasConcluidas.size, 
                        Color(0xFF166534)
                    )
                    MétricaCard(
                        "Próximos Lembretes", 
                        resumo.proximosLembretes.size, 
                        Color(0xFFD97706)
                    )
                }
            }
        }
    }
}

@Composable
fun MétricaCard(rotulo: String, quantidade: Int, cor: Color) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(cor))
                Text(
                    quantidade.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                rotulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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