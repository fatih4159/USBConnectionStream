package com.example.usbconnectionstream.util

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.USB_SERVICE
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED
import android.hardware.usb.UsbManager.ACTION_USB_DEVICE_DETACHED
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.example.usbconnectionstream.ui.components.IndicatorButtonState
import com.example.usbconnectionstream.ui.screens.mUSBDevice
import com.example.usbconnectionstream.ui.screens.mUSBManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UsbSerialConnection {
    companion object {
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
        const val TIMEOUT = 1000 // Communication timeout in milliseconds
        var usbReceiver: BroadcastReceiver? = null
        fun unregisterReceiver(context: Context) {
            context.unregisterReceiver(usbReceiver)
        }

        fun getUsbReceiver(
            status: MutableState<IndicatorButtonState>,
            instance: UsbSerialConnection,
            buttonEnabledState: MutableState<Boolean>
        ): BroadcastReceiver {
            if (usbReceiver != null) return usbReceiver as BroadcastReceiver
            usbReceiver = object : BroadcastReceiver() {

                override fun onReceive(context: Context, intent: Intent) {
                    when (intent.action) {
                        ACTION_USB_DEVICE_ATTACHED -> {
                            val usbDevice: UsbDevice? =
                                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                            if (usbDevice?.deviceId != null) {
                                Toast.makeText(
                                    context,
                                    "USB Device ${usbDevice.deviceId} connected",
                                    Toast.LENGTH_SHORT
                                ).show()
                                try {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        instance.obtainUsbManager(context!!)
                                            .onSuccess { usbManager ->
                                                mUSBManager = usbManager
                                                if (usbManager.deviceList.isNotEmpty()) {
                                                    mUSBDevice =
                                                        usbManager.deviceList.values.first()

                                                    val permissionIntent =
                                                        PendingIntent.getBroadcast(
                                                            context,
                                                            0,
                                                            Intent(UsbSerialConnection.ACTION_USB_PERMISSION),
                                                            FLAG_IMMUTABLE
                                                        )
                                                    if (mUSBDevice != null) usbManager.requestPermission(
                                                        mUSBDevice, permissionIntent
                                                    )
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        Toast.makeText(
                                                            context,
                                                            "Permission for USB Device ${usbDevice.deviceId} granted",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        status.value = IndicatorButtonState.SUCCESS
                                                        buttonEnabledState.value = true
                                                    }
                                                } else {
                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        Toast.makeText(
                                                            context,
                                                            "Permission for USB Device ${usbDevice.deviceId} NOT granted",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        status.value = IndicatorButtonState.FAILURE
                                                        buttonEnabledState.value = false
                                                    }

                                                }

                                            }.onFailure {
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    Toast.makeText(
                                                        context,
                                                        "Failed getting UsbManager for Device ${usbDevice.deviceId} ",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    status.value = IndicatorButtonState.FAILURE
                                                    buttonEnabledState.value = false
                                                }

                                            }
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }


                            } else {
                                Toast.makeText(
                                    context,
                                    "USB Device did not send its ID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        ACTION_USB_DEVICE_DETACHED -> {

                            val usbDevice: UsbDevice? =
                                intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                            Toast.makeText(
                                context,
                                "USB Device ${usbDevice?.deviceId} disconnected",
                                Toast.LENGTH_SHORT
                            ).show()
                            CoroutineScope(Dispatchers.Main).launch {
                                if (usbDevice?.deviceId != null) {

                                    mUSBDevice = null
                                    status.value = IndicatorButtonState.FAILURE
                                    buttonEnabledState.value = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "USB Device did not send its ID",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        }

                        ACTION_USB_PERMISSION -> {
                            synchronized(this) {
                                val device: UsbDevice? =
                                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

                                if (intent.getBooleanExtra(
                                        UsbManager.EXTRA_PERMISSION_GRANTED,
                                        false
                                    )
                                ) {
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
            return usbReceiver as BroadcastReceiver
        }
    }

    private var job: Job? = null

    public suspend fun readDataFromScanner(
        context: Context,
        device: UsbDevice,
        usbManager: UsbManager,
        onRead: (String) -> Unit
    ) {
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
        try {
            val result = context.getSystemService(USB_SERVICE) as UsbManager
            return when (result) {
                null -> {
                    Result.failure(Exception("Could not obtain UsbManager"))
                }

                else -> {
                    Result.success(result)
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

    }


}

