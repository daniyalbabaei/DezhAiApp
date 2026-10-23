package com.example.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.components.ChatDrawer
import com.example.ui.components.ChatInputArea
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ChatTopBar
import com.example.ui.components.EmptyChatView
import com.example.ui.components.ExportDocumentDialog
import com.example.ui.components.GgufManagerDialog
import com.example.ui.components.HtmlPreviewDialog
import com.example.ui.components.ModelQuickSettingsDialog
import com.example.ui.components.ProjectDialog
import com.example.ui.theme.DarkBg
import com.example.ui.viewmodel.DezhAiViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun ChatScreen(
    viewModel: DezhAiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val models by viewModel.allModels.collectAsStateWithLifecycle()

    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val currentProjectId by viewModel.currentProjectId.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val streamingText by viewModel.streamingText.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isWebSearchEnabled by viewModel.isWebSearchEnabled.collectAsStateWithLifecycle()
    val isSearchingWeb by viewModel.isSearchingWeb.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showModelManager by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showProjectDialog by remember { mutableStateOf(false) }
    var previewHtmlCode by remember { mutableStateOf<String?>(null) }
    var exportContent by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // Handle back press when drawer is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    // Auto scroll down when messages arrive or streaming text updates
    LaunchedEffect(currentMessages.size, streamingText) {
        if (currentMessages.isNotEmpty() || streamingText.isNotEmpty()) {
            val totalItems = currentMessages.size + (if (isGenerating && streamingText.isNotEmpty()) 1 else 0)
            if (totalItems > 0) {
                listState.animateScrollToItem(totalItems - 1)
            }
        }
    }

    // Attachment text file launcher
    val attachFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = BufferedReader(InputStreamReader(stream)).readText()
                    inputText = if (inputText.isNotBlank()) {
                        "$inputText\n\n[ضمیمه فایل]:\n${text.take(3000)}"
                    } else {
                        "[ضمیمه فایل]:\n${text.take(3000)}"
                    }
                    Toast.makeText(context, "محتوای فایل به متن اضافه شد", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در خواندن فایل: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                sessions = sessions,
                projects = projects,
                activeSessionId = currentSessionId,
                activeProjectId = currentProjectId,
                activeModel = selectedModel,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onSelectSession = { viewModel.selectSession(it) },
                onSelectProject = { viewModel.selectProject(it) },
                onNewChat = { viewModel.createNewChat() },
                onOpenNewProjectDialog = { showProjectDialog = true },
                onOpenModelManager = { showModelManager = true },
                onDeleteSession = { viewModel.deleteSession(it) },
                onRenameSession = { id, title -> viewModel.renameSession(id, title) },
                onDeleteProject = { viewModel.deleteProject(it) },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                ChatTopBar(
                    activeModel = selectedModel,
                    allModels = models,
                    activeProject = currentProject,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNewChat = { viewModel.createNewChat() },
                    onSelectModel = { viewModel.selectModel(it) },
                    onOpenModelManager = { showModelManager = true },
                    onOpenQuickSettings = { showQuickSettings = true },
                    onClearChat = { viewModel.clearChat() }
                )
            },
            bottomBar = {
                ChatInputArea(
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    isGenerating = isGenerating,
                    isWebSearchEnabled = isWebSearchEnabled,
                    isSearchingWeb = isSearchingWeb,
                    activeModel = selectedModel,
                    onSendMessage = { text ->
                        viewModel.sendMessage(text)
                        inputText = ""
                    },
                    onStopGeneration = { viewModel.stopGeneration() },
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    onOpenModelManager = { showModelManager = true },
                    onAttachFile = { attachFileLauncher.launch("*/*") }
                )
            },
            containerColor = DarkBg,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(DarkBg)
            ) {
                if (currentMessages.isEmpty() && !isGenerating) {
                    EmptyChatView(
                        activeModel = selectedModel,
                        activeProject = currentProject,
                        onSelectPrompt = { prompt ->
                            inputText = prompt
                            viewModel.sendMessage(prompt)
                            inputText = ""
                        },
                        onOpenModelManager = { showModelManager = true },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(currentMessages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                onPreviewHtml = { code -> previewHtmlCode = code },
                                onExportDocument = { content -> exportContent = content }
                            )
                        }

                        // Live streaming assistant response
                        if (isGenerating && streamingText.isNotEmpty()) {
                            item(key = "streaming_response") {
                                ChatMessageItem(
                                    message = ChatMessageEntity(
                                        sessionId = currentSessionId ?: 0,
                                        role = "assistant",
                                        content = streamingText
                                    ),
                                    isStreaming = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Quick Model & Context Settings Dialog
    if (showQuickSettings && selectedModel != null) {
        ModelQuickSettingsDialog(
            model = selectedModel!!,
            onSaveModel = { viewModel.updateModel(it) },
            onDismiss = { showQuickSettings = false }
        )
    }

    // Live HTML Preview Dialog
    previewHtmlCode?.let { html ->
        HtmlPreviewDialog(
            htmlCode = html,
            onDismiss = { previewHtmlCode = null }
        )
    }

    // Export Document Dialog (PDF, Word, Slides)
    exportContent?.let { content ->
        val title = currentSession?.title ?: "سند DezhAi"
        ExportDocumentDialog(
            documentTitle = title,
            content = content,
            onDismiss = { exportContent = null }
        )
    }

    // GGUF Model Manager Dialog
    if (showModelManager) {
        GgufManagerDialog(
            models = models,
            activeModel = selectedModel,
            onSelectModel = { viewModel.selectModel(it) },
            onImportGgufFile = { uri -> viewModel.importGgufFile(uri) },
            onUpdateModel = { updated -> viewModel.updateModel(updated) },
            onDeleteModel = { deleted -> viewModel.deleteModel(deleted) },
            onDismiss = { showModelManager = false }
        )
    }

    // Project Dialog
    if (showProjectDialog) {
        ProjectDialog(
            onSaveProject = { name, desc, prompt, color ->
                viewModel.createProject(name, desc, prompt, color)
            },
            onDismiss = { showProjectDialog = false }
        )
    }
}
