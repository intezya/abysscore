package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.Admin
import org.springframework.data.jpa.repository.JpaRepository

interface AdminRepository : JpaRepository<Admin, Long>
