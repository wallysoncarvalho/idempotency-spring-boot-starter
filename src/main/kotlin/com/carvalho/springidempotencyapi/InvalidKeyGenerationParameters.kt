package com.carvalho.springidempotencyapi

class InvalidKeyGenerationParameters(override val message: String?) : RuntimeException(message)
