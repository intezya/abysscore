package com.intezya.abysscore.service

import com.intezya.abysscore.dto.user.UserAuthRequest
import com.intezya.abysscore.dto.user.UserAuthResponse
import com.intezya.abysscore.utils.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class UserService(
    private val passwordUtils: PasswordUtils,
    private val userEventService: UserEventService,
    private val authenticationService: AuthenticationService,
) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    fun registerUser(request: UserAuthRequest, ip: String): UserAuthResponse =
        handleAuthRequest(
            request,
            ip,
            authenticationService::register,
            userEventService::sendRegistrationEvent
        )

    fun loginUser(request: UserAuthRequest, ip: String): UserAuthResponse =
        handleAuthRequest(
            request,
            ip,
            authenticationService::authenticate,
            userEventService::sendLoginEvent
        )

    private fun handleAuthRequest(
        request: UserAuthRequest,
        ip: String,
        authAction: (UserAuthRequest) -> UserAuthResponse,
        sendEvent: (String, String, String, Boolean) -> Unit
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
}
