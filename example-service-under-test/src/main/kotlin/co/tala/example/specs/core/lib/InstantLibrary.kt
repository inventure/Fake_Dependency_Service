package co.tala.example.specs.core.lib

import java.time.*

fun now(): Instant = Instant.now()
fun nowMinusDays(daysAgo: Long): Instant = now().minusDays(daysAgo)
fun Instant.minusDays(daysAgo: Long): Instant = this.minusHours(24 * daysAgo)
fun Instant.plusDays(daysAhead: Long): Instant = this.minusDays(-daysAhead)
fun Instant.minusHours(hoursAgo: Long): Instant = this.minusMinutes(hoursAgo * 60)
fun Instant.plusHours(hoursAhead: Long): Instant = this.minusHours(-hoursAhead)
fun Instant.minusMinutes(minutesAgo: Long): Instant = this.minusSeconds(minutesAgo * 60)
fun Instant.plusMinutes(minutesAhead: Long): Instant = this.minusMinutes(-minutesAhead)
fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.of("UTC"))
