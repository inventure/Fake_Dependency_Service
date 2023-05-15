package co.tala.api.fakedependency.extension

import co.tala.api.fakedependency.constant.Constant.MOCK_RESOURCES
import co.tala.api.fakedependency.constant.HttpMethod
import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.hasMockResources(): Boolean = requestURI.contains(MOCK_RESOURCES)
fun HttpServletRequest.getHttpMethod(): HttpMethod = HttpMethod.of(method.uppercase())
