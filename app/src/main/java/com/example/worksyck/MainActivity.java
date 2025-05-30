package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout);

        // Update date and hours
        updateDate();
        startUpdatingHours();

    }

    private void initializeViews() {
        // Link UI elements
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);

        dateText = findViewById(R.id.dateText);
        hoursText = findViewById(R.id.hoursText);
        handler = new Handler(Looper.getMainLooper());
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