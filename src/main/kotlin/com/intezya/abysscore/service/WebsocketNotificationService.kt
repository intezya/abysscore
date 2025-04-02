package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.draft.DraftActionDTO
import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.message.websocket.draft.process.CharactersRevealMessage
import com.intezya.abysscore.model.message.websocket.draft.process.DraftActionPerformMessage
import com.intezya.abysscore.model.message.websocket.draft.process.DraftEndMessage
import com.intezya.abysscore.model.message.websocket.match.invite.MatchInviteAcceptedMessage
import com.intezya.abysscore.model.message.websocket.match.invite.MatchInviteReceivedMessage
import com.intezya.abysscore.model.message.websocket.match.invite.MatchInviteRejectedMessage
import com.intezya.abysscore.model.message.websocket.match.process.MatchTimeoutMessage
import com.intezya.abysscore.model.message.websocket.matchmaking.MatchCreatedMessage
import com.intezya.abysscore.model.message.websocket.user.action.UserLoggedInMessage
import com.intezya.abysscore.model.message.websocket.user.action.UserLoggedOutMessage
import com.intezya.abysscore.service.interfaces.WebsocketMessageBroker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class WebsocketNotificationService(
    @Qualifier("mainWebSocketMessageService") private val mainWebsocketMessageService: WebsocketMessageBroker<Long>,
//    private val matchWebsocketMessageService: WebsocketMessageBroker<Long>,
    @Qualifier("draftWebSocketMessageService") private val draftWebsocketMessageService: WebsocketMessageBroker<Long>,
) {

    fun userLoggedIn(userId: Long, username: String) {
        val message = UserLoggedInMessage(
            username = username,
            currentOnline = mainWebsocketMessageService.getOnline(),
        )

        mainWebsocketMessageService.broadcast(message, except = listOf(userId))
    }

    fun userLoggedOut(userId: Long, username: String) {
        val message = UserLoggedOutMessage(
            username = username,
            currentOnline = mainWebsocketMessageService.getOnline(),
        )

        mainWebsocketMessageService.broadcast(message, except = listOf(userId))
    }

    fun inviteReceived(userId: Long, inviteId: Long, inviterUsername: String) {
        val message = MatchInviteReceivedMessage(
            inviteId = inviteId,
            inviterUsername = inviterUsername,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }

    fun inviteAccepted(userId: Long, inviteId: Long, inviteeUsername: String) {
        val message = MatchInviteAcceptedMessage(
            inviteId = inviteId,
            inviteeUsername = inviteeUsername,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }

    fun inviteRejected(userId: Long, inviteId: Long, inviteeUsername: String) {
        val message = MatchInviteRejectedMessage(
            inviteId = inviteId,
            inviteeUsername = inviteeUsername,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }

    fun matchCreated(userId: Long, matchId: Long, opponent: UserDTO) {
        val message = MatchCreatedMessage(
            matchId = matchId,
            opponent = opponent,
        )

        mainWebsocketMessageService.sendToUser(userId, message)
    }

    fun charactersRevealed(opponentId: Long, characters: List<DraftCharacterDTO>) {
        val message = CharactersRevealMessage(characters = characters)

        mainWebsocketMessageService.sendToUser(opponentId, message)
    }

    fun draftActionPerform(opponentId: Long, draftAction: DraftActionDTO) {
        val message = DraftActionPerformMessage(action = draftAction)

        draftWebsocketMessageService.sendToUser(opponentId, message)
    }

    fun automaticDraftActionPerform(playerId: Long, draftAction: DraftActionDTO) {
        val message = DraftActionPerformMessage(action = draftAction)

        draftWebsocketMessageService.sendToUser(playerId, message)
    }

    fun sendTimeoutDefeat(playerId: Long, matchId: Long) {
        val message = MatchTimeoutMessage(
            matchId = matchId,
            result = "defeat",
            reason = "Timeout exceeded",
        )

        mainWebsocketMessageService.sendToUser(playerId, message)
    }

    fun sendTimeoutVictory(playerId: Long, matchId: Long) {
        val message = MatchTimeoutMessage(
            matchId = matchId,
            result = "victory",
            reason = "Opponent timeout",
        )

        mainWebsocketMessageService.sendToUser(playerId, message)
    }

    fun sendTimeoutDraw(player1Id: Long, player2Id: Long, matchId: Long) {
        val message = MatchTimeoutMessage(
            matchId = matchId,
            result = "draw",
            reason = "Both players timeout",
        )

        mainWebsocketMessageService.sendToUser(player1Id, message)
        mainWebsocketMessageService.sendToUser(player2Id, message)
    }

    fun draftEnd(
        player1Id: Long,
        player2Id: Long,
        player1Characters: List<DraftCharacterDTO>,
        player2Characters: List<DraftCharacterDTO>,
    ) {
        val message = DraftEndMessage(
            player1Characters = player1Characters,
            player2Characters = player2Characters,
        )

        draftWebsocketMessageService.sendToUser(player1Id, message)
        draftWebsocketMessageService.sendToUser(player2Id, message)
        mainWebsocketMessageService.sendToUser(player1Id, message)
        mainWebsocketMessageService.sendToUser(player2Id, message)

    }
}
