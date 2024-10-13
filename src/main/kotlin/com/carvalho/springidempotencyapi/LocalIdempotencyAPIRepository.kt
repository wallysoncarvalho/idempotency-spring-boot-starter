package com.carvalho.springidempotencyapi

import org.springframework.stereotype.Component

@Component
class LocalIdempotencyAPIRepository: IdempotencyAPIRepository {
    private val storage = mutableMapOf<String, IdempotencyEntry>()

    override fun save(entry: IdempotencyEntry) {
        synchronized(this) {

            if(storage.containsKey(entry.key)) {
                throw DuplicateStorageKeyException("Key ${entry.key} already exists.")
            }

            storage[entry.key] = entry
        }
    }

    override fun update(entry: IdempotencyEntry) {
        synchronized(this) { storage[entry.key] = entry }
    }

    override fun getByKey(key: String): IdempotencyEntry? {
        return storage[key]?.also { println("") }
    }

    override fun remove(id: String) {
        storage.remove(id)
    }

    class DuplicateStorageKeyException(message: String): RuntimeException(message)
}
