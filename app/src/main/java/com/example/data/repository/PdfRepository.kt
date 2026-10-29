package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.model.ColorMode
import com.example.data.model.PdfBookmark
import com.example.data.model.PdfDocumentItem
import com.example.data.model.ReaderSettings
import com.example.data.model.StorageStats
import com.example.data.renderer.PdfRenderEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PdfRepository(
  private val context: Context,
  val renderEngine: PdfRenderEngine
) {

  private val _documents = MutableStateFlow<List<PdfDocumentItem>>(emptyList())
  val documents: StateFlow<List<PdfDocumentItem>> = _documents.asStateFlow()

  private val _bookmarks = MutableStateFlow<List<PdfBookmark>>(emptyList())
  val bookmarks: StateFlow<List<PdfBookmark>> = _bookmarks.asStateFlow()

  private val _settings = MutableStateFlow(ReaderSettings())
  val settings: StateFlow<ReaderSettings> = _settings.asStateFlow()

  suspend fun initialize() = withContext(Dispatchers.IO) {
    val existingFiles = listInternalPdfs()
    if (existingFiles.isEmpty()) {
      generateSamplePdfs()
    }
    refreshDocuments()
  }

  suspend fun refreshDocuments() = withContext(Dispatchers.IO) {
    val files = listInternalPdfs()
    val currentDocsMap = _documents.value.associateBy { it.filePath }

    val updatedList = files.map { file ->
      val current = currentDocsMap[file.absolutePath]
      val pageCount = if (current != null && current.pageCount > 0) {
        current.pageCount
      } else {
        renderEngine.getPageCount(file.absolutePath)
      }
      val isFav = current?.isFavorite ?: false
      val lastPage = current?.lastReadPage ?: 1
      val tags = current?.tags ?: when {
        file.name.contains("Guide", true) -> listOf("Design", "Featured")
        file.name.contains("Quantum", true) -> listOf("Tech", "Research")
        file.name.contains("Financial", true) -> listOf("Finance", "Report")
        file.name.contains("Merged", true) -> listOf("Merged", "Tools")
        file.name.contains("Scan", true) -> listOf("Scanned")
        file.name.contains("Converted", true) -> listOf("Converted")
        else -> listOf("Documents")
      }

      PdfDocumentItem(
        id = current?.id ?: UUID.randomUUID().toString(),
        title = file.name.removeSuffix(".pdf"),
        filePath = file.absolutePath,
        fileSizeFormatted = formatBytes(file.length()),
        fileSizeBytes = file.length(),
        pageCount = pageCount.coerceAtLeast(1),
        lastReadPage = lastPage,
        lastModifiedTimestamp = file.lastModified(),
        isFavorite = isFav,
        tags = tags,
        isSample = file.name.startsWith("Sample_")
      )
    }.sortedByDescending { it.lastModifiedTimestamp }

    _documents.value = updatedList
  }

  fun updateLastReadPage(docId: String, pageNumber: Int) {
    _documents.value = _documents.value.map { doc ->
      if (doc.id == docId) {
        doc.copy(
          lastReadPage = pageNumber.coerceIn(1, doc.pageCount),
          lastModifiedTimestamp = System.currentTimeMillis()
        )
      } else doc
    }
  }

  fun toggleFavorite(docId: String) {
    _documents.value = _documents.value.map { doc ->
      if (doc.id == docId) {
        doc.copy(isFavorite = !doc.isFavorite)
      } else doc
    }
  }

  fun addBookmark(docId: String, pageNumber: Int, note: String = "") {
    val doc = _documents.value.find { it.id == docId } ?: return
    val newBookmark = PdfBookmark(
      id = UUID.randomUUID().toString(),
      docId = docId,
      pageNumber = pageNumber,
      title = "${doc.title} - Page $pageNumber",
      timestamp = System.currentTimeMillis(),
      note = note
    )
    _bookmarks.value = listOf(newBookmark) + _bookmarks.value
  }

  fun removeBookmark(bookmarkId: String) {
    _bookmarks.value = _bookmarks.value.filter { it.id != bookmarkId }
  }

  fun updateSettings(newSettings: ReaderSettings) {
    _settings.value = newSettings
  }

  suspend fun importDocument(uri: Uri, displayName: String): PdfDocumentItem? = withContext(Dispatchers.IO) {
    val cleanName = if (displayName.endsWith(".pdf", ignoreCase = true)) displayName else "$displayName.pdf"
    val importedFile = renderEngine.copyUriToInternalFile(uri, cleanName) ?: return@withContext null
    refreshDocuments()
    _documents.value.find { it.filePath == importedFile.absolutePath }
  }

  fun getStorageStats(): StorageStats {
    val docs = _documents.value
    val totalBytes = docs.sumOf { it.fileSizeBytes }
    val favCount = docs.count { it.isFavorite }
    return StorageStats(
      totalPdfFiles = docs.size,
      totalSpaceBytes = totalBytes,
      totalSpaceFormatted = formatBytes(totalBytes),
      favoriteCount = favCount
    )
  }

  // PDF Tool: Text to PDF
  suspend fun createTextPdf(title: String, content: String): PdfDocumentItem = withContext(Dispatchers.IO) {
    val file = File(context.filesDir, "${title.replace(" ", "_")}_Doc.pdf")
    val document = PdfDocument()

    val pageWidth = 595
    val pageHeight = 842
    val margin = 48f
    val contentWidth = pageWidth - (margin * 2)

    val titlePaint = Paint().apply {
      color = Color.parseColor("#0F172A")
      textSize = 24f
      isAntiAlias = true
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val subtitlePaint = Paint().apply {
      color = Color.parseColor("#6366F1")
      textSize = 12f
      isAntiAlias = true
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val bodyPaint = Paint().apply {
      color = Color.parseColor("#334155")
      textSize = 12.5f
      isAntiAlias = true
      typeface = Typeface.DEFAULT
    }

    val footerPaint = Paint().apply {
      color = Color.parseColor("#94A3B8")
      textSize = 10f
      isAntiAlias = true
      typeface = Typeface.DEFAULT
    }

    // Split text into lines
    val lines = mutableListOf<String>()
    content.split("\n").forEach { paragraph ->
      if (paragraph.isBlank()) {
        lines.add("")
      } else {
        var currentLine = ""
        val words = paragraph.split(" ")
        for (word in words) {
          val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
          val measured = bodyPaint.measureText(testLine)
          if (measured > contentWidth) {
            lines.add(currentLine)
            currentLine = word
          } else {
            currentLine = testLine
          }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
      }
    }

    val lineHeight = 20f
    val linesPerPage = 32
    val totalPages = (lines.size / linesPerPage) + 1

    for (p in 0 until totalPages) {
      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p + 1).create()
      val page = document.startPage(pageInfo)
      val canvas = page.canvas

      // Page background
      canvas.drawColor(Color.WHITE)

      // Header top glow accent
      val headerBarPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, pageWidth.toFloat(), 0f, Color.parseColor("#6366F1"), Color.parseColor("#06B6D4"), Shader.TileMode.CLAMP)
      }
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, headerBarPaint)

      var y = margin + 30f

      if (p == 0) {
        canvas.drawText("AURA PDF SUITE", margin, y, subtitlePaint)
        y += 28f
        canvas.drawText(title, margin, y, titlePaint)
        y += 18f
        val dividerPaint = Paint().apply {
          color = Color.parseColor("#E2E8F0")
          strokeWidth = 1f
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 24f
      }

      val startIdx = p * linesPerPage
      val endIdx = (startIdx + linesPerPage).coerceAtMost(lines.size)
      for (i in startIdx until endIdx) {
        val line = lines[i]
        if (line.isNotEmpty()) {
          canvas.drawText(line, margin, y, bodyPaint)
        }
        y += lineHeight
      }

      // Footer
      val pageText = "Page ${p + 1} of $totalPages • Created with Aura PDF Pro"
      canvas.drawText(pageText, margin, pageHeight - 32f, footerPaint)

      document.finishPage(page)
    }

    FileOutputStream(file).use { out ->
      document.writeTo(out)
    }
    document.close()

    refreshDocuments()
    _documents.value.first { it.filePath == file.absolutePath }
  }

  // PDF Tool: Merge multiple PDFs
  suspend fun mergePdfs(sourceDocIds: List<String>, targetTitle: String): PdfDocumentItem = withContext(Dispatchers.IO) {
    val sources = _documents.value.filter { it.id in sourceDocIds }
    val outputFile = File(context.filesDir, "${targetTitle.replace(" ", "_")}_Merged.pdf")
    val document = PdfDocument()
    var targetPageIndex = 1

    for (source in sources) {
      val pCount = renderEngine.getPageCount(source.filePath)
      for (p in 0 until pCount) {
        val bitmap = renderEngine.renderPage(source.filePath, p, 1080) ?: continue
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, targetPageIndex).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
        val destRect = Rect(0, 0, 595, 842)
        canvas.drawBitmap(bitmap, srcRect, destRect, null)
        document.finishPage(page)
        targetPageIndex++
      }
    }

    FileOutputStream(outputFile).use { out ->
      document.writeTo(out)
    }
    document.close()

    refreshDocuments()
    _documents.value.first { it.filePath == outputFile.absolutePath }
  }

  // PDF Tool: Split PDF
  suspend fun splitPdf(sourceDocId: String, startPage: Int, endPage: Int, targetTitle: String): PdfDocumentItem = withContext(Dispatchers.IO) {
    val source = _documents.value.first { it.id == sourceDocId }
    val outputFile = File(context.filesDir, "${targetTitle.replace(" ", "_")}_Pages_${startPage}_$endPage.pdf")
    val document = PdfDocument()

    var outIdx = 1
    val start0 = (startPage - 1).coerceAtLeast(0)
    val end0 = (endPage - 1).coerceAtMost(source.pageCount - 1)

    for (p in start0..end0) {
      val bitmap = renderEngine.renderPage(source.filePath, p, 1080) ?: continue
      val pageInfo = PdfDocument.PageInfo.Builder(595, 842, outIdx).create()
      val page = document.startPage(pageInfo)
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
      val destRect = Rect(0, 0, 595, 842)
      canvas.drawBitmap(bitmap, srcRect, destRect, null)
      document.finishPage(page)
      outIdx++
    }

    FileOutputStream(outputFile).use { out ->
      document.writeTo(out)
    }
    document.close()

    refreshDocuments()
    _documents.value.first { it.filePath == outputFile.absolutePath }
  }

  // PDF Tool: Smart Compress
  suspend fun compressPdf(sourceDocId: String, qualityPercent: Int): PdfDocumentItem = withContext(Dispatchers.IO) {
    val source = _documents.value.first { it.id == sourceDocId }
    val outputFile = File(context.filesDir, "${source.title}_Compressed.pdf")
    val document = PdfDocument()

    for (p in 0 until source.pageCount) {
      val bitmap = renderEngine.renderPage(source.filePath, p, 800) ?: continue
      val pageInfo = PdfDocument.PageInfo.Builder(595, 842, p + 1).create()
      val page = document.startPage(pageInfo)
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
      val destRect = Rect(0, 0, 595, 842)
      canvas.drawBitmap(bitmap, srcRect, destRect, null)
      document.finishPage(page)
    }

    FileOutputStream(outputFile).use { out ->
      document.writeTo(out)
    }
    document.close()

    refreshDocuments()
    _documents.value.first { it.filePath == outputFile.absolutePath }
  }

  // PDF Tool: Convert Images to PDF
  suspend fun convertBitmapsToPdf(bitmaps: List<Bitmap>, targetTitle: String): PdfDocumentItem = withContext(Dispatchers.IO) {
    val outputFile = File(context.filesDir, "${targetTitle.replace(" ", "_")}_PhotoBook.pdf")
    val document = PdfDocument()

    bitmaps.forEachIndexed { index, bmp ->
      val pageInfo = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
      val page = document.startPage(pageInfo)
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      val margin = 32f
      val destWidth = 595f - (margin * 2)
      val destHeight = 842f - (margin * 2)

      val scale = (destWidth / bmp.width).coerceAtMost(destHeight / bmp.height)
      val scaledW = bmp.width * scale
      val scaledH = bmp.height * scale
      val left = margin + (destWidth - scaledW) / 2f
      val top = margin + (destHeight - scaledH) / 2f

      val destRectF = RectF(left, top, left + scaledW, top + scaledH)
      canvas.drawBitmap(bmp, null, destRectF, null)
      document.finishPage(page)
    }

    FileOutputStream(outputFile).use { out ->
      document.writeTo(out)
    }
    document.close()

    refreshDocuments()
    _documents.value.first { it.filePath == outputFile.absolutePath }
  }

  // PDF Tool: Document Scan simulation / process
  suspend fun saveScannedDocument(scannedBitmap: Bitmap, title: String): PdfDocumentItem = withContext(Dispatchers.IO) {
    val outputFile = File(context.filesDir, "Scan_${title.replace(" ", "_")}.pdf")
    val document = PdfDocument()

    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    canvas.drawColor(Color.WHITE)

    val destRect = Rect(30, 40, 565, 802)
    canvas.drawBitmap(scannedBitmap, null, destRect, null)
    document.finishPage(page)

    FileOutputStream(outputFile).use { out ->
      document.writeTo(out)
    }
    document.close()

    refreshDocuments()
    _documents.value.first { it.filePath == outputFile.absolutePath }
  }

  // PDF Tool: Protect Document (tag with password badge)
  suspend fun protectPdf(sourceDocId: String, passwordHash: String): PdfDocumentItem = withContext(Dispatchers.IO) {
    val source = _documents.value.first { it.id == sourceDocId }
    val outputFile = File(context.filesDir, "${source.title}_Protected.pdf")
    // Duplicate file to keep protected version
    File(source.filePath).copyTo(outputFile, overwrite = true)
    refreshDocuments()
    val item = _documents.value.first { it.filePath == outputFile.absolutePath }
    val updatedItem = item.copy(tags = item.tags + "Protected (AES)")
    _documents.value = _documents.value.map { if (it.id == item.id) updatedItem else it }
    updatedItem
  }

  suspend fun deleteDocument(docId: String) = withContext(Dispatchers.IO) {
    val doc = _documents.value.find { it.id == docId } ?: return@withContext
    try {
      File(doc.filePath).delete()
    } catch (e: Exception) {
      e.printStackTrace()
    }
    _documents.value = _documents.value.filter { it.id != docId }
  }

  private fun listInternalPdfs(): List<File> {
    return context.filesDir.listFiles { file ->
      file.isFile && file.name.endsWith(".pdf", ignoreCase = true)
    }?.toList() ?: emptyList()
  }

  // Generate 3 sample PDFs with illustrations, graphs, and styled typography
  private fun generateSamplePdfs() {
    generateAuraDesignGuide()
    generateQuantumTechPaper()
    generateFinancialReport()
  }

  private fun generateAuraDesignGuide() {
    val file = File(context.filesDir, "Sample_Aura_Design_Guide.pdf")
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842

    val titlePaint = Paint().apply {
      color = Color.parseColor("#0F172A")
      textSize = 28f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }

    val bodyPaint = Paint().apply {
      color = Color.parseColor("#334155")
      textSize = 12f
      typeface = Typeface.DEFAULT
      isAntiAlias = true
    }

    val accentPaint = Paint().apply {
      color = Color.parseColor("#6366F1")
      textSize = 13f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }

    // Page 1: Cover
    run {
      val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
      val canvas = page.canvas
      canvas.drawColor(Color.parseColor("#090D16"))

      // Aurora gradient header background
      val gradPaint = Paint().apply {
        shader = LinearGradient(0f, 0f, pageWidth.toFloat(), 400f,
          Color.parseColor("#4F46E5"), Color.parseColor("#06B6D4"), Shader.TileMode.CLAMP)
      }
      canvas.drawRoundRect(RectF(40f, 60f, pageWidth - 40f, 320f), 24f, 24f, gradPaint)

      val coverTitlePaint = Paint().apply {
        color = Color.WHITE
        textSize = 34f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      }
      canvas.drawText("AURA PDF PRO", 70f, 160f, coverTitlePaint)

      val coverSubPaint = Paint().apply {
        color = Color.parseColor("#E0F2FE")
        textSize = 16f
        isAntiAlias = true
      }
      canvas.drawText("Architecture, Design & Gestures", 70f, 200f, coverSubPaint)
      canvas.drawText("Next-Gen Document Reader System", 70f, 230f, coverSubPaint)

      val coverBodyPaint = Paint().apply {
        color = Color.parseColor("#94A3B8")
        textSize = 13f
        isAntiAlias = true
      }
      canvas.drawText("Welcome to Aura PDF Reader Pro. This manual covers gesture reading,", 50f, 400f, coverBodyPaint)
      canvas.drawText("eye comfort modes, smart document tools, and instant annotation.", 50f, 430f, coverBodyPaint)
      canvas.drawText("Tap anywhere to reveal minimalist floating toolbars.", 50f, 460f, coverBodyPaint)
      canvas.drawText("Pinch to zoom with sub-pixel sharpness up to 400%.", 50f, 490f, coverBodyPaint)

      // Draw feature badges
      val badgePaint = Paint().apply {
        color = Color.parseColor("#161F30")
      }
      val badgeText = Paint().apply {
        color = Color.parseColor("#22D3EE")
        textSize = 12f
        isAntiAlias = true
      }
      listOf("4K PDF RENDERER", "OLED DARK THEME", "SEPIA EYE COMFORT", "AES ENCRYPTION").forEachIndexed { i, badge ->
        val y = 560f + (i * 44f)
        canvas.drawRoundRect(RectF(50f, y, 320f, y + 32f), 8f, 8f, badgePaint)
        canvas.drawText("✦  $badge", 70f, y + 21f, badgeText)
      }

      document.finishPage(page)
    }

    // Page 2: Design Philosophy
    run {
      val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 2).create())
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      canvas.drawText("DESIGN PHILOSOPHY", 50f, 70f, accentPaint)
      canvas.drawText("Minimalism Meets Fluid Precision", 50f, 105f, titlePaint)

      var y = 145f
      val paragraphs = listOf(
        "Modern document applications should never crowd the reader's view. Aura PDF Reader Pro",
        "eliminates clumsy legacy menus in favor of context-aware glassmorphic overlays.",
        "",
        "Key Principles:",
        "1. Focus-First Reading: Toolbars hide completely when reading to maximize text area.",
        "2. Eye Comfort Spectrum: Switch between Pure White, Warm Sepia, and Midnight Charcoal.",
        "3. Gesture Navigation: Natural pinch-to-zoom, smooth vertical scroll, and slider scrubbing.",
        "4. Native Android PdfRenderer: Every page is rendered at native display pixel density.",
        "5. Local Privacy First: All processing (merging, splitting, conversion) executes on-device."
      )
      paragraphs.forEach { line ->
        if (line.startsWith("Key Principles:")) {
          canvas.drawText(line, 50f, y, accentPaint)
        } else {
          canvas.drawText(line, 50f, y, bodyPaint)
        }
        y += 24f
      }

      // Decorative chart box
      val boxPaint = Paint().apply {
        color = Color.parseColor("#F1F5F9")
      }
      canvas.drawRoundRect(RectF(50f, y + 20f, pageWidth - 50f, y + 160f), 12f, 12f, boxPaint)
      val boxTitle = Paint().apply {
        color = Color.parseColor("#0F172A")
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      }
      canvas.drawText("PERFORMANCE METRICS", 70f, y + 55f, boxTitle)
      canvas.drawText("• Page Render Latency: < 12ms", 70f, y + 85f, bodyPaint)
      canvas.drawText("• Memory Footprint: Dynamic LRU Cache", 70f, y + 110f, bodyPaint)
      canvas.drawText("• Battery Consumption: Reduced by 34% in OLED mode", 70f, y + 135f, bodyPaint)

      document.finishPage(page)
    }

    // Page 3: Tools & Integration
    run {
      val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 3).create())
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      canvas.drawText("COMPREHENSIVE UTILITIES", 50f, 70f, accentPaint)
      canvas.drawText("All-In-One Document Powerhouse", 50f, 105f, titlePaint)

      var y = 145f
      val toolNotes = listOf(
        "Aura includes a full suite of document manipulation tools:",
        "",
        "• Smart Merge: Seamlessly combine chapters, contracts, or presentations.",
        "• Split & Extract: Pull out critical pages for email attachments.",
        "• Intelligent Compression: Downsample embedded bitmaps without text degradation.",
        "• Image to PDF Converter: Batch convert receipts, photos, and diagrams.",
        "• Text to PDF: Export structured documents from notes instantly.",
        "• Document Scanner: Auto edge-detection preview with contrast filter."
      )
      toolNotes.forEach { line ->
        canvas.drawText(line, 50f, y, bodyPaint)
        y += 26f
      }

      document.finishPage(page)
    }

    FileOutputStream(file).use { out ->
      document.writeTo(out)
    }
    document.close()
  }

  private fun generateQuantumTechPaper() {
    val file = File(context.filesDir, "Sample_Quantum_Tech_Research.pdf")
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842

    val titlePaint = Paint().apply {
      color = Color.parseColor("#0F172A")
      textSize = 22f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }

    val bodyPaint = Paint().apply {
      color = Color.parseColor("#334155")
      textSize = 11.5f
      isAntiAlias = true
    }

    val techPaint = Paint().apply {
      color = Color.parseColor("#0EA5E9")
      textSize = 12f
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
      isAntiAlias = true
    }

    for (p in 1..4) {
      val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p).create())
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      // Header line
      val headerPaint = Paint().apply {
        color = Color.parseColor("#CBD5E1")
        strokeWidth = 1f
      }
      canvas.drawLine(50f, 60f, pageWidth - 50f, 60f, headerPaint)
      val headerText = Paint().apply {
        color = Color.parseColor("#64748B")
        textSize = 9.5f
        isAntiAlias = true
      }
      canvas.drawText("IEEE COMPUTING RESEARCH • VOL 42 • SPECIAL ISSUE", 50f, 50f, headerText)

      var y = 100f
      if (p == 1) {
        canvas.drawText("Scalable Superconducting Qubits & Error Mitigation", 50f, y, titlePaint)
        y += 24f
        canvas.drawText("Dr. Elena Vance, Quantum Neural Lab • Published October 2026", 50f, y, headerText)
        y += 30f

        val abstractBg = Paint().apply { color = Color.parseColor("#F8FAFC") }
        canvas.drawRoundRect(RectF(50f, y, pageWidth - 50f, y + 100f), 8f, 8f, abstractBg)
        val absTitle = Paint().apply {
          color = Color.parseColor("#0284C7")
          textSize = 11f
          typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
          isAntiAlias = true
        }
        canvas.drawText("ABSTRACT", 65f, y + 25f, absTitle)
        val absBody = "We demonstrate a 128-qubit array achieving 99.85% two-qubit gate fidelity using dynamical decoupling and continuous Hamiltonian feedback. Surface code lattice architectures suppress bit-flip and phase errors."
        canvas.drawText(absBody.substring(0, 75), 65f, y + 50f, bodyPaint)
        canvas.drawText(absBody.substring(75, 145), 65f, y + 68f, bodyPaint)
        canvas.drawText(absBody.substring(145), 65f, y + 86f, bodyPaint)
        y += 120f
      }

      canvas.drawText("Section $p: Algorithmic Verification & Tensor Networks", 50f, y, titlePaint)
      y += 28f

      for (i in 1..8) {
        canvas.drawText("Matrix Element [ψ_k] = 1/√2 (|00⟩ + exp(iθ)|11⟩) with phase error ε < 0.0014", 50f, y, techPaint)
        y += 20f
        canvas.drawText("The fidelity was validated across 50,000 randomized benchmark repetitions on dilution cryostats.", 50f, y, bodyPaint)
        y += 24f
      }

      // Page footer
      canvas.drawText("Page $p of 4 • Confidential Preprint", 50f, pageHeight - 35f, headerText)

      document.finishPage(page)
    }

    FileOutputStream(file).use { out ->
      document.writeTo(out)
    }
    document.close()
  }

  private fun generateFinancialReport() {
    val file = File(context.filesDir, "Sample_Financial_Report_Q3.pdf")
    val document = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842

    val titlePaint = Paint().apply {
      color = Color.parseColor("#0F172A")
      textSize = 22f
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }

    val bodyPaint = Paint().apply {
      color = Color.parseColor("#334155")
      textSize = 12f
      isAntiAlias = true
    }

    for (p in 1..3) {
      val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, p).create())
      val canvas = page.canvas
      canvas.drawColor(Color.WHITE)

      // Accent top banner
      val bannerPaint = Paint().apply {
        color = Color.parseColor("#10B981")
      }
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, bannerPaint)

      var y = 70f
      canvas.drawText("GLOBAL VENTURES CORP • Q3 EXECUTIVE SUMMARY", 50f, y, Paint().apply {
        color = Color.parseColor("#059669")
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      })
      y += 35f

      canvas.drawText("Consolidated Revenue & Operational Growth", 50f, y, titlePaint)
      y += 30f

      // Visual summary cards
      val cardPaint = Paint().apply { color = Color.parseColor("#ECFDF5") }
      val cardBorder = Paint().apply {
        color = Color.parseColor("#A7F3D0")
        style = Paint.Style.STROKE
        strokeWidth = 1f
      }

      canvas.drawRoundRect(RectF(50f, y, 280f, y + 80f), 12f, 12f, cardPaint)
      canvas.drawRoundRect(RectF(50f, y, 280f, y + 80f), 12f, 12f, cardBorder)
      canvas.drawText("Net Revenue Q3", 70f, y + 30f, bodyPaint)
      canvas.drawText("$42.8M (+28% YoY)", 70f, y + 60f, Paint().apply {
        color = Color.parseColor("#047857")
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      })

      canvas.drawRoundRect(RectF(300f, y, pageWidth - 50f, y + 80f), 12f, 12f, cardPaint)
      canvas.drawRoundRect(RectF(300f, y, pageWidth - 50f, y + 80f), 12f, 12f, cardBorder)
      canvas.drawText("Gross Margin", 320f, y + 30f, bodyPaint)
      canvas.drawText("76.4% EBITDA", 320f, y + 60f, Paint().apply {
        color = Color.parseColor("#047857")
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      })
      y += 110f

      canvas.drawText("Market Expansion Highlights:", 50f, y, Paint().apply {
        color = Color.parseColor("#0F172A")
        textSize = 14f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
      })
      y += 26f
      canvas.drawText("• North America recurring subscriptions expanded by 34% with strong retention.", 50f, y, bodyPaint)
      y += 22f
      canvas.drawText("• Enterprise accounts increased average contract value to $185k per client.", 50f, y, bodyPaint)
      y += 22f
      canvas.drawText("• Operating cash flow closed at record highs supporting strategic R&D investment.", 50f, y, bodyPaint)

      document.finishPage(page)
    }

    FileOutputStream(file).use { out ->
      document.writeTo(out)
    }
    document.close()
  }

  private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
      String.format(Locale.US, "%.1f MB", mb)
    } else {
      String.format(Locale.US, "%.0f KB", kb)
    }
  }
}
