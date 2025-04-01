package com.intezya.abysscore.controller

import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.service.storage.AvatarStorageService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

@RestController
@RequestMapping("/users")
class UserAvatarController(private val avatarStorageService: AvatarStorageService) {
    @GetMapping("/me/avatar")
    fun getMyAvatar(@AuthenticationPrincipal contextUser: User): ResponseEntity<InputStream> {
        val result = avatarStorageService.getFile(contextUser)
        result ?: return ResponseEntity.noContent().build()
        return ResponseEntity.ok(result)
    }

    @PostMapping("/me/avatar")
    fun uploadMyAvatar(@AuthenticationPrincipal contextUser: User, file: MultipartFile): ResponseEntity<String> =
        ResponseEntity.ok(
            avatarStorageService.uploadFile(
                file = file,
                user = contextUser,
            ),
        )

    @GetMapping("/avatars/{id}/")
    fun getAvatarById(@PathVariable id: String): InputStream? = avatarStorageService.getFile(id)
}
