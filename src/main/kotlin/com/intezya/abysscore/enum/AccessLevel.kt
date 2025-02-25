package com.intezya.abysscore.enum

enum class AccessLevel(val value: Int) {
    USER(0),
    ADMIN(3),
    CREATE_ITEM(13),
    GIVE_ITEM(15),
    UPDATE_ITEM(16),
    DELETE_ITEM(17),
    ADD_ADMIN(31),
    DEV(63)
}
