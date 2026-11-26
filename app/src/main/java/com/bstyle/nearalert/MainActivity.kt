package com.bstyle.nearalert

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        status = TextView(this).apply { textSize = 20f; setPadding(48, 96, 48, 48) }
        setContentView(status)
        ask()
    }

    private fun ask() {
        val p = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) p.add(Manifest.permission.POST_NOTIFICATIONS)
        if (p.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions(p.toTypedArray(), 1); return
        }
        if (Build.VERSION.SDK_INT >= 29 &&
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            status.text = "اختار 'السماح طول الوقت' للموقع"
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 2); return
        }
        register()
    }

    override fun onRequestPermissionsResult(c: Int, p: Array<out String>, r: IntArray) {
        if (c == 1 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ask()
        else if (c == 2) {
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) register()
            else status.text = "لازم تفتح إعدادات التطبيق > الموقع > السماح طول الوقت"
        }
    }

    private fun register() {
        val g = Geofence.Builder().setRequestId("jouri-mall")
            .setCircularRegion(21.276402, 40.446297, 300f)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER).build()
        val req = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER).addGeofence(g).build()
        val pi = PendingIntent.getBroadcast(this, 0, Intent(this, GeofenceReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        try {
            LocationServices.getGeofencingClient(this).addGeofences(req, pi)
                .addOnSuccessListener { status.text = "تم التفعيل. هيجيلك إشعار لما تقرب من بي ستايل جوري مول." }
                .addOnFailureListener { status.text = "فشل التفعيل: " + it.message }
        } catch (e: SecurityException) { status.text = "الصلاحية ناقصة" }
    }
}
