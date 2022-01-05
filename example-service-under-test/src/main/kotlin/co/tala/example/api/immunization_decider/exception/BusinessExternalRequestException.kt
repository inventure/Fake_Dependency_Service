package co.tala.example.api.immunization_decider.exception

import co.tala.example.http.client.core.ApiResponse

class BusinessExternalRequestException(
    response: ApiResponse<*>,
    sourceRefId: String
) : BusinessException(
    sourceRefId = sourceRefId,
    errorMessage = "Request to ${response.method} ${response.url} failed! sourceRefId: $sourceRefId, statusCode: ${response.statusCode}"
)
