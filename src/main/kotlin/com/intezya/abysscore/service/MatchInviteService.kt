package com.intezya.abysscore.service

import com.intezya.abysscore.model.entity.MatchInvite
import com.intezya.abysscore.repository.MatchInviteRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class MatchInviteService(
    private val matchInviteRepository: MatchInviteRepository,
    private val userService: UserService,
    @Value("\${abysscore.match-invite.active-diff-seconds:}") private val activeDiffSeconds: Long = 15,
) {

    fun create(userId: Long, invitedUserId: Long): MatchInvite {
        matchInviteRepository.findActiveByInviterIdAndInviteeId(
            userId,
            invitedUserId,
        ).ifPresent {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Match invite already exists",
            )
        }

        val invitee = userService.findUserWithThrow(invitedUserId)

        if (!invitee.receiveMatchInvites) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User does not allow receiving match invites",
            )
        }

        val inviter = userService.findUserWithThrow(userId)

        // TODO notify invited user

        return matchInviteRepository.save(
            MatchInvite(
                inviter = inviter,
                invitee = invitee,
                activeDiffSeconds = 15,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun findInvitesWhereUserIsInvitee(userId: Long, pageable: Pageable): Page<MatchInvite> = matchInviteRepository.findActiveByInviterId(userId, pageable)

    fun acceptInvite(userId: Long, inviteId: Long) {
        val invite = matchInviteRepository.findByIdAndInviteeId(inviteId, userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found")
        }

        matchInviteRepository.delete(invite)
        // TODO: notify inviter
        // TODO: create match
        return
    }

    fun declineInvite(userId: Long, inviteId: Long) {
        val invite = matchInviteRepository.findByIdAndInviteeId(inviteId, userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Invite not found")
        }

        matchInviteRepository.delete(invite)
        // TODO: notify inviter
        return
    }
}
