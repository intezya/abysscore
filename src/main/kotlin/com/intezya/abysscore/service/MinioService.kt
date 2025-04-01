package com.intezya.abysscore.service

import com.intezya.abysscore.enum.MinioBucket
import io.minio.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

private val AWS_BUCKETS = listOf(
    "userimages",
)

// TODO: remove unified work. should be separated (move bucket name to constructor and make custom beans)

@Service
class MinioService(private val minioClient: MinioClient) {
    fun init() {
        AWS_BUCKETS.forEach { bucketName ->
            val bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build(),
            )

            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build(),
                )
            }
        }
    }

    fun uploadFile(bucketName: MinioBucket, file: MultipartFile, objectName: String): String {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName.bucketName)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build(),
        )

        return getFileUrl(bucketName, objectName)
    }

    fun getFile(bucketName: MinioBucket, objectName: String): InputStream = minioClient.getObject(
        GetObjectArgs.builder()
            .bucket(bucketName.bucketName)
            .`object`(objectName)
            .build(),
    )

    private fun getFileUrl(bucketName: MinioBucket, objectName: String): String = minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .bucket(bucketName.bucketName)
            .`object`(objectName)
            .method(io.minio.http.Method.GET)
            .build(),
    )
}
