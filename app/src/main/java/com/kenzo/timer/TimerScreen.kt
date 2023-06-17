package com.kenzo.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimerScreen() {
    var second by remember { mutableStateOf(10L) }
    var status by remember { mutableStateOf(Status.Stopped) }
    val rest by rememberTimerAsState(
        second = second,
        status = status,
        onCountZero = { status = Status.Ringing }
    )

    Vibrator(status == Status.Ringing)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = when (status) {
                    Status.Stopped -> Color(0xFFFFFFFF)
                    Status.Counting -> Color.Green.copy(alpha = 0.1f)
                    Status.Paused -> Color.Yellow.copy(alpha = 0.1f)
                    Status.Ringing -> Color.Red.copy(alpha = 0.1f)
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.width(IntrinsicSize.Min)) {
                if (status == Status.Stopped) {
                    EditableCount(
                        second = second,
                        onSecondChange = { second = it }
                    )
                } else {
                    Text(
                        text = (if (status == Status.Stopped) second else rest).toString(),
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { status = status.next() },
                enabled = second > 0
            ) {
                Text(
                    text = when (status) {
                        Status.Stopped -> "Start"
                        Status.Counting -> "Pause"
                        Status.Paused -> "Resume"
                        Status.Ringing -> "Stop"
                    }
                )
            }
            if (status == Status.Paused) {
                Button(
                    onClick = { status = Status.Stopped },
                ) {
                    Text(text = "Reset")
                }
            }
        }
    }
}

@Composable
fun rememberTimerAsState(
    second: Long,
    status: Status,
    onCountZero: () -> Unit
): MutableState<Long> {
    val rest = remember { mutableStateOf(second) }

    when (status) {
        Status.Stopped -> {
            rest.value = second
        }
        Status.Counting -> {
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
                scope.launch {
                    while (rest.value > 0) {
                        delay(1000L)
                        rest.value = rest.value - 1
                        if (rest.value == 0L) {
                            onCountZero()
                        }
                    }
                }
            }
        }
        else -> {}
    }
    return rest
}

@Composable
fun EditableCount(
    second: Long,
    onSecondChange: (Long) -> Unit
) {
    val textFieldValue by rememberUpdatedState(
        newValue = TextFieldValue(
            text = second.toString(),
            selection = TextRange(second.toString().length)
        )
    )
    BasicTextField(
        value = textFieldValue,
        onValueChange = {
            if (it.text.length <= 2) {
                runCatching { it.text.ifEmpty { "0" }.toLong() }
                    .onSuccess { newSecond -> onSecondChange(newSecond) }
            }
        },
        textStyle = MaterialTheme.typography.displayLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

enum class Status {
    Stopped,
    Counting,
    Paused,
    Ringing;

    fun next(): Status {
        return when (this) {
            Stopped -> Counting
            Counting -> Paused
            Paused -> Counting
            Ringing -> Stopped
        }
    }
}