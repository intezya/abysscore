package com.intezya.abysscore.enum

enum class AccessLevel(val value: Int) {
    USER(0),
    VIEW_INVENTORY(1),
    ADMIN(2),
    CREATE_ITEM(3),
    GIVE_ITEM(4),
    UPDATE_ITEM(5),
    ADD_ADMIN(6),
    DELETE_ITEM(7),
    DEV(1000)
}
