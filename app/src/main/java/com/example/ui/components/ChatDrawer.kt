package com.example.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.GgufModelEntity
import com.example.data.local.entity.ProjectEntity
import com.example.ui.theme.ChatGptEmerald
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSidebar
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DezhAiCyan
import com.example.ui.theme.DezhAiTeal
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDrawer(
    sessions: List<ChatSessionEntity>,
    projects: List<ProjectEntity>,
    activeSessionId: Long?,
    activeProjectId: Long?,
    activeModel: GgufModelEntity?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectSession: (Long) -> Unit,
    onSelectProject: (Long?) -> Unit,
    onNewChat: () -> Unit,
    onOpenNewProjectDialog: () -> Unit,
    onOpenModelManager: () -> Unit,
    onDeleteSession: (Long) -> Unit,
    onRenameSession: (Long, String) -> Unit,
    onDeleteProject: (ProjectEntity) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sessionToRename by remember { mutableStateOf<ChatSessionEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

    val filteredSessions = if (searchQuery.isBlank()) {
        sessions
    } else {
        sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    // Group sessions by date
    val groupedSessions = remember(filteredSessions) {
        groupSessionsByDate(filteredSessions)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .background(DarkSidebar)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(14.dp)
    ) {
        // App Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(ChatGptEmerald),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "DezhAi Shield",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DezhAi",
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GGUF",
                            color = DezhAiCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "دستیار هوشمند و محلی",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // New Chat Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                .clickable {
                    onNewChat()
                    onCloseDrawer()
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("drawer_new_chat_button"),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "چت جدید",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "New",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("جستجوی چت‌ها...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = ChatGptEmerald,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Scrollable List with Projects & Chats
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Projects Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "پروژه‌ها (Workspaces)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onOpenNewProjectDialog,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Project",
                            tint = ChatGptEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // All chats / No project filter option
            item {
                ProjectItemRow(
                    name = "همه چت‌ها (بدون پروژه)",
                    colorHex = "#8E8EA0",
                    isSelected = activeProjectId == null,
                    onClick = {
                        onSelectProject(null)
                    },
                    onDelete = null
                )
            }

            // Projects List
            items(projects, key = { "proj_${it.id}" }) { project ->
                ProjectItemRow(
                    name = project.name,
                    colorHex = project.colorHex,
                    isSelected = activeProjectId == project.id,
                    onClick = {
                        onSelectProject(project.id)
                    },
                    onDelete = {
                        projectToDelete = project
                    }
                )
            }

            // Chats History Header & Items
            groupedSessions.forEach { (timeGroup, sessionList) ->
                item {
                    Text(
                        text = timeGroup,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                items(sessionList, key = { "sess_${it.id}" }) { session ->
                    SessionItemRow(
                        session = session,
                        isSelected = session.id == activeSessionId,
                        onClick = {
                            onSelectSession(session.id)
                            onCloseDrawer()
                        },
                        onRename = {
                            sessionToRename = session
                            renameText = session.title
                        },
                        onDelete = {
                            onDeleteSession(session.id)
                        }
                    )
                }
            }
        }

        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

        // Bottom Bar: Active GGUF Model status & Manager
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .clickable {
                    onOpenModelManager()
                    onCloseDrawer()
                }
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DezhAiCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = DezhAiCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = activeModel?.name?.take(18) ?: "مدل GGUF انتخاب نشده",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${activeModel?.quantType ?: "Q4_K_M"} • ${activeModel?.nThreads ?: 4} Threads",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Manage Models",
                    tint = ChatGptEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Rename Dialog
    sessionToRename?.let { sess ->
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("تغییر عنوان چت", color = TextWhite) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ChatGptEmerald
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            onRenameSession(sess.id, renameText.trim())
                        }
                        sessionToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChatGptEmerald)
                ) {
                    Text("ذخیره")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Delete Project Dialog
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("حذف پروژه", color = TextWhite) },
            text = { Text("آیا از حذف پروژه «${proj.name}» مطمئن هستید؟ چت‌های این پروژه حذف نخواهند شد.", color = TextGray) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(proj)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("انصراف", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun ProjectItemRow(
    name: String,
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    var menuOpen by remember { mutableStateOf(false) }
    val parsedColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {
            ChatGptEmerald
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(parsedColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            color = if (isSelected) TextWhite else TextGray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (onDelete != null) {
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Project options",
                        tint = TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(DarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("حذف پروژه", color = Color(0xFFEF4444), fontSize = 12.sp) },
                        onClick = {
                            menuOpen = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionItemRow(
    session: ChatSessionEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            tint = if (isSelected) TextWhite else TextMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = session.title,
            color = if (isSelected) TextWhite else TextGray,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Box {
            IconButton(
                onClick = { menuOpen = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Session options",
                    tint = TextMuted,
                    modifier = Modifier.size(15.dp)
                )
            }

            DropdownMenu(
                expanded = menuOpen,
                onDismissRequest = { menuOpen = false },
                modifier = Modifier.background(DarkSurface)
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DriveFileRenameOutline,
                                contentDescription = null,
                                tint = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تغییر نام", color = TextWhite, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        menuOpen = false
                        onRename()
                    }
                )

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حذف چت", color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        menuOpen = false
                        onDelete()
                    }
                )
            }
        }
    }
}

private fun groupSessionsByDate(sessions: List<ChatSessionEntity>): Map<String, List<ChatSessionEntity>> {
    val now = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val sevenDaysMs = 7 * oneDayMs

    val today = mutableListOf<ChatSessionEntity>()
    val yesterday = mutableListOf<ChatSessionEntity>()
    val lastSevenDays = mutableListOf<ChatSessionEntity>()
    val older = mutableListOf<ChatSessionEntity>()

    sessions.forEach { session ->
        val diff = now - session.updatedAt
        when {
            diff < oneDayMs -> today.add(session)
            diff < 2 * oneDayMs -> yesterday.add(session)
            diff < sevenDaysMs -> lastSevenDays.add(session)
            else -> older.add(session)
        }
    }

    val result = mutableMapOf<String, List<ChatSessionEntity>>()
    if (today.isNotEmpty()) result["امروز (Today)"] = today
    if (yesterday.isNotEmpty()) result["دیروز (Yesterday)"] = yesterday
    if (lastSevenDays.isNotEmpty()) result["۷ روز گذشته"] = lastSevenDays
    if (older.isNotEmpty()) result["قدیمی‌تر"] = older
    return result
}
