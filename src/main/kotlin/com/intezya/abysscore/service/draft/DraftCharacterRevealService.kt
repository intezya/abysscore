package com.intezya.abysscore.service.draft

import com.intezya.abysscore.enum.DraftActionType
import com.intezya.abysscore.enum.DraftState
import com.intezya.abysscore.enum.MatchStatus
import com.intezya.abysscore.event.draftprocess.CharactersRevealEvent
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.repository.MatchRepository
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
    private val matchRepository: MatchRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val draftActionService: DraftActionService,
) {
    fun revealCharacters(user: User, characters: List<DraftCharacterDTO>): MatchDraft {
        val match = draftValidationService.validateMatchStatus(
            user,
            MatchStatus.PENDING,
            "Match is not in reveal characters stage",
        )
        val draft = match.draft

        if (match.hasPlayerAlreadyRevealedCharacters(user)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already revealed your characters")
        }

        draftValidationService.validateDraftState(draft, expectedState = DraftState.CHARACTER_REVEAL)
        val playerInfo = draftValidationService.getPlayerInfo(match, user.id)

        registerPlayerCharacters(draft, playerInfo, characters)
        draftActionService.logDraftAction(draft, playerInfo.player, DraftActionType.REVEAL_CHARACTERS)

        if (draft.bothPlayersReady()) {
            advanceToDraftingState(match, draft)
        }

        eventPublisher.publishEvent(CharactersRevealEvent(this, match, user, characters))

        return matchDraftRepository.save(draft)
    }

    private fun registerPlayerCharacters(
        draft: MatchDraft,
        playerInfo: PlayerInfo,
        characters: List<DraftCharacterDTO>,
    ) {
        val characterEntities = characters.map { it.toEntity() }

        if (playerInfo.isPlayer1) {
            draft.player1AvailableCharacters.addAll(characterEntities)
            draft.isPlayer1Ready = true
        } else {
            draft.player2AvailableCharacters.addAll(characterEntities)
            draft.isPlayer2Ready = true
        }
    }

    private fun advanceToDraftingState(match: Match, draft: MatchDraft) {
        match.status = MatchStatus.DRAFTING
        matchRepository.save(match)

        draft.currentState = DraftState.DRAFTING
        draft.currentStateStartTime = LocalDateTime.now()
        draft.currentStateDeadline = draft.calculateDeadline()
    }
}
