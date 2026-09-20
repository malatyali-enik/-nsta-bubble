package com.vipbubble

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var tvNotificationStatus: TextView
    private lateinit var tvOverlayStatus: TextView
    private lateinit var btnGrantNotif: Button
    private lateinit var btnGrantOverlay: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvNotificationStatus = findViewById(R.id.tvNotifStatus)
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus)
        btnGrantNotif = findViewById(R.id.btnGrantNotif)
        btnGrantOverlay = findViewById(R.id.btnGrantOverlay)

        // 1. Bildirim Dinleme İzni Yönlendirmesi
        btnGrantNotif.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Listeden 'VIP DM Bubble'ı bulup aktif edin", Toast.LENGTH_LONG).show()
        }

        // 2. Diğer Uygulamaların Üzerinde Görünme İzni (SYSTEM_ALERT_WINDOW)
        btnGrantOverlay.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Üstte çizim izni zaten verilmiş!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatuses()
    }

    private fun updatePermissionStatuses() {
        // Bildirim dinleme izni kontrolü
        val isNotifGranted = isNotificationServiceEnabled()
        tvNotificationStatus.text = if (isNotifGranted) "Bildirim Erişimi: ONAYLANDI ✅" else "Bildirim Erişimi: VERİLMEDİ ❌"
        btnGrantNotif.isEnabled = !isNotifGranted

        // Overlay izni kontrolü
        val isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
        tvOverlayStatus.text = if (isOverlayGranted) "Üstte Çizim İzni: ONAYLANDI ✅" else "Üstte Çizim İzni: VERİLMEDİ ❌"
        btnGrantOverlay.isEnabled = !isOverlayGranted
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }
}