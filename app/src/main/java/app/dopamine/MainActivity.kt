package app.dopamine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.dopamine.ui.AppNav
import app.dopamine.ui.theme.DopamineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val container = (application as App).container
        setContent {
            DopamineTheme {
                AppNav(container)
            }
        }
    }
}
