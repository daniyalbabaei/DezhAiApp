package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GgufModelEntity
import com.example.ui.theme.ChatGptEmerald
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DezhAiCyan
import com.example.ui.theme.DezhAiPurple
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelQuickSettingsDialog(
    model: GgufModelEntity,
    onSaveModel: (GgufModelEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var temperature by remember(model.id) { mutableFloatStateOf(model.temperature) }
    var topP by remember(model.id) { mutableFloatStateOf(model.topP) }
    var contextLength by remember(model.id) { mutableIntStateOf(model.contextLength) }
    var threads by remember(model.id) { mutableIntStateOf(model.nThreads) }
    var endpointUrl by remember(model.id) { mutableStateOf(model.customEndpointUrl ?: "") }

    val contextPresets = listOf(2048, 4096, 8192, 16384)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBg,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DezhAiCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = DezhAiCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تنظیمات سریع مدل GGUF",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ChatGptEmerald.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = model.quantType,
                                    color = ChatGptEmerald,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = model.name,
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Context Window Selector
            Text(
                text = "طول پنجره کانتکست (Context Size):",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                contextPresets.forEach { ctx ->
                    val isSelected = contextLength == ctx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ChatGptEmerald else DarkSurface)
                            .border(1.dp, if (isSelected) ChatGptEmerald else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { contextLength = ctx }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${ctx / 1024}K",
                                color = if (isSelected) Color.White else TextWhite,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$ctx",
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Temperature Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("دما / خلاقیت (Temperature):", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = String.format("%.2f", temperature) + when {
                        temperature < 0.3f -> " (بسیار دقیق و متمرکز)"
                        temperature < 0.8f -> " (متعادل)"
                        else -> " (خلاقانه و متنوع)"
                    },
                    color = ChatGptEmerald,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0.0f..1.5f,
                colors = SliderDefaults.colors(
                    thumbColor = ChatGptEmerald,
                    activeTrackColor = ChatGptEmerald,
                    inactiveTrackColor = DarkBorder
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Top-P Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("تنوع نمونه‌برداری (Top-P):", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(String.format("%.2f", topP), color = DezhAiCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = topP,
                onValueChange = { topP = it },
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = DezhAiCyan,
                    activeTrackColor = DezhAiCyan,
                    inactiveTrackColor = DarkBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 4. CPU Threads
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("رشته‌های پردازشی پردازنده (CPU Threads):", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("تعداد هسته‌های موازی برای شتاب‌دهی استنتاج", color = TextMuted, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "−",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { if (threads > 1) threads-- }
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "$threads",
                        color = ChatGptEmerald,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "+",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { if (threads < 8) threads++ }
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Local Server Endpoint URL
            Text("آدرس سرور محلی (Ollama / llama.cpp):", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = endpointUrl,
                onValueChange = { endpointUrl = it },
                placeholder = { Text("اختیاری: http://localhost:11434/api/generate", color = TextMuted, fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = ChatGptEmerald,
                    unfocusedBorderColor = DarkBorder
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Save Button
            Button(
                onClick = {
                    val updated = model.copy(
                        temperature = temperature,
                        topP = topP,
                        contextLength = contextLength,
                        nThreads = threads,
                        customEndpointUrl = endpointUrl.ifBlank { null }
                    )
                    onSaveModel(updated)
                    Toast.makeText(context, "تنظیمات مدل با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ChatGptEmerald),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("اعمال و ذخیره تنظیمات", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
