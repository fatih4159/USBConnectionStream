package com.example.usbconnectionstream

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.usbconnectionstream.ui.components.IndicatorButton
import com.example.usbconnectionstream.ui.components.IndicatorButtonState
import com.example.usbconnectionstream.ui.theme.MyApplication2Theme
import com.example.usbconnectionstream.util.UsbSerialConnection
import com.example.usbconnectionstream.util.UsbSerialConnection.Companion.ACTION_USB_PERMISSION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        var instance = UsbSerialConnection()
        lateinit var mUSBManager: UsbManager
        var mUSBDevice: UsbDevice? =null


        super.onCreate(savedInstanceState)
        setContent {
            MyApplication2Theme {
                var scrolltext = remember { mutableStateOf("") }
                var indicator1 = remember { mutableStateOf(IndicatorButtonState.DEFAULT) }
                var indicator2 = remember { mutableStateOf(IndicatorButtonState.DEFAULT) }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        IndicatorButton(
                            buttonLabel = "Obtain USB Manager", indicatorButtonState = indicator1,enable = true
                        ) {
                            indicator1.value = IndicatorButtonState.WAITING
                            CoroutineScope(Dispatchers.IO).launch {
                                instance.obtainUsbManager(this@MainActivity)
                                    .onSuccess { usbManager ->
                                        mUSBManager = usbManager

                                        if (usbManager.deviceList.isNotEmpty()) {
                                            mUSBDevice = usbManager.deviceList.values.first()

                                            val permissionIntent = PendingIntent.getBroadcast(
                                                this@MainActivity,
                                                0,
                                                Intent(ACTION_USB_PERMISSION),
                                                0
                                            )
                                            if (mUSBDevice != null) usbManager.requestPermission(
                                                mUSBDevice, permissionIntent
                                            )
                                            indicator1.value = IndicatorButtonState.SUCCESS
                                        } else {
                                            indicator1.value = IndicatorButtonState.FAILURE
                                        }

                                    }.onFailure {
                                        indicator1.value = IndicatorButtonState.FAILURE
                                    }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        IndicatorButton(
                            buttonLabel = "Try Fetching Data", indicatorButtonState = indicator2,enable = mUSBDevice !=null
                        ) {
                            indicator2.value = IndicatorButtonState.WAITING
                            CoroutineScope(Dispatchers.IO).launch {
                                indicator2.value = IndicatorButtonState.SUCCESS

                                mUSBDevice?.let {
                                    instance.readDataFromScanner(
                                        this@MainActivity, it, mUSBManager
                                    ) {
                                        scrolltext.value.plus("\n$it")
                                    }
                                }
                            }
                            return@IndicatorButton
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            scrolltext.value,
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!", modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplication2Theme {
        Greeting("Android")
    }
}