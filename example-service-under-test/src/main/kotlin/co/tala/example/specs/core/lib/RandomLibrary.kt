package co.tala.example.specs.core.lib

import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

fun nextLong(): Long = abs(Random.nextLong())
fun nextLong(from: Long, until: Long): Long = if (from == until) from else Random.nextLong(from, until)
fun nextInt(): Int = abs(Random.nextInt())
fun nextInt(from: Int, until: Int): Int = if (from == until) from else Random.nextInt(from, until)
fun nextDouble(): Double = Random.nextDouble()
fun nextDouble(from: Double, until: Double): Double = Random.nextDouble(from, until)
fun randomUUID(): String = UUID.randomUUID().toString()
fun randomHex(): String = randomUUID().replace("-", "")
