package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.user.UserGlobalStatistic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserGlobalStatisticRepository : JpaRepository<UserGlobalStatistic, Long>
