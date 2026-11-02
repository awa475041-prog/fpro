package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun CustomUrlDialog(
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirmUrl: (String) -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }

    val presets = listOf(
        "Facebook" to "https://m.facebook.com",
        "Twitter / X" to "https://mobile.twitter.com",
        "Instagram" to "https://www.instagram.com",
        "YouTube" to "https://m.youtube.com",
        "Reddit" to "https://www.reddit.com",
        "Google" to "https://www.google.com"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("custom_url_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تغيير عنوان الموقع (URL)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = urlText,
                    onValueChange = { urlText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("url_text_field"),
                    placeholder = { Text("أدخل رابط الموقع أو كلمة البحث...", fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "مواقع جاهزة وسريعة:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Presets Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    presets.chunked(3).forEach { rowPresets ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowPresets.forEach { (label, url) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { urlText = url },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (urlText == url) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (urlText == url) CyberCyan else DarkBorder
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (urlText == url) CyberCyan else TextPrimaryDark,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondaryDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = { onConfirmUrl(urlText) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("confirm_open_url"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = androidx.compose.ui.graphics.Color(0xFF00363D)
                        )
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فتح الموقع فوراً", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
