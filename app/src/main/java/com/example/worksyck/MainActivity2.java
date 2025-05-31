package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Buttons
        Button btnQr = findViewById(R.id.btnQr);
        Button btnAddEmployee = findViewById(R.id.btnAddEmployee);
        Button   Employee= findViewById(R.id.Employee);
        Button   payroll_entry= findViewById(R.id.payroll_entry);
        Button   add_holiday= findViewById(R.id.add_holiday);
        Button   cont_holiday= findViewById(R.id.cont_holidays);
        Button       btnDepartment= findViewById(R.id.departmentBtn);
        Button      btnDesignations= findViewById(R.id.jobTitleBtn);
        Button  createSalarySlip=findViewById(R.id.salary_slip);
        Button  gps=findViewById(R.id.gps);

       Button btnLeaveRequest = findViewById(R.id.HrLeaveRequest);
        Button btnOverRequest = findViewById(R.id.HrOverRequest);


        Intent intent = getIntent();

        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role=getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id=getIntent().getStringExtra("company_id");
        // Initialize NavigationHelper and set back button functionality
        // QR Button Click
        btnQr.setOnClickListener(v -> {
            Intent qrIntent = new Intent(MainActivity2.this, QrDisplayActivity.class);
            startActivity(qrIntent);
        });
        payroll_entry.setOnClickListener(v -> {

            Intent     payroll_entry_Intent = new Intent(MainActivity2.this, Payroll_Entry.class);
            payroll_entry_Intent.putExtra("company_id",company_id);
            payroll_entry_Intent.putExtra("user_id",userId);
            startActivity(payroll_entry_Intent);
        });

        add_holiday.setOnClickListener(v -> {

            Intent     Add_holiday_Intent = new Intent(MainActivity2.this, AddHoliday.class);

            startActivity(Add_holiday_Intent);

        });
        cont_holiday.setOnClickListener(v -> {

            Intent     cont_holiday_Intent = new Intent(MainActivity2.this, show_holidays.class);

            startActivity(cont_holiday_Intent);

        });
        createSalarySlip.setOnClickListener(v -> {

            Intent     salary_slip_Intent = new Intent(MainActivity2.this,SalarySlip.class);

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

            Intent addIntent = new Intent(MainActivity2.this, EmployeeAddActivity.class);
            addIntent.putExtra("company_id",company_id);
            startActivity(addIntent);
        });Employee.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, EmployeesListActivity.class);
            startActivity(addIntent);
        });gps.setOnClickListener(v -> {
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