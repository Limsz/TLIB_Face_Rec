package net.simplifiedcoding.mlkitsample.facedetector

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import net.simplifiedcoding.mlkitsample.CameraXViewModel
import net.simplifiedcoding.mlkitsample.databinding.ActivityFaceDetectionBinding
import java.util.concurrent.Executors

class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel: CameraXViewModel by viewModels()

    private var hasTurnedLeft = false
    private var hasTurnedRight = false
    private var hasNoddedUp = false
    private var detectionSuccessful = false

    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()

        cameraXViewModel.processCameraProvider.observe(this) { provider ->
            processCameraProvider = provider
            bindCameraPreview()
            bindInputAnalyzer()
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding camera preview: ${e.message}")
        }
    }

    private fun bindInputAnalyzer() {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .enableTracking()
                .build()
        )

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(detector, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding image analysis: ${e.message}")
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy) {
        if (detectionSuccessful) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    binding.graphicOverlay.clear()

                    for (face in faces) {
                        val faceBox = FaceBox(
                            binding.graphicOverlay,
                            face,
                            mediaImage.cropRect,
                            if (detectionSuccessful) Color.TRANSPARENT else Color.GREEN
                        )
                        binding.graphicOverlay.add(faceBox)

                        val headEulerAngleY: Float = face.headEulerAngleY  // Left-right rotation
                        val headEulerAngleX: Float = face.headEulerAngleX  // Up-down rotation

                        Log.d("Liveness", "Head Turn Y: $headEulerAngleY, Head Tilt X: $headEulerAngleX")

                        // Head Turn Left Detection
                        if (!hasTurnedLeft && headEulerAngleY < -15f) {
                            hasTurnedLeft = true
                            updateProgress()
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Turned Left!", Toast.LENGTH_SHORT).show()
                            }
                            Log.d("Liveness", "User turned left!")
                        }

                        // Head Turn Right Detection
                        if (!hasTurnedRight && headEulerAngleY > 15f) {
                            hasTurnedRight = true
                            updateProgress()
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Turned Right!", Toast.LENGTH_SHORT).show()
                            }
                            Log.d("Liveness", "User turned right!")
                        }

                        // Head Nod Detection
                        if (!hasNoddedUp && headEulerAngleX < -10f) {
                            hasNoddedUp = true
                            updateProgress()
                            runOnUiThread {
                                Toast.makeText(applicationContext, "Nod Detected!", Toast.LENGTH_SHORT).show()
                            }
                            Log.d("Liveness", "Nod Detected!")
                        }

                        /// Final Success Check
                        if (hasTurnedLeft && hasTurnedRight && hasNoddedUp) {
                            detectionSuccessful = true
                            faceBox.updateColor(Color.TRANSPARENT)

                            runOnUiThread {
                                binding.progressBar.progress = 100
                                binding.progressText.text = "100%"
                                binding.statusText.text = "Liveness Detection Successful!"
                                binding.successIcon.visibility = View.VISIBLE

                                // Show final Toast message
                                Toast.makeText(applicationContext, "Liveness Detection Successful!", Toast.LENGTH_LONG).show()

                                // Delay before navigating to SuccessActivity
                                binding.root.postDelayed({
                                    val intent = Intent(this, SuccessActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                }, 10000) // Wait 6 seconds before proceeding
                            }

                            Log.d("Liveness", "Liveness Detection Successful!")
                            binding.graphicOverlay.invalidate()
                        }

                    }

                    binding.graphicOverlay.invalidate()

                }
                .addOnFailureListener { e ->
                    Log.e("Liveness", "Face detection failed: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun updateProgress() {
        progress += 33
        if (progress > 99) progress = 99 // Ensures 100% is only set upon success

        runOnUiThread {
            binding.progressBar.progress = progress
            binding.progressText.text = "$progress%"
            binding.statusText.text = when (progress) {
                in 0..32 -> "Please turn your head left"
                in 33..65 -> "Please turn your head right"
                else -> "Please nod your head"
            }
        }
    }

    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName

        fun startActivity(context: Context) {
            val intent = Intent(context, FaceDetectionActivity::class.java)
            context.startActivity(intent)
        }
    }
}
