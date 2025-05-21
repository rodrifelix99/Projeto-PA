package getjson

import com.sun.net.httpserver.HttpServer
import jsonpackage.models.JsonElement
import jsonpackage.toJsonElement
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.primaryConstructor

/**
 * GetJson
 *
 * This class implements the GetJson API that will answer the API requests
 *
 * It uses reflection to find the @Mapping annotations and the @Path and @Param
 * annotations to extract the parameters from the request and call the
 * corresponding method in the controller.
 *
 * @param controllers The controllers to register in the server
 */

class GetJson(vararg controllers: KClass<*>) {

    // Estrutura interna que associa um padrão de rota ao seu manipulador
    private data class Route(
        val pathPattern: String,
        val handler: (pathVars: Map<String, String>, queryParams: Map<String, String>) -> JsonElement
    )

    // Lista mutável onde armazenamos todas as rotas registadas
    private val routes = mutableListOf<Route>()

    // No construtor, regista cada controlador recebido via reflexão
    init {
        controllers.forEach { registerController(it) }
    }

    // Função que regista métodos anotados de um determinado controlador
    private fun registerController(controllerClass: KClass<*>) {
        // Obtém o path base da classe através da anotação @Mapping ou sai se não existir
        val base = controllerClass.findAnnotation<Mapping>()?.value ?: return
        // Cria instância do controlador usando o construtor primário
        val instance = controllerClass.primaryConstructor!!.call()

        // Percorre cada method da classe à procura da anotação @Mapping
        for (func in controllerClass.memberFunctions) {
            val mapping = func.findAnnotation<Mapping>()
            if (mapping != null) {
                // monta o padrão completo
                val full = "/${base.trimStart('/')}/${mapping.value.trimStart('/')}"
                    .replace("//", "/")
                // adiciona à lista de rotas
                routes += Route(full) { pathVars, queryParams ->
                    val args = mutableListOf<Any?>()
                    for (p in func.parameters.drop(1)) {
                        val raw = if (p.findAnnotation<Path>() != null) {
                            pathVars[p.name]!!
                        } else if (p.findAnnotation<Param>() != null) {
                            queryParams[p.name]!!
                        } else {
                            error("Sem @Path ou @Param: ${p.name}")
                        }
                        args += when (p.type.classifier) {
                            Int::class    -> raw.toInt()
                            Double::class -> raw.toDouble()
                            Boolean::class-> raw.toBoolean()
                            String::class -> raw
                            else          -> raw
                        }
                    }
                    val result = func.call(instance, *args.toTypedArray())
                    toJsonElement(result)
                }
            }
        }
    }

    // Inicia o servidor HTTP e faz o dispatch interno de todas as rotas
    fun start(port: Int) {
        // Cria servidor ligado à porta especificada
        val server = HttpServer.create(InetSocketAddress(port), 0)
        // Usa um pool de threads para processar pedidos concorrentes
        server.executor = Executors.newFixedThreadPool(4)

        // Regista um único context raiz onde todo o dispatch é feito manualmente
        server.createContext("/") { exchange ->
            // Obtém o URI da requisição
            val uri = exchange.requestURI
            // Normaliza o path removendo a barra inicial
            val rawPath = uri.path.trimStart('/')
            // Extrai parâmetros da query ‘string’ para um mapa
            val queryParams = parseQuery(uri)

            // Procura a primeira rota cujo padrão, transformado em regex, bate com o path
            val route = routes.firstOrNull { r ->
                val regex = ("^" +
                        r.pathPattern.trimStart('/')
                            .replace("\\{[^}]+}".toRegex(), "([^/]+)") +
                        "$"
                        ).toRegex()
                regex.matches(rawPath)
            }

            // Se não encontrar rota, devolve 404
            if (route == null) {
                val msg = "404 - Not found"
                exchange.sendResponseHeaders(404, msg.length.toLong())
                exchange.responseBody.use { it.write(msg.toByteArray()) }
                return@createContext
            }

            // Extrai os valores das path-vars e chama o handler para obter JsonElement
            val pathVars = extractPathVars(route.pathPattern, uri)
            val json = route.handler(pathVars, queryParams)
            // Serialize o JsonElement para bytes
            val bytes = json.toJsonString().toByteArray()

            // Envia cabeçalhos de sucesso e escreve o corpo da resposta
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        // Arranca o servidor de forma não bloqueante
        server.start()
        println("Server started on port $port")
    }

    // Função auxiliar que extrai variáveis do path com base no padrão da rota
    private fun extractPathVars(pattern: String, uri: URI): Map<String, String> {
        val result = mutableMapOf<String,String>()

        // Remove barras iniciais e parte o padrão e o path por “/”
        val patternParts = pattern.trimStart('/').split('/')
        val pathParts = uri.path.trimStart('/').split('/')

        // Para cada segmento do padrão, se for do tipo {nome}, associa ao valor correspondente
        for (i in patternParts.indices) {
            val pat = patternParts[i]
            if (pat.startsWith("{") && pat.endsWith("}")) {
                val name = pat.removePrefix("{").removeSuffix("}")
                // Garante que existe valor naquele índice
                if (i < pathParts.size) {
                    result[name] = pathParts[i]
                }
            }
        }

        return result
    }

    // Função auxiliar que converte a query ‘string’ em mapa de pares chave-valor
    private fun parseQuery(uri: URI): Map<String, String> =
        uri.rawQuery
            // Divide por & e depois por =, garantindo pares válidos
            ?.split("&")
            ?.mapNotNull {
                it.split("=").takeIf { parts -> parts.size == 2 }
                    ?.let { parts -> parts[0] to parts[1] }
            }
            // Se não houver query, devolve mapa vazio
            ?.toMap()
            ?: emptyMap()
}
