package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletResponse

class ErrorResponseHandler(private val properties: RestAPIIdempotencyProperties) {

    fun handleInvalidKey(response: HttpServletResponse) {
        handleErrorResponse(response, properties.errorScenariosResponse.invalidKey)
    }

    fun handleOperationInProgress(response: HttpServletResponse) {
        handleErrorResponse(response, properties.errorScenariosResponse.operationInProgress)
    }

    fun handleOperationAlreadyPerformed(response: HttpServletResponse) {
        handleErrorResponse(response, properties.errorScenariosResponse.operationAlreadyPerformed)
    }

    private fun handleErrorResponse(
        response: HttpServletResponse,
        errorScenario: RestAPIIdempotencyProperties.ErrorScenario
    ) {
        response.status = errorScenario.status
        response.writer.write(errorScenario.message)
    }
}
