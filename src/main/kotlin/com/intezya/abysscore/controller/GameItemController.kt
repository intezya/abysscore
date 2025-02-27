package com.intezya.abysscore.controller

import com.intezya.abysscore.dto.game_item.CreateGameItemRequest
import com.intezya.abysscore.dto.user.UserAuthInfoDTO
import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.service.GameItemService
import com.intezya.abysscore.utils.security.RequiresAccessLevel
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/items")
@SecurityRequirement(name = "bearer-jwt")
class GameItemController(
    private val gameItemService: GameItemService,
) {
    @PostMapping("/create")
    @RequiresAccessLevel(AccessLevel.CREATE_ITEM)
    fun createItem(
        @RequestBody
        @Valid
        request: CreateGameItemRequest,
    ): ResponseEntity<GameItem> {
        return ResponseEntity(
            gameItemService.createGameItem(request),
            HttpStatus.CREATED,
        )
    }

    @GetMapping
    fun getAll(@ParameterObject pageable: Pageable): PagedModel<GameItem> {
        val gameItems: Page<GameItem> = gameItemService.findAll(pageable)
        return PagedModel(gameItems)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): GameItem = gameItemService.findById(id)

    @PutMapping("/{itemId}")
    @RequiresAccessLevel(AccessLevel.UPDATE_ITEM)
    fun updateItem(
        @PathVariable itemId: Long,
        @RequestBody @Valid gameItem: CreateGameItemRequest,
    ): ResponseEntity<GameItem> {
        return ResponseEntity(
            gameItemService.updateItem(itemId, gameItem),
            HttpStatus.OK,
        )
    }

    @DeleteMapping("/{itemId}")
    @RequiresAccessLevel(AccessLevel.DELETE_ITEM)
    fun deleteItem(@PathVariable itemId: Long): ResponseEntity<Unit> {
        gameItemService.deleteItem(itemId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
