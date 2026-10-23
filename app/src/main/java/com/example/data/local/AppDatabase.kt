package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.ChatSessionDao
import com.example.data.local.dao.GgufModelDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.GgufModelEntity
import com.example.data.local.entity.ProjectEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        GgufModelEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun ggufModelDao(): GgufModelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dezhai_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            suspend fun populateInitialData(database: AppDatabase) {
                val modelDao = database.ggufModelDao()
                val projectDao = database.projectDao()
                val sessionDao = database.chatSessionDao()
                val messageDao = database.chatMessageDao()

                // Default GGUF Models
                val defaultModels = listOf(
                    GgufModelEntity(
                        name = "Llama-3.2-3B-Instruct",
                        filename = "Llama-3.2-3B-Instruct-Q4_K_M.gguf",
                        fileSizeMb = 1920.0,
                        architecture = "Llama",
                        quantType = "Q4_K_M",
                        isImported = false,
                        isSelected = true,
                        temperature = 0.7f,
                        topP = 0.9f,
                        contextLength = 4096,
                        nThreads = 4
                    ),
                    GgufModelEntity(
                        name = "Qwen2.5-Coder-1.5B",
                        filename = "Qwen2.5-Coder-1.5B-Instruct-Q4_K_M.gguf",
                        fileSizeMb = 1040.0,
                        architecture = "Qwen",
                        quantType = "Q4_K_M",
                        isImported = false,
                        isSelected = false,
                        temperature = 0.3f,
                        topP = 0.85f,
                        contextLength = 8192,
                        nThreads = 4
                    ),
                    GgufModelEntity(
                        name = "DeepSeek-R1-Distill-1.5B",
                        filename = "DeepSeek-R1-Distill-Q4_K_M.gguf",
                        fileSizeMb = 1120.0,
                        architecture = "DeepSeek",
                        quantType = "Q4_K_M",
                        isImported = false,
                        isSelected = false,
                        temperature = 0.6f,
                        topP = 0.95f,
                        contextLength = 4096,
                        nThreads = 4
                    ),
                    GgufModelEntity(
                        name = "Mistral-7B-Instruct-v0.3",
                        filename = "Mistral-7B-Instruct-v0.3-Q4_K_M.gguf",
                        fileSizeMb = 4150.0,
                        architecture = "Mistral",
                        quantType = "Q4_K_M",
                        isImported = false,
                        isSelected = false,
                        temperature = 0.7f,
                        topP = 0.9f,
                        contextLength = 4096,
                        nThreads = 4
                    )
                )
                modelDao.insertAll(defaultModels)

                // Default Project
                val defaultProjectId = projectDao.insertProject(
                    ProjectEntity(
                        name = "Dezh Workspace",
                        description = "فضای کاری پیش‌فرض دژ برای توسعه نرم‌افزار، تولید محتوا و هوش مصنوعی",
                        systemPrompt = "You are DezhAi, an intelligent, precise and helpful AI assistant running on a local GGUF engine. Answer in the user's preferred language (Persian or English). Be concise, structured, and deliver well-commented code when asked.",
                        colorHex = "#10A37F",
                        iconName = "shield"
                    )
                )

                // Default Starter Chat
                val starterSessionId = sessionDao.insertSession(
                    ChatSessionEntity(
                        title = "معرفی و راهنمای مدل‌های GGUF",
                        projectId = defaultProjectId,
                        modelId = 1L
                    )
                )

                messageDao.insertMessage(
                    ChatMessageEntity(
                        sessionId = starterSessionId,
                        role = "user",
                        content = "سلام DezhAi! قابلیت‌های این برنامه چیست و چطور می‌توانم مدل GGUF خودم را اضافه کنم؟"
                    )
                )

                messageDao.insertMessage(
                    ChatMessageEntity(
                        sessionId = starterSessionId,
                        role = "assistant",
                        content = """سلام! به **DezhAi** خوش آمدید 🛡️

من یک دستیار هوش مصنوعی با ظاهر مدرن مشابه ChatGPT هستم که امکان اجرای مدل‌های **GGUF** را مستقیماً روی دستگاه یا اتصال به سرورهای محلی به شما می‌دهد.

### قابلیت‌های کلیدی:
1. **مدیریت مدل‌های GGUF**:
   - می‌توانید از منوی بالا یا کشوی کناری گزینه **«مدیریت مدل‌های GGUF»** را انتخاب کنید و فایل‌های `.gguf` خود را با کلیک روی **«بارگذاری فایل GGUF»** اضافه کنید.
   - تنظیم پارامترهای مدل مانند **دما (Temperature)**، **Top-P**، **طول پنجره محتوا (Context Size)** و **تعداد رشته‌های پردازشی (Threads)**.
   - امکان اتصال به سرور محلی (Local Server) مانند Ollama یا `llama.cpp server` (مثلاً `http://localhost:11434` یا `http://10.0.2.2:8080`).

2. **پروژه‌ها (Projects)**:
   - ساخت فضاهای کاری مجزا با دستورالعمل‌های سیستمی اختصاصی و متصل به مدل‌های خاص.

3. **ذخیره‌سازی هوشمند پایگاه داده (Room)**:
   - تمام چت‌ها، پیام‌ها، مدل‌ها و پروژه‌ها به‌صورت دائمی روی دستگاه ذخیره می‌شوند.

چطور می‌توانم در پروژه یا سوال بعدی کمکتان کنم؟""",
                        tokensPerSecond = 24.6,
                        tokenCount = 285,
                        latencyMs = 320
                    )
                )
            }
        }
    }
}
