package com.intezya.abysscore.service

import com.intezya.abysscore.dto.admin.AdminAuthRequest
import com.intezya.abysscore.dto.admin.AdminAuthResponse
import com.intezya.abysscore.dto.event.UserActionEvent
import com.intezya.abysscore.dto.user.UserAuthRequest
import com.intezya.abysscore.dto.user.UserAuthResponse
import com.intezya.abysscore.entity.User
import com.intezya.abysscore.enum.UserActionEventType
import com.intezya.abysscore.repository.AdminRepository
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.utils.AuthUtils
import com.intezya.abysscore.utils.PasswordUtils
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val passwordUtils: PasswordUtils,
    private val authUtils: AuthUtils,
    private val adminRepository: AdminRepository,
    private val eventPublisher: EventPublisher,
) {
    companion object {
        private const val USER_EVENT_TOPIC = "user-action-events"
        private const val ADMIN_EXTRA_EXPIRATION_MS = 3_600_000 // 1 hour
    }

    fun registerUser(request: UserAuthRequest, ip: String): UserAuthResponse =
        handleAuthRequest(
            request = request,
            ip = ip,
            authAction = ::register,
            eventType = UserActionEventType.REGISTRATION
        )

    fun loginUser(request: UserAuthRequest, ip: String): UserAuthResponse =
        handleAuthRequest(
            request = request,
            ip = ip,
            authAction = ::authenticate,
            eventType = UserActionEventType.LOGIN
        )

    private fun handleAuthRequest(
        request: UserAuthRequest,
        ip: String,
        authAction: (UserAuthRequest) -> UserAuthResponse,
        eventType: UserActionEventType
    ): UserAuthResponse {
        val hashedHwid = passwordUtils.hashHwid(request.hwid)
        return runCatching {
            authAction(request)
        }.onSuccess {
            publishUserActionEvent(request.username, ip, hashedHwid, eventType, true)
        }.onFailure {
            publishUserActionEvent(request.username, ip, hashedHwid, eventType, false)
        }.getOrThrow()
    }

    private fun publishUserActionEvent(
        username: String,
        ip: String,
        hwid: String,
        eventType: UserActionEventType,
        isSuccess: Boolean
    ) {
        val event = UserActionEvent(
            username = username,
            ip = ip,
            eventType = eventType,
            hwid = hwid,
            isSuccess = isSuccess
        )
        eventPublisher.sendActionEvent(event, event.username, USER_EVENT_TOPIC)
    }

    private fun register(request: UserAuthRequest): UserAuthResponse {
        return try {
            val user = User(
                username = request.username,
                password = passwordUtils.hashPassword(request.password),
                hwid = passwordUtils.hashHwid(request.hwid),
            )
            userRepository.save(user)
            UserAuthResponse(token = authUtils.generateJwtToken(user))
        } catch (ex: Exception) {
            when {
                ex.message?.contains("uc_users_username") == true ->
                    throw ResponseStatusException(HttpStatus.CONFLICT, "User already exists")

                ex.message?.contains("uc_users_hwid") == true ->
                    throw ResponseStatusException(HttpStatus.CONFLICT, "Only 1 account allowed per device")

                else -> throw ex
            }
        }
    }

    private fun authenticate(request: UserAuthRequest): UserAuthResponse {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

        if (!passwordUtils.verifyPassword(request.password, user.password)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password")
        }

        if (user.hwid != null && !passwordUtils.verifyHwid(request.hwid, user.hwid)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid hardware ID")
        }

        return UserAuthResponse(token = authUtils.generateJwtToken(user))
    }

    fun adminLogin(request: AdminAuthRequest, ip: String): AdminAuthResponse {
        val hashedHwid = passwordUtils.hashHwid(request.hwid)

        return runCatching {
            val user = userRepository.findByUsername(request.username)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "User not found") }

            val admin = adminRepository.findById(user.id!!)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found") }

            AdminAuthResponse(
                token = authUtils.generateJwtToken(
                    user = user,
                    accessLevel = admin.accessLevel.value,
                    extraExpirationMs = ADMIN_EXTRA_EXPIRATION_MS
                )
            )
        }.onSuccess {
            publishUserActionEvent(request.username, ip, hashedHwid, UserActionEventType.ADMIN_LOGIN, true)
        }.onFailure {
            publishUserActionEvent(request.username, ip, hashedHwid, UserActionEventType.ADMIN_LOGIN, false)
        }.getOrThrow()
    }
}
