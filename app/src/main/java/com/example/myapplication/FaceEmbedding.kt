package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt


class FaceEmbedding(private val context: Context) {

    private val NUM_THREADS = 4
    private lateinit var interpreter_mobilefacenet: Interpreter

    fun load_mobilefacenet() {

        val tfliteModel = FileUtil.loadMappedFile(context, "mobilefacenet2.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(NUM_THREADS)
        }
        interpreter_mobilefacenet = Interpreter(tfliteModel, options)

        val inputTensor = interpreter_mobilefacenet.getInputTensor(0)
        val inputShape = inputTensor.shape() // e.g., [1, 160, 120, 3]
        val inputType = inputTensor.dataType()

        Log.d(
            "TFLITE",
            "Input mobilefacenet shape: ${inputShape.contentToString()}, type: $inputType"
        )
        Log.d("MODEL", "Input mobilefacenet shape: ${inputTensor.shape().contentToString()}")

        for (i in 0 until interpreter_mobilefacenet.outputTensorCount) {
            val outputTensor = interpreter_mobilefacenet.getOutputTensor(i)
            Log.d(
                "MODEL",
                "Output $i mobilefacenet shape: ${outputTensor.shape().contentToString()}"
            )
        }
    }


    fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in vec1.indices) {
            dot += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }
        return dot / (sqrt(normA) * sqrt(normB))
    }

    fun l2Normalize(vector: FloatArray): FloatArray {
        var sum = 0f
        for (v in vector) {
            sum += v * v
        }
        val norm = sqrt(sum)
        return if (norm > 0) {
            vector.map { it / norm }.toFloatArray()
        } else {
            vector  // 避免除以0的情况
        }
    }

    fun euclideanDistance(vec1: FloatArray, vec2: FloatArray): Float {
        var sum = 0f
        for (i in vec1.indices) {
            val diff = vec1[i] - vec2[i]
            sum += diff * diff
        }
        return sqrt(sum)
    }


    fun getEmbedding(bitmap: Bitmap?): FloatArray {
        val resized = Bitmap.createScaledBitmap(bitmap!!, 160, 160, true)
        val input = convertBitmapToBuffer(resized)
        val output = Array(1) { FloatArray(128) }
        interpreter_mobilefacenet.run(input, output)
        return output[0]
    }

    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(1 * 160 * 160 * 3 * 4)
        inputBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(160 * 160)
        bitmap.getPixels(intValues, 0, 160, 0, 0, 160, 160)
        for (i in intValues.indices) {
            val `val` = intValues[i]
            val r = (`val` shr 16 and 0xFF) / 255.0f
            val g = (`val` shr 8 and 0xFF) / 255.0f
            val b = (`val` and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        return inputBuffer
    }


}