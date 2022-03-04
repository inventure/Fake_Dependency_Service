package co.tala.api.fakedependency.controller

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator


class HealthController : HealthIndicator {
    override fun health(): Health = Health.up().build()
}
