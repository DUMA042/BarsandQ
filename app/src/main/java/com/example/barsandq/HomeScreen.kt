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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {

    var barcode by rememberSaveable { mutableStateOf<String?>("No Code Scanned") }

    // Camera permission state
    val permissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    var oncancel by remember(permissionState.status.shouldShowRationale){mutableStateOf(permissionState.status.shouldShowRationale)}

/////


    if (barcode!=null){

        Column {
            if (oncancel) {
                ShowRationaleDialog({oncancel=false},{permissionState.launchPermissionRequest()}, body = permissionState.permission)
            }

            val textToShow = if (permissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The Camera permission is important for this app. Please grant the permission."

            } else if (!permissionState.status.isGranted) {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required

                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }else{
                barcode?:"No Scaned"
            }


            Text(textToShow)
            if (permissionState.status.isGranted){
                Button(onClick = {barcode=null}) {

                    Text("Scan Qr or Bar code ")
                }
            }
            else {
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Request permission")
                }
            }


        }
}
    else{
        ScanCode(onQrCodeDetected = {
            barcode = it
        })

    }


}