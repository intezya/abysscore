package com.intezya.abysscore.service.storage

import com.intezya.abysscore.enum.MinioBucket
import io.minio.*
import io.minio.http.Method
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
class MinioStorageService(private val bucket: MinioBucket = MinioBucket.NULL) {
    @Autowired
    private lateinit var minioClient: MinioClient

    fun init() {
        val bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucket.bucketName).build(),
        )

        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucket.bucketName).build(),
            )
        }
    }

    fun uploadFile(file: MultipartFile, objectName: String): String {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucket.bucketName)
                .`object`(objectName)
                .stream(file.inputStream, file.size, -1)
                .contentType(file.contentType)
                .build(),
        )

        return getFileUrl(objectName)
    }

    fun getFile(objectName: String): InputStream = minioClient.getObject(
        GetObjectArgs.builder()
            .bucket(bucket.bucketName)
            .`object`(objectName)
            .build(),
    )

    private fun getFileUrl(objectName: String): String = minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .bucket(bucket.bucketName)
            .`object`(objectName)
            .method(Method.GET)
            .build(),
    )
}
