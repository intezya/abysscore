package com.intezya.abysscore.utils.fixtures

import com.intezya.abysscore.model.entity.User
import java.util.*

object UserFixtures {
    fun generateDefaultUser(
        id: Long = 0L,
        username: String = "test_user",
        password: String = "test_password",
        hwid: String? = "test_hwid",
    ): User = User(
        username = username,
        password = password,
        hwid = hwid,
    ).apply { this.id = id }

    fun generateDefaultUserWithRandomCreds(id: Long = 0L): User = User(
        username = UUID.randomUUID().toString(),
        password = UUID.randomUUID().toString(),
        hwid = UUID.randomUUID().toString(),
    ).apply { this.id = id }
}
