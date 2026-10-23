package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DocumentGenerator {

    private fun getExportDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9آ-ی]+"), "_").take(25)
    }

    /**
     * Generates a paginated PDF document from Markdown/Text content using native PdfDocument.
     */
    fun createPdf(context: Context, title: String, content: String): File {
        val cleanTitle = title.ifBlank { "DezhAi_Document" }
        val filename = "DezhAi_${sanitizeFilename(cleanTitle)}_${System.currentTimeMillis()}.pdf"
        val destFile = File(getExportDir(context), filename)

        val pdfDoc = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points
        val margin = 40f
        val printableWidth = (pageWidth - (margin * 2)).toInt()

        val textPaint = TextPaint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = 12f
            isAntiAlias = true
        }

        val headingPaint = TextPaint().apply {
            color = Color.rgb(16, 163, 127) // Emerald
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val codePaint = TextPaint().apply {
            color = Color.rgb(50, 50, 50)
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }

        val metaPaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 9f
            isAntiAlias = true
        }

        val lines = content.replace("```", "\n[CODE_BLOCK]\n").lines()
        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDoc.startPage(pageInfo)
        var canvas = page.canvas

        // Draw header
        fun drawHeaderFooter(c: android.graphics.Canvas, pNum: Int) {
            val headerPaint = Paint().apply {
                color = Color.rgb(16, 163, 127)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            c.drawText("DezhAi 🛡️", margin, 35f, headerPaint)

            val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
            metaPaint.textAlign = Paint.Align.RIGHT
            c.drawText(dateStr, pageWidth - margin, 35f, metaPaint)
            metaPaint.textAlign = Paint.Align.LEFT

            val linePaint = Paint().apply {
                color = Color.rgb(220, 220, 220)
                strokeWidth = 1f
            }
            c.drawLine(margin, 45f, pageWidth - margin, 45f, linePaint)

            // Footer
            c.drawLine(margin, pageHeight - 35f, pageWidth - margin, pageHeight - 35f, linePaint)
            c.drawText("DezhAi Local GGUF Engine - خروجی معتبر سند", margin, pageHeight - 22f, metaPaint)
            metaPaint.textAlign = Paint.Align.RIGHT
            c.drawText("صفحه $pNum", pageWidth - margin, pageHeight - 22f, metaPaint)
            metaPaint.textAlign = Paint.Align.LEFT
        }

        drawHeaderFooter(canvas, pageNumber)
        var y = 65f

        // Title on first page
        val titleLayout = StaticLayout.Builder.obtain(title, 0, title.length, headingPaint, printableWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .build()
        canvas.save()
        canvas.translate(margin, y)
        titleLayout.draw(canvas)
        canvas.restore()
        y += titleLayout.height + 15f

        var inCode = false

        for (line in lines) {
            if (line.contains("[CODE_BLOCK]")) {
                inCode = !inCode
                continue
            }

            val currentPaint = when {
                inCode -> codePaint
                line.startsWith("#") -> headingPaint
                else -> textPaint
            }

            val cleanLine = if (line.startsWith("#")) line.replace(Regex("^#+\\s*"), "") else line
            if (cleanLine.isBlank()) {
                y += 8f
                continue
            }

            val layout = StaticLayout.Builder.obtain(cleanLine, 0, cleanLine.length, currentPaint, printableWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .build()

            // Check if line overflows current page
            if (y + layout.height > pageHeight - 50f) {
                pdfDoc.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDoc.startPage(pageInfo)
                canvas = page.canvas
                drawHeaderFooter(canvas, pageNumber)
                y = 60f
            }

            canvas.save()
            canvas.translate(margin, y)
            layout.draw(canvas)
            canvas.restore()
            y += layout.height + 4f
        }

        pdfDoc.finishPage(page)

        FileOutputStream(destFile).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return destFile
    }

    /**
     * Generates a Microsoft Word compliant document (.doc / HTML-Word standard).
     */
    fun createWordDoc(context: Context, title: String, content: String): File {
        val cleanTitle = title.ifBlank { "DezhAi_Word_Document" }
        val filename = "DezhAi_${sanitizeFilename(cleanTitle)}_${System.currentTimeMillis()}.doc"
        val destFile = File(getExportDir(context), filename)

        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())

        // Format markdown to HTML for Word
        var bodyHtml = content
            .replace("\n", "<br/>")
            .replace(Regex("```([a-zA-Z0-9]*)(.*?)```")) { match ->
                val lang = match.groupValues[1]
                val code = match.groupValues[2].replace("<br/>", "\n")
                "<pre style=\"background-color: #2D2D2D; color: #ECECF1; padding: 12px; border-radius: 6px; font-family: Consolas, monospace;\"><code>$code</code></pre>"
            }
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
            .replace(Regex("###\\s*(.*?)(<br/>|$)"), "<h3 style=\"color: #10A37F;\">$1</h3>")
            .replace(Regex("##\\s*(.*?)(<br/>|$)"), "<h2 style=\"color: #06B6D4;\">$1</h2>")
            .replace(Regex("#\\s*(.*?)(<br/>|$)"), "<h1 style=\"color: #10A37F;\">$1</h1>")

        val htmlDoc = """
            <!DOCTYPE html>
            <html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>
            <head>
                <meta charset="utf-8">
                <title>$title</title>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Arial, sans-serif;
                        line-height: 1.6;
                        color: #202123;
                        direction: rtl;
                        text-align: right;
                        margin: 40px;
                    }
                    .header-box {
                        border-bottom: 2px solid #10A37F;
                        padding-bottom: 10px;
                        margin-bottom: 25px;
                    }
                    .title {
                        color: #10A37F;
                        font-size: 24px;
                        font-weight: bold;
                        margin: 0;
                    }
                    .meta {
                        color: #71717A;
                        font-size: 12px;
                        margin-top: 4px;
                    }
                    pre, code {
                        direction: ltr;
                        text-align: left;
                    }
                </style>
            </head>
            <body>
                <div class="header-box">
                    <div class="title">DezhAi 🛡️ — $title</div>
                    <div class="meta">تولید شده توسط DezhAi Local GGUF Engine در تاریخ $dateStr</div>
                </div>
                <div class="content">
                    $bodyHtml
                </div>
            </body>
            </html>
        """.trimIndent()

        destFile.writeText(htmlDoc, Charsets.UTF_8)
        return destFile
    }

    /**
     * Generates an interactive Slide Presentation (.html / PPT deck).
     */
    fun createPresentation(context: Context, title: String, content: String): File {
        val cleanTitle = title.ifBlank { "DezhAi_Presentation" }
        val filename = "DezhAi_${sanitizeFilename(cleanTitle)}_Slides_${System.currentTimeMillis()}.html"
        val destFile = File(getExportDir(context), filename)

        // Split text into slides by sections or headers
        val sections = content.split(Regex("(?=(?:^|\\n)#{1,3}\\s)"))
        val slides = mutableListOf<String>()

        slides.add("<h1>$title</h1><p style='color: #10A37F; font-size: 20px;'>ارائه تولید شده توسط DezhAi 🛡️</p><p style='color: #9CA3AF;'>موتور هوش مصنوعی محلی با پشتیبانی مدل‌های GGUF</p>")

        for (sec in sections) {
            val trimmed = sec.trim()
            if (trimmed.isNotBlank()) {
                var slideHtml = trimmed
                    .replace("\n", "<br/>")
                    .replace(Regex("###\\s*(.*?)(<br/>|$)"), "<h3 style='color: #38BDF8;'>$1</h3>")
                    .replace(Regex("##\\s*(.*?)(<br/>|$)"), "<h2 style='color: #10A37F;'>$1</h2>")
                    .replace(Regex("#\\s*(.*?)(<br/>|$)"), "<h1 style='color: #10A37F;'>$1</h1>")
                    .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                slides.add(slideHtml)
            }
        }

        val slidesJsonArray = slides.joinToString(",") { "\"${it.replace("\"", "\\\"").replace("\n", " ")}\"" }

        val presentationHtml = """
            <!DOCTYPE html>
            <html lang="fa" dir="rtl">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ارائه: $title</title>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background: #0F172A;
                        color: #ECECF1;
                        font-family: 'Segoe UI', Tahoma, sans-serif;
                        display: flex;
                        flex-direction: column;
                        height: 100vh;
                        overflow: hidden;
                    }
                    .slide-container {
                        flex: 1;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 24px;
                    }
                    .slide-card {
                        background: #1E293B;
                        border: 1px solid #334155;
                        border-radius: 20px;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.5);
                        width: 90%;
                        max-width: 800px;
                        min-height: 400px;
                        padding: 40px;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;
                        line-height: 1.8;
                        font-size: 18px;
                        transition: all 0.3s ease;
                    }
                    .controls {
                        background: #111827;
                        border-top: 1px solid #1F2937;
                        padding: 14px 24px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                    }
                    button {
                        background: #10A37F;
                        color: white;
                        border: none;
                        padding: 10px 20px;
                        border-radius: 10px;
                        cursor: pointer;
                        font-weight: bold;
                        font-size: 14px;
                    }
                    button:disabled {
                        opacity: 0.4;
                        cursor: not-allowed;
                    }
                    .counter {
                        color: #94A3B8;
                        font-weight: bold;
                    }
                </style>
            </head>
            <body>
                <div class="slide-container">
                    <div class="slide-card" id="slide-content"></div>
                </div>
                <div class="controls">
                    <button id="prevBtn" onclick="prevSlide()">◀ قبلی</button>
                    <div class="counter" id="slide-counter">اسلاید ۱ از ۱</div>
                    <button id="nextBtn" onclick="nextSlide()">بعدی ▶</button>
                </div>
                <script>
                    const slides = [$slidesJsonArray];
                    let current = 0;
                    function update() {
                        document.getElementById('slide-content').innerHTML = slides[current];
                        document.getElementById('slide-counter').innerText = 'اسلاید ' + (current + 1) + ' از ' + slides.length;
                        document.getElementById('prevBtn').disabled = current === 0;
                        document.getElementById('nextBtn').disabled = current === slides.length - 1;
                    }
                    function prevSlide() { if(current > 0) { current--; update(); } }
                    function nextSlide() { if(current < slides.length - 1) { current++; update(); } }
                    update();
                </script>
            </body>
            </html>
        """.trimIndent()

        destFile.writeText(presentationHtml, Charsets.UTF_8)
        return destFile
    }

    /**
     * Shares a generated file using the Android system share sheet.
     */
    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری یا ذخیره سند: $title"))
    }
}
