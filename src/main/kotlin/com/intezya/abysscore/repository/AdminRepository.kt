package com.intezya.abysscore.repository

import com.intezya.abysscore.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<Admin, Long>
