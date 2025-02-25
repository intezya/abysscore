package com.intezya.abysscore.controller

import com.intezya.abysscore.dto.user.UserAuthInfoDTO
import com.intezya.abysscore.dto.user_item.UserItemDTO
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.service.UserItemService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/account/inventory")
@SecurityRequirement(name = "bearer-jwt")
class UserItemController(
    private val userItemService: UserItemService,
) {
    @GetMapping
    fun getAll(@ParameterObject pageable: Pageable): PagedModel<UserItemDTO> {
        val userAuthData = SecurityContextHolder.getContext().authentication.principal as UserAuthInfoDTO
        return PagedModel(userItemService.findAllUserItems(userAuthData.id, pageable))
    }

    @PostMapping("/{username}")
    fun create(
        @PathVariable username: String,
        @RequestParam("item_id") gameItemId: Long,
    ): UserItemDTO {
        val userAuthData = SecurityContextHolder.getContext().authentication.principal as UserAuthInfoDTO
        if (userAuthData.accessLevel < AccessLevel.GIVE_ITEM.value) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not enough access level")
        }
        return userItemService.issueForPlayerFromAdmin(username, gameItemId, userAuthData.id)
    }
}
