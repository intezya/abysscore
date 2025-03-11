package com.intezya.abysscore.security.service

import com.intezya.abysscore.repository.UserRepository
import com.intezya.abysscore.security.dto.AuthDTO
import com.intezya.abysscore.security.dto.toAuthDTO
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): AuthDTO = userRepository.findByUsername(username).orElseThrow {
        UsernameNotFoundException("User not found")
    }.toAuthDTO()

    // TODO: throws hwid exception
    fun updateHwid(id: Long, hwid: String) {
        userRepository.updateHwid(id, hwid)
    }
}
