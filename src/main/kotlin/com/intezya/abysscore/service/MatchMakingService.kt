package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.draft.DraftStep
import com.intezya.abysscore.model.entity.DEFAULT_DRAFT_SCHEMA
import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.MatchDraft
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.repository.MatchRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MatchMakingService(
    private val userService: UserService,
    private val matchRepository: MatchRepository,
    private val matchDraftRepository: MatchDraftRepository,
) {
    fun createMatch(user1: User, user2: User): Match {
        val match = Match().apply {
            this.player1 = user1
            this.player2 = user2
        }
        matchRepository.save(match)

        createDraft(match)

        userService.setCurrentMatch(user1, match)
        userService.setCurrentMatch(user2, match)

        // TODO: notify users

        return match
    }

    fun createMatchFromInvite(user1: User, user2: User): Match {
        // TODO: remove users from search

        return createMatch(user1, user2)
    }

    private fun createDraft(match: Match, draftSteps: List<DraftStep>? = null): MatchDraft {
        val draft = MatchDraft().apply {
            this.match = match
            setDraftSteps(draftSteps ?: DEFAULT_DRAFT_SCHEMA)
        }

        return matchDraftRepository.save(draft)
    }
}
