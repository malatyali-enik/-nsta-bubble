package com.vipbubble.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class InstagramNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "InstagramNotifService"
        private const val INSTAGRAM_PACKAGE = "com.instagram.android"
        
        // SharedPreferences üzerinden dinamik VIP listesi çekilebilir
        val defaultVipContacts = mutableListOf("Ahmet", "Ayşe", "En Yakın Arkadaş")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        if (sbn == null) return

        // 1. Sadece Instagram bildirimlerini filtrele
        if (sbn.packageName == INSTAGRAM_PACKAGE) {
            val extras = sbn.notification.extras ?: return
            
            // Mesajı atan kişinin adı (android.title)
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            // Mesaj içeriği
            val messageText = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            Log.d(TAG, "Instagram bildirimi yakalandı: Başlık='$title', Mesaj='$messageText'")

            // 2. VIP Kontrolü: Kişi kayıtlı VIP listesinde var mı?
            val isVip = isSenderInVipList(title)

            // 3. Ekran açık mı kontrolü (isInteractive)
            val screenOn = isScreenOn()

            if (isVip && screenOn) {
                Log.d(TAG, "VIP eşleşti ($title) ve ekran açık -> Baloncuk fırlatılıyor!")
                launchFloatingBubble(title, messageText)
            } else {
                Log.d(TAG, "Bildirim atlandı. VIP: $isVip, Ekran Açık: $screenOn")
            }
        }
    }

    private fun isSenderInVipList(senderName: String): Boolean {
        if (senderName.isBlank()) return false
        val prefs = getSharedPreferences("vip_prefs", Context.MODE_PRIVATE)
        val savedVips = prefs.getStringSet("vip_list", defaultVipContacts.toSet()) ?: defaultVipContacts.toSet()
        
        // İsmin birebir veya parça olarak eşleşmesi (örn. 'Ahmet', 'Ahmet Yılmaz')
        return savedVips.any { vip ->
            senderName.equals(vip, ignoreCase = true) || senderName.contains(vip, ignoreCase = true)
        }
    }

    private fun isScreenOn(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isInteractive ?: false
    }

    private fun launchFloatingBubble(senderName: String, messagePreview: String) {
        val intent = Intent(this, FloatingBubbleService::class.java).apply {
            action = FloatingBubbleService.ACTION_SHOW_BUBBLE
            putExtra(FloatingBubbleService.EXTRA_SENDER, senderName)
            putExtra(FloatingBubbleService.EXTRA_MESSAGE, messagePreview)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}