package com.mohamed.pdfreader.ui.screens.reader

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mohamed.pdfreader.pdfcore.PdfRendererCore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    uri: Uri,
    onBack: () -> Unit,
    viewModel: PdfReaderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val selectedWord by viewModel.selectedWord.collectAsState()
    val translationResult by viewModel.translationResult.collectAsState()
    val wordsList by viewModel.currentWords.collectAsState()
    
    val pdfCore = remember { PdfRendererCore(context, uri) }
    DisposableEffect(Unit) {
        onDispose { pdfCore.close() }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pdfCore.pageCount.toString() + " صفحة", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            ) {
                items(pdfCore.pageCount) { index ->
                    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                    LaunchedEffect(index) {
                        scope.launch {
                            val renderedBmp = pdfCore.renderPage(index, screenWidthPx)
                            bitmap = renderedBmp
                            // بمجرد تحميل الصفحة الأولى، نستخرج النص منها للترجمة
                            // يمكن تطويرها لاحقاً لتعمل مع الصفحة المعروضة فقط
                            if (index == 0 && renderedBmp != null) {
                                viewModel.processPageForOcr(renderedBmp)
                            }
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Page $index",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures { tapOffset ->
                                        // البحث هل الضغطة جاءت فوق كلمة معينة؟
                                        val x = tapOffset.x.toInt()
                                        val y = tapOffset.y.toInt()
                                        
                                        val tappedWord = wordsList.find { wordBox ->
                                            wordBox.boundingBox.contains(x, y)
                                        }
                                        
                                        if (tappedWord != null) {
                                            viewModel.onWordTapped(tappedWord.text)
                                        }
                                    }
                                },
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

            // بطاقة الترجمة والنطق السفلية (BottomSheet)
            if (selectedWord != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.dismissTranslationDialog() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedWord ?: "",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = { viewModel.speakWord(selectedWord ?: "") },
                                modifier = Modifier.background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), 
                                    shape = RoundedCornerShape(50)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "استمع",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = translationResult ?: "",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
