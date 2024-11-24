package com.carvalho.idempotency.existingrequesthandlers

import com.carvalho.idempotency.Entry
import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.exceptions.InvalidKeyGenerationException
import com.carvalho.idempotency.util.CacheBodyServletResponse

class InvalidIdempotencyKey(
    private val properties: RestAPIIdempotencyProperties
) : ExistingRequestStrategy {

    override fun isHandled(
        storedEntry: Entry?,
        newEntry: Entry,
        response: CacheBodyServletResponse,
        throwable: Throwable?
    ): Boolean {
        if (throwable !is InvalidKeyGenerationException) return false

        handleErrorResponse(response, properties.errorsResponse.invalidKey)

        return true
    }
}
