package com.example.worksyck;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SalaryDraftDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_draft_details);
        // Request storage permissions
        // Get the SalaryDraft object from the intent
        SalaryDraft draft = (SalaryDraft) getIntent().getSerializableExtra("salary_draft");
        // Initialize TextViews
        TextView employeeNameText = findViewById(R.id.employeeNameText);
        TextView employeeCodeText = findViewById(R.id.employeeCodeText);
        TextView departmentText = findViewById(R.id.departmentText);
        TextView designationText = findViewById(R.id.designationText);
        TextView salaryStructureTypeText = findViewById(R.id.salaryStructureTypeText);
        TextView periodText = findViewById(R.id.periodText);
        TextView netSalaryText = findViewById(R.id.netSalaryText);
        TextView baseSalaryText = findViewById(R.id.baseSalaryText);
        TextView regularHoursText = findViewById(R.id.regularHoursText);
        TextView regularHourRateText = findViewById(R.id.regularHourRateText);
        TextView regularSalaryText = findViewById(R.id.regularSalaryText);
        TextView overtimeHoursText = findViewById(R.id.overtimeHoursText);
        TextView overtimeHourRateText = findViewById(R.id.overtimeHourRateText);
        TextView overtimeSalaryText = findViewById(R.id.overtimeSalaryText);
        TextView bonusText = findViewById(R.id.bonusText);
        TextView salaryIncrementText = findViewById(R.id.salaryIncrementText);
        TextView expectedWorkingDaysText = findViewById(R.id.expectedWorkingDaysText);
        TextView absentDaysText = findViewById(R.id.absentDaysText);
        TextView statusText = findViewById(R.id.statusText);

        // Set common fields
        employeeNameText.setText(draft.getEmployeeName());
        employeeCodeText.setText("Code: " + draft.getEmployeeCode());
        departmentText.setText("Department: " + draft.getDepartmentName());
        designationText.setText("Designation: " + draft.getDesignationName());
        salaryStructureTypeText.setText("Salary Structure: " + draft.getSalaryStructureType());
        periodText.setText("Period: " + draft.getPeriodStart() + " to " + draft.getPeriodEnd());
        netSalaryText.setText("Net Salary: $" + String.format("%.2f", draft.getNetSalary()));
        bonusText.setText("Bonus: $" + String.format("%.2f", draft.getBonus()));
        salaryIncrementText.setText("Salary Increment: $" + String.format("%.2f", draft.getSalaryIncrement()));
        expectedWorkingDaysText.setText("Expected Working Days: " + draft.getExpectedWorkingDays());
        absentDaysText.setText("Absent Days: " + draft.getAbsentDays());
        statusText.setText("Status: " + draft.getStatus());

        if (draft.getSalaryStructureType().equalsIgnoreCase("base salary")) {
            baseSalaryText.setText("Base Salary: $" + String.format("%.2f", draft.getBaseSalary()));


            // عرض بيانات الساعات الإضافية وسعر الساعة العادية
            regularHourRateText.setText("Regular Hour Rate: $" + String.format("%.2f", draft.getRegularHourRate()));
            overtimeHoursText.setText("Overtime Hours: " + String.format("%.2f", draft.getOvertimeHours()));
            overtimeHourRateText.setText("Overtime Hour Rate: $" + String.format("%.2f", draft.getOvertimeHourRate()));
            overtimeSalaryText.setText("Overtime Salary: $" + String.format("%.2f", draft.getOvertimeSalary()));

            // عرض العناصر
            regularHourRateText.setVisibility(View.VISIBLE);
            overtimeHoursText.setVisibility(View.VISIBLE);
            overtimeHourRateText.setVisibility(View.VISIBLE);
            overtimeSalaryText.setVisibility(View.VISIBLE);
            baseSalaryText.setVisibility(View.VISIBLE);
        } else if (draft.getSalaryStructureType().equalsIgnoreCase("per hour")) {
            regularHoursText.setText("Regular Hours: " + String.format("%.2f", draft.getRegularHours()));
            regularHourRateText.setText("Regular Hour Rate: $" + String.format("%.2f", draft.getRegularHourRate()));
            regularSalaryText.setText("Regular Salary: $" + String.format("%.2f", draft.getRegularSalary()));
            overtimeHoursText.setText("Overtime Hours: " + String.format("%.2f", draft.getOvertimeHours()));
            overtimeHourRateText.setText("Overtime Hour Rate: $" + String.format("%.2f", draft.getOvertimeHourRate()));
            overtimeSalaryText.setText("Overtime Salary: $" + String.format("%.2f", draft.getOvertimeSalary()));
            regularHoursText.setVisibility(View.VISIBLE);
            regularHourRateText.setVisibility(View.VISIBLE);
            regularSalaryText.setVisibility(View.VISIBLE);
            overtimeHoursText.setVisibility(View.VISIBLE);
            overtimeHourRateText.setVisibility(View.VISIBLE);
            overtimeSalaryText.setVisibility(View.VISIBLE);
        }
    }
}