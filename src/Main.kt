import getjson.GetJson
import getjson.Controller

/**
 * Main funcion
 *
 * This class runs the API
 *
 */

fun main() {
    // Arranca o framework com o Controller
    val app = GetJson(Controller::class)
    // Arranca na porta 8081
    app.start(8081)
}
