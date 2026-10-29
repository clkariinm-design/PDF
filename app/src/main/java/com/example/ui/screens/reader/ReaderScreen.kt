package com.example.ui.screens.reader

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColorMode
import com.example.data.model.PdfDocumentItem
import com.example.ui.components.AuraBadge
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.ReadingEyeComfortTint
import com.example.ui.theme.ReadingSepiaBg
import com.example.ui.viewmodel.PdfViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
  viewModel: PdfViewModel,
  document: PdfDocumentItem,
  modifier: Modifier = Modifier
) {
  BackHandler {
    viewModel.closeReader()
  }

  val currentPage by viewModel.readerPage.collectAsState()
  val controlsVisible by viewModel.readerControlsVisible.collectAsState()
  val colorMode by viewModel.readerColorMode.collectAsState()
  val currentPageBitmap by viewModel.currentPageBitmap.collectAsState()
  val isRendering by viewModel.isRenderingPage.collectAsState()
  val bookmarks by viewModel.bookmarks.collectAsState()

  val isCurrentPageBookmarked = remember(bookmarks, document.id, currentPage) {
    bookmarks.any { it.docId == document.id && it.pageNumber == currentPage }
  }

  // Zoom and pan state
  var scale by remember { mutableFloatStateOf(1f) }
  var offsetX by remember { mutableFloatStateOf(0f) }
  var offsetY by remember { mutableFloatStateOf(0f) }

  // Reset zoom on page turn
  LaunchedEffect(currentPage) {
    scale = 1f
    offsetX = 0f
    offsetY = 0f
  }

  // Page Grid thumbnail sheet
  var showGridSheet by remember { mutableStateOf(false) }
  var showColorModeDialog by remember { mutableStateOf(false) }

  // Canvas background based on reading mode
  val canvasBg = when (colorMode) {
    ColorMode.NORMAL -> Color(0xFF1E2430)
    ColorMode.SEPIA -> ReadingSepiaBg
    ColorMode.DARK -> Color(0xFF000000)
    ColorMode.NIGHT_COMFORT -> Color(0xFF181510)
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(canvasBg)
      .testTag("pdf_reader_screen")
  ) {
    // PDF Page Viewer & Gesture Area
    Box(
      modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = {
              viewModel.toggleControls()
            },
            onDoubleTap = {
              scale = if (scale > 1.2f) 1f else 2.2f
              offsetX = 0f
              offsetY = 0f
            }
          )
        }
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(1f, 3.5f)
            if (scale > 1f) {
              val maxPanX = 500f * (scale - 1f)
              val maxPanY = 800f * (scale - 1f)
              offsetX = (offsetX + pan.x).coerceIn(-maxPanX, maxPanX)
              offsetY = (offsetY + pan.y).coerceIn(-maxPanY, maxPanY)
            } else {
              offsetX = 0f
              offsetY = 0f
            }
          }
        },
      contentAlignment = Alignment.Center
    ) {
      if (currentPageBitmap != null) {
        val colorFilter = when (colorMode) {
          ColorMode.NORMAL -> null
          ColorMode.SEPIA -> ColorFilter.tint(Color(0xFFEBDCB9), BlendMode.Multiply)
          ColorMode.DARK -> ColorFilter.tint(Color(0xFFE2E8F0), BlendMode.Screen)
          ColorMode.NIGHT_COMFORT -> ColorFilter.tint(Color(0xFFFFE0B2), BlendMode.Multiply)
        }

        Image(
          bitmap = currentPageBitmap!!.asImageBitmap(),
          contentDescription = "PDF Page $currentPage",
          contentScale = ContentScale.Fit,
          colorFilter = colorFilter,
          modifier = Modifier
            .fillMaxSize()
            .padding(if (scale == 1f) 12.dp else 0.dp)
            .graphicsLayer {
              scaleX = scale
              scaleY = scale
              translationX = offsetX
              translationY = offsetY
            }
        )
      } else {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          CircularProgressIndicator(
            color = AuraCyan,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Rendering Page $currentPage...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
          )
        }
      }

      // Eye comfort blue-light filter overlay
      if (colorMode == ColorMode.NIGHT_COMFORT) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(ReadingEyeComfortTint)
        )
      }
    }

    // Top Floating Glass Toolbar
    AnimatedVisibility(
      visible = controlsVisible,
      enter = fadeIn() + slideInVertically { -it },
      exit = fadeOut() + slideOutVertically { -it },
      modifier = Modifier.align(Alignment.TopCenter)
    ) {
      ReaderTopBar(
        title = document.title,
        currentPage = currentPage,
        totalPages = document.pageCount,
        isBookmarked = isCurrentPageBookmarked,
        onBack = { viewModel.closeReader() },
        onBookmark = { viewModel.bookmarkCurrentPage() },
        onColorMode = { showColorModeDialog = true },
        onGrid = { showGridSheet = true }
      )
    }

    // Bottom Floating Navigation & Scrubbing Toolbar
    AnimatedVisibility(
      visible = controlsVisible,
      enter = fadeIn() + slideInVertically { it },
      exit = fadeOut() + slideOutVertically { it },
      modifier = Modifier.align(Alignment.BottomCenter)
    ) {
      ReaderBottomBar(
        currentPage = currentPage,
        totalPages = document.pageCount,
        scale = scale,
        onPageChange = { viewModel.setPage(it) },
        onNext = { viewModel.nextPage() },
        onPrev = { viewModel.previousPage() },
        onZoomReset = {
          scale = 1f
          offsetX = 0f
          offsetY = 0f
        }
      )
    }
  }

  // Thumbnail Grid Bottom Sheet
  if (showGridSheet) {
    ModalBottomSheet(
      onDismissRequest = { showGridSheet = false },
      containerColor = MaterialTheme.colorScheme.surface,
      sheetState = rememberModalBottomSheetState()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Page Jump (${document.pageCount} Pages)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          IconButton(onClick = { showGridSheet = false }) {
            Icon(Icons.Filled.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 90.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
        ) {
          items((1..document.pageCount).toList()) { pageNum ->
            val isCurrent = pageNum == currentPage
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (isCurrent) AuraIndigo.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
              border = BorderStroke(
                if (isCurrent) 2.dp else 1.dp,
                if (isCurrent) AuraIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
              ),
              modifier = Modifier
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  viewModel.setPage(pageNum)
                  showGridSheet = false
                }
            ) {
              Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text(
                  text = "$pageNum",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isCurrent) AuraIndigo else MaterialTheme.colorScheme.onSurface
                )
                if (isCurrent) {
                  Spacer(modifier = Modifier.height(4.dp))
                  AuraBadge(text = "CURRENT", containerColor = AuraIndigo, contentColor = Color.White)
                }
              }
            }
          }
        }
      }
    }
  }

  // Reading Color Mode Selector Sheet
  if (showColorModeDialog) {
    ModalBottomSheet(
      onDismissRequest = { showColorModeDialog = false },
      containerColor = MaterialTheme.colorScheme.surface
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 16.dp)
      ) {
        Text(
          text = "Reading Comfort & Tints",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        listOf(
          Triple(ColorMode.NORMAL, "Day Mode (Original)", Color(0xFFFFFFFF)),
          Triple(ColorMode.SEPIA, "Sepia Comfort (Warm Paper)", ReadingSepiaBg),
          Triple(ColorMode.DARK, "Night Charcoal (OLED Friendly)", Color(0xFF1E293B)),
          Triple(ColorMode.NIGHT_COMFORT, "Nocturne Eye Shield (Blue-Light Safe)", Color(0xFFFBBF24))
        ).forEach { (mode, label, previewColor) ->
          val isSelected = mode == colorMode
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) AuraIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, if (isSelected) AuraIndigo else Color.Transparent),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
              .clip(RoundedCornerShape(14.dp))
              .clickable {
                viewModel.setColorMode(mode)
                showColorModeDialog = false
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(previewColor)
              )
              Spacer(modifier = Modifier.width(14.dp))
              Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Composable
fun ReaderTopBar(
  title: String,
  currentPage: Int,
  totalPages: Int,
  isBookmarked: Boolean,
  onBack: () -> Unit,
  onBookmark: () -> Unit,
  onColorMode: () -> Unit,
  onGrid: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xDD0D1527),
      border = BorderStroke(1.dp, Color(0x336366F1)),
      shadowElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("reader_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "Page $currentPage of $totalPages",
              style = MaterialTheme.typography.labelSmall,
              color = AuraCyan
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBookmark) {
            Icon(
              imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (isBookmarked) AuraAmber else Color.White
            )
          }

          IconButton(onClick = onColorMode) {
            Icon(
              imageVector = Icons.Filled.Palette,
              contentDescription = "Color Mode",
              tint = Color.White
            )
          }

          IconButton(onClick = onGrid) {
            Icon(
              imageVector = Icons.Filled.GridView,
              contentDescription = "Thumbnails",
              tint = Color.White
            )
          }
        }
      }
    }
  }
}

@Composable
fun ReaderBottomBar(
  currentPage: Int,
  totalPages: Int,
  scale: Float,
  onPageChange: (Int) -> Unit,
  onNext: () -> Unit,
  onPrev: () -> Unit,
  onZoomReset: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = Color(0xDD0D1527),
      border = BorderStroke(1.dp, Color(0x3306B6D4)),
      shadowElevation = 12.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 18.dp, vertical = 12.dp)
      ) {
        // Slider scrubber
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "1",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
          )

          Slider(
            value = currentPage.toFloat(),
            onValueChange = { onPageChange(it.toInt()) },
            valueRange = 1f..totalPages.toFloat().coerceAtLeast(1f),
            steps = (totalPages - 2).coerceAtLeast(0),
            colors = SliderDefaults.colors(
              thumbColor = AuraCyan,
              activeTrackColor = AuraIndigo,
              inactiveTrackColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 10.dp)
              .testTag("reader_page_slider")
          )

          Text(
            text = "$totalPages",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
          )
        }

        // Action controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0x33FFFFFF),
            modifier = Modifier.clip(CircleShape)
          ) {
            IconButton(
              onClick = onPrev,
              enabled = currentPage > 1,
              modifier = Modifier.size(38.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Page",
                tint = if (currentPage > 1) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
              )
            }
          }

          if (scale > 1.05f) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AuraIndigo.copy(alpha = 0.3f),
              border = BorderStroke(1.dp, AuraIndigo),
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onZoomReset)
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = "${(scale * 100).toInt()}% • Reset",
                style = MaterialTheme.typography.labelSmall,
                color = AuraCyan,
                fontWeight = FontWeight.Bold
              )
            }
          } else {
            Text(
              text = "Tap page to hide controls",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.5f)
            )
          }

          Surface(
            shape = CircleShape,
            color = Color(0x33FFFFFF),
            modifier = Modifier.clip(CircleShape)
          ) {
            IconButton(
              onClick = onNext,
              enabled = currentPage < totalPages,
              modifier = Modifier.size(38.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Page",
                tint = if (currentPage < totalPages) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}
