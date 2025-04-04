package com.intezya.abysscore.utils.fixtures

import com.intezya.abysscore.model.dto.matchprocess.SubmitRoomResultRequest
import com.intezya.abysscore.model.entity.match.Match
import com.intezya.abysscore.model.entity.user.User
import kotlin.random.Random

object MatchFixtures {
    fun createDefaultMatch(
        id: Long = 0L,
        user1: User = UserFixtures.generateDefaultUserWithRandomCreds(),
        user2: User = UserFixtures.generateDefaultUserWithRandomCreds(),
    ): Match = Match(
        player1 = user1,
        player2 = user2,
    ).apply {
        this.id = id
    }

    fun createSubmitResult(roomNumber: Int = Random.nextInt(1, 3), time: Int = Random.nextInt(10, 100)) =
        SubmitRoomResultRequest(
            roomNumber = roomNumber,
            time = time,
        )
}
