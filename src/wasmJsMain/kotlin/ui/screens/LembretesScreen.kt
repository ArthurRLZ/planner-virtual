package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.usecase.lembrete.CriarLembreteUseCase

@Composable
fun LembretesScreen(criarLembreteUseCase: CriarLembreteUseCase) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Tela de lembretes (em construção)")
    }
}