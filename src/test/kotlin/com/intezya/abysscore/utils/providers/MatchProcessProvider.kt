package com.intezya.abysscore.utils.providers

import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

class MatchProcessProvider {
    companion object {
        @JvmStatic
        fun provideInvalidSubmitData(): Stream<Arguments> = Stream.of(
            // room n, timer
            Arguments.of(-1, 1), // Номер комнаты должен быть в пределах от 1 до 3
            Arguments.of(0, 1), // Номер комнаты должен быть в пределах от 1 до 3
            Arguments.of(4, 1), // Номер комнаты должен быть в пределах от 1 до 3
            Arguments.of(1, -1), // Время прохождения не может быть меньше 0
            Arguments.of(2, -1), // Время прохождения не может быть меньше 0
            Arguments.of(3, -1), // Время прохождения не может быть меньше 0
            Arguments.of(
                4,
                -1,
            ), // Номер комнаты должен быть в пределах от 1 до 3 и время прохождения не может быть меньше 0
        )
    }
}
