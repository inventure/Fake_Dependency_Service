package co.tala.example.api.immunization_decider.business.resolver

import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import co.tala.example.http.client.lib.service.user.model.UserResponse
import org.springframework.stereotype.Component

interface IImmunizationResolver {
    fun resolve(userResponse: UserResponse, historyResponse: ImmunizationHistoryResponse): List<ImmunizationType>
}

@Component
class ImmunizationResolver(private val resolvers: List<IImmunizationTypeResolver>) : IImmunizationResolver {
    override fun resolve(
        userResponse: UserResponse,
        historyResponse: ImmunizationHistoryResponse
    ): List<ImmunizationType> = resolvers.mapNotNull { it.resolve(userResponse, historyResponse) }
}
