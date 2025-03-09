package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.user_item.UserItemDTO
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.security.dto.UserAuthInfoDTO
import com.intezya.abysscore.service.UserItemService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account/inventory")
@SecurityRequirement(name = "bearer-jwt")
class UserItemController(
    private val userItemService: UserItemService,
) {
    //го юзать openapi generator + contract first подход
    @GetMapping
    fun getAll(
        @ParameterObject pageable: Pageable,
        @AuthenticationPrincipal userAuthData: UserAuthInfoDTO,
    ): PagedModel<UserItemDTO> {
        return PagedModel(userItemService.findAllUserItems(userAuthData.id, pageable))
    }

    @GetMapping("/{userId}")
    @RequiresAccessLevel(AccessLevel.VIEW_INVENTORY)
    fun getUserInventory(
        @ParameterObject pageable: Pageable,
        @PathVariable userId: Long,
    ): PagedModel<UserItemDTO> {
        return PagedModel(userItemService.findAllUserItems(userId, pageable))
    }

    @PostMapping("/{username}")
    @RequiresAccessLevel(AccessLevel.GIVE_ITEM)
    fun create(
        @PathVariable username: String,
        @RequestParam("item_id") gameItemId: Long,
        @AuthenticationPrincipal userAuthData: UserAuthInfoDTO,
    ): UserItemDTO {
        return userItemService.issueForPlayerFromAdmin(username, gameItemId, userAuthData.id)
    }
}
