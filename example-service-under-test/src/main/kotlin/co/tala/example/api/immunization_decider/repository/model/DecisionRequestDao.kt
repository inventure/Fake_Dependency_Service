package co.tala.example.api.immunization_decider.repository.model

import java.time.Instant
import javax.persistence.*

@Entity
@Table(schema = "immunization_decider", name = "decision_requests")
data class DecisionRequestDao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0,

    @Column(name = "user_id")
    var userId: Long,

    @Column(name = "request_id")
    var requestId: String,

    @Column(name = "source_ref_id")
    var sourceRefId: String,

    @Column(name = "status")
    var status: String,

    @Column(name = "error")
    var error: String? = null,

    @Column(name = "started_at")
    var startedAt: Instant = Instant.now(),

    @Column(name = "finished_at")
    var finishedAt: Instant? = null
)
