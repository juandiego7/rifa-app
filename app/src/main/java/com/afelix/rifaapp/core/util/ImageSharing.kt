package com.afelix.rifaapp.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageSharing {
    fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String) {
        try {
            val cachePath = File(context.cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "$fileName.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir Imagen"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun captureView(view: View): Bitmap? {
        if (view.width <= 0 || view.height <= 0) {
            // View not measured yet, try to measure it
            val specWidth = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val specHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(specWidth, specHeight)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }
        
        if (view.width <= 0 || view.height <= 0) return null

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE) // Ensure white background
        view.draw(canvas)
        return bitmap
    }
}

@Composable
fun <T : View> ViewCaptureWrapper(
    onViewReady: (T) -> Unit,
    content: @Composable () -> Unit
) {
    AndroidView(
        factory = { context ->
            val frameLayout = FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            val composeView = ComposeView(context).apply {
                setContent { content() }
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            frameLayout.addView(composeView)
            @Suppress("UNCHECKED_CAST")
            onViewReady(frameLayout as T)
            frameLayout
        },
        update = {
            // Ensure onViewReady is called if not already
            @Suppress("UNCHECKED_CAST")
            onViewReady(it as T)
        }
    )
}
