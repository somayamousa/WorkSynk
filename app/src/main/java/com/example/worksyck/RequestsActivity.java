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


// Initialize NavigationHelper and set back button functionality
        navigationHelper = new NavigationHelper(this,userId,email,fullname,role,company_id);
        navigationHelper.enableBackButton();

        // Initialize views
        initializeViews();

        // إعداد Bottom Navigation باستخدام الـ Helper
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout);



        // تفعيل النقر على CardView للانتقال إلى الأنشطة المناسبة
        leaveRequestCardView.setOnClickListener(v -> navigateToActivity(LeaveRequest.class));
        overtimeRequestCardView.setOnClickListener(v -> navigateToActivity(OvertimeRequest.class));
    }

    private void initializeViews() {
        // ربط العناصر في XML بالكود
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);
        leaveRequestCardView = findViewById(R.id.leaveRequestCardView);
        overtimeRequestCardView = findViewById(R.id.overtimeRequestCardView);

        // تفعيل زر الرجوع باستخدام الـ Helper
        navigationHelper.setBackButtonListener(backButton);  // استدعاء زر الرجوع
    }

    private void navigateToActivity(Class<?> activityClass) {
        // التنقل إلى الأنشطة المناسبة
        Intent intent = new Intent(RequestsActivity.this, activityClass);
        startActivity(intent);
    }
}
