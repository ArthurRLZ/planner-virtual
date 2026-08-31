package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import domain.model.DiaSemana
import domain.model.Lembrete
import domain.model.Recorrencia
import domain.model.TipoLembrete
import domain.usecase.lembrete.CriarLembreteUseCase
import domain.usecase.lembrete.ListarLembretesUseCase
import domain.usecase.lembrete.RemoverLembreteUseCase
import kotlinx.coroutines.launch
import util.novoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LembretesScreen(
    criarLembreteUseCase: CriarLembreteUseCase,
    listarLembretesUseCase: ListarLembretesUseCase,
    removerLembreteUseCase: RemoverLembreteUseCase
) {
    var descricao by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoLembrete.REUNIAO) }
    var recorrencia by remember { mutableStateOf(Recorrencia.UNICO) }
    var diaSemana by remember { mutableStateOf(DiaSemana.SEGUNDA) }
    var tipoExpandido by remember { mutableStateOf(false) }
    var diaExpandido by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    // Controla o refresh da listagem: incrementar força buscar os lembretes de novo
    var versao by remember { mutableStateOf(0) }
    val lembretes = remember(versao) { listarLembretesUseCase() }

    // Lembrete selecionado para confirmação de exclusão (Issue #17)
    var lembreteParaExcluir by remember { mutableStateOf<Lembrete?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Novo lembrete", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = descricao,
            onValueChange = {
                descricao = it
                mensagemErro = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Descrição") },
            singleLine = true,
            isError = mensagemErro != null,
            supportingText = mensagemErro?.let { erro -> { Text(erro) } }
        )

        ExposedDropdownMenuBox(
            expanded = tipoExpandido,
            onExpandedChange = { tipoExpandido = it }
        ) {
            OutlinedTextField(
                value = tipo.rotulo(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpandido) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = tipoExpandido,
                onDismissRequest = { tipoExpandido = false }
            ) {
                TipoLembrete.entries.forEach { opcao ->
                    DropdownMenuItem(
                        text = { Text(opcao.rotulo()) },
                        onClick = {
                            tipo = opcao
                            tipoExpandido = false
                        }
                    )
                }
            }
        }

        Text("Recorrência", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Recorrencia.entries.forEach { opcao ->
                FilterChip(
                    selected = recorrencia == opcao,
                    onClick = { recorrencia = opcao },
                    label = { Text(opcao.rotulo()) }
                )
            }
        }

        if (recorrencia == Recorrencia.SEMANAL) {
            ExposedDropdownMenuBox(
                expanded = diaExpandido,
                onExpandedChange = { diaExpandido = it }
            ) {
                OutlinedTextField(
                    value = diaSemana.rotulo(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dia da semana") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = diaExpandido) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = diaExpandido,
                    onDismissRequest = { diaExpandido = false }
                ) {
                    DiaSemana.entries.forEach { opcao ->
                        DropdownMenuItem(
                            text = { Text(opcao.rotulo()) },
                            onClick = {
                                diaSemana = opcao
                                diaExpandido = false
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (descricao.isBlank()) {
                    mensagemErro = "Informe a descrição do lembrete."
                    return@Button
                }

                criarLembreteUseCase(
                    Lembrete(
                        id = novoId(),
                        descricao = descricao.trim(),
                        tipo = tipo,
                        recorrencia = recorrencia,
                        diaSemana = diaSemana.takeIf { recorrencia == Recorrencia.SEMANAL }
                    )
                ).onSuccess {
                    descricao = ""
                    versao++
                    mensagemSucesso = "Lembrete criado com sucesso."
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Lembrete criado com sucesso.")
                    }
                }.onFailure {
                    mensagemErro = "Não foi possível criar o lembrete."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar lembrete")
        }

        mensagemSucesso?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text("Lembretes cadastrados", style = MaterialTheme.typography.titleMedium)

        if (lembretes.isEmpty()) {
            Text(
                "Nenhum lembrete cadastrado ainda.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            lembretes.forEach { lembrete ->
                LembreteItem(
                    lembrete = lembrete,
                    onExcluir = { lembreteParaExcluir = lembrete }
                )
            }
        }
    }

    // Host das mensagens de feedback (criação/exclusão)
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp)
    )

    // Diálogo de confirmação de exclusão (Issue #17)
    lembreteParaExcluir?.let { lembrete ->
        AlertDialog(
            onDismissRequest = { lembreteParaExcluir = null },
            title = { Text("Excluir lembrete") },
            text = {
                Text("Tem certeza de que deseja remover o lembrete \"${lembrete.descricao}\"? Esta ação não poderá ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        removerLembreteUseCase(lembrete.id)
                        versao++
                        lembreteParaExcluir = null
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Lembrete excluído com sucesso.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { lembreteParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
    }
}

@Composable
private fun LembreteItem(
    lembrete: Lembrete,
    onExcluir: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lembrete.descricao,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                val subtitulo = buildString {
                    append(lembrete.tipo.rotulo())
                    append(" • ")
                    append(lembrete.recorrencia.rotulo())
                    if (lembrete.recorrencia == Recorrencia.SEMANAL && lembrete.diaSemana != null) {
                        append(" (")
                        append(lembrete.diaSemana.rotulo())
                        append(")")
                    }
                }
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onExcluir) {
                TrashIcon(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun TipoLembrete.rotulo() = when (this) {
    TipoLembrete.REUNIAO -> "Reunião"
    TipoLembrete.LIGACAO -> "Ligação"
    TipoLembrete.COMPRA -> "Compra"
    TipoLembrete.ESTUDO -> "Estudo"
    TipoLembrete.EXERCICIO -> "Exercício"
    TipoLembrete.ENTREGA_TRABALHO -> "Entrega de Trabalho"
}

private fun Recorrencia.rotulo() = when (this) {
    Recorrencia.UNICO -> "Único"
    Recorrencia.SEMANAL -> "Semanal"
}

private fun DiaSemana.rotulo() = when (this) {
    DiaSemana.SEGUNDA -> "Segunda-feira"
    DiaSemana.TERCA -> "Terça-feira"
    DiaSemana.QUARTA -> "Quarta-feira"
    DiaSemana.QUINTA -> "Quinta-feira"
    DiaSemana.SEXTA -> "Sexta-feira"
    DiaSemana.SABADO -> "Sábado"
    DiaSemana.DOMINGO -> "Domingo"
}