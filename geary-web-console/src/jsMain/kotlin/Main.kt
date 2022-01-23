import QueryOptions.*
import androidx.compose.runtime.*
import com.mineinabyss.geary.webconsole.data.EntityInfo
import components.Container
import components.Row
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

enum class QueryOptions() {
    PLAYER, ID
}

fun main() {
    renderComposable(rootElementId = "root") {
        var input by remember { mutableStateOf("") }
        var entityInfo: EntityInfo? by remember { mutableStateOf(null) }
        val coroutineScope = rememberCoroutineScope()
        var queryOption by remember { mutableStateOf(ID) }

        Container {
            Row {
                TextArea(input) { onInput { input = it.value } }
                Button({
                    onClick {
                        coroutineScope.launch {
                            entityInfo = when (queryOption) {
                                PLAYER -> getEntityInfo(input)
                                ID -> getEntityInfo(input.toInt())
                            }
                        }
                    }
                }) {
                    Text("Search")
                }

                Select({
                    onChange { queryOption = valueOf(it.value!!) }
                }) {
                    for (option in values())
                        Option(option.name) {
                            Text(option.name)
                        }
                }
                entityInfo?.apply {
                    H1 { Text("Entity Info") }
                    Text(info)
                }
            }
        }
    }
}
