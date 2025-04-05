package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.user.BanHistory
import org.springframework.data.jpa.repository.JpaRepository

interface BanHistoryRepository : JpaRepository<BanHistory, Long>
