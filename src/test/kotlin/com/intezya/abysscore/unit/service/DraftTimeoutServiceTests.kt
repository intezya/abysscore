package com.intezya.abysscore.unit.service

import com.intezya.abysscore.model.entity.draft.MatchDraft
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchDraftRepository
import com.intezya.abysscore.service.MatchProcessService
import com.intezya.abysscore.service.draft.DraftActionService
import com.intezya.abysscore.service.draft.DraftCompletionService
import com.intezya.abysscore.service.draft.DraftTimeoutService
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.springframework.context.ApplicationEventPublisher

class DraftTimeoutServiceTests {
    private val matchDraftRepository = mockk<MatchDraftRepository>(relaxed = true)
    private val draftActionService = mockk<DraftActionService>()
    private val matchProcessService = mockk<MatchProcessService>()
    private val draftCompletionService = mockk<DraftCompletionService>()
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val draftTimeoutService = DraftTimeoutService(
        matchDraftRepository = matchDraftRepository,
        draftActionService = draftActionService,
        matchProcessService = matchProcessService,
        draftCompletionService = draftCompletionService,
        applicationEventPublisher = applicationEventPublisher,
    )

    private val player1 = mockk<User>(relaxed = true)
    private val player2 = mockk<User>(relaxed = true)
    private val match = mockk<Match>(relaxed = true)
    private val draft = mockk<MatchDraft>(relaxed = true)

    @BeforeEach
    fun setup() {
        clearAllMocks()

        every { player1.id } returns 1L
        every { player2.id } returns 2L

        every { player1.currentMatch } returns match
        every { player2.currentMatch } returns match

        every { match.draft } returns draft
    }

    // TODO: add check statistic call when added
//    @Test
//    fun `must cancel match if timeout`() {
//        every { draft.timeout } returns 1L
//        every { draftActionService.logDraftAction(any(), any(), any(), any()) } returns mockk()
//    }
}
