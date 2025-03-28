package com.intezya.abysscore.service

import com.intezya.abysscore.model.dto.draft.DraftCharacterDTO
import com.intezya.abysscore.model.dto.user.UserDTO
import com.intezya.abysscore.model.message.websocket.draftprocess.CharactersRevealMessage
import com.intezya.abysscore.model.message.websocket.matchinvites.MatchInviteAcceptedMessage
import com.intezya.abysscore.model.message.websocket.matchinvites.MatchInviteReceivedMessage
import com.intezya.abysscore.model.message.websocket.matchinvites.MatchInviteRejectedMessage
import com.intezya.abysscore.model.message.websocket.matchmaking.MatchCreatedMessage
import com.intezya.abysscore.model.message.websocket.useractions.UserLoggedInMessage
import com.intezya.abysscore.model.message.websocket.useractions.UserLoggedOutMessage
import com.intezya.abysscore.service.interfaces.WebsocketMessageBroker
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class WebsocketNotificationService(
    @Qualifier("mainWebSocketMessageService") private val mainWebsocketMessageService: WebsocketMessageBroker<Long>,
//    private val matchWebsocketMessageService: WebsocketMessageBroker<Long>,
//    private val draftWebsocketMessageService: WebsocketMessageBroker<Long>,
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
}
