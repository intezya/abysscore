package com.intezya.abysscore

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.intezya.abysscore.entity.GameItem
import com.intezya.abysscore.repository.GameItemRepository
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.util.*

@RestController
@RequestMapping("/rest/admin-ui/gameItems")
class GameItemResource(private val gameItemRepository: GameItemRepository, private val objectMapper: ObjectMapper) {
    @GetMapping
    fun getAll(@ParameterObject pageable: Pageable): PagedModel<GameItem> {
        val gameItems: Page<GameItem> = gameItemRepository.findAll(pageable)
        return PagedModel(gameItems)
    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long): GameItem {
        val gameItemOptional: Optional<GameItem> = gameItemRepository.findById(id)
        return gameItemOptional.orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
        }
    }

    @GetMapping("/by-ids")
    fun getMany(@RequestParam ids: List<Long>): List<GameItem> {
        return gameItemRepository.findAllById(ids)
    }

    @PostMapping
    fun create(@RequestBody gameItem: GameItem): GameItem {
        return gameItemRepository.save(gameItem)
    }

    @PatchMapping("/{id}")
    @Throws(IOException::class)
    fun patch(@PathVariable id: Long, @RequestBody patchNode: JsonNode): GameItem {
        val gameItem: GameItem = gameItemRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
        }
        objectMapper.readerForUpdating(gameItem).readValue<GameItem>(patchNode)
        return gameItemRepository.save(gameItem)
    }

    @PatchMapping
    @Throws(IOException::class)
    fun patchMany(@RequestParam ids: List<Long>, @RequestBody patchNode: JsonNode): List<Long> {
        val gameItems: Collection<GameItem> = gameItemRepository.findAllById(ids)
        for (gameItem in gameItems) {
            objectMapper.readerForUpdating(gameItem).readValue<GameItem>(patchNode)
        }
        val resultGameItems: List<GameItem> = gameItemRepository.saveAll(gameItems)
        return resultGameItems.mapNotNull(GameItem::id)
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): GameItem? {
        val gameItem: GameItem? = gameItemRepository.findById(id).orElse(null)
        if (gameItem != null) {
            gameItemRepository.delete(gameItem)
        }
        return gameItem
    }

    @DeleteMapping
    fun deleteMany(@RequestParam ids: List<Long>) {
        gameItemRepository.deleteAllById(ids)
    }
}
