package com.bstyle.nearalert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val e = GeofencingEvent.fromIntent(i) ?: return
        if (e.hasError()) return
        val last = c.getSharedPreferences("n", 0)
        if (System.currentTimeMillis() - last.getLong("t", 0) < 6 * 3600 * 1000) return
        last.edit().putLong("t", System.currentTimeMillis()).apply()
        val nm = c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel("near", "قريب من الفرع", NotificationManager.IMPORTANCE_HIGH))
        val n = NotificationCompat.Builder(c, "near").setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("بي ستايل - جوري مول")
            .setContentText("أنت قريب منا! تعالى النهارده، احجز دلوقتي.")
            .setAutoCancel(true).build()
        nm.notify(1, n)
    }
}
