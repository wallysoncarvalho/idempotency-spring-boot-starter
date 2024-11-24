package com.carvalho.idempotency.exceptions

class ExistingIdempotencyKeyException(message: String): RuntimeException(message)
