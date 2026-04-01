package com.cyberqbit.ceptekabin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cyberqbit.ceptekabin.ui.navigation.NavGraph
import com.cyberqbit.ceptekabin.ui.theme.CepteKabinTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Uygulama açıkken veya dışarıdan .kmb intent'i geldiğinde bu state güncellenir.
     * NavGraph bu state'i izler, uygun ekrana geçince import sayfasını açar.
     */
    private val _pendingImportUri = mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Uygulama doğrudan .kmb dosyasıyla açıldıysa URI'yi yakala
        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CepteKabinTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        pendingImportUri = _pendingImportUri.value,
                        onImportUriConsumed = { _pendingImportUri.value = null }
                    )
                }
            }
        }
    }

    /**
     * Uygulama arka plandayken .kmb dosyasına tıklanırsa burası tetiklenir.
     * setIntent() çağrısı önemli: bir sonraki onNewIntent'in temiz gelmesini sağlar.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return

        val uri: Uri = intent.data ?: return
        val uriStr = uri.toString().lowercase()
        val mimeType = runCatching { contentResolver.getType(uri) }.getOrNull() ?: ""

        val isKmb = uriStr.endsWith(".kmb")
            || mimeType == "application/octet-stream"
            || mimeType == "application/zip"

        if (isKmb) {
            Log.d("CepteKabin", "📦 .kmb dosyası yakalandı: $uri")
            _pendingImportUri.value = uri
        }
    }
}
