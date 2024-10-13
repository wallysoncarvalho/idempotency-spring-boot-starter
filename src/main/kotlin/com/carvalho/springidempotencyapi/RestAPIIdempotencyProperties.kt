package com.carvalho.springidempotencyapi

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "rest-api-idempotency")
data class RestAPIIdempotencyProperties(
    var enabled: Boolean = false,
    var uriPatters: List<String> = emptyList(),
    var errorScenariosResponse: ErrorScenariosResponse
){

    fun uriMatch(uri: String): Boolean {
        return uriPatters.any { uri.matches(it.toRegex()) }
    }

    fun findUriPattern(uri: String): String? {
        return uriPatters.find { uri.matches(it.toRegex()) }
    }

    data class ErrorScenariosResponse(
        var invalidKey: ErrorScenario = ErrorScenario(),
        var operationInProgress: ErrorScenario = ErrorScenario(),
        var operationAlreadyPerformed: ErrorScenario = ErrorScenario()
    )

    data class ErrorScenario(var status: Int = 400, var message: String = "")
}
