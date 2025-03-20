package com.intezya.abysscore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AbyssCoreApplication

fun main(args: Array<String>) {
    runApplication<AbyssCoreApplication>(*args)
}
