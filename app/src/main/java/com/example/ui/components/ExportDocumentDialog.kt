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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.export.DocumentGenerator
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
fun ExportDocumentDialog(
    documentTitle: String,
    content: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                            .background(ChatGptEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = ChatGptEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ساخت و خروجی سند",
                            color = TextWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تبدیل پاسخ DezhAi به قالب‌های استاندارد اداری و ارائه",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Export Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. PDF Document
                ExportOptionCard(
                    icon = Icons.Default.PictureAsPdf,
                    iconColor = Color(0xFFEF4444),
                    title = "سند قابل چاپ PDF (.pdf)",
                    description = "صفحه‌بندی استاندارد A4، سربرگ اختصاصی DezhAi و شماره‌گذاری صفحات",
                    badge = "پیشنهادی",
                    onClick = {
                        try {
                            val pdfFile = DocumentGenerator.createPdf(context, documentTitle, content)
                            DocumentGenerator.shareFile(context, pdfFile, "application/pdf", documentTitle)
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در تولید PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 2. Microsoft Word Document
                ExportOptionCard(
                    icon = Icons.Default.Description,
                    iconColor = Color(0xFF3B82F6),
                    title = "سند مایکروسافت ورد Word (.doc)",
                    description = "قالب‌بندی کامل متون، جداول، کدهای برنامه‌نویسی و سازگار با آفیس و Google Docs",
                    badge = "DOC",
                    onClick = {
                        try {
                            val wordFile = DocumentGenerator.createWordDoc(context, documentTitle, content)
                            DocumentGenerator.shareFile(context, wordFile, "application/msword", documentTitle)
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در تولید سند ورد: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 3. PowerPoint / Slides Presentation
                ExportOptionCard(
                    icon = Icons.Default.Slideshow,
                    iconColor = Color(0xFFF59E0B),
                    title = "ارائه اسلاید و پاورپوینت (.slides / PPT)",
                    description = "تبدیل خودکار سرفصل‌ها به اسلایدهای تعاملی و شیک با کنترل‌های اسلاید شو",
                    badge = "اسلایدها",
                    onClick = {
                        try {
                            val presentationFile = DocumentGenerator.createPresentation(context, documentTitle, content)
                            DocumentGenerator.shareFile(context, presentationFile, "text/html", documentTitle)
                            onDismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "خطا در ساخت اسلایدها: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ExportOptionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    badge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(iconColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = iconColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
