package com.intezya.abysscore.initializer

import com.intezya.abysscore.service.storage.MinioStorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class MinioInitializer(private val minioStorageService: MinioStorageService) : CommandLineRunner {
    override fun run(vararg args: String?) {
        minioStorageService.init()
    }
}
