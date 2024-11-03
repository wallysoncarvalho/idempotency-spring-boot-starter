package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class IdempotencyKeyManager(
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator,
    private val errorResponseHandler: ErrorResponseHandler
) {

    fun generate(request: HttpServletRequest, response: HttpServletResponse) = runCatching {
        idempotencyKeyGenerator.generate(request)
    }.getOrElse {
        if (it is InvalidKeyGenerationParameters) {
            errorResponseHandler.handleInvalidKey(response)
            return null
        }
        throw it
    }
}
