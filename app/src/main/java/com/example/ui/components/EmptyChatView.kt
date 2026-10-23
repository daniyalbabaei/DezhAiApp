package com.example.ui.components

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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GgufModelEntity
import com.example.data.local.entity.ProjectEntity
import com.example.ui.theme.ChatGptEmerald
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DezhAiCyan
import com.example.ui.theme.DezhAiPurple
import com.example.ui.theme.DezhAiTeal
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun EmptyChatView(
    activeModel: GgufModelEntity?,
    activeProject: ProjectEntity?,
    onSelectPrompt: (String) -> Unit,
    onOpenModelManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Citadel Logo
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            ChatGptEmerald.copy(alpha = 0.9f),
                            DezhAiTeal.copy(alpha = 0.7f),
                            DarkSurface
                        )
                    )
                )
                .border(2.dp, ChatGptEmerald.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "DezhAi Shield",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DezhAi",
            color = TextWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (activeProject != null) "پروژه فعال: ${activeProject.name}" else "چطور می‌توانم به شما کمک کنم؟",
            color = TextGray,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Active GGUF Model Info Card
        if (activeModel != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                    .clickable { onOpenModelManager() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = DezhAiCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${activeModel.name} • ${activeModel.quantType} • ${activeModel.contextLength} ctx",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "⚙️ تنظیمات",
                    color = ChatGptEmerald,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4 Suggestion Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PromptCard(
                icon = Icons.Default.Language,
                iconColor = DezhAiCyan,
                title = "جستجوی زنده در وب (Web Search)",
                description = "استخراج آنلاین اطلاعات، اخبار و مقالات با ذکر منبع",
                onClick = { onSelectPrompt("جدیدترین رویدادها و اخبار فناوری هوش مصنوعی و مدل‌های زبانی را در وب جستجو کن و با منبع توضیح بده.") }
            )

            PromptCard(
                icon = Icons.Default.Code,
                iconColor = EmeraldLight,
                title = "کدنویسی و توسعه نرم‌افزار",
                description = "یک اسکریپت پایتون ناهمگام با مستندات کامل بنویس",
                onClick = { onSelectPrompt("یک اسکریپت پایتون کامل و ناهمگام (AsyncIO) برای دریافت و پردازش داده‌ها بنویس.") }
            )

            PromptCard(
                icon = Icons.Default.SettingsSuggest,
                iconColor = ChatGptEmerald,
                title = "بررسی مشخصات مدل GGUF",
                description = "نمایش پارامترها و تنظیمات بهینه مدل جاری",
                onClick = { onSelectPrompt("مشخصات مدل GGUF جاری و راهنمای بهینه‌سازی پارامترهای دما و رشته‌های پردازشی را توضیح بده.") }
            )

            PromptCard(
                icon = Icons.Default.Psychology,
                iconColor = DezhAiPurple,
                title = "تحلیل منطقی و استدلال عمیق",
                description = "مراحل گام‌به‌گام طراحی معماری یک سیستم مقیاس‌پذیر",
                onClick = { onSelectPrompt("مراحل گام‌به‌گام تفکر و طراحی معماری یک سامانه هوش مصنوعی مقیاس‌پذیر با پایگاه داده محلی را تدوین کن.") }
            )

            PromptCard(
                icon = Icons.Default.Create,
                iconColor = Color(0xFFF59E0B),
                title = "تولید محتوا و نگارش خلاقانه",
                description = "نگارش متن حرفه‌ای و معرفی نوآوری‌های هوش مصنوعی",
                onClick = { onSelectPrompt("یک متن حرفه‌ای و جذاب برای معرفی دستیار هوش مصنوعی امن و محلی DezhAi بنویس.") }
            )
        }
    }
}

@Composable
private fun PromptCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
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
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
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
