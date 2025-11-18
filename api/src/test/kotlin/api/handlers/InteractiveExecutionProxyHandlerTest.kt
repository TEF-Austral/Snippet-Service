package api.handlers

import authorization.AuthorizationService
import authorization.UserAction
import dtos.types.Language
import entity.Snippet
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.util.ReflectionTestUtils
import java.util.concurrent.CompletableFuture
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import repositories.SnippetRepository
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class InteractiveExecutionProxyHandlerTest {

    @Mock
    private lateinit var snippetRepository: SnippetRepository

    @Mock
    private lateinit var authorizationService: AuthorizationService

    @Mock
    private lateinit var jwtDecoder: JwtDecoder

    @Mock
    private lateinit var m2mClientManager: OAuth2AuthorizedClientManager

    @Mock
    private lateinit var downstreamSession: WebSocketSession

    @Mock
    private lateinit var jwt: Jwt

    private lateinit var handler: InteractiveExecutionProxyHandler

    private val printScriptDomain = "localhost:8081"

    @BeforeEach
    fun setUp() {
        handler =
            InteractiveExecutionProxyHandler(
                snippetRepository,
                authorizationService,
                jwtDecoder,
                m2mClientManager,
                printScriptDomain,
            )
    }

    @Test
    fun `afterConnectionEstablished closes session when snippetId is missing`() {
        val attributes = mutableMapOf<String, Any>()
        `when`(downstreamSession.attributes).thenReturn(attributes)

        handler.afterConnectionEstablished(downstreamSession)

        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionEstablished closes session when token is missing`() {
        val attributes = mutableMapOf<String, Any>("snippetId" to 1L)
        `when`(downstreamSession.attributes).thenReturn(attributes)

        handler.afterConnectionEstablished(downstreamSession)

        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionEstablished closes session when snippet not found`() {
        val snippetId = 1L
        val token = "valid-token"
        val attributes =
            mutableMapOf<String, Any>(
                "snippetId" to snippetId,
                "token" to token,
            )
        `when`(downstreamSession.attributes).thenReturn(attributes)
        `when`(downstreamSession.id).thenReturn("session-1")
        `when`(jwtDecoder.decode(token)).thenReturn(jwt)
        `when`(jwt.subject).thenReturn("user-123")
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.empty())

        handler.afterConnectionEstablished(downstreamSession)

        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionEstablished closes session when user lacks permission`() {
        val snippetId = 1L
        val token = "valid-token"
        val userId = "user-123"
        val ownerId = "owner-456"
        val snippet =
            Snippet(
                id = snippetId,
                ownerId = ownerId,
                name = "test",
                description = "test snippet",
                bucketContainer = "container",
                bucketKey = "key",
                language = Language.PYTHON,
                version = "1.1",
                author = "test-author",
            )
        val attributes =
            mutableMapOf<String, Any>(
                "snippetId" to snippetId,
                "token" to token,
            )
        `when`(downstreamSession.attributes).thenReturn(attributes)
        `when`(downstreamSession.id).thenReturn("session-1")
        `when`(jwtDecoder.decode(token)).thenReturn(jwt)
        `when`(jwt.subject).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationService.checkPermission(
                userId,
                UserAction.EDIT,
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(false)

        handler.afterConnectionEstablished(downstreamSession)

        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionEstablished closes when M2M token cannot be obtained`() {
        val snippetId = 1L
        val token = "valid-token"
        val userId = "user-123"
        val ownerId = "owner-456"
        val snippet =
            Snippet(
                id = snippetId,
                ownerId = ownerId,
                name = "test",
                description = "test snippet",
                bucketContainer = "container",
                bucketKey = "key",
                language = Language.PYTHON,
                version = "1.1",
                author = "test-author",
            )
        val attributes =
            mutableMapOf<String, Any>(
                "snippetId" to snippetId,
                "token" to token,
            )
        `when`(downstreamSession.attributes).thenReturn(attributes)
        `when`(downstreamSession.id).thenReturn("session-1")
        `when`(jwtDecoder.decode(token)).thenReturn(jwt)
        `when`(jwt.subject).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationService.checkPermission(
                userId,
                UserAction.EDIT,
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)
        // Simulate manager returning null authorized client -> null token
        `when`(m2mClientManager.authorize(Mockito.any(OAuth2AuthorizeRequest::class.java)))
            .thenReturn(null)

        handler.afterConnectionEstablished(downstreamSession)

        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionEstablished closes when M2M authorize throws`() {
        val snippetId = 3L
        val token = "tok"
        val userId = "usr"
        val ownerId = "own"
        val snippet =
            Snippet(
                id = snippetId,
                ownerId = ownerId,
                name = "n",
                description = "d",
                bucketContainer = "c",
                bucketKey = "k",
                language = Language.PYTHON,
                version = "v",
                author = "a",
            )
        val attrs = mutableMapOf<String, Any>("snippetId" to snippetId, "token" to token)
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-x")
        `when`(jwtDecoder.decode(token)).thenReturn(jwt)
        `when`(jwt.subject).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationService.checkPermission(
                userId,
                UserAction.EDIT,
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)
        `when`(m2mClientManager.authorize(Mockito.any(OAuth2AuthorizeRequest::class.java)))
            .thenThrow(RuntimeException("auth boom"))

        handler.afterConnectionEstablished(downstreamSession)

        // Current handler behavior: it logs the auth error and closes the downstream session
        verify(downstreamSession, never()).sendMessage(Mockito.any(TextMessage::class.java))
        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionEstablished success path sets upstream and sends init`() {
        val snippetId = 100L
        val token = "down-token"
        val userId = "user-abc"
        val ownerId = "owner-xyz"
        val snippet =
            Snippet(
                id = snippetId,
                ownerId = ownerId,
                name = "my-snippet",
                description = "desc",
                bucketContainer = "container",
                bucketKey = "key",
                language = Language.PYTHON,
                version = "1.0",
                author = "author",
            )
        val attrs = mutableMapOf<String, Any>("snippetId" to snippetId, "token" to token)
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-1")
        `when`(jwtDecoder.decode(token)).thenReturn(jwt)
        `when`(jwt.subject).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationService.checkPermission(
                userId,
                UserAction.EDIT,
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)

        val accessToken =
            OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "m2m-token",
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600),
            )
        val authorizedClient = Mockito.mock(OAuth2AuthorizedClient::class.java)
        `when`(authorizedClient.accessToken).thenReturn(accessToken)
        `when`(m2mClientManager.authorize(Mockito.any(OAuth2AuthorizeRequest::class.java)))
            .thenReturn(authorizedClient)

        val webSocketClient = Mockito.mock(StandardWebSocketClient::class.java)
        val upstream = Mockito.mock(WebSocketSession::class.java)
        val future = CompletableFuture<WebSocketSession>()
        future.complete(upstream)
        `when`(
            webSocketClient.execute(Mockito.any(UpstreamHandler::class.java), Mockito.anyString()),
        ).thenReturn(future)
        ReflectionTestUtils.setField(handler, "webSocketClient", webSocketClient)

        handler.afterConnectionEstablished(downstreamSession)

        assert(attrs["UPSTREAM_SESSION"] === upstream)
        val captor = ArgumentCaptor.forClass(TextMessage::class.java)
        verify(upstream).sendMessage(captor.capture())
        val payload = captor.value.payload
        assert(payload.contains("InitExecution"))
        assert(payload.contains(snippet.bucketContainer))
        assert(payload.contains(snippet.bucketKey!!))
        assert(payload.contains(snippet.version))
    }

    @Test
    fun `afterConnectionEstablished sends error and closes when upstream connect fails`() {
        val snippetId = 200L
        val token = "down-token"
        val userId = "user-abc"
        val ownerId = "owner-xyz"
        val snippet =
            Snippet(
                id = snippetId,
                ownerId = ownerId,
                name = "my-snippet",
                description = "desc",
                bucketContainer = "container",
                bucketKey = "key",
                language = Language.PYTHON,
                version = "1.0",
                author = "author",
            )
        val attrs = mutableMapOf<String, Any>("snippetId" to snippetId, "token" to token)
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-1")
        `when`(jwtDecoder.decode(token)).thenReturn(jwt)
        `when`(jwt.subject).thenReturn(userId)
        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
        `when`(
            authorizationService.checkPermission(
                userId,
                UserAction.EDIT,
                snippetId.toString(),
                ownerId,
            ),
        ).thenReturn(true)

        val accessToken =
            OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "m2m-token",
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(3600),
            )
        val authorizedClient = Mockito.mock(OAuth2AuthorizedClient::class.java)
        `when`(authorizedClient.accessToken).thenReturn(accessToken)
        `when`(m2mClientManager.authorize(Mockito.any(OAuth2AuthorizeRequest::class.java)))
            .thenReturn(authorizedClient)

        val webSocketClient = Mockito.mock(StandardWebSocketClient::class.java)
        val failedFuture = CompletableFuture<WebSocketSession>()
        failedFuture.completeExceptionally(RuntimeException("boom"))
        `when`(
            webSocketClient.execute(Mockito.any(UpstreamHandler::class.java), Mockito.anyString()),
        ).thenReturn(failedFuture)
        ReflectionTestUtils.setField(handler, "webSocketClient", webSocketClient)

        handler.afterConnectionEstablished(downstreamSession)

        verify(downstreamSession).sendMessage(Mockito.any(TextMessage::class.java))
        verify(downstreamSession).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `handleMessage forwards to upstream when available and open`() {
        val attrs = mutableMapOf<String, Any>()
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-1")
        val upstream = Mockito.mock(WebSocketSession::class.java)
        `when`(upstream.isOpen).thenReturn(true)
        attrs["UPSTREAM_SESSION"] = upstream

        val msg = TextMessage("hello")

        handler.handleMessage(downstreamSession, msg)

        verify(upstream).sendMessage(msg)
    }

    @Test
    fun `handleMessage does nothing when upstream session missing`() {
        val attrs = mutableMapOf<String, Any>()
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-2")

        handler.handleMessage(downstreamSession, TextMessage("ping"))
    }

    @Test
    fun `handleMessage does not forward when upstream is closed`() {
        val attrs = mutableMapOf<String, Any>()
        val upstream = Mockito.mock(WebSocketSession::class.java)
        `when`(upstream.isOpen).thenReturn(false)
        attrs["UPSTREAM_SESSION"] = upstream
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-3")

        val msg = TextMessage("hello")
        handler.handleMessage(downstreamSession, msg)

        verify(upstream, never()).sendMessage(msg)
    }

    @Test
    fun `handleMessage catches exception sending to upstream`() {
        val attrs = mutableMapOf<String, Any>()
        val upstream = Mockito.mock(WebSocketSession::class.java)
        `when`(upstream.isOpen).thenReturn(true)
        Mockito
            .doThrow(
                RuntimeException("send boom"),
            ).`when`(upstream)
            .sendMessage(Mockito.any(TextMessage::class.java))
        attrs["UPSTREAM_SESSION"] = upstream
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-4")

        handler.handleMessage(downstreamSession, TextMessage("boom"))
    }

    @Test
    fun `afterConnectionClosed closes upstream if open`() {
        val attrs = mutableMapOf<String, Any>()
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-1")
        val upstream = Mockito.mock(WebSocketSession::class.java)
        `when`(upstream.isOpen).thenReturn(true)
        attrs["UPSTREAM_SESSION"] = upstream

        handler.afterConnectionClosed(downstreamSession, CloseStatus.NORMAL)

        verify(upstream).close(CloseStatus.NORMAL)
    }

    @Test
    fun `afterConnectionClosed ignores when upstream is closed`() {
        val attrs = mutableMapOf<String, Any>()
        val upstream = Mockito.mock(WebSocketSession::class.java)
        `when`(upstream.isOpen).thenReturn(false)
        attrs["UPSTREAM_SESSION"] = upstream
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-5")

        handler.afterConnectionClosed(downstreamSession, CloseStatus.NORMAL)

        verify(upstream, never()).close(Mockito.any(CloseStatus::class.java))
    }

    @Test
    fun `afterConnectionClosed catches exception when closing upstream`() {
        val attrs = mutableMapOf<String, Any>()
        val upstream = Mockito.mock(WebSocketSession::class.java)
        `when`(upstream.isOpen).thenReturn(true)
        Mockito
            .doThrow(
                RuntimeException("close boom"),
            ).`when`(upstream)
            .close(Mockito.any(CloseStatus::class.java))
        attrs["UPSTREAM_SESSION"] = upstream
        `when`(downstreamSession.attributes).thenReturn(attrs)
        `when`(downstreamSession.id).thenReturn("dwn-6")

        handler.afterConnectionClosed(downstreamSession, CloseStatus.SERVER_ERROR)
    }
}
