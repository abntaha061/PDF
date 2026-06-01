package com.mohamed.pdfreader.ui.screens.reader

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mohamed.pdfreader.domain.model.DrawPath
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
    val listState = rememberLazyListState()
    
    val selectedWord by viewModel.selectedWord.collectAsState()
    val translationResult by viewModel.translationResult.collectAsState()
    val wordsList by viewModel.currentWords.collectAsState()
    val showJumpDialog by viewModel.showJumpDialog.collectAsState()
    val pageDrawings by viewModel.pageDrawings.collectAsState()
    val summaryResult by viewModel.summaryResult.collectAsState()
    
    val pdfCore = remember { PdfRendererCore(context, uri) }
    val totalPages = pdfCore.pageCount

    DisposableEffect(Unit) {
        onDispose { pdfCore.close() }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    val currentPageIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    // التحكم في العرض والرسم
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    var isEditMode by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color.Red) }
    
    // متغيرات الخط الحالي أثناء الرسم
    var currentPath by remember { mutableStateOf<Path?>(null) }

    val colors = listOf(Color.Red, Color.Blue, Color(0xFF22C55E), Color(0xFFF59E0B), Color.Black)

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            text = if(isEditMode) "وضع التعديل" else (uri.lastPathSegment ?: "PDF"),
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    actions = {
                        // زر الذكاء الاصطناعي (التلخيص)
                        IconButton(onClick = { viewModel.summarizeCurrentPage() }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "تلخيص", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        // زر تفعيل وضع الرسم
                        IconButton(onClick = { 
                            isEditMode = !isEditMode
                            scale = 1f // إعادة الحجم الطبيعي للرسم
                            offset = Offset.Zero
                        }) {
                            Icon(
                                Icons.Default.Edit, 
                                contentDescription = "رسم", 
                                tint = if (isEditMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        if (!isEditMode) {
                            IconButton(onClick = { viewModel.setShowJumpDialog(true) }) {
                                Icon(Icons.Default.Numbers, contentDescription = "انتقال", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            IconButton(onClick = {
                                viewModel.addBookmark(uri.toString(), currentPageIndex)
                                Toast.makeText(context, "تم الحفظ", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = "إشارة", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        } else {
                            // زر مسح الشخبطة
                            IconButton(onClick = { viewModel.clearDrawings(currentPageIndex) }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح الكل", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                
                // شريط الألوان يظهر فقط في وضع التعديل
                if (isEditMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { selectedColor = color }
                                    // تمييز اللون المختار
                                    .padding(4.dp)
                                    .background(if (selectedColor == color) Color.White.copy(alpha = 0.5f) else Color.Transparent, CircleShape)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isEditMode) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "1", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = currentPageIndex.toFloat(),
                            onValueChange = { newValue ->
                                scope.launch { listState.scrollToItem(newValue.toInt()) }
                            },
                            valueRange = 0f..(if (totalPages > 1) totalPages - 1 else 0).toFloat(),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(text = totalPages.toString(), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isEditMode) {
                        if (!isEditMode) {
                            // وضع التصفح العادي (تكبير وتصغير)
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    val maxOffset = (scale - 1) * screenWidthPx / 2
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                        y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                    }
                    .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y)
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), userScrollEnabled = !isEditMode) {
                    items(totalPages) { index ->
                        var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                        val drawingsForPage = pageDrawings[index] ?: emptyList()

                        LaunchedEffect(index) {
                            scope.launch {
                                val renderedBmp = pdfCore.renderPage(index, screenWidthPx)
                                bitmap = renderedBmp
                                if (index == currentPageIndex && renderedBmp != null) {
                                    viewModel.processPageForOcr(renderedBmp)
                                }
                            }
                        }

                        if (bitmap != null) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Image(
                                    bitmap = bitmap!!.asImageBitmap(),
                                    contentDescription = "Page $index",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pointerInput(isEditMode) {
                                            if (isEditMode && index == currentPageIndex) {
                                                // وضع الرسم
                                                detectDragGestures(
                                                    onDragStart = { startPoint ->
                                                        currentPath = Path().apply { moveTo(startPoint.x, startPoint.y) }
                                                    },
                                                    onDrag = { change, _ ->
                                                        currentPath?.lineTo(change.position.x, change.position.y)
                                                    },
                                                    onDragEnd = {
                                                        currentPath?.let { path ->
                                                            viewModel.addDrawingToPage(index, DrawPath(path, selectedColor))
                                                        }
                                                        currentPath = null
                                                    }
                                                )
                                            } else {
                                                // النقر للترجمة
                                                detectTapGestures(
                                                    onDoubleTap = {
                                                        scale = if (scale > 1f) 1f else 2.5f
                                                        offset = Offset.Zero
                                                    },
                                                    onTap = { tapOffset ->
                                                        if (scale == 1f && index == currentPageIndex) {
                                                            val x = tapOffset.x.toInt()
                                                            val y = tapOffset.y.toInt()
                                                            val tappedWord = wordsList.find { wordBox ->
                                                                wordBox.boundingBox.contains(x, y)
                                                            }
                                                            if (tappedWord != null) {
                                                                viewModel.onWordTapped(tappedWord.text)
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                    contentScale = ContentScale.FillWidth
                                )
                                
                                // طبقة الـ Canvas للرسم فوق الصورة
                                Canvas(modifier = Modifier.matchParentSize()) {
                                    drawingsForPage.forEach { drawPath ->
                                        drawPath(
                                            path = drawPath.path,
                                            color = drawPath.color,
                                            style = Stroke(width = drawPath.strokeWidth)
                                        )
                                    }
                                    // رسم الخط الحالي الذي يتم رسمه الآن
                                    if (isEditMode && index == currentPageIndex && currentPath != null) {
                                        drawPath(
                                            path = currentPath!!,
                                            color = selectedColor,
                                            style = Stroke(width = 5f)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

            // مؤشر الصفحة العائم
            if (!isEditMode) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 16.dp, end = 16.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)).padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("${currentPageIndex + 1} / $totalPages", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // بطاقة الترجمة (BottomSheet)
        if (selectedWord != null) {
            ModalBottomSheet(onDismissRequest = { viewModel.dismissTranslationDialog() }, containerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Text(text = selectedWord ?: "", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(onClick = { viewModel.speakWord(selectedWord ?: "") }, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(50))) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "استمع", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = translationResult ?: "", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // بطاقة الذكاء الاصطناعي (Summary BottomSheet)
        if (summaryResult != null) {
            ModalBottomSheet(onDismissRequest = { viewModel.dismissSummaryDialog() }, containerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الذكاء الاصطناعي", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))
                    // نعرض النص المستخرج أو التلخيص مع سكرول إذا كان النص طويلاً
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        item {
                            Text(
                                text = summaryResult ?: "",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // نافذة الانتقال لصفحة معينة
        if (showJumpDialog) {
            var pageInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.setShowJumpDialog(false) },
                title = { Text("الانتقال إلى صفحة") },
                text = {
                    OutlinedTextField(
                        value = pageInput, onValueChange = { pageInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("أدخل رقم الصفحة (1 - $totalPages)") }, singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val pageNum = pageInput.toIntOrNull()
                        if (pageNum != null && pageNum in 1..totalPages) {
                            scope.launch { listState.scrollToItem(pageNum - 1) }
                            viewModel.setShowJumpDialog(false)
                        } else {
                            Toast.makeText(context, "رقم صفحة غير صالح", Toast.LENGTH_SHORT).show()
                        }
                    }) { Text("انتقال") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowJumpDialog(false) }) { Text("إلغاء", color = MaterialTheme.colorScheme.error) }
                }
            )
        }
    }
}
