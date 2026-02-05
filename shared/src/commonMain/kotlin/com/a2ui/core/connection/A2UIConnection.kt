package com.a2ui.core.connection

import com.a2ui.core.model.A2UIActionEvent
import com.a2ui.core.state.A2UIStateManager
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A2UI Connection - Handles communication with the gateway.
 * Updated for A2UISurface and A2UIActionEvent types.
 */
class A2UIConnection(
    private val stateManager: A2UIStateManager,
    private val config: A2UIConnectionConfig = A2UIConnectionConfig()
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val client = HttpClient {
        install(WebSockets)
        install(ContentNegotiation) {
            json(json)
        }
    }

    private var session: WebSocketSession? = null
    private var connectionJob: Job? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    suspend fun connect(scope: CoroutineScope) {
        if (_connectionState.value is ConnectionState.Connected) return

        _connectionState.value = ConnectionState.Connecting

        try {
            client.webSocketSession(
                host = config.host,
                port = config.port,
                path = config.path
            ).also { wsSession ->
                session = wsSession
                _connectionState.value = ConnectionState.Connected

                connectionJob = scope.launch {
                    listenForMessages(wsSession)
                }

                scope.launch {
                    stateManager.actions.collect { action ->
                        sendAction(action)
                    }
                }
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
        }
    }

    suspend fun disconnect() {
        connectionJob?.cancel()
        session?.close()
        session = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private suspend fun sendAction(action: A2UIActionEvent) {
        val message = A2UIMessage(
            type = "action",
            payload = ActionPayload(
                componentId = action.componentId,
                actionName = action.actionName,
                context = action.context?.toString(),
                value = action.value?.toString()
            )
        )

        session?.send(Frame.Text(json.encodeToString(message)))
    }

    private suspend fun listenForMessages(wsSession: WebSocketSession) {
        try {
            for (frame in wsSession.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        handleMessage(text)
                    }
                    is Frame.Close -> {
                        _connectionState.value = ConnectionState.Disconnected
                        break
                    }
                    else -> { /* Ignore other frame types */ }
                }
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection lost")
        }
    }

    private fun handleMessage(text: String) {
        runCatching {
            val message = json.decodeFromString<A2UIMessage>(text)

            when (message.type) {
                "surface", "document" -> {
                    message.payload?.let { payload ->
                        if (payload is SurfacePayload) {
                            stateManager.loadDocument(payload.content)
                        }
                    }
                }
                "commands" -> {
                    message.payload?.let { payload ->
                        if (payload is CommandsPayload) {
                            stateManager.applyCommands(payload.jsonl)
                        }
                    }
                }
                "loading" -> {
                    message.payload?.let { payload ->
                        if (payload is LoadingPayload) {
                            stateManager.setLoading(payload.isLoading)
                        }
                    }
                }
                "clear" -> {
                    stateManager.clear()
                }
            }
        }
    }
}

data class A2UIConnectionConfig(
    val host: String = "localhost",
    val port: Int = 18789,
    val path: String = "/a2ui",
    val token: String? = null
)

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

@Serializable
data class A2UIMessage(
    val type: String,
    val payload: MessagePayload? = null
)

@Serializable
sealed class MessagePayload

@Serializable
data class SurfacePayload(val content: String) : MessagePayload()

@Serializable
data class CommandsPayload(val jsonl: String) : MessagePayload()

@Serializable
data class LoadingPayload(val isLoading: Boolean) : MessagePayload()

@Serializable
data class ActionPayload(
    val componentId: String,
    val actionName: String,
    val context: String?,
    val value: String?
) : MessagePayload()
