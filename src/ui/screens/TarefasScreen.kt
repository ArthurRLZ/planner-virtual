package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.usecase.tarefa.CriarTarefaUseCase

@Composable
fun TarefasScreen(criarTarefaUseCase: CriarTarefaUseCase) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tela de tarefas (em construção)")
    }
}