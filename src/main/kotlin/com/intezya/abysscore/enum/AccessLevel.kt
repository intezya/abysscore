package com.intezya.abysscore.enum

enum class AccessLevel(val value: Int) {
    USER(0),
    VIEW_INVENTORY(2),
    ADMIN(3),
    CREATE_ITEM(13),
    GIVE_ITEM(15),
    UPDATE_ITEM(16),
    ADD_ADMIN(31),
    DELETE_ITEM(47),
    DEV(63)
}
