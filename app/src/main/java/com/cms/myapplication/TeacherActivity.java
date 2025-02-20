package com.cms.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;

public class TeacherActivity extends AppCompatActivity {
    private ImageView qrCodeImage;
    private TextView attendanceList;
    private SimpleServer server;
    private String ssid, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        qrCodeImage = findViewById(R.id.qrCodeImage);
        attendanceList = findViewById(R.id.attendanceList);

        ssid = "Attendance_" + System.currentTimeMillis();
        password = "Pass456"; // Fixed for simplicity
        long timestamp = System.currentTimeMillis() / 1000; // Unix timestamp in seconds
        String qrData = ssid + ":" + password + ":" + timestamp;
        generateAndShareQRCode(qrData);
        startServer();

        // Manually enable hotspot with ssid/password in settings
    }

    private void generateAndShareQRCode(String data) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 200, 200);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565);
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            qrCodeImage.setImageBitmap(bitmap);

            // Share to WhatsApp
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "AttendanceQR", null);
            Uri uri = Uri.parse(path);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setPackage("com.whatsapp");
            startActivity(Intent.createChooser(shareIntent, "Share QR to WhatsApp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        server = new SimpleServer(8080);
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SimpleServer extends NanoHTTPD {
        public SimpleServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String studentId = session.getParms().get("student_id");
            String receivedTimestamp = session.getParms().get("timestamp");
            long currentTime = System.currentTimeMillis() / 1000;
            if (studentId != null && receivedTimestamp != null &&
                    Long.parseLong(receivedTimestamp) + 300 > currentTime) { // 5-minute window
                runOnUiThread(() -> attendanceList.append("\n" + studentId));
                return Response.newFixedLengthResponse("Attendance marked");
            }
            return Response.newFixedLengthResponse("Error: Expired or invalid");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) server.stop();
    }
}