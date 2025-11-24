#ifndef SECRETS_MANAGER_H
#define SECRETS_MANAGER_H

#include <Firebase_ESP_Client.h>
#include <ArduinoJson.h>

class SecretsManager {
private:
    bool secretsLoaded = false;
    String wifiSSID, wifiPassword, firebaseApiKey, supabaseUrl, supabaseKey;

public:
    void initialize();
    bool fetchSecretsFromDatabase();
    String getWifiSSID() { return secretsLoaded ? wifiSSID : "Redmi 10"; }
    String getWifiPassword() { return secretsLoaded ? wifiPassword : "aishterul23"; }
    String getFirebaseApiKey() { return secretsLoaded ? firebaseApiKey : "AIzasyDhcsIE3LS3IGddMyKt2JPh_ShDF0v5fps"; }
    String getSupabaseUrl() { return secretsLoaded ? supabaseUrl : "https://sdpsctmrlxitnokcnelo.supabase.co"; }
    String getSupabaseKey() { return secretsLoaded ? supabaseKey : "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."; }
    bool areSecretsLoaded() { return secretsLoaded; }
};

#endif