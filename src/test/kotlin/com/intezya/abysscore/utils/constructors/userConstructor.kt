package com.intezya.abysscore.utils.constructors

import com.intezya.abysscore.model.dto.user.UserAuthRequest
import com.intezya.abysscore.model.dto.user.UserAuthResponse
import com.intezya.abysscore.model.entity.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.bouncycastle.cms.RecipientId.password

fun constructUser(): User {
    return User(
        id = 1,
        username = "test",
        password = "test",
        hwid = "test"
    )
}

fun constructAuthRequest(): UserAuthRequest {
    return UserAuthRequest(
        username="test",
        password="testtest",
        hwid="test",
    )
}
