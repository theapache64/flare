package com.theapache64.flare

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    companion object {
        val TAG = FCMService::class.java.simpleName
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            makeForeground()

            val isFlash = remoteMessage.data["is_flash"]!!.toBoolean()

            if (isFlash) {
                turnOnFlash()
            } else {
                turnOffFlash()
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun makeForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            notificationIntent, 0
        )

        val notification = NotificationCompat.Builder(this, "flare")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Flare")
            .setContentText("Doing some work...")
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1337, notification)
    }

    private var mCamera: Camera? = null
    private var parameters: Camera.Parameters? = null
    private var camManager: CameraManager? = null

    private fun turnOffFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val cameraId: String
                camManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                if (camManager != null) {
                    cameraId =
                        camManager!!.cameraIdList[0] // Usually front camera is at 0 position.
                    camManager!!.setTorchMode(cameraId, false)
                    stopForeground(true)
                }
            } catch (@SuppressLint("NewApi") e: CameraAccessException) {
                e.printStackTrace()
            }

        } else {
            mCamera = Camera.open()
            parameters = mCamera!!.parameters
            parameters!!.flashMode = Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = parameters
            mCamera!!.stopPreview()
            stopForeground(true)
        }
    }

    private fun turnOnFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                camManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                var cameraId: String? = null // Usually front camera is at 0 position.
                if (camManager != null) {
                    cameraId = camManager!!.cameraIdList[0]
                    camManager!!.setTorchMode(cameraId!!, true)
                    stopForeground(true)
                }
            } catch (@SuppressLint("NewApi") e: CameraAccessException) {
                Log.e(TAG, e.toString())
            }

        } else {
            mCamera = Camera.open()
            parameters = mCamera!!.parameters
            parameters!!.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            mCamera!!.parameters = parameters
            mCamera!!.startPreview()
            stopForeground(true)
        }
    }
}