package com.carvalho.idempotency.defaultimplementations

import com.carvalho.idempotency.EntryRepository
import com.carvalho.idempotency.Entry
import com.carvalho.idempotency.exceptions.ExistingIdempotencyKeyException

class LocalEntryRepository: EntryRepository {
    private val storage = mutableMapOf<String, Entry>()

    override fun insertOrThrowIfExists(entry: Entry) {
        synchronized(this) {

            if(storage.containsKey(entry.key)) {
                throw ExistingIdempotencyKeyException("Key ${entry.key} already exists.")
            }

            storage[entry.key] = entry
        }
    }

    override fun update(entry: Entry) {
        synchronized(this) { storage[entry.key] = entry }
    }

    override fun getByKey(key: String): Entry? {
        return storage[key]
    }

    override fun remove(id: String) {
        storage.remove(id)
    }
}
