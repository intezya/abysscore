package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.service.UserItemService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/account/inventory")
@SecurityRequirement(name = "bearer-jwt")
class UserItemController(
    private val userItemService: UserItemService,
) {
    @GetMapping("")
    fun getSelfInventory(
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
        @AuthenticationPrincipal authentication: User,
    ): PagedModel<UserItemDTO> = PagedModel(userItemService.findAllUserItems(authentication.id, pageable))

    @GetMapping("/{userId}")
    @RequiresAccessLevel(AccessLevel.VIEW_INVENTORY)
    fun getUserInventory(
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
        @PathVariable userId: Long,
    ): PagedModel<UserItemDTO> = PagedModel(userItemService.findAllUserItems(userId, pageable))

    @PostMapping("/{userId}")
    @RequiresAccessLevel(AccessLevel.GIVE_ITEM)
    fun create(
        @PathVariable userId: Long,
        @RequestParam("item_id") gameItemId: Long,
        @AuthenticationPrincipal authentication: User,
    ): UserItemDTO = userItemService.issueForPlayerFromAdmin(userId, gameItemId, authentication.id)
}
