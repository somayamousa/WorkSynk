package com.example.worksyck;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class MainActivity2 extends AppCompatActivity {
    private int userId;
    private String email, fullname,role,company_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);



        // Buttons
        CardView btnQr = findViewById(R.id.btnQr);
        CardView btnAddEmployee = findViewById(R.id.btnAddEmployee);
     //   CardView Employee = findViewById(R.id.Employee);
        CardView Additional_Salary = findViewById(R.id.Additional_Salary);
        CardView cont_holiday = findViewById(R.id.cont_holidays);
        CardView btnDepartment = findViewById(R.id.departmentBtn);
        CardView btnDesignations = findViewById(R.id.jobTitleBtn);
        CardView createSalarySlip = findViewById(R.id.salary_slip);
        CardView gps = findViewById(R.id.gps);
        CardView workPolicy = findViewById(R.id.workPolicy);
        CardView salary_drafts = findViewById(R.id.salary_drafts);
        CardView btnLeaveRequest = findViewById(R.id.HrLeaveRequest);
        CardView btnOverRequest = findViewById(R.id.HrOverRequest);

        Intent intent = getIntent();

        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role=getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id=getIntent().getStringExtra("company_id");
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(MainActivity2.this);
            progressDialog.setMessage("Logging out...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressDialog.dismiss();
                Intent loginIntent = new Intent(MainActivity2.this, login.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginIntent);
                finish();
            }, 1500);
        });

        // Initialize NavigationHelper and set back button functionality
        // QR Button Click
        btnQr.setOnClickListener(v -> {
            Intent qrIntent = new Intent(MainActivity2.this, QrDisplayActivity.class);
            startActivity(qrIntent);
        });
        Additional_Salary.setOnClickListener(v -> {

            Intent      Additional_Salary_Intent = new Intent(MainActivity2.this, AdditionalSalaryActivity.class);
            Additional_Salary_Intent.putExtra("company_id",company_id);
            Additional_Salary_Intent.putExtra("user_id",userId);
            startActivity( Additional_Salary_Intent);
        });
        salary_drafts.setOnClickListener(v -> {

            Intent      salary_drafts_Intent = new Intent(MainActivity2.this, SalaryDraftsActivity.class);
            salary_drafts_Intent.putExtra("company_id",company_id);
            salary_drafts_Intent.putExtra("user_id",userId);
            startActivity( salary_drafts_Intent);
        });
        workPolicy.setOnClickListener(v -> {
            Intent      workPolicy_Intent = new Intent(MainActivity2.this, workPolicySettingsActivity.class);
            workPolicy_Intent.putExtra("company_id",company_id);
            workPolicy_Intent.putExtra("user_id",userId);
            startActivity( workPolicy_Intent);
        });
      //  add_holiday.setOnClickListener(v -> {
         //   Intent     Add_holiday_Intent = new Intent(MainActivity2.this, AddHoliday.class);
         //   startActivity(Add_holiday_Intent);
      //  });
        cont_holiday.setOnClickListener(v -> {

            Intent     cont_holiday_Intent = new Intent(MainActivity2.this, show_holidays.class);

            startActivity(cont_holiday_Intent);

        });
        createSalarySlip.setOnClickListener(v -> {
            Intent     salary_slip_Intent = new Intent(MainActivity2.this,SalarySlip.class);
            salary_slip_Intent .putExtra("company_id",company_id);
            salary_slip_Intent .putExtra("user_id",userId);
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
        // Add Employee Button Click
        btnAddEmployee.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, EmployeesActivity.class);
            addIntent.putExtra("company_id",company_id);
            startActivity(addIntent);
        });
        //Employee.setOnClickListener(v -> {
          //  Intent addIntent = new Intent(MainActivity2.this, EmployeesActivity.class);
          //  startActivity(addIntent); });
gps.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, MapsActivity.class);
            addIntent.putExtra("user_id", userId);
            addIntent.putExtra("email", email);
            addIntent.putExtra("fullname", fullname);
            addIntent.putExtra("role", role);
            addIntent.putExtra("company_id", company_id);
            addIntent.putExtra("company_code", company_id);            startActivity(addIntent);
        });
    }
}