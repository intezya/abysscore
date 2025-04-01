package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.MinioBucket
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.service.MinioService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@RestController
@RequestMapping("/users")
class UserAvatarController(private val minioService: MinioService) {
    @GetMapping("/me/avatar")
    fun getMyAvatar(@AuthenticationPrincipal contextUser: User): InputStream = minioService.getFile(
        bucketName = MinioBucket.USER_IMAGES,
        objectName = contextUser.id.toString(),
    )

    @PostMapping("/me/avatar")
    fun uploadMyAvatar(@AuthenticationPrincipal contextUser: User, file: MultipartFile) {
        minioService.uploadFile(
            bucketName = MinioBucket.USER_IMAGES,
            file = file,
            objectName = contextUser.id.toString(),
        )
    }
}
