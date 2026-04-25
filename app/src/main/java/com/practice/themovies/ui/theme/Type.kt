package com.practice.themovies.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.practice.ui.Montserrat

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Montserrat, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
)
