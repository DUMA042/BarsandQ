package com.example.barsandq


import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toComposeRect
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.delay


@Composable
fun ScanCode(
    onQrCodeDetected: (String) -> Unit, // Callback to handle detected QR/barcode
    modifier: Modifier = Modifier
) {
    // State to hold the detected barcode value
    var barcode by remember { mutableStateOf<String?>(null) }

    // Get the current context and lifecycle owner for camera operations
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // State to track if a QR/barcode has been detected
    var qrCodeDetected by remember { mutableStateOf(false) }

    // State to hold the bounding rectangle of the detected barcode
    var boundingRect by remember { mutableStateOf<Rect?>(null) }

    // Initialize the camera controller with the current context
    val cameraController = remember {
        LifecycleCameraController(context)
    }

    // AndroidView to integrate the camera preview and barcode scanning
    AndroidView(
        modifier = modifier.fillMaxSize(), // Make the view take up the entire screen
        factory = { ctx ->
            PreviewView(ctx).apply {
                // Configure barcode scanning options for supported formats
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_CODABAR,
                        Barcode.FORMAT_CODE_93,
                        Barcode.FORMAT_CODE_39,
                        Barcode.FORMAT_CODE_128,
                        Barcode.FORMAT_EAN_8,
                        Barcode.FORMAT_EAN_13,
                        Barcode.FORMAT_AZTEC
                    )
                    .build()

                // Initialize the barcode scanner client with the configured options
                val barcodeScanner = BarcodeScanning.getClient(options)

                // Set up the image analysis analyzer for barcode detection
                cameraController.setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(ctx), // Use the main executor
                    MlKitAnalyzer(
                        listOf(barcodeScanner), // Pass the barcode scanner
                        ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED, // Use view-referenced coordinates
                        ContextCompat.getMainExecutor(ctx) // Use the main executor
                    ) { result: MlKitAnalyzer.Result? ->
                        // Process the barcode scanning results
                        val barcodeResults = result?.getValue(barcodeScanner)
                        if (!barcodeResults.isNullOrEmpty()) {
                            // Update the barcode state with the first detected barcode
                            barcode = barcodeResults.first().rawValue

                            // Update the state to indicate a barcode has been detected
                            qrCodeDetected = true

                            // Update the bounding rectangle of the detected barcode
                            boundingRect = barcodeResults.first().boundingBox

                            // Log the bounding box for debugging purposes
                            Log.d("Looking for Barcode ", barcodeResults.first().boundingBox.toString())
                        }
                    }
                )

                // Bind the camera controller to the lifecycle owner
                cameraController.bindToLifecycle(lifecycleOwner)

                // Set the camera controller for the PreviewView
                this.controller = cameraController
            }
        }
    )

    // If a QR/barcode has been detected, trigger the callback
    if (qrCodeDetected) {
        LaunchedEffect(Unit) {
            // Delay for a short duration to allow recomposition
            delay(100) // Adjust delay as needed

            // Call the callback with the detected barcode value
            onQrCodeDetected(barcode ?: "")
        }

        // Draw a rectangle around the detected barcode
        DrawRectangle(rect = boundingRect)
    }
}

@Composable
fun DrawRectangle(rect: Rect?) {
    // Convert the Android Rect to a Compose Rect
    val composeRect = rect?.toComposeRect()

    // Draw the rectangle on a Canvas if the rect is not null
    composeRect?.let {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(it.left, it.top), // Set the top-left position
                size = Size(it.width, it.height), // Set the size of the rectangle
                style = Stroke(width = 5f) // Use a stroke style with a width of 5f
            )
        }
    }
}