package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
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

    // Calculated Dynamic Values
    val totalStudents = students.size
    val currentStudents = students.filter {
        it.status == "Current" || it.status == "বর্তমান" || it.status == "Active" || it.status == "সক্রিয়"
    }
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

    val defaultClasses = listOf(
        "প্রাক-প্রাথমিক ৪+",
        "প্রাক-প্রাথমিক ৫+",
        "১ম শ্রেণি",
        "২য় শ্রেণি",
        "৩য় শ্রেণি",
        "৪র্থ শ্রেণি",
        "৫ম শ্রেণি"
    )
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
        } catch (_: Exception) {
            null
        }
    }

    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. MODERN HERO SCHOOL BANNER (ACCESSIBLE & POLISHED)
        // ==========================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_header_card")
                .semantics(mergeDescendants = true) {
                    contentDescription = "বিদ্যালয়ের তথ্য: ${schoolInfo?.schoolName ?: "পশ্চিম রামপুর সরকারি প্রাথমিক বিদ্যালয়"}, EMIS কোড: ${schoolInfo?.emisCode ?: "134251"}, প্রধান শিক্ষক: ${schoolInfo?.headTeacherName ?: "মো: রফিকুল ইসলাম"}"
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (schoolLogoBitmap != null) {
                            Image(
                                bitmap = schoolLogoBitmap.asImageBitmap(),
                                contentDescription = "বিদ্যালয়ের প্রতীক লোগো",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = "বিদ্যালয় আইকন",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
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
                        Spacer(modifier = Modifier.height(2.dp))
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
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("btn_dashboard_edit_school")
                            .semantics {
                                contentDescription = "বিদ্যালয়ের তথ্য ও সেটিংস সম্পাদনা করুন"
                                role = Role.Button
                            }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tag,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "EMIS: ${schoolInfo?.emisCode ?: "134251"}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f, fill = false).padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
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
                        color = if (isDark) Color(0xFF1B4332) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF2D6A4F) else Color(0xFFC8E6C9))
                    ) {
                        Text(
                            text = "সেশন ${BanglaUtils.toBanglaDigits(2025)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF95D5B2) else Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. BENTO KEY PERFORMANCE INDICATORS
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "শিক্ষার্থী পরিসংখ্যান ও সারসংক্ষেপ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "বিস্তারিত দেখতে যেকোনো কার্ডে ট্যাপ করুন",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${BanglaUtils.toBanglaDigits(currentStudents.size)} বর্তমান",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Bento Top Hero Metric: Total Students (Full Width prominent card)
        AccessibleBentoHeroMetricCard(
            title = "মোট নথিভুক্ত শিক্ষার্থী",
            value = "${BanglaUtils.toBanglaDigits(totalStudents)} জন",
            subtext = "বর্তমানে সক্রিয়: ${BanglaUtils.toBanglaDigits(currentStudents.size)} জন শিক্ষার্থী",
            badgeText = "সকল রেকর্ড",
            icon = Icons.Filled.Groups,
            accentColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C),
            containerColor = if (isDark) Color(0xFF003831) else Color(0xFFE0F2F1),
            onClick = {
                viewModel.filterClass.value = null
                viewModel.filterGender.value = null
                viewModel.filterStatus.value = "ALL"
                viewModel.filterSpecialNeeds.value = null
                onNavigateToSection("students")
            }
        )

        // Bento Row 1: Boys & Girls (2-Column equal cards with high contrast)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessibleBentoMetricCard(
                title = "মোট ছাত্র",
                value = "${BanglaUtils.toBanglaDigits(totalBoys)} জন",
                badgeText = "${BanglaUtils.toBanglaDigits(boyPercentage)}% অনুপাত",
                icon = Icons.Filled.Male,
                accentColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0),
                containerColor = if (isDark) Color(0xFF0D325E) else Color(0xFFE3F2FD),
                accessibilityLabel = "মোট ছাত্র ${BanglaUtils.toBanglaDigits(totalBoys)} জন, অনুপাত ${BanglaUtils.toBanglaDigits(boyPercentage)} শতাংশ, শুধুমাত্র ছাত্রদের দেখতে ট্যাপ করুন",
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterGender.value = "ছাত্র"
                    viewModel.filterClass.value = null
                    viewModel.filterStatus.value = "Current"
                    viewModel.filterSpecialNeeds.value = null
                    onNavigateToSection("students")
                }
            )

            AccessibleBentoMetricCard(
                title = "মোট ছাত্রী",
                value = "${BanglaUtils.toBanglaDigits(totalGirls)} জন",
                badgeText = "${BanglaUtils.toBanglaDigits(girlPercentage)}% অনুপাত",
                icon = Icons.Filled.Female,
                accentColor = if (isDark) Color(0xFFF48FB1) else Color(0xFFC2185B),
                containerColor = if (isDark) Color(0xFF4A0E2E) else Color(0xFFFCE4EC),
                accessibilityLabel = "মোট ছাত্রী ${BanglaUtils.toBanglaDigits(totalGirls)} জন, অনুপাত ${BanglaUtils.toBanglaDigits(girlPercentage)} শতাংশ, শুধুমাত্র ছাত্রীদের দেখতে ট্যাপ করুন",
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterGender.value = "ছাত্রী"
                    viewModel.filterClass.value = null
                    viewModel.filterStatus.value = "Current"
                    viewModel.filterSpecialNeeds.value = null
                    onNavigateToSection("students")
                }
            )
        }

        // Bento Row 2: Special Needs, Internal & External Catchment (3-Column balanced cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccessibleBentoMetricCard(
                title = "বিশেষ চাহিদা",
                value = "${BanglaUtils.toBanglaDigits(specialNeedsCount)} জন",
                badgeText = "বিশেষ সেবা",
                icon = Icons.Filled.Accessible,
                accentColor = if (isDark) Color(0xFFFFB74D) else Color(0xFFD97706),
                containerColor = if (isDark) Color(0xFF3E2704) else Color(0xFFFEF3C7),
                accessibilityLabel = "বিশেষ চাহিদা সম্পন্ন শিক্ষার্থী ${BanglaUtils.toBanglaDigits(specialNeedsCount)} জন, তালিকা দেখতে ট্যাপ করুন",
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.searchQuery.value = ""
                    viewModel.filterClass.value = null
                    viewModel.filterSpecialNeeds.value = true
                    onNavigateToSection("students")
                }
            )

            AccessibleBentoMetricCard(
                title = "অভ্যন্তরীণ",
                value = "${BanglaUtils.toBanglaDigits(internalCount)} জন",
                badgeText = "${BanglaUtils.toBanglaDigits(internalPercentage)}% ক্যাচ.",
                icon = Icons.Filled.Home,
                accentColor = if (isDark) Color(0xFFCE93D8) else Color(0xFF6D28D9),
                containerColor = if (isDark) Color(0xFF330D4F) else Color(0xFFEDE9FE),
                accessibilityLabel = "অভ্যন্তরীণ ক্যাচমেন্ট শিক্ষার্থী ${BanglaUtils.toBanglaDigits(internalCount)} জন, অনুপাত ${BanglaUtils.toBanglaDigits(internalPercentage)} শতাংশ",
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterClass.value = null
                    viewModel.filterSpecialNeeds.value = null
                    onNavigateToSection("students")
                }
            )

            AccessibleBentoMetricCard(
                title = "বহিরাগত",
                value = "${BanglaUtils.toBanglaDigits(externalCount)} জন",
                badgeText = "${BanglaUtils.toBanglaDigits(externalPercentage)}% বাইরের",
                icon = Icons.Filled.DirectionsBus,
                accentColor = if (isDark) Color(0xFF9FA8DA) else Color(0xFF1E40AF),
                containerColor = if (isDark) Color(0xFF101B4B) else Color(0xFFDBEAFE),
                accessibilityLabel = "বহিরাগত এলাকার শিক্ষার্থী ${BanglaUtils.toBanglaDigits(externalCount)} জন, অনুপাত ${BanglaUtils.toBanglaDigits(externalPercentage)} শতাংশ",
                modifier = Modifier.weight(1f),
                onClick = {
                    viewModel.filterClass.value = null
                    viewModel.filterSpecialNeeds.value = null
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
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            text = "শ্রেণিভিত্তিক শিক্ষার্থী বিশ্লেষণ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "যেকোনো শ্রেণিতে সরাসরি ফিল্টার করতে বারে ট্যাপ করুন",
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

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Visual Accessible Bar Graph with 48dp Minimum Touch Areas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
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
                        val fraction = if (maxClassCount > 0) (count.toFloat() / maxClassCount).coerceIn(0.10f, 1f) else 0.10f
                        val color = barColors[index % barColors.size]

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

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .minimumInteractiveComponentSize()
                                .clickable {
                                    viewModel.filterClass.value = className
                                    viewModel.filterGender.value = null
                                    viewModel.filterStatus.value = "Current"
                                    viewModel.filterSpecialNeeds.value = null
                                    onNavigateToSection("students")
                                }
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "$className: ${BanglaUtils.toBanglaDigits(count)} জন শিক্ষার্থী, শ্রেণি ফিল্টার করতে ট্যাপ করুন"
                                    role = Role.Button
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Value Pill atop Bar
                            Surface(
                                color = if (count > 0) color.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = BanglaUtils.toBanglaDigits(count),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (count > 0) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Proportional Bar Column
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.68f)
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                color.copy(alpha = 0.85f),
                                                color
                                            )
                                        )
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Short Class Name
                            Text(
                                text = shortName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Fast Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(defaultClasses) { className ->
                        val count = classCounts[className] ?: 0
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.filterClass.value = className
                                    viewModel.filterGender.value = null
                                    viewModel.filterStatus.value = "Current"
                                    viewModel.filterSpecialNeeds.value = null
                                    onNavigateToSection("students")
                                }
                                .semantics(mergeDescendants = true) {
                                    contentDescription = "$className নির্বাচন করুন, ${BanglaUtils.toBanglaDigits(count)} জন শিক্ষার্থী"
                                    role = Role.Button
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Class,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$className (${BanglaUtils.toBanglaDigits(count)})",
                                    fontSize = 12.sp,
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
                    .testTag("dashboard_gender_ratio_card")
                    .semantics(mergeDescendants = true) {
                        contentDescription = "লিঙ্গ অনুপাত: ছাত্র ${BanglaUtils.toBanglaDigits(boyPercentage)} শতাংশ, ছাত্রী ${BanglaUtils.toBanglaDigits(girlPercentage)} শতাংশ"
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented Accessible Ratio Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((totalBoys.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(if (isDark) Color(0xFF42A5F5) else Color(0xFF1976D2))
                        )
                        Box(
                            modifier = Modifier
                                .weight((totalGirls.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(if (isDark) Color(0xFFEC407A) else Color(0xFFC2185B))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF42A5F5) else Color(0xFF1976D2))
                                )
                                Text("ছাত্র", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = "${BanglaUtils.toBanglaDigits(boyPercentage)}% (${BanglaUtils.toBanglaDigits(totalBoys)})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFFEC407A) else Color(0xFFC2185B))
                                )
                                Text("ছাত্রী", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = "${BanglaUtils.toBanglaDigits(girlPercentage)}% (${BanglaUtils.toBanglaDigits(totalGirls)})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Catchment Area Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .testTag("dashboard_catchment_card")
                    .semantics(mergeDescendants = true) {
                        contentDescription = "ক্যাচমেন্ট এলাকা: অভ্যন্তরীণ ${BanglaUtils.toBanglaDigits(internalPercentage)} শতাংশ, বহিরাগত ${BanglaUtils.toBanglaDigits(externalPercentage)} শতাংশ"
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF6A1B9A)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented Catchment Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((internalCount.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(if (isDark) Color(0xFFAB47BC) else Color(0xFF6A1B9A))
                        )
                        Box(
                            modifier = Modifier
                                .weight((externalCount.coerceAtLeast(1)).toFloat())
                                .fillMaxHeight()
                                .background(if (isDark) Color(0xFFFFA726) else Color(0xFFEF6C00))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFFAB47BC) else Color(0xFF6A1B9A))
                                )
                                Text("অভ্যন্তর", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = "${BanglaUtils.toBanglaDigits(internalPercentage)}% (${BanglaUtils.toBanglaDigits(internalCount)})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFFFFA726) else Color(0xFFEF6C00))
                                )
                                Text("বহিরাগত", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                text = "${BanglaUtils.toBanglaDigits(externalPercentage)}% (${BanglaUtils.toBanglaDigits(externalCount)})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. QUICK ACTIONS HUB (ACCESSIBLE SOLID M3 GRID)
        // ==========================================
        Column {
            Text(
                text = "দ্রুত কার্যক্রম ও প্রশাসনিক টুলস",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "দৈনন্দিন কার্যাবলি দ্রুত পরিচালনা করুন",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val quickActions = listOf(
            AccessibleQuickActionItem(
                label = "শিক্ষার্থী ব্যবস্থাপনা",
                subLabel = "সকল শিক্ষার্থী তালিকা, ভর্তি ও কাস্টম তথ্য",
                icon = Icons.Filled.People,
                targetRoute = "students",
                accentColor = if (isDark) Color(0xFF80CBC4) else Color(0xFF00695C),
                bgCol = if (isDark) Color(0xFF003831) else Color(0xFFE0F2F1)
            ),
            AccessibleQuickActionItem(
                label = "উপস্থিতি ও হাজিরা",
                subLabel = "দৈনিক হাজিরা গ্রহণ ও উপস্থিতি রিপোর্ট",
                icon = Icons.Filled.CalendarMonth,
                targetRoute = "tools_hub/attendance_report",
                accentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                bgCol = if (isDark) Color(0xFF0C4A6E) else Color(0xFFE0F2FE)
            ),
            AccessibleQuickActionItem(
                label = "প্রবেশপত্র ও আইডি কার্ড",
                subLabel = "পরীক্ষার এডমিট কার্ড প্রস্তুত ও প্রিন্ট",
                icon = Icons.Filled.Badge,
                targetRoute = "tools_hub/admit_card",
                accentColor = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4338CA),
                bgCol = if (isDark) Color(0xFF312E81) else Color(0xFFEDE9FE)
            ),
            AccessibleQuickActionItem(
                label = "সার্টিফিকেট ও সিট প্ল্যান",
                subLabel = "প্রত্যয়ন ও প্রশংসা পত্র, আসন বিন্যাস",
                icon = Icons.Filled.WorkspacePremium,
                targetRoute = "tools_hub/certificate_maker",
                accentColor = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309),
                bgCol = if (isDark) Color(0xFF451A03) else Color(0xFFFEF3C7)
            ),
            AccessibleQuickActionItem(
                label = "ডাটা ব্যাকআপ ও সেটিংস",
                subLabel = "গুগল ড্রাইভ সিঙ্ক, ব্যাকআপ ও প্রোফাইল",
                icon = Icons.Filled.CloudSync,
                targetRoute = "settings",
                accentColor = if (isDark) Color(0xFFF472B6) else Color(0xFFBE185D),
                bgCol = if (isDark) Color(0xFF500724) else Color(0xFFFCE7F3)
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // First 4 actions in 2x2 grid
            quickActions.take(4).chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { action ->
                        AccessibleQuickActionCard(
                            item = action,
                            onClick = { onNavigateToSection(action.targetRoute) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5th action: Full width banner card for Cloud Backup & Settings
            quickActions.lastOrNull()?.let { backupAction ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable { onNavigateToSection(backupAction.targetRoute) }
                        .testTag("btn_quick_action_${backupAction.targetRoute}")
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${backupAction.label}, ${backupAction.subLabel}, সেটিংস দেখতে ট্যাপ করুন"
                            role = Role.Button
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(backupAction.bgCol),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = backupAction.icon,
                                contentDescription = null,
                                tint = backupAction.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = backupAction.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = backupAction.subLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = backupAction.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ==========================================
// ACCESSIBLE COMPONENT HELPERS
// ==========================================

@Composable
fun AccessibleBentoHeroMetricCard(
    title: String,
    value: String,
    subtext: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
            .testTag("summary_card_total_students")
            .semantics(mergeDescendants = true) {
                contentDescription = "$title, $value, $subtext, সকল শিক্ষার্থী তালিকা দেখতে ট্যাপ করুন"
                role = Role.Button
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        color = containerColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AccessibleBentoMetricCard(
    title: String,
    value: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    accessibilityLabel: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
            .testTag("summary_card_${title}")
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                role = Role.Button
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

data class AccessibleQuickActionItem(
    val label: String,
    val subLabel: String,
    val icon: ImageVector,
    val targetRoute: String,
    val accentColor: Color,
    val bgCol: Color
)

@Composable
fun AccessibleQuickActionCard(
    item: AccessibleQuickActionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable { onClick() }
            .testTag("btn_quick_action_${item.targetRoute}")
            .semantics(mergeDescendants = true) {
                contentDescription = "${item.label}, ${item.subLabel}, খুলতে ট্যাপ করুন"
                role = Role.Button
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.bgCol),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
