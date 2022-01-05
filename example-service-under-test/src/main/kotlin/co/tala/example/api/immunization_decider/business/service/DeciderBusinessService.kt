package co.tala.example.api.immunization_decider.business.service

import co.tala.example.api.immunization_decider.business.resolver.IImmunizationResolver
import co.tala.example.api.immunization_decider.controller.model.InitiateRequestDto
import co.tala.example.api.immunization_decider.exception.BusinessException
import co.tala.example.api.immunization_decider.exception.BusinessExternalRequestException
import co.tala.example.api.immunization_decider.exception.RequestNotFoundException
import co.tala.example.api.immunization_decider.repository.DecisionRequestRepository
import co.tala.example.api.immunization_decider.repository.DecisionResultRepository
import co.tala.example.api.immunization_decider.repository.model.DecisionRequestDao
import co.tala.example.api.immunization_decider.repository.model.DecisionResultDao
import co.tala.example.http.client.core.ApiResponse
import co.tala.example.http.client.lib.factory.IClientFactory
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatus
import co.tala.example.http.client.lib.service.immunization_decider.model.ImmunizationDecisionStatusResponse
import co.tala.example.http.client.lib.service.immunization_history.IImmunizationHistoryClient
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import co.tala.example.http.client.lib.service.pharmacy.IPharmacyClient
import co.tala.example.http.client.lib.service.user.IUserClient
import co.tala.example.http.client.lib.service.user.model.UserResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.time.Instant

interface IDeciderBusinessService {
    fun initiateRequest(requestId: String, request: InitiateRequestDto): DecisionRequestDao
    fun processRequest(decisionRequestId: Long): Unit?
    fun getStatus(sourceRefId: String): ImmunizationDecisionStatusResponse
}

@Component
class DeciderBusinessService(
    private val userClientFactory: IClientFactory<IUserClient>,
    private val immunizationHistoryClientFactory: IClientFactory<IImmunizationHistoryClient>,
    private val pharmacyClientFactory: IClientFactory<IPharmacyClient>,
    private val requestRepository: DecisionRequestRepository,
    private val resultRepository: DecisionResultRepository,
    private val immunizationResolver: IImmunizationResolver,
    private val mapper: ObjectMapper
) : IDeciderBusinessService {
    override fun initiateRequest(requestId: String, request: InitiateRequestDto): DecisionRequestDao {
        if (requestRepository.findBySourceRefId(request.sourceRefId).isPresent) throw Exception("Cannot create duplicate requests!")

        val dao = DecisionRequestDao(
            userId = request.userId,
            requestId = requestId,
            sourceRefId = request.sourceRefId,
            status = ImmunizationDecisionStatus.IN_PROGRESS.toString()
        )

        return requestRepository.save(dao)
    }

    override fun processRequest(decisionRequestId: Long) = tryBusinessExecution {
        val dao: DecisionRequestDao = requestRepository.findById(decisionRequestId).get()
        val sourceRefId = dao.sourceRefId

        val userResponse: UserResponse = userClientFactory.create(requestId = dao.requestId).let { client ->
            val response = client.getUser(dao.userId.toString()).validate(sourceRefId)
            if (response.body?.dateOfBirth == null) throw BusinessExternalRequestException(response, sourceRefId)
            response.body
        }

        val historyResponse: ImmunizationHistoryResponse =
            immunizationHistoryClientFactory.create(requestId = dao.requestId).let { client ->
                val response = client.getHistory(dao.userId.toString()).validate(sourceRefId)
                if (response.body?.occurrences == null) throw BusinessExternalRequestException(response, sourceRefId)
                response.body
            }

        val availableImmunizations = immunizationResolver.resolve(userResponse, historyResponse)

        val updateRequestDao = dao.copy(
            status = ImmunizationDecisionStatus.SUCCESS.toString(),
            finishedAt = Instant.now()
        )
        requestRepository.save(updateRequestDao)

        val resultDao = DecisionResultDao(
            decisionRequestId = updateRequestDao.id,
            userId = updateRequestDao.userId,
            requestId = updateRequestDao.requestId,
            sourceRefId = updateRequestDao.sourceRefId,
            availableImmunizations = mapper.writeValueAsString(availableImmunizations.map { it.toString() }),
        )

        resultRepository.save(resultDao)

        // TODO put record in Kafka with result, then have consumer call Pharmacy service

        pharmacyClientFactory
            .create(requestId = dao.requestId)
            .postImmunizationDecision(request = getStatus(dao.sourceRefId))
            .validate(sourceRefId)
        Unit
    }

    private fun <T> ApiResponse<T>.validate(sourceRefId: String): ApiResponse<T> = apply {
        if (!isSuccessful || body == null) throw BusinessExternalRequestException(this, sourceRefId)
    }

    override fun getStatus(sourceRefId: String): ImmunizationDecisionStatusResponse {
        val requestDao = requestRepository.findBySourceRefId(sourceRefId).let {
            if (it.isEmpty) throw RequestNotFoundException(sourceRefId)
            it.get()
        }
        val resultDao = resultRepository.findBySourceRefId(sourceRefId)
        val status = ImmunizationDecisionStatus.valueOf(requestDao.status)
        val availableImmunizations = if (resultDao.isEmpty) emptyList() else mapper.readValue(
            resultDao.get().availableImmunizations,
            object : TypeReference<List<String>>() {}
        ).map {
            ImmunizationType.valueOf(it)
        }

        return ImmunizationDecisionStatusResponse(
            sourceRefId = sourceRefId,
            userId = requestDao.userId,
            status = status,
            availableImmunizations = availableImmunizations,
            startedAt = requestDao.startedAt,
            finishedAt = requestDao.finishedAt,
            error = requestDao.error
        )
    }

    private fun updateRequestError(ex: BusinessException) {
        val dao = requestRepository.findBySourceRefId(ex.sourceRefId)
        if (dao.isPresent) {
            requestRepository.save(
                dao.get().copy(
                    error = ex.errorMessage,
                    status = ImmunizationDecisionStatus.FAILURE.toString(),
                    finishedAt = Instant.now()
                )
            )
        }
    }

    private fun <T> tryBusinessExecution(block: () -> T): T? = try {
        block()
    } catch (ex: BusinessException) {
        updateRequestError(ex)
        null
    }
}
