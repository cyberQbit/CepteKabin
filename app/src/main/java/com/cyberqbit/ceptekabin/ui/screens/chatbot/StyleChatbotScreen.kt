package com.cyberqbit.ceptekabin.ui.screens.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cyberqbit.ceptekabin.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyleChatbotScreen(
    onNavigateBack: () -> Unit,
    viewModel: StyleChatbotViewModel = hiltViewModel()
) {
    val uiState       by viewModel.uiState.collectAsState()
    val isDark        = isSystemInDarkTheme()
    val listState     = rememberLazyListState()
    val scope         = rememberCoroutineScope()
    var inputText     by remember { mutableStateOf("") }
    var showKeyDialog by remember { mutableStateOf(uiState.apiKey.isBlank()) }

    // Yeni mesaj gelince en alta scroll
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(uiState.messages.size - 1) }
        }
    }

    if (showKeyDialog) {
        ApiKeySetupDialog(
            onConfirm = { key -> viewModel.setApiKey(key); showKeyDialog = false },
            onDismiss = onNavigateBack
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(Brush.radialGradient(
                                    listOf(PrimaryLight, SecondaryDark)
                                )),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null,
                                Modifier.size(18.dp), tint = White)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Stil Asistanı", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("Dolabın hakkında her şeyi biliyor",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) Grey400 else Grey600)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showKeyDialog = true }) {
                        Icon(Icons.Default.Key, "API Ayarları",
                            tint = if (isDark) Grey400 else Grey600)
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, "Sohbeti Sil",
                            tint = if (isDark) Grey400 else Grey600)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                color = if (isDark) SurfaceDark else SurfaceLight
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("örn: Yarın iş toplantısı için ne giyeyim?") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = PrimaryLight,
                            unfocusedBorderColor = if (isDark) Grey700 else Grey300
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !uiState.isLoading) {
                                viewModel.sendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) PrimaryLight else Grey500)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(Modifier.size(20.dp), White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, "Gönder", tint = White)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(
                    if (isDark) listOf(Grey900, SurfaceDark) else listOf(Grey100, White)
                ))
                .padding(padding)
        ) {
            // Öneri chip'leri (boşsa)
            if (uiState.messages.isEmpty()) {
                SuggestionChips(
                    onSelect = { viewModel.sendMessage(it) },
                    isDark   = isDark
                )
            }

            LazyColumn(
                state   = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages) { msg ->
                    MessageBubble(message = msg, isDark = isDark)
                }
                if (uiState.isLoading) {
                    item { TypingIndicator(isDark) }
                }
            }
        }
    }
}

@Composable
private fun SuggestionChips(onSelect: (String) -> Unit, isDark: Boolean) {
    val suggestions = listOf(
        "Bugün ne giysem?",
        "3 günlük İstanbul gezisi için bavul listesi",
        "Yarın iş toplantısına uygun kombin",
        "Hafta sonu gündelik kombin öner",
        "Kış için sıcak ama şık kombinler"
    )
    Column(Modifier.padding(16.dp)) {
        Text("Hızlı sorular:",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDark) Grey500 else Grey600)
        Spacer(Modifier.height(8.dp))
        suggestions.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    SuggestionChip(
                        onClick = { onSelect(s) },
                        label   = { Text(s, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isDark: Boolean) {
    val isUser = message.role == ChatRole.USER
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                Modifier.size(28.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(PrimaryLight, SecondaryDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AutoAwesome, null, Modifier.size(16.dp), tint = White)
            }
            Spacer(Modifier.width(8.dp))
        }
        Surface(
            shape = RoundedCornerShape(
                topStart    = if (isUser) 16.dp else 4.dp,
                topEnd      = if (isUser) 4.dp  else 16.dp,
                bottomStart = 16.dp, bottomEnd = 16.dp
            ),
            color = if (isUser) PrimaryLight
                    else if (isDark) GlassDarkSurface else GlassLightSurface,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text     = message.content,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style    = MaterialTheme.typography.bodyMedium,
                color    = if (isUser) White
                           else if (isDark) Grey100 else Grey900
            )
        }
    }
}

@Composable
private fun TypingIndicator(isDark: Boolean) {
    Row(Modifier.padding(start = 36.dp)) {
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp,
                bottomStart = 16.dp, bottomEnd = 16.dp),
            color = if (isDark) GlassDarkSurface else GlassLightSurface
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { i ->
                    Box(
                        Modifier.size(6.dp).clip(CircleShape)
                            .background(if (isDark) Grey400 else Grey500)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeySetupDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var key by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("OpenAI API Anahtarı") },
        text  = {
            Column {
                Text(
                    "Stil asistanı için kendi OpenAI API anahtarınızı kullanın. " +
                    "Anahtar yalnızca cihazınızda saklanır, hiçbir sunucuya gönderilmez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = key, onValueChange = { key = it },
                    label = { Text("sk-...") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (key.isNotBlank()) onConfirm(key.trim()) },
                enabled = key.isNotBlank()) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
