// GetJson.kt
package getjson

import jsonpackage.toJsonElement
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor
import com.sun.net.httpserver.HttpServer
import jsonpackage.models.JsonElement

class GetJson(vararg controllers: KClass<*>) {

    private data class Route(
        val pathPattern: String,
        val httpMethod: String = "GET",
        val handler: (Map<String,String>, Map<String,String>) -> JsonElement
    )

    private val routes = mutableListOf<Route>()

    init {
        // Registar controllers e métodos anotados
        controllers.forEach { registerController(it) }
        println("=== Rotas registadas ===")
        routes.forEach { println("  pattern='${it.pathPattern}'") }
    }

    private fun registerController(controllerClass: KClass<*>) {
        val basePath = controllerClass.findAnnotation<Mapping>()?.value ?: return
        val instance = controllerClass.primaryConstructor!!.call()
        for (func in controllerClass.memberFunctions) {
            val m = func.findAnnotation<Mapping>() ?: continue
            val fullPath = "/$basePath/${m.value}".replace("//", "/")
            routes += Route(
                pathPattern = fullPath,
                handler = { pathVars, queryParams ->
                    // Construir lista de argumentos para a invocação
                    val args = func.parameters.drop(1).map { param ->
                        when {
                            param.findAnnotation<Path>() != null ->
                                pathVars[param.name]!!
                            param.findAnnotation<Param>() != null ->
                                queryParams[param.name]!!
                            else ->
                                throw IllegalArgumentException("Parâmetro não anotado")
                        }
                    }
                    // Invocar e converter
                    val result = func.call(instance, *args.toTypedArray())
                    toJsonElement(result)
                }
            )
        }
    }

    fun start(port: Int) {
        val server = HttpServer.create(InetSocketAddress(port), 0)
        server.executor = Executors.newFixedThreadPool(4)

        // Só um context, puxa tudo para cá
        server.createContext("/") { exchange ->
            val uri = exchange.requestURI
            val path = uri.path
            val queryParams = parseQuery(uri)
            val stripped = path.removePrefix("/")

            routes.forEach { route ->
                val regex = ("^" +
                        route.pathPattern
                            .replace("\\{[^}]+\\}".toRegex(), "([^/]+)")
                            .removePrefix("/") +
                        "$"
                        ).toRegex()
                println("    tenta '${route.pathPattern}' => regex='${regex.pattern}' → matches? ${regex.matches(stripped)}")
            }

            // Acha a rota cujo regex bate com o path completo
            val route = routes.firstOrNull { route ->
                // Constrói o regex a partir do pattern
                val regex = ("^" +
                        route.pathPattern
                            .replace("\\{[^}]+\\}".toRegex(), "([^/]+)")
                            .removePrefix("/") +
                        "$"
                        ).toRegex()
                regex.matches(path.removePrefix("/"))
            }

            if (route == null) {
                val msg = "Not found"
                exchange.sendResponseHeaders(404, msg.toByteArray().size.toLong())
                exchange.responseBody.use { it.write(msg.toByteArray()) }
                return@createContext
            }

            // Se houver rota, extrai os vars e chama o handler
            val pathVars = extractPathVars(route.pathPattern, uri)
            val json    = route.handler(pathVars, queryParams)
            val resp    = json.toJsonString().toByteArray()

            exchange.sendResponseHeaders(200, resp.size.toLong())
            exchange.responseBody.use { it.write(resp) }
        }

        server.start()
        println("Server started on port $port")
    }

    private fun extractPathVars(pattern: String, uri: URI): Map<String, String> {
        // 1. Extrai os nomes dos parâmetros: tudo o que está entre { e }
        val paramNames = "\\{([^}]+)\\}".toRegex()
            .findAll(pattern)
            .map { it.groupValues[1] }
            .toList()

        // 2. Constrói um regex a partir do pattern, substituindo {var} por "([^/]+)"
        val regexPattern = pattern
            .replace("\\{[^}]+\\}".toRegex(), "([^/]+)")
            .let { "^$it\$" } // garante match completo
        val regex = regexPattern.toRegex()

        // 3. Tenta casar com o path real
        val path = uri.path
        val match = regex.matchEntire(path) ?: return emptyMap()

        // 4. Os grupos de captura correspondem aos valores
        val values = match.groupValues.drop(1) // descarta groupValues[0] = match completo

        // 5. Mapeia nome -> valor
        return paramNames.zip(values).toMap()
    }

    private fun parseQuery(uri: URI): Map<String,String> {
        return uri.rawQuery
            ?.split("&")
            ?.mapNotNull {
                it.split("=").takeIf { parts -> parts.size == 2 }
                    ?.let { parts -> parts[0] to parts[1] }
            }
            ?.toMap()
            ?: emptyMap()
    }

}
