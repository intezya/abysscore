package com.intezya.abysscore.utils.providers

import com.intezya.abysscore.model.dto.gameitem.CreateGameItemRequest
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.security.dto.AuthRequest
import io.github.serpro69.kfaker.faker

object RandomProvider {
    private val f = faker {}

    fun constructUser(
        id: Long = 0L,
        username: String = f.name.firstName(),
        password: String = f.random.randomString(length = 20, numericalChars = true) + "1",
        hwid: String? = f.random.nextUUID(),
    ): User = User(
        username = username,
        password = password,
        hwid = hwid,
    ).apply { this.id = id }

    fun constructAuthRequest(
        username: String = f.name.firstName().take(16),
        password: String = f.random.randomString(length = 20, numericalChars = true) + "1",
        hwid: String = f.random.nextUUID(),
    ): AuthRequest = AuthRequest(
        username = username,
        password = password,
        hwid = hwid,
    )

    fun constructCreateGameItemRequest(
        name: String = f.marketing.buzzwords(),
        collection: String = f.marketing.buzzwords(),
        type: Int = f.random.nextInt(0, 2),
        rarity: Int = f.random.nextInt(0, 5),
    ): CreateGameItemRequest = CreateGameItemRequest(
        name = name,
        collection = collection,
        type = type,
        rarity = rarity,
    )
}
