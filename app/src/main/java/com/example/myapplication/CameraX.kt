package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.ByteArrayOutputStream

class CameraX(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    lateinit var face_1: Bitmap
    lateinit var face_2: Bitmap
    lateinit var output_1: FloatArray
    lateinit var output_2: FloatArray
    lateinit var boxes_1: List<RectF>
    lateinit var boxes_2: List<RectF>

    fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var lastAnalyzedTime = 0L
        var flag = 1;

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(640, 640)) // 模型输入大小
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->

                Log.d("CAMERA", "=")
                Log.d("CAMERA", "=")
                Log.d("CAMERA", "=")
                Log.d("CAMERA", "Image format: ${imageProxy.format}")
                Log.d("CAMERA", "=")
                Log.d("CAMERA", "=")
                Log.d("CAMERA", "=")

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastAnalyzedTime >= 20) {
                    lastAnalyzedTime = currentTime

                    val bitmap = imageProxyToBitmapSafe(imageProxy)
                    if (bitmap != null) {
                        // 在这里处理 bitmap，比如传入 TFLite 模型推理 detectFaces(bitmap)
                        Log.d("CAMERA", "Got bitmap: ${bitmap.width}x${bitmap.height}")
                        var pic_resize = resizeWithPadding(bitmap, inputWidth, inputHeight)

                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

                        val matrix = android.graphics.Matrix()
                        matrix.postRotate(rotationDegrees.toFloat())
                        matrix.postScale(-1f, 1f)
                        var pic_me =
                            Bitmap.createBitmap(
                                pic_resize,
                                0,
                                0,
                                pic_resize.width,
                                pic_resize.height,
                                matrix,
                                true
                            )


                        var boxes_me = faceDetaction.detectFaces(pic_me)
                        if (!::boxes_1.isInitialized) {
                            boxes_1 = faceDetaction.detectFaces(pic_1)
                        }

                        if (!::boxes_2.isInitialized) {
                            boxes_2 = faceDetaction.detectFaces(pic_2)
                        }

                        if (boxes_me != null && boxes_me.size > 0 &&
                            boxes_1 != null && boxes_1.size > 0 &&
                            boxes_2 != null && boxes_2.size > 0
                        ) {

                            imageView_1.post {
                                overlayView.setRect(boxes_me[0])
                                imageView_1.setImageBitmap(pic_me)
                            }

                            var face_me = facePic.getFace(pic_me, boxes_me)
                            var output_me = faceEmbedding.getEmbedding(face_me)

                            if (!::face_1.isInitialized) {
                                val result_1 = facePic.getFace(pic_1, boxes_1)
                                if (result_1 != null) {
                                    face_1 = result_1
                                    if (!::output_1.isInitialized) {
                                        output_1 = faceEmbedding.getEmbedding(face_1)
                                    }
                                }
                            }

                            if (!::face_2.isInitialized) {
                                val result_2 = facePic.getFace(pic_2, boxes_2)
                                if (result_2 != null) {
                                    face_2 = result_2
                                    if (!::output_2.isInitialized) {
                                        output_2 = faceEmbedding.getEmbedding(face_2)
                                    }
                                }
                            }



                            if (flag == 1) {
                                flag = 2;

                                var cos_1 = faceEmbedding.cosineSimilarity(
                                    faceEmbedding.l2Normalize(output_me),
                                    faceEmbedding.l2Normalize(output_1)
                                )

                                println("cos=${cos_1}")
                                println("========================================")

                                imageView_2.post {
                                    imageView_2.setImageBitmap(pic_1)
                                }

                            } else {
                                flag = 1;

                                var cos_2 = faceEmbedding.cosineSimilarity(
                                    faceEmbedding.l2Normalize(output_me),
                                    faceEmbedding.l2Normalize(output_2)
                                )
                                println("cos=${cos_2}")
                                println("========================================")
                                imageView_2.post {
                                    imageView_2.setImageBitmap(pic_2)
                                }
                            }
                        }
                    }
                }
                imageProxy.close()
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                Log.e("CAMERA", "绑定失败", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun resizeWithPadding(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val ratio = minOf(
            targetWidth.toFloat() / bitmap.width,
            targetHeight.toFloat() / bitmap.height
        )
        val scaledWidth = (bitmap.width * ratio).toInt()
        val scaledHeight = (bitmap.height * ratio).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        val paddedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(paddedBitmap)
        canvas.drawColor(Color.BLACK) // 使用黑色填充背景
        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f
        canvas.drawBitmap(scaledBitmap, left, top, null)
        return paddedBitmap
    }

    fun imageProxyToBitmap_(image: ImageProxy): Bitmap {
        val yBuffer = image.planes[0].buffer // Y
        val uBuffer = image.planes[1].buffer // U
        val vBuffer = image.planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize) // NOTE: v before u
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)
        val imageBytes = out.toByteArray()

        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // 可选：旋转 bitmap（如果你需要处理竖屏拍照）
        val rotationDegrees = image.imageInfo.rotationDegrees
        if (rotationDegrees != 0) {
            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        return bitmap
    }

    fun imageProxyToBitmapSafe(image: ImageProxy): Bitmap {
        val width = image.width
        val height = image.height
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val y = ByteArray(yBuffer.remaining())
        val u = ByteArray(uBuffer.remaining())
        val v = ByteArray(vBuffer.remaining())

        yBuffer.get(y)
        uBuffer.get(u)
        vBuffer.get(v)

        for (row in 0 until height) {
            for (col in 0 until width) {
                val yIndex = row * yRowStride + col
                val uvIndex = (row / 2) * uvRowStride + (col / 2) * uvPixelStride

                val yVal = y[yIndex].toInt() and 0xFF
                val uVal = u[uvIndex].toInt() and 0xFF
                val vVal = v[uvIndex].toInt() and 0xFF

                // YUV to RGB
                val yF = yVal.toFloat()
                val uF = uVal - 128f
                val vF = vVal - 128f

                var r = (yF + 1.370705f * vF).toInt()
                var g = (yF - 0.337633f * uF - 0.698001f * vF).toInt()
                var b = (yF + 1.732446f * uF).toInt()

                r = r.coerceIn(0, 255)
                g = g.coerceIn(0, 255)
                b = b.coerceIn(0, 255)

                val color = Color.rgb(r, g, b)
                bitmap.setPixel(col, row, color)
            }
        }

        image.close()
        return bitmap
    }

    //  将 ImageProxy 转换为 Bitmap（RGB）
    private fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21,
            android.graphics.ImageFormat.NV21,
            image.width,
            image.height,
            null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, image.width, image.height),
            100,
            out
        )
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

}