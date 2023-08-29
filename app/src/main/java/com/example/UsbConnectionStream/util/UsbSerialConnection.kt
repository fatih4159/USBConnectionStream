package com.example.UsbConnectionStream.util

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log

private const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
private lateinit var permissionIntent: PendingIntent

class UsbSerialConnection {

    suspend fun getUsbSerialDataSteam(
        connection: UsbDeviceConnection
    ): Result<Boolean> {

        val inputStream = connection.serial.byteInputStream()
        val buffer = ByteArray(1024)
        var read = inputStream.read(buffer)
        while (read != -1) {
            Log.d("UsbSerialConnection", "Received: ${buffer.toString(Charsets.UTF_8)}")
            read = inputStream.read(buffer)
        }


        return when (buffer.isNotEmpty()) {
            true -> {
                //do something
                Result.success(true)
            }

            false -> {
                //do something
                Result.failure(Exception("Could not read Buffer"))
            }
        }
    }

    private suspend fun claimInterface(
        connection: UsbDeviceConnection, usbInterface: UsbInterface
    ): Result<Boolean> {
        var result = connection.claimInterface(usbInterface, true)

        return when (result) {
            true -> {
                //do something
                Result.success(true)
            }

            false -> {
                //do something
                Result.failure(Exception("Could not claim interface"))
            }
        }
    }

    suspend fun openDeviceConnection(
        usbManager: UsbManager, device: UsbDevice
    ): Result<UsbDeviceConnection> {

        val result: UsbDeviceConnection?
        try {

            result = usbManager.openDevice(device)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return when (result) {
            null -> {
                //do something
                Result.failure(Exception("Could not open device connection"))
            }

            else -> {
                //do something
                Result.success(result)
            }

        }
    }

    suspend fun fetchFirstInterface(usbDevice: UsbDevice): Result<UsbInterface> {
        val result = usbDevice.getInterface(0)
        return when (result) {
            null -> {
                //do something
                Result.failure(Exception("Could not obtain UsbInterface"))
            }

            else -> {
                //do something
                Result.success(result)
            }
        }
    }

    suspend fun fetchFirstUsbDevice(usbManager: UsbManager): Result<UsbDevice> {
        var result = usbManager.deviceList.values.first()
        return when (result) {
            null -> {
                //do something
                Result.failure(Exception("Could not obtain UsbDevice"))
            }

            else -> {
                //do something
                Result.success(result)
            }
        }
    }

    suspend fun getPermissions(
        usbManager: UsbManager, usbDevice: UsbDevice, context: Context
    ): Result<Unit> {


        permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE
        )
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        context.registerReceiver(usbReceiver, filter)

        usbManager.requestPermission(usbDevice, permissionIntent)

        return Result.success(Unit)
    }

    suspend fun obtainUsbManager(context: Context): Result<UsbManager> {
        val result = context.getSystemService(USB_SERVICE) as UsbManager
        return when (result) {
            null -> {
                //do something
                Result.failure(Exception("Could not obtain UsbManager"))
            }

            else -> {
                //do something
                Result.success(result)
            }
        }

    }

}

private val usbReceiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_USB_PERMISSION == intent.action) {
            synchronized(this) {
                val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.apply {
                        //call method to set up device communication
                    }
                } else {
                    Log.d("usbReceiver", "permission denied for device $device")
                }
            }
        }
    }
}
