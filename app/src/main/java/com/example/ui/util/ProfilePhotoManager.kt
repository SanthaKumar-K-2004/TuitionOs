package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages student/staff profile photo capture, gallery selection, and local storage.
 * Photos are stored in app's internal files directory under "photos/" subfolder.
 */
object ProfilePhotoManager {

    private const val PHOTOS_DIR = "photos"
    private const val MAX_PHOTO_SIZE = 512 // pixels, square

    /**
     * Saves an image URI to internal storage and returns the local file path.
     * Handles both camera output URIs and gallery content URIs.
     */
    suspend fun savePhotoToInternalStorage(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val photosDir = File(context.filesDir, PHOTOS_DIR)
                if (!photosDir.exists()) photosDir.mkdirs()

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val outputFile = File(photosDir, "STU_${timestamp}.jpg")

                // Decode and compress the image
                val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return@withContext null
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) return@withContext null

                // Resize to max dimensions
                val resizedBitmap = resizeBitmap(bitmap, MAX_PHOTO_SIZE)

                // Save as JPEG with quality 85
                FileOutputStream(outputFile).use { out ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }

                // Clean up bitmaps
                if (resizedBitmap !== bitmap) bitmap.recycle()
                resizedBitmap.recycle()

                outputFile.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Creates a temporary URI for camera capture using FileProvider.
     */
    fun createCameraImageUri(context: Context): Uri {
        val photosDir = File(context.filesDir, PHOTOS_DIR)
        if (!photosDir.exists()) photosDir.mkdirs()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val photoFile = File(photosDir, "CAM_${timestamp}.jpg")
        return FileProvider.getUriForFile(context, "com.example.provider", photoFile)
    }

    /**
     * Resizes a bitmap to fit within the specified max dimension while maintaining aspect ratio.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = maxDimension.toFloat() / maxOf(width, height).toFloat()
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Deletes a photo file by its absolute path.
     */
    fun deletePhoto(context: Context, filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists() && file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Composable photo picker UI for student/staff profile.
 * Shows current avatar and options to take photo or pick from gallery.
 */
@Composable
fun ProfilePhotoPicker(
    currentPhotoPath: String?,
    onPhotoSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPickerDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = ProfilePhotoManager.savePhotoToInternalStorage(context, uri)
                if (path != null) onPhotoSelected(path)
            }
        }
    }

    // Camera capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraUri
        if (success && uri != null) {
            scope.launch {
                val path = ProfilePhotoManager.savePhotoToInternalStorage(context, uri)
                if (path != null) onPhotoSelected(path)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Avatar display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD), CircleShape)
                .border(3.dp, Color(0xFF00288E), CircleShape)
        ) {
            if (currentPhotoPath != null && currentPhotoPath.isNotEmpty()) {
                AsyncImage(
                    model = File(currentPhotoPath),
                    contentDescription = "Profile Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "No Photo",
                    tint = Color(0xFF00288E),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { showPickerDialog = true }) {
            Text(
                text = if (currentPhotoPath.isNullOrEmpty()) "Add Photo / புகைப்படம் சேர்"
                       else "Change Photo / மாற்று",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00288E)
            )
        }
    }

    // Photo source picker dialog
    if (showPickerDialog) {
        Dialog(onDismissRequest = { showPickerDialog = false }) {
            Column(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Choose Photo Source",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF00288E)
                )
                Text(
                    "புகைப்பட மூலத்தைத் தேர்வுசெய்",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Camera option
                TextButton(
                    onClick = {
                        showPickerDialog = false
                        val uri = ProfilePhotoManager.createCameraImageUri(context)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFF00288E),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text("Take Photo / கேமரா", color = Color(0xFF1A1A1A))
                }

                // Gallery option
                TextButton(
                    onClick = {
                        showPickerDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = Color(0xFF00288E),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text("Choose from Gallery / கேலரி", color = Color(0xFF1A1A1A))
                }

                // Remove photo option (only if photo exists)
                if (!currentPhotoPath.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            showPickerDialog = false
                            ProfilePhotoManager.deletePhoto(context, currentPhotoPath)
                            onPhotoSelected("")
                        }
                    ) {
                        Text("Remove Photo / நீக்கு", color = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

// Extension to make Icon available in the Box
@Composable
private fun Icon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
