package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletRequest

interface IdempotencyKeyGenerator {

    fun generate(request: HttpServletRequest): String?
}
