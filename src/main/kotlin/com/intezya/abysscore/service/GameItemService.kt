package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.game_item.CreateGameItemRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.repository.GameItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class GameItemService(
    private val gameItemRepository: GameItemRepository,
) {
    fun createGameItem(createGameItemRequest: CreateGameItemRequest): GameItem {
        return gameItemRepository.save(createGameItemRequest.toEntity())
    }

    fun findById(itemId: Long): GameItem {
        return gameItemRepository.findById(itemId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found with id: $itemId")
        }
    }

    fun findAll(pageable: Pageable): Page<GameItem> {
        return gameItemRepository.findAll(pageable)
    }

    fun updateItem(itemId: Long, createGameItemRequest: CreateGameItemRequest): GameItem {
        if (!gameItemRepository.existsById(itemId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot update. Item not found with id: $itemId")
        }

        val gameItem = createGameItemRequest.toEntity()
        gameItem.id = itemId
        return gameItemRepository.save(gameItem)
    }

    fun deleteItem(itemId: Long) {
        if (!gameItemRepository.existsById(itemId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Cannot delete. Item not found with id: $itemId")
        }
        gameItemRepository.deleteById(itemId)
    }
}
