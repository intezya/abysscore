package com.intezya.abysscore.enum

enum class AccessLevel {
    USER,
    VIEW_ALL_USERS,
    VIEW_INVENTORY,
    VIEW_MATCHES,
    ADMIN,
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
