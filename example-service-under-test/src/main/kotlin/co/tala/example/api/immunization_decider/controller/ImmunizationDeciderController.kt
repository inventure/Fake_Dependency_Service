package co.tala.example.api.immunization_decider.controller

import co.tala.api.immunization_decider.constant.X_REQUEST_ID
import co.tala.example.api.immunization_decider.business.service.IDeciderBusinessService
import co.tala.example.api.immunization_decider.controller.model.InitiateRequestDto
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse
import io.swagger.annotations.Api
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    "/immunization-decider/decisions",
    produces = ["application/json"]
)
@Api(value = "Immunization Decider Controller")
class ImmunizationDeciderController(private val service: IDeciderBusinessService) {
    @PostMapping
    fun initiateDecision(
        @RequestHeader(X_REQUEST_ID) requestId: String,
        @Valid @RequestBody request: InitiateRequestDto
    ): ResponseEntity<Unit> {
        val dao = service.initiateRequest(requestId, request)

        // TODO put record in Kafka to process the request instead of spawning async thread
        return ResponseEntity.accepted().build<Unit?>().also {
            GlobalScope.launch {
                service.processRequest(dao.id)
            }
        }
    }

    @GetMapping
    fun getStatus(
        @RequestHeader(X_REQUEST_ID) requestId: String,
        @RequestParam sourceRefId: String
    ): ResponseEntity<ImmunizationDecisionStatusResponse> = ResponseEntity.ok(service.getStatus(sourceRefId))
}
