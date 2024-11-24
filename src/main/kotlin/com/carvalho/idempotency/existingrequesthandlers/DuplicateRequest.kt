package com.carvalho.idempotency.existingrequesthandlers

import com.carvalho.idempotency.Entry
import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.util.CacheBodyServletResponse

class DuplicateRequest(
    private val properties: RestAPIIdempotencyProperties
) : ExistingRequestStrategy {

    override fun isHandled(
        storedEntry: Entry?,
        newEntry: Entry,
        response: CacheBodyServletResponse,
        throwable: Throwable?
    ): Boolean {
        if (storedEntry == null || storedEntry.hasResponse().not()) return false

        if (storedEntry isDifferentFrom newEntry) {
            handleErrorResponse(response, properties.errorsResponse.duplicate)
            return true
        }

        storedEntry.updateServlet(response)

        return true
    }
}
