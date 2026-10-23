package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.GgufModelEntity
import com.example.data.local.entity.ProjectEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DezhAiRepository(private val database: AppDatabase) {

    private val projectDao = database.projectDao()
    private val sessionDao = database.chatSessionDao()
    private val messageDao = database.chatMessageDao()
    private val modelDao = database.ggufModelDao()

    // Projects
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: Long): ProjectEntity? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)
    }

    suspend fun createProject(project: ProjectEntity): Long = withContext(Dispatchers.IO) {
        projectDao.insertProject(project)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.deleteProject(project)
    }

    // Sessions
    val allSessions: Flow<List<ChatSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsForProject(projectId: Long): Flow<List<ChatSessionEntity>> {
        return sessionDao.getSessionsForProject(projectId)
    }

    suspend fun getSessionById(id: Long): ChatSessionEntity? = withContext(Dispatchers.IO) {
        sessionDao.getSessionById(id)
    }

    suspend fun createSession(session: ChatSessionEntity): Long = withContext(Dispatchers.IO) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(id: Long) = withContext(Dispatchers.IO) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun updateSessionTitle(id: Long, title: String) = withContext(Dispatchers.IO) {
        sessionDao.updateSessionTitle(id, title)
    }

    // Messages
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>> {
        return messageDao.getMessagesForSession(sessionId)
    }

    suspend fun addMessage(message: ChatMessageEntity): Long = withContext(Dispatchers.IO) {
        // Also update session updatedAt timestamp
        val session = sessionDao.getSessionById(message.sessionId)
        if (session != null) {
            sessionDao.updateSession(session.copy(updatedAt = System.currentTimeMillis()))
        }
        messageDao.insertMessage(message)
    }

    suspend fun clearMessages(sessionId: Long) = withContext(Dispatchers.IO) {
        messageDao.clearMessagesForSession(sessionId)
    }

    // GGUF Models
    val allModels: Flow<List<GgufModelEntity>> = modelDao.getAllModels()

    suspend fun getSelectedModel(): GgufModelEntity? = withContext(Dispatchers.IO) {
        modelDao.getSelectedModel()
    }

    suspend fun getModelById(id: Long): GgufModelEntity? = withContext(Dispatchers.IO) {
        modelDao.getModelById(id)
    }

    suspend fun insertModel(model: GgufModelEntity): Long = withContext(Dispatchers.IO) {
        modelDao.insertModel(model)
    }

    suspend fun updateModel(model: GgufModelEntity) = withContext(Dispatchers.IO) {
        modelDao.updateModel(model)
    }

    suspend fun deleteModel(model: GgufModelEntity) = withContext(Dispatchers.IO) {
        modelDao.deleteModel(model)
    }

    suspend fun selectModel(id: Long) = withContext(Dispatchers.IO) {
        modelDao.deselectAllModels()
        modelDao.setSelectedModel(id)
    }
}
