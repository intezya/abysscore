package com.intezya.abysscore.utils.containers

import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.GenericContainer

@TestConfiguration
class TestMinioConfiguration {
    @Bean
    fun minioClient(): MinioClient {
        val minioContainer =
            GenericContainer("minio/minio:latest")
                .withExposedPorts(9000, 9001)
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
                .withCommand("server", "/data", "--console-address", ":9001")

        minioContainer.start()

        val endpoint = "http://${minioContainer.host}:${minioContainer.getMappedPort(9000)}"

        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials("minioadmin", "minioadmin")
            .build()
    }

    @Bean
    fun createBucketIfNotExists(minioClient: MinioClient): Boolean {
        val bucketName = "testbucket"
        val bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())

        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }

        return true
    }
}
