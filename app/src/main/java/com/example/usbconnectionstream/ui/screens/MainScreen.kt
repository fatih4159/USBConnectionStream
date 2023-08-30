package com.example.usbconnectionstream.ui.screens

import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.usbconnectionstream.MainActivity
import com.example.usbconnectionstream.ui.components.IndicatorButton
import com.example.usbconnectionstream.ui.components.IndicatorButtonState
import com.example.usbconnectionstream.ui.theme.MyApplication2Theme
import com.example.usbconnectionstream.util.UsbSerialConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var instance = UsbSerialConnection()
private lateinit var mUSBManager: UsbManager
private var mUSBDevice: UsbDevice? = null

@Composable
fun MainScreen(
    context: MainActivity?
) {
    var scrolltext = remember { mutableStateOf("") }
    var indicator1 = remember { mutableStateOf(IndicatorButtonState.DEFAULT) }
    var indicator2 = remember { mutableStateOf(IndicatorButtonState.DEFAULT) }
    var buttonEnabledState = remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "USB Serial Input\nStream Reader",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        IndicatorButton(
            buttonLabel = "Obtain USB Manager", indicatorButtonState = indicator1, enable = true
        ) {
            indicator1.value = IndicatorButtonState.WAITING
            CoroutineScope(Dispatchers.IO).launch {
                instance.obtainUsbManager(context!!)
                    .onSuccess { usbManager ->
                        mUSBManager = usbManager

                        if (usbManager.deviceList.isNotEmpty()) {
                            mUSBDevice = usbManager.deviceList.values.first()

                            val permissionIntent = PendingIntent.getBroadcast(
                                context,
                                0,
                                Intent(UsbSerialConnection.ACTION_USB_PERMISSION),
                                0
                            )
                            if (mUSBDevice != null) usbManager.requestPermission(
                                mUSBDevice, permissionIntent
                            )
                            indicator1.value = IndicatorButtonState.SUCCESS
                            buttonEnabledState.value = true
                        } else {
                            indicator1.value = IndicatorButtonState.FAILURE
                            buttonEnabledState.value = false

                        }

                    }.onFailure {
                        indicator1.value = IndicatorButtonState.FAILURE
                        buttonEnabledState.value = false

                    }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        IndicatorButton(
            buttonLabel = "Try Fetching Data",
            indicatorButtonState = indicator2,
            enable = buttonEnabledState.value
        ) {
            indicator2.value = IndicatorButtonState.WAITING
            CoroutineScope(Dispatchers.IO).launch {
                indicator2.value = IndicatorButtonState.SUCCESS

                mUSBDevice?.let { it ->
                    instance.readDataFromScanner(
                        context!!, it, mUSBManager
                    ) { result ->
                        scrolltext.value = "$result\n${scrolltext.value}"
                    }
                }
            }
            return@IndicatorButton
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = scrolltext.value,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.LightGray),
            color = Color.Black
        )
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplication2Theme {
        MainScreen(context = null)
    }
}