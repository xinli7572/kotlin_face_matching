package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder



class FaceDetaction(private val context: Context) {

    private val NUM_THREADS = 4
    private lateinit var interpreter:Interpreter

    fun load_centerFace() {
        val tfliteModel = FileUtil.loadMappedFile(context, "CenterFace.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(NUM_THREADS)
        }
         interpreter = Interpreter(tfliteModel, options)

        val inputTensor = interpreter.getInputTensor(0)
        val inputShape = inputTensor.shape() // e.g., [1, 160, 120, 3]
        val inputType = inputTensor.dataType()

        Log.d("TFLITE", "Input shape: ${inputShape.contentToString()}, type: $inputType")
        Log.d("MODEL", "Input shape: ${inputTensor.shape().contentToString()}")

        for (i in 0 until interpreter.outputTensorCount) {
            val outputTensor = interpreter.getOutputTensor(i)
            Log.d("MODEL", "Output $i shape: ${outputTensor.shape().contentToString()}")
        }

    }


    fun detectFaces(bitmap: Bitmap): List<RectF> {
        val inputBuffer = convertBitmapToByteBufferBatch(bitmap)

        // 2. Define output buffers
        val heatmap =
            Array(1) { Array(outputHeight) { Array(outputWidth) { FloatArray(1) } } }  // 1x240x320x1
        val scale =
            Array(1) { Array(outputHeight) { Array(outputWidth) { FloatArray(2) } } }    // 1x240x320x2
        val offset =
            Array(1) { Array(outputHeight) { Array(outputWidth) { FloatArray(2) } } }   // 1x240x320x2
        val landmarks =
            Array(1) { Array(outputHeight) { Array(outputWidth) { FloatArray(10) } } } // 1x240x320x10

        val outputMap = mapOf(
            0 to heatmap,
            1 to scale,
            2 to offset,
            3 to landmarks
        )

        // 3. Run inference
        interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputMap)

        // 4. Parse output
        val boxes = mutableListOf<RectF>()

        var maxConf = 0f

        for (y in 0 until outputHeight) {
            for (x in 0 until outputWidth) {
                val conf = heatmap[0][y][x][0]

                if (conf > maxConf) {
                    maxConf = conf
                }
                //println("---------conf--${conf}----------")
                if (conf > 0.8) {  // Threshold confidence
                    val s0 = scale[0][y][x][0]
                    val s1 = scale[0][y][x][1]
                    val o0 = offset[0][y][x][0]
                    val o1 = offset[0][y][x][1]

                    val cx = (x + o0) * 3
                    val cy = (y + o1 + 10) * 4
                    val w = Math.exp(s0.toDouble()).toFloat() * 4
                    val h = Math.exp(s1.toDouble()).toFloat() * 6

                    val left = cx - w / 2
                    val top = cy - h / 2
                    val right = cx + w / 2
                    val bottom = cy + h / 2

                    boxes.add(RectF(left, top, right, bottom))
                }
            }
        }

        Log.d("MODEL_RUN", "Max confidence: $maxConf")

        return boxes
    }




    private fun convertBitmapToByteBufferBatch(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(inputHeight * inputWidth * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(inputWidth * inputHeight)
        bitmap.getPixels(intValues, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        for (pixel in intValues) {
            val r = (pixel shr 16 and 0xFF).toFloat()
            val g = (pixel shr 8 and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()
            // 保持 RGB 顺序，且值为 [0,255]
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        byteBuffer.rewind()
        return byteBuffer
    }



}