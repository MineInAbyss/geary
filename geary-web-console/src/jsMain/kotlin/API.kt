import com.mineinabyss.geary.webconsole.data.EntityInfo
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.browser.window

val endpoint = window.location.origin // only needed until https://youtrack.jetbrains.com/issue/KTOR-453 is resolved

val jsonClient = HttpClient {
    install(JsonFeature) { serializer = KotlinxSerializer() }
}

suspend fun getEntityInfo(id: Int): EntityInfo {
    return jsonClient.get("$endpoint${EntityInfo.path}/id/$id")
}

suspend fun getEntityInfo(playerName: String): EntityInfo {
    return jsonClient.get("$endpoint${EntityInfo.path}/player/$playerName")
}

//suspend fun addShoppingListItem(shoppingListItem: ShoppingListItem) {
//    jsonClient.post<Unit>(endpoint + ShoppingListItem.path) {
//        contentType(ContentType.Application.Json)
//        body = shoppingListItem
//    }
//}
//
//suspend fun deleteShoppingListItem(shoppingListItem: ShoppingListItem) {
//    jsonClient.delete<Unit>(endpoint + ShoppingListItem.path + "/${shoppingListItem.id}")
//}
