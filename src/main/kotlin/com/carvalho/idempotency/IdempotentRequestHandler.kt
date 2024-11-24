package com.carvalho.idempotency

import com.carvalho.idempotency.existingrequesthandlers.ExistingRequestStrategy
import com.carvalho.idempotency.util.CacheBodyServletRequest
import com.carvalho.idempotency.util.CacheBodyServletResponse

class IdempotentRequestHandler(
    private val keyGenerator: IdempotencyKeyGenerator,
    private val repository: EntryRepository,
    private val existingRequestHandlers: List<ExistingRequestStrategy>
) {

    fun handleIdempotentRequest(
        request: CacheBodyServletRequest,
        response: CacheBodyServletResponse
    ): Result<Entry?> {
        val key = keyGenerator.generate(request, response).getOrElse { return Result.failure(it) }
        val entry = Entry(key = key, requestBody = request.inputStream.readAllBytes())

        return runCatching {
            repository.insertOrThrowIfExists(entry)
            entry
        }.onFailure { exception ->
            existingRequestHandlers.firstOrNull { handler ->
                handler.isHandled(
                    storedEntry = repository.getByKey(entry.key),
                    newEntry = entry,
                    response = response,
                    throwable = exception
                )
            } ?: throw exception
        }
    }
}
