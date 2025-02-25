package com.intezya.abysscore.repository

import com.intezya.abysscore.entity.GameItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GameItemRepository : JpaRepository<GameItem, Long>
