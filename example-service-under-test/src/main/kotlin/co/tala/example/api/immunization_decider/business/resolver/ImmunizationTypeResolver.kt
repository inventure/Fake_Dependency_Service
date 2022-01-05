package co.tala.example.api.immunization_decider.business.resolver

import co.tala.example.specs.core.lib.minusDays
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationHistoryResponse
import co.tala.example.http.client.lib.service.immunization_history.model.ImmunizationType
import co.tala.example.http.client.lib.service.user.model.UserResponse
import java.time.Instant

interface IImmunizationTypeResolver {
    fun IImmunizationTypeResolver.minimumAge(): Long
    fun IImmunizationTypeResolver.daysBetweenInjections(): Long
    fun IImmunizationTypeResolver.injectionsPerPeriod(): Int
    fun IImmunizationTypeResolver.daysUntilExpiration(): Long
    fun IImmunizationTypeResolver.getType(): ImmunizationType
    fun resolve(userResponse: UserResponse, historyResponse: ImmunizationHistoryResponse): ImmunizationType? {
        if (userResponse.dateOfBirth == null || historyResponse.occurrences == null) return null
        val immunizationType = getType()
        val atMinimumAge: Boolean = Instant.now()
            .minusDays(minimumAge() * 365)
            .isAfter(userResponse.dateOfBirth)
        val daysBetweenInjections: Long = daysBetweenInjections()
        val injectionsPerPeriod: Int = injectionsPerPeriod()
        val daysUntilExpiration: Long = daysUntilExpiration()
        val injectionsDayAgo: List<Long> = historyResponse.occurrences
            .asSequence()
            // Filter by Immunization type
            .filter { it.type == immunizationType }
            .sortedByDescending { it.date }
            .mapNotNull { it.date }
            // Convert date to days ago
            .map { Instant.now().minusMillis(it.toEpochMilli()).toEpochMilli() / 86400 / 1000 }
            // Remove history that is expired
            .filter { it < daysUntilExpiration }
            .take(injectionsPerPeriod)
            .toList()

        return when {
            !atMinimumAge -> null
            injectionsDayAgo.isEmpty() -> immunizationType
            injectionsDayAgo.size >= injectionsPerPeriod -> null
            injectionsDayAgo.first() > daysBetweenInjections -> immunizationType
            else -> null
        }
    }
}

class Covid19ImmunizationTypeResolver : IImmunizationTypeResolver {
    override fun IImmunizationTypeResolver.minimumAge(): Long = 18
    override fun IImmunizationTypeResolver.daysBetweenInjections(): Long = 28
    override fun IImmunizationTypeResolver.injectionsPerPeriod(): Int = 2
    override fun IImmunizationTypeResolver.daysUntilExpiration(): Long = 365
    override fun IImmunizationTypeResolver.getType(): ImmunizationType = ImmunizationType.COVID19
}

class TdapImmunizationTypeResolver : IImmunizationTypeResolver {
    override fun IImmunizationTypeResolver.minimumAge(): Long = 10
    override fun IImmunizationTypeResolver.daysBetweenInjections(): Long = 0
    override fun IImmunizationTypeResolver.injectionsPerPeriod(): Int = 1
    override fun IImmunizationTypeResolver.daysUntilExpiration(): Long = 365 * 10
    override fun IImmunizationTypeResolver.getType(): ImmunizationType = ImmunizationType.TDAP
}

class InfluenzaImmunizationTypeResolver : IImmunizationTypeResolver {
    override fun IImmunizationTypeResolver.minimumAge(): Long = 1
    override fun IImmunizationTypeResolver.daysBetweenInjections(): Long = 0
    override fun IImmunizationTypeResolver.injectionsPerPeriod(): Int = 1
    override fun IImmunizationTypeResolver.daysUntilExpiration(): Long = 365
    override fun IImmunizationTypeResolver.getType(): ImmunizationType = ImmunizationType.INFLUENZA
}
