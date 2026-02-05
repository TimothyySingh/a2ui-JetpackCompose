package com.a2ui.app

import androidx.compose.ui.window.ComposeUIViewController
import com.a2ui.core.connection.A2UIConnectionConfig

fun MainViewController(
    host: String = "localhost",
    port: Int = 18789,
    path: String = "/a2ui"
) = ComposeUIViewController {
    App(
        config = A2UIConnectionConfig(
            host = host,
            port = port,
            path = path
        )
    )
}
