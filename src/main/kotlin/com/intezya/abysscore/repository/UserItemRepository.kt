package com.intezya.abysscore.repository

import com.intezya.abysscore.model.entity.item.UserItem
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserItemRepository : JpaRepository<UserItem, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<UserItem>
}
