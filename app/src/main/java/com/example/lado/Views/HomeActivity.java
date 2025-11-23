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
import com.example.lado.services.FirebaseService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private TextView textUltrasonic, textCamera, textCurrentDistance, textCurrentStatus, textCurrentStamp;
    private TextView textLastSync, textWelcome, textESPStatus;

    private static final String TAG = "HomeActivity";

    private String uid;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice espDevice;
    private BluetoothSocket espSocket;

    private final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Handler handler = new Handler();
    private static final long ESP_SCAN_INTERVAL_MS = 10000;

    private static final int PERMISSION_REQUEST_BLUETOOTH = 1001;

    private FirebaseService firebaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupBottomNavigation();

        uid = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .getString("userId", "");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            textESPStatus.setText("Bluetooth non supporté");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1
            );
        }

        checkBluetoothPermission();

        if (!uid.isEmpty()) {
            startPeriodicESPScan();
            loadSensorsFromFirebase();

            // service notifications
            firebaseService = new FirebaseService(this, uid);
            firebaseService.startListening();
        }
    }

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

            if (id == R.id.nav_livestream)
                intent = new Intent(this, LivestreamActivity.class);
            else if (id == R.id.nav_statics)
                intent = new Intent(this, StaticsActivity.class);
            else if (id == R.id.nav_notifications)
                intent = new Intent(this, NotificationsActivity.class);
            else if (id == R.id.nav_profile)
                intent = new Intent(this, ProfileActivity.class);

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            return true;
        });
    }

    private void checkBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        PERMISSION_REQUEST_BLUETOOTH
                );
            }
        }
    }

    private void startPeriodicESPScan() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scanESP32Bluetooth(found -> {
                    textESPStatus.setText(found ? "ESP32 disponible" : "ESP32 non disponible");
                    handler.postDelayed(this, ESP_SCAN_INTERVAL_MS);
                });
            }
        });
    }

    private void scanESP32Bluetooth(ESPScanCallback callback) {

        new Thread(() -> {
            boolean found = false;

            Set<BluetoothDevice> pairedDevices = getPairedDevicesSafe();
            if (pairedDevices != null) {
                for (BluetoothDevice device : pairedDevices) {
                    if ("ESP32".equals(device.getName())) {
                        espDevice = device;
                        found = true;
                        connectToESP32Bluetooth();
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
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {

                runOnUiThread(() ->
                        textESPStatus.setText("Permission Bluetooth manquante"));

                return null;
            }

            return bluetoothAdapter.getBondedDevices();

        } catch (Exception e) {
            Log.e(TAG, "Erreur Bluetooth: " + e.getMessage());
            return null;
        }
    }

    private void connectToESP32Bluetooth() {
        new Thread(() -> {
            try {
                if (espSocket != null && espSocket.isConnected())
                    espSocket.close();

                espSocket = espDevice.createRfcommSocketToServiceRecord(SPP_UUID);
                espSocket.connect();

                runOnUiThread(() -> textESPStatus.setText("Connecté à ESP32"));

                sendUIDToESP32Bluetooth(uid);

            } catch (Exception e) {
                runOnUiThread(() -> textESPStatus.setText("Connexion Bluetooth échouée"));
            }
        }).start();
    }

    private void sendUIDToESP32Bluetooth(String uid) {
        try {
            if (espSocket == null || !espSocket.isConnected()) return;

            OutputStream outputStream = espSocket.getOutputStream();
            outputStream.write(uid.getBytes());
            outputStream.flush();

            runOnUiThread(() -> textESPStatus.setText("UID envoyé"));

        } catch (Exception ignored) {}
    }

    private interface ESPScanCallback {
        void onScanCompleted(boolean success);
    }

    private void loadSensorsFromFirebase() {

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {

                if (!snap.exists()) return;

                // username
                String username = snap.child("username").getValue(String.class);
                if (username != null) {
                    textWelcome.setText("Bonjour " + username);
                }

                DataSnapshot sensors = snap.child("sensors");
                if (!sensors.exists()) return;

                // Ultrasonic
                Object distUS = sensors.child("ultrasonic").child("distance").getValue();
                if (distUS != null)
                    textUltrasonic.setText(distUS + " cm");

                // Camera
                Object cameraObj = sensors.child("camera").child("status").getValue();
                if (cameraObj != null)
                    textCamera.setText(cameraObj.toString());

                // Current
                DataSnapshot curr = sensors.child("current");
                if (curr.exists()) {
                    Object dist = curr.child("distance").getValue();
                    Object stat = curr.child("status").getValue();
                    Object stamp = curr.child("stamp").getValue();

                    if (dist != null)
                        textCurrentDistance.setText(dist + " cm");

                    if (stat != null)
                        textCurrentStatus.setText(stat.toString());

                    if (stamp != null) {
                        textCurrentStamp.setText(stamp.toString());
                        textLastSync.setText(getTimeAgo(stamp.toString()));
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
        try {
            java.text.SimpleDateFormat sdf =
                    new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.util.Date date = sdf.parse(isoStamp);
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
        if (tv != null) {
            tv.setText(val == null ? "--" : val);
        }
    }
}
