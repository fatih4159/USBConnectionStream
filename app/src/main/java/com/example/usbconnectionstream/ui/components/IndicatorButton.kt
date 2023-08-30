package com.example.usbconnectionstream.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun IndicatorButton(
    buttonLabel: String,
    indicatorButtonState: MutableState<IndicatorButtonState> = mutableStateOf(IndicatorButtonState.DEFAULT),
    enable :Boolean,
    onClick: () -> Unit
) {
    Button(enabled = enable,modifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth(), onClick = {
        CoroutineScope(Dispatchers.IO).launch {
            onClick()
        }
    }) {
        Text(text = buttonLabel)
        Icon(indicatorButtonState.value.icon, contentDescription = null)
    }

}

enum class IndicatorButtonState(icon: ImageVector) {
    SUCCESS(Icons.Default.Check),
    FAILURE(Icons.Default.Clear),
    WAITING(Icons.Default.Search),
    DEFAULT(Icons.Default.PlayArrow);
    var icon = icon


}