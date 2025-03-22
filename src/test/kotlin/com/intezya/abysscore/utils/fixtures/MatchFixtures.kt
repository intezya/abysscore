package com.intezya.abysscore.utils.fixtures

import com.intezya.abysscore.model.entity.Match
import com.intezya.abysscore.model.entity.User

object MatchFixtures {
    fun createDefaultMatch(
        id: Long = 0L,
        user1: User = UserFixtures.createDefaultUserWithRandomCreds(),
        user2: User = UserFixtures.createDefaultUserWithRandomCreds(),
    ): Match = Match(
        player1 = user1,
        player2 = user2,
    ).apply {
        this.id = id
    }
}
