package com.mohamed.pdfreader.ui.screens.reader

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mohamed.pdfreader.pdfcore.PdfRendererCore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(uri: Uri, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // إنشاء المحرك وإغلاقه عند الخروج من الشاشة
    val pdfCore = remember { PdfRendererCore(context, uri) }
    DisposableEffect(Unit) {
        onDispose { pdfCore.close() }
    }

    // حساب عرض الشاشة الفعلي بالبيكسل لاستخدامه في جودة الصورة
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("القارئ", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(padding)
        ) {
            items(pdfCore.pageCount) { index ->
                var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                // طلب رسم الصفحة (Lazy loading)
                LaunchedEffect(index) {
                    scope.launch {
                        bitmap = pdfCore.renderPage(index, screenWidthPx)
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Page $index",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp), // مسافة صغيرة بين الصفحات
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    // Placeholder أثناء تحميل الصفحة
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .background(MaterialTheme.colorScheme.background)
                    )
                }
            }
        }
    }
}
