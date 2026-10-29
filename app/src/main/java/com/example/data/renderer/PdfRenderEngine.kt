package com.example.data.renderer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfRenderEngine(private val context: Context) {

  // Bitmap cache key: "docPath_pageIndex_width"
  private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
  private val cacheSize = maxMemory / 8 // 1/8th of available memory
  private val bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
    override fun sizeOf(key: String, bitmap: Bitmap): Int {
      return bitmap.byteCount / 1024
    }
  }

  suspend fun getPageCount(filePath: String): Int = withContext(Dispatchers.IO) {
    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    try {
      val file = File(filePath)
      if (!file.exists() || file.length() == 0L) return@withContext 0
      pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
      renderer = PdfRenderer(pfd)
      renderer.pageCount
    } catch (e: Exception) {
      e.printStackTrace()
      0
    } finally {
      renderer?.close()
      pfd?.close()
    }
  }

  suspend fun renderPage(
    filePath: String,
    pageIndex: Int,
    destWidth: Int = 1080
  ): Bitmap? = withContext(Dispatchers.IO) {
    val cacheKey = "${filePath}_${pageIndex}_$destWidth"
    bitmapCache.get(cacheKey)?.let { return@withContext it }

    val file = File(filePath)
    if (!file.exists() || file.length() == 0L) return@withContext null

    var pfd: ParcelFileDescriptor? = null
    var renderer: PdfRenderer? = null
    try {
      pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
      renderer = PdfRenderer(pfd)
      if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

      val page = renderer.openPage(pageIndex)
      val pageWidth = page.width
      val pageHeight = page.height

      // Scale proportionally
      val scale = if (destWidth > 0 && pageWidth > 0) destWidth.toFloat() / pageWidth.toFloat() else 1.5f
      val targetW = (pageWidth * scale).toInt().coerceAtLeast(100)
      val targetH = (pageHeight * scale).toInt().coerceAtLeast(100)

      val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
      bitmap.eraseColor(Color.WHITE)

      page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
      page.close()

      bitmapCache.put(cacheKey, bitmap)
      bitmap
    } catch (e: Exception) {
      e.printStackTrace()
      null
    } finally {
      renderer?.close()
      pfd?.close()
    }
  }

  suspend fun copyUriToInternalFile(uri: Uri, fileName: String): File? = withContext(Dispatchers.IO) {
    try {
      val destination = File(context.filesDir, "imported_$fileName")
      context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(destination).use { outputStream ->
          inputStream.copyTo(outputStream)
        }
      }
      destination
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun clearCache() {
    bitmapCache.evictAll()
  }
}
