package com.intezya.abysscore.controller.crud

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.dto.match.MatchDTO
import com.intezya.abysscore.model.dto.match.toDTO
import com.intezya.abysscore.security.annotations.RequiresAccessLevel
import com.intezya.abysscore.service.crud.MatchService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/matches")
class MatchController(private val matchService: MatchService) {
    @GetMapping("/{matchId}")
    @RequiresAccessLevel(AccessLevel.VIEW_MATCHES)
    fun findMatch(@PathVariable("matchId") matchId: Long): MatchDTO = matchService.findById(matchId).toDTO()
}
