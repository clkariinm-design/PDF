package com.example.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
  viewModel: PdfViewModel,
  modifier: Modifier = Modifier
) {
  val documents by viewModel.documents.collectAsState()
  val activeTool by viewModel.activeTool.collectAsState()
  val isProcessing by viewModel.isToolProcessing.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("tools_screen"),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
  ) {
    item {
      Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "PDF Studio Tools",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Spacer(modifier = Modifier.width(8.dp))
          AuraBadge(text = "PRO UNLOCKED", containerColor = AuraIndigo.copy(alpha = 0.2f), contentColor = AuraCyan)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "On-device document engine. Zero cloud uploads, 100% private.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    item {
      ToolHeroCard(
        title = "Smart Merge Studio",
        subtitle = "Combine research, invoices, and reports with one tap",
        badge = "HIGH SPEED",
        icon = Icons.Filled.MergeType,
        color = AuraIndigo,
        onClick = { viewModel.openTool(ToolCategory.MERGE) }
      )
      Spacer(modifier = Modifier.height(14.dp))
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ModernToolCard(
          title = "Split & Extract",
          subtitle = "Pull selected pages",
          icon = Icons.Filled.ContentCut,
          color = AuraViolet,
          badge = "PAGES",
          modifier = Modifier.weight(1f),
          onClick = { viewModel.openTool(ToolCategory.SPLIT) }
        )
        ModernToolCard(
          title = "Compress",
          subtitle = "Save up to 75% size",
          icon = Icons.Filled.Compress,
          color = AuraCyan,
          badge = "SHRINK",
          modifier = Modifier.weight(1f),
          onClick = { viewModel.openTool(ToolCategory.COMPRESS) }
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        ModernToolCard(
          title = "Text to PDF",
          subtitle = "Format typed notes",
          icon = Icons.Filled.Description,
          color = AuraEmerald,
          badge = "CONVERT",
          modifier = Modifier.weight(1f),
          onClick = { viewModel.openTool(ToolCategory.TEXT_TO_PDF) }
        )
        ModernToolCard(
          title = "Cam Scanner",
          subtitle = "Auto-perspective scan",
          icon = Icons.Filled.PhotoCamera,
          color = AuraAmber,
          badge = "ENHANCE",
          modifier = Modifier.weight(1f),
          onClick = { viewModel.openTool(ToolCategory.SCAN_DOC) }
        )
      }
      Spacer(modifier = Modifier.height(12.dp))
    }

    item {
      ModernToolCard(
        title = "Protect & Encrypt",
        subtitle = "Add AES-256 password protection to sensitive files",
        icon = Icons.Filled.Lock,
        color = AuraRose,
        badge = "SECURITY",
        modifier = Modifier.fillMaxWidth(),
        onClick = { viewModel.openTool(ToolCategory.PROTECT) }
      )
      Spacer(modifier = Modifier.height(100.dp))
    }
  }

  // Active Tool Modal Sheet
  if (activeTool != null) {
    ModalBottomSheet(
      onDismissRequest = { if (!isProcessing) viewModel.closeTool() },
      containerColor = MaterialTheme.colorScheme.surface,
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        when (activeTool) {
          ToolCategory.MERGE -> MergeToolSheet(
            documents = documents,
            isProcessing = isProcessing,
            onMerge = { ids, title -> viewModel.executeMergePdfs(ids, title) },
            onCancel = { viewModel.closeTool() }
          )
          ToolCategory.SPLIT -> SplitToolSheet(
            documents = documents,
            isProcessing = isProcessing,
            onSplit = { id, start, end, title -> viewModel.executeSplitPdf(id, start, end, title) },
            onCancel = { viewModel.closeTool() }
          )
          ToolCategory.COMPRESS -> CompressToolSheet(
            documents = documents,
            isProcessing = isProcessing,
            onCompress = { id, quality -> viewModel.executeCompressPdf(id, quality) },
            onCancel = { viewModel.closeTool() }
          )
          ToolCategory.TEXT_TO_PDF -> TextToPdfToolSheet(
            isProcessing = isProcessing,
            onCreate = { title, content -> viewModel.executeTextToPdf(title, content) },
            onCancel = { viewModel.closeTool() }
          )
          ToolCategory.SCAN_DOC -> CamScanToolSheet(
            isProcessing = isProcessing,
            onSave = { bmp, title -> viewModel.executeSaveScannedDocument(bmp, title) },
            onCancel = { viewModel.closeTool() }
          )
          ToolCategory.PROTECT -> ProtectToolSheet(
            documents = documents,
            isProcessing = isProcessing,
            onProtect = { id, pwd -> viewModel.executeProtectPdf(id, pwd) },
            onCancel = { viewModel.closeTool() }
          )
          else -> {}
        }
      }
    }
  }
}

@Composable
fun ToolHeroCard(
  title: String,
  subtitle: String,
  badge: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit
) {
  AuraGlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("tool_hero_merge"),
    shape = RoundedCornerShape(24.dp),
    backgroundColor = MaterialTheme.colorScheme.surface,
    elevation = 6.dp,
    onClick = onClick
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(color.copy(alpha = 0.16f), color.copy(alpha = 0.04f))
          )
        )
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          AuraBadge(text = badge, containerColor = color.copy(alpha = 0.18f), contentColor = color)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }
  }
}

@Composable
fun ModernToolCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  color: Color,
  badge: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    shadowElevation = 3.dp,
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
          )
        }
        AuraBadge(text = badge, containerColor = color.copy(alpha = 0.12f), contentColor = color)
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

// 1. Merge Tool Sheet
@Composable
fun MergeToolSheet(
  documents: List<PdfDocumentItem>,
  isProcessing: Boolean,
  onMerge: (List<String>, String) -> Unit,
  onCancel: () -> Unit
) {
  val selectedIds = remember { mutableStateListOf<String>() }
  var outputTitle by remember { mutableStateOf("Unified_Aura_Report") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Text(
      text = "Merge PDF Documents",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Select 2 or more documents to combine in sequential order.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = outputTitle,
      onValueChange = { outputTitle = it },
      label = { Text("Resulting File Title") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "Choose Files (${selectedIds.size} selected):",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
    ) {
      items(documents) { doc ->
        val isSelected = selectedIds.contains(doc.id)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (isSelected) AuraIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          border = BorderStroke(1.dp, if (isSelected) AuraIndigo else Color.Transparent),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
              if (isSelected) selectedIds.remove(doc.id) else selectedIds.add(doc.id)
            }
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.Description,
              contentDescription = null,
              tint = if (isSelected) AuraIndigo else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = doc.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "${doc.pageCount} pages • ${doc.fileSizeFormatted}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (isProcessing) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator(color = AuraIndigo, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text("Merging vector pages into new PDF...", style = MaterialTheme.typography.bodyMedium)
      }
    } else {
      AuraGradientButton(
        text = "Merge Selected (${selectedIds.size})",
        onClick = { onMerge(selectedIds.toList(), outputTitle) },
        enabled = selectedIds.size >= 2,
        modifier = Modifier.fillMaxWidth()
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}

// 2. Split Tool Sheet
@Composable
fun SplitToolSheet(
  documents: List<PdfDocumentItem>,
  isProcessing: Boolean,
  onSplit: (String, Int, Int, String) -> Unit,
  onCancel: () -> Unit
) {
  var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
  var startPage by remember { mutableIntStateOf(1) }
  var endPage by remember { mutableIntStateOf(selectedDoc?.pageCount?.coerceAtMost(2) ?: 1) }
  var outputTitle by remember { mutableStateOf("Extracted_Pages") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Text(
      text = "Split & Extract Pages",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Extract specific page ranges into a dedicated new PDF document.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (selectedDoc != null) {
      Text(
        text = "Source: ${selectedDoc!!.title} (${selectedDoc!!.pageCount} pages)",
        style = MaterialTheme.typography.titleSmall,
        color = AuraIndigo,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = startPage.toString(),
          onValueChange = { it.toIntOrNull()?.let { v -> startPage = v.coerceIn(1, selectedDoc!!.pageCount) } },
          label = { Text("Start Page") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
          value = endPage.toString(),
          onValueChange = { it.toIntOrNull()?.let { v -> endPage = v.coerceIn(startPage, selectedDoc!!.pageCount) } },
          label = { Text("End Page") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = outputTitle,
        onValueChange = { outputTitle = it },
        label = { Text("Output Title") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))

      if (isProcessing) {
        CircularProgressIndicator(color = AuraViolet, modifier = Modifier.size(24.dp))
      } else {
        AuraGradientButton(
          text = "Extract Pages $startPage to $endPage",
          onClick = { onSplit(selectedDoc!!.id, startPage, endPage, outputTitle) },
          gradientColors = listOf(AuraViolet, AuraCyan),
          modifier = Modifier.fillMaxWidth()
        )
      }
    } else {
      Text("No documents available to split.")
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}

// 3. Compress Tool Sheet
@Composable
fun CompressToolSheet(
  documents: List<PdfDocumentItem>,
  isProcessing: Boolean,
  onCompress: (String, Int) -> Unit,
  onCancel: () -> Unit
) {
  var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
  var compressionQuality by remember { mutableStateOf(70f) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Text(
      text = "Smart PDF Compress",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Downsamples high-res background scans while keeping vectors sharp.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (selectedDoc != null) {
      Text(
        text = "Target: ${selectedDoc!!.title} (${selectedDoc!!.fileSizeFormatted})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = AuraCyan
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Quality Preset: ${compressionQuality.toInt()}% (Approx 60% size drop)",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium
      )

      Slider(
        value = compressionQuality,
        onValueChange = { compressionQuality = it },
        valueRange = 40f..90f,
        steps = 4,
        colors = SliderDefaults.colors(thumbColor = AuraCyan, activeTrackColor = AuraCyan)
      )

      Spacer(modifier = Modifier.height(18.dp))

      if (isProcessing) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(color = AuraCyan, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(12.dp))
          Text("Optimizing bitmap tables and streams...", style = MaterialTheme.typography.bodyMedium)
        }
      } else {
        AuraGradientButton(
          text = "Compress & Save",
          onClick = { onCompress(selectedDoc!!.id, compressionQuality.toInt()) },
          gradientColors = listOf(AuraCyan, AuraIndigo),
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
  }
}

// 4. Text to PDF Sheet
@Composable
fun TextToPdfToolSheet(
  isProcessing: Boolean,
  onCreate: (String, String) -> Unit,
  onCancel: () -> Unit
) {
  var title by remember { mutableStateOf("Project_Executive_Brief") }
  var content by remember {
    mutableStateOf(
      """Executive Summary:
Next-generation digital document architecture requires distraction-free reading, responsive edge-to-edge layout, and native bitmap rendering.

Key Milestones:
• Milestone A: Sub-pixel font smoothing and color temperature adjustments for sepia and dark OLED displays.
• Milestone B: Zero-permission file ingestion leveraging Android Storage Access Framework.
• Milestone C: Real-time multi-page compilation with custom vector headers and automated page indexing.

Prepared by: Aura PDF Engineering Team"""
    )
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Text(
      text = "Compile Notes to PDF",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Transforms formatted text and notes into a clean, typed PDF document.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = title,
      onValueChange = { title = it },
      label = { Text("Document Title") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedTextField(
      value = content,
      onValueChange = { content = it },
      label = { Text("Notes Content / Paragraphs") },
      modifier = Modifier
        .fillMaxWidth()
        .height(180.dp),
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (isProcessing) {
      CircularProgressIndicator(color = AuraEmerald, modifier = Modifier.size(24.dp))
    } else {
      AuraGradientButton(
        text = "Generate PDF Document",
        onClick = { onCreate(title, content) },
        gradientColors = listOf(AuraEmerald, AuraCyan),
        modifier = Modifier.fillMaxWidth()
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}

// 5. Cam Scan Tool Sheet
@Composable
fun CamScanToolSheet(
  isProcessing: Boolean,
  onSave: (Bitmap, String) -> Unit,
  onCancel: () -> Unit
) {
  var scanTitle by remember { mutableStateOf("Invoice_Receipt_Scan") }
  var selectedFilter by remember { mutableStateOf("Magic Color") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Text(
      text = "Aura Document Scanner",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Simulates instant paper capture with edge contrast enhancement.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(14.dp))

    OutlinedTextField(
      value = scanTitle,
      onValueChange = { scanTitle = it },
      label = { Text("Scan Document Title") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "Enhancement Filter:",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      listOf("Magic Color", "High Contrast", "Clean B&W").forEach { filter ->
        val isSelected = filter == selectedFilter
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isSelected) AuraAmber.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
          border = BorderStroke(1.dp, if (isSelected) AuraAmber else Color.Transparent),
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { selectedFilter = filter }
            .padding(vertical = 10.dp)
        ) {
          Text(
            text = filter,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AuraAmber else MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    if (isProcessing) {
      CircularProgressIndicator(color = AuraAmber, modifier = Modifier.size(24.dp))
    } else {
      AuraGradientButton(
        text = "Capture & Compile to PDF",
        onClick = {
          // Generate simulated clean scanned page bitmap
          val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
          val canvas = Canvas(bmp)
          canvas.drawColor(AndroidColor.parseColor("#FDFBF7"))

          val p = Paint().apply {
            color = AndroidColor.parseColor("#1E293B")
            textSize = 24f
            isAntiAlias = true
          }
          canvas.drawText("OFFICIAL SCANNED RECEIPT", 60f, 100f, p)

          p.textSize = 14f
          p.color = AndroidColor.parseColor("#475569")
          canvas.drawText("Transaction ID: #AUR-984210", 60f, 140f, p)
          canvas.drawText("Timestamp: ${System.currentTimeMillis()}", 60f, 165f, p)
          canvas.drawText("Filter Applied: $selectedFilter", 60f, 190f, p)
          canvas.drawText("Status: Paid & Verified", 60f, 215f, p)

          onSave(bmp, scanTitle)
        },
        gradientColors = listOf(AuraAmber, AuraRose),
        modifier = Modifier.fillMaxWidth()
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}

// 6. Protect Tool Sheet
@Composable
fun ProtectToolSheet(
  documents: List<PdfDocumentItem>,
  isProcessing: Boolean,
  onProtect: (String, String) -> Unit,
  onCancel: () -> Unit
) {
  var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
  var password by remember { mutableStateOf("AuraSecret@2026") }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp)
  ) {
    Text(
      text = "Protect & Encrypt PDF",
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = "Applies 256-bit password locking and security permissions.",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (selectedDoc != null) {
      Text(
        text = "Target: ${selectedDoc!!.title}",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = AuraRose
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Encryption Password") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))

      if (isProcessing) {
        CircularProgressIndicator(color = AuraRose, modifier = Modifier.size(24.dp))
      } else {
        AuraGradientButton(
          text = "Encrypt & Lock Document",
          onClick = { onProtect(selectedDoc!!.id, password) },
          gradientColors = listOf(AuraRose, AuraViolet),
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
  }
}
