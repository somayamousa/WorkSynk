package com.example.worksyck;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class RequestsActivity extends AppCompatActivity {

    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private CardView leaveRequestCardView, overtimeRequestCardView;
    private ImageView backButton;
    private NavigationHelper navigationHelper;  // إنشاء كائن من الـ Helper

    private int userId,company_id;
    private String email, fullname,role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);


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

        // Set up Bottom Navigation
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);

        // Set up CardView click listeners
        leaveRequestCardView.setOnClickListener(v -> navigateToActivity(LeaveRequest.class));
        overtimeRequestCardView.setOnClickListener(v -> navigateToActivity(OvertimeRequest.class));


    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);
        leaveRequestCardView = findViewById(R.id.leaveRequestCardView);
        overtimeRequestCardView = findViewById(R.id.overtimeRequestCardView);

        navigationHelper.setBackButtonListener(backButton);
    }

    private void navigateToActivity(Class<?> activityClass) {
        // التنقل إلى الأنشطة المناسبة
        Intent intent = new Intent(RequestsActivity.this, activityClass);
        startActivity(intent);
    }
}