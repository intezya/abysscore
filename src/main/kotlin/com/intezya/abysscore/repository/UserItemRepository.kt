package com.intezya.abysscore.repository

import com.intezya.abysscore.entity.UserItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserItemRepository : JpaRepository<UserItem, Long>
