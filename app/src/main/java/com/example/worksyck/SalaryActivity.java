package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class SalaryActivity extends AppCompatActivity {

    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private ImageView backButton;
    private NavigationHelper navigationHelper;
    private int userId, company_id;
    private String email, fullname, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary); // Create this layout file

        // Retrieve intent extras
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getIntExtra("company_id", 0);

        // Initialize NavigationHelper
        navigationHelper = new NavigationHelper(this, userId, email, fullname, role, company_id);
        navigationHelper.enableBackButton();

        // Initialize views
        initializeViews();

        // Set up Bottom Navigation using NavigationHelper
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);
    }

    private void initializeViews() {
        // Bind XML elements
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);

        // Set back button listener using NavigationHelper
        navigationHelper.setBackButtonListener(backButton);
    }
}