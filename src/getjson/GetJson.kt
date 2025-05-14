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
        controllerClass.memberFunctions
            .mapNotNull { func ->
                func.findAnnotation<Mapping>()?.let { m ->
                    // Constrói a rota completa combinando base e subpath
                    val full = "/${base.trimStart('/')}/${m.value.trimStart('/')}"
                        .replace("//", "/")
                    full to func
                }
            }
            .forEach { (pattern, func) ->
                routes += Route(pattern) { pathVars, queryParams ->
                    val args: List<Any?> = func.parameters.drop(1).map { p ->
                        // 1) obtém sempre o valor raw como String
                        val raw = when {
                            p.findAnnotation<Path>()  != null -> pathVars[p.name]!!
                            p.findAnnotation<Param>() != null -> queryParams[p.name]!!
                            else -> error("Parâmetro sem @Path ou @Param: ${p.name}")
                        }
                        // 2) converte para o tipo esperado pelo parâmetro
                        when (p.type.classifier) {
                            Int::class    -> raw.toInt()
                            Double::class -> raw.toDouble()
                            Boolean::class-> raw.toBoolean()
                            String::class -> raw
                            else          -> raw // ou error("Tipo não suportado: ${p.type}")
                        }
                    }
                    // 3) invoca o método com os argumentos tipados
                    val result = func.call(instance, *args.toTypedArray())
                    toJsonElement(result)
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
        // Encontra todos os nomes de variáveis definidos entre {…}
        val names = "\\{([^}]+)}".toRegex()
            .findAll(pattern)
            .map { it.groupValues[1] }
            .toList()
        // Constrói regex que captura os valores no URI
        val regex = ("^" +
                pattern.trimStart('/')
                    .replace("\\{[^}]+}".toRegex(), "([^/]+)") +
                "$"
                ).toRegex()

        // Tenta casar o path normalizado; se falhar, retorna mapa vazio
        val match = regex.matchEntire(uri.path.trimStart('/')) ?: return emptyMap()
        // Obtém todos os grupos de captura (exceto o match completo)
        val values = match.groupValues.drop(1)
        // Associa cada nome ao respetivo valor capturado
        return names.zip(values).toMap()
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
