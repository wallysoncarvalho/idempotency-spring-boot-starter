package com.carvalho.idempotency.existingrequesthandlers

import com.carvalho.idempotency.Entry
import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.util.CacheBodyServletResponse
import jakarta.servlet.http.HttpServletResponse

interface ExistingRequestStrategy {

    fun isHandled(
        storedEntry: Entry?,
        newEntry: Entry,
        response: CacheBodyServletResponse,
        throwable: Throwable? = null
    ): Boolean

    fun handleErrorResponse(
        responseWrapper: CacheBodyServletResponse,
        errorScenario: RestAPIIdempotencyProperties.ErrorScenario
    ) {
        (responseWrapper.response as HttpServletResponse).status = errorScenario.status
        responseWrapper.response.contentType = errorScenario.contentType
        responseWrapper.response.outputStream.write(errorScenario.message.toByteArray())
    }
}
