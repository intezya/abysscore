package com.intezya.abysscore.utils.providers

import com.intezya.abysscore.model.dto.user.UserAuthRequest
import com.intezya.abysscore.model.entity.User
import io.github.serpro69.kfaker.faker

class RandomProvider {
    companion object {
        private val f = faker {}

        fun constructUser(
            id: Long? = null,
            username: String = f.name.firstName(),
            password: String = f.random.nextUUID(),
            hwid: String = f.random.nextUUID(),
        ): User {
            return User(
                id = id,
                username = username,
                password = password,
                hwid = hwid,
            )
        }

        fun constructAuthRequest(
            username: String = f.name.firstName().take(16),
            password: String = f.random.randomString(length = 20, numericalChars = true) + "1",
            hwid: String = f.random.nextUUID(),
        ): UserAuthRequest {
            return UserAuthRequest(
                username = username,
                password = password,
                hwid = hwid,
            )
        }

        fun constructAuthRequest(
            user: User,
        ): UserAuthRequest {
            return UserAuthRequest(
                username = user.username,
                password = user.password,
                hwid = user.hwid!!,
            )
        }

        fun ipv4(): String = f.internet.iPv4Address()

    }
}
