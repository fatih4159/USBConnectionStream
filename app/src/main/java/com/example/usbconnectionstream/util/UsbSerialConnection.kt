package com.example.usbconnectionstream.util

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
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext


class UsbSerialConnection {
    companion object {
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        const val TIMEOUT = 1000 // Communication timeout in milliseconds
    }

    private var job: Job? = null

    public suspend fun readDataFromScanner(context:Context,device: UsbDevice,usbManager:UsbManager,onRead:(String)->Unit){
            val scannerInterface = device.getInterface(0) // assuming the scanner uses interface 0
            val endpoint = scannerInterface.getEndpoint(0) // assuming the endpoint is the first one

            // Open the USB Connection
            val connection = usbManager.openDevice(device)

            // Claim the interface before communication
            connection.claimInterface(scannerInterface, true)

            val buffer = ByteArray(endpoint.maxPacketSize)
            while (NonCancellable.isActive) { // Continue reading while the coroutine is active
                val bytesRead = connection.bulkTransfer(endpoint, buffer, buffer.size, TIMEOUT)
                if (bytesRead > 0) {
                    val readData = String(buffer, 0, bytesRead)
                    // Process the scanned data
                    withContext(Dispatchers.Main) {
                        // Update UI or perform other actions with scanned data
                        Toast.makeText(context, "Scanned data: $readData", Toast.LENGTH_SHORT)
                            .show()
                        onRead(readData)
                    }


                }
            }

            // Release the interface after communication
            connection.releaseInterface(scannerInterface)

            // Close the connection
            connection.close()
    }

    suspend fun obtainUsbManager(context: Context): Result<UsbManager> {
        try{
            val result = context.getSystemService(USB_SERVICE) as UsbManager
            return when (result) {
                null -> {
                    Result.failure(Exception("Could not obtain UsbManager"))
                }

                else -> {
                    Result.success(result)
                }
            }
        }catch (e:Exception){
            return Result.failure(e)
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


}

