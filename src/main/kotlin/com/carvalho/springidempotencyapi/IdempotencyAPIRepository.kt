package com.carvalho.springidempotencyapi

interface IdempotencyAPIRepository {

    fun save(entry: IdempotencyEntry)

    fun update(entry: IdempotencyEntry)

    fun getByKey(key: String): IdempotencyEntry?

    fun remove(id: String)
}
