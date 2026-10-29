package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ColorMode
import com.example.data.model.PageTransition
import com.example.data.model.PdfBookmark
import com.example.data.model.PdfDocumentItem
import com.example.data.model.ReaderSettings
import com.example.data.model.StorageStats
import com.example.data.model.ToolCategory
import com.example.data.model.ViewMode
import com.example.data.renderer.PdfRenderEngine
import com.example.data.repository.PdfRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class NavigationTab(val label: String) {
  HOME("Home"),
  LIBRARY("Library"),
  TOOLS("Tools"),
  SETTINGS("Settings")
}

data class UiNotification(
  val id: Long = System.currentTimeMillis(),
  val message: String,
  val isError: Boolean = false
)

class PdfViewModel(application: Application) : AndroidViewModel(application) {

  val renderEngine = PdfRenderEngine(application)
  val repository = PdfRepository(application, renderEngine)

  val documents: StateFlow<List<PdfDocumentItem>> = repository.documents
  val bookmarks: StateFlow<List<PdfBookmark>> = repository.bookmarks
  val settings: StateFlow<ReaderSettings> = repository.settings

  private val _currentTab = MutableStateFlow(NavigationTab.HOME)
  val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

  // Currently opened PDF for full reader
  private val _activeDocument = MutableStateFlow<PdfDocumentItem?>(null)
  val activeDocument: StateFlow<PdfDocumentItem?> = _activeDocument.asStateFlow()

  // Reader state
  private val _readerPage = MutableStateFlow(1)
  val readerPage: StateFlow<Int> = _readerPage.asStateFlow()

  private val _readerControlsVisible = MutableStateFlow(true)
  val readerControlsVisible: StateFlow<Boolean> = _readerControlsVisible.asStateFlow()

  private val _readerColorMode = MutableStateFlow(ColorMode.NORMAL)
  val readerColorMode: StateFlow<ColorMode> = _readerColorMode.asStateFlow()

  private val _readerViewMode = MutableStateFlow(ViewMode.SINGLE_PAGE)
  val readerViewMode: StateFlow<ViewMode> = _readerViewMode.asStateFlow()

  private val _currentPageBitmap = MutableStateFlow<Bitmap?>(null)
  val currentPageBitmap: StateFlow<Bitmap?> = _currentPageBitmap.asStateFlow()

  private val _isRenderingPage = MutableStateFlow(false)
  val isRenderingPage: StateFlow<Boolean> = _isRenderingPage.asStateFlow()

  // Tools workflow state
  private val _activeTool = MutableStateFlow<ToolCategory?>(null)
  val activeTool: StateFlow<ToolCategory?> = _activeTool.asStateFlow()

  private val _isToolProcessing = MutableStateFlow(false)
  val isToolProcessing: StateFlow<Boolean> = _isToolProcessing.asStateFlow()

  private val _notification = MutableStateFlow<UiNotification?>(null)
  val notification: StateFlow<UiNotification?> = _notification.asStateFlow()

  // Library filters
  private val _librarySearch = MutableStateFlow("")
  val librarySearch: StateFlow<String> = _librarySearch.asStateFlow()

  private val _libraryTagFilter = MutableStateFlow<String?>(null)
  val libraryTagFilter: StateFlow<String?> = _libraryTagFilter.asStateFlow()

  init {
    viewModelScope.launch {
      repository.initialize()
    }
  }

  fun setTab(tab: NavigationTab) {
    _currentTab.value = tab
  }

  fun setLibrarySearch(query: String) {
    _librarySearch.value = query
  }

  fun setLibraryTag(tag: String?) {
    _libraryTagFilter.value = tag
  }

  fun dismissNotification() {
    _notification.value = null
  }

  fun showNotification(message: String, isError: Boolean = false) {
    _notification.value = UiNotification(message = message, isError = isError)
  }

  fun openDocument(doc: PdfDocumentItem) {
    _activeDocument.value = doc
    _readerPage.value = doc.lastReadPage
    _readerColorMode.value = doc.readingColorMode
    _readerControlsVisible.value = true
    loadPageBitmap(doc.filePath, doc.lastReadPage)
  }

  fun closeReader() {
    _activeDocument.value?.let { doc ->
      repository.updateLastReadPage(doc.id, _readerPage.value)
    }
    _activeDocument.value = null
    _currentPageBitmap.value = null
  }

  fun toggleControls() {
    _readerControlsVisible.value = !_readerControlsVisible.value
  }

  fun setControlsVisibility(visible: Boolean) {
    _readerControlsVisible.value = visible
  }

  fun setPage(page: Int) {
    val doc = _activeDocument.value ?: return
    val clamped = page.coerceIn(1, doc.pageCount)
    if (clamped != _readerPage.value) {
      _readerPage.value = clamped
      repository.updateLastReadPage(doc.id, clamped)
      loadPageBitmap(doc.filePath, clamped)
    }
  }

  fun nextPage() {
    setPage(_readerPage.value + 1)
  }

  fun previousPage() {
    setPage(_readerPage.value - 1)
  }

  fun setColorMode(mode: ColorMode) {
    _readerColorMode.value = mode
  }

  fun setViewMode(mode: ViewMode) {
    _readerViewMode.value = mode
  }

  fun toggleCurrentDocumentFavorite() {
    _activeDocument.value?.let { doc ->
      repository.toggleFavorite(doc.id)
      _activeDocument.value = doc.copy(isFavorite = !doc.isFavorite)
    }
  }

  fun bookmarkCurrentPage(note: String = "") {
    val doc = _activeDocument.value ?: return
    repository.addBookmark(doc.id, _readerPage.value, note)
    showNotification("Bookmark added for Page ${_readerPage.value}")
  }

  fun openTool(tool: ToolCategory) {
    _activeTool.value = tool
  }

  fun closeTool() {
    _activeTool.value = null
  }

  fun importDocumentFromUri(uri: Uri, displayName: String) {
    viewModelScope.launch {
      val imported = repository.importDocument(uri, displayName)
      if (imported != null) {
        showNotification("Imported \"${imported.title}\" successfully")
        openDocument(imported)
      } else {
        showNotification("Failed to import document", isError = true)
      }
    }
  }

  fun executeTextToPdf(title: String, content: String) {
    viewModelScope.launch {
      _isToolProcessing.value = true
      try {
        val created = repository.createTextPdf(title, content)
        showNotification("Document created: ${created.title}")
        closeTool()
        openDocument(created)
      } catch (e: Exception) {
        showNotification("Creation failed: ${e.localizedMessage}", isError = true)
      } finally {
        _isToolProcessing.value = false
      }
    }
  }

  fun executeMergePdfs(selectedDocIds: List<String>, title: String) {
    viewModelScope.launch {
      _isToolProcessing.value = true
      try {
        val merged = repository.mergePdfs(selectedDocIds, title)
        showNotification("Merged into: ${merged.title}")
        closeTool()
        openDocument(merged)
      } catch (e: Exception) {
        showNotification("Merge failed: ${e.localizedMessage}", isError = true)
      } finally {
        _isToolProcessing.value = false
      }
    }
  }

  fun executeSplitPdf(docId: String, startPage: Int, endPage: Int, title: String) {
    viewModelScope.launch {
      _isToolProcessing.value = true
      try {
        val split = repository.splitPdf(docId, startPage, endPage, title)
        showNotification("Extracted ${endPage - startPage + 1} pages to ${split.title}")
        closeTool()
        openDocument(split)
      } catch (e: Exception) {
        showNotification("Split failed: ${e.localizedMessage}", isError = true)
      } finally {
        _isToolProcessing.value = false
      }
    }
  }

  fun executeCompressPdf(docId: String, quality: Int) {
    viewModelScope.launch {
      _isToolProcessing.value = true
      try {
        val compressed = repository.compressPdf(docId, quality)
        showNotification("Optimized! File saved at ${compressed.fileSizeFormatted}")
        closeTool()
        openDocument(compressed)
      } catch (e: Exception) {
        showNotification("Compress failed: ${e.localizedMessage}", isError = true)
      } finally {
        _isToolProcessing.value = false
      }
    }
  }

  fun executeSaveScannedDocument(bitmap: Bitmap, title: String) {
    viewModelScope.launch {
      _isToolProcessing.value = true
      try {
        val scanned = repository.saveScannedDocument(bitmap, title)
        showNotification("Document scanned & compiled into PDF!")
        closeTool()
        openDocument(scanned)
      } catch (e: Exception) {
        showNotification("Scan failed: ${e.localizedMessage}", isError = true)
      } finally {
        _isToolProcessing.value = false
      }
    }
  }

  fun executeProtectPdf(docId: String, passwordHash: String) {
    viewModelScope.launch {
      _isToolProcessing.value = true
      try {
        val protectedDoc = repository.protectPdf(docId, passwordHash)
        showNotification("Encrypted with AES-256 successfully!")
        closeTool()
        openDocument(protectedDoc)
      } catch (e: Exception) {
        showNotification("Protection failed: ${e.localizedMessage}", isError = true)
      } finally {
        _isToolProcessing.value = false
      }
    }
  }

  fun deleteDoc(docId: String) {
    viewModelScope.launch {
      repository.deleteDocument(docId)
      showNotification("Document deleted")
    }
  }

  fun clearAppCache() {
    renderEngine.clearCache()
    showNotification("Render cache cleaned (0 MB in memory)")
  }

  fun updateSettings(newSettings: ReaderSettings) {
    repository.updateSettings(newSettings)
  }

  private fun loadPageBitmap(filePath: String, pageNumber: Int) {
    viewModelScope.launch {
      _isRenderingPage.value = true
      val bmp = renderEngine.renderPage(filePath, pageNumber - 1, 1200)
      _currentPageBitmap.value = bmp
      _isRenderingPage.value = false
    }
  }
}
