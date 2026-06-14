package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun FloatingAiChat(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isChatOpen by remember { mutableStateOf(false) }
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()

    val activeService = viewModel.getActiveAiService()
    val hasKeysConfigured = if (activeService == "Gemini") {
        viewModel.getGeminiApiKey().isNotEmpty()
    } else {
        viewModel.getGroqApiKey().isNotEmpty()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // 1. Floating Circle Button (when chat is CLOSED)
        AnimatedVisibility(
            visible = !isChatOpen,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            FloatingActionButton(
                onClick = { isChatOpen = true },
                containerColor = PrimaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, CircleShape)
                    .testTag("floating_ai_chat_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Open AI Chat Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // 2. Expandable Chat Dialogue UI card (when chat is OPEN)
        AnimatedVisibility(
            visible = isChatOpen,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, StatusInactive),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .height(520.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .testTag("ai_chat_card_window")
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // --- Header ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Sparks",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TuitionOS Assistant",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Model: $activeService AI",
                                    color = OnPrimaryContainerColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Reset/Clear History Button
                            IconButton(
                                onClick = {
                                    viewModel.clearChatHistory()
                                    Toast.makeText(context, "History Cleared", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Clear Chat",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Dismiss/Close Chat Panel Button
                            IconButton(
                                onClick = { isChatOpen = false },
                                modifier = Modifier.testTag("close_ai_chat_button").size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Minimize Chat",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // --- Warning / Notice Header if keys are missing ---
                    if (!hasKeysConfigured) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF4E5))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "API Key not configured. Save $activeService key in Settings first.",
                                    color = Color(0xFF663C00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // --- Conversation Scroll List ---
                    val listState = rememberLazyListState()
                    LaunchedEffect(chatMessages.size) {
                        if (chatMessages.isNotEmpty()) {
                            listState.animateScrollToItem(chatMessages.size - 1)
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(BackgroundColor)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages, key = { it.id }) { message ->
                            val alignment = if (message.isUser) Alignment.End else Alignment.Start
                            val containerColor = if (message.isUser) PrimaryColor else Color.White
                            val textColor = if (message.isUser) Color.White else OnSurfaceColor
                            val corners = if (message.isUser) {
                                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                            } else {
                                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                            }
                            val borderProp = if (message.isUser) null else BorderStroke(1.dp, StatusInactive)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = alignment
                            ) {
                                Card(
                                    shape = corners,
                                    border = borderProp,
                                    colors = CardDefaults.cardColors(containerColor = containerColor),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Box(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = message.text,
                                            color = textColor,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (isThinking) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryColor
                                    )
                                    Text(
                                        text = "AI is thinking (ஆலோசிக்கிறது)...",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = OnSurfaceVariantColor
                                    )
                                }
                            }
                        }
                    }

                    // --- Scrollable Suggestions Chips Line ---
                    val suggestions = listOf(
                        "Who has the most outstanding fees?",
                        "Active students overview",
                        "Show staff list & batches",
                        "High attendance students"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundColor)
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                        ) {
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(suggestions) { keyword ->
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(1.dp, OutlineVariantColor, RoundedCornerShape(12.dp))
                                            .clickable(enabled = !isThinking && hasKeysConfigured) {
                                                viewModel.sendMessageToAi(keyword)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = keyword,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (hasKeysConfigured) PrimaryColor else OutlineColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- Input Box Lower Bar ---
                    var textInput by remember { mutableStateOf("") }
                    val controller = LocalSoftwareKeyboardController.current

                    val executeSend = {
                        if (textInput.isNotBlank() && !isThinking) {
                            viewModel.sendMessageToAi(textInput)
                            textInput = ""
                            controller?.hide()
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .border(BorderStroke(1.dp, StatusInactive))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Ask TuitionOS AI... (e.g. outstanding fees)", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 100.dp)
                                .testTag("ai_chat_input_text"),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                            singleLine = true,
                            enabled = !isThinking && hasKeysConfigured,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { executeSend() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryColor,
                                unfocusedBorderColor = Color.Transparent,
                                disabledBorderColor = Color.Transparent,
                                disabledPlaceholderColor = OutlineColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { executeSend() },
                            enabled = textInput.isNotBlank() && !isThinking && hasKeysConfigured,
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (textInput.isNotBlank() && !isThinking && hasKeysConfigured) PrimaryColor else SurfaceColor
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .testTag("ai_chat_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send text",
                                tint = if (textInput.isNotBlank() && !isThinking && hasKeysConfigured) Color.White else OutlineColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
