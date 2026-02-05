package com.a2ui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.a2ui.core.connection.A2UIConnectionConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Configure connection (can be loaded from settings/intent)
        val config = A2UIConnectionConfig(
            host = "localhost",
            port = 18789,
            path = "/a2ui"
        )
        
        setContent {
            App(config = config)
        }
    }
}
