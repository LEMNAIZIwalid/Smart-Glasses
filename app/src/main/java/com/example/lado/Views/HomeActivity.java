package com.example.lado.Views;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lado.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private TextView textUltrasonic, textCamera, textCurrentDistance, textCurrentStatus, textCurrentStamp;
    private TextView textLastSync, textWelcome, textESPStatus;

    private static final String TAG = "HomeActivity";
    private String uid;
    private String lastCameraStatus = "--";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice espDevice;
    private BluetoothSocket espSocket;
    private final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Handler handler = new Handler();
    private static final long ESP_SCAN_INTERVAL_MS = 10000; // 10s

    private static final int PERMISSION_REQUEST_BLUETOOTH = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupBottomNavigation();

        uid = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", "");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        checkBluetoothPermission();

        if (!uid.isEmpty()) {
            startPeriodicESPScan(uid);
            loadSensorsFromFirebase();
        }
    }

    /** ------------------- INIT UI ------------------- */
    private void initViews() {
        textWelcome = findViewById(R.id.textWelcome);
        textUltrasonic = findViewById(R.id.textUltrasonic);
        textCamera = findViewById(R.id.textCamera);
        textCurrentDistance = findViewById(R.id.textCurrentDistance);
        textCurrentStatus = findViewById(R.id.textCurrentStatus);
        textCurrentStamp = findViewById(R.id.textCurrentStamp);
        textLastSync = findViewById(R.id.textLastSync);
        textESPStatus = findViewById(R.id.textESPStatus);

        safeSetText(textUltrasonic, "--");
        safeSetText(textCamera, "--");
        safeSetText(textCurrentDistance, "--");
        safeSetText(textCurrentStatus, "--");
        safeSetText(textCurrentStamp, "--");
        safeSetText(textLastSync, "--");
        safeSetText(textESPStatus, "Connexion ESP32...");
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_historique) return true;

            Intent intent = null;
            if (id == R.id.nav_livestream) intent = new Intent(this, LivestreamActivity.class);
            else if (id == R.id.nav_statics) intent = new Intent(this, StaticsActivity.class);
            else if (id == R.id.nav_notifications) intent = new Intent(this, NotificationsActivity.class);
            else if (id == R.id.nav_profile) intent = new Intent(this, ProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });
    }

    /** ------------------- PERMISSIONS ------------------- */
    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        PERMISSION_REQUEST_BLUETOOTH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> textESPStatus.setText("❌ Permission BLUETOOTH_CONNECT refusée"));
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /** ------------------- SCAN ESP32 ------------------- */
    private void startPeriodicESPScan(String uid) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scanESP32Bluetooth(uid, success -> {
                    textESPStatus.setText(success ? "✅ ESP32 disponible" : "❌ ESP32 non disponible");
                    handler.postDelayed(this, ESP_SCAN_INTERVAL_MS);
                });
            }
        });
    }

    private void scanESP32Bluetooth(String uid, ESPScanCallback callback) {
        new Thread(() -> {
            boolean found = false;
            Set<BluetoothDevice> pairedDevices = getPairedDevicesSafe();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    if ("ESP32".equals(device.getName())) {
                        espDevice = device;
                        found = true;
                        connectToESP32Bluetooth(uid);
                        break;
                    }
                }
            }
            boolean finalFound = found;
            runOnUiThread(() -> callback.onScanCompleted(finalFound));
        }).start();
    }

    private Set<BluetoothDevice> getPairedDevicesSafe() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(() -> textESPStatus.setText("❌ Permission BLUETOOTH_CONNECT manquante"));
                return null;
            }
            return bluetoothAdapter.getBondedDevices();
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException: " + e.getMessage());
            runOnUiThread(() -> textESPStatus.setText("❌ Erreur sécurité Bluetooth"));
            return null;
        }
    }

    private void connectToESP32Bluetooth(String uid) {
        new Thread(() -> {
            try {
                if (espSocket != null && espSocket.isConnected()) espSocket.close();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> textESPStatus.setText("❌ Permission BLUETOOTH_CONNECT manquante"));
                    return;
                }

                espSocket = espDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                espSocket.connect();
                runOnUiThread(() -> textESPStatus.setText("🔌 Connecté à ESP32"));

                sendUIDToESP32Bluetooth(uid);

            } catch (SecurityException e) {
                runOnUiThread(() -> textESPStatus.setText("❌ Erreur sécurité Bluetooth"));
                Log.e(TAG, "SecurityException Bluetooth : " + e.getMessage());
            } catch (IOException e) {
                runOnUiThread(() -> textESPStatus.setText("❌ Connexion Bluetooth échouée"));
                Log.e(TAG, "IOException : " + e.getMessage());
                try { if (espSocket != null) espSocket.close(); } catch (IOException ex) {}
            }
        }).start();
    }

    private void sendUIDToESP32Bluetooth(String uid) {
        if (espSocket == null || !espSocket.isConnected()) return;
        new Thread(() -> {
            try {
                OutputStream outputStream = espSocket.getOutputStream();
                outputStream.write(uid.getBytes());
                outputStream.flush();
                runOnUiThread(() -> textESPStatus.setText("UID envoyé via Bluetooth"));
            } catch (IOException e) {
                runOnUiThread(() -> textESPStatus.setText("❌ Erreur envoi UID Bluetooth"));
                Log.e(TAG, "Erreur envoi UID Bluetooth : " + e.getMessage());
            }
        }).start();
    }

    private interface ESPScanCallback {
        void onScanCompleted(boolean success);
    }

    /** ------------------- FIREBASE ------------------- */
    private void loadSensorsFromFirebase() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) return;

                String username = snap.child("username").getValue(String.class);
                if (username != null) textWelcome.setText("👋 Bonjour " + username);

                DataSnapshot sensors = snap.child("sensors");
                if (!sensors.exists()) return;

                Object distUS = sensors.child("ultrasonic").child("distance").getValue();
                if (distUS != null) textUltrasonic.setText(distUS + " cm");

                Object cameraObj = sensors.child("camera").child("status").getValue();
                if (cameraObj != null) {
                    String s = cameraObj.toString();
                    if (!s.equals(lastCameraStatus)) {
                        textCamera.setText(s);
                        lastCameraStatus = s;
                    }
                }

                DataSnapshot curr = sensors.child("current");
                if (curr.exists()) {
                    Object dist = curr.child("distance").getValue();
                    Object stat = curr.child("status").getValue();
                    Object stamp = curr.child("stamp").getValue();

                    if (dist != null) textCurrentDistance.setText(dist + " cm");
                    if (stat != null) textCurrentStatus.setText(stat.toString());
                    if (stamp != null) {
                        String s = stamp.toString();
                        textCurrentStamp.setText(s);
                        textLastSync.setText(getTimeAgo(s));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    private String getTimeAgo(String isoStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        try {
            Date date = sdf.parse(isoStamp);
            if (date == null) return "--";

            long diff = System.currentTimeMillis() - date.getTime();
            if (diff < 60000) return "À l'instant";
            long minutes = diff / 60000;
            if (minutes < 60) return "il y a " + minutes + " min";
            long hours = minutes / 60;
            if (hours < 24) return "il y a " + hours + " h";
            return "il y a " + (hours / 24) + " j";
        } catch (Exception e) {
            return "--";
        }
    }

    private void safeSetText(TextView tv, String val) {
        if (tv != null) tv.setText(val == null ? "--" : val);
    }
}
