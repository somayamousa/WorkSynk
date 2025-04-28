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
    private LinearLayout homeLayout;
    private LinearLayout requestsLayout,check;
    private String email,fullname;
    private int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        userId = getIntent().getIntExtra("user_id", 0);
        // Initialize UI elements
        dateText = findViewById(R.id.dateText);
        hoursText = findViewById(R.id.hoursText);
        handler = new Handler(Looper.getMainLooper());
        homeLayout = findViewById(R.id.home);
        requestsLayout = findViewById(R.id.requests);
        check = findViewById(R.id.checkIn);
        updateDate();  // update the date.
        startUpdatingHours(); // start updating hours.
        setupNavigation();  // setup the nav bar.
    }

    private void updateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM, dd", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateText.setText(currentDate);
    }

    private void startUpdatingHours() {
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
                    // Handle thread interruption (e.g., log it)
                }
            }
        }).start();
    }

    private void setupNavigation() {
        // Home button functionality.
        homeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reopen the same MainActivity.
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Requests button functionality
        requestsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the RequestsActivity.
                Intent intent = new Intent(MainActivity.this, RequestsActivity.class); // Corrected class name
                startActivity(intent);
            }
        });
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the RequestsActivity.
                Intent intent = new Intent(MainActivity.this, attendance.class); // Corrected class name
                intent.putExtra("user_id", userId);
                intent.putExtra("email", email);
                intent.putExtra("fullname", fullname);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // You might want to stop the thread here if it's not handled elsewhere to prevent memory leaks.
    }
}