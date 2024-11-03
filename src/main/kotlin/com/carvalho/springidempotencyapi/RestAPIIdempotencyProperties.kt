package com.carvalho.springidempotencyapi

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "rest-api-idempotency")
data class RestAPIIdempotencyProperties(
    var enabled: Boolean = false,
    var uriPatters: List<String> = emptyList(),
    var errorsResponse: ErrorsResponse = ErrorsResponse()
) {

    fun uriMatch(uri: String): Boolean {
        return uriPatters.any { uri.matches(it.toRegex()) }
    }

    fun findUriPattern(uri: String): String? {
        return uriPatters.find { uri.matches(it.toRegex()) }
    }

    data class ErrorsResponse(
        var invalidKey: ErrorScenario = ErrorScenario(400, "Invalid key"),
        var inProgress: ErrorScenario = ErrorScenario(409, "Operation in progress"),
        var alreadyPerformed: ErrorScenario = ErrorScenario(409, "Operation already performed")
    )

    data class ErrorScenario(var status: Int, var message: String)
}
