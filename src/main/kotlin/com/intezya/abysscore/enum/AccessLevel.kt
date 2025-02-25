package com.intezya.abysscore.enum

enum class AccessLevel(val value: Int) {
    USER(0),
    ADMIN(3),
    GIVE_ITEM(15),
    ADD_ADMIN(31),
    DEV(63)
}
