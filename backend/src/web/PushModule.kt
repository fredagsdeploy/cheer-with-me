package dev.fredag.cheerwithme.web

import dev.fredag.cheerwithme.model.Platform
import dev.fredag.cheerwithme.model.UnauthorizedException
import dev.fredag.cheerwithme.service.PushService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext

data class DeviceRegistration(val pushToken: String, val platform: Platform)

fun Routing.pushRouting(pushService: PushService, testing: Boolean = false) {
    post("/push/register-device") {
        val registration = call.receive<DeviceRegistration>()
        val nick = getNickOrUnauthorized()
        pushService.registerDeviceToken(nick, registration)

        call.respondText("Hello")
    }

    post("/push/test/{nick}") {
        pushService.push(call.parameters["nick"]!!, call.receive())
    }

}

private fun PipelineContext<Unit, ApplicationCall>.getNickOrUnauthorized(): String {
    val authHeader = call.request.header("authorization")
    if (authHeader.isNullOrBlank()) {
        throw UnauthorizedException("No authorization header present")
    }

    return authHeader.split(" ")[1]
}