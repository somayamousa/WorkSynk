//package com.example.worksyck;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class SalaryDraftDetailsActivity extends AppCompatActivity {
//    private TextView employeeNameText, employeeCodeText, departmentText, designationText, salaryTypeText;
//    private TextView expectedWorkDaysText, absentDaysText, hourlyRateText, overtimeRateText, allowancesText, netSalaryText;
//    private Button editButton;
//    private SalaryDraft draft;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_salary_draft_details);
//
//        // Initialize views
//        employeeNameText = findViewById(R.id.employeeNameText);
//        employeeCodeText = findViewById(R.id.employeeCodeText);
//        departmentText = findViewById(R.id.departmentText);
//        designationText = findViewById(R.id.designationText);
//        salaryTypeText = findViewById(R.id.salaryTypeText);
//        expectedWorkDaysText = findViewById(R.id.expectedWorkDaysText);
//        absentDaysText = findViewById(R.id.absentDaysText);
//        hourlyRateText = findViewById(R.id.hourlyRateText);
//        overtimeRateText = findViewById(R.id.overtimeRateText);
//        allowancesText = findViewById(R.id.allowancesText);
//        netSalaryText = findViewById(R.id.netSalaryText);
//        editButton = findViewById(R.id.editButton);
//
//        // Get draft from intent
//        draft = (SalaryDraft) getIntent().getSerializableExtra("salary_draft");
//        if (draft == null) {
//            Toast.makeText(this, "Invalid draft data", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        // Set non-editable fields
//        employeeNameText.setText(draft.getEmployeeName());
//        employeeCodeText.setText("Code: " + draft.getEmployeeCode());
//        departmentText.setText("Department: " + draft.getDepartmentName());
//        designationText.setText("Designation: " + draft.getDesignationName());
//        salaryTypeText.setText("Salary Type: " + draft.getSalaryStructureType());
//        expectedWorkDaysText.setText("Expected Work Days: " + draft.getExpectedWorkDays());
//        absentDaysText.setText("Absent Days: " + draft.getAbsentDays());
//        hourlyRateText.setText("Hourly Rate: $" + String.format("%.2f", draft.getHourlyRate()));
//        overtimeRateText.setText("Overtime Rate: $" + String.format("%.2f", draft.getOvertimeRate()));
//        allowancesText.setText("Allowances: $" + String.format("%.2f", draft.getAllowances()));
//        netSalaryText.setText("Net Salary: $" + String.format("%.2f", draft.getNetSalary()));
//
//        // Edit button click
//        editButton.setOnClickListener(v -> {
////            Intent intent = new Intent(this, Edit.class);
////            intent.putExtra("salary_draft", draft);
////            startActivity(intent);
//        });
//    }
//}