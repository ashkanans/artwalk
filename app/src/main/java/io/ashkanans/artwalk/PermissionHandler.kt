package io.ashkanans.artwalk

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler(private val activity: Activity) {
    private val PERMISSION_REQUEST_CODE_ALL = 10

    fun requestAllPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.GET_ACCOUNTS,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_MEDIA_IMAGES
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE_ALL
            )
        } else {
            Toast.makeText(activity, "All permissions are already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun handleRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE_ALL) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(activity, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Some permissions are denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
