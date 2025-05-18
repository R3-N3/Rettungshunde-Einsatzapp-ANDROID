package com.rettungshundeEinsatzApp.service.myLocation


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopLocationServiceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val stopIntent = Intent(context, MyLocationService::class.java)
        stopIntent.action = MyLocationService.ACTION_STOP_LOCATION
        context.stopService(stopIntent)
    }
}