package io.github.brunolealme.sse_sample

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class ChatService(
    private val redisTemplate: ReactiveRedisTemplate<String, Any>,
    @Value($$"${app.cache.context-ttl-seconds}") private val contextTtlSeconds: Long
) {
    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    private fun getSessionKey(sessionId: String) = "chat::session:$sessionId"
    private fun getUserSessionsKey(userId: String) = "user::sessions:$userId"
    private fun getContextKey(key: String) = "chat::context:$key"

    // 1. HISTÓRICO POR SESSÃO + ÍNDICE DO USUÁRIO (Ordenado por último acesso)
    fun saveMessageAndIndex(userId: String, sessionId: String, message: Message): Mono<Void> {
        val sessionKey = getSessionKey(sessionId)
        val userSessionsKey = getUserSessionsKey(userId)
        val now = System.currentTimeMillis().toDouble()

        // Salva a mensagem na lista
        val appendMessage = redisTemplate.opsForList().rightPush(sessionKey, message)
            .flatMap { redisTemplate.expire(sessionKey, Duration.ofDays(21)) } // TTL de 21 dias

        // Atualiza o índice do usuário (ZSet: Score = Timestamp atual)
        val updateIndex = redisTemplate.opsForZSet().add(userSessionsKey, sessionId, now)
            .flatMap {
                redisTemplate.expire(
                    userSessionsKey,
                    Duration.ofDays(30)
                )
            } // Índice expira se inativo por 30 dias

        // Executa ambos concorrentemente de forma reativa
        return Mono.zip(appendMessage, updateIndex).then()
    }

    fun getHistory(sessionId: String): Flux<Message> {
        return redisTemplate.opsForList()
            .range(getSessionKey(sessionId), 0, -1)
            .map { it as Message }
    }

    fun getUserSessions(userId: String): Flux<String> {
        // Retorna as sessões ordenadas do maior score (mais recente) para o menor
        return redisTemplate.opsForZSet()
            .reverseRange(getUserSessionsKey(userId), org.springframework.data.domain.Range.unbounded<Long>())
            .map { it as String }
    }

    // 2. CACHE DE CONTEXTO COM TTL CURTO CONFIGURÁVEL
    fun getContext(key: String): Mono<String> {
        return redisTemplate.opsForValue().get(getContextKey(key)).map { it as String }
    }

    fun saveContext(key: String, value: String): Mono<Boolean> {
        return redisTemplate.opsForValue().set(
            getContextKey(key),
            value,
            Duration.ofSeconds(contextTtlSeconds)
        )
    }
}

//    fun getSessionKey(sessionId: String): String = "chat::session:$sessionId"
//
//    fun saveMessage(sessionId: String, message: Message): Mono<Long> {
//        return redisTemplate.opsForList().rightPush(getSessionKey(sessionId), message)
//    }
//
//    fun getHistory(sessionId: String): Flux<Message> {
//        return redisTemplate.opsForList().range(getSessionKey(sessionId), 0, -1)