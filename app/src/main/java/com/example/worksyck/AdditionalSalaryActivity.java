package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdditionalSalaryActivity extends AppCompatActivity {

    private AutoCompleteTextView autocompleteDepartment, autocompleteEmployee;
    private ArrayList<Departments> departments;
    private ArrayList<String> departmentNames;
    private ArrayList<Employee> employeeList;
    private RequestQueue requestQueue;
    private boolean isAutoCompleteInitialized = false;
    private static final String BASE_URL = "http://10.0.2.2/worksync/";
    private int company_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_additional_salary);

        // Initialize Views
        autocompleteDepartment = findViewById(R.id.autocompleteDepartment);
        autocompleteEmployee = findViewById(R.id.autocompleteEmployee);
        MaterialButton btnProceed = findViewById(R.id.btnProceed);
        ImageView backButton = findViewById(R.id.backButton);

        departments = new ArrayList<>();
        departmentNames = new ArrayList<>();
        employeeList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        // Back Button
        backButton.setOnClickListener(v -> finish());

        // Fetch Company ID
        Intent intent = getIntent();
        String companyIdStr = intent.getStringExtra("company_id");
        int companyId = -1;
        if (companyIdStr != null) {
            try {
                companyId = Integer.parseInt(companyIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (companyId != -1) {
            company_id = companyId;
        } else {
            Log.d("Error", "No Company Id assigned For The Hr");
        }

        // Setup Department Autocomplete
        autocompleteDepartment.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDepartment = (String) parent.getItemAtPosition(position);
            if (selectedDepartment.equals("All Departments") || selectedDepartment.equals("No departments available")) {
                fetchEmployees(null);
            } else {
                String departmentId = departments.stream()
                        .filter(dep -> dep.getName().equals(selectedDepartment))
                        .findFirst()
                        .map(Departments::getId)
                        .orElse(null);
                fetchEmployees(departmentId);
            }
        });

        // Setup Employee Autocomplete
        autocompleteEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase().trim();
                ArrayList<String> filteredNames = new ArrayList<>();
                for (Employee emp : employeeList) {
                    if (emp.getFullname().toLowerCase().contains(query)) {
                        filteredNames.add(emp.getFullname());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AdditionalSalaryActivity.this,
                        android.R.layout.simple_dropdown_item_1line, filteredNames);
                autocompleteEmployee.setAdapter(adapter);
                isAutoCompleteInitialized = true;
            }
        });

        // Proceed Button
        btnProceed.setOnClickListener(v -> {
            String selectedEmployeeId = getSelectedEmployeeId();
            if (selectedEmployeeId != null) {
                String selectedEmployeeName = autocompleteEmployee.getText().toString();
                Intent intent1 = new Intent(AdditionalSalaryActivity.this, SalaryIncreasesActivity.class);
                intent1.putExtra("employee_id", selectedEmployeeId);
                intent1.putExtra("employee_name", selectedEmployeeName);
                intent1.putExtra("company_id", String.valueOf(company_id));
                startActivity(intent1);
            } else {
                Toast.makeText(AdditionalSalaryActivity.this, "Please select a valid employee", Toast.LENGTH_SHORT).show();
            }
        });

        FetchAllDepartments();
    }

    private String getSelectedEmployeeId() {
        String selectedEmployeeName = autocompleteEmployee.getText().toString();
        return employeeList.stream()
                .filter(emp -> emp.getFullname().equals(selectedEmployeeName))
                .findFirst()
                .map(Employee::getId)
                .orElse(null);
    }

    private void FetchAllDepartments() {
        String get_all_departments_url = BASE_URL + "fetch_all_departments.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_departments_url,
                response -> {
                    Log.d("Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");
                        if (status.equals("success") && !message.equals("No departments found.")) {
                            departmentNames.add("All Departments");
                            JSONArray data = jsonObject.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                String dep_name = data.getJSONObject(i).getString("name");
                                String dep_id = data.getJSONObject(i).getString("id");
                                departments.add(new Departments(dep_id, dep_name));
                                departmentNames.add(dep_name);
                            }
                        } else {
                            Log.d("Case 2 :", "No departments found.");
                            departmentNames.add("No departments available");
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_dropdown_item_1line, departmentNames);
                        autocompleteDepartment.setAdapter(adapter);
                        autocompleteDepartment.setEnabled(!departmentNames.contains("No departments available"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                },
                error -> Log.e("Error:", String.valueOf(error)));
        requestQueue.add(stringRequest);
    }

    private void fetchEmployees(String departmentId) {
        String url;
        if (departmentId == null) {
            url = BASE_URL + "fetch_all_employees.php?company_id=" + company_id;
        } else {
            url = BASE_URL + "fetch_employees_by_department.php?department_id=" + departmentId + "&company_id=" + company_id;
        }
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    employeeList.clear();
                    try {
                        Log.d("Response", response);
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success") && !jsonObject.getString("message").equals("No employee found")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                String id = obj.optString("id", "");
                                String full_name = obj.optString("fullname", "");
                                String email = obj.optString("email", "");
                                String department = obj.optString("department_name", "");
                                String company = obj.optString("company_name", "");
                                String designation = obj.optString("designation_name", "");
                                employeeList.add(new Employee(id, full_name, email, department, company, designation));
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line,
                                    employeeList.stream().map(Employee::getFullname).toList());
                            autocompleteEmployee.setAdapter(adapter);
                            isAutoCompleteInitialized = true;
                            autocompleteEmployee.setEnabled(true);
                        } else {
                            Log.d("No Employee Found", "No employee found");
                            ArrayList<String> noEmployeeList = new ArrayList<>();
                            noEmployeeList.add("No Employee Found");
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_dropdown_item_1line, noEmployeeList);
                            autocompleteEmployee.setAdapter(emptyAdapter);
                            autocompleteEmployee.setEnabled(false);
                            isAutoCompleteInitialized = true;
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                    }
                },
                error -> Log.e("Volley Error", error.toString()));
        requestQueue.add(request);
    }
}