package com.example.worksyck; // IMPORTANT: Ensure this package name matches your project's package

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
// import android.widget.ImageView; // لم تعد هناك حاجة لاستيراد ImageView إذا لم يتم استخدامها مباشرة لأيقونة القائمة
import android.widget.TextView;
import android.widget.Toast; // For showing temporary messages

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity2 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Declare UI components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView navHeaderUsername;
    private TextView navHeaderEmail;

    // Variables to store user data received from Intent
    private int userId;
    private String email, fullname, role, company_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable EdgeToEdge display for full-screen immersive experience (optional, depends on your theme)
        // EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        // Apply window insets (padding for system bars like status bar, navigation bar)
        // This is important for EdgeToEdge to prevent content from going under system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialize UI Components
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        // 2. Retrieve User Data from the Intent that started this Activity
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fullname = intent.getStringExtra("fullname");
        role = intent.getStringExtra("role");
        userId = intent.getIntExtra("user_id", 0); // 0 is a default value if "user_id" is not found
        company_id = intent.getStringExtra("company_id");

        // 3. Set up the Toolbar as the ActionBar for the activity
        setSupportActionBar(toolbar);
        // Hide the default title provided by the ActionBar, as you have a custom TextView for the title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // قد تحتاج إلى تفعيل هذه الأسطر إذا لم تظهر الأيقونة بعد التعديل (لكن في أغلب الحالات لا حاجة لها مع ActionBarDrawerToggle)
            // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // getSupportActionBar().setHomeButtonEnabled(true);
        }

        // 4. Set up the ActionBarDrawerToggle for the Navigation Drawer
        // هذا الجزء هو المسؤول عن إظهار أيقونة الهمبرغر وربطها بالـ DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,                      // Host Activity
                drawerLayout,              // DrawerLayout object
                toolbar,                   // Toolbar to integrate with (هذا الربط هو المفتاح!)
                R.string.navigation_drawer_open,  // String resource for "open" accessibility description
                R.string.navigation_drawer_close  // String resource for "close" accessibility description
        );
        drawerLayout.addDrawerListener(toggle); // Link the toggle to the drawer
        toggle.syncState(); // Synchronize the state of the drawer indicator with the linked DrawerLayout

        // 5. Set this Activity as the listener for Navigation Drawer item clicks
        navigationView.setNavigationItemSelectedListener(this);

        // 6. تم حذف الجزء الذي كان يعثر على ImageView باسم menu_icon ويعين مستمع نقر لها
        // لأننا الآن نعتمد على ActionBarDrawerToggle لعرض الأيقونة والتحكم فيها
        /*
        ImageView menuIcon = findViewById(R.id.menu_icon);
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        */

        // 7. Populate the Navigation Drawer Header with user data
        // Get the header view from the NavigationView
        View headerView = navigationView.getHeaderView(0); // Assuming header is the first (and only) header
        navHeaderUsername = headerView.findViewById(R.id.nav_header_username);
        navHeaderEmail = headerView.findViewById(R.id.nav_header_email);

        // Set the text for username and email, with null checks
        if (fullname != null && !fullname.isEmpty()) {
            navHeaderUsername.setText(fullname);
        } else {
            navHeaderUsername.setText("Guest User"); // Default if no full name
        }
        if (email != null && !email.isEmpty()) {
            navHeaderEmail.setText(email);
        } else {
            navHeaderEmail.setText("No Email"); // Default if no email
        }

        // 8. Set up listeners for "Quick Actions" buttons on the main dashboard
        // These are separate from the Navigation Drawer items
        findViewById(R.id.quick_add_employee).setOnClickListener(v -> {
            Toast.makeText(MainActivity2.this, "Quick Add Employee clicked", Toast.LENGTH_SHORT).show();
            // Example: Start the Add Employee Activity directly from the dashboard
            // Intent addIntent = new Intent(MainActivity2.this, EmployeeAddActivity.class);
            // addIntent.putExtra("company_id", company_id);
            // startActivity(addIntent);
        });

        findViewById(R.id.quick_view_leaves).setOnClickListener(v -> {
            Toast.makeText(MainActivity2.this, "Quick View Leaves clicked", Toast.LENGTH_SHORT).show();
            // Example: Start the Leave Requests Activity directly from the dashboard
            // Intent leaveRequestIntent = new Intent(MainActivity2.this, HrLeaveRequest.class);
            // startActivity(leaveRequestIntent);
        });
    }

    /**
     * This method is called when an item in the Navigation Drawer is selected.
     * It handles navigation to different activities based on the selected menu item's ID.
     */
    @SuppressLint("NonConstantResourceId") // Suppress lint warning for R.id usage in if-else if chain
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId(); // Get the ID of the selected menu item

        // Use an if-else if structure to handle clicks for each menu item
        if (id == R.id.nav_home) {
            // If "Home" is clicked, you might just close the drawer or refresh the current dashboard view.
            Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_add_employee) {
            //   Intent addIntent = new Intent(MainActivity2.this, EmployeeAddActivity.class);
            //  addIntent.putExtra("company_id", company_id);
            // startActivity(addIntent);
        } else if (id == R.id.nav_employees) {
            Intent employeesListIntent = new Intent(MainActivity2.this, EmployeesListActivity.class);
            startActivity(employeesListIntent);
        } else if (id == R.id.nav_manage_departments) {
            Intent departmentIntent = new Intent(MainActivity2.this, DepartmentsActivity.class);
            startActivity(departmentIntent);
        } else if (id == R.id.nav_manage_designations) {
            Intent designationsIntent = new Intent(MainActivity2.this, DesignationsActivity.class);
            startActivity(designationsIntent);
        } else if (id == R.id.nav_additional_salary) {
            Intent additionalSalaryIntent = new Intent(MainActivity2.this, AdditionalSalaryActivity.class);
            additionalSalaryIntent.putExtra("company_id", company_id);
            additionalSalaryIntent.putExtra("user_id", userId);
            startActivity(additionalSalaryIntent);
        } else if (id == R.id.nav_create_salary_slip) {
            Intent salarySlipIntent = new Intent(MainActivity2.this, SalarySlip.class);
            salarySlipIntent.putExtra("company_id", company_id);
            salarySlipIntent.putExtra("user_id", userId);
            salarySlipIntent.putExtra("email", email);
            startActivity(salarySlipIntent);
        } else if (id == R.id.nav_add_holiday) {
            Intent addHolidayIntent = new Intent(MainActivity2.this, AddHoliday.class);
            startActivity(addHolidayIntent);
        } else if (id == R.id.nav_manage_holidays) {
            Intent contHolidayIntent = new Intent(MainActivity2.this, show_holidays.class);
            startActivity(contHolidayIntent);
        } else if (id == R.id.nav_leave_request) {
            Intent leaveRequestIntent = new Intent(MainActivity2.this, HrLeaveRequest.class);
            startActivity(leaveRequestIntent);
        } else if (id == R.id.nav_overtime_request) {
            Intent overtimeRequestIntent = new Intent(MainActivity2.this, HrOvertimeRequest.class);
            startActivity(overtimeRequestIntent);
        } else if (id == R.id.nav_scan_qr) {
            Intent qrIntent = new Intent(MainActivity2.this, QrDisplayActivity.class);
            startActivity(qrIntent);
        } else if (id == R.id.nav_gps_tracking) {
            Intent gpsIntent = new Intent(MainActivity2.this, MapsActivity.class);
            gpsIntent.putExtra("user_id", userId);
            gpsIntent.putExtra("email", email);
            gpsIntent.putExtra("fullname", fullname);
            gpsIntent.putExtra("role", role);
            gpsIntent.putExtra("company_id", company_id);
            // Note: "company_code" is passed with the same value as "company_id". Review if this is intentional.
            gpsIntent.putExtra("company_code", company_id);
            startActivity(gpsIntent);
        } else if (id == R.id.nav_work_policy) {
            Intent workPolicyIntent = new Intent(MainActivity2.this, workPolicySettingsActivity.class);
            workPolicyIntent.putExtra("company_id", company_id);
            workPolicyIntent.putExtra("user_id", userId);
            startActivity(workPolicyIntent);
        } else if (id == R.id.nav_logout) {
            // Handle your logout logic here
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            // Example: Navigate to LoginActivity and clear back stack
            // Intent logoutIntent = new Intent(MainActivity2.this, LoginActivity.class);
            // logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(logoutIntent);
            // finish(); // Finish current activity
        }

        // Close the navigation drawer after an item is selected
        drawerLayout.closeDrawer(GravityCompat.START);
        return true; // Indicate that the item selection has been handled
    }

    /**
     * Override the default back button behavior.
     * If the drawer is open, close it first. Otherwise, perform default back action.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START); // Close the drawer
        } else {
            super.onBackPressed(); // Perform default back button action
        }
    }
}