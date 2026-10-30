package com.arelore.android.prompt.data

import java.security.MessageDigest

/** 与 Web / 服务端约定：传 sha256(明文) 十六进制，禁止明文密码上线。 */
fun sha256Hex(plain: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(plain.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}
