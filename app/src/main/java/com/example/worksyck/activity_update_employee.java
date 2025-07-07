package com.example.worksyck;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class activity_update_employee extends AppCompatActivity {

    EditText emailEditText, fullNameEditText, phoneEditText, androidIdEditText,
            departmentIdEditText, designationIdEditText, baseSalaryEditText,
            normalHourRateEditText, overtimeHourRateEditText, expectedHoursPerDayEditText,
            workingDaysPerWeekEditText;
    Spinner salaryStructureSpinner;
    CheckBox statusCheckBox;

    // Day checkboxes
    CheckBox mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox,
            fridayCheckBox, saturdayCheckBox, sundayCheckBox;

    Button updateButton;

    final String[] salaryOptions = {"base salary", "per hour"};

    int employeeId = -1; // You can replace this with actual ID (e.g., getIntent().getIntExtra)

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_employee);
        employeeId=Integer.parseInt(getIntent().getStringExtra("user_id"));
        // Bind views
        emailEditText = findViewById(R.id.emailEditText);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        androidIdEditText = findViewById(R.id.androidIdEditText);
        departmentIdEditText = findViewById(R.id.departmentIdEditText);
        designationIdEditText = findViewById(R.id.designationIdEditText);
        baseSalaryEditText = findViewById(R.id.baseSalaryEditText);
        normalHourRateEditText = findViewById(R.id.normalHourRateEditText);
        overtimeHourRateEditText = findViewById(R.id.overtimeHourRateEditText);
        expectedHoursPerDayEditText = findViewById(R.id.expectedHoursPerDayEditText);
        workingDaysPerWeekEditText = findViewById(R.id.workingDaysPerWeekEditText);
        salaryStructureSpinner = findViewById(R.id.salaryStructureSpinner);
        statusCheckBox = findViewById(R.id.statusCheckBox);

        // Day checkboxes
        mondayCheckBox = findViewById(R.id.mondayCheckBox);
        tuesdayCheckBox = findViewById(R.id.tuesdayCheckBox);
        wednesdayCheckBox = findViewById(R.id.wednesdayCheckBox);
        thursdayCheckBox = findViewById(R.id.thursdayCheckBox);
        fridayCheckBox = findViewById(R.id.fridayCheckBox);
        saturdayCheckBox = findViewById(R.id.saturdayCheckBox);
        sundayCheckBox = findViewById(R.id.sundayCheckBox);
        updateButton = findViewById(R.id.updateButton);

        // Spinner setup
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, salaryOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        salaryStructureSpinner.setAdapter(spinnerAdapter);

        salaryStructureSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                toggleSalaryFields();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        fetchEmployeeData();

        updateButton.setOnClickListener(v -> updateEmployeeData());
    }

    private void toggleSalaryFields() {
        String selected = salaryStructureSpinner.getSelectedItem().toString();
        if (selected.equals("per hour")) {
            baseSalaryEditText.setEnabled(false);
            baseSalaryEditText.setText("");
            normalHourRateEditText.setEnabled(true);
            overtimeHourRateEditText.setEnabled(true);
        } else {
            baseSalaryEditText.setEnabled(true);
            normalHourRateEditText.setEnabled(true);
            overtimeHourRateEditText.setEnabled(true);
        }
    }

    private void fetchEmployeeData() {
        String url = "http://10.0.2.2/worksync/fetch_employee_data.php";

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("employee_id", employeeId);

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            if (json.getString("status").equals("success")) {
                                JSONObject data = json.getJSONObject("data");

                                emailEditText.setText(data.getString("email"));
                                fullNameEditText.setText(data.getString("fullname"));
                                phoneEditText.setText(data.getString("phone"));
                                statusCheckBox.setChecked(data.getString("status").equals("active"));
                                androidIdEditText.setText(data.isNull("android_id") ? "" : data.getString("android_id"));
                                departmentIdEditText.setText(data.getString("department_id"));
                                designationIdEditText.setText(data.getString("designation_id"));
                                expectedHoursPerDayEditText.setText(data.getString("expected_hours_per_day"));
                                workingDaysPerWeekEditText.setText(data.getString("working_day_per_week"));
                                normalHourRateEditText.setText(data.getString("normal_hour_rate"));
                                overtimeHourRateEditText.setText(data.getString("overtime_hour_rate"));
                                baseSalaryEditText.setText(data.isNull("base_salary") ? "" : data.getString("base_salary"));

                                int pos = data.getString("salary_structure_type").equals("per hour") ? 1 : 0;
                                salaryStructureSpinner.setSelection(pos);
                                toggleSalaryFields();

                                // Working days
                                JSONObject workingDays = json.getJSONObject("working_days");
                                sundayCheckBox.setChecked(workingDays.optInt("1") == 1);
                                mondayCheckBox.setChecked(workingDays.optInt("2") == 1);
                                tuesdayCheckBox.setChecked(workingDays.optInt("3") == 1);
                                wednesdayCheckBox.setChecked(workingDays.optInt("4") == 1);
                                thursdayCheckBox.setChecked(workingDays.optInt("5") == 1);
                                fridayCheckBox.setChecked(workingDays.optInt("6") == 1);
                                saturdayCheckBox.setChecked(workingDays.optInt("7") == 1);

                            } else {
                                Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show()
            ) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return requestBody.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmployeeData() {
        String url = "http://10.0.2.2/worksync/update_employee_data.php";

        try {
            JSONObject body = new JSONObject();
            body.put("employee_id", employeeId);
            body.put("email", emailEditText.getText().toString());
            body.put("fullname", fullNameEditText.getText().toString());
            body.put("phone", phoneEditText.getText().toString());
            body.put("status", statusCheckBox.isChecked() ? "active" : "inactive");
            body.put("android_id", androidIdEditText.getText().toString().isEmpty() ? JSONObject.NULL : androidIdEditText.getText().toString());
            body.put("department_id", departmentIdEditText.getText().toString());
            body.put("designation_id", designationIdEditText.getText().toString());
            body.put("salary_structure_type", salaryStructureSpinner.getSelectedItem().toString());
            body.put("base_salary", baseSalaryEditText.isEnabled() ? baseSalaryEditText.getText().toString() : JSONObject.NULL);
            body.put("normal_hour_rate", normalHourRateEditText.getText().toString());
            body.put("overtime_hour_rate", overtimeHourRateEditText.getText().toString());
            body.put("expected_hours_per_day", expectedHoursPerDayEditText.getText().toString());
            body.put("working_day_per_week", workingDaysPerWeekEditText.getText().toString());

            // Working days map
            JSONObject workingDays = new JSONObject();
            workingDays.put("1", sundayCheckBox.isChecked() ? 1 : 0);
            workingDays.put("2", mondayCheckBox.isChecked() ? 1 : 0);
            workingDays.put("3", tuesdayCheckBox.isChecked() ? 1 : 0);
            workingDays.put("4", wednesdayCheckBox.isChecked() ? 1 : 0);
            workingDays.put("5", thursdayCheckBox.isChecked() ? 1 : 0);
            workingDays.put("6", fridayCheckBox.isChecked() ? 1 : 0);
            workingDays.put("7", saturdayCheckBox.isChecked() ? 1 : 0);

            body.put("working_days", workingDays);

            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            Toast.makeText(this, json.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(this, "Parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show()
            ) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return body.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            Volley.newRequestQueue(this).add(request);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
