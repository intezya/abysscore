package com.intezya.abysscore.unit.service

import com.intezya.abysscore.event.draftprocess.CharactersRevealEvent
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.match.player.PlayerInfo
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.repository.MatchRepository
import com.intezya.abysscore.service.draft.DraftActionService
import com.intezya.abysscore.service.draft.DraftCharacterRevealService
import com.intezya.abysscore.service.draft.DraftValidationService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class DraftCharacterRevealServiceTest {
    private val matchDraftRepository = mockk<MatchDraftRepository>(relaxed = true)
    private val draftValidationService = mockk<DraftValidationService>()
    private val matchRepository = mockk<MatchRepository>(relaxed = true)
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)
    private val draftActionService = mockk<DraftActionService>()

    private val draftCharacterRevealService = DraftCharacterRevealService(
        matchDraftRepository,
        draftValidationService,
        matchRepository,
        eventPublisher,
        draftActionService,
    )

    val match = mockk<Match>()
    val user = mockk<User>()

    @BeforeEach
    fun setup() {
        clearAllMocks()

        every { match.hasPlayerAlreadyRevealedCharacters(any()) } returns false
        every { match.draft } returns mockk()
        every { draftValidationService.validateMatchStatus(any(), any(), any()) } returns match
        every { draftValidationService.getPlayerInfo(any(), any()) } returns mockk(relaxed = true)
        every { draftValidationService.validateDraftState(any(), any()) } returns Unit
        every { user.id } returns 0L
        every { draftActionService.logDraftAction(any(), any(), any(), any()) } returns mockk()
    }

    @Test
    fun `should throw exception if user not in match`() {
        val characters = listOf<DraftCharacterDTO>()

        every { draftValidationService.validateMatchStatus(any(), any(), any()) } throws
            ResponseStatusException(HttpStatus.BAD_REQUEST)

        assertThrows<ResponseStatusException> {
            draftCharacterRevealService.revealCharacters(user, characters)
        }
    }

    @Test
    fun `should throw exception if user already revealed characters`() {
        val characters = listOf<DraftCharacterDTO>()

        every { match.hasPlayerAlreadyRevealedCharacters(any()) } returns true

        assertThrows<ResponseStatusException> {
            draftCharacterRevealService.revealCharacters(user, characters)
        }
    }

    @Test
    fun `should throw exception if draft not in character reveal state`() {
        val characters = listOf<DraftCharacterDTO>()

        every { match.hasPlayerAlreadyRevealedCharacters(any()) } returns false
        every { draftValidationService.validateDraftState(any(), any()) } throws
            ResponseStatusException(HttpStatus.BAD_REQUEST)

        assertThrows<ResponseStatusException> {
            draftCharacterRevealService.revealCharacters(user, characters)
        }
    }

    @Test
    fun `should publish characters reveal event`() {
        val characters = listOf<DraftCharacterDTO>()
        val playerInfo = mockk<PlayerInfo>()

        every { match.draft } returns mockk(relaxed = true)
        every { draftValidationService.getPlayerInfo(match, user.id) } returns playerInfo
        every { playerInfo.player } returns user
        every { playerInfo.isPlayer1 } returns true
        every { matchDraftRepository.save(any()) } returns mockk()

        draftCharacterRevealService.revealCharacters(user, characters)

        verify {
            eventPublisher.publishEvent(
                match<CharactersRevealEvent> {
                    it.player == user
                    it.characters == characters
                    it.match == match
                },
            )
        }
    }
}
