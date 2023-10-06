//import com.example.plugins.*
//import io.ktor.server.application.*
//import io.ktor.server.cio.*
//import io.ktor.server.engine.*
//
//fun main() {
//    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
//        .start(wait = true)
//}
//
//fun Application.module() {
//    configureRouting()
//}


import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.fx.coroutines.resourceScope
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.serialization.Serializable


@Serializable
data class Data(val id: Int, val title: String, val description: String)

fun main() = SuspendApp {
    resourceScope {
        server(CIO, port = 8080) {
            install(ContentNegotiation) {
                json()
            }

            routing {
                get("/") {
                    call.respond(Data(123, "Hello", "This is the response"))
                }
            }
        }
        awaitCancellation()
    }
}