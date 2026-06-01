package com.mohamed.pdfreader.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Recent : Screen("recent", "الأخيرة", Icons.Default.History)
    object Bookmarks : Screen("bookmarks", "الإشارات", Icons.Default.Bookmarks)
    object Search : Screen("search", "بحث", Icons.Default.Search)
    object Settings : Screen("settings", "الإعدادات", Icons.Default.Settings)
    
    object Reader : Screen("reader/{fileUri}", "القارئ", Icons.Default.History) {
        fun createRoute(fileUri: String) = "reader/$fileUri"
    }
}
