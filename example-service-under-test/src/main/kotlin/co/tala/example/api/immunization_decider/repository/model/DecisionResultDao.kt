package co.tala.example.api.immunization_decider.repository.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(schema = "immunization_decider", name = "decision_results")
data class DecisionResultDao(
    @Id
    @Column(name = "decision_request_id")
    var decisionRequestId: Long,

    @Column(name = "user_id")
    var userId: Long,

    @Column(name = "request_id")
    var requestId: String,

    @Column(name = "source_ref_id")
    var sourceRefId: String,

    @Column(name = "available_immunizations")
    var availableImmunizations: String
)
