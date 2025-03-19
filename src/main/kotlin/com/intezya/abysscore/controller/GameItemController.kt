package com.intezya.abysscore.controller

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.gameitem.CreateGameItemRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.service.crud.GameItemService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
@SecurityRequirement(name = "bearer-jwt")
class GameItemController(
    private val gameItemService: GameItemService,
) {
    @PostMapping("")
    @RequiresAccessLevel(AccessLevel.CREATE_ITEM)
    fun createItem(
        @RequestBody @Valid request: CreateGameItemRequest,
    ): ResponseEntity<GameItem> = ResponseEntity(
        gameItemService.createGameItem(request),
        HttpStatus.CREATED,
    )

    @GetMapping
    fun getAll(
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
    ): PagedModel<GameItem> {
        val gameItems: Page<GameItem> = gameItemService.findAll(pageable)
        return PagedModel(gameItems)
    }

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: Long,
    ): GameItem = gameItemService.findById(id)

    @PutMapping("/{itemId}")
    @RequiresAccessLevel(AccessLevel.UPDATE_ITEM)
    fun updateItem(
        @PathVariable itemId: Long,
        @RequestBody @Valid gameItem: CreateGameItemRequest,
    ): ResponseEntity<GameItem> = ResponseEntity(
        gameItemService.updateItem(itemId, gameItem),
        HttpStatus.OK,
    )

    @DeleteMapping("/{itemId}")
    @RequiresAccessLevel(AccessLevel.DELETE_ITEM)
    fun deleteItem(
        @PathVariable itemId: Long,
    ): ResponseEntity<Unit> {
        gameItemService.deleteItem(itemId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
