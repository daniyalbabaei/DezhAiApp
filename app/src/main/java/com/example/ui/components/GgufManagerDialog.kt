package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GgufModelEntity
import com.example.ui.theme.ChatGptEmerald
import com.example.ui.theme.CodeBg
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DezhAiCyan
import com.example.ui.theme.DezhAiPurple
import com.example.ui.theme.DezhAiTeal
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GgufManagerDialog(
    models: List<GgufModelEntity>,
    activeModel: GgufModelEntity?,
    onSelectModel: (Long) -> Unit,
    onImportGgufFile: (Uri) -> Unit,
    onUpdateModel: (GgufModelEntity) -> Unit,
    onDeleteModel: (GgufModelEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedForEditing by remember(activeModel) { mutableStateOf(activeModel ?: models.firstOrNull()) }

    // File picker launcher for importing .gguf files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportGgufFile(uri)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBg,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
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
                            .background(ChatGptEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = ChatGptEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مدیریت مدل‌های GGUF",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "بارگذاری، پیکربندی و انتخاب موتور هوش مصنوعی",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action: Import GGUF File Button
            Button(
                onClick = {
                    // Accept all files or .gguf MIME type
                    filePickerLauncher.launch(arrayOf("*/*"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("import_gguf_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChatGptEmerald),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Upload GGUF",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بارگذاری فایل جدید GGUF از دستگاه (.gguf)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scrollable Content
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "مدل‌های موجود:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(models, key = { it.id }) { model ->
                    val isSelected = model.id == activeModel?.id
                    val isEditingThis = selectedForEditing?.id == model.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEditingThis) DarkSurfaceVariant else DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ChatGptEmerald else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedForEditing = model }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = model.name,
                                            color = TextWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (model.isImported) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DezhAiPurple.copy(alpha = 0.3f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "وارد شده",
                                                    color = DezhAiPurple,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${model.architecture} • ${model.quantType} • ${if (model.fileSizeMb > 0) String.format("%.0f MB", model.fileSizeMb) else "نهفته"} • ${model.contextLength} ctx",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ChatGptEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "فعال",
                                            color = ChatGptEmerald,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onSelectModel(model.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text("انتخاب", fontSize = 11.sp, color = TextWhite)
                                    }
                                }
                            }
                        }
                    }
                }

                // Model Tuner Card for selectedForEditing
                selectedForEditing?.let { model ->
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        ModelTunerSection(
                            model = model,
                            onSave = { updated ->
                                onUpdateModel(updated)
                                selectedForEditing = updated
                            },
                            onDelete = {
                                onDeleteModel(model)
                                selectedForEditing = models.firstOrNull { it.id != model.id }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelTunerSection(
    model: GgufModelEntity,
    onSave: (GgufModelEntity) -> Unit,
    onDelete: () -> Unit
) {
    var temperature by remember(model.id) { mutableFloatStateOf(model.temperature) }
    var topP by remember(model.id) { mutableFloatStateOf(model.topP) }
    var threads by remember(model.id) { mutableIntStateOf(model.nThreads) }
    var contextLength by remember(model.id) { mutableIntStateOf(model.contextLength) }
    var endpointUrl by remember(model.id) { mutableStateOf(model.customEndpointUrl ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = DezhAiCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تنظیمات پارامترهای ${model.name.take(16)}",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (model.isImported) {
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Model",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Temperature Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("دما (Temperature):", color = TextGray, fontSize = 12.sp)
            Text(String.format("%.2f", temperature), color = ChatGptEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

        // Top-P Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("احتمال نمونه‌برداری (Top-P):", color = TextGray, fontSize = 12.sp)
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

        // Threads & Context Length
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Threads
            Column(modifier = Modifier.weight(1f)) {
                Text("رشته‌ها (CPU Threads):", color = TextGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$threads رشته", color = TextWhite, fontSize = 12.sp)
                    Row {
                        Text(
                            text = "−",
                            color = TextWhite,
                            modifier = Modifier
                                .clickable { if (threads > 1) threads-- }
                                .padding(horizontal = 6.dp)
                        )
                        Text(
                            text = "+",
                            color = TextWhite,
                            modifier = Modifier
                                .clickable { if (threads < 8) threads++ }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }

            // Context Window
            Column(modifier = Modifier.weight(1f)) {
                Text("طول پنجره کانتکست:", color = TextGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$contextLength tokens", color = TextWhite, fontSize = 12.sp)
                    Text(
                        text = "تغییر",
                        color = ChatGptEmerald,
                        fontSize = 11.sp,
                        modifier = Modifier.clickable {
                            contextLength = when (contextLength) {
                                2048 -> 4096
                                4096 -> 8192
                                else -> 2048
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Local Server Endpoint URL
        Text("آدرس سرور محلی (Ollama / llama.cpp server):", color = TextGray, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = endpointUrl,
            onValueChange = { endpointUrl = it },
            placeholder = { Text("مثال: http://localhost:11434/api/generate", color = TextMuted, fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = ChatGptEmerald,
                unfocusedBorderColor = DarkBorder
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Save Button
        Button(
            onClick = {
                onSave(
                    model.copy(
                        temperature = temperature,
                        topP = topP,
                        nThreads = threads,
                        contextLength = contextLength,
                        customEndpointUrl = endpointUrl.ifBlank { null }
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = ChatGptEmerald),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ذخیره تنظیمات مدل", color = Color.White, fontSize = 13.sp)
        }
    }
}
