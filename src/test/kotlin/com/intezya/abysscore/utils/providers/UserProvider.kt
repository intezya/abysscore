package com.intezya.abysscore.utils.providers

import com.intezya.abysscore.enum.AccessLevel
import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.security.jwt.JwtUtils
import org.junit.jupiter.params.provider.Arguments
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import java.util.stream.Stream

@TestConfiguration
class UserProvider {
    companion object {
        @Autowired
        private lateinit var jwtUtils: JwtUtils

        @JvmStatic
        fun provideInvalidUsername(): Stream<Arguments> =
            Stream.of(
                Arguments.of("qw"), // Слишком короткий
                Arguments.of("thisusernameis22length"), // Слишком длинный
            )

        @JvmStatic
        fun provideInvalidPassword(): Stream<Arguments> =
            Stream.of(
                Arguments.of("7length"), // Слишком короткий, не хватает символов
                Arguments.of("simplepassword"), // Нет заглавных букв, нет цифр
                Arguments.of("Simplepassword"), // Нет цифр
                Arguments.of("SIMPLEPASSWORD"), // Нет строчных букв и цифр
                Arguments.of("sIMPLEPASSWORD"), // Нет заглавных букв и цифр
                Arguments.of("12345678"), // Нет букв, только цифры
                Arguments.of("simplepassword123"), // Нет заглавных букв
                Arguments.of("SIMPLEPASSWORD123"), // Нет строчных букв
                Arguments.of(""), // Пустой пароль
                Arguments.of("short"), // Слишком короткий (меньше 8 символов)
                Arguments.of("password "), // Пробел в пароле
                Arguments.of("17lengthpassword!".repeat(16)), // Слишком длинный (больше 256 символов)
                Arguments.of("password".repeat(33)), // Слишком длинный (больше 256 символов)
                Arguments.of("!@#$$%^&*()12345"), // Содержит специальные символы
                Arguments.of("12345!@#$$$$"), // Содержит специальные символы
                Arguments.of("A B C D E F"), // Пробелы в пароле
            )

        @JvmStatic
        fun provideUsernameWithAnyCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of("username1", "USERNAME1"),
                Arguments.of("username2", "userNAME2"),
                Arguments.of("username3", "UsErNaMe3"),
                Arguments.of("username4", "USERNAME4"),
                Arguments.of("userNAME5", "USERname5"),
                Arguments.of("uSeR_nAmE6", "UsEr_NaMe6"),
                Arguments.of("User-1237", "user-1237"),
                Arguments.of("UserName8", "userNAME8"),
            )

        fun tokenWithAccess(
            accessLevel: AccessLevel,
            user: User = RandomProvider.constructUser(id = 1L),
        ): String =
            jwtUtils.generateJwtToken(
                user = User(),
                accessLevel = accessLevel.value,
            )
    }
}
