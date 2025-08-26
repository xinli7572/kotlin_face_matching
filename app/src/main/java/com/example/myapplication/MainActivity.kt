package com.example.myapplication

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import android.Manifest
import android.widget.ImageView


val outputHeight = 160
val outputWidth = 160
val inputHeight = 640
val inputWidth = 640

lateinit var previewView: PreviewView
lateinit var imageView_1: ImageView
lateinit var imageView_2: ImageView
lateinit var overlayView: OverlayView
val cameraExecutor = Executors.newSingleThreadExecutor()

lateinit var pic_1: Bitmap
lateinit var pic_2: Bitmap

lateinit var faceEmbedding: FaceEmbedding
lateinit var faceDetaction: FaceDetaction
lateinit var facePic: FacePic
lateinit var faceUtil: FaceUtil


class MainActivity : AppCompatActivity() {

    lateinit var cameraX: CameraX

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewView = findViewById(R.id.previewView)
        imageView_1 = findViewById(R.id.imageView_1)
        imageView_2 = findViewById(R.id.imageView_2)
        overlayView = findViewById(R.id.overlayView)



        faceEmbedding = FaceEmbedding(this)
        faceDetaction = FaceDetaction(this)
        facePic = FacePic(this)
        faceUtil = FaceUtil(this)
        cameraX = CameraX(this, this)


        faceDetaction.load_centerFace()
        faceEmbedding.load_mobilefacenet()

        pic_1 = facePic.getTestBitmap("1.jpg")
        pic_2 = facePic.getTestBitmap("test.jpg")


        // 请求摄像头权限
        if (allPermissionsGranted()) {
            cameraX.startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }


        if (pic_1 == null || pic_2 == null) {
            Toast.makeText(this.applicationContext, "图片加载失败", Toast.LENGTH_SHORT).show()
        } else {
//
//            var boxes_1 = faceDetaction.detectFaces(pic_1)
//            var boxes_2 = faceDetaction.detectFaces(pic_2)
//
//            var face_1 = facePic.getFace(pic_1, boxes_1)
//            var face_2 = facePic.getFace(pic_2, boxes_2)
//
//            var output_1 = faceEmbedding.getEmbedding(face_1)
//            var output_2 = faceEmbedding.getEmbedding(face_2)
//
//            var cos = faceEmbedding.cosineSimilarity(
//                faceEmbedding.l2Normalize(output_1),
//                faceEmbedding.l2Normalize(output_2)
//            )
//
//            imageView_1.setImageBitmap(pic_1)
//            imageView_2.setImageBitmap(pic_2)
//
//            println("========================================")
//            println("output===${output_1.joinToString(", ")}")
//            println("output===${output_2.joinToString(", ")}")
//            println("========================================")
//
//            println("=============cos===${cos}========================")
//            println("========================================")
        }
    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                cameraX.startCamera()
            } else {
                Toast.makeText(this, "需要摄像头权限", Toast.LENGTH_SHORT).show()
            }
        }

    private fun allPermissionsGranted(): Boolean {
        val b = ContextCompat.checkSelfPermission(
            baseContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        return b
    }
}
