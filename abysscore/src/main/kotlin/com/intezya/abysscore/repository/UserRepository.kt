package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    fun findByUsername(username: String): Optional<User>

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.hwid = :hwid WHERE u.id = :id")
    fun updateHwid(id: Long, hwid: String): Int
}
