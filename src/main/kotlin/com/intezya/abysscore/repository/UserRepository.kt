package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

private const val FIND_BY_USERNAME = """
    SELECT u FROM User u 
    WHERE
        LOWER(u.username) = LOWER(:username)
"""

private const val UPDATE_HWID_BY_ID = """
    UPDATE User u
    SET
        u.hwid = :hwid
    WHERE
        u.id = :id
"""

@Repository
interface UserRepository : JpaRepository<User, Long> {
    @Query(FIND_BY_USERNAME)
    fun findByUsername(username: String): Optional<User>

    @Modifying
    @Transactional
    @Query(UPDATE_HWID_BY_ID)
    fun updateHwid(id: Long, hwid: String)
}
