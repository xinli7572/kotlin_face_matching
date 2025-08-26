# Kotlin Face Matching (Android)

This project implements **face detection and face embedding** on Android using **Kotlin** and **TensorFlow Lite**. It runs entirely offline and is suitable for real-time face recognition and comparison on mobile devices.

## ✨ Features

- 🎯 **Face Detection** using [InsightFace](https://github.com/deepinsight/insightface)'s CenterFace model.
- 🔐 **Face Embedding (Feature Extraction)** using MobileFaceNet trained with ArcFace loss.
- ⚙️ Models converted from ONNX to TFLite using Docker (`onnx2tflite`).
- 📱 Fully offline, optimized for Android devices.

---

## 📂 Project Structure
```
app/
└── src/
└── main/
├── java/com/example/myapplication/
│ ├── MainActivity.kt
│ ├── CameraX.kt
│ ├── FaceDetection.kt
│ ├── FaceEmbedding.kt
│ ├── FaceUtil.kt
│ └── OverlayView.kt
├── assets/
│ ├── CenterFace.tflite
│ ├── mobilefacenet.tflite
│ ├── 1.jpg
│ ├── 2.jpg
│ └── test.jpg
```
---
## Requirements

- Android Studio 4.0 or higher
- Android device with camera access
- OpenCV SDK (integrated into the project)
## 🧠 Model Details

### 1. Face Detection: CenterFace

- Source: [InsightFace CenterFace](https://github.com/deepinsight/insightface/tree/master/detection/centerface)
- Format: ONNX → TFLite
- Purpose: Detects face bounding boxes and facial landmarks.

### 2. Face Embedding: MobileFaceNet

- Trained with ArcFace Loss
- Source: [InsightFace ArcFace (MobileFaceNet)](https://github.com/deepinsight/insightface/tree/master/recognition/arcface_torch)
- Format: ONNX → TFLite
- Purpose: Extracts 128D or 512D facial embeddings for face comparison.

---

## 🐳 ONNX to TFLite Conversion using Docker

Use Dockerized `onnx2tflite` to convert models:

```bash
	docker run --rm -it \
	  -v $(pwd):/workdir \
	  -w /workdir \
	  pinto0309/onnx2tf:latest \
	  onnx2tf -i centerface.onnx


