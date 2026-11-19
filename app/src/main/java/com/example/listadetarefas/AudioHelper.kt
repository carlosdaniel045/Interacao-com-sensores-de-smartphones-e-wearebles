package com.example.listadetarefas

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager

class AudioHelper(private val context: Context) {

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val packageManager = context.packageManager

    fun possuiAltoFalante(): Boolean {

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            return false
        }

        return audioManager
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
    }

    fun bluetoothConectado(): Boolean {

        return audioManager
            .getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
            }
    }

    fun getAudioManager(): AudioManager {
        return audioManager
    }
}