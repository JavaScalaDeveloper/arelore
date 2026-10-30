package com.arelore.android.prompt.data

data class AuthUserInfo(
    val id: String? = null,
    val username: String? = null,
    val nickname: String? = null,
    val avatar: String? = null
)

data class AuthLoginData(
    val token: String? = null,
    val user: AuthUserInfo? = null
)
