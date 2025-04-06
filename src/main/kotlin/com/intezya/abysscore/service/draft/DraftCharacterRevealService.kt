package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.draftprocess.BothPlayersReadyEvent
import com.intezya.abysscore.event.draftprocess.CharactersRevealEvent
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchDraftRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
@Transactional
class DraftCharacterRevealService(
    private val matchDraftRepository: MatchDraftRepository,
    private val draftValidationService: DraftValidationService,
    private val eventPublisher: ApplicationEventPublisher,
    private val draftActionService: DraftActionService,
) {
    fun revealCharacters(user: User, characters: List<DraftCharacterDTO>): MatchDraft {
        val match = user.currentMatch ?: throw IllegalStateException("User is not in a match")
        val draft = match.draft

        if (match.hasPlayerAlreadyRevealedCharacters(user)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You have already revealed your characters",
            )
        }

        val playerInfo = draftValidationService.getPlayerInfo(match, user.id)

        registerPlayerCharacters(draft, playerInfo, characters)

        eventPublisher.publishEvent(CharactersRevealEvent(this, match, user, characters))

        return matchDraftRepository.save(draft)
    }

    fun readyForDraft(user: User): MatchDraft {
        val match = user.currentMatch ?: throw IllegalStateException("User is not in a match")
        val draft = match.draft

        if (!match.hasPlayerAlreadyRevealedCharacters(user)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You have not revealed your characters")
        }

        if (match.player1 == user) {
            draft.isPlayer1Ready = true
        } else {
            draft.isPlayer2Ready = true
        }

        if (draft.bothPlayersReady()) {
            match.status = MatchStatus.DRAFTING
            match.draft.currentState = DraftState.DRAFTING
            match.draft.currentStateStartTime = LocalDateTime.now()

            eventPublisher.publishEvent(BothPlayersReadyEvent(this, match))
        }

        val matchDraft = matchDraftRepository.save(draft)

        return matchDraft
    }

    private fun registerPlayerCharacters(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characters: List<DraftCharacterDTO>,
    ) {
        val characterEntities = characters.map { it.name }

        if (playerInfo.isPlayer1) {
            draft.player1Characters.addAll(characterEntities)
        } else {
            draft.player2Characters.addAll(characterEntities)
        }
    }
}
