package com.example.worksyck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView txtMonthDays, txtPresentDays, txtAbsentDays, txtBadRecords, txtLeaves, txtHolidays;
    private ProgressBar hoursProgress;
    private RequestQueue requestQueue;
    private TextView dateText;
    private TextView hoursText;
    private Handler handler;
    private int userId, company_id;
    private String email, fullname, role;
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private NavigationHelper navigationHelper;
    private Button logoutBtn;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getIntExtra("company_id", 0);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Initialize NavigationHelper
        navigationHelper = new NavigationHelper(this, userId, email, fullname, role, company_id);
        navigationHelper.enableBackButton();

        // Initialize views
        initializeViews();

        // Set up Bottom Navigation
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);

        // Update date and hours
        updateDate();
        startUpdatingHours();
        fetchAttendanceData();

        logoutBtn.setOnClickListener(v -> logout());
    }

    private void initializeViews() {
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);
        dateText = findViewById(R.id.dateText);
        hoursText = findViewById(R.id.hoursText);
        handler = new Handler(Looper.getMainLooper());
        txtMonthDays = findViewById(R.id.txtMonthDays);
        txtPresentDays = findViewById(R.id.txtPresentDays);
        txtAbsentDays = findViewById(R.id.txtAbsentDays);
        txtBadRecords = findViewById(R.id.txtBadRecords);
        txtLeaves = findViewById(R.id.txtLeaves);
        txtHolidays = findViewById(R.id.txtHolidays);
        hoursProgress = findViewById(R.id.hoursProgress);
        requestQueue = Volley.newRequestQueue(this);
        logoutBtn = findViewById(R.id.buttonLogout); // Make sure you have this button in your layout
    }

    private void fetchAttendanceData() {
        String url = "http://192.168.1.6/worksync/get_attendance_summary.php";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", userId);
            requestBody.put("company_id", company_id);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) {
                            String errorMsg = response.optString("message", "Unknown error occurred");
                            Toast.makeText(MainActivity.this, "Server error: " + errorMsg, Toast.LENGTH_LONG).show();
                            return;
                        }

                        updateAttendanceUI(response);

                    } catch (JSONException e) {
                        Log.e("MainActivity", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Error parsing server response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Network error";

                    if (error.networkResponse != null) {
                        errorMsg += " (Status: " + error.networkResponse.statusCode + ")";
                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            errorMsg += "\nResponse: " + responseBody;
                            Log.e("MainActivity", "Error response: " + responseBody);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else if (error.getMessage() != null) {
                        errorMsg += ": " + error.getMessage();
                    }

                    Log.e("MainActivity", errorMsg);
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void updateAttendanceUI(JSONObject response) throws JSONException {
        int monthDays = response.getInt("month_days");
        int presentDays = response.getInt("present_days");
        int absentDays = response.getInt("absent_days");
        int badRecords = response.getInt("bad_records");
        int leaves = response.getInt("leaves");
        int holidays = response.getInt("holidays");
        int workProgress = response.getInt("work_progress");

        txtMonthDays.setText(String.format(Locale.getDefault(),
                "Month Days\n%d", monthDays));
        txtPresentDays.setText(String.format(Locale.getDefault(),
                "Present Days\n%d", presentDays));
        txtAbsentDays.setText(String.format(Locale.getDefault(),
                "Absent Days\n%d", absentDays));
        txtBadRecords.setText(String.format(Locale.getDefault(),
                "Bad Records\n%d", badRecords));
        txtLeaves.setText(String.format(Locale.getDefault(),
                "Leaves\n%d", leaves));
        txtHolidays.setText(String.format(Locale.getDefault(),
                "Holidays\n%d", holidays));

        hoursProgress.setProgress(workProgress);
    }

    private void updateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd,MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateText.setText(currentDate);
    }

    private void startUpdatingHours() {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    handler.post(() -> hoursText.setText(currentTime + " Hours"));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                // Thread interrupted
            }
        }).start();
    }

    private void logout() {
        new Thread(() -> {
            try {
                URL url = new URL("http://192.168.1.6/worksync/logout.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int responseCode = conn.getResponseCode();
                conn.disconnect();

                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        prefs.edit().clear().apply();
                        Toast.makeText(MainActivity.this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "فشل تسجيل الخروج. الكود: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "خطأ في الاتصال بالخادم: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
    }
}