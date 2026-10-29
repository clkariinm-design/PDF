package com.example.data.model

enum class ViewMode {
  CONTINUOUS_SCROLL,
  SINGLE_PAGE
}

enum class ColorMode {
  NORMAL,
  SEPIA,
  DARK,
  NIGHT_COMFORT
}

enum class PageTransition {
  SLIDE,
  FADE,
  CURL
}

enum class ToolCategory(val title: String, val subtitle: String, val badge: String) {
  MERGE("Merge PDF", "Combine multiple documents into one seamless file", "POPULAR"),
  SPLIT("Split & Extract", "Extract specific page ranges or split into parts", "PRECISE"),
  COMPRESS("Smart Compress", "Reduce PDF file size up to 75% with zero blur", "AI-ENHANCED"),
  IMAGE_TO_PDF("Images to PDF", "Convert gallery photos and scans to crystal PDF", "INSTANT"),
  TEXT_TO_PDF("Text & Notes to PDF", "Compile formatted notes into clean typed PDF", "CLEAN"),
  SCAN_DOC("Aura Cam Scan", "Scan physical documents with auto perspective warp", "PRO"),
  PROTECT("Protect & Encrypt", "Lock files with AES-256 password & permissions", "SECURITY")
}

data class PdfDocumentItem(
  val id: String,
  val title: String,
  val filePath: String,
  val uriString: String? = null,
  val fileSizeFormatted: String,
  val fileSizeBytes: Long,
  val pageCount: Int,
  val lastReadPage: Int = 1,
  val lastModifiedTimestamp: Long,
  val isFavorite: Boolean = false,
  val tags: List<String> = emptyList(),
  val readingColorMode: ColorMode = ColorMode.NORMAL,
  val isSample: Boolean = false
) {
  val readProgressFraction: Float
    get() = if (pageCount > 0) (lastReadPage.toFloat() / pageCount.toFloat()).coerceIn(0f, 1f) else 0f
}

data class PdfBookmark(
  val id: String,
  val docId: String,
  val pageNumber: Int,
  val title: String,
  val timestamp: Long,
  val note: String = ""
)

data class StorageStats(
  val totalPdfFiles: Int,
  val totalSpaceBytes: Long,
  val totalSpaceFormatted: String,
  val favoriteCount: Int,
  val readingHoursThisWeek: Float = 6.4f
)

data class ReaderSettings(
  val defaultViewMode: ViewMode = ViewMode.SINGLE_PAGE,
  val defaultColorMode: ColorMode = ColorMode.NORMAL,
  val pageTransition: PageTransition = PageTransition.SLIDE,
  val keepScreenAwake: Boolean = true,
  val invertPdfInDark: Boolean = true,
  val eyeComfortWarmth: Float = 0.35f,
  val autoScrollSpeed: Float = 1.0f,
  val hapticFeedback: Boolean = true
)
