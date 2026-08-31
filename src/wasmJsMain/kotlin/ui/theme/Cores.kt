package ui.theme

import androidx.compose.ui.graphics.Color
import domain.model.Categoria

/**
 * Mapeamento de cada Categoria para sua cor de destaque visual (Issue #9).
 *
 * Usado em TarefasScreen.kt e MetasScreen.kt para garantir que tarefas e metas
 * da mesma categoria sejam sempre exibidas com a mesma cor na UI.
 */
fun Categoria.corVisual(): Color = when (this) {
    Categoria.FACULDADE -> Color(0xFF2563EB)         // Royal Blue
    Categoria.TRABALHO -> Color(0xFFD97706)          // Warm Amber
    Categoria.SAUDE -> Color(0xFF059669)             // Emerald Green
    Categoria.LAZER -> Color(0xFF9333EA)             // Fuchsia / Purple
    Categoria.PROJETOS_PESSOAIS -> Color(0xFF0D9488) // Cyan / Teal
    Categoria.ESTUDOS -> Color(0xFF4F46E5)           // Indigo
}

