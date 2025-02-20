package com.cms.myapplication;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class StudentActivity extends AppCompatActivity {
    private TextView statusText;
    private String studentId = "S" + (int)(Math.random() * 1000); // Random for demo
    private final ActivityResultLauncher<ScanOptions> scanLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String[] data = result.getContents().split(":");
            String ssid = data[0];
            String password = data[1];
            String timestamp = data[2];
            connectToHotspot(ssid, password); // Manual connection
            sendAttendance(timestamp);
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        statusText = findViewById(R.id.statusText);
        Button btnScan = findViewById(R.id.btnScanQR);
        btnScan.setOnClickListener(v -> scanQRCode());
    }

    private void scanQRCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR from WhatsApp");
        options.setBeepEnabled(false);
        scanLauncher.launch(options);
    }

    private void connectToHotspot(String ssid, String password) {
        // Manual connection via Wi-Fi settings for simplicity
    }

    private void sendAttendance(String timestamp) {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.43.1:8080"); // Default Android hotspot IP
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                String data = "student_id=" + studentId + "&timestamp=" + timestamp;
                OutputStream os = conn.getOutputStream();
                os.write(data.getBytes());
                os.flush();
                os.close();

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> {
                        statusText.setText("Status: Attendance marked");
                        disconnectFromHotspot();
                    });
                } else {
                    runOnUiThread(() -> statusText.setText("Status: Failed"));
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> statusText.setText("Status: Failed"));
            }
        }).start();
    }

    private void disconnectFromHotspot() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.disconnect();
    }
}