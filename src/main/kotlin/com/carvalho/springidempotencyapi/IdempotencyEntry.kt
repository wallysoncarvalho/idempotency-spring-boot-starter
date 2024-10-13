package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletResponse
import java.time.Instant
import java.util.Base64

class IdempotencyEntry(
    val key: String,
    val input: String,
    val createdAt: Instant = Instant.now()
){
    var output: String? = null

    fun update(responseData: ByteArray) {
        output = Base64.getEncoder().encode(responseData).decodeToString()
    }
}
