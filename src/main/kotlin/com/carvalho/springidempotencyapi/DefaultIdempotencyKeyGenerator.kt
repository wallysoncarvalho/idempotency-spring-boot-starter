package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

@Component
class DefaultIdempotencyKeyGenerator(
    private val properties: RestAPIIdempotencyProperties
) : IdempotencyKeyGenerator {

    override fun generate(request: HttpServletRequest): String {
        val idempotencyKey = request.getHeader("Idempotency-Key")
        val clientId = request.getHeader("Client-Id")
        val uriPattern = properties.findUriPattern(request.requestURI) ?: ""

        if (idempotencyKey == null || clientId == null) {
            throw InvalidKeyGenerationParameters("Idempotency-Key or Client-Id header not found.")
        }

        return uriPattern + clientId + idempotencyKey
    }
}
