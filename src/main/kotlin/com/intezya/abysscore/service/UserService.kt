package com.intezya.abysscore.service

import com.intezya.abysscore.dto.event.UserActionEvent
import com.intezya.abysscore.dto.user.UserAuthRequest
import com.intezya.abysscore.dto.user.UserAuthResponse
import com.intezya.abysscore.enum.UserActionEventType
import com.intezya.abysscore.utils.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class UserService(
    private val passwordUtils: PasswordUtils,
    private val authenticationService: AuthenticationService,
    private val eventPublisher: EventPublisher
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    companion object {
        private const val USER_EVENT_TOPIC = "user-action-events"
    }


    fun registerUser(request: UserAuthRequest, ip: String): UserAuthResponse =
        handleAuthRequest(
            request,
            ip,
            authenticationService::register,
            this::sendRegisterEvent,
        )

    fun loginUser(request: UserAuthRequest, ip: String): UserAuthResponse =
        handleAuthRequest(
            request,
            ip,
            authenticationService::authenticate,
            this::sendLoginEvent,
        )

    private fun handleAuthRequest(
        request: UserAuthRequest,
        ip: String,
        authAction: (UserAuthRequest) -> UserAuthResponse,
        sendEvent: (String, String, String, Boolean) -> Unit,
    ): UserAuthResponse {
        val hashedHwid = passwordUtils.hashHwid(request.hwid)
        return runCatching {
            authAction(request)
        }.onSuccess {
            coroutineScope.launch {
                sendEvent(request.username, ip, hashedHwid, true)
            }
        }.onFailure {
            coroutineScope.launch {
                sendEvent(request.username, ip, hashedHwid, false)
            }
        }.getOrThrow()
    }


    private fun sendRegisterEvent(
        username: String,
        ip: String,
        hwid: String,
        isSuccess: Boolean,
    ) {
        val event = UserActionEvent(
            username = username,
            ip = ip,
            eventType = UserActionEventType.REGISTRATION,
            hwid = hwid,
            isSuccess = isSuccess
        )
        eventPublisher.sendActionEvent(event, event.username, USER_EVENT_TOPIC)
    }

    private fun sendLoginEvent(
        username: String,
        ip: String,
        hwid: String,
        isSuccess: Boolean,
    ) {
        val event = UserActionEvent(
            username = username,
            ip = ip,
            eventType = UserActionEventType.LOGIN,
            hwid = hwid,
            isSuccess = isSuccess
        )
        eventPublisher.sendActionEvent(event, event.username, USER_EVENT_TOPIC)
    }
}
