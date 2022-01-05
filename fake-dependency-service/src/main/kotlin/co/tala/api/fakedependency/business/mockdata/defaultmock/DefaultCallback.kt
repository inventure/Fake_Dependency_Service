package co.tala.api.fakedependency.business.mockdata.defaultmock

data class DefaultCallback(
    val isEnabled: Boolean = false,
    val action: (payload: Any?) -> Unit = {}
)
