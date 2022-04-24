import QueryOptions.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import com.mineinabyss.geary.webconsole.data.EntityInfo
import kotlinx.coroutines.launch
import org.jetbrains.skiko.wasm.onWasmReady

enum class QueryOptions {
    PLAYER, ID
}

fun main() {
    onWasmReady {
        Window("Geary web console") {
            var input by remember { mutableStateOf("") }
            var entityInfo: EntityInfo? by remember { mutableStateOf(null) }
            val coroutineScope = rememberCoroutineScope()
            var queryOption by remember { mutableStateOf(ID) }

            Row {
                TextField(input, onValueChange = { input = it })
                Button(onClick = {
                    coroutineScope.launch {
                        entityInfo = when (queryOption) {
                            PLAYER -> getEntityInfo(input)
                            ID -> getEntityInfo(input.toInt())
                        }
                    }
                }) {
                    Text("Search")
                }
                LazyColumn {
                    items(values()) { option ->
                        Button(onClick = { queryOption = option }) {
                            Text(option.name)
                        }
                    }
                }

                entityInfo?.apply {
                    Text("Entity Info", style = MaterialTheme.typography.h1)
                    Text(info)
                }
            }
        }
    }
}
