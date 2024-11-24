package com.carvalho.idempotency

import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.exceptions.InvalidKeyGenerationException
import com.carvalho.idempotency.util.CacheBodyServletRequest
import com.carvalho.idempotency.util.CacheBodyServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

abstract class IdempotencyKeyGenerator(
    private val properties: RestAPIIdempotencyProperties
) {

    @Throws(InvalidKeyGenerationException::class)
    fun generate(
        request: CacheBodyServletRequest,
        response: CacheBodyServletResponse
    ): Result<String> = runCatching { generateKey(request) }.onFailure {
        val errorScenario = properties.errorsResponse.invalidKey

        (response.response as HttpServletResponse).status = errorScenario.status
        response.response.contentType = errorScenario.contentType
        response.response.outputStream.write(errorScenario.message.toByteArray())
    }

    protected abstract fun generateKey(request: HttpServletRequest): String
}
