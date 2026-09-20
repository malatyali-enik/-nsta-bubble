package com.vipbubble.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.vipbubble.R
import kotlin.math.abs

class FloatingBubbleService : Service() {

    companion object {
        const val ACTION_SHOW_BUBBLE = "com.vipbubble.ACTION_SHOW_BUBBLE"
        const val ACTION_DISMISS = "com.vipbubble.ACTION_DISMISS"
        const val EXTRA_SENDER = "extra_sender"
        const val EXTRA_MESSAGE = "extra_message"
        private const val NOTIF_CHANNEL_ID = "vip_bubble_channel"
        private const val NOTIF_ID = 101

        // Instagram DM Doğrudan Deep Link'leri
        const val INSTAGRAM_DIRECT_URI = "instagram://direct_v2"
        const val INSTAGRAM_WEB_INBOX = "https://instagram.com/direct/inbox/"
    }

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startInForeground()
    }

    private fun startInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "VIP Bubble Servisi",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("VIP Baloncuk Aktif")
            .setContentText("Instagram VIP mesajları için overlay hazır")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_BUBBLE -> {
                val sender = intent.getStringExtra(EXTRA_SENDER) ?: "VIP Mesaj"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
                showFloatingWidget(sender, message)
            }
            ACTION_DISMISS -> {
                removeBubble()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun showFloatingWidget(senderName: String, messageText: String) {
        // Zaten baloncuk varsa önce temizle
        removeBubble()

        val inflater = LayoutInflater.from(this)
        bubbleView = inflater.inflate(R.layout.widget_bubble, null)

        val tvSender = bubbleView?.findViewById<TextView>(R.id.tvSenderName)
        val tvInitial = bubbleView?.findViewById<TextView>(R.id.tvAvatarInitial)
        val btnClose = bubbleView?.findViewById<ImageView>(R.id.btnCloseBubble)

        tvSender?.text = senderName
        tvInitial?.text = senderName.take(1).uppercase()

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        setupDragAndClickListeners(senderName)

        btnClose?.setOnClickListener {
            removeBubble()
        }

        try {
            windowManager?.addView(bubbleView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDragAndClickListeners(senderName: String) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        val touchSlop = 10

        bubbleView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params?.x = initialX + (event.rawX - initialTouchX).toInt()
                    params?.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(bubbleView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = abs(event.rawX - initialTouchX)
                    val diffY = abs(event.rawY - initialTouchY)

                    // Parmağın hareketi çok az ise bu bir TIKLAMADIR
                    if (diffX < touchSlop && diffY < touchSlop) {
                        openInstagramDirect()
                        removeBubble()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun openInstagramDirect() {
        // 1. Önce doğrudan Instagram App Direct Deep Link'ini dene
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_DIRECT_URI)).apply {
            setPackage("com.instagram.android")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(appIntent)
        } catch (e: Exception) {
            // Instagram uygulaması yüklü değilse veya URI açılamazsa tarayıcı ile direct inbox aç
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(INSTAGRAM_WEB_INBOX)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(webIntent)
        }
    }

    private fun removeBubble() {
        if (bubbleView != null && windowManager != null) {
            try {
                windowManager?.removeView(bubbleView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            bubbleView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeBubble()
    }
}