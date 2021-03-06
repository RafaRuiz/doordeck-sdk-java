package com.doordeck.sdk.ui.qrcode

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.AttributeSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.doordeck.sdk.common.utils.LOG
import com.doordeck.sdk.ui.unlock.UnlockActivity
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import java.util.*

/**
 * Custom view for the QRCode
 */
internal class QRcodeView : CompoundBarcodeView {

    private val TAG = QRcodeView::class.java.simpleName

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    // check if the perm of the camera has been granted before starting scanning
    fun start() {
        val cameraPermission = Manifest.permission.CAMERA
        val permissionCheck = ContextCompat.checkSelfPermission(context, cameraPermission)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            startScanning()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.CAMERA), QRcodeActivity.CAMERA)
        }

    }

    // start scanning once we permission are granted
    private fun startScanning() {
        resume()
        initializeFromIntent(
                Intent()
                        .putExtra(Intents.Scan.SCAN_TYPE, Intents.Scan.MIXED_SCAN)
        )
        decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                val scan = result.toString()
                LOG.d(TAG, "scanned data : $result")
                val id = scan.substring(scan.lastIndexOf("/") + 1)
                if (isUUID(id)) {
                    pause()
                    UnlockActivity.start(context, id)
                } else {
                    // show error
                    LOG.d(TAG, "not a uuid")
                }
            }

            override fun possibleResultPoints(resultPoints: List<ResultPoint>) {

            }
        })
    }

    // verify if the content from in the QR code is an UUID
    private fun isUUID(id: String): Boolean {
        return try {
            UUID.fromString(id)
            true
        } catch (e: IllegalArgumentException) {
            false
        }

    }
}
