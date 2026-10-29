package com.example.ui.screens.home

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocumentItem
import com.example.data.model.ToolCategory
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraGlassCard
import com.example.ui.components.AuraGradientButton
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraEmerald
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraRose
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.PdfViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: PdfViewModel,
  modifier: Modifier = Modifier
) {
  val documents by viewModel.documents.collectAsState()
  val favorites = remember(documents) { documents.filter { it.isFavorite } }
  val storageStats = remember(documents) { viewModel.repository.getStorageStats() }

  val openPdfLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let {
      val name = it.lastPathSegment?.substringAfterLast('/') ?: "Imported_Document.pdf"
      viewModel.importDocumentFromUri(it, name)
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("home_screen"),
    contentPadding = PaddingValues(bottom = 100.dp)
  ) {
    // Top Greeting & Header
    item {
      HomeTopHeader(
        docCount = documents.size,
        onSearchClick = { viewModel.setTab(com.example.ui.viewmodel.NavigationTab.LIBRARY) }
      )
    }

    // Large "Open PDF" Hero Card
    item {
      OpenPdfHeroCard(
        onOpenPdfClick = {
          openPdfLauncher.launch(arrayOf("application/pdf"))
        }
      )
    }

    // Quick Tools Row
    item {
      QuickToolsSection(
        onToolClick = { category -> viewModel.openTool(category) }
      )
    }

    // Storage and Vault Analytics
    item {
      StorageAnalyticsCard(
        totalFiles = storageStats.totalPdfFiles,
        totalSize = storageStats.totalSpaceFormatted,
        favoriteCount = storageStats.favoriteCount
      )
    }

    // Recent Documents Section
    item {
      SectionHeader(
        title = "Recent Documents",
        count = documents.size,
        actionLabel = "View All",
        onActionClick = { viewModel.setTab(com.example.ui.viewmodel.NavigationTab.LIBRARY) }
      )
    }

    if (documents.isEmpty()) {
      item {
        EmptyDocumentsCard(
          onOpenPdfClick = { openPdfLauncher.launch(arrayOf("application/pdf")) }
        )
      }
    } else {
      items(documents.take(5), key = { it.id }) { doc ->
        RecentDocumentCard(
          document = doc,
          onClick = { viewModel.openDocument(doc) },
          onFavoriteToggle = { viewModel.repository.toggleFavorite(doc.id) },
          renderThumbnail = {
            var thumbBmp by remember { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(doc.filePath) {
              thumbBmp = viewModel.renderEngine.renderPage(doc.filePath, 0, 240)
            }
            thumbBmp
          }
        )
      }
    }

    // Favorites Carousel if any
    if (favorites.isNotEmpty()) {
      item {
        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(
          title = "Favorites & Starred",
          count = favorites.size
        )
      }

      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(favorites, key = { "fav_${it.id}" }) { favDoc ->
            FavoriteDocumentCard(
              document = favDoc,
              onClick = { viewModel.openDocument(favDoc) }
            )
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }
  }
}

@Composable
fun HomeTopHeader(
  docCount: Int,
  onSearchClick: () -> Unit
) {
  val dateStr = remember {
    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 18.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "AURA",
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.Black,
          color = MaterialTheme.colorScheme.onBackground,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        AuraBadge(
          text = "PRO",
          containerColor = AuraIndigo.copy(alpha = 0.2f),
          contentColor = AuraCyan
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = dateStr,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.size(44.dp)
      ) {
        IconButton(
          onClick = onSearchClick,
          modifier = Modifier.testTag("home_search_button")
        ) {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "Search",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
fun OpenPdfHeroCard(
  onOpenPdfClick: () -> Unit
) {
  AuraGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 6.dp)
      .testTag("hero_open_pdf_card"),
    shape = RoundedCornerShape(26.dp),
    backgroundColor = MaterialTheme.colorScheme.surface,
    elevation = 8.dp,
    onClick = onOpenPdfClick
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(
              AuraIndigo.copy(alpha = 0.14f),
              AuraCyan.copy(alpha = 0.08f),
              Color.Transparent
            )
          )
        )
        .padding(24.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          AuraBadge(
            text = "INSTANT RENDER",
            containerColor = AuraCyan.copy(alpha = 0.15f),
            contentColor = AuraCyan
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Open Any PDF",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Read books, research papers, reports & scans in ultra-high resolution.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
          Spacer(modifier = Modifier.height(16.dp))

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = AuraIndigo,
            shadowElevation = 4.dp
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Choose File",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Glowing emblem
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(
                  AuraIndigo.copy(alpha = 0.35f),
                  AuraCyan.copy(alpha = 0.12f),
                  Color.Transparent
                )
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.PictureAsPdf,
            contentDescription = null,
            tint = AuraIndigo,
            modifier = Modifier.size(38.dp)
          )
        }
      }
    }
  }
}

@Composable
fun QuickToolsSection(
  onToolClick: (ToolCategory) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 14.dp)
  ) {
    Text(
      text = "Smart Document Suite",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickToolPill(
        title = "Merge",
        icon = Icons.Filled.MergeType,
        color = AuraIndigo,
        modifier = Modifier.weight(1f),
        onClick = { onToolClick(ToolCategory.MERGE) }
      )
      QuickToolPill(
        title = "Split",
        icon = Icons.Filled.ContentCut,
        color = AuraViolet,
        modifier = Modifier.weight(1f),
        onClick = { onToolClick(ToolCategory.SPLIT) }
      )
      QuickToolPill(
        title = "Compress",
        icon = Icons.Filled.Compress,
        color = AuraCyan,
        modifier = Modifier.weight(1f),
        onClick = { onToolClick(ToolCategory.COMPRESS) }
      )
      QuickToolPill(
        title = "Cam Scan",
        icon = Icons.Filled.PhotoCamera,
        color = AuraEmerald,
        modifier = Modifier.weight(1f),
        onClick = { onToolClick(ToolCategory.SCAN_DOC) }
      )
    }
  }
}

@Composable
fun QuickToolPill(
  title: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    shadowElevation = 2.dp,
    modifier = modifier
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = color,
          modifier = Modifier.size(19.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun StorageAnalyticsCard(
  totalFiles: Int,
  totalSize: String,
  favoriteCount: Int
) {
  AuraGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 6.dp),
    shape = RoundedCornerShape(20.dp),
    backgroundColor = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AuraIndigo.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.Security,
            contentDescription = null,
            tint = AuraIndigo,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = "Local Vault Storage",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "$totalFiles files • $totalSize stored securely on device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      AuraBadge(
        text = "AES SAFE",
        containerColor = AuraEmerald.copy(alpha = 0.15f),
        contentColor = AuraEmerald
      )
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  count: Int,
  actionLabel: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(horizontal = 8.dp, vertical = 2.dp)
      ) {
        Text(
          text = count.toString(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Bold
        )
      }
    }

    if (actionLabel != null && onActionClick != null) {
      Text(
        text = actionLabel,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = AuraCyan,
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .clickable(onClick = onActionClick)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}

@Composable
fun RecentDocumentCard(
  document: PdfDocumentItem,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit,
  renderThumbnail: @Composable () -> Bitmap?
) {
  val thumbnail = renderThumbnail()

  AuraGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 6.dp)
      .testTag("doc_card_${document.id}"),
    shape = RoundedCornerShape(18.dp),
    backgroundColor = MaterialTheme.colorScheme.surface,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Document Thumbnail / Cover
      Box(
        modifier = Modifier
          .size(width = 54.dp, height = 72.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        if (thumbnail != null) {
          Image(
            bitmap = thumbnail.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Icon(
            imageVector = Icons.Filled.Description,
            contentDescription = null,
            tint = AuraIndigo,
            modifier = Modifier.size(28.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = document.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (document.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (document.isFavorite) AuraRose else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "${document.pageCount} pages",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
          )
          Text(
            text = document.fileSizeFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reading progress indicator
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          LinearProgressIndicator(
            progress = { document.readProgressFraction },
            modifier = Modifier
              .weight(1f)
              .height(4.dp)
              .clip(CircleShape),
            color = AuraIndigo,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "p. ${document.lastReadPage}/${document.pageCount}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}

@Composable
fun FavoriteDocumentCard(
  document: PdfDocumentItem,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, AuraIndigo.copy(alpha = 0.3f)),
    shadowElevation = 3.dp,
    modifier = Modifier
      .width(150.dp)
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(AuraIndigo.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Filled.PictureAsPdf,
            contentDescription = null,
            tint = AuraIndigo,
            modifier = Modifier.size(18.dp)
          )
        }

        Icon(
          imageVector = Icons.Filled.Favorite,
          contentDescription = null,
          tint = AuraRose,
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = document.title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "${document.pageCount} pages",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun EmptyDocumentsCard(
  onOpenPdfClick: () -> Unit
) {
  AuraGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    shape = RoundedCornerShape(20.dp),
    backgroundColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Filled.Description,
        contentDescription = null,
        tint = AuraIndigo.copy(alpha = 0.5f),
        modifier = Modifier.size(48.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "No Documents Found",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Tap below to import a PDF file or try one of the built-in sample guides.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      AuraGradientButton(
        text = "Import PDF",
        onClick = onOpenPdfClick,
        icon = Icons.Filled.Add
      )
    }
  }
}
