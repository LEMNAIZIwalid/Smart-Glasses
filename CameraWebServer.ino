#include "esp_camera.h"
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include "addons/TokenHelper.h"
#include "addons/RTDBHelper.h"
#include "board_config.h"
#include "secrets_manager.h"  // ✅ CHANGÉ : secrets.h → secrets_manager.h
#include <time.h>
#include "base64.h"
#include <HTTPClient.h>

// ============================
// CONFIGURATION
// ============================
SecretsManager secretsManager;  // ✅ AJOUTÉ
const char *ssid = secretsManager.getWifiSSID().c_str();        // ✅ MODIFIÉ
const char *password = secretsManager.getWifiPassword().c_str(); // ✅ MODIFIÉ
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

// ============================
// PINS HARDWARE
// ============================
#define TRIG_PIN 14
#define ECHO_PIN 15
#define BUZZER_PIN 13

// ============================
// UID UTILISATEUR
// ============================
const String USER_UID = "LDCPnLKK6EeePBjBZQOIu1eZMys1";
const String BASE_PATH = "users/" + USER_UID;

// ============================
// VARIABLES
// ============================
unsigned long lastUploadImage = 0;
const unsigned long imageInterval = 30000;
unsigned long lastFirebaseSend = 0;
const unsigned long firebaseInterval = 2000;
const float distanceThreshold = 2.0;
float lastSentDistance = 1000.0;
bool cameraStatus = true;
const float DANGER_THRESHOLD = 40.0;
bool wasInDanger = false;
unsigned long buzzerPrevMillis = 0;
bool buzzerState = false;

// ============================
// FONCTION: Configuration NTP
// ============================
void setupNTP() {
  Serial.print("🕐 Configuration NTP...");
  
  // Configuration du timezone (Paris)
  configTime(3600, 3600, "pool.ntp.org", "time.nist.gov");
  
  // Attendre que l'heure soit synchronisée
  int retry = 0;
  while (time(nullptr) < 1000000000 && retry < 20) {
    Serial.print(".");
    delay(1000);
    retry++;
  }
  
  time_t now = time(nullptr);
  if (now > 1000000000) {
    Serial.println(" OK");
    Serial.print("📅 Heure actuelle: ");
    Serial.println(getISOTimestamp());
  } else {
    Serial.println(" FAIL - Utilisation heure locale");
  }
}

// ============================
// FONCTION: Timestamp ISO 8601 CORRIGÉ
// ============================
String getISOTimestamp() {
  time_t now = time(nullptr);
  
  // Vérifier si l'heure NTP est valide
  if (now < 1000000000) {
    // Heure NTP non disponible, utiliser millis() comme fallback
    unsigned long currentMillis = millis();
    unsigned long days = currentMillis / 86400000;
    currentMillis %= 86400000;
    unsigned long hours = currentMillis / 3600000;
    currentMillis %= 3600000;
    unsigned long minutes = currentMillis / 60000;
    currentMillis %= 60000;
    unsigned long seconds = currentMillis / 1000;
    
    // Format ISO 8601 avec date fictive mais temps réel
    char buffer[30];
    snprintf(buffer, sizeof(buffer), "2024-01-01T%02lu:%02lu:%02luZ", 
             hours, minutes, seconds);
    return String(buffer);
  }
  
  // Heure NTP valide
  struct tm timeinfo;
  gmtime_r(&now, &timeinfo);
  char buffer[30];
  strftime(buffer, sizeof(buffer), "%Y-%m-%dT%H:%M:%SZ", &timeinfo);
  return String(buffer);
}

// ============================
// FONCTION: Reconnexion WiFi
// ============================
void connectWiFi() {
  if(WiFi.status() != WL_CONNECTED) {
    Serial.print("🔄 WiFi...");
    WiFi.disconnect();
    WiFi.begin(ssid, password);
    
    unsigned long startAttemptTime = millis();
    while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < 15000) {
      delay(500);
      Serial.print(".");
    }
    
    if(WiFi.status() == WL_CONNECTED) {
      Serial.println(" OK");
      // NTP sera configuré dans le setup
    } else {
      Serial.println(" FAIL");
    }
  }
}

// ============================
// FONCTION: Initialisation Firebase
// ============================
bool initFirebase() {
  Serial.print("🔄 Firebase...");
  
  // ✅ MODIFIÉ : Utilisation des secrets dynamiques
  config.api_key = secretsManager.getFirebaseApiKey().c_str();
  config.database_url = "https://lado-smartglasses-default-rtdb.firebaseio.com/";
  auth.user.email = "marysakouti@gmail.com";
  auth.user.password = "Mary123";
  config.token_status_callback = tokenStatusCallback;
  
  fbdo.setBSSLBufferSize(4096, 1024);
  
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
  
  unsigned long startTime = millis();
  while (!Firebase.ready() && millis() - startTime < 10000) {
    delay(500);
    Serial.print(".");
  }
  
  if (Firebase.ready()) {
    Serial.println(" OK");
    return true;
  } else {
    Serial.println(" FAIL");
    return false;
  }
}

// ============================
// FONCTION: Synchronisation statut caméra
// ============================
void syncCameraStatus() {
  String statusPath = BASE_PATH + "/sensors/camera/status";
  
  if (Firebase.RTDB.getString(&fbdo, statusPath.c_str())) {
    String status = fbdo.stringData();
    bool newStatus = (status == "on");
    
    if (cameraStatus != newStatus) {
      cameraStatus = newStatus;
      Serial.println("📷 Caméra: " + status);
    }
  }
}

// ============================
// FONCTION: Envoi sécurisé Firebase
// ============================
bool sendToFirebase(const String& path, const String& value) {
  String fullPath = BASE_PATH + "/" + path;
  if (Firebase.RTDB.setString(&fbdo, fullPath.c_str(), value)) {
    return true;
  } else {
    Serial.print(" FirebaseErr");
    return false;
  }
}

bool sendToFirebase(const String& path, float value) {
  String fullPath = BASE_PATH + "/" + path;
  if (Firebase.RTDB.setFloat(&fbdo, fullPath.c_str(), value)) {
    return true;
  } else {
    Serial.print(" FirebaseErr");
    return false;
  }
}

// ============================
// FONCTION: Upload Supabase
// ============================
String uploadToSupabase(camera_fb_t *fb){
  if(!fb) return "";

  Serial.print("📤 Upload...");
  
  String filename = "obstacle_" + String(millis()) + ".jpg";
  
  // ✅ MODIFIÉ : Utilisation des secrets dynamiques
  String url = secretsManager.getSupabaseUrl() + "/storage/v1/object/obstacles/" + filename;

  HTTPClient http;
  http.begin(url);
  http.addHeader("Authorization", "Bearer " + secretsManager.getSupabaseKey());
  http.addHeader("Content-Type", "image/jpeg");
  http.setTimeout(15000);

  int httpCode = http.sendRequest("POST", (uint8_t*)fb->buf, fb->len);
  String publicUrl = "";

  if(httpCode == 200) {
    publicUrl = secretsManager.getSupabaseUrl() + "/storage/v1/object/public/obstacles/" + filename;
    Serial.println(" OK");
  } else {
    Serial.printf(" FAIL%d", httpCode);
  }

  http.end();
  return publicUrl;
}

// ============================
// FONCTION: Buzzer
// ============================
void handleBuzzer(float distance) {
  unsigned long now = millis();

  if (distance > 0 && distance < 20) {
    if (now - buzzerPrevMillis >= 250) {
      buzzerPrevMillis = now;
      buzzerState = !buzzerState;
      digitalWrite(BUZZER_PIN, buzzerState ? HIGH : LOW);
    }
  } 
  else if (distance >= 20 && distance < 40) {
    if (now - buzzerPrevMillis >= 100) {
      buzzerPrevMillis = now;
      buzzerState = !buzzerState;
      digitalWrite(BUZZER_PIN, buzzerState ? HIGH : LOW);
    }
  } 
  else {
    digitalWrite(BUZZER_PIN, LOW);
    buzzerState = false;
  }
}

// ============================
// FONCTION: Créer notification avec AUTO-ID
// ============================
void createNotification(String message) {
  String timestamp = getISOTimestamp();
  
  FirebaseJson notification;
  notification.set("message", message);
  notification.set("timestamp", timestamp);
  
  // Firebase génère automatiquement une clé unique
  String notificationPath = "notifications";
  
  if (Firebase.RTDB.pushJSON(&fbdo, (BASE_PATH + "/" + notificationPath).c_str(), &notification)) {
    Serial.print(" 📱Notif");
    Serial.print(" [" + timestamp + "]");
  } else {
    Serial.print(" ❌Notif");
  }
}

// ============================
// FONCTION: Capture photo
// ============================
void capturePhoto(float distance) {
  Serial.print(" 📸");
  
  camera_fb_t *fb = esp_camera_fb_get();
  if (fb) {
    String url = uploadToSupabase(fb);
    
    if (url != "") {
      sendToFirebase("sensors/camera/obstacle_image", url);
      Serial.print(" ✅Photo");
    } else {
      Serial.print(" ❌Photo");
    }
    
    esp_camera_fb_return(fb);
  } else {
    Serial.print(" ❌CAM");
  }
}

// ============================
// SETUP
// ============================
void setup() {
  Serial.begin(115200);
  Serial.println("\n🚀 ESP32-CAM - Démarrage");

  // ✅ AJOUTÉ : Initialisation des secrets
  secretsManager.initialize();

  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  digitalWrite(BUZZER_PIN, LOW);

  // Configuration caméra
  camera_config_t configCam;
  configCam.ledc_channel = LEDC_CHANNEL_0;
  configCam.ledc_timer = LEDC_TIMER_0;
  configCam.pin_d0 = Y2_GPIO_NUM;
  configCam.pin_d1 = Y3_GPIO_NUM;
  configCam.pin_d2 = Y4_GPIO_NUM;
  configCam.pin_d3 = Y5_GPIO_NUM;
  configCam.pin_d4 = Y6_GPIO_NUM;
  configCam.pin_d5 = Y7_GPIO_NUM;
  configCam.pin_d6 = Y8_GPIO_NUM;
  configCam.pin_d7 = Y9_GPIO_NUM;
  configCam.pin_xclk = XCLK_GPIO_NUM;
  configCam.pin_pclk = PCLK_GPIO_NUM;
  configCam.pin_vsync = VSYNC_GPIO_NUM;
  configCam.pin_href = HREF_GPIO_NUM;
  configCam.pin_sccb_sda = SIOD_GPIO_NUM;
  configCam.pin_sccb_scl = SIOC_GPIO_NUM;
  configCam.pin_pwdn = PWDN_GPIO_NUM;
  configCam.pin_reset = RESET_GPIO_NUM;
  configCam.xclk_freq_hz = 20000000;
  configCam.frame_size = FRAMESIZE_SVGA;
  configCam.pixel_format = PIXFORMAT_JPEG;
  configCam.fb_location = CAMERA_FB_IN_PSRAM;
  configCam.jpeg_quality = 12;
  configCam.fb_count = 1;

  if (esp_camera_init(&configCam) != ESP_OK) {
    Serial.println("❌ Caméra FAIL");
  } else {
    Serial.println("✅ Caméra OK");
  }

  connectWiFi();
  
  // ✅ CONFIGURATION NTP AVANT FIREBASE
  setupNTP();
  
  initFirebase();

  Serial.println("✅ Prêt - Seuil: <" + String(DANGER_THRESHOLD) + "cm");
  Serial.println("📁 Structure: users/" + USER_UID + "\n");
}

// ============================
// LOOP PRINCIPAL
// ============================
void loop() {
  connectWiFi();
  
  if (!Firebase.ready()) {
    initFirebase();
    delay(2000);
    return;
  }

  // Synchronisation statut caméra
  static unsigned long lastStatusSync = 0;
  if (millis() - lastStatusSync > 3000) {
    syncCameraStatus();
    lastStatusSync = millis();
  }

  // Mesure distance
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);

  long duration = pulseIn(ECHO_PIN, HIGH, 30000);
  if (duration == 0) {
    delay(100);
    return;
  }

  float distance = duration * 0.034 / 2;
  if (distance > 400 || distance < 2) {
    delay(100);
    return;
  }

  Serial.printf("📏 %.1fcm ", distance);
  
  bool isDanger = (distance < DANGER_THRESHOLD);
  String statusMessage = isDanger ? "danger" : "safe";
  
  if (distance < 20) Serial.print("🔴");
  else if (distance < 40) Serial.print("🟠");
  else Serial.print("🟢");

  handleBuzzer(distance);

  unsigned long now = millis();

  // MODE DANGER
  if (isDanger) {
    bool shouldSend = (abs(distance - lastSentDistance) > distanceThreshold) ||
                     (now - lastFirebaseSend > firebaseInterval);
    
    if (shouldSend) {
      bool success = true;
      
      success &= sendToFirebase("sensors/ultrasonic/distance", distance);
      success &= sendToFirebase("sensors/current/distance", distance);
      success &= sendToFirebase("sensors/current/stamp", getISOTimestamp());
      success &= sendToFirebase("sensors/current/status", statusMessage);
      success &= sendToFirebase("sensors/camera/status", cameraStatus ? "on" : "off");
      
      if (success) {
        lastSentDistance = distance;
        lastFirebaseSend = now;
        Serial.print(" ✅FB");
        
        static unsigned long lastNotif = 0;
        if (now - lastNotif > 5000) {
          lastNotif = now;
          createNotification("Current distance : " + String(distance, 1) + "cm");
          
          if (cameraStatus) {
            createNotification("Camera : on - Obstacle detected");
          } else {
            createNotification("Camera : off - Obstacle detected");
          }
        }
      } else {
        Serial.print(" ❌FB");
      }
    }

    if (cameraStatus && (now - lastUploadImage > imageInterval)) {
      lastUploadImage = now;
      capturePhoto(distance);
    }
    
    wasInDanger = true;
  } 
  // MODE SAFE
  else {
    if (wasInDanger) {
      Serial.print(" SAFE-CLEAN ");
      sendToFirebase("sensors/current/stamp", getISOTimestamp());
      sendToFirebase("sensors/current/status", statusMessage);
      wasInDanger = false;
    } else {
      Serial.print(" SAFE ");
      static unsigned long lastSafeUpdate = 0;
      if (now - lastSafeUpdate > 10000) {
        lastSafeUpdate = now;
        sendToFirebase("sensors/current/stamp", getISOTimestamp());
        sendToFirebase("sensors/current/status", statusMessage);
      }
    }
  }

  Serial.println();
  delay(500);
}