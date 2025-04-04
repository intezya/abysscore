package com.intezya.abysscore.service

import com.intezya.abysscore.event.match.invite.InviteAcceptedEvent
import com.intezya.abysscore.event.match.invite.InviteReceivedEvent
import com.intezya.abysscore.event.match.invite.InviteRejectedEvent
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.match.matchmaking.MatchInvite
import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.MatchInviteRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class MatchInviteService(
    private val matchInviteRepository: MatchInviteRepository,
    private val userService: UserService,
    private val matchService: MatchMakingService,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${abysscore.match-invite.active-diff-seconds:}") private val activeDiffSeconds: Long = 15,
) {

    fun create(userId: Long, inviteeUsername: String): MatchInvite {
        val invitee = userService.findUserWithThrow(inviteeUsername)
        val inviter = userService.findUserWithThrow(userId)

        validateInviteRequest(inviter, invitee)
        checkForExistingInvite(userId, invitee.id)
        // TODO: notify invited user

        val invite = matchInviteRepository.save(
            MatchInvite(activeDiffSeconds = activeDiffSeconds).apply {
                this.inviter = inviter
                this.invitee = invitee
            },
        )

        eventPublisher.publishEvent(
            InviteReceivedEvent(
                this,
                inviteId = invite.id,
                invitee = invitee,
                inviter = inviter,
            ),
        )

        return invite
    }

    private fun validateInviteRequest(inviter: User, invitee: User) {
        if (invitee.id == inviter.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot invite yourself")
        }

        if (!invitee.receiveMatchInvites) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "User does not allow receiving match invites") // 403?
        }

        if (invitee.currentMatch != null) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already in a match")
        }
    }

    private fun checkForExistingInvite(inviterId: Long, inviteeId: Long) {
        matchInviteRepository.findActiveByInviterIdAndInviteeId(inviterId, inviteeId).ifPresent {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Match invite already exists")
        }
    }

    fun acceptInvite(userId: Long, inviteId: Long): Match {
        val invite = findInviteForUser(inviteId, userId)
        matchInviteRepository.delete(invite)

        eventPublisher.publishEvent(
            InviteAcceptedEvent(
                this,
                inviteId = inviteId,
                invitee = invite.invitee,
                inviter = invite.inviter,
            ),
        )

        return matchService.createMatchFromInvite(invite.inviter, invite.invitee)
    }

    fun declineInvite(userId: Long, inviteId: Long) {
        val invite = findInviteForUser(inviteId, userId)
        matchInviteRepository.delete(invite)

        eventPublisher.publishEvent(
            InviteRejectedEvent(
                this,
                inviteId = inviteId,
                invitee = invite.invitee,
                inviter = invite.inviter,
            ),
        )
    }

    private fun findInviteForUser(inviteId: Long, userId: Long): MatchInvite =
        matchInviteRepository.findByIdAndInviteeId(inviteId, userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found")
        }
}
