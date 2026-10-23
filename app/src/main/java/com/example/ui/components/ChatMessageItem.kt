package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.example.network.WebSearchEngine
import com.example.network.WebSearchResult
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.theme.ChatGptEmerald
import com.example.ui.theme.CodeBg
import com.example.ui.theme.CodeHeaderBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DezhAiCyan
import com.example.ui.theme.DezhAiPurple
import com.example.ui.theme.DezhAiTeal
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isStreaming: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Assistant Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ChatGptEmerald, DezhAiTeal)
                        )
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "DezhAi Shield",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (isUser) {
                // User Message Bubble
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("user_message_bubble")
                ) {
                    Text(
                        text = message.content,
                        color = TextWhite,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            } else {
                // Assistant Message with formatted Markdown, Code Blocks, Web Sources, and Telemetry
                AssistantMessageBody(
                    content = message.content,
                    isStreaming = isStreaming,
                    tokensPerSecond = message.tokensPerSecond,
                    tokenCount = message.tokenCount,
                    latencyMs = message.latencyMs,
                    searchQuery = message.searchQuery,
                    webSourcesJson = message.webSourcesJson
                )
            }
        }
    }
}

@Composable
private fun AssistantMessageBody(
    content: String,
    isStreaming: Boolean,
    tokensPerSecond: Double,
    tokenCount: Int,
    latencyMs: Long,
    searchQuery: String? = null,
    webSourcesJson: String? = null
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val webSources = remember(webSourcesJson) {
        WebSearchEngine.fromJson(webSourcesJson)
    }

    // Blinking cursor during streaming
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Render Web Sources Carousel if search was executed
        if (webSources.isNotEmpty()) {
            WebSourcesSection(sources = webSources, searchQuery = searchQuery)
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Parse <think> reasoning tags if present
        val (thinkContent, mainContent) = parseThinkingTags(content)

        if (thinkContent.isNotBlank()) {
            ThinkingBlock(thinkContent = thinkContent)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Render main content with markdown / code blocks
        RenderMarkdownContent(text = mainContent)

        if (isStreaming) {
            Text(
                text = "▌",
                color = ChatGptEmerald,
                fontSize = 16.sp,
                modifier = Modifier
                    .alpha(cursorAlpha)
                    .padding(top = 2.dp)
            )
        }

        // Footer Telemetry & Action Buttons
        if (!isStreaming && content.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Telemetry Badge
                if (tokensPerSecond > 0 || tokenCount > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = ChatGptEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tokensPerSecond} t/s • ${tokenCount} tokens • ${latencyMs}ms",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Copy Action
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("DezhAi Response", content)
                            clipboard.setPrimaryClip(clip)
                            copied = true
                            Toast.makeText(context, "متن کپی شد", Toast.LENGTH_SHORT).show()
                            scope.launch {
                                delay(2000)
                                copied = false
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = if (copied) ChatGptEmerald else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThinkingBlock(thinkContent: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, DezhAiPurple.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Thinking",
                    tint = DezhAiPurple,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "فرایند تفکر عمیق (DeepSeek Reasoning)",
                    color = DezhAiPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Text(
                text = thinkContent.trim(),
                color = TextGray,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun RenderMarkdownContent(text: String) {
    // Split text into code blocks and prose
    val segments = text.split("```")

    for (i in segments.indices) {
        if (i % 2 == 0) {
            // Normal prose text
            if (segments[i].isNotBlank()) {
                Text(
                    text = segments[i],
                    color = TextWhite,
                    fontSize = 15.sp,
                    lineHeight = 23.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        } else {
            // Code block
            val codeRaw = segments[i]
            val firstLineBreak = codeRaw.indexOf('\n')
            val lang = if (firstLineBreak > 0) codeRaw.substring(0, firstLineBreak).trim() else "code"
            val codeBody = if (firstLineBreak > 0) codeRaw.substring(firstLineBreak + 1) else codeRaw

            CodeBlock(language = lang.ifBlank { "code" }, code = codeBody.trimEnd())
        }
    }
}

@Composable
fun CodeBlock(language: String, code: String) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CodeBg)
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(8.dp))
    ) {
        // Code Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CodeHeaderBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = language.lowercase(),
                color = TextGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code", code)
                        clipboard.setPrimaryClip(clip)
                        copied = true
                        Toast.makeText(context, "کد کپی شد", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            delay(2000)
                            copied = false
                        }
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                    contentDescription = "Copy Code",
                    tint = if (copied) ChatGptEmerald else TextGray,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (copied) "کپی شد" else "کپی کد",
                    color = if (copied) ChatGptEmerald else TextGray,
                    fontSize = 11.sp
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFD4D4D4),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 19.sp
            )
        }
    }
}

private fun parseThinkingTags(raw: String): Pair<String, String> {
    val startTag = "<think>"
    val endTag = "</think>"
    val startIndex = raw.indexOf(startTag)
    val endIndex = raw.indexOf(endTag)

    if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
        val think = raw.substring(startIndex + startTag.length, endIndex).trim()
        val rest = raw.substring(0, startIndex) + raw.substring(endIndex + endTag.length)
        return Pair(think, rest.trim())
    }
    return Pair("", raw)
}

@Composable
fun WebSourcesSection(sources: List<WebSearchResult>, searchQuery: String?) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, ChatGptEmerald.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Sources",
                    tint = ChatGptEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "منابع وب (${sources.size} نتیجه آنلاین)",
                    color = ChatGptEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sources.forEach { source ->
                    SourceCard(
                        source = source,
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "خطا در باز کردن لینک: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SourceCard(source: WebSearchResult, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .border(1.dp, Color(0xFF383838), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = source.domain,
                color = DezhAiCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open",
                tint = TextMuted,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = source.title,
            color = TextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            lineHeight = 15.sp
        )
        if (source.snippet.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = source.snippet,
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}
