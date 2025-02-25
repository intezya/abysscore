package com.intezya.abysscore.repository

import com.intezya.abysscore.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AdminRepository : JpaRepository<Admin, Long> {
    fun findByUserId(userId: Long): Optional<Admin>
}
