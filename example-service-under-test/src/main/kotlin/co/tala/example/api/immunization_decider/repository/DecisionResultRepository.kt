package co.tala.example.api.immunization_decider.repository

import co.tala.example.api.immunization_decider.repository.model.DecisionResultDao
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface DecisionResultRepository : JpaRepository<DecisionResultDao, Long> {
    fun findBySourceRefId(sourceRefId: String): Optional<DecisionResultDao>
}
