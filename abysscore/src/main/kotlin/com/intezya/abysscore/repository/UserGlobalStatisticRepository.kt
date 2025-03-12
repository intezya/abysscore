package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.User
import com.intezya.abysscore.model.entity.UserGlobalStatistic
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserGlobalStatisticRepository : JpaRepository<UserGlobalStatistic, Long> {
}
