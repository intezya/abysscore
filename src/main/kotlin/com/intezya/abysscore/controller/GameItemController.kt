package com.intezya.abysscore.controller

import com.intezya.abysscore.dto.game_item.CreateGameItemRequest
import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.service.GameItemService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    fun getAllItems(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id") sortBy: String,
        @RequestParam(defaultValue = "ASC") direction: String
    ): ResponseEntity<Page<GameItem>> { // todo: add pagination
        val sort = Sort.by(Sort.Direction.fromString(direction), sortBy)
        val pageable = PageRequest.of(page, size, sort)
        return ResponseEntity(
            gameItemService.getAllItems(pageable),
            HttpStatus.OK,
        )
    }

    @GetMapping("/{itemId}")
    fun getItem(@PathVariable itemId: Long): ResponseEntity<GameItem> {
        return ResponseEntity(
            gameItemService.getItem(itemId),
            HttpStatus.OK,
        )
    }

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
