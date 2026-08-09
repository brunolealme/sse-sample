package io.github.brunolealme.sse_sample

import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import java.util.concurrent.ConcurrentHashMap

//@RestController
//@RequestMapping("/api/chat")
class ChatControllerOld(private val chatService: ChatService) {

//    // Sinks são canais para enviar eventos em tempo real para conexões ativas
//    private val sessionSinks = ConcurrentHashMap<String, Sinks.Many<ServerSentEvent<Message>>>()
//
//    private fun getOrCreateSink(sessionId: String): Sinks.Many<ServerSentEvent<Message>> {
//        return sessionSinks.computeIfAbsent(sessionId) {
//            Sinks.many().multicast().onBackpressureBuffer()
//        }
//    }
//
//    // Endpoint SSE para o cliente escutar o histórico + tempo real
//    @GetMapping("/stream/{sessionId}", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
//    fun streamChat(@PathVariable sessionId: String): Flux<ServerSentEvent<Message>> {
//
//        // 1. Busca o histórico existente do Redis
//        val historyFlux = chatService.getHistory(sessionId)
//            .map { ServerSentEvent.builder(it).event("history").build() }
//
//        // 2. Escuta novos eventos ao vivo daquela sessão
//        val liveFlux = getOrCreateSink(sessionId).asFlux()
//
//        // Concatena o histórico estático com os eventos em tempo real
//        return Flux.concat(historyFlux, liveFlux)
//    }
//
//    // Endpoint para enviar uma nova mensagem (Simula recebimento e resposta prefixada)
//    @PostMapping("/send/{sessionId}")
//    fun sendMessage(
//        @PathVariable sessionId: String,
//        @RequestParam content: String
//    ): Flux<Message> {
//        val userMessage = Message(sender = "USER", content = content)
//        // Exemplo de resposta prefixada automatizada
//        val aiMessage = Message(sender = "AI", content = "Resposta prefixada para: '$content'")
//
//        val sink = getOrCreateSink(sessionId)
//
//        // Salva e transmite a mensagem do usuário
//        val userFlow = chatService.saveMessage(sessionId, userMessage)
//            .doOnSuccess { sink.tryEmitNext(ServerSentEvent.builder(userMessage).event("message").build()) }
//            .map { userMessage }
//
//        // Salva e transmite a resposta da IA
//        val aiFlow = chatService.saveMessage(sessionId, aiMessage)
//            .doOnSuccess { sink.tryEmitNext(ServerSentEvent.builder(aiMessage).event("message").build()) }
//            .map { aiMessage }
//
//        return Flux.concat(userFlow, aiFlow)
//    }
}