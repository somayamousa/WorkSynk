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
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

public class attendance extends AppCompatActivity {
    private static final String TAG = "AttendanceApp";
    private static final String WORK_HOURS_API_URL = "http://10.0.2.2/worksync/attendence.php";
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
    private String email, fullname,role;
    private String macAddress;
    private int biometricAttempts = 0;
    private boolean isAuthenticationInProgress = false;

    // Network
    private RequestQueue requestQueue;
    private LinearLayout homeLayout;

    // Biometric
    private BiometricPrompt biometricPrompt;
    private KeyStore keyStore;

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
        homeLayout = findViewById(R.id.homeLayout);

        // Set current date
        currentDateText.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(new Date()));

        startButton.setOnClickListener(v -> {
            if (!isWorking && !isAuthenticationInProgress) {
                checkBiometricSupport(false);
            }
        });
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reopen the same MainActivity.
                Intent intent = new Intent(attendance.this, MainActivity.class);
                // Pass all user data to next activity
                intent.putExtra("user_id", userId);
                intent.putExtra("email", email);
                intent.putExtra("fullname", fullname);
                intent.putExtra("role", role);
                intent.putExtra("mac_address", macAddress);
                startActivity(intent);
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
        fullname = getIntent().getStringExtra("fullname");
        role=getIntent().getStringExtra("role");
        macAddress=getIntent().getStringExtra("mac_address");
        userId = getIntent().getIntExtra("user_id", 0);

        if (email == null || userId == 0) {
            showToast("Invalid user credentials");
            finish();
        }
    }
    private boolean isMacAddressRegistered() {
        // Check if we have a MAC address from the intent (from database)
        return macAddress != null && !macAddress.isEmpty();
    }

    private boolean isCurrentMacAddressValid() {
        String currentMac = getMacAddress(this);
        return currentMac.equals(macAddress);
    }

    private void verifyMacAddress() {
        if (!isMacAddressRegistered()) {
            // No MAC in database - register current one
            registerMacAddress();
        } else if (!isCurrentMacAddressValid()) {
            // MAC changed - show warning
            showMacAddressChangedDialog();
        }
    }

    private void registerMacAddress() {
        String currentMac = getMacAddress(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("user_id", userId);
            requestBody.put("mac_address", currentMac);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating MAC registration request", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                WORK_HOURS_API_URL, // Or use a different endpoint
                requestBody,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            macAddress = currentMac;
                            showToast("Device registered successfully");
                        } else {
                            showToast("Failed to register device: " + response.optString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing MAC registration response", e);
                    }
                },
                error -> {
                    Log.e(TAG, "Network error registering MAC", error);
                    showToast("Network error registering device");
                });

        configureRequest(request);
        requestQueue.add(request);
    }

    private void showMacAddressChangedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Device Changed")
                .setMessage("You're using a different device. Please contact HR to register this device.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Just close the dialog
                })
                .setCancelable(false)
                .show();
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
                .setPositiveButton("OK", (dialog, which) -> {
                    // Just close the dialog, don't allow any action
                })
                .setCancelable(false)
                .show();
    }
    private void checkBiometricSupport(boolean isVerification) {
        // First check MAC address
        if (!isMacAddressRegistered()) {
            registerMacAddress();
            return;
        }

        if (!isCurrentMacAddressValid()) {
            showMacAddressChangedDialog();
            return;
        }

        // Then check fingerprint changes
        if (hasFingerprintChanged()) {
            showFingerprintChangedDialog();
            return;
        }


        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Device supports biometrics and has enrolled fingerprints
                String title = isVerification ? "Verify Identity" : "Authenticate to Continue";
                String subtitle = isVerification ? "Authenticate to stop work" : "Authenticate to start work";
                showBiometricPrompt(title, subtitle);
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Device supports biometrics but no fingerprints enrolled
                showToast("No fingerprints enrolled");
                promptFingerprintEnrollment();
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
                    // Open fingerprint settings
                    try {
                        Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                        intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG);
                        startActivity(intent);
                    } catch (Exception e) {
                        // Fallback to general security settings if specific intent fails
                        startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    fallbackToQR();
                })
                .setCancelable(false)
                .show();
    }
    public static String getMacAddress(Context context) {
        String macAddress = getMacAddressByWifiInfo(context);
        if (!TextUtils.isEmpty(macAddress)) {
            return macAddress;
        }

        macAddress = getMacAddressByNetworkInterface();
        if (!TextUtils.isEmpty(macAddress)) {
            return macAddress;
        }

        return "02:00:00:00:00:00"; // Default MAC address for Android 6.0+
    }

    private static String getMacAddressByWifiInfo(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return null;
            }
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getMacAddress();
        } catch (Exception e) {
            return null;
        }
    }
    private static  String getMacAddressByNetworkInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) {
                    continue;
                }

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            // Handle exception
        }
        return null;
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
            // First time usage, save the current hash
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

            // Combine package signature with biometric hardware info
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
        if (!isMacAddressRegistered()) {
            registerMacAddress();
            return;
        }

        if (!isCurrentMacAddressValid()) {
            showMacAddressChangedDialog();
            return;
        }

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
        if (!isCurrentMacAddressValid()) {
            showMacAddressChangedDialog();
            return;
        }
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
            requestBody.put("mac_address", macAddress); // Add MAC to the request

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
        String decryptedData = QRUtils.decrypt(encryptedQrData);

        if (decryptedData == null || !isValidQrFormat(decryptedData) || isQrCodeExpired(decryptedData)) {
            showToast("Invalid or expired QR code");
            return;
        }

        // QR code is valid locally; proceed as authenticated
        handleAuthSuccess();
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

    private void configureRequest(JsonObjectRequest request) {
        request.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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