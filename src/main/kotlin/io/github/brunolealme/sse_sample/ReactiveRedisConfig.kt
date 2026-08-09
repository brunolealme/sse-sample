package io.github.brunolealme.sse_sample

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

@Configuration
class RedisReactiveConfig {

    @Bean
    fun reactiveRedisTemplate(
        factory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, Any> {

        // Instancia o ObjectMapper com suporte nativo a Data Classes do Kotlin
        val mapper: ObjectMapper = jacksonObjectMapper()

        // Configura o serializador JSON mapeado diretamente para sua classe Message
        val jsonSerializer = JacksonJsonRedisSerializer(mapper, Any::class.java)

        // Define chaves como String e valores como JSON
        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>(StringRedisSerializer())
            .value(jsonSerializer)
            .build()

        return ReactiveRedisTemplate(factory, serializationContext)
    }
}