package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.GgufModelEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.repository.DezhAiRepository
import com.example.engine.GgufEngine
import com.example.network.WebSearchEngine
import com.example.network.WebSearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TelemetryInfo(
    val tokensPerSecond: Double = 0.0,
    val tokenCount: Int = 0,
    val latencyMs: Long = 0
)

class DezhAiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = DezhAiRepository(database)
    private val ggufEngine = GgufEngine(application)
    private val webSearchEngine = WebSearchEngine()

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allModels: StateFlow<List<GgufModelEntity>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    val currentProjectId: StateFlow<Long?> = _currentProjectId.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentMessages.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isWebSearchEnabled = MutableStateFlow(true)
    val isWebSearchEnabled: StateFlow<Boolean> = _isWebSearchEnabled.asStateFlow()

    private val _isSearchingWeb = MutableStateFlow(false)
    val isSearchingWeb: StateFlow<Boolean> = _isSearchingWeb.asStateFlow()

    private val _currentTelemetry = MutableStateFlow(TelemetryInfo())
    val currentTelemetry: StateFlow<TelemetryInfo> = _currentTelemetry.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var activeJob: Job? = null
    private var messagesJob: Job? = null

    val selectedModel: StateFlow<GgufModelEntity?> = allModels
        .combine(_currentSessionId) { models, _ ->
            models.find { it.isSelected } ?: models.firstOrNull()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentProject: StateFlow<ProjectEntity?> = allProjects
        .combine(_currentProjectId) { projects, projId ->
            projects.find { it.id == projId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentSession: StateFlow<ChatSessionEntity?> = allSessions
        .combine(_currentSessionId) { sessions, sessId ->
            sessions.find { it.id == sessId }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Auto-select latest or first session when initialized
        viewModelScope.launch {
            val sessions = repository.allSessions.first()
            if (sessions.isNotEmpty()) {
                selectSession(sessions.first().id)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        observeSessionMessages(sessionId)
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            if (session != null) {
                _currentProjectId.value = session.projectId
            }
        }
    }

    private fun observeSessionMessages(sessionId: Long) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { list ->
                _currentMessages.value = list
            }
        }
    }

    fun createNewChat(projectId: Long? = _currentProjectId.value) {
        viewModelScope.launch {
            val activeModel = repository.getSelectedModel()
            val newSession = ChatSessionEntity(
                title = "چت جدید",
                projectId = projectId,
                modelId = activeModel?.id
            )
            val newId = repository.createSession(newSession)
            _currentProjectId.value = projectId
            selectSession(newId)
        }
    }

    fun selectProject(projectId: Long?) {
        _currentProjectId.value = projectId
        // Find if this project has sessions
        viewModelScope.launch {
            if (projectId != null) {
                val sessions = repository.getSessionsForProject(projectId).first()
                if (sessions.isNotEmpty()) {
                    selectSession(sessions.first().id)
                } else {
                    createNewChat(projectId)
                }
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = repository.allSessions.first()
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().id)
                } else {
                    _currentSessionId.value = null
                    _currentMessages.value = emptyList()
                }
            }
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun clearChat() {
        val sId = _currentSessionId.value ?: return
        viewModelScope.launch {
            repository.clearMessages(sId)
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        viewModelScope.launch {
            var sessionId = _currentSessionId.value
            if (sessionId == null) {
                val activeModel = repository.getSelectedModel()
                val session = ChatSessionEntity(
                    title = trimmed.take(30),
                    projectId = _currentProjectId.value,
                    modelId = activeModel?.id
                )
                sessionId = repository.createSession(session)
                selectSession(sessionId)
            } else {
                // If title was default "چت جدید", update it to prompt
                val session = repository.getSessionById(sessionId)
                if (session != null && (session.title == "چت جدید" || session.title.isBlank())) {
                    repository.updateSessionTitle(sessionId, trimmed.take(35))
                }
            }

            // Save User message
            val userMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "user",
                content = trimmed
            )
            repository.addMessage(userMsg)

            // Start generation
            val model = repository.getSelectedModel() ?: allModels.value.firstOrNull() ?: GgufModelEntity(
                name = "DezhAi Default",
                filename = "default.gguf"
            )
            val project = _currentProjectId.value?.let { repository.getProjectById(it) }

            _isGenerating.value = true
            _streamingText.value = ""
            val startTime = System.currentTimeMillis()

            activeJob = viewModelScope.launch {
                try {
                    var webSources: List<WebSearchResult> = emptyList()
                    if (_isWebSearchEnabled.value) {
                        _isSearchingWeb.value = true
                        try {
                            webSources = webSearchEngine.search(trimmed)
                        } catch (_: Exception) {
                            webSources = emptyList()
                        }
                        _isSearchingWeb.value = false
                    }

                    val history = _currentMessages.value
                    val streamFlow = ggufEngine.streamInference(
                        model = model,
                        project = project,
                        conversationHistory = history,
                        userPrompt = trimmed,
                        webSearchResults = webSources
                    )

                    val buffer = StringBuilder()
                    streamFlow.collect { token ->
                        buffer.append(token)
                        _streamingText.value = buffer.toString()
                    }

                    val totalTimeMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
                    val generated = buffer.toString()
                    val tokenCount = (generated.length / 3.8).toInt().coerceAtLeast(1)
                    val tokensPerSec = (tokenCount.toDouble() / (totalTimeMs / 1000.0) * 10.0).toInt() / 10.0

                    _currentTelemetry.value = TelemetryInfo(
                        tokensPerSecond = tokensPerSec,
                        tokenCount = tokenCount,
                        latencyMs = totalTimeMs
                    )

                    // Insert assistant response with web sources if searched
                    val assistantMsg = ChatMessageEntity(
                        sessionId = sessionId,
                        role = "assistant",
                        content = generated,
                        tokensPerSecond = tokensPerSec,
                        tokenCount = tokenCount,
                        latencyMs = totalTimeMs,
                        searchQuery = if (webSources.isNotEmpty()) trimmed else null,
                        webSourcesJson = if (webSources.isNotEmpty()) WebSearchEngine.toJson(webSources) else null
                    )
                    repository.addMessage(assistantMsg)
                } catch (_: Exception) {
                    // Canceled or error
                    if (_streamingText.value.isNotBlank()) {
                        val assistantMsg = ChatMessageEntity(
                            sessionId = sessionId,
                            role = "assistant",
                            content = _streamingText.value + " [توقف تولید]"
                        )
                        repository.addMessage(assistantMsg)
                    }
                } finally {
                    _isSearchingWeb.value = false
                    _isGenerating.value = false
                    _streamingText.value = ""
                }
            }
        }
    }

    fun toggleWebSearch() {
        _isWebSearchEnabled.value = !_isWebSearchEnabled.value
    }

    fun stopGeneration() {
        activeJob?.cancel()
        _isGenerating.value = false
    }

    // Projects CRUD
    fun createProject(name: String, description: String, systemPrompt: String, colorHex: String) {
        viewModelScope.launch {
            val project = ProjectEntity(
                name = name,
                description = description,
                systemPrompt = systemPrompt,
                colorHex = colorHex
            )
            val newId = repository.createProject(project)
            selectProject(newId)
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (_currentProjectId.value == project.id) {
                _currentProjectId.value = null
            }
        }
    }

    // GGUF Models
    fun selectModel(modelId: Long) {
        viewModelScope.launch {
            repository.selectModel(modelId)
        }
    }

    fun updateModel(model: GgufModelEntity) {
        viewModelScope.launch {
            repository.updateModel(model)
        }
    }

    fun deleteModel(model: GgufModelEntity) {
        viewModelScope.launch {
            repository.deleteModel(model)
        }
    }

    fun importGgufFile(uri: Uri) {
        viewModelScope.launch {
            val meta = ggufEngine.extractGgufMetadata(uri)
            val newModel = GgufModelEntity(
                name = meta.displayName,
                filename = meta.filename,
                uriString = uri.toString(),
                fileSizeMb = meta.fileSizeMb,
                architecture = meta.architecture,
                quantType = meta.quantType,
                isImported = true,
                isSelected = true
            )
            val id = repository.insertModel(newModel)
            repository.selectModel(id)
        }
    }
}
