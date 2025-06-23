package com.example.worksyck;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class NavigationHelper {
    private static int userId, company_id;
    private static String email, fullname, role;
    private AppCompatActivity activity;

    public NavigationHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public NavigationHelper(AppCompatActivity activity, int userId, String email, String fullname, String role, int company_id) {
        this.activity = activity;
        NavigationHelper.userId = userId;
        NavigationHelper.email = email;
        NavigationHelper.fullname = fullname;
        NavigationHelper.role = role;
        NavigationHelper.company_id = company_id;
    }

    public void enableBackButton() {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public void setBottomNavigationListeners(LinearLayout[] bottomNavItems, LinearLayout homeLayout, LinearLayout requestsLayout, LinearLayout checkInLayout, LinearLayout salaryLayout, LinearLayout attendanceLayout) {
        for (LinearLayout item : bottomNavItems) {
            item.setOnClickListener(v -> {
                navigateToActivity(getActivityClass(item, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout));
                updateSelection(item, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);
            });
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        if (activityClass != null && activityClass != activity.getClass()) { // Prevent restarting the same activity
            Intent intent = new Intent(activity, activityClass);
            intent.putExtra("user_id", userId);
            intent.putExtra("email", email);
            intent.putExtra("fullname", fullname);
            intent.putExtra("company_id", company_id);
            intent.putExtra("role", role);
            activity.startActivity(intent);
        }
    }

    private Class<?> getActivityClass(LinearLayout item, LinearLayout homeLayout, LinearLayout requestsLayout, LinearLayout checkInLayout, LinearLayout salaryLayout, LinearLayout attendanceLayout) {
        if (item == homeLayout) return MainActivity.class;
        if (item == requestsLayout) return RequestsActivity.class;
        if (item == checkInLayout) return attendance.class; // Ensure this class exists
      //  if (item == salaryLayout) return SalaryActivity.class;
        if (item == attendanceLayout) return ShowAttendanceRecord.class; // Adjust if AttendanceActivity has a different name
        return null;
    }

    private void updateSelection(LinearLayout selectedLayout, LinearLayout homeLayout, LinearLayout requestsLayout, LinearLayout checkInLayout, LinearLayout salaryLayout, LinearLayout attendanceLayout) {
        homeLayout.setSelected(false);
        requestsLayout.setSelected(false);
        checkInLayout.setSelected(false);
        salaryLayout.setSelected(false);
        attendanceLayout.setSelected(false);
        selectedLayout.setSelected(true);
    }

    public void setBackButtonListener(ImageView backButton) {
        backButton.setOnClickListener(v -> activity.onBackPressed());
    }
}