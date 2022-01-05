package co.tala.api.fakedependency.business.helper

import org.springframework.stereotype.Component

@Component
class Sleep : ISleep {
    override fun forMillis(millis: Long) {
        Thread.sleep(millis)
    }
}
