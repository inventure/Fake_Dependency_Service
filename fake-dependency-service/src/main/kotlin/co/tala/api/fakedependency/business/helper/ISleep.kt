package co.tala.api.fakedependency.business.helper

interface ISleep {
    /**
     * Sleeps for duration set in milliseconds.
     */
    fun forMillis(millis: Long)
}
