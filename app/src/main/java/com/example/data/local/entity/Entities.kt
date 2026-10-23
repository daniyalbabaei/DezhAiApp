package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a workspace/project (e.g. Code project, Research, Persian Writing)
 * with custom system instructions, attached context, and an assigned GGUF model.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val systemPrompt: String = "",
    val defaultModelId: Long? = null,
    val colorHex: String = "#10A37F",
    val iconName: String = "folder",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Represents a chat conversation session, optionally grouped under a Project.
 */
@Entity(
    tableName = "chat_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ChatSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val projectId: Long? = null,
    val modelId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents an individual message in a chat session.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val tokensPerSecond: Double = 0.0,
    val tokenCount: Int = 0,
    val latencyMs: Long = 0,
    val isThinking: Boolean = false,
    val searchQuery: String? = null,
    val webSourcesJson: String? = null
)

/**
 * Represents a GGUF model that can be loaded, selected, and tuned.
 * Supports both on-device imported .gguf files and built-in optimized presets,
 * as well as optional local server endpoints (Ollama / llama.cpp server).
 */
@Entity(tableName = "gguf_models")
data class GgufModelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val filename: String,
    val uriString: String? = null,
    val fileSizeMb: Double = 0.0,
    val architecture: String = "Llama", // "Llama", "Qwen", "Mistral", "DeepSeek", "Gemma"
    val quantType: String = "Q4_K_M", // "Q4_K_M", "Q5_K_M", "Q8_0", "FP16"
    val isImported: Boolean = false,
    val isSelected: Boolean = false,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val contextLength: Int = 4096,
    val nThreads: Int = 4,
    val customEndpointUrl: String? = null // e.g. http://10.0.2.2:11434/api/generate or http://localhost:8080/completion
)
