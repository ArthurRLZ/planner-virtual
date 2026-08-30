package ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import domain.model.Categoria
import domain.model.Meta
import domain.model.PeriodoMeta
import domain.model.StatusMeta
import domain.usecase.meta.AtualizarStatusMetaUseCase
import domain.usecase.meta.CriarMetaUseCase
import domain.usecase.meta.ListarMetasUseCase
import domain.usecase.meta.RemoverMetaUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import util.hoje
import util.novoId

/**
 * Tela principal de Planejamento de Metas (Issue #4).
 *
 * Funcionalidades:
 * - Indicador de progresso circular real do Total Geral no cabeçalho.
 * - Estados vazios acionáveis com botão CTA que faz scroll até o formulário e foca no input.
 * - Cards com datas legíveis e concisas, seletor semântico de status e exclusão com diálogo de confirmação.
 * - Seções de período colapsáveis/expansíveis.
 * - Feedback flutuante com Snackbar.
 */
@Composable
fun MetasScreen(
    criarMetaUseCase: CriarMetaUseCase,
    listarMetasUseCase: ListarMetasUseCase,
    atualizarStatusMetaUseCase: AtualizarStatusMetaUseCase,
    removerMetaUseCase: RemoverMetaUseCase
) {
    var versao by remember { mutableStateOf(0) }
    val metas = remember(versao) { listarMetasUseCase() }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    // Período selecionado compartilhado para permitir que o CTA de seções vazias pré-preencha o formulário
    var periodoFormulario by remember { mutableStateOf(PeriodoMeta.SEMANAL) }

    // Estado para o diálogo modal de confirmação de exclusão
    var metaParaExcluir by remember { mutableStateOf<Meta?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Cabeçalho com indicador de progresso geral
            item {
                MetasHeader(
                    totalMetas = metas.size,
                    concluidas = metas.count { it.status == StatusMeta.CUMPRIDA }
                )
            }

            // 2. Formulário de criação de metas
            item {
                MetaFormCard(
                    periodoSelecionado = periodoFormulario,
                    onPeriodoChange = { periodoFormulario = it },
                    focusRequester = focusRequester,
                    onSalvar = { novaMeta ->
                        val resultado = criarMetaUseCase(novaMeta)
                        resultado.onSuccess {
                            versao++
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Meta adicionada com sucesso!")
                            }
                        }
                        resultado
                    }
                )
            }

            // 3. Listagem organizada por períodos com seções colapsáveis e CTAs
            item {
                PeriodList(
                    metas = metas,
                    onStatusChange = { idMeta, novoStatus ->
                        atualizarStatusMetaUseCase(idMeta, novoStatus)
                        versao++
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Status atualizado para ${novoStatus.visual().rotulo}")
                        }
                    },
                    onSolicitarExclusao = { meta ->
                        metaParaExcluir = meta
                    },
                    onAdicionarMetaNoPeriodo = { periodoAlvo ->
                        periodoFormulario = periodoAlvo
                        coroutineScope.launch {
                            // Rola suavemente até o topo da tela
                            listState.animateScrollToItem(index = 0)
                            delay(120)
                            try {
                                focusRequester.requestFocus()
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }

        // Host para mensagens Toast / Snackbar de feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        // Modal / Diálogo de Confirmação de Exclusão
        metaParaExcluir?.let { meta ->
            AlertDialog(
                onDismissRequest = { metaParaExcluir = null },
                icon = {
                    TrashIcon(
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = "Excluir Meta",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = "Tem certeza de que deseja remover a meta \"${meta.descricao}\"? Esta ação não poderá ser desfeita.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            removerMetaUseCase(meta.id)
                            versao++
                            metaParaExcluir = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Meta excluída com sucesso.")
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
                        onClick = { metaParaExcluir = null },
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
// 1. CABEÇALHO COM INDICADOR DE PROGRESSO GERAL
// ============================================================================

@Composable
fun MetasHeader(
    totalMetas: Int,
    concluidas: Int
) {
    val progressoGeral = if (totalMetas > 0) concluidas.toFloat() / totalMetas else 0f
    val porcentagem = (progressoGeral * 100).toInt()

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
                    text = "Planejamento de Metas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Organize seus objetivos por horizonte de tempo e acompanhe seu rendimento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            // Indicador de Progresso Circular para o Total Geral
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
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
                            progress = { progressoGeral },
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
                            text = "TOTAL GERAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "$concluidas de $totalMetas concluídas",
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
// 2. FORMULÁRIO DE CRIAÇÃO (MetaFormCard)
// ============================================================================

@Composable
fun MetaFormCard(
    periodoSelecionado: PeriodoMeta,
    onPeriodoChange: (PeriodoMeta) -> Unit,
    focusRequester: FocusRequester,
    onSalvar: (Meta) -> Result<Meta>
) {
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.FACULDADE) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-form-card"),
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
                    text = "Cadastrar Nova Meta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Campo de Descrição com FocusRequester
            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                    mensagemErro = null
                },
                label = { Text("Qual é o seu objetivo?") },
                placeholder = { Text("Ex: Finalizar a implementação do módulo de relatórios") },
                isError = mensagemErro != null,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("meta-input-descricao")
            )

            // Linha com Seletores de Categoria e Período
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Dropdown de Categoria com dot temático e contagem
                Box(modifier = Modifier.weight(1f)) {
                    CategoryDropdownSelector(
                        label = "Categoria",
                        opcoes = Categoria.entries,
                        selecionado = categoria,
                        onSelecionar = { categoria = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meta-select-categoria")
                    )
                }

                // Dropdown de Período com ícone de calendário
                Box(modifier = Modifier.weight(1f)) {
                    PeriodDropdownSelector(
                        label = "Período",
                        opcoes = PeriodoMeta.entries,
                        selecionado = periodoSelecionado,
                        onSelecionar = onPeriodoChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meta-select-periodo")
                    )
                }
            }

            // Mensagem de Erro de Validação
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
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.testTag("meta-error-mensagem")
                        )
                    }
                }
            }

            // Botão de Ação com estado de submissão/loading
            Button(
                onClick = {
                    if (descricao.isBlank()) {
                        mensagemErro = "A descrição da meta não pode ficar em branco."
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        delay(100)
                        val hoje = hoje()
                        val (inicio, fim) = calcularIntervalo(periodoSelecionado, hoje)

                        val novaMeta = Meta(
                            id = novoId(),
                            descricao = descricao.trim(),
                            categoria = categoria,
                            status = StatusMeta.NAO_CUMPRIDA,
                            periodo = periodoSelecionado,
                            dataInicio = inicio,
                            dataFim = fim
                        )

                        val resultado = onSalvar(novaMeta)
                        resultado
                            .onSuccess {
                                descricao = ""
                                mensagemErro = null
                            }
                            .onFailure { ex ->
                                mensagemErro = ex.message ?: "Falha ao registrar meta."
                            }
                        isSubmitting = false
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("meta-button-salvar")
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
                    Text(if (isSubmitting) "Salvando..." else "Adicionar Meta")
                }
            }
        }
    }
}

// ============================================================================
// 3. LISTAGEM AGRUPADA COM SEÇÕES COLAPSÁVEIS (PeriodList & MetaSection)
// ============================================================================

@Composable
fun PeriodList(
    metas: List<Meta>,
    onStatusChange: (idMeta: String, novoStatus: StatusMeta) -> Unit,
    onSolicitarExclusao: (Meta) -> Unit,
    onAdicionarMetaNoPeriodo: (PeriodoMeta) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-list-container"),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        PeriodoMeta.entries.forEach { periodo ->
            val metasDoPeriodo = metas.filter { it.periodo == periodo }

            MetaSection(
                periodo = periodo,
                metas = metasDoPeriodo,
                onStatusChange = onStatusChange,
                onSolicitarExclusao = onSolicitarExclusao,
                onAdicionarMetaNoPeriodo = onAdicionarMetaNoPeriodo
            )
        }
    }
}

@Composable
fun MetaSection(
    periodo: PeriodoMeta,
    metas: List<Meta>,
    onStatusChange: (idMeta: String, novoStatus: StatusMeta) -> Unit,
    onSolicitarExclusao: (Meta) -> Unit,
    onAdicionarMetaNoPeriodo: (PeriodoMeta) -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val concluidas = metas.count { it.status == StatusMeta.CUMPRIDA }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-group-${periodo.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabeçalho da Seção com toggle de expandir/colapsar
        Surface(
            onClick = { isExpanded = !isExpanded },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CalendarMiniIcon(
                        color = periodo.corTema(),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = periodo.tituloSecao(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (metas.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (concluidas == metas.size) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (concluidas == metas.size) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "$concluidas/${metas.size} cumpridas ${periodo.rotuloTemporalSecao()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (concluidas == metas.size) Color(0xFF166534) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    ChevronIcon(
                        isExpanded = isExpanded,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // Conteúdo da seção
        if (isExpanded) {
            if (metas.isEmpty()) {
                ActionableEmptyState(
                    periodo = periodo,
                    onAdicionarClick = { onAdicionarMetaNoPeriodo(periodo) }
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    metas.forEach { meta ->
                        MetaCard(
                            meta = meta,
                            onStatusChange = { novoStatus -> onStatusChange(meta.id, novoStatus) },
                            onExcluirClick = { onSolicitarExclusao(meta) }
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. COMPONENTE DE CARD DE META (MetaCard)
// ============================================================================

@Composable
fun MetaCard(
    meta: Meta,
    onStatusChange: (StatusMeta) -> Unit,
    onExcluirClick: () -> Unit
) {
    val corCategoria = meta.categoria.corVisual()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-card-${meta.id}"),
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
            // Faixa lateral com a cor da categoria
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(corCategoria)
                    .testTag("meta-card-color-stripe-${meta.id}")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Linha Superior: Badge de Categoria e Intervalo de Datas Legível
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(
                        categoria = meta.categoria,
                        cor = corCategoria,
                        modifier = Modifier.testTag("meta-categoria-tag-${meta.id}")
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            CalendarMiniIcon(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = formatarIntervaloDatas(meta.dataInicio, meta.dataFim),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Descrição da Meta
                Text(
                    text = meta.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("meta-card-descricao-${meta.id}")
                )

                // Linha Inferior: Seletor de Status e Botão de Lixeira
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDropdownSelector(
                        statusAtual = meta.status,
                        onStatusChange = onStatusChange,
                        modifier = Modifier.testTag("meta-status-select-${meta.id}")
                    )

                    IconButton(
                        onClick = onExcluirClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("meta-button-remover-${meta.id}")
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
// 5. ESTADO VAZIO ACIONÁVEL COM BOTÃO CTA
// ============================================================================

@Composable
fun ActionableEmptyState(
    periodo: PeriodoMeta,
    onAdicionarClick: () -> Unit
) {
    val corTema = periodo.corTema()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = corTema.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, corTema.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = corTema.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CalendarMiniIcon(
                            color = corTema,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Nenhuma meta para ${periodo.rotuloTemporalSecao()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Defina objetivos para acompanhar seu progresso.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = onAdicionarClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, corTema.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlusIcon(color = corTema, modifier = Modifier.size(12.dp))
                    Text(
                        text = "Adicionar ${periodo.nomeAmigavel().lowercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = corTema
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. COMPONENTES VISUAIS AUXILIARES E SELETORES
// ============================================================================

@Composable
fun CategoryBadge(
    categoria: Categoria,
    cor: Color = categoria.corVisual(),
    modifier: Modifier = Modifier
) {
    Surface(
        color = cor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, cor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(cor)
            )
            Text(
                text = categoria.nomeAmigavel(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = cor
            )
        }
    }
}

@Composable
fun StatusDropdownSelector(
    statusAtual: StatusMeta,
    onStatusChange: (StatusMeta) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val visual = statusAtual.visual()

    Box(modifier = modifier) {
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
                StatusGlyphIcon(status = statusAtual, color = visual.corTexto, modifier = Modifier.size(13.dp))
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
            StatusMeta.entries.forEach { statusOpcao ->
                val opVisual = statusOpcao.visual()
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusGlyphIcon(status = statusOpcao, color = opVisual.corTexto, modifier = Modifier.size(14.dp))
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
fun CategoryDropdownSelector(
    label: String,
    opcoes: List<Categoria>,
    selecionado: Categoria,
    onSelecionar: (Categoria) -> Unit,
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
                        text = "$label (${opcoes.size} opções)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(selecionado.corVisual())
                        )
                        Text(
                            text = selecionado.nomeAmigavel(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                ChevronDownIcon(color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(9.dp))
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(opcao.corVisual())
                            )
                            Text(
                                text = opcao.nomeAmigavel(),
                                fontWeight = if (opcao == selecionado) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
fun PeriodDropdownSelector(
    label: String,
    opcoes: List<PeriodoMeta>,
    selecionado: PeriodoMeta,
    onSelecionar: (PeriodoMeta) -> Unit,
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
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CalendarMiniIcon(
                            color = selecionado.corTema(),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = selecionado.nomeAmigavel(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                ChevronDownIcon(color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(9.dp))
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalendarMiniIcon(
                                color = opcao.corTema(),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = opcao.nomeAmigavel(),
                                fontWeight = if (opcao == selecionado) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

// ============================================================================
// 7. ÍCONES VETORIAIS VIA CANVAS (100% Nítidos e Compatíveis)
// ============================================================================

@Composable
fun CheckIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.15f, size.height * 0.52f)
            lineTo(size.width * 0.40f, size.height * 0.78f)
            lineTo(size.width * 0.85f, size.height * 0.22f)
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun PartialProgressIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        drawCircle(color = color, style = Stroke(width = strokeWidth))
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = true
        )
    }
}

@Composable
fun PendingCircleIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        drawCircle(color = color, style = Stroke(width = strokeWidth))
    }
}

@Composable
fun StatusGlyphIcon(status: StatusMeta, color: Color, modifier: Modifier = Modifier) {
    when (status) {
        StatusMeta.CUMPRIDA -> CheckIcon(color = color, modifier = modifier)
        StatusMeta.PARCIALMENTE_CUMPRIDA -> PartialProgressIcon(color = color, modifier = modifier)
        StatusMeta.NAO_CUMPRIDA -> PendingCircleIcon(color = color, modifier = modifier)
    }
}

@Composable
fun ChevronDownIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.6.dp.toPx()
        val path = Path().apply {
            moveTo(0f, size.height * 0.35f)
            lineTo(size.width * 0.5f, size.height * 0.85f)
            lineTo(size.width, size.height * 0.35f)
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun ChevronIcon(isExpanded: Boolean, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val path = Path().apply {
            if (isExpanded) {
                moveTo(0f, size.height * 0.7f)
                lineTo(size.width * 0.5f, size.height * 0.25f)
                lineTo(size.width, size.height * 0.7f)
            } else {
                moveTo(0f, size.height * 0.3f)
                lineTo(size.width * 0.5f, size.height * 0.75f)
                lineTo(size.width, size.height * 0.3f)
            }
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

@Composable
fun CalendarMiniIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.5.dp.toPx()
        val r = 2.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, size.height * 0.22f),
            size = Size(size.width, size.height * 0.78f),
            cornerRadius = CornerRadius(r),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height * 0.48f),
            end = Offset(size.width, size.height * 0.48f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, 0f),
            end = Offset(size.width * 0.28f, size.height * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, 0f),
            end = Offset(size.width * 0.72f, size.height * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun PlusIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, 0f),
            end = Offset(size.width * 0.5f, size.height),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height * 0.5f),
            end = Offset(size.width, size.height * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun AlertIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val path = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.38f),
            end = Offset(size.width * 0.5f, size.height * 0.65f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = strokeWidth * 0.6f,
            center = Offset(size.width * 0.5f, size.height * 0.82f)
        )
    }
}

@Composable
fun TrashIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.5.dp.toPx()
        drawLine(
            color = color,
            start = Offset(size.width * 0.15f, size.height * 0.24f),
            end = Offset(size.width * 0.85f, size.height * 0.24f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val handlePath = Path().apply {
            moveTo(size.width * 0.38f, size.height * 0.24f)
            lineTo(size.width * 0.38f, size.height * 0.10f)
            lineTo(size.width * 0.62f, size.height * 0.10f)
            lineTo(size.width * 0.62f, size.height * 0.24f)
        }
        drawPath(handlePath, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        val binPath = Path().apply {
            moveTo(size.width * 0.24f, size.height * 0.26f)
            lineTo(size.width * 0.28f, size.height * 0.90f)
            lineTo(size.width * 0.72f, size.height * 0.90f)
            lineTo(size.width * 0.76f, size.height * 0.26f)
        }
        drawPath(binPath, color = color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))
        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.40f),
            end = Offset(size.width * 0.42f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.58f, size.height * 0.40f),
            end = Offset(size.width * 0.58f, size.height * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

// ============================================================================
// 8. FORMATAÇÃO VISUAL, CORES E UTILITÁRIOS
// ============================================================================

fun Categoria.corVisual(): Color = when (this) {
    Categoria.FACULDADE -> Color(0xFF2563EB)         // Royal Blue
    Categoria.TRABALHO -> Color(0xFFD97706)          // Warm Amber
    Categoria.SAUDE -> Color(0xFF059669)             // Emerald Green
    Categoria.LAZER -> Color(0xFF9333EA)             // Fuchsia / Purple
    Categoria.PROJETOS_PESSOAIS -> Color(0xFF0D9488) // Cyan / Teal
    Categoria.ESTUDOS -> Color(0xFF4F46E5)           // Indigo
}

fun Categoria.nomeAmigavel(): String = when (this) {
    Categoria.FACULDADE -> "Faculdade"
    Categoria.TRABALHO -> "Trabalho"
    Categoria.SAUDE -> "Saúde"
    Categoria.LAZER -> "Lazer"
    Categoria.PROJETOS_PESSOAIS -> "Projetos Pessoais"
    Categoria.ESTUDOS -> "Estudos"
}

fun PeriodoMeta.corTema(): Color = when (this) {
    PeriodoMeta.SEMANAL -> Color(0xFF2563EB) // Azul
    PeriodoMeta.MENSAL -> Color(0xFF7C3AED)  // Violeta
    PeriodoMeta.ANUAL -> Color(0xFF059669)   // Esmeralda
}

fun PeriodoMeta.nomeAmigavel(): String = when (this) {
    PeriodoMeta.SEMANAL -> "Semanal"
    PeriodoMeta.MENSAL -> "Mensal"
    PeriodoMeta.ANUAL -> "Anual"
}

fun PeriodoMeta.tituloSecao(): String = when (this) {
    PeriodoMeta.SEMANAL -> "Metas da Semana"
    PeriodoMeta.MENSAL -> "Metas do Mês"
    PeriodoMeta.ANUAL -> "Metas do Ano"
}

fun PeriodoMeta.rotuloTemporalSecao(): String = when (this) {
    PeriodoMeta.SEMANAL -> "nesta semana"
    PeriodoMeta.MENSAL -> "neste mês"
    PeriodoMeta.ANUAL -> "neste ano"
}

data class StatusVisual(
    val rotulo: String,
    val corTexto: Color,
    val corContainer: Color,
    val corBorda: Color
)

fun StatusMeta.visual(): StatusVisual = when (this) {
    StatusMeta.CUMPRIDA -> StatusVisual(
        rotulo = "Cumprida",
        corTexto = Color(0xFF166534),
        corContainer = Color(0xFFDCFCE7),
        corBorda = Color(0xFF86EFAC)
    )
    StatusMeta.PARCIALMENTE_CUMPRIDA -> StatusVisual(
        rotulo = "Parcial",
        corTexto = Color(0xFF9A3412),
        corContainer = Color(0xFFFFEDD5),
        corBorda = Color(0xFFFDBA74)
    )
    StatusMeta.NAO_CUMPRIDA -> StatusVisual(
        rotulo = "Pendente",
        corTexto = Color(0xFF475569),
        corContainer = Color(0xFFF1F5F9),
        corBorda = Color(0xFFCBD5E1)
    )
}

/**
 * Formata datas em formato legível e compacto (ex: "30 ago – 06 set").
 */
fun formatarIntervaloDatas(inicio: LocalDate, fim: LocalDate): String {
    val mesInicio = mesAbreviado(inicio.monthNumber)
    val mesFim = mesAbreviado(fim.monthNumber)

    val dataInicioStr = "${inicio.dayOfMonth.toString().padStart(2, '0')} $mesInicio"
    val dataFimStr = "${fim.dayOfMonth.toString().padStart(2, '0')} $mesFim"

    return "$dataInicioStr – $dataFimStr"
}

fun mesAbreviado(mesNumero: Int): String = when (mesNumero) {
    1 -> "jan"
    2 -> "fev"
    3 -> "mar"
    4 -> "abr"
    5 -> "mai"
    6 -> "jun"
    7 -> "jul"
    8 -> "ago"
    9 -> "set"
    10 -> "out"
    11 -> "nov"
    12 -> "dez"
    else -> ""
}

fun calcularIntervalo(periodo: PeriodoMeta, referencia: LocalDate): Pair<LocalDate, LocalDate> =
    when (periodo) {
        PeriodoMeta.SEMANAL -> referencia to referencia.plus(1, DateTimeUnit.WEEK)
        PeriodoMeta.MENSAL -> referencia to referencia.plus(1, DateTimeUnit.MONTH)
        PeriodoMeta.ANUAL -> referencia to referencia.plus(1, DateTimeUnit.YEAR)
    }
