package com.kenzo.timer

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun Vibrator(vibrate: Boolean) {
    val context = LocalContext.current
    val vibrator = remember { Vibrator(context) }
    if (vibrate) {
        vibrator.vibrate()
    } else {
        vibrator.cancel()
    }
}

class Vibrator(private val context: Context) {

    fun vibrate(@IntRange(0, 255) amplitude: Int = 150) {
        val timings = longArrayOf(30, 30)
        val amplitudes = intArrayOf(amplitude, amplitude / 2)
        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager ?: return
            val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, 0)
            val combinedVibration = CombinedVibration.createParallel(vibrationEffect)
            vibratorManager.vibrate(combinedVibration)
        } else {
            @Suppress("DEPRECATION") val vibrator =
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            val vibrationEffect = VibrationEffect.createWaveform(timings, amplitudes, 0)
            vibrator.vibrate(vibrationEffect)
        }
    }

    fun cancel() {
        if (Build.VERSION_CODES.S <= Build.VERSION.SDK_INT) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager ?: return
            vibratorManager.cancel()
        } else {
            @Suppress("DEPRECATION") val vibrator =
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            vibrator.cancel()
        }
    }
}