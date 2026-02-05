package com.a2ui.core.connection

import com.a2ui.core.render.A2UIActionEvent
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
 * A2UI Connection - Handles communication with the OpenClaw gateway
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
    
    /**
     * Connect to the OpenClaw gateway
     */
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
                
                // Start listening for messages
                connectionJob = scope.launch {
                    listenForMessages(wsSession)
                }
                
                // Forward actions to gateway
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
    
    /**
     * Disconnect from the gateway
     */
    suspend fun disconnect() {
        connectionJob?.cancel()
        session?.close()
        session = null
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Send an action event to the gateway
     */
    private suspend fun sendAction(action: A2UIActionEvent) {
        val message = A2UIMessage(
            type = "action",
            payload = ActionPayload(
                nodeId = action.nodeId,
                handler = action.handler,
                data = action.payload?.toString()
            )
        )
        
        session?.send(Frame.Text(json.encodeToString(message)))
    }
    
    /**
     * Listen for incoming messages from the gateway
     */
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
    
    /**
     * Handle incoming message from gateway
     */
    private fun handleMessage(text: String) {
        runCatching {
            val message = json.decodeFromString<A2UIMessage>(text)
            
            when (message.type) {
                "document" -> {
                    message.payload?.let { payload ->
                        if (payload is DocumentPayload) {
                            stateManager.loadDocument(payload.content)
                        }
                    }
                }
                "node" -> {
                    message.payload?.let { payload ->
                        if (payload is NodePayload) {
                            stateManager.loadNode(payload.content)
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

/**
 * Connection configuration
 */
data class A2UIConnectionConfig(
    val host: String = "localhost",
    val port: Int = 18789,
    val path: String = "/a2ui",
    val token: String? = null
)

/**
 * Connection state
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Message types for gateway communication
 */
@Serializable
data class A2UIMessage(
    val type: String,
    val payload: MessagePayload? = null
)

@Serializable
sealed class MessagePayload

@Serializable
data class DocumentPayload(val content: String) : MessagePayload()

@Serializable
data class NodePayload(val content: String) : MessagePayload()

@Serializable
data class CommandsPayload(val jsonl: String) : MessagePayload()

@Serializable
data class LoadingPayload(val isLoading: Boolean) : MessagePayload()

@Serializable
data class ActionPayload(
    val nodeId: String?,
    val handler: String,
    val data: String?
) : MessagePayload()
