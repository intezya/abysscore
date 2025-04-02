package com.intezya.abysscore.security.service

import com.intezya.abysscore.model.entity.user.User
import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.utils.PasswordUtils
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository, private val passwordUtils: PasswordUtils) :
    UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): User = userRepository.findByUsername(username).orElseThrow {
        UsernameNotFoundException("User not found")
    }

    // TODO: throws hwid exception
    fun updateHwid(id: Long, hwid: String) {
        userRepository.updateHwid(id, passwordUtils.hashHwid(hwid))
    }
}
