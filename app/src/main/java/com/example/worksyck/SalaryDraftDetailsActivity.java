//package com.example.worksyck;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import org.json.JSONException;
//import org.json.JSONObject;
//import java.util.HashMap;
//import java.util.Map;
//
//public class SalaryDraftDetailsActivity extends AppCompatActivity {
//    private SalaryDraft salaryDraft;
//    private LinearLayout displayLayout, editLayout;
//    private TextView paySlipCodeText, employeeCodeText, employeeNameText, departmentText, designationText,
//            employeeIdText, netSalaryText, baseSalaryText, periodStartText, periodEndText,
//            monthText, yearText, bonusText, salaryIncrementText, salaryStructureTypeText,
//            regularHoursText, regularHourRateText, regularSalaryText, overtimeHoursText,
//            overtimeHourRateText, overtimeSalaryText, expectedWorkingDaysText, absentDaysText,
//            statusText, createdAtText;
//    private TextView paySlipCodeEditText, employeeCodeEditText, employeeNameEditText, departmentEditText,
//            designationEditText, createdAtEditText;
//    private EditText employeeIdInput, netSalaryInput, baseSalaryInput, periodStartInput, periodEndInput,
//            monthInput, yearInput, bonusInput, salaryIncrementInput, salaryStructureTypeInput,
//            regularHoursInput, regularHourRateInput, regularSalaryInput, overtimeHoursInput,
//            overtimeHourRateInput, overtimeSalaryInput, expectedWorkingDaysInput, absentDaysInput;
//    private Spinner statusSpinner;
//    private Button editBtn, confirmBtn, cancelBtn, generatePdfBtn;
//    private RequestQueue requestQueue;
//    private boolean isHourlySalary;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_salary_draft_details);
//
//        // Get draft from intent
//        salaryDraft = (SalaryDraft) getIntent().getSerializableExtra("salary_draft");
//        if (salaryDraft == null) {
//            Toast.makeText(this, "No draft data", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        // Check salary structure type
//        isHourlySalary = salaryDraft.getSalaryStructureType().equalsIgnoreCase("hourly");
//
//        // Initialize display views
//        displayLayout = findViewById(R.id.displayLayout);
//        paySlipCodeText = findViewById(R.id.paySlipCodeText);
//        employeeCodeText = findViewById(R.id.employeeCodeText);
//        employeeNameText = findViewById(R.id.employeeNameText);
//        departmentText = findViewById(R.id.departmentText);
//        designationText = findViewById(R.id.designationText);
//        employeeIdText = findViewById(R.id.employeeIdText);
//        netSalaryText = findViewById(R.id.netSalaryText);
//        baseSalaryText = findViewById(R.id.baseSalaryText);
//        periodStartText = findViewById(R.id.periodStartText);
//        periodEndText = findViewById(R.id.periodEndText);
//        monthText = findViewById(R.id.monthText);
//        yearText = findViewById(R.id.yearText);
//        bonusText = findViewById(R.id.bonusText);
//        salaryIncrementText = findViewById(R.id.salaryIncrementText);
//        salaryStructureTypeText = findViewById(R.id.salaryStructureTypeText);
//        regularHoursText = findViewById(R.id.regularHoursText);
//        regularHourRateText = findViewById(R.id.regularHourRateText);
//        regularSalaryText = findViewById(R.id.regularSalaryText);
//        overtimeHoursText = findViewById(R.id.overtimeHoursText);
//        overtimeHourRateText = findViewById(R.id.overtimeHourRateText);
//        overtimeSalaryText = findViewById(R.id.overtimeSalaryText);
//        expectedWorkingDaysText = findViewById(R.id.expectedWorkingDaysText);
//        absentDaysText = findViewById(R.id.absentDaysText);
//        statusText = findViewById(R.id.statusText);
//        createdAtText = findViewById(R.id.createdAtText);
//        editBtn = findViewById(R.id.editBtn);
//        generatePdfBtn = findViewById(R.id.generatePdfBtn);
//
//        // Initialize edit views
//        editLayout = findViewById(R.id.editLayout);
//        paySlipCodeEditText = findViewById(R.id.paySlipCodeEditText);
//        employeeCodeEditText = findViewById(R.id.employeeCodeEditText);
//        employeeNameEditText = findViewById(R.id.employeeNameEditText);
//        departmentEditText = findViewById(R.id.departmentEditText);
//        designationEditText = findViewById(R.id.designationEditText);
//        employeeIdInput = findViewById(R.id.employeeIdInput);
//        netSalaryInput = findViewById(R.id.netSalaryInput);
//        baseSalaryInput = findViewById(R.id.baseSalaryInput);
//        periodStartInput = findViewById(R.id.periodStartInput);
//        periodEndInput = findViewById(R.id.periodEndInput);
//        monthInput = findViewById(R.id.monthInput);
//        yearInput = findViewById(R.id.yearInput);
//        bonusInput = findViewById(R.id.bonusInput);
//        salaryIncrementInput = findViewById(R.id.salaryIncrementInput);
//        salaryStructureTypeInput = findViewById(R.id.salaryStructureTypeInput);
//        regularHoursInput = findViewById(R.id.regularHoursInput);
//        regularHourRateInput = findViewById(R.id.regularHourRateInput);
//        regularSalaryInput = findViewById(R.id.regularSalaryInput);
//        overtimeHoursInput = findViewById(R.id.overtimeHoursInput);
//        overtimeHourRateInput = findViewById(R.id.overtimeHourRateInput);
//        overtimeSalaryInput = findViewById(R.id.overtimeSalaryInput);
//        expectedWorkingDaysInput = findViewById(R.id.expectedWorkingDaysInput);
//        absentDaysInput = findViewById(R.id.absentDaysInput);
//        statusSpinner = findViewById(R.id.statusSpinner);
//        createdAtEditText = findViewById(R.id.createdAtEditText);
//        confirmBtn = findViewById(R.id.confirmBtn);
//        cancelBtn = findViewById(R.id.cancelBtn);
//
//        requestQueue = Volley.newRequestQueue(this);
//
//        // Populate display views
//        populateDisplayViews();
//
//        // Setup status spinner for edit mode
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.status_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        statusSpinner.setAdapter(adapter);
//        String[] statusArray = getResources().getStringArray(R.array.status_array);
//        for (int i = 0; i < statusArray.length; i++) {
//            if (statusArray[i].equals(salaryDraft.gets())) {
//                statusSpinner.setSelection(i);
//                break;
//            }
//        }
//
//        // Setup buttons
//        editBtn.setOnClickListener(v -> switchToEditMode());
//        confirmBtn.setOnClickListener(v -> updateDraft());
//        cancelBtn.setOnClickListener(v -> switchToDisplayMode());
//        generatePdfBtn.setOnClickListener(v -> generateSalarySlipPdf());
//    }
//
//    private void populateDisplayViews() {
//        paySlipCodeText.setText("Pay Slip Code: " + salaryDraft.getPaySlipCode());
//        employeeCodeText.setText("Employee Code: " + salaryDraft.getEmployeeCode());
//        employeeNameText.setText("Employee Name: " + salaryDraft.getEmployeeName());
//        departmentText.setText("Department: " + salaryDraft.getDepartmentName());
//        designationText.setText("Designation: " + salaryDraft.getDesignationName());
//        employeeIdText.setText("Employee ID: " + salaryDraft.getEmployeeId());
//        netSalaryText.setText("Net Salary: $" + String.format("%.2f", salaryDraft.getNetSalary()));
//        periodStartText.setText("Period Start: " + salaryDraft.getPeriodStart());
//        periodEndText.setText("Period End: " + salaryDraft.getPeriodEnd());
//        monthText.setText("Month: " + salaryDraft.getMonth());
//        yearText.setText("Year: " + salaryDraft.getYear());
//        salaryStructureTypeText.setText("Salary Structure Type: " + salaryDraft.getSalaryStructureType());
//        createdAtText.setText("Created At: " + (salaryDraft.getCreatedAt() != null ? salaryDraft.getCreatedAt() : "N/A"));
//        statusText.setText("Status: " + salaryDraft.getStatus());
//
//        // Conditionally show fields based on salary structure type
//        if (!isHourlySalary) {
//            baseSalaryText.setText("Base Salary: " + (salaryDraft.getBaseSalary() != null ? String.format("%.2f", salaryDraft.getBaseSalary()) : "N/A"));
//            bonusText.setText("Bonus: $" + String.format("%.2f", salaryDraft.getBonus()));
//            salaryIncrementText.setText("Salary Increment: $" + String.format("%.2f", salaryDraft.getSalaryIncrement()));
//        } else {
//            baseSalaryText.setVisibility(View.GONE);
//            bonusText.setVisibility(View.GONE);
//            salaryIncrementText.setVisibility(View.GONE);
//        }
//
//        regularHoursText.setText("Regular Hours: " + (salaryDraft.getRegularHours() != null ? String.format("%.2f", salaryDraft.getRegularHours()) : "N/A"));
//        regularHourRateText.setText("Regular Hour Rate: " + (salaryDraft.getRegularHourRate() != null ? String.format("%.2f", salaryDraft.getRegularHourRate()) : "N/A"));
//        regularSalaryText.setText("Regular Salary: " + (salaryDraft.getRegularSalary() != null ? String.format("%.2f", salaryDraft.getRegularSalary()) : "N/A"));
//        overtimeHoursText.setText("Overtime Hours: " + (salaryDraft.getOvertimeHours() != null ? String.format("%.2f", salaryDraft.getOvertimeHours()) : "N/A"));
//        overtimeHourRateText.setText("Overtime Hour Rate: " + (salaryDraft.getOvertimeHourRate() != null ? String.format("%.2f", salaryDraft.getOvertimeHourRate()) : "N/A"));
//        overtimeSalaryText.setText("Overtime Salary: " + (salaryDraft.getOvertimeSalary() != null ? String.format("%.2f", salaryDraft.getOvertimeSalary()) : "N/A"));
//        expectedWorkingDaysText.setText("Expected Working Days: " + (salaryDraft.getExpectedWorkingDays() != null ? salaryDraft.getExpectedWorkingDays() : "N/A"));
//        absentDaysText.setText("Absent Days: " + (salaryDraft.getAbsentDays() != null ? salaryDraft.getAbsentDays() : "N/A"));
//    }
//
//    private void switchToEditMode() {
//        // Populate edit views
//        paySlipCodeEditText.setText("Pay Slip Code: " + salaryDraft.getPaySlipCode());
//        employeeCodeEditText.setText("Employee Code: " + salaryDraft.getEmployeeCode());
//        employeeNameEditText.setText("Employee Name: " + salaryDraft.getEmployeeName());
//        departmentEditText.setText("Department: " + salaryDraft.getDepartmentName());
//        designationEditText.setText("Designation: " + salaryDraft.getDesignationName());
//        employeeIdInput.setText(String.valueOf(salaryDraft.getEmployeeId()));
//        netSalaryInput.setText(String.format("%.2f", salaryDraft.getNetSalary()));
//        periodStartInput.setText(salaryDraft.getPeriodStart());
//        periodEndInput.setText(salaryDraft.getPeriodEnd());
//        monthInput.setText(String.valueOf(salaryDraft.getMonth()));
//        yearInput.setText(String.valueOf(salaryDraft.getYear()));
//        salaryStructureTypeInput.setText(salaryDraft.getSalaryStructureType());
//        createdAtEditText.setText("Created At: " + (salaryDraft.getCreatedAt() != null ? salaryDraft.getCreatedAt() : "N/A"));
//
//        // Conditionally show edit fields
//        if (!isHourlySalary) {
//            baseSalaryInput.setText(salaryDraft.getBaseSalary() != null ? String.format("%.2f", salaryDraft.getBaseSalary()) : "");
//            bonusInput.setText(String.format("%.2f", salaryDraft.getBonus()));
//            salaryIncrementInput.setText(String.format("%.2f", salaryDraft.getSalaryIncrement()));
//        } else {
//            baseSalaryInput.setVisibility(View.GONE);
//            bonusInput.setVisibility(View.GONE);
//            salaryIncrementInput.setVisibility(View.GONE);
//        }
//
//        regularHoursInput.setText(salaryDraft.getRegularHours() != null ? String.format("%.2f", salaryDraft.getRegularHours()) : "");
//        regularHourRateInput.setText(salaryDraft.getRegularHourRate() != null ? String.format("%.2f", salaryDraft.getRegularHourRate()) : "");
//        regularSalaryInput.setText(salaryDraft.getRegularSalary() != null ? String.format("%.2f", salaryDraft.getRegularSalary()) : "");
//        overtimeHoursInput.setText(salaryDraft.getOvertimeHours() != null ? String.format("%.2f", salaryDraft.getOvertimeHours()) : "");
//        overtimeHourRateInput.setText(salaryDraft.getOvertimeHourRate() != null ? String.format("%.2f", salaryDraft.getOvertimeHourRate()) : "");
//        overtimeSalaryInput.setText(salaryDraft.getOvertimeSalary() != null ? String.format("%.2f", salaryDraft.getOvertimeSalary()) : "");
//        expectedWorkingDaysInput.setText(salaryDraft.getExpectedWorkingDays() != null ? String.valueOf(salaryDraft.getExpectedWorkingDays()) : "");
//        absentDaysInput.setText(salaryDraft.getAbsentDays() != null ? String.valueOf(salaryDraft.getAbsentDays()) : "");
//
//        // Switch layouts
//        displayLayout.setVisibility(View.GONE);
//        editLayout.setVisibility(View.VISIBLE);
//    }
//
//    private void switchToDisplayMode() {
//        displayLayout.setVisibility(View.VISIBLE);
//        editLayout.setVisibility(View.GONE);
//    }
//
//    private void updateDraft() {
//        // Basic validation
//        if (employeeIdInput.getText().toString().isEmpty() ||
//                netSalaryInput.getText().toString().isEmpty() ||
//                periodStartInput.getText().toString().isEmpty() ||
//                periodEndInput.getText().toString().isEmpty() ||
//                monthInput.getText().toString().isEmpty() ||
//                yearInput.getText().toString().isEmpty() ||
//                salaryStructureTypeInput.getText().toString().isEmpty()) {
//            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String url = "http://10.0.2.2/worksync/update_salary_draft.php";
//        StringRequest request = new StringRequest(Request.Method.POST, url,
//                response -> {
//                    try {
//                        JSONObject jsonObject = new JSONObject(response);
//                        if (jsonObject.getString("status").equals("success")) {
//                            Toast.makeText(this, "Draft updated successfully", Toast.LENGTH_SHORT).show();
//                            // Update local draft object
//                            salaryDraft = new SalaryDraft(
//                                    salaryDraft.getId(),
//                                    Integer.parseInt(employeeIdInput.getText().toString()),
//                                    salaryDraft.getEmployeeCode(),
//                                    salaryDraft.getEmployeeName(),
//                                    Double.parseDouble(netSalaryInput.getText().toString()),
//                                    baseSalaryInput.getText().toString().isEmpty() ? null : Double.parseDouble(baseSalaryInput.getText().toString()),
//                                    periodStartInput.getText().toString(),
//                                    periodEndInput.getText().toString(),
//                                    Integer.parseInt(monthInput.getText().toString()),
//                                    Integer.parseInt(yearInput.getText().toString()),
//                                    bonusInput.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(bonusInput.getText().toString()),
//                                    salaryIncrementInput.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(salaryIncrementInput.getText().toString()),
//                                    salaryStructureTypeInput.getText().toString(),
//                                    regularHoursInput.getText().toString().isEmpty() ? null : Double.parseDouble(regularHoursInput.getText().toString()),
//                                    regularHourRateInput.getText().toString().isEmpty() ? null : Double.parseDouble(regularHourRateInput.getText().toString()),
//                                    regularSalaryInput.getText().toString().isEmpty() ? null : Double.parseDouble(regularSalaryInput.getText().toString()),
//                                    overtimeHoursInput.getText().toString().isEmpty() ? null : Double.parseDouble(overtimeHoursInput.getText().toString()),
//                                    overtimeHourRateInput.getText().toString().isEmpty() ? null : Double.parseDouble(overtimeHourRateInput.getText().toString()),
//                                    overtimeSalaryInput.getText().toString().isEmpty() ? null : Double.parseDouble(overtimeSalaryInput.getText().toString()),
//                                    expectedWorkingDaysInput.getText().toString().isEmpty() ? null : Integer.parseInt(expectedWorkingDaysInput.getText().toString()),
//                                    absentDaysInput.getText().toString().isEmpty() ? null : Integer.parseInt(absentDaysInput.getText().toString()),
//                                    salaryDraft.getCreatedAt(),
//                                    statusSpinner.getSelectedItem().toString(),
//                                    salaryDraft.getDepartmentName(),
//                                    salaryDraft.getDesignationName(),
//                                    salaryDraft.getPaySlipCode()
//                            );
//                            populateDisplayViews();
//                            switchToDisplayMode();
//                            setResult(RESULT_OK);
//                        } else {
//                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        Log.e("JSON Error", e.getMessage());
//                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> {
//                    Log.e("Volley Error", error.getMessage());
//                    Toast.makeText(this, "Error updating draft", Toast.LENGTH_SHORT).show();
//                }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("id", String.valueOf(salaryDraft.getId()));
//                params.put("employee_id", employeeIdInput.getText().toString());
//                params.put("net_salary", netSalaryInput.getText().toString());
//                params.put("base_salary", baseSalaryInput.getText().toString());
//                params.put("period_start", periodStartInput.getText().toString());
//                params.put("period_end", periodEndInput.getText().toString());
//                params.put("month", monthInput.getText().toString());
//                params.put("year", yearInput.getText().toString());
//                params.put("bonus", bonusInput.getText().toString());
//                params.put("salary_increment", salaryIncrementInput.getText().toString());
//                params.put("salary_structure_type", salaryStructureTypeInput.getText().toString());
//                params.put("regular_hours", regularHoursInput.getText().toString());
//                params.put("regular_hour_rate", regularHourRateInput.getText().toString());
//                params.put("regular_salary", regularSalaryInput.getText().toString());
//                params.put("overtime_hours", overtimeHoursInput.getText().toString());
//                params.put("overtime_hour_rate", overtimeHourRateInput.getText().toString());
//                params.put("overtime_salary", overtimeSalaryInput.getText().toString());
//                params.put("expected_working_days", expectedWorkingDaysInput.getText().toString());
//                params.put("absent_days", absentDaysInput.getText().toString());
//                params.put("status", statusSpinner.getSelectedItem().toString());
//                return params;
//            }
//        };
//        requestQueue.add(request);
//    }
//
//    private void generateSalarySlipPdf() {
//        // Placeholder for PDF generation
//        Toast.makeText(this, "PDF generation not implemented", Toast.LENGTH_SHORT).show();
//        // Example implementation:
//        /*
//        new SalarySlip().generateSalarySlipPdf(
//                salaryDraft.getEmployeeName(),
//                salaryDraft.getEmployeeCode(),
//                salaryDraft.getPaySlipCode(),
//                String.valueOf(salaryDraft.getMonth()),
//                salaryDraft.getBaseSalary(),
//                salaryDraft.getNetSalary(),
//                salaryDraft.getRegularHours(),
//                salaryDraft.getRegularHourRate(),
//                salaryDraft.getRegularSalary(),
//                salaryDraft.getOvertimeHours(),
//                salaryDraft.getOvertimeHourRate(),
//                salaryDraft.getOvertimeSalary(),
//                salaryDraft.getSalaryIncrement(),
//                salaryDraft.getBonus(),
//                salaryDraft.getPeriodStart(),
//                salaryDraft.getPeriodEnd()
//        );
//        */
//    }
//}
