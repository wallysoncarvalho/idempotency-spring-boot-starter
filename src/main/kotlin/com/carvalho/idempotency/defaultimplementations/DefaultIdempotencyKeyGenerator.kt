package com.carvalho.idempotency.defaultimplementations

import com.carvalho.idempotency.IdempotencyKeyGenerator
import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.exceptions.InvalidKeyGenerationException
import jakarta.servlet.http.HttpServletRequest

class DefaultIdempotencyKeyGenerator(
    private val properties: RestAPIIdempotencyProperties
) : IdempotencyKeyGenerator(properties) {

    override fun generateKey(request: HttpServletRequest): String {
        val idempotencyKey = request.getHeader("Idempotency-Key")
        val resource = properties.findResourceBy(request.requestURI, request.method)

        if (idempotencyKey == null) throw InvalidKeyGenerationException()

        return "$idempotencyKey:${resource.uriPattern}:${resource.method}"
    }
}
