import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.springmcp.webadmin.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}