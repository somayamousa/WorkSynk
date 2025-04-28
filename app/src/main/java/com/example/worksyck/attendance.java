package com.example.worksyck;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class attendance extends AppCompatActivity {
    private static final String TAG = "AttendanceApp";
    private static final String BIOMETRIC_API_URL = "http://192.168.1.113/worksync/biometric_api.php";
    private static final String WORK_HOURS_API_URL = "http://192.168.1.113/worksync/attendence.php";
    private static final int MAX_BIOMETRIC_ATTEMPTS = 3;
    private static final int REQUEST_TIMEOUT_MS = 15000;
    private static final String KEYSTORE_ALIAS = "biometric_encryption_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    // UI Components
    private Button startButton, stopButton;
    private TextView  currentDateText, selectedAddressText;
    private TextView checkInTimeText, checkOutTimeText;

    // Authentication state
    private boolean isWorking = false;
    private long startTime = 0;
    private int userId;
    private String email, userName;
    private int biometricAttempts = 0;
    private boolean isAuthenticationInProgress = false;

    // Network
    private RequestQueue requestQueue;

    // Biometric
    private BiometricPrompt biometricPrompt;
    private KeyStore keyStore;
    private Cipher cipher;

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
    }

    private void initializeViews() {
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        currentDateText = findViewById(R.id.currentDateText);
        selectedAddressText = findViewById(R.id.selectedAddressText);
        checkInTimeText = findViewById(R.id.checkInTime);
        checkOutTimeText = findViewById(R.id.checkOutTime);

        // Set current date
        currentDateText.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        startButton.setOnClickListener(v -> {
            if (!isWorking && !isAuthenticationInProgress) {
                checkBiometricSupport(false);
            }
        });

        stopButton.setOnClickListener(v -> {
            if (isWorking && !isAuthenticationInProgress) {
                checkBiometricSupport(true);
            }
        });
    }

    private void setupUserData() {
        email = getIntent().getStringExtra("email");
        userId = getIntent().getIntExtra("user_id", 0);
        userName = getIntent().getStringExtra("user_name");

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
            generateSecretKey();
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

    private void checkBiometricSupport(boolean isVerification) {
        // First check device hardware capability
        switch (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                checkServerBiometricStatus(isVerification);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                showToast("Device doesn't support biometrics");
                fallbackToQR();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                showToast("Biometrics currently unavailable");
                fallbackToQR();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                showToast("No biometrics enrolled in device settings");
                fallbackToQR();
                break;
            default:
                Log.d("log","unkown error");
                showToast("Unknown biometric error");
                fallbackToQR();
        }
    }


    private void handleBiometricSupportResponse(JSONObject response, boolean isVerification) {
        try {
            if (response.getString("status").equals("success")) {
                boolean hasBiometric = response.getBoolean("has_biometric");

                if (isVerification && hasBiometric) {
                    showBiometricPrompt("Verify Identity", "Authenticate to continue");
                } else if (!isVerification && !hasBiometric) {
                    showBiometricEnrollmentPrompt();
                } else {
                    fallbackToQR();
                }
            } else {
                showToast("Server error checking biometric status");
                fallbackToQR();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing biometric support response", e);
            fallbackToQR();
        }
    }

    private void showBiometricPrompt(String title, String subtitle) {
        if (isAuthenticationInProgress) return;
        isAuthenticationInProgress = true;

        try {
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEYSTORE_ALIAS, null);
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText("Use QR Code")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .build();

            biometricPrompt.authenticate(promptInfo, new BiometricPrompt.CryptoObject(cipher));
        } catch (Exception e) {
            Log.e(TAG, "Biometric authentication error", e);
            isAuthenticationInProgress = false;
            fallbackToQR();
        }
    }

    private void showBiometricEnrollmentPrompt() {
        if (isAuthenticationInProgress) return;
        isAuthenticationInProgress = true;

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Register Biometric")
                .setSubtitle("Scan your fingerprint to register")
                .setDescription("This will allow faster authentication in the future")
                .setNegativeButtonText("Use QR Code Instead")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        try {
            biometricPrompt.authenticate(promptInfo);
        } catch (Exception e) {
            Log.e(TAG, "Biometric enrollment error", e);
            isAuthenticationInProgress = false;
            fallbackToQR();
        }
    }

    private void handleBiometricSuccess(BiometricPrompt.AuthenticationResult result) {
        isAuthenticationInProgress = false;
        biometricAttempts = 0;
        processBiometricResult(result);
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

    private void processBiometricResult(BiometricPrompt.AuthenticationResult result) {
        try {
            // Generate secure biometric template using Android's crypto object
            byte[] encrypted = result.getCryptoObject().getCipher().doFinal(
                    generateBiometricTemplate(userId).getBytes(StandardCharsets.UTF_8));

            String encryptedBiometricData = Base64.encodeToString(encrypted, Base64.DEFAULT);
            String action = isWorking ? "verify_biometric" : "register_biometric";

            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("email", email);
                requestBody.put("action", action);
                requestBody.put("biometric_data", encryptedBiometricData);
                requestBody.put("iv", Base64.encodeToString(
                        result.getCryptoObject().getCipher().getIV(), Base64.DEFAULT));
            } catch (JSONException e) {
                Log.e(TAG, "Error creating biometric request JSON", e);
                fallbackToQR();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BIOMETRIC_API_URL,
                    requestBody,
                    response -> handleBiometricProcessingResponse(response, action),
                    error -> {
                        Log.e(TAG, "Network error processing biometric", error);
                        fallbackToQR();
                    });

            configureRequest(request);
            requestQueue.add(request);
        } catch (Exception e) {
            Log.e(TAG, "Error processing biometric result", e);
            fallbackToQR();
        }
    }
    private String generateBiometricTemplate(int userId) {
        try {
            // 1. Gather unique device and user identifiers
            String deviceId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String userUniqueId = String.valueOf(userId);

            // 2. Get current timestamp with nanosecond precision
            long timestamp = System.nanoTime();

            // 3. Generate cryptographically secure random data
            SecureRandom secureRandom = new SecureRandom();
            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);
            String randomData = Base64.encodeToString(randomBytes, Base64.NO_WRAP);

            // 4. Combine all components with proper delimiters
            String rawTemplate = String.format(Locale.US,
                    "%s|%s|%d|%s|%f",
                    deviceId,
                    userUniqueId,
                    timestamp,
                    randomData,
                    Math.random()
            );

            // 5. Create SHA-256 hash of the combined data
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(rawTemplate.getBytes(StandardCharsets.UTF_8));

            // 6. Add additional salt and hash again
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            digest.update(salt);

            // 7. Convert to Base64 for storage/transmission
            byte[] hash = digest.digest();
            String template = Base64.encodeToString(hash, Base64.NO_WRAP);

            // 8. Store the salt securely for later verification
            SharedPreferences prefs = getSharedPreferences("BiometricPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putString("biometric_salt_" + userId,
                            Base64.encodeToString(salt, Base64.NO_WRAP))
                    .apply();

            return template;

        } catch (Exception e) {
            Log.e(TAG, "Error generating biometric template", e);

            // Fallback mechanism with limited security
            SecureRandom fallbackRandom = new SecureRandom();
            long fallbackTimestamp = System.currentTimeMillis();
            int fallbackRandomValue = fallbackRandom.nextInt(1000000);

            return String.format(Locale.US,
                    "fallback_%d_%d_%d",
                    userId,
                    fallbackTimestamp,
                    fallbackRandomValue
            );
        }
    }

    private void handleBiometricProcessingResponse(JSONObject response, String action) {
        try {
            if (response.getString("status").equals("success")) {
                if (action.equals("verify_biometric")) {
                    if (response.getBoolean("authenticated")) {
                        handleAuthSuccess();
                    } else {
                        showToast("Biometric verification failed");
                        fallbackToQR();
                    }
                } else {
                    showToast("Biometric registered successfully");
                    handleAuthSuccess();
                }
            } else {
                showToast("Server error: " + response.optString("message"));
                fallbackToQR();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing biometric response", e);
            fallbackToQR();
        }
    }





    private String decryptQRData(String encryptedData) {
        try {
            // First try Java-style decryption
            return QRUtils.decrypt(encryptedData);
        } catch (Exception e) {
            Log.e(TAG, "QR decryption failed", e);
            return null;
        }
    }

    private void handleQrVerificationResponse(JSONObject response) {
        try {
            if (response.getString("status").equals("success") &&
                    response.getBoolean("authenticated")) {
                handleAuthSuccess();
            } else {
                showToast("Invalid QR code: " + response.optString("message"));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing QR response", e);
            showToast("Error processing QR response");
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
        sendWorkRecord("start");
        updateUI();
        showToast("Work started at " + formatTime(startTime));

        // Update check-in time display
        checkInTimeText.setText(formatTime(startTime));
    }

    private void stopWork() {
        long endTime = System.currentTimeMillis();
        isWorking = false;
        saveState();
        sendWorkRecord("end");
        updateUI();
        showToast("Work ended at " + formatTime(endTime));

        // Update check-out time display
        checkOutTimeText.setText(formatTime(endTime));
    }

    private void sendWorkRecord(String action) {
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startTime));
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
                new Date(action.equals("start") ? startTime : System.currentTimeMillis()));

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("user_id", userId);
            requestBody.put("date", date);
            requestBody.put("action", action);
            requestBody.put("time", time);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating work record JSON", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                WORK_HOURS_API_URL,
                requestBody,
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) {
                            Log.e(TAG, "Error saving work record: " + response.optString("message"));
                            showToast("Error saving attendance record");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing work record response", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Network error saving work record", error);
                    showToast("Network error saving record");
                });

        configureRequest(request);
        requestQueue.add(request);
    }
    private void fallbackToQR() {
        isAuthenticationInProgress = false;
        showToast("Using QR code authentication");

        // Launch QR scanner
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
        // Step 1: Decrypt the QR code data
        String decryptedData = QRUtils.decrypt(encryptedQrData);

        if (decryptedData == null) {
            showToast("Invalid QR code format");
            return;
        }

        // Step 2: Verify the QR code format
        if (!isValidQrFormat(decryptedData)) {
            showToast("Invalid QR code content");
            return;
        }

        // Step 3: Check expiration (QR codes are valid for 24 hours)
        if (isQrCodeExpired(decryptedData)) {
            showToast("QR code has expired");
            return;
        }

        // Step 4: Verify with server
        verifyQRWithServer(encryptedQrData, decryptedData);
    }

    private boolean isValidQrFormat(String decryptedData) {
        // Expected format: "WorkSync_<timestamp>_<random_hash>"
        String[] parts = decryptedData.split("_");

        // Check basic structure
        if (parts.length < 3) return false;

        // Check prefix
        if (!"WorkSync".equals(parts[0])) return false;

        // Check timestamp is numeric
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
            return true; // If we can't parse, consider it expired
        }
    }

    private void verifyQRWithServer(String encryptedQrData, String decryptedData) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("action", "verify_qr");
            requestBody.put("qr_data", encryptedQrData);
            requestBody.put("decrypted_data", decryptedData);
            requestBody.put("user_id", userId);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating QR verification request", e);
            showToast("Error processing QR code");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                BIOMETRIC_API_URL,
                requestBody,
                response -> {
                    try {
                        if (response.getString("status").equals("success") &&
                                response.getBoolean("authenticated")) {

                            if (isWorking) {
                                stopWork();
                            } else {
                                startWork();
                            }
                        } else {
                            showToast("QR verification failed: " +
                                    response.optString("message", ""));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing QR response", e);
                        showToast("Error processing QR response");
                    }
                },
                error -> {
                    Log.e(TAG, "Network error verifying QR", error);
                    showToast("Network error verifying QR");
                });

        configureRequest(request);
        requestQueue.add(request);
    }
    private void configureRequest(JsonObjectRequest request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }
    // Replace all direct JsonObjectRequest creations with this method
    private JsonObjectRequest createAuthenticatedRequest(int method, String url, JSONObject jsonRequest,
                                                         Response.Listener<JSONObject> listener,
                                                         Response.ErrorListener errorListener) {
        JsonObjectRequest request = new JsonObjectRequest(method, url, jsonRequest, listener, errorListener) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                // Add auth token if available
                SharedPreferences prefs = getSharedPreferences("AuthPrefs", MODE_PRIVATE);
                String authToken = prefs.getString("auth_token", null);
                if (authToken != null) {
                    headers.put("Authorization", "Bearer " + authToken);
                }

                // Add device info headers
                headers.put("Device-ID", Settings.Secure.getString(
                        getContentResolver(), Settings.Secure.ANDROID_ID));

                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    headers.put("App-Version", pInfo.versionName);
                    headers.put("App-Version-Code", String.valueOf(pInfo.versionCode));
                } catch (PackageManager.NameNotFoundException e) {
                    headers.put("App-Version", "unknown");
                }

                headers.put("Platform", "Android");
                headers.put("OS-Version", Build.VERSION.RELEASE);

                return headers;
            }
        };

        // Apply the retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        return request;
    }

    // Example usage in checkServerBiometricStatus():
    private void checkServerBiometricStatus(boolean isVerification) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("action", "check_biometric_support");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating request JSON", e);
            fallbackToQR();
            return;
        }

        // Using the new method instead of creating JsonObjectRequest directly
        JsonObjectRequest request = createAuthenticatedRequest(
                Request.Method.POST,
                BIOMETRIC_API_URL,
                requestBody,
                response -> handleBiometricSupportResponse(response, isVerification),
                error -> {
                    Log.e(TAG, "Network error checking biometric support", error);
                    fallbackToQR();
                });

        requestQueue.add(request);
    }

    private void loadSavedState() {
        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
        isWorking = prefs.getBoolean("isWorking", false);
        startTime = prefs.getLong("startTime", 0);
    }

    private void saveState() {
        SharedPreferences prefs = getSharedPreferences("AttendancePrefs", MODE_PRIVATE);
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
        // Custom QR scanner activity with additional configuration
    }

}