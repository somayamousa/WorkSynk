package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
    private int userId,company_id;
    private String email, fullname,role;
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private NavigationHelper navigationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role=getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getIntExtra("company_id", 0);

        // Initialize NavigationHelper and set back button functionality
        navigationHelper = new NavigationHelper(this,userId,email,fullname,role,company_id);
        navigationHelper.enableBackButton();

        // Initialize views
        initializeViews();

        // إعداد Bottom Navigation باستخدام الـ Helper
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, attendanceLayout);

        // Update date and hours
        updateDate();
        startUpdatingHours();
        fetchAttendanceData();

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

        // New views for attendance summary
        txtMonthDays = findViewById(R.id.txtMonthDays);
        txtPresentDays = findViewById(R.id.txtPresentDays);
        txtAbsentDays = findViewById(R.id.txtAbsentDays);
        txtBadRecords = findViewById(R.id.txtBadRecords);
        txtLeaves = findViewById(R.id.txtLeaves);
        txtHolidays = findViewById(R.id.txtHolidays);
        hoursProgress = findViewById(R.id.hoursProgress);
        requestQueue = Volley.newRequestQueue(this);

    }
    private void fetchAttendanceData() {
        String url = "http://192.168.1.6/worksync/get_attendance_summary.php";

        // For emulator use: "http://10.0.2.2/worksync/get_attendance_summary.php"

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
                        // Check if the response is successful
                        if (!response.getString("status").equals("success")) {
                            String errorMsg = response.optString("message", "Unknown error occurred");
                            Toast.makeText(MainActivity.this, "Server error: " + errorMsg, Toast.LENGTH_LONG).show();
                            return;
                        }

                        // Update UI with the response data
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

        // Set timeout and retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add request to queue
        requestQueue.add(request);
    }

    private void updateAttendanceUI(JSONObject response) throws JSONException {
        // Update attendance summary cards
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

        // Update progress bar
        hoursProgress.setProgress(workProgress);
    }
    private void updateDate() {
        // Update the current date in the TextView
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd,MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateText.setText(currentDate);
    }


    private void startUpdatingHours() {
        // Create a new thread to update the current time every second
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                hoursText.setText(currentTime + " Hours");
                            }
                        });
                        Thread.sleep(1000); // Sleep for 1 second
                    }
                } catch (InterruptedException e) {
                    // Handle thread interruption
                }
            }
        }).start();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Handle thread termination here if needed to avoid memory leaks
    }
}