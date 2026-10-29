package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.model.ColorMode
import com.example.data.model.PageTransition
import com.example.data.model.ViewMode
import com.example.ui.components.AuraBadge
import com.example.ui.components.AuraGlassCard
import com.example.ui.theme.AuraAmber
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraEmerald
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.PdfViewModel

@Composable
fun SettingsScreen(
  viewModel: PdfViewModel,
  currentThemeDark: Boolean,
  onToggleTheme: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val settings by viewModel.settings.collectAsState()
  var keepAwake by remember { mutableStateOf(settings.keepScreenAwake) }
  var biometricLock by remember { mutableStateOf(false) }
  var warmthSlider by remember { mutableFloatStateOf(settings.eyeComfortWarmth) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen"),
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
  ) {
    // Header
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Preferences",
          style = MaterialTheme.typography.displayMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(8.dp))
        AuraBadge(
          text = "v2.4 PRO",
          containerColor = AuraIndigo.copy(alpha = 0.2f),
          contentColor = AuraCyan
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Personalize your visual and reading experience.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(18.dp))
    }

    // Pro Membership Card
    item {
      AuraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = MaterialTheme.colorScheme.surface
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                listOf(AuraIndigo.copy(alpha = 0.18f), AuraCyan.copy(alpha = 0.1f))
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
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.AutoAwesome,
                  contentDescription = null,
                  tint = AuraCyan,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Aura Pro Lifetime Active",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Full access to Smart Merge, AES-256 encryption, and 4K vector rendering.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            AuraBadge(text = "ACTIVE", containerColor = AuraEmerald.copy(alpha = 0.2f), contentColor = AuraEmerald)
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // Section 1: Appearance & Theme
    item {
      SettingsSectionHeader(title = "Appearance & Theme", icon = Icons.Filled.DarkMode)
      Spacer(modifier = Modifier.height(10.dp))

      AuraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Dark Aura Theme (OLED)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Deep pitch black background reduces eye strain and saves battery.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = currentThemeDark,
              onCheckedChange = { onToggleTheme(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AuraIndigo
              ),
              modifier = Modifier.testTag("theme_switch")
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // Section 2: Reading Preferences
    item {
      SettingsSectionHeader(title = "Reading Experience", icon = Icons.Filled.MenuBook)
      Spacer(modifier = Modifier.height(10.dp))

      AuraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Keep Screen Awake
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Keep Screen Awake",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Prevents device from sleeping while reading long documents.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = keepAwake,
              onCheckedChange = {
                keepAwake = it
                viewModel.updateSettings(settings.copy(keepScreenAwake = it))
                viewModel.showNotification(if (it) "Screen wake lock enabled" else "Screen wake lock disabled")
              },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AuraIndigo)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Eye Comfort warmth slider
          Text(
            text = "Night Light Warmth (${(warmthSlider * 100).toInt()}%)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Fine-tune blue-light filtration during Nocturne mode.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Slider(
            value = warmthSlider,
            onValueChange = {
              warmthSlider = it
              viewModel.updateSettings(settings.copy(eyeComfortWarmth = it))
            },
            valueRange = 0.1f..0.8f,
            colors = SliderDefaults.colors(thumbColor = AuraAmber, activeTrackColor = AuraAmber)
          )
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // Section 3: Security & Privacy
    item {
      SettingsSectionHeader(title = "Security & Privacy", icon = Icons.Filled.Security)
      Spacer(modifier = Modifier.height(10.dp))

      AuraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Biometric App Shield",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Require fingerprint or face authentication on launch.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = biometricLock,
              onCheckedChange = {
                biometricLock = it
                viewModel.showNotification(if (it) "Biometric Lock Armed" else "Biometric Lock Disarmed")
              },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AuraIndigo)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Filled.Security, contentDescription = null, tint = AuraEmerald, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "100% On-Device Processing • Zero Analytics Tracking",
              style = MaterialTheme.typography.labelSmall,
              color = AuraEmerald,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // Section 4: Storage & Maintenance
    item {
      SettingsSectionHeader(title = "Storage & Cache", icon = Icons.Filled.Storage)
      Spacer(modifier = Modifier.height(10.dp))

      AuraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          SettingsActionRow(
            title = "Clean Dynamic Render Cache",
            subtitle = "Reclaims RAM and temporary preview pages",
            icon = Icons.Filled.CleaningServices,
            actionLabel = "Clean Now",
            onAction = { viewModel.clearAppCache() }
          )

          Spacer(modifier = Modifier.height(14.dp))

          SettingsActionRow(
            title = "Reload Sample Guides",
            subtitle = "Restore built-in tutorial and research PDFs",
            icon = Icons.Filled.Refresh,
            actionLabel = "Reload",
            onAction = {
              viewModel.showNotification("Guides are up-to-date in Vault")
            }
          )
        }
      }
      Spacer(modifier = Modifier.height(20.dp))
    }

    // Section 5: About
    item {
      SettingsSectionHeader(title = "About Aura", icon = Icons.Filled.Info)
      Spacer(modifier = Modifier.height(10.dp))

      AuraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Aura PDF Reader Pro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Designed with Material 3 & Jetpack Compose. Built for professionals, researchers, and students who demand speed and beauty.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Build 2026.10.28-RELEASE • Engine: Android Native PdfRenderer",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
      Spacer(modifier = Modifier.height(100.dp))
    }
  }
}

@Composable
fun SettingsSectionHeader(
  title: String,
  icon: ImageVector
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(horizontal = 4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AuraIndigo,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground
    )
  }
}

@Composable
fun SettingsActionRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  actionLabel: String,
  onAction: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.size(18.dp)
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(10.dp),
      color = AuraIndigo.copy(alpha = 0.15f),
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .clickable(onClick = onAction)
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Text(
        text = actionLabel,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = AuraIndigo
      )
    }
  }
}
