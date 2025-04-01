package com.intezya.abysscore.service.storage

import com.intezya.abysscore.enum.MinioBucket
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.service.UserService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@Service
class AvatarStorageService(private val userService: UserService) {
    private val minioStorageService = MinioStorageService(bucket = MinioBucket.USER_IMAGES)

    fun uploadFile(file: MultipartFile, user: User): String {
        val fileUrl = minioStorageService.uploadFile(file, user.id.toString())
        userService.setAvatarUrl(user, fileUrl)
        return fileUrl
    }

    fun getFile(user: User): InputStream? {
        user.avatarUrl ?: return null
        return minioStorageService.getFile(user.avatarUrl!!)
    }

    fun getFile(fileId: String): InputStream? {
        if (fileId.isBlank()) {
            return null
        }
        return minioStorageService.getFile(fileId)
    }
}
