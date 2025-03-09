package com.intezya.abysscore.constants

const val AUTH_BASE_PATH = "/auth"
const val REGISTER_PATH = "$AUTH_BASE_PATH/register"
const val LOGIN_PATH = "$AUTH_BASE_PATH/login"
const val INFO_PATH = "$AUTH_BASE_PATH/info"

const val AUTHORIZATION_HEADER = "Authorization"
const val BEARER_PREFIX = "Bearer "

const val TEST_IP = "127.0.0.1"
const val INVALID_TOKEN = """
    eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
    eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.
    SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
"""
