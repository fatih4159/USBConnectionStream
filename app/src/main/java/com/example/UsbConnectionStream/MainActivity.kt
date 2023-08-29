package com.example.UsbConnectionStream

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.UsbConnectionStream.ui.theme.MyApplication2Theme
import com.example.UsbConnectionStream.util.UsbSerialConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var instance = UsbSerialConnection()
        lateinit var mUSBManager: UsbManager
        lateinit var mUSBDevice: UsbDevice
        lateinit var mUSBInterface: UsbInterface
        lateinit var mUSBDeviceConnection: UsbDeviceConnection


        super.onCreate(savedInstanceState)
        setContent {
            MyApplication2Theme {
                var text1 = remember { mutableStateOf("1.Obtain USB Manager") }
                var text2 = remember { mutableStateOf("2.Fetch first USB-Device") }
                var text3 = remember { mutableStateOf("3.Fetch first Interface") }
                var text4 = remember { mutableStateOf("4.Request Permission") }
                var text5 = remember { mutableStateOf("5.Open Device Connection") }
                var text6 = remember { mutableStateOf("6.Get Data from Stream") }
                var text7 = remember { mutableStateOf("7.Claim Interface") }
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(text = "Follow the Order of the Buttons")
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))
                        ButtonWithText(text1) {
                            run {
                                CoroutineScope(Dispatchers.IO).launch {
                                    instance.obtainUsbManager(applicationContext)
                                        .onSuccess { usbManager ->
                                            mUSBManager = usbManager
                                            text1.value = "Success"
                                        }.onFailure { exception ->
                                            text1.value = "Failure"
                                        }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                        ButtonWithText(text = text2) {
                            run {
                                CoroutineScope(Dispatchers.IO).launch {
                                    instance.fetchFirstUsbDevice(mUSBManager)
                                        .onSuccess { usbDevice ->
                                            mUSBDevice = usbDevice
                                            text2.value = "Success"
                                        }.onFailure { exception ->
                                            text2.value = "Failure"
                                        }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                        ButtonWithText(text = text3) {
                            run {
                                CoroutineScope(Dispatchers.IO).launch {
                                    instance.fetchFirstInterface(mUSBDevice)
                                        .onSuccess { usbInterface ->
                                            mUSBInterface = usbInterface
                                            text3.value = "Success"
                                        }.onFailure { exception ->
                                            text3.value = "Failure"
                                        }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                        ButtonWithText(text = text4) {
                            run {
                                CoroutineScope(Dispatchers.IO).launch {
                                    instance.getPermissions(mUSBManager, mUSBDevice, applicationContext)
                                        .onSuccess {
                                            text4.value = "Success"
                                        }.onFailure { exception ->
                                            text4.value = "Failure"
                                        }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                        ButtonWithText(text = text5) {
                            run {
                                CoroutineScope(Dispatchers.IO).launch {
                                    instance.openDeviceConnection(mUSBManager, mUSBDevice)
                                        .onSuccess { usbdeviceconnection ->
                                            mUSBDeviceConnection = usbdeviceconnection
                                            text5.value = "Success"
                                        }.onFailure { exception ->
                                            text5.value = "Failure"
                                        }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.fillMaxWidth().height(10.dp))

                        ButtonWithText(text = text6) {
                            run {
                                CoroutineScope(Dispatchers.IO).launch {
                                    instance.getUsbSerialDataSteam(mUSBDeviceConnection)
                                        .onSuccess { usbdeviceconnection ->
                                            text6.value = "Success"
                                        }.onFailure { exception ->
                                            text6.value = "Failure"
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun ButtonWithText(
    text: MutableState<String>, onClick: () -> Unit
) {
    Button(modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth(), onClick = {
        CoroutineScope(Dispatchers.IO).launch {
            onClick()
        }
    }) {
        Text(text = text.value)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplication2Theme {
        Greeting("Android")
    }
}