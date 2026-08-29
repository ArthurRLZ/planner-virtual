package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.model.Categoria
import domain.model.Meta
import domain.model.PeriodoMeta
import domain.model.StatusMeta
import domain.usecase.meta.AtualizarStatusMetaUseCase
import domain.usecase.meta.CriarMetaUseCase
import domain.usecase.meta.ListarMetasUseCase
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import util.hoje
import util.novoId

@Composable
fun MetasScreen(
    criarMetaUseCase: CriarMetaUseCase,
    listarMetasUseCase: ListarMetasUseCase,
    atualizarStatusMetaUseCase: AtualizarStatusMetaUseCase
) {
    var descricao by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.FACULDADE) }
    var periodo by remember { mutableStateOf(PeriodoMeta.SEMANAL) }
    var erro by remember { mutableStateOf<String?>(null) }

    // "versao" força a lista a ser recarregada do repositório a cada mudança.
    var versao by remember { mutableStateOf(0) }
    val metas = remember(versao) { listarMetasUseCase() }

    Column(modifier = Modifier.padding(24.dp).fillMaxSize()) {
        Text("Metas", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição da meta") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EnumDropdown(
                label = "Categoria",
                options = Categoria.entries.toList(),
                selected = categoria,
                onSelected = { categoria = it }
            )
            EnumDropdown(
                label = "Período",
                options = PeriodoMeta.entries.toList(),
                selected = periodo,
                onSelected = { periodo = it }
            )
        }
        Spacer(Modifier.height(8.dp))

        erro?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(onClick = {
            if (descricao.isBlank()) {
                erro = "A descrição não pode ficar vazia"
                return@Button
            }
            val (inicio, fim) = periodoParaIntervalo(periodo, hoje())

            val resultado = criarMetaUseCase(
                Meta(
                    id = novoId(),
                    descricao = descricao,
                    categoria = categoria,
                    status = StatusMeta.NAO_CUMPRIDA,
                    periodo = periodo,
                    dataInicio = inicio,
                    dataFim = fim
                )
            )

            resultado
                .onSuccess {
                    descricao = ""
                    erro = null
                    versao++
                }
                .onFailure { erro = it.message }
        }) {
            Text("Adicionar Meta")
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        Text("Metas cadastradas (${metas.size})", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(metas, key = { it.id }) { meta ->
                MetaCard(
                    meta = meta,
                    onStatusChange = { novoStatus ->
                        atualizarStatusMetaUseCase(meta.id, novoStatus)
                        versao++
                    }
                )
            }
        }
    }
}

@Composable
private fun MetaCard(meta: Meta, onStatusChange: (StatusMeta) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(meta.descricao, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${meta.categoria} · ${meta.periodo} · ${meta.dataInicio} a ${meta.dataFim}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            EnumDropdown(
                label = "Status",
                options = StatusMeta.entries.toList(),
                selected = meta.status,
                onSelected = onStatusChange
            )
        }
    }
}

/** Dropdown genérico reutilizável para qualquer enum (categoria, período, status...). */
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("$label: $selected")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.toString()) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun periodoParaIntervalo(periodo: PeriodoMeta, referencia: LocalDate): Pair<LocalDate, LocalDate> =
    when (periodo) {
        PeriodoMeta.SEMANAL -> referencia to referencia.plus(1, DateTimeUnit.WEEK)
        PeriodoMeta.MENSAL -> referencia to referencia.plus(1, DateTimeUnit.MONTH)
        PeriodoMeta.ANUAL -> referencia to referencia.plus(1, DateTimeUnit.YEAR)
    }
