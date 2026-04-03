package com.cyberqbit.ceptekabin.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.R
import com.cyberqbit.ceptekabin.ui.theme.*

@Composable
fun GoogleSignInScreen(
    onSignInSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onSignInSuccess()
    }

    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) BackgroundDark else BackgroundLight
    val cardColor = if (isDark) SurfaceVariantDark else White
    val textColor = if (isDark) White else Grey900
    val subTextColor = if (isDark) Grey300 else Grey600

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .shadow(
                    elevation = if (isDark) 0.dp else 16.dp, 
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = PrimaryCyan,
                    spotColor = PrimaryCyan
                )
                .background(cardColor, RoundedCornerShape(24.dp))
                .padding(vertical = 48.dp, horizontal = 24.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "CepteKabin Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CepteKabin",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Senin Dolabın, Senin Kombinin!",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryLight,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Tüm cihazlardan gardırobuna erişmek için Google hesabınla giriş yap.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = subTextColor,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (isLoading) {
                CircularProgressIndicator(color = PrimaryCyan)
            } else {
                // Yüksek Kontrastlı Standart Buton (Okunabilirlik Garantili)
                Button(
                    onClick = {
                        val signInIntent = viewModel.getGoogleSignInIntent()
                        launcher.launch(signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) White else Grey900,
                        contentColor = if (isDark) Grey900 else White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Google ile Giriş Yap",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
