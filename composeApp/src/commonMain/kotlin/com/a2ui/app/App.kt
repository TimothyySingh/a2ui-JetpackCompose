package com.a2ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a2ui.core.connection.A2UIConnection
import com.a2ui.core.connection.A2UIConnectionConfig
import com.a2ui.core.connection.ConnectionState
import com.a2ui.core.render.A2UIRenderer
import com.a2ui.core.state.A2UIStateManager
import kotlinx.coroutines.launch

@Composable
fun App(
    config: A2UIConnectionConfig = A2UIConnectionConfig()
) {
    val stateManager = remember { A2UIStateManager() }
    val connection = remember { A2UIConnection(stateManager, config) }
    
    val document by stateManager.document.collectAsState()
    val isLoading by stateManager.isLoading.collectAsState()
    val connectionState by connection.connectionState.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                connectionState is ConnectionState.Error -> {
                    ConnectionErrorScreen(
                        error = (connectionState as ConnectionState.Error).message,
                        onRetry = {
                            scope.launch { connection.connect(scope) }
                        }
                    )
                }
                connectionState is ConnectionState.Connecting -> {
                    LoadingScreen("Connecting...")
                }
                connectionState is ConnectionState.Disconnected -> {
                    DisconnectedScreen(
                        onConnect = {
                            scope.launch { connection.connect(scope) }
                        }
                    )
                }
                isLoading -> {
                    LoadingScreen("Loading UI...")
                }
                document != null -> {
                    A2UIRenderer(
                        document = document!!,
                        onAction = { action ->
                            scope.launch {
                                stateManager.handleAction(action)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    EmptyScreen()
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}

@Composable
private fun DisconnectedScreen(onConnect: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "A2UI",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Not connected to agent",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onConnect) {
                Text("Connect")
            }
        }
    }
}

@Composable
private fun ConnectionErrorScreen(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Connection Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Waiting for UI...",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "The agent will push a UI when ready",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
