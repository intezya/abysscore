package com.intezya.abysscore.initializer

import com.intezya.abysscore.service.MinioService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class MinioInitializer(private val minioService: MinioService) : CommandLineRunner {
    override fun run(vararg args: String?) {
        minioService.init()
    }
}
