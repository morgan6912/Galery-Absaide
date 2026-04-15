package com.absaide.gallery.presentation.ar

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.absaide.gallery.presentation.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*

// ── Tipos de marco disponibles ────────────────────────────────────────────
enum class FrameStyle(val label: String, val colors: List<Color>) {
    GOLD   ("Dorado",       listOf(Color(0xFFD4AF37), Color(0xFFF5E27A), Color(0xFFB8860B), Color(0xFFD4AF37))),
    SILVER ("Plateado",     listOf(Color(0xFFC0C0C0), Color(0xFFE8E8E8), Color(0xFF909090), Color(0xFFC0C0C0))),
    BLACK  ("Negro",        listOf(Color(0xFF1A1A1A), Color(0xFF333333), Color(0xFF000000), Color(0xFF1A1A1A))),
    WOOD   ("Madera",       listOf(Color(0xFF8B4513), Color(0xFFA0522D), Color(0xFF6B3410), Color(0xFF8B4513))),
    WHITE  ("Blanco",       listOf(Color(0xFFFFFFFF), Color(0xFFF0F0F0), Color(0xFFDDDDDD), Color(0xFFFFFFFF))),
    NONE   ("Sin marco",    listOf(Color.Transparent, Color.Transparent, Color.Transparent, Color.Transparent)),
}

@Composable
fun ArtworkARScreen(
    artworkImageUrl: String,
    artworkTitle: String,
    artworkDescription: String,
    onBack: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope          = rememberCoroutineScope()

    // ── Permisos ──────────────────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { hasCameraPermission = it }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── Estado de la obra ─────────────────────────────────────────────────
    var artworkBitmap   by remember { mutableStateOf<ImageBitmap?>(null) }
    var artworkOffsetX  by remember { mutableStateOf(0f) }
    var artworkOffsetY  by remember { mutableStateOf(0f) }
    var artworkScale    by remember { mutableStateOf(0.45f) }
    var artworkRotation by remember { mutableStateOf(0f) }
    var containerWidth  by remember { mutableStateOf(1080) }
    var containerHeight by remember { mutableStateOf(1920) }
    var isPlaced        by remember { mutableStateOf(false) }
    var selectedFrame   by remember { mutableStateOf(FrameStyle.GOLD) }
    var showFramePicker by remember { mutableStateOf(false) }
    var showGrid        by remember { mutableStateOf(false) }
    var hint            by remember { mutableStateOf("Apunta a una pared y toca para colocar la obra") }
    var saveSuccess     by remember { mutableStateOf(false) }
    var isPlacing       by remember { mutableStateOf(false) }

    // Animación de aparición
    val placeScale by animateFloatAsState(
        targetValue  = if (isPlacing) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label        = "placeScale",
        finishedListener = { isPlacing = false }
    )

    // ── Cargar imagen ─────────────────────────────────────────────────────
    LaunchedEffect(artworkImageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val stream = java.net.URL(artworkImageUrl).openStream()
                val bmp    = BitmapFactory.decodeStream(stream)
                withContext(Dispatchers.Main) { artworkBitmap = bmp?.asImageBitmap() }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // ── Sin permiso ───────────────────────────────────────────────────────
    if (!hasCameraPermission) {
        Box(
            Modifier.fillMaxSize().background(Color(0xFF08080F)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text("📷", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Necesitamos la cámara para mostrar la obra en tu espacio",
                    color = WhiteText,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick  = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors   = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Permitir cámara", color = Color.White) }
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onBack) { Text("Volver", color = GrayText) }
            }
        }
        return
    }

    // ── Vista AR principal ────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                containerWidth  = it.size.width
                containerHeight = it.size.height
            }
    ) {

        // ── 1. Preview cámara trasera ──────────────────────────────────
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    val future = ProcessCameraProvider.getInstance(ctx)
                    future.addListener({
                        val provider = future.get()
                        val preview  = Preview.Builder().build()
                            .also { it.setSurfaceProvider(surfaceProvider) }
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview
                            )
                        } catch (e: Exception) { e.printStackTrace() }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── 2. Grid de referencia ──────────────────────────────────────
        if (showGrid) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color.White.copy(alpha = 0.15f)
                val cols = 6
                val rows = 10
                val colW = size.width / cols
                val rowH = size.height / rows
                for (i in 1 until cols) {
                    drawLine(gridColor, Offset(i * colW, 0f), Offset(i * colW, size.height), 1f)
                }
                for (i in 1 until rows) {
                    drawLine(gridColor, Offset(0f, i * rowH), Offset(size.width, i * rowH), 1f)
                }
                // Línea central horizontal y vertical más marcada
                drawLine(Color.White.copy(alpha = 0.3f),
                    Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 2f)
                drawLine(Color.White.copy(alpha = 0.3f),
                    Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2f)
            }
        }

        // ── 3. Obra superpuesta con gestos ─────────────────────────────
        if (artworkBitmap != null) {
            val bitmap = artworkBitmap!!
            val artW   = (containerWidth * artworkScale * placeScale)
            val artH   = artW * (bitmap.height.toFloat() / bitmap.width.toFloat())
            val frameStroke = if (selectedFrame == FrameStyle.NONE) 0f else 14f
            val frameW = artW + frameStroke * 2
            val frameH = artH + frameStroke * 2

            // Sombra debajo de la obra
            if (isPlaced) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawOval(
                        color      = Color.Black.copy(alpha = 0.25f),
                        topLeft    = Offset(
                            artworkOffsetX + frameW * 0.1f,
                            artworkOffsetY + frameH + 4f
                        ),
                        size       = androidx.compose.ui.geometry.Size(frameW * 0.8f, 16f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Toque simple → colocar
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            if (!isPlaced) {
                                artworkOffsetX = offset.x - frameW / 2
                                artworkOffsetY = offset.y - frameH / 2
                                isPlaced       = true
                                isPlacing      = true
                                hint = "Arrastra · Pellizca para escalar · Dos dedos para rotar"
                            }
                        }
                    }
                    // Pellizcar para escalar + rotar con dos dedos
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            var prevDist     = 0f
                            var prevAngle    = 0f
                            var fingerCount  = 0

                            awaitFirstDown(requireUnconsumed = false)
                            do {
                                val event = awaitPointerEvent()
                                val pointers = event.changes.filter { it.pressed }
                                fingerCount = pointers.size

                                if (fingerCount == 2) {
                                    val p0 = pointers[0].position
                                    val p1 = pointers[1].position
                                    val dist  = sqrt((p1.x - p0.x).pow(2) + (p1.y - p0.y).pow(2))
                                    val angle = Math.toDegrees(atan2(
                                        (p1.y - p0.y).toDouble(),
                                        (p1.x - p0.x).toDouble()
                                    )).toFloat()

                                    if (prevDist > 0f) {
                                        val scaleDelta = dist / prevDist
                                        artworkScale = (artworkScale * scaleDelta).coerceIn(0.15f, 0.95f)
                                    }
                                    if (prevAngle != 0f) {
                                        val angleDelta = angle - prevAngle
                                        artworkRotation = (artworkRotation + angleDelta) % 360f
                                    }
                                    prevDist  = dist
                                    prevAngle = angle
                                    pointers.forEach { it.consume() }
                                }
                            } while (pointers.any { it.pressed })
                        }
                    }
                    // Drag con un dedo → mover obra
                    .pointerInput(isPlaced) {
                        if (!isPlaced) return@pointerInput
                        detectDragGestures { _, dragAmount ->
                            artworkOffsetX = (artworkOffsetX + dragAmount.x)
                                .coerceIn(0f, (containerWidth - frameW).coerceAtLeast(0f))
                            artworkOffsetY = (artworkOffsetY + dragAmount.y)
                                .coerceIn(0f, (containerHeight - frameH).coerceAtLeast(0f))
                        }
                    }
                    // Dibujar imagen y marco
                    .drawWithContent {
                        drawContent()
                        if (isPlaced) {
                            drawIntoCanvas { canvas ->
                                val paint = android.graphics.Paint().apply {
                                    isAntiAlias = true
                                }
                                canvas.nativeCanvas.save()
                                canvas.nativeCanvas.rotate(
                                    artworkRotation,
                                    artworkOffsetX + frameW / 2,
                                    artworkOffsetY + frameH / 2
                                )

                                // Dibujar imagen
                                if (bitmap.asAndroidBitmap() != null) {
                                    canvas.nativeCanvas.drawBitmap(
                                        bitmap.asAndroidBitmap(),
                                        null,
                                        android.graphics.RectF(
                                            artworkOffsetX + frameStroke,
                                            artworkOffsetY + frameStroke,
                                            artworkOffsetX + frameStroke + artW,
                                            artworkOffsetY + frameStroke + artH
                                        ),
                                        paint
                                    )
                                }

                                // Dibujar marco si no es NONE
                                if (selectedFrame != FrameStyle.NONE) {
                                    val framePaint = android.graphics.Paint().apply {
                                        isAntiAlias = true
                                        style = android.graphics.Paint.Style.STROKE
                                        strokeWidth = frameStroke * 2
                                        shader = android.graphics.LinearGradient(
                                            artworkOffsetX, artworkOffsetY,
                                            artworkOffsetX + frameW, artworkOffsetY + frameH,
                                            intArrayOf(
                                                selectedFrame.colors[0].toArgb(),
                                                selectedFrame.colors[1].toArgb(),
                                                selectedFrame.colors[2].toArgb(),
                                                selectedFrame.colors[3].toArgb()
                                            ),
                                            null,
                                            android.graphics.Shader.TileMode.CLAMP
                                        )
                                    }
                                    canvas.nativeCanvas.drawRect(
                                        artworkOffsetX,
                                        artworkOffsetY,
                                        artworkOffsetX + frameW,
                                        artworkOffsetY + frameH,
                                        framePaint
                                    )
                                }

                                canvas.nativeCanvas.restore()
                            }
                        }
                    }
            )
        }

        // ── 4. Hint superior ──────────────────────────────────────────
        AnimatedVisibility(
            visible = true,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 72.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(hint, color = Color.White, fontSize = 12.sp,
                    textAlign = TextAlign.Center)
            }
        }

        // ── 5. Toast de guardado ───────────────────────────────────────
        AnimatedVisibility(
            visible   = saveSuccess,
            enter     = fadeIn() + slideInVertically(),
            exit      = fadeOut(),
            modifier  = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(TealPrimary.copy(alpha = 0.95f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text("✅ Guardado en tu galería", color = Color.White,
                    style = MaterialTheme.typography.titleMedium)
            }
        }

        // ── 6. Panel inferior ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Selector de marcos
            AnimatedVisibility(visible = showFramePicker) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.82f)
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Estilo de marco", color = GrayText, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            FrameStyle.entries.forEach { style ->
                                val isSelected = style == selectedFrame
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isSelected) TealPrimary.copy(alpha = 0.3f)
                                            else Color.White.copy(alpha = 0.08f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) TealPrimary else GrayBorder,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedFrame = style }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        // Preview del color del marco
                                        if (style != FrameStyle.NONE) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(style.colors)
                                                    )
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .border(1.dp, GrayBorder, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) { Text("✕", color = GrayText, fontSize = 12.sp) }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(style.label, color = WhiteText, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Slider de tamaño
            if (isPlaced) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.78f)
                    )
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tamaño", color = GrayText, fontSize = 12.sp)
                            Text("${(artworkScale * 200).toInt()} cm aprox.",
                                color = TealPrimary, fontSize = 12.sp)
                        }
                        Slider(
                            value         = artworkScale,
                            onValueChange = { artworkScale = it },
                            valueRange    = 0.15f..0.95f,
                            colors        = SliderDefaults.colors(
                                thumbColor         = TealPrimary,
                                activeTrackColor   = TealPrimary,
                                inactiveTrackColor = GrayBorder
                            )
                        )
                    }
                }
            }

            // Barra de acciones
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                colors   = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.78f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Título de la obra
                    Text(
                        artworkTitle,
                        color    = TealPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )

                    // Botón grid
                    IconActionButton(
                        label   = if (showGrid) "Grid ✓" else "Grid",
                        onClick = { showGrid = !showGrid }
                    )

                    // Botón marcos
                    IconActionButton(
                        label   = "Marco",
                        onClick = { showFramePicker = !showFramePicker }
                    )

                    // Botón guardar captura
                    IconActionButton(
                        label   = "📸",
                        onClick = {
                            scope.launch {
                                val saved = saveScreenCapture(
                                    context, artworkTitle,
                                    artworkBitmap
                                )
                                if (saved) {
                                    saveSuccess = true
                                    delay(2000)
                                    saveSuccess = false
                                }
                            }
                        }
                    )
                }
            }
        }

        // ── 7. Botones flotantes superiores ────────────────────────────
        // Botón volver
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.7f))
        ) {
            IconButton(onClick = onBack) {
                Text("←", fontSize = 22.sp, color = WhiteText)
            }
        }

        // Badge AR
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(8.dp))
                .background(TealPrimary.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text("📷 AR", color = Color.White, fontSize = 12.sp,
                letterSpacing = 0.5.sp)
        }

        // ── 8. Cargando imagen ─────────────────────────────────────────
        if (artworkBitmap == null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TealPrimary)
                    Spacer(Modifier.height(10.dp))
                    Text("Cargando obra...", color = WhiteText, fontSize = 13.sp)
                }
            }
        }
    }
}

// ── Botón de acción pequeño ───────────────────────────────────────────────
@Composable
fun IconActionButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick      = onClick,
        shape        = RoundedCornerShape(10.dp),
        colors       = ButtonDefaults.textButtonColors(
            contentColor = WhiteText
        ),
        modifier     = Modifier.padding(horizontal = 2.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}

// ── Guardar captura en galería ────────────────────────────────────────────
suspend fun saveScreenCapture(
    context: android.content.Context,
    title: String,
    bitmap: ImageBitmap?
): Boolean {
    if (bitmap == null) return false
    return withContext(Dispatchers.IO) {
        try {
            val androidBitmap = bitmap.asAndroidBitmap()
            val filename      = "Absaide_AR_${System.currentTimeMillis()}.jpg"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/Absaide")
                }
                val uri = context.contentResolver
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let { u ->
                    context.contentResolver.openOutputStream(u)?.use { out ->
                        androidBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    true
                } ?: false
            } else {
                val dir = java.io.File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ), "Absaide"
                )
                if (!dir.exists()) dir.mkdirs()
                val file = java.io.File(dir, filename)
                java.io.FileOutputStream(file).use { out ->
                    androidBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, out)
                }
                android.media.MediaScannerConnection.scanFile(
                    context, arrayOf(file.absolutePath), null, null
                )
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// ── Stubs requeridos ──────────────────────────────────────────────────────
enum class ARStatus { CHECKING, NO_PERMISSION, NOT_SUPPORTED, SUPPORTED }

fun checkArSupport(context: android.content.Context): ARStatus = ARStatus.NOT_SUPPORTED