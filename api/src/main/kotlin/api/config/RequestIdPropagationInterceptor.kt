package api.config

import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class RequestIdPropagationInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val requestId = MDC.get("requestId")
        if (requestId != null) {
            request.headers.set("X-Request-ID", requestId)
        }
        return execution.execute(request, body)
    }
}
