package com.carvalho.idempotency.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "idempotency")
data class RestAPIIdempotencyProperties(
    var enabled: Boolean = false,
    var resources: List<Resource> = emptyList(),
    var errorsResponse: ErrorsResponse = ErrorsResponse()
) {

    fun uriMatch(uri: String): Boolean {
        return resources.any { uri.matches(it.uriPattern.toRegex()) }
    }

    fun findResourceBy(path: String, method: String): Resource {
        return resources.first { resource ->
            resource.uriPattern.matches(path.toRegex()) && resource.method == method
        }
    }

    data class Resource(var uriPattern: String, var method: String)

    data class ErrorsResponse(
        var invalidKey: ErrorScenario = ErrorScenario(
            400,
            "application/json",
            """{"message": "Invalid key"}"""
        ),
        var inProgress: ErrorScenario = ErrorScenario(
            409,
            "application/json",
            """{"message": "Operation in progress"}"""
        ),
        var duplicate: ErrorScenario = ErrorScenario(
            409,
            "application/json",
            """{"message": "Operation already performed"}"""
        )
    )

    data class ErrorScenario(var status: Int, var contentType: String, var message: String)
}
