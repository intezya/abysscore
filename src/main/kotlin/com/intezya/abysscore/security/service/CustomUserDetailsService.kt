package com.intezya.abysscore.security.service

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): User = userRepository.findByUsername(username).orElseThrow {
        UsernameNotFoundException("User not found")
    }

    // TODO: throws hwid exception
    fun updateHwid(id: Long, hwid: String) {
        userRepository.updateHwid(id, hwid)
    }
}
