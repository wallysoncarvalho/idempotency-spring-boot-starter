package com.carvalho.idempotency.exceptions

class InvalidKeyGenerationException(
    override val message: String = "Invalid idempotency key generation."
) : RuntimeException(message)
