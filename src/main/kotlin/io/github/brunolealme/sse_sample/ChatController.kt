package io.github.brunolealme.sse_sample

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/api/chat")
class ChatController(private val resilienceHandler: ChatResilienceHandler) {

    private val sessionSinks = ConcurrentHashMap<String, Sinks.Many<ServerSentEvent<Message>>>()

    private fun getOrCreateSink(sessionId: String): Sinks.Many<ServerSentEvent<Message>> {
        return sessionSinks.computeIfAbsent(sessionId) {
            Sinks.many().multicast().onBackpressureBuffer()
        }
    }

    @GetMapping("/stream/{sessionId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamChat(@PathVariable sessionId: String): Flux<ServerSentEvent<Message>> {
        // Busca histórico de forma resiliente
        val historyFlux = resilienceHandler.getHistoryFailOpen(sessionId)
            .map { ServerSentEvent.builder(it).event("history").build() }

        val liveFlux = getOrCreateSink(sessionId).asFlux()

        return Flux.concat(historyFlux, liveFlux)
    }

    @PostMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sendMessage(
        @RequestBody request: RequestDTO
    ): Flux<Message> {
        val userMessage = Message(sender = request.userId, content = request.content)
        val aiMessage = Message(sender = "AI", content = "Resposta prefixada para: '$request.content'")

        val sink = getOrCreateSink(request.sessionId)

        // Pipeline do usuário com fail-open
        val userFlow = resilienceHandler.saveMessageFailOpen(request.userId, request.sessionId, userMessage)
            .doOnTerminate { sink.tryEmitNext(ServerSentEvent.builder(userMessage).event("message").build()) }
            .then(Mono.just(userMessage))

        // Pipeline da IA com fail-open
        val aiFlow = resilienceHandler.saveMessageFailOpen(request.userId, request.sessionId, aiMessage)
            .doOnTerminate { sink.tryEmitNext(ServerSentEvent.builder(aiMessage).event("message").build()) }
            .then(Mono.just(aiMessage))

        return Flux.concat(userFlow, aiFlow)
    }
}
