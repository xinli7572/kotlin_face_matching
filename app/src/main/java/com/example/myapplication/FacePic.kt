package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log

class FacePic(private val context: Context) {

    fun getTestBitmap(pic: String): Bitmap {
        val inputStream = context.assets.open(pic)
        var bitmap = BitmapFactory.decodeStream(inputStream)

        return resizeWithPadding(bitmap, inputWidth, inputHeight)
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

    fun drawRect(pic: Bitmap, rect: RectF): Bitmap {
        val mutableBitmap = pic.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(rect, paint)
        return mutableBitmap
    }

    fun getFace(pic: Bitmap, boxes: List<RectF>): Bitmap? {

        val rect = boxes[0]

        val left = rect.left.toInt().coerceAtLeast(0)
        val top = rect.top.toInt().coerceAtLeast(0)
        val right = rect.right.toInt().coerceAtMost(pic.width)
        val bottom = rect.bottom.toInt().coerceAtMost(pic.height)


        val width = right - left
        val height = bottom - top


        if (width > 0 && height > 0) {
            val tmp = Bitmap.createBitmap(pic, left, top, width, height)
            var tmp2 = Bitmap.createScaledBitmap(tmp, 160, 160, true)
            return tmp2
        } else {
            Log.e("BitmapCrop", "无效的裁剪区域，跳过操作")
        }

        return null
    }
}