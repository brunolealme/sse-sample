package io.github.brunolealme.sse_sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SseSampleApplication

fun main(args: Array<String>) {
    runApplication<SseSampleApplication>(*args)
}
