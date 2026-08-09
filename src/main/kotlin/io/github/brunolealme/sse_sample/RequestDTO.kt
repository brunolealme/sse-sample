package io.github.brunolealme.sse_sample

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.UUID

data class RequestDTO(
    @JsonProperty("sessionId")
    val sessionId: String = UUID.randomUUID().toString(),

    @JsonProperty("userId")
    val userId: String,

    @JsonProperty("content")
    val content: String
) : Serializable