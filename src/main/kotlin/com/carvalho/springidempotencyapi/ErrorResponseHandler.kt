package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class ErrorResponseHandler(private val properties: RestAPIIdempotencyProperties) {

    fun handleInvalidKey(response: HttpServletResponse) {
        handleErrorResponse(response, properties.errorsResponse.invalidKey)
    }

    fun handleOperationInProgress(response: HttpServletResponse) {
        handleErrorResponse(response, properties.errorsResponse.inProgress)
    }

    fun handleOperationAlreadyPerformed(response: HttpServletResponse) {
        handleErrorResponse(response, properties.errorsResponse.alreadyPerformed)
    }

    private fun handleErrorResponse(
        response: HttpServletResponse,
        errorScenario: RestAPIIdempotencyProperties.ErrorScenario
    ) {
        response.status = errorScenario.status
        response.writer.write(errorScenario.message)
    }
}
