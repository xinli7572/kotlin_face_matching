package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap

class FaceUtil(private val context: Context) {

    fun getCosSimlarity(pic_1: Bitmap, pic_2: Bitmap): Float {


        var boxes_1 = faceDetaction.detectFaces(pic_1)
        var boxes_2 = faceDetaction.detectFaces(pic_2)

        if (boxes_1 != null && boxes_1.size > 0 && boxes_2 != null && boxes_2.size > 0) {

            var face_1 = facePic.getFace(pic_1, boxes_1)
            var face_2 = facePic.getFace(pic_2, boxes_2)

            var output_1 = faceEmbedding.getEmbedding(face_1)
            var output_2 = faceEmbedding.getEmbedding(face_2)

            var cos = faceEmbedding.cosineSimilarity(
                faceEmbedding.l2Normalize(output_1),
                faceEmbedding.l2Normalize(output_2)
            )

            println("========================================")
            println("output===${output_1.joinToString(", ")}")
            println("output===${output_2.joinToString(", ")}")
            println("========================================")
            return cos
        }

        return 0f
    }
}