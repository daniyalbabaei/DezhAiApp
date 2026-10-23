package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GgufModelEntity
import com.example.ui.theme.ChatGptEmerald
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkInputBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DezhAiCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search

@Composable
fun ChatInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    isGenerating: Boolean,
    isWebSearchEnabled: Boolean,
    isSearchingWeb: Boolean,
    activeModel: GgufModelEntity?,
    onSendMessage: (String) -> Unit,
    onStopGeneration: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onOpenModelManager: () -> Unit,
    onAttachFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Main Input Capsule
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(DarkInputBg)
                    .border(1.dp, DarkBorder, RoundedCornerShape(26.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Live Web Searching Banner Indicator
                    if (isSearchingWeb) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChatGptEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = ChatGptEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "در حال جستجو در وب برای یافتن جدیدترین اطلاعات...",
                                color = ChatGptEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Text Entry Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 28.dp, max = 130.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputText.isEmpty()) {
                                Text(
                                    text = if (isWebSearchEnabled) "با قابلیت سرچ وب بنویسید..." else "پیامی برای DezhAi بنویسید...",
                                    color = TextMuted,
                                    fontSize = 15.sp
                                )
                            }
                            BasicTextField(
                                value = inputText,
                                onValueChange = onInputChange,
                                textStyle = TextStyle(
                                    color = TextWhite,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                ),
                                cursorBrush = SolidColor(ChatGptEmerald),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_input_text_field"),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Default
                                )
                            )
                        }
                    }

                    // Action Controls inside input box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attach file button
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { onAttachFile() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Attach file",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Web Search Toggle Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isWebSearchEnabled) ChatGptEmerald.copy(alpha = 0.2f) else DarkSurface.copy(alpha = 0.8f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isWebSearchEnabled) ChatGptEmerald else DarkBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onToggleWebSearch() }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Web Search",
                                tint = if (isWebSearchEnabled) ChatGptEmerald else TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isWebSearchEnabled) "جستجوی وب" else "آفلاین",
                                color = if (isWebSearchEnabled) ChatGptEmerald else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isWebSearchEnabled) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Active GGUF Model Chip
                        if (activeModel != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface.copy(alpha = 0.8f))
                                    .clickable { onOpenModelManager() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = DezhAiCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${activeModel.name.take(16)} (${activeModel.quantType})",
                                    color = TextWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Send or Stop Button
                        if (isGenerating) {
                            // Stop Generation Button
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { onStopGeneration() }
                                    .testTag("stop_generation_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.Black)
                                )
                            }
                        } else {
                            // Send Button
                            val isReady = inputText.isNotBlank()
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isReady) Color.White else Color(0xFF50515F))
                                    .clickable(enabled = isReady) {
                                        onSendMessage(inputText)
                                    }
                                    .testTag("send_message_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Send",
                                    tint = if (isReady) Color.Black else Color(0xFF2E2F38),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Disclaimer caption
            Text(
                text = "DezhAi می‌تواند اشتباه کند. اطلاعات مهم را بررسی کنید.",
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp)
            )
        }
    }
}
