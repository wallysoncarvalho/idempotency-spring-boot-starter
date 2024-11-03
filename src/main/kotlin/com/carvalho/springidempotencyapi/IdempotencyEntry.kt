package com.carvalho.springidempotencyapi

class IdempotencyEntry(
    val key: String,
    val request: ByteArray,
){
    var response: ByteArray? = null

    fun update(responseData: ByteArray) {
        response = responseData
    }
}
