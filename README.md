# Instagram VIP DM Floating Bubble Android Project
📱 Android NotificationListenerService & SYSTEM_ALERT_WINDOW Overlay Uygulaması

Bu proje, Instagram'dan gelen VIP bildirimlerini dinleyen ve ekranda sürüklenebilir yüzen bir sohbet balonu çıkaran tam teşekküllü yerel (native) Android projesidir.

---

## ⚡ APK Nasıl Oluşturulur? (3 Kolay Yöntem)

### Yöntem 1: GitHub Actions ile Bulutta Otomatik APK (Bilgisayara program kurmadan!)
Bu projenin içinde hazır `.github/workflows/build-apk.yml` CI/CD pipeline'ı bulunur.
1. GitHub hesabınızda yeni bir repo oluşturun.
2. Bu ZIP'teki tüm dosyaları o depoya yükleyin (git push).
3. GitHub'da **Actions** sekmesine tıklayın; sistem 1 dakikada Android ortamını kurup projeyi derler.
4. Çıkan `InstagramVipBubble-Debug-APK` artifact'ini doğrudan telefonunuza indirip kurun!

### Yöntem 2: Android Studio ile Tek Tıkla APK
1. Bu ZIP dosyasını bir klasöre çıkartın.
2. Android Studio'yu açın ve **Open** diyerek klasörü seçin (Gradle otomatik senkronize olur).
3. Üst menüden: **Build > Build Bundle(s) / APK(s) > Build APK(s)** seçeneğine tıklayın.
4. 1 dakika sonra çıkan bildirimdeki **locate** bağlantısına tıklayın.
   Dosya yolu: `app/build/outputs/apk/debug/app-debug.apk`
5. Bu APK'yı telefonunuza atıp kurun!

### Yöntem 3: Terminal / Komut Satırı ile
```bash
# Mac & Linux:
./gradlew assembleDebug

# Windows (CMD / PowerShell):
gradlew.bat assembleDebug
```
Oluşan APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🚀 Temel Özellikler
1. **NotificationListenerService (`InstagramNotificationService.kt`)**: Sadece `com.instagram.android` paketini ve tanımlı VIP kişileri filtreler. Ekran açıksa (`isInteractive`) baloncuk servisini tetikler.
2. **SYSTEM_ALERT_WINDOW (`FloatingBubbleService.kt`)**: Ekranda sürüklenebilir baloncuk çizer.
3. **Deep Link Hızlı Geçiş**: Baloncuğa tıklandığında `instagram://direct_v2` üzerinden Instagram DM kutusunu açar.

## 📱 İzinler (Uygulama ilk açıldığında verilmeli):
1. **Bildirim Erişimi**: Settings > Notification Listener
2. **Diğer Uygulamaların Üzerinde Görünme**: Settings > Manage Overlay
