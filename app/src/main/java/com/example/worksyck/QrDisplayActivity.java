package com.example.worksyck;



import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QrDisplayActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1001;
    private ImageView qrImageView;
    private Bitmap qrBitmap;
    private String companyName = "WorkSync"; // Replace or get from Intent

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_qr_display);
        Button generateButton = findViewById(R.id.generate);

        generateButton.setOnClickListener(v -> {

            generateAndShowQRCode();
        });
        qrImageView = findViewById(R.id.qrImageView);
        Button saveBtn = findViewById(R.id.saveQrBtn);


        saveBtn.setOnClickListener(v -> {
            if (hasStoragePermission()) {
                saveQrImage();
            } else {
                requestStoragePermission();
            }
        });
    }

    private void generateAndShowQRCode() {
        try {
            long timestamp = System.currentTimeMillis();
            String rawData = "WorkSync_" + timestamp + "_" + companyName;

            String encrypted = QRUtils.encrypt(rawData);
            qrBitmap = QRUtils.generateQRCodeBitmap(encrypted, 500, 500);
            qrImageView.setImageBitmap(qrBitmap);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "QR Generation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void saveQrImage() {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String fileName = "QR_" + companyName + "_" + date + ".png";
        QRUtils.saveBitmapToDownloads(this, qrBitmap, fileName);
        Toast.makeText(this, "QR Code saved to Downloads", Toast.LENGTH_SHORT).show();
    }

    private boolean hasStoragePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || // Scoped storage
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQrImage();
            } else {
                Toast.makeText(this, "Permission denied to save QR code", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
