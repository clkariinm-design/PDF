package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuraCyan
import com.example.ui.theme.AuraIndigo
import com.example.ui.theme.AuraViolet
import com.example.ui.viewmodel.NavigationTab

@Composable
fun AuraGlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(22.dp),
  backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
  borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
  borderWidth: Dp = 1.dp,
  elevation: Dp = 4.dp,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val surfaceModifier = if (onClick != null) {
    modifier
      .shadow(elevation, shape, clip = false)
      .clip(shape)
      .clickable(onClick = onClick)
  } else {
    modifier
      .shadow(elevation, shape, clip = false)
      .clip(shape)
  }

  Surface(
    modifier = surfaceModifier,
    shape = shape,
    color = backgroundColor,
    border = BorderStroke(
      borderWidth,
      Brush.linearGradient(
        colors = listOf(
          borderColor.copy(alpha = 0.8f),
          borderColor.copy(alpha = 0.2f),
          borderColor.copy(alpha = 0.5f)
        )
      )
    )
  ) {
    content()
  }
}

@Composable
fun AuraGradientButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  gradientColors: List<Color> = listOf(AuraIndigo, AuraCyan),
  enabled: Boolean = true,
  testTag: String = "aura_gradient_button"
) {
  val brush = Brush.horizontalGradient(gradientColors)
  val shape = RoundedCornerShape(16.dp)

  Box(
    modifier = modifier
      .testTag(testTag)
      .shadow(6.dp, shape)
      .clip(shape)
      .background(brush)
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 20.dp, vertical = 14.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = Color.White,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun AuraBadge(
  text: String,
  modifier: Modifier = Modifier,
  containerColor: Color = AuraIndigo.copy(alpha = 0.15f),
  contentColor: Color = AuraCyan
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(containerColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      color = contentColor,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    )
  }
}

@Composable
fun AuraFloatingBottomNav(
  currentTab: NavigationTab,
  onTabSelected: (NavigationTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(32.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
      border = BorderStroke(
        1.dp,
        Brush.horizontalGradient(
          colors = listOf(
            AuraIndigo.copy(alpha = 0.5f),
            AuraCyan.copy(alpha = 0.4f),
            AuraViolet.copy(alpha = 0.3f)
          )
        )
      ),
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth()
        .height(68.dp)
        .testTag("floating_bottom_bar")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        NavigationTab.values().forEach { tab ->
          val isSelected = tab == currentTab
          val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.05f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "tab_scale"
          )
          val indicatorWidth by animateDpAsState(
            targetValue = if (isSelected) 48.dp else 0.dp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "tab_pill"
          )

          val (selectedIcon, unselectedIcon) = when (tab) {
            NavigationTab.HOME -> Icons.Filled.Home to Icons.Outlined.Home
            NavigationTab.LIBRARY -> Icons.Filled.Description to Icons.Outlined.Description
            NavigationTab.TOOLS -> Icons.Filled.Layers to Icons.Outlined.Layers
            NavigationTab.SETTINGS -> Icons.Filled.Settings to Icons.Outlined.Settings
          }

          Column(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(20.dp))
              .clickable(onClick = { onTabSelected(tab) })
              .padding(vertical = 6.dp)
              .scale(scale)
              .testTag("nav_tab_${tab.name.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = if (isSelected) selectedIcon else unselectedIcon,
              contentDescription = tab.label,
              tint = if (isSelected) AuraIndigo else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = tab.label,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Animated subtle glow indicator under active tab
            Box(
              modifier = Modifier
                .width(indicatorWidth)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                  Brush.horizontalGradient(listOf(AuraIndigo, AuraCyan))
                )
            )
          }
        }
      }
    }
  }
}
