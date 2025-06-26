package com.example.worksyck;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeeProfileActivity extends AppCompatActivity {

    private TextView fullnameText, emailText, phoneText, departmentText, designationText,
            codeText, statusText, macText, salaryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);

        fullnameText = findViewById(R.id.profile_fullname);
        emailText = findViewById(R.id.profile_email);
        phoneText = findViewById(R.id.profile_phone);
        departmentText = findViewById(R.id.profile_department);
        designationText = findViewById(R.id.profile_designation);
        codeText = findViewById(R.id.profile_code);
        statusText = findViewById(R.id.profile_status);
        macText = findViewById(R.id.profile_mac);
        salaryText = findViewById(R.id.profile_salary);

        // عرض البيانات المرسلة من EmployeesActivity
        fullnameText.setText("Name: " + getIntent().getStringExtra("fullname"));
        emailText.setText("Email: " + getIntent().getStringExtra("email"));
        phoneText.setText("Phone: " + getIntent().getStringExtra("phone"));
        departmentText.setText("Department: " + getIntent().getStringExtra("department"));
        designationText.setText("Designation: " + getIntent().getStringExtra("designation"));
        codeText.setText("Employee Code: " + getIntent().getStringExtra("employee_code"));
        statusText.setText("Status: " + getIntent().getStringExtra("status"));
        macText.setText("Device ID: " + getIntent().getStringExtra("mac_address"));
        salaryText.setText("Base Salary: " + getIntent().getDoubleExtra("base_salary", 0.0));
    }
}
