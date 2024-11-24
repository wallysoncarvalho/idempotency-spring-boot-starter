package com.carvalho.idempotency

import com.carvalho.idempotency.util.CacheBodyServletResponse
import org.springframework.http.HttpStatusCode

class IdempotentResponseHandler(private val repository: EntryRepository) {

    fun handle(
        entry: Entry,
        response: CacheBodyServletResponse,
        controllerResult: Result<Unit>
    ) {
        controllerResult.onFailure { repository.remove(entry.key) }.getOrThrow()

        if (!HttpStatusCode.valueOf(response.status).is2xxSuccessful) {
            repository.remove(entry.key)
            return
        }

        entry.updateWith(response)

        repository.update(entry)
    }
}
