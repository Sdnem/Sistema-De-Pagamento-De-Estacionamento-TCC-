package com.example.myapplication

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

@Composable
fun CameraView(
    onBarCodeDetected: (List<Barcode>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Câmera
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = cameraProviderFuture.get()

    // Preview
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    preview.setSurfaceProvider(previewView.surfaceProvider)

    // Scanner
    val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    val scanner = BarcodeScanning.getClient(options)

    // Analisador de imagem
    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()

    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
        processImageProxy(scanner, imageProxy) { barcodes ->
            if (barcodes.isNotEmpty()) {
                onBarCodeDetected(barcodes)
                imageAnalysis.clearAnalyzer() // Para a análise após a primeira detecção
                cameraProvider.unbindAll()   // Desliga a câmera
            }
        }
    }

    // Vincula a câmera ao ciclo de vida
    cameraProvider.unbindAll()
    cameraProvider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_BACK_CAMERA,
        preview,
        imageAnalysis
    )

    // Exibe a preview da câmera na tela
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onSuccess: (List<Barcode>) -> Unit
) {
    val image = imageProxy.image ?: return
    val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            onSuccess(barcodes)
        }
        .addOnFailureListener {
            Log.e("CAMERA_VIEW", "Erro ao processar o barcode: ${it.message}")
        }
        .addOnCompleteListener {
            imageProxy.close() // Fecha o imageProxy para que o próximo frame possa ser processado
        }
}
