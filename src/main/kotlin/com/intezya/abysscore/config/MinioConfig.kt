package com.intezya.abysscore.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinioConfig(
    @Value("\${minio.url}") private val url: String,
    @Value("\${minio.access_key}") private val accessKey: String,
    @Value("\${minio.secret_key}") private val secretKey: String,
) {
    @Bean
    fun configureClient(): MinioClient = MinioClient.builder()
        .endpoint(url)
        .credentials(accessKey, secretKey)
        .build()
}
