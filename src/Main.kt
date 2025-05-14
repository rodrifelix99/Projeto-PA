import getjson.GetJson
import getjson.Controller

fun main() {
    // Instancia o framework com o teu Controller
    val app = GetJson(Controller::class)
    // Arranca na porta 8080
    app.start(8080)
}
