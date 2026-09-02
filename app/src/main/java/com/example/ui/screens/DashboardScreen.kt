package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BanglaUtils
import com.example.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val schoolInfo by viewModel.schoolInfo.collectAsState()
    val students by viewModel.allStudents.collectAsState()
    val customFields by viewModel.customFields.collectAsState()
    val formulaRules by viewModel.formulaRules.collectAsState()

    // Calculated Dynamic Values
    val totalStudents = students.size
    val currentStudents = students.filter { it.status == "Current" || it.status == "বর্তমান" || it.status == "Active" || it.status == "সক্রিয়" }
    val totalBoys = currentStudents.count { it.gender == "ছাত্র" }
    val totalGirls = currentStudents.count { it.gender == "ছাত্রী" }
    val specialNeedsCount = currentStudents.count { it.isSpecialNeeds }

    // Internal vs External calculation based on dynamic formula / village
    val internalCount = currentStudents.count { viewModel.getStudentCategory(it) == "অভ্যন্তরীণ" }
    val externalCount = currentStudents.count { viewModel.getStudentCategory(it) == "বহিরাগত" }

    // Percentages
    val effectiveCount = if (currentStudents.isNotEmpty()) currentStudents.size else 1
    val boyPercentage = (totalBoys * 100f / effectiveCount).toInt()
    val girlPercentage = 100 - boyPercentage
    val internalPercentage = (internalCount * 100f / effectiveCount).toInt()
    val externalPercentage = 100 - internalPercentage

    // Classwise count map
    val classCounts = currentStudents.groupBy { it.studentClass }
        .mapValues { it.value.size }

    val defaultClasses = listOf("প্রাক-প্রাথমিক ৪+", "প্রাক-প্রাথমিক ৫+", "১ম শ্রেণি", "২য় শ্রেণি", "৩য় শ্রেণি", "৪র্থ শ্রেণি", "৫ম শ্রেণি")
    val maxClassCount = (defaultClasses.maxOfOrNull { classCounts[it] ?: 0 } ?: 1).coerceAtLeast(1)

    // School Profile Logo
    val schoolLogoBitmap = remember(schoolInfo?.logoUri) {
        try {
            val uriStr = schoolInfo?.logoUri
            if (!uriStr.isNullOrBlank() && uriStr.startsWith("data:image")) {
                val b64 = uriStr.substringAfter("base64,")
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. MODERN HERO SCHOOL BANNER
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_header_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            border = CardDefaults.outlinedCardBorder(enabled = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (schoolLogoBitmap != null) {
                            Image(
                                bitmap = schoolLogoBitmap.asImageBitmap(),
                                contentDescription = "বিদ্যালয়ের প্রোফাইল ছবি",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = "School Logo",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schoolInfo?.schoolName ?: "১৫৪ নং পশ্চিম রামপুর সরকারি প্রাথমিক বিদ্যালয়",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = schoolInfo?.tagline ?: "জ্ঞান, মনন ও স্বপ্নের সোপান",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { onNavigateToSection("settings") },
                        modifier = Modifier.testTag("btn_dashboard_edit_school")
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit School Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Tag, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "EMIS: ${schoolInfo?.emisCode ?: "134251"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                        Text(
                            text = "প্রধান: ${schoolInfo?.headTeacherName ?: "মো: রফিকুল ইসলাম"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "সেশন ${BanglaUtils.toBanglaDigits(2025)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. KEY PERFORMANCE INDICATORS (SOLID M3 CARDS)
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "শিক্ষার্থী সারসংক্ষেপ ও প্রধান পরিসংখ্যান",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "ফিল্টার করতে ট্যাপ করুন ➔",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Row 1: Total, Boys, Girls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModernMetricCard(
                title = "মোট শিক্ষার্থী",
                value = BanglaUtils.toBanglaDigits(totalStudents),
                badgeText = "সকল নথিভুক্ত",
                icon = Icons.Filled.Groups,
                accentColor = Color(0xFF00695C),
                containerColor = Color(0xFFE0F2F1),
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterClass.value = null
                    viewModel.filterGender.value = null
                    viewModel.filterStatus.value = "ALL"
                    onNavigateToSection("students")
                }
            )

            ModernMetricCard(
                title = "মোট ছাত্র",
                value = BanglaUtils.toBanglaDigits(totalBoys),
                badgeText = "${BanglaUtils.toBanglaDigits(boyPercentage)}% অনুপাত",
                icon = Icons.Filled.Male,
                accentColor = Color(0xFF1565C0),
                containerColor = Color(0xFFE3F2FD),
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterGender.value = "ছাত্র"
                    viewModel.filterClass.value = null
                    viewModel.filterStatus.value = "Current"
                    onNavigateToSection("students")
                }
            )

            ModernMetricCard(
                title = "মোট ছাত্রী",
                value = BanglaUtils.toBanglaDigits(totalGirls),
                badgeText = "${BanglaUtils.toBanglaDigits(girlPercentage)}% অনুপাত",
                icon = Icons.Filled.Female,
                accentColor = Color(0xFFC2185B),
                containerColor = Color(0xFFFCE4EC),
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterGender.value = "ছাত্রী"
                    viewModel.filterClass.value = null
                    viewModel.filterStatus.value = "Current"
                    onNavigateToSection("students")
                }
            )
        }

        // Row 2: Special Needs, Internal, External
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModernMetricCard(
                title = "বিশেষ চাহিদা",
                value = BanglaUtils.toBanglaDigits(specialNeedsCount),
                badgeText = "বিশেষ সেবা",
                icon = Icons.Filled.Accessible,
                accentColor = Color(0xFFEF6C00),
                containerColor = Color(0xFFFFF3E0),
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.searchQuery.value = ""
                    viewModel.filterClass.value = null
                    viewModel.filterSpecialNeeds.value = true
                    onNavigateToSection("students")
                }
            )

            ModernMetricCard(
                title = "অভ্যন্তরীণ",
                value = BanglaUtils.toBanglaDigits(internalCount),
                badgeText = "${BanglaUtils.toBanglaDigits(internalPercentage)}% ক্যাচমেন্ট",
                icon = Icons.Filled.Home,
                accentColor = Color(0xFF6A1B9A),
                containerColor = Color(0xFFF3E5F5),
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterClass.value = null
                    onNavigateToSection("students")
                }
            )

            ModernMetricCard(
                title = "বহিরাগত",
                value = BanglaUtils.toBanglaDigits(externalCount),
                badgeText = "${BanglaUtils.toBanglaDigits(externalPercentage)}% বাইরের",
                icon = Icons.Filled.DirectionsBus,
                accentColor = Color(0xFF283593),
                containerColor = Color(0xFFEDE7F6),
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterClass.value = null
                    onNavigateToSection("students")
                }
            )
        }

        // ==========================================
        // 3. CLASS-WISE ENROLLMENT BAR CHART & GRAPH
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_class_chart_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            border = CardDefaults.outlinedCardBorder(enabled = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "শ্রেণিভিত্তিক শিক্ষার্থী বিশ্লেষণ চার্ট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "নির্দিষ্ট শ্রেণিতে সরাসরি যেতে বারে ট্যাপ করুন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "৭টি শ্রেণি",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Visual Bar Graph
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val barColors = listOf(
                        Color(0xFF00897B),
                        Color(0xFF00ACC1),
                        Color(0xFF1E88E5),
                        Color(0xFF5E35B1),
                        Color(0xFF8E24AA),
                        Color(0xFFD81B60),
                        Color(0xFFE53935)
                    )

                    defaultClasses.forEachIndexed { index, className ->
                        val count = classCounts[className] ?: 0
                        val fraction = if (maxClassCount > 0) (count.toFloat() / maxClassCount).coerceIn(0.08f, 1f) else 0.08f
                        val color = barColors[index % barColors.size]

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    viewModel.filterClass.value = className
                                    viewModel.filterGender.value = null
                                    viewModel.filterStatus.value = "Current"
                                    onNavigateToSection("students")
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Value Pill
                            Surface(
                                color = if (count > 0) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = BanglaUtils.toBanglaDigits(count),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Vertical Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.65f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(color)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Short Class Name
                            val shortName = when (className) {
                                "প্রাক-প্রাথমিক ৪+" -> "প্রাক ৪+"
                                "প্রাক-প্রাথমিক ৫+" -> "প্রাক ৫+"
                                "১ম শ্রেণি" -> "১ম"
                                "২য় শ্রেণি" -> "২য়"
                                "৩য় শ্রেণি" -> "৩য়"
                                "৪র্থ শ্রেণি" -> "৪র্থ"
                                "৫ম শ্রেণি" -> "৫ম"
                                else -> className
                            }

                            Text(
                                text = shortName,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Fast Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(defaultClasses) { className ->
                        val count = classCounts[className] ?: 0
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.filterClass.value = className
                                    viewModel.filterGender.value = null
                                    viewModel.filterStatus.value = "Current"
                                    onNavigateToSection("students")
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = CardDefaults.outlinedCardBorder(enabled = true)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Filled.Class, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "$className (${BanglaUtils.toBanglaDigits(count)})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. GENDER & CATCHMENT RATIO VISUALIZERS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Gender Ratio Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_gender_ratio_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                border = CardDefaults.outlinedCardBorder(enabled = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "লিঙ্গ অনুপাত",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Filled.PieChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented Ratio Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((totalBoys.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF1976D2))
                        )
                        Box(
                            modifier = Modifier
                                .weight((totalGirls.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFFC2185B))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF1976D2)))
                            Text("ছাত্র: ${BanglaUtils.toBanglaDigits(boyPercentage)}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFC2185B)))
                            Text("ছাত্রী: ${BanglaUtils.toBanglaDigits(girlPercentage)}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Catchment Area Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_catchment_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                border = CardDefaults.outlinedCardBorder(enabled = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ক্যাচমেন্ট এলাকা",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF6A1B9A))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Segmented Catchment Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((internalCount.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFF6A1B9A))
                        )
                        Box(
                            modifier = Modifier
                                .weight((externalCount.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(Color(0xFFEF6C00))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF6A1B9A)))
                            Text("অভ্যন্তর: ${BanglaUtils.toBanglaDigits(internalPercentage)}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF6C00)))
                            Text("বহিরাগত: ${BanglaUtils.toBanglaDigits(externalPercentage)}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. QUICK ACTIONS HUB (MODERN SOLID GRID)
        // ==========================================
        Text(
            text = "দ্রুত কার্যক্রম ও টুলস (Quick Actions)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val quickActions = listOf(
            ModernQuickActionItem(
                label = "শিক্ষার্থী তালিকা ও ব্যবস্থাপনা",
                subLabel = "তথ্য দেখা, এডিট ও কাস্টমাইজেশন",
                icon = Icons.Filled.People,
                targetRoute = "students",
                accentColor = Color(0xFF00695C),
                bgCol = Color(0xFFE0F2F1)
            ),
            ModernQuickActionItem(
                label = "প্রবেশপত্র ও আইডি কার্ড মেকার",
                subLabel = "ডিজিটাল কার্ড জেনারেশন ও প্রিন্ট",
                icon = Icons.Filled.Badge,
                targetRoute = "tools_hub",
                accentColor = Color(0xFF4338CA),
                bgCol = Color(0xFFEDE9FE)
            ),
            ModernQuickActionItem(
                label = "আসন বিন্যাস ও টুলস হাব",
                subLabel = "পরীক্ষা রুম প্ল্যান ও সার্টিফিকেট",
                icon = Icons.Filled.Widgets,
                targetRoute = "tools_hub",
                accentColor = Color(0xFFB45309),
                bgCol = Color(0xFFFEF3C7)
            ),
            ModernQuickActionItem(
                label = "বিদ্যালয় তথ্য ও ডেটা সেটিংস",
                subLabel = "প্রোফাইল, থিম ও ডেটা ব্যাকআপ",
                icon = Icons.Filled.School,
                targetRoute = "settings",
                accentColor = Color(0xFFBE185D),
                bgCol = Color(0xFFFCE7F3)
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quickActions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { action ->
                        ModernQuickActionCard(
                            item = action,
                            onClick = { onNavigateToSection(action.targetRoute) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ==========================================
// COMPONENT HELPERS
// ==========================================

@Composable
fun ModernMetricCard(
    title: String,
    value: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("summary_card_${title}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class ModernQuickActionItem(
    val label: String,
    val subLabel: String,
    val icon: ImageVector,
    val targetRoute: String,
    val accentColor: Color,
    val bgCol: Color
)

@Composable
fun ModernQuickActionCard(
    item: ModernQuickActionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("btn_quick_action_${item.targetRoute}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = CardDefaults.outlinedCardBorder(enabled = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.bgCol),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = item.subLabel,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
