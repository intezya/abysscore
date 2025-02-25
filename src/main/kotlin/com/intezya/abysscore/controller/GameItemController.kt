package com.intezya.abysscore.controller

import com.intezya.abysscore.dto.game_item.CreateGameItemRequest
import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.service.GameItemService
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/items")
class GameItemController(
    private val gameItemService: GameItemService,
) {
    @PostMapping("/create")
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
    fun deleteItem(@PathVariable itemId: Long): ResponseEntity<Unit> {
        gameItemService.deleteItem(itemId)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
