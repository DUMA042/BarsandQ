package com.example.barsandq

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
@OptIn(ExperimentalPermissionsApi::class) // Opt-in to use experimental permissions API
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    // State to hold the scanned barcode value, saved across recompositions
    var barcode by rememberSaveable { mutableStateOf<String?>("No Code Scanned") }

    // State to manage the camera permission
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA // Permission being requested
    )

    // State to track whether to show the rationale dialog for the permission
    var oncancel by remember(permissionState.status.shouldShowRationale) {
        mutableStateOf(permissionState.status.shouldShowRationale)
    }

    // Check if a barcode has been scanned
    if (barcode != null) {

        // Box to hold the UI elements and center them on the screen
        Box(
            modifier = Modifier
                .fillMaxSize() // Make the Box take up the entire screen
                .background(Color.LightGray) // Optional: Add a background color for visibility
        ) {
            // Column to arrange UI elements vertically and center them
            Column(
                modifier = Modifier
                    .align(Alignment.Center) // Center the Column within the Box
                    .padding(16.dp), // Optional: Add padding
                horizontalAlignment = Alignment.CenterHorizontally, // Center children horizontally
                verticalArrangement = Arrangement.Center // Center children vertically
            ) {
                // Show rationale dialog if permission is denied and rationale can be shown
                if (oncancel) {
                    ShowRationaleDialog(
                        onDismiss = { oncancel = false }, // Callback when dialog is dismissed
                        onConfirm = { permissionState.launchPermissionRequest() }, // Callback to request permission
                        body = permissionState.permission // Permission being requested
                    )
                }

                // Determine the text to show based on the permission state
                val textToShow = if (permissionState.status.shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // explain why the app requires this permission
                    "The Camera permission is important for this app. Please grant the permission."
                } else if (!permissionState.status.isGranted) {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    "Camera permission required for this feature to be available. " +
                            "Please grant the permission"
                } else {
                    // If permission is granted, show the scanned barcode or a default message
                    barcode ?: "No Scanned"
                }

                // Display the determined text
                Text(textToShow)

                // Show a button to scan a QR or barcode if permission is granted
                if (permissionState.status.isGranted) {
                    Button(onClick = { barcode = null }) {
                        Text("Scan QR or Barcode")
                    }
                } else {
                    // Show a button to request camera permission if not granted
                    Button(onClick = { permissionState.launchPermissionRequest() }) {
                        Text("Request permission")
                    }
                }
            }
        }
    } else {
        // If no barcode has been scanned, show the QR/barcode scanner
        ScanCode(onQrCodeDetected = {
            barcode = it // Update the barcode state with the scanned value
        })
    }

}
