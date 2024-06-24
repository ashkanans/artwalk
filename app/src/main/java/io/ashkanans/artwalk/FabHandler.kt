package io.ashkanans.artwalk

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabHandler(
    private val activity: Activity,
    private val fab: FloatingActionButton,
    private val imageHandler: ImageHandler
) {
    init {
        fab.setOnClickListener { showBottomDialog() }
    }

    private fun showBottomDialog() {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottomsheetlayout)

        val takePhotoLayout = dialog.findViewById<LinearLayout>(R.id.layout_take_photo)
        val uploadGalleryLayout = dialog.findViewById<LinearLayout>(R.id.layout_upload_gallery)
        val cancelButton = dialog.findViewById<ImageView>(R.id.cancelButton)

        takePhotoLayout.setOnClickListener {
            dialog.dismiss()
            if (!imageHandler.checkCameraPermissions()) {
                imageHandler.requestCameraPermissions()
            } else {
                imageHandler.dispatchTakePictureIntent()
            }
        }

        uploadGalleryLayout.setOnClickListener {
            dialog.dismiss()
            imageHandler.openGalleryAndSelectImages()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            attributes.windowAnimations = R.style.DialogAnimation
            setGravity(Gravity.BOTTOM)
        }
    }
}
