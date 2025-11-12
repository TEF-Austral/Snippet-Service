package api.config

import com.newrelic.api.agent.NewRelic
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(1)
class RequestIdFilter : OncePerRequestFilter() {

    companion object {
        const val REQUEST_ID_HEADER = "X-Request-ID"
        const val REQUEST_ID_MDC_KEY = "requestId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId =
            request.getHeader(REQUEST_ID_HEADER)
                ?: UUID.randomUUID().toString()

        MDC.put(REQUEST_ID_MDC_KEY, requestId)

        response.setHeader(REQUEST_ID_HEADER, requestId)

        try {
            NewRelic.addCustomParameter("requestId", requestId)
        } catch (e: Exception) {
            throw Exception("Failed to add requestId to New Relic", e)
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
