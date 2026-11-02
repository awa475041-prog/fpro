package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    uiState: WebTurboUiState,
    onDismiss: () -> Unit,
    onToggleAdBlocker: () -> Unit,
    onToggleVideoDownloader: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onToggleDistractionFree: () -> Unit,
    onToggleDomAcceleration: () -> Unit,
    onOpenUrlDialog: () -> Unit,
    onClearCache: () -> Unit,
    onReloadPage: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = DarkSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = CyberCyan.copy(alpha = 0.5f))
        },
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .border(1.dp, CyberCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "WebTurbo Icon",
                            tint = CyberCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "إعدادات الحقن والتسريع",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "WebTurbo Injection Engine v2.4",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonEmerald.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "مفعل ونشط",
                        color = NeonEmerald,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Real-time HUD stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "زمن الاستجابة",
                    value = "${uiState.pageLoadTimeMs} ms",
                    icon = Icons.Default.Speed,
                    tint = CyberCyan
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "إعلانات محجوبة",
                    value = "${uiState.blockedAdsCount}",
                    icon = Icons.Default.Shield,
                    tint = NeonEmerald
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "فيديوهات تم رصدها",
                    value = "${uiState.detectedVideos.size}",
                    icon = Icons.Default.Download,
                    tint = ElectricBlue
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ميزات الحقن الفوري (Injection Controls)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = CyberCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Feature Switch Items
            FeatureSwitchCard(
                title = "حظر الإعلانات والمنشورات الممولة",
                subtitle = "إخفاء فوري للمنشورات الترويجية والإعلانات المزعجة",
                icon = Icons.Default.Block,
                checked = uiState.isAdBlockerEnabled,
                onCheckedChange = { onToggleAdBlocker() },
                testTag = "toggle_ad_blocker"
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureSwitchCard(
                title = "زر تنزيل الفيديوهات داخل الصفحة",
                subtitle = "حقن زر عائم وثابت فوق كل فيديو لتنزيله بجودة فائقة",
                icon = Icons.Default.FileDownload,
                checked = uiState.isVideoDownloaderEnabled,
                onCheckedChange = { onToggleVideoDownloader() },
                testTag = "toggle_video_downloader"
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureSwitchCard(
                title = "الوضع النقي - إخفاء الأشرطة",
                subtitle = "إخفاء الشريط العلوي واللافتات الثابتة لتجربة شاشة كاملة",
                icon = Icons.Default.Fullscreen,
                checked = uiState.isDistractionFreeEnabled,
                onCheckedChange = { onToggleDistractionFree() },
                testTag = "toggle_distraction_free"
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureSwitchCard(
                title = "الوضع المظلم الفائق (AMOLED Dark)",
                subtitle = "حقن ألوان سوداء داكنة لحماية العين وتوفير البطارية",
                icon = Icons.Default.DarkMode,
                checked = uiState.isDarkModeEnabled,
                onCheckedChange = { onToggleDarkMode() },
                testTag = "toggle_dark_mode"
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureSwitchCard(
                title = "التسريع العتادي وتخزين DOM",
                subtitle = "تسريع الرندر عبر GPU وتخزين الملفات محلياً لأقصى سرعة",
                icon = Icons.Default.ElectricBolt,
                checked = uiState.isDomAccelerationEnabled,
                onCheckedChange = { onToggleDomAcceleration() },
                testTag = "toggle_dom_acceleration"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "إجراءات التحكم السريع",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = CyberCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenUrlDialog()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_change_url"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تغيير الموقع", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        onClearCache()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_clear_cache"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مسح الكاش", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        onReloadPage()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("action_reload_page"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color(0xFF00363D))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تحديث", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FeatureSwitchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (checked) DarkSurfaceVariant else DarkSurfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) CyberCyan.copy(alpha = 0.4f) else DarkBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked) CyberCyan.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) CyberCyan else TextSecondaryDark,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (checked) TextPrimaryDark else TextSecondaryDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CyberCyan,
                    checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                    uncheckedThumbColor = TextSecondaryDark,
                    uncheckedTrackColor = DarkBackground
                )
            )
        }
    }
}
