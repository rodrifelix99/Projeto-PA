package tests

import getjson.Controller
import getjson.GetJson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.*
import kotlin.concurrent.thread

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntegrationTests {
    val testPort = 8080

    private val client = OkHttpClient()
    private lateinit var serverThread: Thread

    @BeforeAll
    fun startServer() {
        serverThread = thread(start = true) {
            GetJson(Controller::class).start(testPort)
        }
        Thread.sleep(1000) // Wait for server to start
    }

    @AfterAll
    fun stopServer() {
        serverThread.interrupt()
    }

    private fun call(path: String): String {
        val req = Request.Builder()
            .url("http://localhost:$testPort$path")
            .build()
        client.newCall(req).execute().use { resp ->
            Assertions.assertTrue(resp.isSuccessful, "Expected 200 to $path")
            return resp.body!!.string()
        }
    }

    @Test
    fun testInts() {
        Assertions.assertEquals("[1, 2, 3]", call("/api/ints"))
    }

    @Test
    fun testPair() {
        Assertions.assertEquals("""{"first": "um", "second": "dois"}""",
            call("/api/pair"))
    }

    @Test
    fun testPathVar() {
        Assertions.assertEquals("\"foo!\"", call("/api/path/foo"))
        Assertions.assertEquals("\"bar!\"", call("/api/path/bar"))
    }

    @Test
    fun testArgs() {
        Assertions.assertEquals("""{"PA": "PAPAPA"}""",
            call("/api/args?n=3&text=PA"))
    }
}
