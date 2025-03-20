package com.intezya.abysscore.service.crud

import com.intezya.abysscore.repository.MatchRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class MatchService(
    private val matchRepository: MatchRepository,
) {
    fun findById(matchId: Long) = matchRepository.findById(matchId).orElseThrow {
        ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found")
    }
}
