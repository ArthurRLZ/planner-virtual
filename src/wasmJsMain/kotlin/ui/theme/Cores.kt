package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import domain.model.Categoria

// Palette do Tema Claro
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFF0D9488),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

// Palette do Tema Escuro (Grafite / Preto)
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE2E8F0),       // Grafite claro / Prata para destaque
    onPrimary = Color(0xFF18181B),     // Texto/ícone sobre a cor primária
    secondary = Color(0xFF2DD4BF),
    background = Color(0xFF121212),    // Preto profundo
    surface = Color(0xFF1E1E1E),       // Grafite escuro para cards/gaveta
    onBackground = Color(0xFFE4E4E7),
    onSurface = Color(0xFFE4E4E7)
)

/**
 * Retorna uma cor vibrante para modo claro e um tom mais suave/pastel para modo escuro.
 */
@Composable
fun Categoria.corVisual(): Color {
    val isDark = isSystemInDarkTheme()
    return when (this) {
        Categoria.FACULDADE -> if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB)
        Categoria.TRABALHO -> if (isDark) Color(0xFFFCD34D) else Color(0xFFD97706)
        Categoria.SAUDE -> if (isDark) Color(0xFF6EE7B7) else Color(0xFF059669)
        Categoria.LAZER -> if (isDark) Color(0xFFC084FC) else Color(0xFF9333EA)
        Categoria.PROJETOS_PESSOAIS -> if (isDark) Color(0xFF5EEAD4) else Color(0xFF0D9488)
        Categoria.ESTUDOS -> if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
    }
}