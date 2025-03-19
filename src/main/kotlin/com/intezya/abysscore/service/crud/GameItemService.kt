package com.intezya.abysscore.service.crud

import com.intezya.abysscore.model.dto.gameitem.CreateGameItemRequest
import com.intezya.abysscore.model.entity.GameItem
import com.intezya.abysscore.repository.GameItemRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class GameItemService(
    private val gameItemRepository: GameItemRepository,
) {
    fun createGameItem(request: CreateGameItemRequest): GameItem = gameItemRepository.save(request.toEntity())

    @Transactional(readOnly = true)
    fun findById(itemId: Long): GameItem = gameItemRepository.findById(itemId)
        .orElseThrow { createItemNotFoundException() }

    @Transactional(readOnly = true)
    fun findAll(pageable: Pageable): Page<GameItem> = gameItemRepository.findAll(pageable)

    fun updateItem(itemId: Long, request: CreateGameItemRequest): GameItem {
        if (!gameItemRepository.existsById(itemId)) {
            throw createItemNotFoundException()
        }

        return gameItemRepository.save(request.toEntity().apply { id = itemId })
    }

    fun deleteItem(itemId: Long) {
        if (!gameItemRepository.existsById(itemId)) {
            throw createItemNotFoundException()
        }
        gameItemRepository.deleteById(itemId)
    }

    private fun createItemNotFoundException() = ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
}
