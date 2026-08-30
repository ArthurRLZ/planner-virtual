package ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import util.hoje
import util.novoId

/**
 * Tela principal para Criação e Listagem de Metas (Issue #4).
 *
 * Permite criar metas associadas a períodos (Semanal, Mensal, Anual),
 * categorizá-las com destaque visual por cores consistentes, e
 * atualizar o status de cumprimento (Cumprida, Parcialmente Cumprida, Não Cumprida).
 */
@Composable
fun MetasScreen(
    criarMetaUseCase: CriarMetaUseCase,
    listarMetasUseCase: ListarMetasUseCase,
    atualizarStatusMetaUseCase: AtualizarStatusMetaUseCase,
    removerMetaUseCase: RemoverMetaUseCase? = null
) {
    // Controlador de versão para recarregar a lista a cada mutação
    var versao by remember { mutableStateOf(0) }
    val metas = remember(versao) { listarMetasUseCase() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Cabeçalho da tela
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎯 Gestão de Metas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Defina, acompanhe e organize seus objetivos para a semana, mês e ano.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            // Formulário de Criação de Metas
            MetaForm(
                onSalvar = { novaMeta ->
                    val resultado = criarMetaUseCase(novaMeta)
                    resultado.onSuccess {
                        versao++
                    }
                    resultado
                }
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        }

        item {
            // Listagem de Metas Agrupadas por Período
            MetaList(
                metas = metas,
                onStatusChange = { idMeta, novoStatus ->
                    atualizarStatusMetaUseCase(idMeta, novoStatus)
                    versao++
                },
                onRemover = { idMeta ->
                    removerMetaUseCase?.invoke(idMeta)
                    versao++
                }
            )
        }
    }
}

/**
 * Componente de Formulário para criação de novas metas.
 *
 * Contém validações básicas de interface para evitar submissões inválidas
 * e inclui tags de teste para automação de QA.
 */
@Composable
fun MetaForm(
    onSalvar: (Meta) -> Result<Meta>
) {
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.FACULDADE) }
    var periodo by remember { mutableStateOf(PeriodoMeta.SEMANAL) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-form-card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Cadastrar Nova Meta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Campo de Descrição
            OutlinedTextField(
                value = descricao,
                onValueChange = {
                    descricao = it
                    mensagemErro = null
                    mensagemSucesso = null
                },
                label = { Text("Descrição do objetivo") },
                placeholder = { Text("Ex: Finalizar módulo do projeto de PLP") },
                isError = mensagemErro != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meta-input-descricao")
            )

            // Seleção de Categoria e Período
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Seletor de Categoria
                Box(modifier = Modifier.weight(1f)) {
                    EnumSelectDropdown(
                        label = "Categoria",
                        options = Categoria.entries,
                        selected = categoria,
                        displayName = { it.nomeExibicao() },
                        onSelected = { categoria = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meta-select-categoria")
                    )
                }

                // Seletor de Período
                Box(modifier = Modifier.weight(1f)) {
                    EnumSelectDropdown(
                        label = "Período",
                        options = PeriodoMeta.entries,
                        selected = periodo,
                        displayName = { it.nomeExibicao() },
                        onSelected = { periodo = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("meta-select-periodo")
                    )
                }
            }

            // Exibição de Mensagens de Validação / Erro / Sucesso
            mensagemErro?.let { erro ->
                Text(
                    text = erro,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("meta-error-mensagem")
                )
            }

            mensagemSucesso?.let { sucesso ->
                Text(
                    text = sucesso,
                    color = Color(0xFF2E7D32),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.testTag("meta-success-mensagem")
                )
            }

            // Botão de Submissão
            Button(
                onClick = {
                    // Validação de interface: campo de descrição não pode estar em branco
                    if (descricao.isBlank()) {
                        mensagemErro = "Por favor, preencha a descrição da meta."
                        mensagemSucesso = null
                        return@Button
                    }

                    val hoje = hoje()
                    val (inicio, fim) = calcularIntervaloPeriodo(periodo, hoje)

                    val novaMeta = Meta(
                        id = novoId(),
                        descricao = descricao.trim(),
                        categoria = categoria,
                        status = StatusMeta.NAO_CUMPRIDA,
                        periodo = periodo,
                        dataInicio = inicio,
                        dataFim = fim
                    )

                    val resultado = onSalvar(novaMeta)
                    resultado.onSuccess {
                        descricao = ""
                        mensagemErro = null
                        mensagemSucesso = "Meta adicionada com sucesso!"
                    }.onFailure { ex ->
                        mensagemErro = ex.message ?: "Erro ao salvar meta."
                        mensagemSucesso = null
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("meta-button-salvar")
            ) {
                Text("Adicionar Meta")
            }
        }
    }
}

/**
 * Componente responsável por renderizar as metas agrupadas logicamente por Período
 * (Semanal, Mensal e Anual), conforme especificado nos requisitos de UI.
 */
@Composable
fun MetaList(
    metas: List<Meta>,
    onStatusChange: (idMeta: String, novoStatus: StatusMeta) -> Unit,
    onRemover: ((idMeta: String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-list-container"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Itera sobre cada período para garantir o agrupamento lógico (Semanal, Mensal, Anual)
        PeriodoMeta.entries.forEach { periodo ->
            val metasDoPeriodo = metas.filter { it.periodo == periodo }

            MetaPeriodoSection(
                periodo = periodo,
                metas = metasDoPeriodo,
                onStatusChange = onStatusChange,
                onRemover = onRemover
            )
        }
    }
}

/**
 * Seção que agrupa visualmente as metas de um período específico.
 */
@Composable
fun MetaPeriodoSection(
    periodo: PeriodoMeta,
    metas: List<Meta>,
    onStatusChange: (idMeta: String, novoStatus: StatusMeta) -> Unit,
    onRemover: ((idMeta: String) -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-group-${periodo.name.lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cabeçalho da seção do período
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = periodo.iconePeriodo(),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = periodo.nomeExibicao(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Contador de metas do período
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "${metas.count { it.status == StatusMeta.CUMPRIDA }}/${metas.size} concluídas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (metas.isEmpty()) {
            Text(
                text = "Nenhuma meta definida para o período ${periodo.nomeExibicao().lowercase()}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metas.forEach { meta ->
                    MetaCard(
                        meta = meta,
                        onStatusChange = { novoStatus -> onStatusChange(meta.id, novoStatus) },
                        onRemover = onRemover?.let { { it(meta.id) } }
                    )
                }
            }
        }
    }
}

/**
 * Card individual de Meta com destaque visual por Categoria (cores consistentes)
 * e controles para atualização dinâmica de status.
 */
@Composable
fun MetaCard(
    meta: Meta,
    onStatusChange: (StatusMeta) -> Unit,
    onRemover: (() -> Unit)? = null
) {
    val corCategoria = meta.categoria.corDestaque()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meta-card-${meta.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Faixa vertical lateral com a cor da categoria (Destaque visual da mesma cor para a mesma categoria)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(corCategoria)
                    .testTag("meta-card-color-stripe-${meta.id}")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Linha superior: Tag da Categoria e Período / Datas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoriaBadge(
                        categoria = meta.categoria,
                        modifier = Modifier.testTag("meta-categoria-tag-${meta.id}")
                    )

                    Text(
                        text = "${meta.dataInicio} até ${meta.dataFim}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Descrição da meta
                Text(
                    text = meta.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("meta-card-descricao-${meta.id}")
                )

                // Linha inferior: Seletor de Status e Ações
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dropdown/Seletor para atualizar o status da meta
                    EnumSelectDropdown(
                        label = "Status",
                        options = StatusMeta.entries,
                        selected = meta.status,
                        displayName = { it.nomeExibicao() },
                        onSelected = onStatusChange,
                        modifier = Modifier.testTag("meta-status-select-${meta.id}")
                    )

                    if (onRemover != null) {
                        TextButton(
                            onClick = onRemover,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("meta-button-remover-${meta.id}")
                        ) {
                            Text("Excluir", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tag/Badge visual que exibe o nome da categoria com fundo e cor destacados
 * de acordo com a categoria da meta.
 */
@Composable
fun CategoriaBadge(
    categoria: Categoria,
    modifier: Modifier = Modifier
) {
    val cor = categoria.corDestaque()
    Surface(
        color = cor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, cor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(cor)
            )
            Text(
                text = categoria.nomeExibicao(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = cor
            )
        }
    }
}

/**
 * Dropdown reutilizável e customizável para seleção de Enums com rótulo descritivo.
 */
@Composable
fun <T> EnumSelectDropdown(
    label: String,
    options: List<T>,
    selected: T,
    displayName: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "$label: ${displayName(selected)}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            options.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(displayName(opcao)) },
                    onClick = {
                        onSelected(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}

// ============================================================================
// Funções Utilitárias e Mapeamentos Visuais
// ============================================================================

/**
 * Mapeamento de cores por categoria (Destaque visual consistente).
 * Cada categoria tem uma cor única que é replicada nos cards, tags e bordas.
 */
fun Categoria.corDestaque(): Color = when (this) {
    Categoria.FACULDADE -> Color(0xFF1565C0)         // Azul
    Categoria.TRABALHO -> Color(0xFFE65100)          // Laranja Escuro
    Categoria.SAUDE -> Color(0xFF2E7D32)             // Verde
    Categoria.LAZER -> Color(0xFF7B1FA2)             // Roxo
    Categoria.PROJETOS_PESSOAIS -> Color(0xFF00838F) // Azul Petróleo / Teal
    Categoria.ESTUDOS -> Color(0xFF4527A0)           // Índigo Profundo
}

/** Nomes de exibição amigáveis para as Categorias */
fun Categoria.nomeExibicao(): String = when (this) {
    Categoria.FACULDADE -> "Faculdade"
    Categoria.TRABALHO -> "Trabalho"
    Categoria.SAUDE -> "Saúde"
    Categoria.LAZER -> "Lazer"
    Categoria.PROJETOS_PESSOAIS -> "Projetos Pessoais"
    Categoria.ESTUDOS -> "Estudos"
}

/** Nomes de exibição amigáveis para os Períodos */
fun PeriodoMeta.nomeExibicao(): String = when (this) {
    PeriodoMeta.SEMANAL -> "Metas Semanais"
    PeriodoMeta.MENSAL -> "Metas Mensais"
    PeriodoMeta.ANUAL -> "Metas Anuais"
}

/** Ícones representativos de cada Período */
fun PeriodoMeta.iconePeriodo(): String = when (this) {
    PeriodoMeta.SEMANAL -> "📅"
    PeriodoMeta.MENSAL -> "🗓️"
    PeriodoMeta.ANUAL -> "🎯"
}

/** Nomes de exibição amigáveis para os Status de Meta */
fun StatusMeta.nomeExibicao(): String = when (this) {
    StatusMeta.CUMPRIDA -> "Cumprida"
    StatusMeta.PARCIALMENTE_CUMPRIDA -> "Parcialmente Cumprida"
    StatusMeta.NAO_CUMPRIDA -> "Não Cumprida"
}

/**
 * Calcula a data de início e fim da meta com base no período selecionado e data de referência.
 */
fun calcularIntervaloPeriodo(periodo: PeriodoMeta, referencia: LocalDate): Pair<LocalDate, LocalDate> =
    when (periodo) {
        PeriodoMeta.SEMANAL -> referencia to referencia.plus(1, DateTimeUnit.WEEK)
        PeriodoMeta.MENSAL -> referencia to referencia.plus(1, DateTimeUnit.MONTH)
        PeriodoMeta.ANUAL -> referencia to referencia.plus(1, DateTimeUnit.YEAR)
    }
