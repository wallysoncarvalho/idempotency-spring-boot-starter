package com.carvalho.idempotency

import com.carvalho.idempotency.exceptions.ExistingIdempotencyKeyException

interface EntryRepository {

    @Throws(ExistingIdempotencyKeyException::class)
    fun insertOrThrowIfExists(entry: Entry)

    fun update(entry: Entry)

    fun getByKey(key: String): Entry?

    fun remove(id: String)
}
