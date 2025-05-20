package com.example.worksyck;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.crypto.KeyGenerator;

public class attendance extends AppCompatActivity {
    private static final String TAG = "AttendanceApp";
    private static final String ATTENDANCE_API_URL = "http://192.168.1.13/worksync/attendence.php";
    private static final String DEVICE_VERIFICATION_URL = "http://192.168.1.13/worksync/device_verification.php";
    private static final int MAX_BIOMETRIC_ATTEMPTS = 3;
    private static final int REQUEST_TIMEOUT_MS = 15000;
    private static final String KEYSTORE_ALIAS = "biometric_encryption_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String ATTENDANCE_PREFS = "AttendancePrefs";

    // UI Components
    private Button startButton, stopButton;
    private TextView currentDateText, checkInTimeText, checkOutTimeText;
    private LinearLayout homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout;

    // Authentication state
    private boolean isWorking = false;
    private long startTime = 0;
    private int userId;
    private String email, fullname, role;
    private int biometricAttempts = 0;
    private boolean isAuthenticationInProgress = false;

    // Network
    private RequestQueue requestQueue;

    // Biometric
    private BiometricPrompt biometricPrompt;
    private KeyStore keyStore;
    private NavigationHelper navigationHelper;

    // QR Code
    private final ActivityResultLauncher<Intent> qrLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::handleQrScanResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        initializeViews();
        setupUserData();
        initializeNetworkQueue();
        initializeBiometricComponents();
        loadSavedState();
        updateUI();

        navigationHelper = new NavigationHelper(this);
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout);
    }

    private void initializeViews() {
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        currentDateText = findViewById(R.id.currentDateText);
        checkInTimeText = findViewById(R.id.checkInTime);
        checkOutTimeText = findViewById(R.id.checkOutTime);
        homeLayout = findViewById(R.id.homeLayout);
        requestsLayout = findViewById(R.id.requestsLayout);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);

        // Set current date
        currentDateText.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        homeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(attendance.this, MainActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("email", email);
            intent.putExtra("fullname", fullname);
            intent.putExtra("role", role);
            startActivity(intent);
        });

        startButton.setOnClickListener(v -> {
            if (!isWorking && !isAuthenticationInProgress) {
                verifyDeviceAndProceed(false, () -> checkBiometricSupport(false));
            }
        });

        stopButton.setOnClickListener(v -> {
            if (isWorking && !isAuthenticationInProgress) {
                verifyDeviceAndProceed(true, () -> checkBiometricSupport(true));
            }
        });
    }

    private void setupUserData() {
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);

        if (email == null || userId == 0) {
            showToast("Invalid user credentials");
            finish();
        }
    }

    private void initializeNetworkQueue() {
        requestQueue = Volley.newRequestQueue(this);
    }

    private void initializeBiometricComponents() {
        try {
            keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);

            BiometricManager biometricManager = BiometricManager.from(this);
            int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                generateSecretKey();
            } else {
                Log.w(TAG, "Biometrics not enrolled, skipping key generation");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize biometric components", e);
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                handleBiometricSuccess(result);
            }

            @Override
            public void onAuthenticationFailed() {
                handleBiometricFailure();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                handleBiometricError(errorCode, errString);
            }
        });
    }

    private String getAndroidId() {
        try {
            String androidId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            return !TextUtils.isEmpty(androidId) ? androidId : "unknown";
        } catch (Exception e) {
            Log.e(TAG, "Error getting Android ID", e);
            return "unknown";
        }
    }

    private void generateSecretKey() throws Exception {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(true)
                    .build());

            keyGenerator.generateKey();
        }
    }

    private void showFingerprintChangedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Fingerprint Changed")
                .setMessage("Your device's fingerprints have been modified. Please contact HR before continuing.")
                .setPositiveButton("OK", (dialog, which) -> {})
                .setCancelable(false)
                .show();
    }

    private void verifyDeviceAndProceed(boolean isVerification, Runnable onSuccess) {
        String androidId = getAndroidId();

        showToast("Verifying device...");

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", userId);
            requestBody.put("email", email);
            requestBody.put("android_id", androidId);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating device verification JSON", e);
            showToast("Device verification failed");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                DEVICE_VERIFICATION_URL,
                requestBody,
                response -> {
                    try {
                        String status = response.getString("status");
                        if ("success".equals(status)) {
                            onSuccess.run();
                        } else {
                            String message = response.optString("message", "Device verification failed");
                            showToast(message);
                            Log.e(TAG, "Device verification failed: " + message);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing device verification response", e);
                        showToast("Device verification error");
                    }
                },
                error -> {
                    Log.e(TAG, "Network error during device verification", error);
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                        try {
                            String responseBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            Log.e(TAG, "Response: " + responseBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error response", e);
                        }
                    }
                    showToast("Verification Failed");
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void checkBiometricSupport(boolean isVerification) {
        if (hasFingerprintChanged()) {
            showFingerprintChangedDialog();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                String title = isVerification ? "Verify Identity" : "Authenticate to Continue";
                String subtitle = isVerification ? "Authenticate to stop work" : "Authenticate to start work";
                showBiometricPrompt(title, subtitle);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                showToast("No fingerprints enrolled");
                promptFingerprintEnrollment();
                title = isVerification ? "Verify Identity" : "Authenticate to Continue";
                subtitle = isVerification ? "Authenticate to stop work" : "Authenticate to start work";
                showBiometricPrompt(title, subtitle);
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showToast("Biometrics currently unavailable");
                fallbackToQR();
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                showToast("Device doesn't support biometrics");
                fallbackToQR();
                break;

            default:
                showToast("Unknown biometric error");
                fallbackToQR();
        }
    }

    private void promptFingerprintEnrollment() {
        new AlertDialog.Builder(this)
                .setTitle("Fingerprint Setup Required")
                .setMessage("You need to register at least one fingerprint to use this feature. Would you like to go to settings now?")
                .setPositiveButton("Go to Settings", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                        intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG);
                        startActivity(intent);
                    } catch (Exception e) {
                        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> fallbackToQR())
                .setCancelable(false)
                .show();
    }

    private void showBiometricPrompt(String title, String subtitle) {
        if (isAuthenticationInProgress) return;
        isAuthenticationInProgress = true;

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText("Use QR Code")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        try {
            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            Log.e(TAG, "Biometric authentication error", e);
            isAuthenticationInProgress = false;
            fallbackToQR();
        }
    }

    private boolean hasFingerprintChanged() {
        SharedPreferences prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE);
        String currentFingerprintHash = generateFingerprintHash();
        String savedFingerprintHash = prefs.getString("fingerprint_hash", null);

        if (savedFingerprintHash == null) {
            prefs.edit().putString("fingerprint_hash", currentFingerprintHash).apply();
            return false;
        }

        return !savedFingerprintHash.equals(currentFingerprintHash);
    }

    private String generateFingerprintHash() {
        try {
            PackageManager pm = getPackageManager();
            String packageName = getPackageName();
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            String combined = info.signatures[0].toCharsString() +
                    Build.MANUFACTURER + Build.BRAND + Build.MODEL;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error generating fingerprint hash", e);
            return "error";
        }
    }

    private void handleBiometricSuccess(BiometricPrompt.AuthenticationResult result) {
        if (hasFingerprintChanged()) {
            showFingerprintChangedDialog();
            return;
        }

        isAuthenticationInProgress = false;
        biometricAttempts = 0;
        handleAuthSuccess();
    }

    private void handleBiometricFailure() {
        isAuthenticationInProgress = false;
        biometricAttempts++;

        if (biometricAttempts >= MAX_BIOMETRIC_ATTEMPTS) {
            showToast("Too many attempts. Using QR code.");
            fallbackToQR();
        } else {
            showToast("Try again. Attempt " + biometricAttempts + "/" + MAX_BIOMETRIC_ATTEMPTS);
        }
    }

    private void handleBiometricError(int errorCode, CharSequence errString) {
        isAuthenticationInProgress = false;
        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
            showToast("Biometric error: " + errString);
        }
    }

    private void handleAuthSuccess() {
        if (isWorking) {
            stopWork();
        } else {
            startWork();
        }
    }

    private void startWork() {
        startTime = System.currentTimeMillis();
        isWorking = true;
        saveState();
        sendAttendanceRecord("start");
        updateUI();
        showToast("Work started at " + formatTime(startTime));
        checkInTimeText.setText(formatTime(startTime));
    }

    private void stopWork() {
        long endTime = System.currentTimeMillis();
        isWorking = false;
        saveState();
        sendAttendanceRecord("end");
        updateUI();
        showToast("Work ended at " + formatTime(endTime));
        checkOutTimeText.setText(formatTime(endTime));
    }

    private void sendAttendanceRecord(String action) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("user_id", userId);
            requestBody.put("date", date);
            requestBody.put("action", action);
            requestBody.put("time", time);
            requestBody.put("android_id", getAndroidId());
        } catch (JSONException e) {
            Log.e(TAG, "Error creating attendance JSON", e);
            showToast("Error creating attendance record");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ATTENDANCE_API_URL,
                requestBody,
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) {
                            Log.e(TAG, "Error saving attendance: " + response.optString("message"));
                            showToast("Error saving attendance: " + response.optString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing attendance response", e);
                        showToast("Error processing attendance response");
                    }
                },
                error -> {
                    Log.e(TAG, "Network error saving attendance", error);
                    showToast("Network error saving attendance");
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void fallbackToQR() {
        isAuthenticationInProgress = false;
        showToast("Using QR code authentication");

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan Admin QR Code");
        integrator.setOrientationLocked(true);
        integrator.setBeepEnabled(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setCameraId(0);
        integrator.setCaptureActivity(QrCaptureActivity.class);
        qrLauncher.launch(integrator.createScanIntent());
    }

    private void handleQrScanResult(androidx.activity.result.ActivityResult result) {
        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(
                    result.getResultCode(), result.getData());

            if (scanResult != null && scanResult.getContents() != null) {
                verifyAdminQRCode(scanResult.getContents());
            } else {
                showToast("QR scan cancelled");
            }
        }
    }

    private void verifyAdminQRCode(String encryptedQrData) {
        String decryptedData = QRUtils.decrypt(encryptedQrData);

        if (decryptedData == null || !isValidQrFormat(decryptedData) || isQrCodeExpired(decryptedData)) {
            showToast("Invalid or expired QR code");
            return;
        }

        handleAuthSuccess();
    }

    private boolean isValidQrFormat(String decryptedData) {
        String[] parts = decryptedData.split("_");
        if (parts.length < 3) return false;
        if (!"WorkSync".equals(parts[0])) return false;
        try {
            Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private boolean isQrCodeExpired(String decryptedData) {
        try {
            String[] parts = decryptedData.split("_");
            long qrTimestamp = Long.parseLong(parts[1]);
            long currentTime = System.currentTimeMillis();
            long twentyFourHours = 24 * 60 * 60 * 1000;
            return (currentTime - qrTimestamp) > twentyFourHours;
        } catch (Exception e) {
            return true;
        }
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences(ATTENDANCE_PREFS, MODE_PRIVATE);
        isWorking = prefs.getBoolean("isWorking", false);
        startTime = prefs.getLong("startTime", 0);
    }

    private void saveState() {
        SharedPreferences prefs = getSharedPreferences(ATTENDANCE_PREFS, MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isWorking", isWorking)
                .putLong("startTime", startTime)
                .apply();
    }

    private void updateUI() {
        runOnUiThread(() -> {
            startButton.setEnabled(!isWorking);
            stopButton.setEnabled(isWorking);

            if (isWorking) {
                checkInTimeText.setText("Working - started at " + formatTime(startTime));
            } else {
                checkInTimeText.setText("Ready to check in");
                checkOutTimeText.setText("");
            }
        });
    }

    private String formatTime(long time) {
        return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(time));
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    public static class QrCaptureActivity extends com.journeyapps.barcodescanner.CaptureActivity {
        // Custom QR scanner activity
    }
}