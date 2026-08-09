package io.github.brunolealme.sse_sample

import java.io.Serializable

data class Message(val sender: String, val content: String, val timestamp: Long = System.currentTimeMillis()) :
    Serializable {
}