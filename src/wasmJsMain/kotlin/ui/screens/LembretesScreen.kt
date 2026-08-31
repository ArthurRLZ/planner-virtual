package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.DiaSemana
import domain.model.Lembrete
import domain.model.Recorrencia
import domain.model.TipoLembrete
import domain.usecase.lembrete.CriarLembreteUseCase
import util.novoId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LembretesScreen(criarLembreteUseCase: CriarLembreteUseCase) {
    var descricao by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(TipoLembrete.REUNIAO) }
    var recorrencia by remember { mutableStateOf(Recorrencia.UNICO) }
    var diaSemana by remember { mutableStateOf(DiaSemana.SEGUNDA) }
    var tipoExpandido by remember { mutableStateOf(false) }
    var diaExpandido by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    mensagemSucesso = "Lembrete criado com sucesso."
                }.onFailure {
                    mensagemErro = "Não foi possível criar o lembrete."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Criar lembrete")
        }

        mensagemSucesso?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
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