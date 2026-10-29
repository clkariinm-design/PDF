package com.example.ui.screens.library

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfDocumentItem
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraGlassCard
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraRose
import com.example.ui.viewmodel.PdfViewModel

@Composable
fun LibraryScreen(
  viewModel: PdfViewModel,
  modifier: Modifier = Modifier
) {
  val documents by viewModel.documents.collectAsState()
  val searchQuery by viewModel.librarySearch.collectAsState()
  val tagFilter by viewModel.libraryTagFilter.collectAsState()

  val openPdfLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let {
      val name = it.lastPathSegment?.substringAfterLast('/') ?: "Document.pdf"
      viewModel.importDocumentFromUri(it, name)
    }
  }

  val filteredDocuments = remember(documents, searchQuery, tagFilter) {
    documents.filter { doc ->
      val matchesSearch = searchQuery.isBlank() || doc.title.contains(searchQuery, ignoreCase = true)
      val matchesTag = tagFilter == null || tagFilter == "All" ||
        (tagFilter == "Favorites" && doc.isFavorite) ||
        doc.tags.any { it.equals(tagFilter, ignoreCase = true) }
      matchesSearch && matchesTag
    }
  }

  val allTags = remember(documents) {
    val list = mutableListOf("All", "Favorites")
    documents.flatMap { it.tags }.distinct().forEach {
      if (!list.contains(it)) list.add(it)
    }
    list
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("library_screen"),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Document Vault",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${filteredDocuments.size} files available",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = CircleShape,
          color = AuraIndigo,
          shadowElevation = 4.dp
        ) {
          IconButton(
            onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) },
            modifier = Modifier.testTag("library_add_pdf_button")
          ) {
            Icon(Icons.Filled.Add, contentDescription = "Add PDF", tint = Color.White)
          }
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Modern Search Bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setLibrarySearch(it) },
        placeholder = { Text("Search by filename or title...", style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = {
          Icon(Icons.Filled.Search, contentDescription = null, tint = AuraIndigo)
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.setLibrarySearch("") }) {
              Icon(Icons.Filled.Clear, contentDescription = "Clear")
            }
          }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AuraIndigo,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("library_search_input")
      )
      Spacer(modifier = Modifier.height(14.dp))
    }

    // Filter Chips Row
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(allTags) { tag ->
          val isSelected = (tagFilter == null && tag == "All") || tagFilter == tag
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) AuraIndigo else MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, if (isSelected) AuraIndigo else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable { viewModel.setLibraryTag(if (tag == "All") null else tag) }
              .padding(horizontal = 14.dp, vertical = 8.dp)
          ) {
            Text(
              text = tag,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }

    if (filteredDocuments.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Filled.Description,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No matching documents",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Try clearing search or check other category filters.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(filteredDocuments, key = { it.id }) { doc ->
        LibraryDocumentRow(
          document = doc,
          onClick = { viewModel.openDocument(doc) },
          onFavoriteToggle = { viewModel.repository.toggleFavorite(doc.id) },
          onDelete = { viewModel.deleteDoc(doc.id) },
          renderThumbnail = {
            var thumbBmp by remember { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(doc.filePath) {
              thumbBmp = viewModel.renderEngine.renderPage(doc.filePath, 0, 240)
            }
            thumbBmp
          }
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    item {
      Spacer(modifier = Modifier.height(90.dp))
    }
  }
}

@Composable
fun LibraryDocumentRow(
  document: PdfDocumentItem,
  onClick: () -> Unit,
  onFavoriteToggle: () -> Unit,
  onDelete: () -> Unit,
  renderThumbnail: @Composable () -> Bitmap?
) {
  var showMenu by remember { mutableStateOf(false) }
  val thumbnail = renderThumbnail()

  AuraGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("library_item_${document.id}"),
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
      Box(
        modifier = Modifier
          .size(width = 50.dp, height = 66.dp)
          .clip(RoundedCornerShape(8.dp))
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
            modifier = Modifier.size(24.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = document.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${document.pageCount} pages • ${document.fileSizeFormatted}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          document.tags.take(2).forEach { tag ->
            AuraBadge(
              text = tag,
              containerColor = AuraIndigo.copy(alpha = 0.12f),
              contentColor = AuraIndigo
            )
          }
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
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

        Box {
          IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.MoreVert,
              contentDescription = "Options",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Open Document") },
              onClick = {
                showMenu = false
                onClick()
              }
            )
            DropdownMenuItem(
              text = { Text(if (document.isFavorite) "Remove from Starred" else "Add to Starred") },
              onClick = {
                showMenu = false
                onFavoriteToggle()
              }
            )
            DropdownMenuItem(
              text = { Text("Delete Document", color = MaterialTheme.colorScheme.error) },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }
    }
  }
}
