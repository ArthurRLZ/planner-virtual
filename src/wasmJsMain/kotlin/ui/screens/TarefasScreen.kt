package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import domain.model.*
import domain.usecase.tarefa.AtualizarStatusTarefaUseCase
import domain.usecase.tarefa.CriarTarefaUseCase
import domain.usecase.tarefa.ListarTarefasPorDataUseCase
import domain.usecase.tarefa.RemoverTarefaUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import ui.theme.corVisual
import util.hoje
import util.novoId

/**
 * Tela principal de Gestão e Planejamento de Tarefas por Dia (Issues #7 e #8).
 *
 * Funcionalidades:
 * - Seletor de data interativo e responsivo.
 * - Formulário para criação de tarefas com suporte a blocos de tempo (30min/1h) e turno.
 * - Listagem filtrada por dia com destaque visual por categoria e prioridade.
 * - Gestão de status de execução em tempo real (Executada, Parcial, Pendente, Cancelada, Adiada).
 * - Indicadores de produtividade diária e feedback flutuante via Snackbar.
 */
@Composable
fun TarefasScreen(
    criarTarefaUseCase: CriarTarefaUseCase,
    listarTarefasPorDataUseCase: ListarTarefasPorDataUseCase,
    atualizarStatusTarefaUseCase: AtualizarStatusTarefaUseCase,
    removerTarefaUseCase: RemoverTarefaUseCase
) {
    var dataSelecionada by remember { mutableStateOf(hoje()) }
    var versao by remember { mutableStateOf(0) }

    val tarefasDoDia = remember(dataSelecionada, versao) {
        listarTarefasPorDataUseCase(dataSelecionada)
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    var tarefaParaExcluir by remember { mutableStateOf<Tarefa?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Cabeçalho com métricas do dia
            item {
                TarefasHeader(
                    data = dataSelecionada,
                    totalTarefas = tarefasDoDia.size,
                    concluidas = tarefasDoDia.count { it.status == StatusTarefa.EXECUTADA }
                )
            }

            // 2. Seletor de Data Interativo
            item {
                SeletorDeData(
                    dataSelecionada = dataSelecionada,
                    onDataSelecionada = { novaData ->
                        dataSelecionada = novaData
                    }
                )
            }

            // 3. Formulário de Criação de Tarefa
            item {
                FormularioTarefa(
                    dataAtual = dataSelecionada,
                    focusRequester = focusRequester,
                    onSalvar = { novaTarefa ->
                        val resultado = criarTarefaUseCase(novaTarefa)
                        resultado.onSuccess {
                            versao++
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Tarefa agendada com sucesso!")
                            }
                        }
                        resultado
                    }
                )
            }

            // 4. Listagem de Tarefas do Dia Selecionado
            item {
                ListaTarefasDia(
                    data = dataSelecionada,
                    tarefas = tarefasDoDia,
                    onStatusChange = { idTarefa, novoStatus ->
                        val res = atualizarStatusTarefaUseCase(idTarefa, novoStatus)
                        res.onSuccess {
                            versao++
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Status alterado para ${novoStatus.rotuloAmigavel()}")
                            }
                        }
                    },
                    onSolicitarExclusao = { tarefa ->
                        tarefaParaExcluir = tarefa
                    },
                    onFocarFormulario = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = 2)
                            delay(120)
                            try {
                                focusRequester.requestFocus()
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }

        // Feedback Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        // Diálogo de Confirmação de Exclusão
        tarefaParaExcluir?.let { tarefa ->
            AlertDialog(
                onDismissRequest = { tarefaParaExcluir = null },
                icon = {
                    TrashIcon(
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Excluir Tarefa",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Tem certeza de que deseja excluir a tarefa \"${tarefa.descricao}\"? Esta ação não pode ser desfeita.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            removerTarefaUseCase(tarefa.id)
                            versao++
                            tarefaParaExcluir = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Tarefa excluída com sucesso.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { tarefaParaExcluir = null },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// ============================================================================
// 1. CABEÇALHO DA TELA DE TAREFAS (TarefasHeader)
// ============================================================================

@Composable
fun TarefasHeader(
    data: LocalDate,
    totalTarefas: Int,
    concluidas: Int
) {
    val progresso = if (totalTarefas > 0) concluidas.toFloat() / totalTarefas else 0f
    val porcentagem = (progresso * 100).toInt()

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Planejamento Diário de Tarefas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Organize suas atividades por blocos de 30min/1h ou turnos do dia.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(40.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "$porcentagem%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = "DESEMPENHO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "$concluidas de $totalTarefas concluídas",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. SELETOR DE DATA INTERATIVO (SeletorDeData)
// ============================================================================

@Composable
fun SeletorDeData(
    dataSelecionada: LocalDate,
    onDataSelecionada: (LocalDate) -> Unit
) {
    val hoje = hoje()

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Linha Superior de Navegação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CalendarMiniIcon(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formatarDataCompleta(dataSelecionada),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (dataSelecionada == hoje) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "HOJE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = { onDataSelecionada(dataSelecionada.plus(-1, DateTimeUnit.DAY)) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("◀ Ontem")
                    }

                    FilledTonalButton(
                        onClick = { onDataSelecionada(hoje) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Hoje")
                    }

                    FilledTonalButton(
                        onClick = { onDataSelecionada(dataSelecionada.plus(1, DateTimeUnit.DAY)) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Amanhã ▶")
                    }
                }
            }

            // Pílulas de Dias da Semana ao Redor da Data Selecionada (-3 a +3 dias)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val dias = (-3..3).map { dataSelecionada.plus(it, DateTimeUnit.DAY) }
                items(dias) { dia ->
                    val isSelecionado = dia == dataSelecionada
                    val isHoje = dia == hoje

                    Surface(
                        onClick = { onDataSelecionada(dia) },
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isSelecionado -> MaterialTheme.colorScheme.primary
                            isHoje -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelecionado) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .width(74.dp)
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = diaSemanaAbreviado(dia.dayOfWeek),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelecionado) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dia.dayOfMonth.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. FORMULÁRIO DE CRIAÇÃO DE TAREFA (FormularioTarefa)
// ============================================================================

enum class ModoAgendamento {
    HORARIO, TURNO
}

@Composable
fun FormularioTarefa(
    dataAtual: LocalDate,
    focusRequester: FocusRequester,
    onSalvar: (Tarefa) -> Result<Tarefa>
) {
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.FACULDADE) }
    var prioridade by remember { mutableStateOf(Prioridade.MEDIA) }
    var modoAgendamento by remember { mutableStateOf(ModoAgendamento.HORARIO) }

    // Campos de Horário e Bloco
    var horaInicio by remember { mutableStateOf(9) }
    var minutoInicio by remember { mutableStateOf(0) }
    var duracaoBloco by remember { mutableStateOf(BlocoTempo.UMA_HORA) }

    // Campo de Turno
    var turnoSelecionado by remember { mutableStateOf(Turno.MANHA) }

    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tarefa-form-card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título do Formulário
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        PlusIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "Cadastrar Nova Tarefa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Campo de Descrição
            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                    mensagemErro = null
                },
                label = { Text("O que você planeja realizar?") },
                placeholder = { Text("Ex: Resolver lista de exercícios de Cálculo") },
                isError = mensagemErro != null,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("tarefa-input-descricao")
            )

            // Linha: Categoria e Prioridade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CategoryDropdownSelector(
                        label = "Categoria",
                        opcoes = Categoria.entries,
                        selecionado = categoria,
                        onSelecionar = { categoria = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    PrioridadeDropdownSelector(
                        selecionado = prioridade,
                        onSelecionar = { prioridade = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Alternador de Modo: Horário Específico vs Turno
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { modoAgendamento = ModoAgendamento.HORARIO },
                        shape = RoundedCornerShape(8.dp),
                        color = if (modoAgendamento == ModoAgendamento.HORARIO) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ClockMiniIcon(
                                color = if (modoAgendamento == ModoAgendamento.HORARIO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Horário (30min / 1h)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (modoAgendamento == ModoAgendamento.HORARIO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        onClick = { modoAgendamento = ModoAgendamento.TURNO },
                        shape = RoundedCornerShape(8.dp),
                        color = if (modoAgendamento == ModoAgendamento.TURNO) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SunMiniIcon(
                                color = if (modoAgendamento == ModoAgendamento.TURNO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Turno do Dia",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (modoAgendamento == ModoAgendamento.TURNO) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Detalhes do Agendamento conforme o Modo Selecionado
            if (modoAgendamento == ModoAgendamento.HORARIO) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Seletor de Horário de Início
                    Box(modifier = Modifier.weight(1f)) {
                        HorarioSelector(
                            hora = horaInicio,
                            minuto = minutoInicio,
                            onHorarioChange = { h, m ->
                                horaInicio = h
                                minutoInicio = m
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Seletor de Bloco de Duração (30 min ou 1 hora)
                    Box(modifier = Modifier.weight(1f)) {
                        BlocoTempoSelector(
                            selecionado = duracaoBloco,
                            onSelecionar = { duracaoBloco = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                // Seletor de Turno
                TurnoSelector(
                    selecionado = turnoSelecionado,
                    onSelecionar = { turnoSelecionado = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Exibição de Mensagem de Erro
            mensagemErro?.let { erro ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlertIcon(
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = erro,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Botão Salvar
            Button(
                onClick = {
                    if (descricao.isBlank()) {
                        mensagemErro = "A descrição da tarefa não pode ficar em branco."
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        delay(100)
                        val novaTarefa = if (modoAgendamento == ModoAgendamento.HORARIO) {
                            Tarefa(
                                id = novoId(),
                                descricao = descricao.trim(),
                                categoria = categoria,
                                data = dataAtual,
                                horarioInicio = LocalTime(horaInicio, minutoInicio),
                                duracaoMinutos = duracaoBloco.minutos,
                                blocoTempo = duracaoBloco,
                                turno = null,
                                status = StatusTarefa.PENDENTE,
                                prioridade = prioridade
                            )
                        } else {
                            Tarefa(
                                id = novoId(),
                                descricao = descricao.trim(),
                                categoria = categoria,
                                data = dataAtual,
                                horarioInicio = null,
                                duracaoMinutos = null,
                                blocoTempo = BlocoTempo.TURNO,
                                turno = turnoSelecionado,
                                status = StatusTarefa.PENDENTE,
                                prioridade = prioridade
                            )
                        }

                        val resultado = onSalvar(novaTarefa)
                        resultado
                            .onSuccess {
                                descricao = ""
                                mensagemErro = null
                            }
                            .onFailure { ex ->
                                mensagemErro = ex.message ?: "Não foi possível agendar a tarefa."
                            }
                        isSubmitting = false
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("tarefa-button-salvar")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        PlusIcon(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                    }
                    Text(if (isSubmitting) "Salvando..." else "Adicionar Tarefa")
                }
            }
        }
    }
}

// ============================================================================
// 4. LISTA DE TAREFAS DO DIA (ListaTarefasDia)
// ============================================================================

@Composable
fun ListaTarefasDia(
    data: LocalDate,
    tarefas: List<Tarefa>,
    onStatusChange: (idTarefa: String, novoStatus: StatusTarefa) -> Unit,
    onSolicitarExclusao: (Tarefa) -> Unit,
    onFocarFormulario: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tarefas de ${data.dayOfMonth.toString().padStart(2, '0')}/${data.monthNumber.toString().padStart(2, '0')} (${tarefas.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (tarefas.isNotEmpty()) {
                val concluidas = tarefas.count { it.status == StatusTarefa.EXECUTADA }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (concluidas == tarefas.size) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (concluidas == tarefas.size) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "$concluidas/${tarefas.size} executadas",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (concluidas == tarefas.size) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (tarefas.isEmpty()) {
            ActionableEmptyStateTarefas(
                data = data,
                onAdicionarClick = onFocarFormulario
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                tarefas.forEach { tarefa ->
                    CardTarefa(
                        tarefa = tarefa,
                        onStatusChange = { novoStatus ->
                            onStatusChange(tarefa.id, novoStatus)
                        },
                        onExcluirClick = {
                            onSolicitarExclusao(tarefa)
                        }
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5. COMPONENTE DE CARD DE TAREFA (CardTarefa) — Integração com Issue #8
// ============================================================================

/**
 * Card individual de exibição de Tarefa com destaque por categoria e prioridade.
 *
 * Integração com AtualizarStatusTarefaUseCase (Issue #8):
 * - O componente [StatusTarefaDropdown] recebe o evento de seleção do usuário.
 * - Ao selecionar um novo status (Executada, Parcial, Cancelada, Adiada ou Pendente),
 *   a função [onStatusChange] é imediatamente acionada, propagando a chamada
 *   para o [AtualizarStatusTarefaUseCase] e atualizando o repositório em tempo real.
 */
@Composable
fun CardTarefa(
    tarefa: Tarefa,
    onStatusChange: (StatusTarefa) -> Unit,
    onExcluirClick: () -> Unit
) {
    val corCategoria = tarefa.categoria.corVisual()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tarefa-card-${tarefa.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            hoveredElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Faixa lateral com a cor da categoria (Destaque visual da Issue #7)
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(corCategoria)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Linha Superior: Categoria, Prioridade e Horário/Turno
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryBadge(categoria = tarefa.categoria, cor = corCategoria)
                        PrioridadeBadge(prioridade = tarefa.prioridade)
                    }

                    // Badge de Horário ou Turno
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (tarefa.horarioInicio != null) {
                                ClockMiniIcon(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(11.dp)
                                )
                                val fim = tarefa.horarioFim()
                                val horarioStr = if (fim != null) {
                                    "${formatarHora(tarefa.horarioInicio)} – ${formatarHora(fim)} (${tarefa.duracaoEfetivaMinutos()}m)"
                                } else {
                                    formatarHora(tarefa.horarioInicio)
                                }
                                Text(
                                    text = horarioStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            } else if (tarefa.turno != null) {
                                SunMiniIcon(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "Turno da ${tarefa.turno.nomeAmigavel()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Descrição da Tarefa
                Text(
                    text = tarefa.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Linha Inferior: Seletor Interativo de Status (Issue #8) e Botão Excluir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusTarefaDropdown(
                        statusAtual = tarefa.status,
                        onStatusChange = onStatusChange
                    )

                    IconButton(
                        onClick = onExcluirClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        TrashIcon(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 6. SELETORES E DROPDOWNS AUXILIARES
// ============================================================================

@Composable
fun StatusTarefaDropdown(
    statusAtual: StatusTarefa,
    onStatusChange: (StatusTarefa) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val visual = statusAtual.visual()

    Box {
        Surface(
            onClick = { expandido = true },
            shape = RoundedCornerShape(8.dp),
            color = visual.corContainer,
            border = BorderStroke(1.dp, visual.corBorda)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusTarefaIcon(status = statusAtual, color = visual.corTexto, modifier = Modifier.size(13.dp))
                Text(
                    text = visual.rotulo,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = visual.corTexto
                )
                ChevronDownIcon(color = visual.corTexto.copy(alpha = 0.7f), modifier = Modifier.size(8.dp))
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            StatusTarefa.entries.forEach { statusOpcao ->
                val opVisual = statusOpcao.visual()
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusTarefaIcon(status = statusOpcao, color = opVisual.corTexto, modifier = Modifier.size(14.dp))
                            Text(
                                text = opVisual.rotulo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (statusOpcao == statusAtual) FontWeight.Bold else FontWeight.Normal,
                                color = opVisual.corTexto
                            )
                        }
                    },
                    onClick = {
                        onStatusChange(statusOpcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun PrioridadeBadge(prioridade: Prioridade) {
    val (corTexto, corFundo, corBorda) = when (prioridade) {
        Prioridade.ALTA -> Triple(Color(0xFFDC2626), Color(0xFFFEE2E2), Color(0xFFFCA5A5))
        Prioridade.MEDIA -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), Color(0xFFFCD34D))
        Prioridade.BAIXA -> Triple(Color(0xFF059669), Color(0xFFD1FAE5), Color(0xFF6EE7B7))
    }

    Surface(
        color = corFundo,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, corBorda)
    ) {
        Text(
            text = prioridade.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = corTexto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun PrioridadeDropdownSelector(
    selecionado: Prioridade,
    onSelecionar: (Prioridade) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expandido = true },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Prioridade",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selecionado.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ChevronDownIcon(color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(9.dp))
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            Prioridade.entries.forEach { op ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = op.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontWeight = if (op == selecionado) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelecionar(op)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun BlocoTempoSelector(
    selecionado: BlocoTempo,
    onSelecionar: (BlocoTempo) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expandido = true },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Duração do Bloco",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (selecionado == BlocoTempo.MEIA_HORA) "30 minutos" else "1 hora",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                ChevronDownIcon(color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(9.dp))
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            DropdownMenuItem(
                text = { Text("30 minutos (Meia Hora)", fontWeight = if (selecionado == BlocoTempo.MEIA_HORA) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onSelecionar(BlocoTempo.MEIA_HORA)
                    expandido = false
                }
            )
            DropdownMenuItem(
                text = { Text("1 hora (Uma Hora)", fontWeight = if (selecionado == BlocoTempo.UMA_HORA) FontWeight.Bold else FontWeight.Normal) },
                onClick = {
                    onSelecionar(BlocoTempo.UMA_HORA)
                    expandido = false
                }
            )
        }
    }
}

@Composable
fun HorarioSelector(
    hora: Int,
    minuto: Int,
    onHorarioChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val horariosPredefinidos = remember {
        val lista = mutableListOf<Pair<Int, Int>>()
        for (h in 6..23) {
            lista.add(h to 0)
            lista.add(h to 30)
        }
        lista
    }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expandido = true },
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Horário de Início",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ClockMiniIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        Text(
                            text = "${hora.toString().padStart(2, '0')}:${minuto.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                ChevronDownIcon(color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(9.dp))
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            horariosPredefinidos.forEach { (h, m) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}",
                            fontWeight = if (h == hora && m == minuto) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onHorarioChange(h, m)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun TurnoSelector(
    selecionado: Turno,
    onSelecionar: (Turno) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Turno.entries.forEach { turno ->
            val isSelected = turno == selecionado
            Surface(
                onClick = { onSelecionar(turno) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SunMiniIcon(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = turno.nomeAmigavel(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ActionableEmptyStateTarefas(
    data: LocalDate,
    onAdicionarClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CalendarMiniIcon(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Nenhuma tarefa agendada para ${formatarDataCurta(data)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Adicione atividades em blocos de tempo ou turnos para este dia.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onAdicionarClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlusIcon(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                    Text(
                        text = "Agendar Tarefa",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// 7. ÍCONES VETORIAIS VIA CANVAS E STATUS VISUAL
// ============================================================================

data class StatusTarefaVisual(
    val rotulo: String,
    val corTexto: Color,
    val corContainer: Color,
    val corBorda: Color
)

fun StatusTarefa.visual(): StatusTarefaVisual = when (this) {
    StatusTarefa.PENDENTE -> StatusTarefaVisual(
        rotulo = "Pendente",
        corTexto = Color(0xFF475569),
        corContainer = Color(0xFFF1F5F9),
        corBorda = Color(0xFFCBD5E1)
    )
    StatusTarefa.EXECUTADA -> StatusTarefaVisual(
        rotulo = "Executada",
        corTexto = Color(0xFF166534),
        corContainer = Color(0xFFDCFCE7),
        corBorda = Color(0xFF86EFAC)
    )
    StatusTarefa.PARCIALMENTE_EXECUTADA -> StatusTarefaVisual(
        rotulo = "Parcial",
        corTexto = Color(0xFF9A3412),
        corContainer = Color(0xFFFFEDD5),
        corBorda = Color(0xFFFDBA74)
    )
    StatusTarefa.CANCELADA -> StatusTarefaVisual(
        rotulo = "Cancelada",
        corTexto = Color(0xFF991B1B),
        corContainer = Color(0xFFFEE2E2),
        corBorda = Color(0xFFFCA5A5)
    )
    StatusTarefa.ADIADA -> StatusTarefaVisual(
        rotulo = "Adiada",
        corTexto = Color(0xFF6B21A8),
        corContainer = Color(0xFFF3E8FF),
        corBorda = Color(0xFFD8B4FE)
    )
}

fun StatusTarefa.rotuloAmigavel(): String = when (this) {
    StatusTarefa.PENDENTE -> "Pendente"
    StatusTarefa.EXECUTADA -> "Executada"
    StatusTarefa.PARCIALMENTE_EXECUTADA -> "Parcialmente Executada"
    StatusTarefa.CANCELADA -> "Cancelada"
    StatusTarefa.ADIADA -> "Adiada"
}

@Composable
fun StatusTarefaIcon(status: StatusTarefa, color: Color, modifier: Modifier = Modifier) {
    when (status) {
        StatusTarefa.EXECUTADA -> CheckIcon(color = color, modifier = modifier)
        StatusTarefa.PARCIALMENTE_EXECUTADA -> PartialProgressIcon(color = color, modifier = modifier)
        StatusTarefa.PENDENTE -> PendingCircleIcon(color = color, modifier = modifier)
        StatusTarefa.CANCELADA -> CancelIcon(color = color, modifier = modifier)
        StatusTarefa.ADIADA -> PostponeIcon(color = color, modifier = modifier)
    }
}

@Composable
fun CancelIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height * 0.2f),
            end = Offset(size.width * 0.8f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.8f, size.height * 0.2f),
            end = Offset(size.width * 0.2f, size.height * 0.8f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PostponeIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6.dp.toPx()
        drawArc(
            color = color,
            startAngle = 45f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        val path = Path().apply {
            moveTo(size.width * 0.65f, size.height * 0.15f)
            lineTo(size.width * 0.90f, size.height * 0.35f)
            lineTo(size.width * 0.65f, size.height * 0.55f)
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun ClockMiniIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.5.dp.toPx()
        drawCircle(color = color, style = Stroke(width = strokeWidth))
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.5f),
            end = Offset(size.width * 0.5f, size.height * 0.22f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.5f),
            end = Offset(size.width * 0.72f, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SunMiniIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.4.dp.toPx()
        drawCircle(color = color, radius = size.minDimension * 0.25f, center = Offset(size.width * 0.5f, size.height * 0.5f))
        for (i in 0..7) {
            val angle = (i * 45) * (kotlin.math.PI / 180.0)
            val startX = (size.width * 0.5f + (size.minDimension * 0.34f) * kotlin.math.cos(angle)).toFloat()
            val startY = (size.height * 0.5f + (size.minDimension * 0.34f) * kotlin.math.sin(angle)).toFloat()
            val endX = (size.width * 0.5f + (size.minDimension * 0.48f) * kotlin.math.cos(angle)).toFloat()
            val endY = (size.height * 0.5f + (size.minDimension * 0.48f) * kotlin.math.sin(angle)).toFloat()
            drawLine(color = color, start = Offset(startX, startY), end = Offset(endX, endY), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
    }
}

// ============================================================================
// 8. UTILITÁRIOS DE FORMATAÇÃO E TEXTO
// ============================================================================

fun Turno.nomeAmigavel(): String = when (this) {
    Turno.MANHA -> "Manhã"
    Turno.TARDE -> "Tarde"
    Turno.NOITE -> "Noite"
}

fun formatarHora(hora: LocalTime): String {
    return "${hora.hour.toString().padStart(2, '0')}:${hora.minute.toString().padStart(2, '0')}"
}

fun formatarDataCompleta(data: LocalDate): String {
    val diaSemana = when (data.dayOfWeek) {
        DayOfWeek.MONDAY -> "Segunda-feira"
        DayOfWeek.TUESDAY -> "Terça-feira"
        DayOfWeek.WEDNESDAY -> "Quarta-feira"
        DayOfWeek.THURSDAY -> "Quinta-feira"
        DayOfWeek.FRIDAY -> "Sexta-feira"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
        else -> ""
    }
    val mes = mesCompleto(data.monthNumber)
    return "$diaSemana, ${data.dayOfMonth} de $mes de ${data.year}"
}

fun formatarDataCurta(data: LocalDate): String {
    return "${data.dayOfMonth.toString().padStart(2, '0')} ${mesAbreviado(data.monthNumber)}"
}

fun diaSemanaAbreviado(diaSemana: DayOfWeek): String = when (diaSemana) {
    DayOfWeek.MONDAY -> "SEG"
    DayOfWeek.TUESDAY -> "TER"
    DayOfWeek.WEDNESDAY -> "QUA"
    DayOfWeek.THURSDAY -> "QUI"
    DayOfWeek.FRIDAY -> "SEX"
    DayOfWeek.SATURDAY -> "SÁB"
    DayOfWeek.SUNDAY -> "DOM"
    else -> ""
}

fun mesCompleto(mesNumero: Int): String = when (mesNumero) {
    1 -> "Janeiro"
    2 -> "Fevereiro"
    3 -> "Março"
    4 -> "Abril"
    5 -> "Maio"
    6 -> "Junho"
    7 -> "Julho"
    8 -> "Agosto"
    9 -> "Setembro"
    10 -> "Outubro"
    11 -> "Novembro"
    12 -> "Dezembro"
    else -> ""
}
