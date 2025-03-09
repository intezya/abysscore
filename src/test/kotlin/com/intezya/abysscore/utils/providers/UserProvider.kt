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
                Arguments.of("username", "USERNAME"),
                Arguments.of("username", "userNAME"),
                Arguments.of("username", "UsErNaMe"),
                Arguments.of("username", "USERNAME"),
                Arguments.of("userNAME", "USERname"),
                Arguments.of("uSeR_nAmE", "UsEr_NaMe"),
                Arguments.of("User-123", "user-123"),
                Arguments.of("UserName", "userNAME"),
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
