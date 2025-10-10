package com.example.myapplication.util

import android.graphics.ImageFormat
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.nio.ByteBuffer

// Esta classe implementa a interface ImageAnalysis.Analyzer
// A função 'analyze' será chamada para cada frame da câmera
class QrCodeAnalyzer(
    private val onBarcodeDetected: (barcodes: List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {

    // Lista de formatos de imagem que o ML Kit suporta
    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888
    )

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        // Se a imagem da câmera não estiver em um formato suportado, ignoramos
        if (imageProxy.format !in supportedImageFormats) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        // Converte a imagem da câmera para o formato que o ML Kit entende
        val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)

        // Configura o scanner para procurar por QR Codes e Códigos de Barras
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_CODE_128
            )
            .build()
        val scanner = BarcodeScanning.getClient(options)

        // Inicia o processamento da imagem
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                // Se o ML Kit encontrar algum código, a 'lambda' onBarcodeDetected será chamada
                if (barcodes.isNotEmpty()) {
                    onBarcodeDetected(barcodes)
                }
            }
            .addOnFailureListener {
                // Log de erro (opcional, mas bom para depuração)
            }
            .addOnCompleteListener {
                // É crucial fechar a imagem para que a câmera possa enviar o próximo frame
                imageProxy.close()
            }
    }
}
