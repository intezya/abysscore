package com.intezya.abysscore.enum

// DON'T UPDATE THIS CODE IF YOU DON'T KNOW WHAT YOU'RE DOING
enum class AccessLevel {
    USER,
    VIEW_ALL_USERS,
    VIEW_INVENTORY,
    VIEW_MATCHES,
    ADMIN,
    USER_BAN,
    CREATE_ITEM,
    GIVE_ITEM,
    UPDATE_ITEM,
    RESET_HWID,
    ADD_ADMIN,
    DELETE_ITEM,
    DEV,
    ;

    val value: Int
        get() = when (this) {
            DEV -> 1000
            else -> ordinal
        }
}
