package components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div

@Composable
fun Container(content: @Composable () -> Unit) {
    Div({ classes("container") }) {
        content()
    }
}

@Composable
fun Row(content: @Composable () -> Unit) {
    Div({ classes("row") }) {
        content()
    }
}
