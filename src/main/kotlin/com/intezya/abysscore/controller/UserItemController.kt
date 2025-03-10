package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.useritem.UserItemDTO
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.service.UserItemService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/account/inventory")
@SecurityRequirement(name = "bearer-jwt")
class UserItemController(
    private val userItemService: UserItemService,
) {
//    @GetMapping("")
//    fun getSelfInventory(
//        @ParameterObject pageable: Pageable,
//        @AuthenticationPrincipal userAuthData: UserAuthInfoDTO,
//    ): PagedModel<UserItemDTO> = PagedModel(userItemService.findAllUserItems(userAuthData.id, pageable))

    @GetMapping("/{userId}")
    @RequiresAccessLevel(AccessLevel.VIEW_INVENTORY)
    fun getUserInventory(
        @ParameterObject pageable: Pageable,
        @PathVariable userId: Long,
    ): PagedModel<UserItemDTO> = PagedModel(userItemService.findAllUserItems(userId, pageable))

//    @PostMapping("/{userId}")
//    @RequiresAccessLevel(AccessLevel.GIVE_ITEM)
//    fun create(
//        @PathVariable userId: Long,
//        @RequestParam("item_id") gameItemId: Long,
//        @AuthenticationPrincipal userAuthData: UserAuthInfoDTO,
//    ): UserItemDTO = userItemService.issueForPlayerFromAdmin(userId, gameItemId, userAuthData.id)
}
