package io.github.brunolealme.sse_sample

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ChatResilienceHandler(private val chatService: ChatService) {
    private val logger = LoggerFactory.getLogger(ChatResilienceHandler::class.java)

    fun saveMessageFailOpen(userId: String, sessionId: String, message: Message): Mono<Void> {
        return chatService.saveMessageAndIndex(userId, sessionId, message)
            .onErrorResume { e ->
                logger.warn("⚠️ REDIS INDISPONÍVEL (Fail-Open ativo) ao salvar mensagem para a sessão $sessionId. Erro: ${e.message}")
                Mono.empty() // Permite que a aplicação continue sem estourar erro para o usuário
            }
    }

    fun getHistoryFailOpen(sessionId: String): Flux<Message> {
        return chatService.getHistory(sessionId)
            .onErrorResume { e ->
                logger.warn("⚠️ REDIS INDISPONÍVEL (Fail-Open ativo) ao buscar histórico da sessão $sessionId.")
                Flux.empty() // Retorna histórico vazio, mas não derruba a conexão SSE
            }
    }

    fun getContextFailOpen(key: String): Mono<String> {
        return chatService.getContext(key)
            .onErrorResume { e ->
                logger.warn("⚠️ REDIS INDISPONÍVEL (Fail-Open ativo) ao buscar cache de contexto: $key.")
                Mono.empty() // Se falhar, assume "miss" no cache e segue para reprocessar
            }
    }
}
