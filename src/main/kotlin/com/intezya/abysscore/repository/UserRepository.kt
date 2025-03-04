package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = :username")
    fun findByUsername(username: String): Optional<User>
}
