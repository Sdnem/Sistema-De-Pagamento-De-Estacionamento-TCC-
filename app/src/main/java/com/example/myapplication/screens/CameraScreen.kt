package com.example.myapplication.screens

import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
// Imports necessários para o botão
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.CheckInState
import com.example.myapplication.EstacionamentoViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val estacionamentoViewModel: EstacionamentoViewModel = viewModel()
    val checkInState by estacionamentoViewModel.checkInState.collectAsState()
    val isScanning = remember { mutableStateOf(true) }

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    LaunchedEffect(checkInState) {
        when (val state = checkInState) {
            is CheckInState.Success -> {
                navController.navigate("estacionamento_ativo/${state.response.horario_entrada}") {
                    popUpTo("home") { inclusive = false }
                }
                estacionamentoViewModel.resetState()
            }
            is CheckInState.Error -> {
                Toast.makeText(context, "Falha no Check-in: ${state.message}", Toast.LENGTH_LONG).show()
                estacionamentoViewModel.resetState()
                isScanning.value = true
            }
            is CheckInState.Loading -> {
                Toast.makeText(context, "Registrando entrada...", Toast.LENGTH_SHORT).show()
            }
            else -> { /* Estado Idle */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status == PermissionStatus.Granted) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val executor = ContextCompat.getMainExecutor(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        val imageAnalyser = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(executor) { imageProxy ->
                                    if (isScanning.value) { // Só processa se estiver esperando um scan
                                        val image = imageProxy.image
                                        if (image != null) {
                                            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
                                            val scanner = BarcodeScanning.getClient()
                                            scanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    if (barcodes.isNotEmpty() && isScanning.value) {
                                                        isScanning.value = false // Trava para evitar múltiplos scans
                                                        estacionamentoViewModel.fazerCheckIn(context) // Inicia o processo
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close() // Sempre fechar o proxy
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyser)
                    }, executor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Se a permissão não foi concedida, solicita novamente.
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
        }

        // ================================================================
        //  BOTÃO DE SIMULAÇÃO DE QR CODE (GATILHO FALSO)
        // ================================================================
        FloatingActionButton(
            onClick = {
                // Só simula o scan se não houver um em andamento
                if (isScanning.value) {
                    isScanning.value = false // Trava para evitar múltiplos cliques
                    Toast.makeText(context, "Simulando leitura de QR Code...", Toast.LENGTH_SHORT).show()
                    // Chama a mesma função que o scanner chamaria
                    estacionamentoViewModel.fazerCheckIn(context)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Posiciona o botão na parte inferior central
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = "Simular Leitura de QR Code"
            )
        }
    }
}
