package com.example.worksyck;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity2 extends AppCompatActivity {
    private int userId;
    private Button logoutBtn;
    private String email, fullname, role, company_id;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SharedPreferences
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Initialize buttons
        Button btnQr = findViewById(R.id.btnQr);
        Button btnAddEmployee = findViewById(R.id.btnAddEmployee);
        Button Employee = findViewById(R.id.Employee);
        Button Additional_Salary = findViewById(R.id.Additional_Salary);
        Button add_holiday = findViewById(R.id.add_holiday);
        Button cont_holiday = findViewById(R.id.cont_holidays);
        Button btnDepartment = findViewById(R.id.departmentBtn);
        Button btnDesignations = findViewById(R.id.jobTitleBtn);
        Button createSalarySlip = findViewById(R.id.salary_slip);
        Button gps = findViewById(R.id.gps);
        Button workPolicy = findViewById(R.id.workPolicy);
       // Button salary_drafts = findViewById(R.id.salary_drafts);
        Button btnLeaveRequest = findViewById(R.id.HrLeaveRequest);
        Button btnOverRequest = findViewById(R.id.HrOverRequest);
        logoutBtn = findViewById(R.id.buttonLogout); // Make sure this ID exists in your layout

        // Get intent extras
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getStringExtra("company_id");

        // Set click listeners
        btnQr.setOnClickListener(v -> {
            Intent qrIntent = new Intent(MainActivity2.this, QrDisplayActivity.class);
            startActivity(qrIntent);
        });

        Additional_Salary.setOnClickListener(v -> {
            Intent Additional_Salary_Intent = new Intent(MainActivity2.this, AdditionalSalaryActivity.class);
            Additional_Salary_Intent.putExtra("company_id", company_id);
            Additional_Salary_Intent.putExtra("user_id", userId);
            startActivity(Additional_Salary_Intent);
        });

        //salary_drafts.setOnClickListener(v -> {
         //   Intent salary_drafts_Intent = new Intent(MainActivity2.this, SalaryDraftsActivity.class);
        //    salary_drafts_Intent.putExtra("company_id", company_id);
          //  salary_drafts_Intent.putExtra("user_id", userId);
           // startActivity(salary_drafts_Intent);
        //});

        workPolicy.setOnClickListener(v -> {
            Intent workPolicy_Intent = new Intent(MainActivity2.this, workPolicySettingsActivity.class);
            workPolicy_Intent.putExtra("company_id", company_id);
            workPolicy_Intent.putExtra("user_id", userId);
            startActivity(workPolicy_Intent);
        });

        add_holiday.setOnClickListener(v -> {
            Intent Add_holiday_Intent = new Intent(MainActivity2.this, AddHoliday.class);
            startActivity(Add_holiday_Intent);
        });

        cont_holiday.setOnClickListener(v -> {
            Intent cont_holiday_Intent = new Intent(MainActivity2.this, show_holidays.class);
            startActivity(cont_holiday_Intent);
        });

        createSalarySlip.setOnClickListener(v -> {
            Intent salary_slip_Intent = new Intent(MainActivity2.this, SalarySlip.class);
            salary_slip_Intent.putExtra("company_id", company_id);
            salary_slip_Intent.putExtra("user_id", userId);
            startActivity(salary_slip_Intent);
        });

        btnDepartment.setOnClickListener(v -> {
            Intent DepartmentIntent = new Intent(MainActivity2.this, DepartmentsActivity.class);
            startActivity(DepartmentIntent);
        });

        btnDesignations.setOnClickListener(v -> {
            Intent DesignationsIntent = new Intent(MainActivity2.this, DesignationsActivity.class);
            startActivity(DesignationsIntent);
        });

        btnLeaveRequest.setOnClickListener(v -> {
            Intent LeaveRequestIntent = new Intent(MainActivity2.this, HrLeaveRequest.class);
            startActivity(LeaveRequestIntent);
        });

        btnOverRequest.setOnClickListener(v -> {
            Intent OvertimeRequestIntent = new Intent(MainActivity2.this, HrOvertimeRequest.class);
            startActivity(OvertimeRequestIntent);
        });

        btnAddEmployee.setOnClickListener(v -> {
            // Uncomment when ready to implement
            // Intent addIntent = new Intent(MainActivity2.this, EmployeeAddActivity.class);
            // addIntent.putExtra("company_id", company_id);
            // startActivity(addIntent);
        });

        Employee.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, EmployeesListActivity.class);
            startActivity(addIntent);
        });

        gps.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, MapsActivity.class);
            addIntent.putExtra("user_id", userId);
            addIntent.putExtra("email", email);
            addIntent.putExtra("fullname", fullname);
            addIntent.putExtra("role", role);
            addIntent.putExtra("company_id", company_id);
            addIntent.putExtra("company_code", company_id);
            startActivity(addIntent);
        });

        // Set logout button listener
        logoutBtn.setOnClickListener(v -> logout());
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
                        Toast.makeText(MainActivity2.this, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity2.this, login.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity2.this, "فشل تسجيل الخروج. الكود: " + responseCode, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity2.this, "خطأ في الاتصال بالخادم: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}