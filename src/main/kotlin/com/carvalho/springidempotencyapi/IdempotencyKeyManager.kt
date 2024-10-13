package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class IdempotencyKeyManager(
    private val idempotencyKeyGenerator: IdempotencyKeyGenerator,
    private val errorResponseHandler: ErrorResponseHandler
) {

    fun generate(request: HttpServletRequest, response: HttpServletResponse): String? {
        return runCatching { idempotencyKeyGenerator.generate(request) }.getOrElse {
            if (it is InvalidKeyGenerationParameters) {
                errorResponseHandler.handleInvalidKey(response)
                return null
            }
            throw it
        }
    }
}
