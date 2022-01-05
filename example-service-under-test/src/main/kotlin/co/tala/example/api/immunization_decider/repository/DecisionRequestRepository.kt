package co.tala.example.api.immunization_decider.repository

import co.tala.example.api.immunization_decider.repository.model.DecisionRequestDao
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DecisionRequestRepository : JpaRepository<DecisionRequestDao, Long> {
    fun findBySourceRefId(sourceRefId: String): Optional<DecisionRequestDao>
}
