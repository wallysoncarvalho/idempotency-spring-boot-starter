package com.carvalho.idempotency.existingrequesthandlers

import com.carvalho.idempotency.Entry
import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.util.CacheBodyServletResponse

class InProgressRequest(
    private val properties: RestAPIIdempotencyProperties
) : ExistingRequestStrategy {

    override fun isHandled(
        storedEntry: Entry?,
        newEntry: Entry,
        response: CacheBodyServletResponse,
        throwable: Throwable?
    ): Boolean {
        if (storedEntry == null || storedEntry.hasResponse()) return false

        handleErrorResponse(response, properties.errorsResponse.inProgress)

        return true
    }
}
