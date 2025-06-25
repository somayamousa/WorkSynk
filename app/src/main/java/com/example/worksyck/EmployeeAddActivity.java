package com.example.worksyck;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeAddActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword, editTextFullName, editTextPhone,
            normalHourCostEditText, overTimeHourCostEditText, baseSalaryEditText,
            expectedHoursEditText, workingDaysPerWeekEditText;
    private RadioGroup radioGroupStatus;
    private Button btnAddEmployee, btnSelectHolidays;
    private RequestQueue requestQueue;
    private ArrayList<Departments> departments;
    private ArrayList<Designations> designations;
    private Spinner departmentsSpinner, designationsSpinner, salaryStructureSpinner;
    private String salaryStructureType, companyId, androidId, selectedStatusValue;
    private double baseSalary, normalHourRate, overTimeRate, expectedHoursPerDay;
    private int departmentId, designationId, workingDaysPerWeek;
    private List<String> departmentNames, designationsName;
    private List<Integer> departmentIds, designationsId;
    private List<Map<String, Integer>> workingDays; // Represents holidays (is_working_day = 0)
    private ImageButton backeEmployeeButton;
    private ProgressDialog progressDialog;
    private boolean[] holidaySelections = new boolean[7]; // Track holiday selections

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_add);

        // Get companyId from intent
        Intent intent = getIntent();
        companyId = intent.getStringExtra("company_id");

        // Initialize views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextPhone = findViewById(R.id.editTextPhone);
        backeEmployeeButton = findViewById(R.id.backButton);
        normalHourCostEditText = findViewById(R.id.normalHourCostEditText);
        overTimeHourCostEditText = findViewById(R.id.overTimeHourCostEditText);
        baseSalaryEditText = findViewById(R.id.baseSalaryEditText);
        expectedHoursEditText = findViewById(R.id.expectedHoursEditText);
        workingDaysPerWeekEditText = findViewById(R.id.workingDaysPerWeekEditText);
        departmentsSpinner = findViewById(R.id.departments_spinner);
        designationsSpinner = findViewById(R.id.designations_spinner);
        salaryStructureSpinner = findViewById(R.id.salary_structure_spinner);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);
        btnSelectHolidays = findViewById(R.id.btnSelectHolidays);

        // Initialize arrays
        departments = new ArrayList<>();
        departmentNames = new ArrayList<>();
        departmentIds = new ArrayList<>();
        designations = new ArrayList<>();
        designationsName = new ArrayList<>();
        designationsId = new ArrayList<>();
        workingDays = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        backeEmployeeButton.setOnClickListener(v -> finish());
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving employee");
        progressDialog.setCancelable(false);

        // Setup salary structure spinner
        String[] salaryStructureItems = {"Select Work Type", "Base Salary", "Per Hour"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, salaryStructureItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        salaryStructureSpinner.setAdapter(adapter);

        salaryStructureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    salaryStructureType = "";
                    baseSalary = 0;
                    overTimeRate = 0;
                    normalHourRate = 0;
                    baseSalaryEditText.setVisibility(View.GONE);
                    normalHourCostEditText.setVisibility(View.GONE);
                    overTimeHourCostEditText.setVisibility(View.GONE);
                    expectedHoursEditText.setVisibility(View.GONE);
                    workingDaysPerWeekEditText.setVisibility(View.GONE);
                    btnSelectHolidays.setVisibility(View.GONE);
                }else if (position == 1) { // Base Salary
                    salaryStructureType = "base salary";
                    baseSalaryEditText.setVisibility(View.VISIBLE);
                    normalHourCostEditText.setVisibility(View.VISIBLE);
                    overTimeHourCostEditText.setVisibility(View.VISIBLE);
                    expectedHoursEditText.setVisibility(View.VISIBLE);
                    workingDaysPerWeekEditText.setVisibility(View.VISIBLE);
                    btnSelectHolidays.setVisibility(View.VISIBLE);

                } else {
                    salaryStructureType = "per hour";
                    baseSalary = 0;
                    normalHourCostEditText.setVisibility(View.VISIBLE);
                    overTimeHourCostEditText.setVisibility(View.VISIBLE);
                    baseSalaryEditText.setVisibility(View.GONE);
                    expectedHoursEditText.setVisibility(View.VISIBLE);
                    workingDaysPerWeekEditText.setVisibility(View.VISIBLE);
                    btnSelectHolidays.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        departmentsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                departmentId = departmentIds.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        designationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                designationId = designationsId.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Holiday selection dialog
        btnSelectHolidays.setOnClickListener(v -> showHolidaySelectionDialog());

        btnAddEmployee.setOnClickListener(v -> {
            if (validateInputs()) {
                Employee employee = createEmployee();
                addNewEmployee(employee);
            }
        });
    }

    private void showHolidaySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Holidays");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_holiday_selection, null);
        CheckBox cbMonday = dialogView.findViewById(R.id.cbMonday);
        CheckBox cbTuesday = dialogView.findViewById(R.id.cbTuesday);
        CheckBox cbWednesday = dialogView.findViewById(R.id.cbWednesday);
        CheckBox cbThursday = dialogView.findViewById(R.id.cbThursday);
        CheckBox cbFriday = dialogView.findViewById(R.id.cbFriday);
        CheckBox cbSaturday = dialogView.findViewById(R.id.cbSaturday);
        CheckBox cbSunday = dialogView.findViewById(R.id.cbSunday);

        // Restore previous selections
        cbMonday.setChecked(holidaySelections[0]);
        cbTuesday.setChecked(holidaySelections[1]);
        cbWednesday.setChecked(holidaySelections[2]);
        cbThursday.setChecked(holidaySelections[3]);
        cbFriday.setChecked(holidaySelections[4]);
        cbSaturday.setChecked(holidaySelections[5]);
        cbSunday.setChecked(holidaySelections[6]);

        builder.setView(dialogView);

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Save selections
            holidaySelections[0] = cbMonday.isChecked();
            holidaySelections[1] = cbTuesday.isChecked();
            holidaySelections[2] = cbWednesday.isChecked();
            holidaySelections[3] = cbThursday.isChecked();
            holidaySelections[4] = cbFriday.isChecked();
            holidaySelections[5] = cbSaturday.isChecked();
            holidaySelections[6] = cbSunday.isChecked();
            updateWorkingDaysList();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateWorkingDaysList() {
        workingDays.clear();
        for (int i = 0; i < 7; i++) {
            Map<String, Integer> day = new HashMap<>();
            day.put("day_of_week", i + 1);
            day.put("is_working_day", holidaySelections[i] ? 0 : 1); // Checked = holiday (0), Unchecked = working day (1)
            workingDays.add(day);
        }
    }

    private boolean validateInputs() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String expectedHoursStr = expectedHoursEditText.getText().toString().trim();
        String workingDaysPerWeekStr = workingDaysPerWeekEditText.getText().toString().trim();

        // Existing validations for email, password, fullName, phone, department, designation, salary structure
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (str_word_count(fullName) < 2) {
            Toast.makeText(this, "Full name must contain at least two words", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!fullName.matches("^[\\p{L}\\s]+$")) {
            Toast.makeText(this, "Full name must contain only letters and spaces", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 8 || !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*") ||
                !password.matches(".*[^a-zA-Z0-9].*")) {
            Toast.makeText(this, "Password must be 8+ chars with uppercase, lowercase, number, and special char", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phone.isEmpty()) {
            Toast.makeText(this, "Phone number is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!phone.matches("^(059|056|052|050|051|053)\\d{7}$")) {
            Toast.makeText(this, "Phone number must be 10 digits starting with 059, 056, 052, 050, 051, or 053", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (departmentId == -1) {
            Toast.makeText(this, "Please select a department", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (designationId == -1) {
            Toast.makeText(this, "Please select a designation", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (salaryStructureType.isEmpty()) {
            Toast.makeText(this, "Please select a salary structure", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (salaryStructureType.equals("base salary")) {
            String baseSalaryStr = baseSalaryEditText.getText().toString().trim();
            String normalHourRateStr = normalHourCostEditText.getText().toString().trim();
            String overTimeRateStr = overTimeHourCostEditText.getText().toString().trim();

            if (baseSalaryStr.isEmpty()) {
                Toast.makeText(this, "Base salary is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                baseSalary = Double.parseDouble(baseSalaryStr);
                if (baseSalary <= 0) {
                    Toast.makeText(this, "Base salary must be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid base salary format", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (normalHourRateStr.isEmpty()) {
                Toast.makeText(this, "Normal hour rate is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                normalHourRate = Double.parseDouble(normalHourRateStr);
                if (normalHourRate <= 0) {
                    Toast.makeText(this, "Normal hour rate must be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid normal hour rate format", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (overTimeRateStr.isEmpty()) {
                Toast.makeText(this, "Overtime hour rate is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                overTimeRate = Double.parseDouble(overTimeRateStr);
                if (overTimeRate <= 0) {
                    Toast.makeText(this, "Overtime hour rate must be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid overtime hour rate format", Toast.LENGTH_SHORT).show();
                return false;
            }

        } else if (salaryStructureType.equals("per hour")) {
            String normalHourRateStr = normalHourCostEditText.getText().toString().trim();
            String overTimeRateStr = overTimeHourCostEditText.getText().toString().trim();
            if (normalHourRateStr.isEmpty()) {
                Toast.makeText(this, "Normal hour rate is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                normalHourRate = Double.parseDouble(normalHourRateStr);
                if (normalHourRate <= 0) {
                    Toast.makeText(this, "Normal hour rate must be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid normal hour rate format", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (overTimeRateStr.isEmpty()) {
                Toast.makeText(this, "Overtime hour rate is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                overTimeRate = Double.parseDouble(overTimeRateStr);
                if (overTimeRate <= 0) {
                    Toast.makeText(this, "Overtime hour rate must be greater than 0", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid overtime hour rate format", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Working hours and days validations
        if (!salaryStructureType.isEmpty()) {
            if (expectedHoursStr.isEmpty()) {
                Toast.makeText(this, "Expected hours per day is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                expectedHoursPerDay = Double.parseDouble(expectedHoursStr);
                if (expectedHoursPerDay <= 0 || expectedHoursPerDay > 24) {
                    Toast.makeText(this, "Expected hours must be between 0 and 24", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid expected hours format", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (workingDaysPerWeekStr.isEmpty()) {
                Toast.makeText(this, "Working days per week is required", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                workingDaysPerWeek = Integer.parseInt(workingDaysPerWeekStr);
                if (workingDaysPerWeek <= 0 || workingDaysPerWeek > 7) {
                    Toast.makeText(this, "Working days per week must be between 1 and 7", Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid working days per week format", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Validate holidays
            long selectedHolidaysCount = workingDays.stream()
                    .filter(day -> day.get("is_working_day") == 0) // Count holidays
                    .count();
            if (selectedHolidaysCount != (7 - workingDaysPerWeek)) {
                Toast.makeText(this, "Selected holidays must match " + (7 - workingDaysPerWeek), Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        // Status validation
        int selectedId = radioGroupStatus.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select a status", Toast.LENGTH_SHORT).show();
            return false;
        }
        RadioButton selectedRadioButton = findViewById(selectedId);
        selectedStatusValue = selectedRadioButton.getText().toString().toLowerCase().trim();

        return true;
    }

    private Employee createEmployee() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        double expectedHours = salaryStructureType.isEmpty() ? 0 : Double.parseDouble(expectedHoursEditText.getText().toString().trim());
        int workingDaysCount = salaryStructureType.isEmpty() ? 0 : Integer.parseInt(workingDaysPerWeekEditText.getText().toString().trim());
        return new Employee(
                email, password, fullName, phone, selectedStatusValue,
                Integer.parseInt(companyId), departmentId, designationId,
                baseSalary, salaryStructureType,
                normalHourRate, overTimeRate, expectedHours, workingDaysCount, workingDays
        );
    }

    private void addNewEmployee(Employee employee) {
        progressDialog.show();
        String add_new_employee_url = "http://10.0.2.2/worksync/add_employee.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, add_new_employee_url,
                response -> {
                    progressDialog.dismiss();
                    Log.d("Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        Toast.makeText(EmployeeAddActivity.this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
                            Intent intent = new Intent(EmployeeAddActivity.this, EmployeeAddedSuccessActivity.class);
                            intent.putExtra("fullName", employee.getFullname());
                            intent.putExtra("email", employee.getEmail());
                            intent.putExtra("phone", employee.getPhone());
                            intent.putExtra("password", employee.getPassword());
                            intent.putExtra("company_id",companyId);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(EmployeeAddActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(EmployeeAddActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_full_name", employee.getFullname());
                params.put("email", employee.getEmail());
                params.put("password", employee.getPassword());
                params.put("status", employee.getStatus());
                params.put("phone_number", employee.getPhone());
                params.put("company_id", String.valueOf(employee.getCompanyId()));
                params.put("department_id", String.valueOf(employee.getDepartmentId()));
                params.put("designation_id", String.valueOf(employee.getDesignationId()));
                params.put("salary_structure_type", employee.getSalaryStructureType());

                if (employee.getSalaryStructureType().equals("base salary")) {
                    params.put("base_salary", String.valueOf(employee.getBaseSalary()));
                    params.put("normal_hour_rate", String.valueOf(employee.getNormalHourRate()));
                    params.put("overtime_hour_rate", String.valueOf(employee.getOvertimeHourRate())); // إضافة Overtime Rate
                } else if (employee.getSalaryStructureType().equals("per hour")) {
                    params.put("normal_hour_rate", String.valueOf(employee.getNormalHourRate()));
                    params.put("overtime_hour_rate", String.valueOf(employee.getOvertimeHourRate()));
                }

                params.put("expected_hours_per_day", String.valueOf(employee.getRegularWorkingHour()));
                params.put("working_day_per_week", String.valueOf(employee.getWorkingDaysPerWeek()));

                try {
                    JSONArray workingDaysArray = new JSONArray();
                    for (Map<String, Integer> day : employee.getWorkingDays()) {
                        JSONObject dayObject = new JSONObject();
                        dayObject.put("day_of_week", day.get("day_of_week"));
                        dayObject.put("is_working_day", day.get("is_working_day"));
                        workingDaysArray.put(dayObject);
                    }
                    params.put("working_days", workingDaysArray.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void fetchAllDepartments() {
        String get_all_departments_url = "http://10.0.2.2/worksync/fetch_all_departments.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_departments_url,
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success") && !jsonObject.getString("message").equals("No departments found.")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            departmentNames.add("Choose Department");
                            departmentIds.add(-1);
                            for (int i = 0; i < data.length(); i++) {
                                String dep_name = data.getJSONObject(i).getString("name");
                                String dep_id = data.getJSONObject(i).getString("id");
                                departments.add(new Departments(dep_id, dep_name));
                                departmentNames.add(dep_name);
                                departmentIds.add(Integer.parseInt(dep_id));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(EmployeeAddActivity.this,
                                    android.R.layout.simple_spinner_item, departmentNames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            departmentsSpinner.setAdapter(adapter);
                        } else {
                            Toast.makeText(EmployeeAddActivity.this, "No departments found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(EmployeeAddActivity.this, "Error parsing departments", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(EmployeeAddActivity.this, "Error fetching departments", Toast.LENGTH_SHORT).show());
        requestQueue.add(stringRequest);
    }

    private void fetchAllDesignations() {
        String get_all_designations_url = "http://10.0.2.2/worksync/fetch_all_job_titiles.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_designations_url,
                response -> {
                    Log.d("Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success") && !jsonObject.getString("message").equals("No job_titles found.")) {
                            designationsName.add("Choose Designations");
                            designationsId.add(-1);
                            JSONArray data = jsonObject.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                String des_name = data.getJSONObject(i).getString("name");
                                String des_id = data.getJSONObject(i).getString("id");
                                designations.add(new Designations(des_id, des_name));
                                designationsName.add(des_name);
                                designationsId.add(Integer.parseInt(des_id));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(EmployeeAddActivity.this,
                                    android.R.layout.simple_spinner_item, designationsName);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            designationsSpinner.setAdapter(adapter);
                        } else {
                            Toast.makeText(EmployeeAddActivity.this, "No designations found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(EmployeeAddActivity.this, "Error parsing designations", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(EmployeeAddActivity.this, "Error fetching designations", Toast.LENGTH_SHORT).show());
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        departments.clear();
        designations.clear();
        departmentNames.clear();
        departmentIds.clear();
        designationsName.clear();
        designationsId.clear();
        fetchAllDesignations();
        fetchAllDepartments();
    }

    private int str_word_count(String str) {
        if (str == null || str.trim().isEmpty()) return 0;
        String[] words = str.trim().split("\\s+");
        return words.length;
    }
}