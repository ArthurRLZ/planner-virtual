import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import ui.App

/**
 * Ponto de entrada da aplicação Web (WasmJs).
 *
 * No Compose Web com WasmJs, o entry point correto é uma função `main()` que
 * monta a UI via [ComposeViewport] no body da página, diferente do
 * Compose Desktop que usa `application { Window { ... } }`.
 *
 * A injeção de repositórios e casos de uso é feita dentro do composable [App],
 * usando `remember {}` para que as instâncias sobrevivam a recomposições.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App()
    }
}
