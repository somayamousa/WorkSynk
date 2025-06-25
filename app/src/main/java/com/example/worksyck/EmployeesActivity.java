package com.example.worksyck;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeesActivity extends AppCompatActivity {
    private FloatingActionButton addEmployeeButton;
    private String companyId;
    private TextView employeeNumberTextView;
    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private List<Employee> employeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees);

        employeeNumberTextView = findViewById(R.id.employeeNumber);
        recyclerView = findViewById(R.id.employeesRecyclerView);
        addEmployeeButton = findViewById(R.id.addEmployeeButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        employeeList = new ArrayList<>();
        adapter = new EmployeeAdapter(employeeList, this::showBottomSheetDialog);
        recyclerView.setAdapter(adapter);

        Intent intent = getIntent();
        companyId = intent.getStringExtra("company_id");

        addEmployeeButton.setOnClickListener(v -> {
            Intent addIntent = new Intent(EmployeesActivity.this, EmployeeAddActivity.class);
            addIntent.putExtra("company_id", companyId);
            startActivity(addIntent);
        });

        fetchEmployees();
    }

    private void showBottomSheetDialog(Employee employee) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_employee_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetView.findViewById(R.id.reset_password).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showResetPasswordDialog(employee);
        });

        bottomSheetView.findViewById(R.id.reset_device_code).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showResetDeviceDialog(employee);
        });

        bottomSheetView.findViewById(R.id.edit_employee).setOnClickListener(v -> {
            Toast.makeText(this, "Edit " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement edit employee logic here
        });

        bottomSheetView.findViewById(R.id.delete_employee).setOnClickListener(v -> {
            Toast.makeText(this, "Delete " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement delete employee logic here
        });

        bottomSheetView.findViewById(R.id.calculate_salary).setOnClickListener(v -> {
            Toast.makeText(this, "Calculate Salary for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement calculate salary logic here
        });

        bottomSheetView.findViewById(R.id.attendance_records).setOnClickListener(v -> {
            Toast.makeText(this, "View Attendance Records for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement attendance records logic here
        });

        bottomSheetView.findViewById(R.id.add_extra_salary).setOnClickListener(v -> {
            Toast.makeText(this, "Add Extra Salary for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement add extra salary logic here
        });

        bottomSheetView.findViewById(R.id.view_profile).setOnClickListener(v -> {
            Toast.makeText(this, "View Profile for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement view profile logic here
        });

        bottomSheetDialog.show();
    }

    private void showResetPasswordDialog(Employee employee) {
        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_reset_password);

        EditText newPasswordEditText = dialog.findViewById(R.id.new_password);
        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button resetButton = dialog.findViewById(R.id.reset_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        resetButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            if (newPassword.length() < 6) {
                newPasswordEditText.setError("Password must be at least 6 characters");
                return;
            }

            resetPassword(employee.getId(), newPassword, dialog);
        });

        dialog.show();
    }

    private void showResetDeviceDialog(Employee employee) {
        Dialog dialog = new Dialog(this, R.style.CustomDialogTheme);
        dialog.setContentView(R.layout.dialog_confirm_reset_device);

        Button cancelButton = dialog.findViewById(R.id.cancel_button);
        Button confirmButton = dialog.findViewById(R.id.confirm_button);

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        confirmButton.setOnClickListener(v -> {
            resetDeviceId(employee.getId(), dialog);
        });

        dialog.show();
    }

    private void resetPassword(String employeeId, String newPassword, Dialog dialog) {
        String url = "http://10.0.2.2/worksync/reset_employee_password.php";

        Map<String, String> params = new HashMap<>();
        params.put("employee_id", employeeId);
        params.put("new_password", newPassword);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EmployeesActivity", "Volley error: " + error.getMessage(), error);
                    Toast.makeText(this, "Error resetting password: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void resetDeviceId(String employeeId, Dialog dialog) {
        String url = "http://10.0.2.2/worksync/reset_employee_device.php";

        Map<String, String> params = new HashMap<>();
        params.put("employee_id", employeeId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
                            // Update the local employee list
                            for (Employee emp : employeeList) {
                                if (emp.getId().equals(employeeId)) {
                                    emp.setMacAddress("N/A");
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        } else if (status.equals("info")) {
                            // Device ID is already unassigned
                            dialog.dismiss();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("EmployeesActivity", "Volley error: " + error.getMessage(), error);
                    Toast.makeText(this, "Error resetting device ID: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void fetchEmployees() {
        if (companyId == null) {
            Toast.makeText(this, "Invalid company ID", Toast.LENGTH_SHORT).show();
            employeeNumberTextView.setText("Employee Number: 0");
            return;
        }

        String url = "http://10.0.2.2/worksync/fetch_all_employees.php?company_id=" + companyId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        String status = json.getString("status");
                        if (status.equals("success")) {
                            JSONArray data = json.getJSONArray("data");
                            employeeList.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject employee = data.getJSONObject(i);
                                Employee emp = new Employee(
                                        employee.getString("id"),
                                        employee.getString("fullname"),
                                        employee.getString("email"),
                                        employee.getString("department_name"),
                                        employee.getString("company_name"),
                                        employee.getString("designation_name"),
                                        employee.getString("employee_code")
                                );
                                emp.setPhone(employee.getString("phone"));
                                emp.setMacAddress(employee.has("android_id") && !employee.isNull("android_id") ? employee.getString("android_id") : "N/A");
                                emp.setStatus(employee.getString("status"));
                                if (employee.has("base_salary") && !employee.isNull("base_salary")) {
                                    emp.setBaseSalary(employee.getDouble("base_salary"));
                                } else {
                                    emp.setBaseSalary(0.0);
                                }
                                emp.setSalaryStructureType(employee.getString("salary_structure_type"));
                                emp.setNormalHourRate(employee.getDouble("normal_hour_rate"));
                                emp.setOvertimeHourRate(employee.getDouble("overtime_hour_rate"));
                                emp.setRegularWorkingHour(employee.getDouble("expected_hours_per_day"));
                                emp.setWorkingDaysPerWeek(employee.getInt("working_day_per_week"));
                                employeeList.add(emp);
                            }
                            adapter.notifyDataSetChanged();
                            employeeNumberTextView.setText("Employee Number: " + employeeList.size());
                        } else {
                            String message = json.getString("message");
                            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            employeeNumberTextView.setText("Employee Number: 0");
                        }
                    } catch (Exception e) {
                        Log.e("EmployeesActivity", "Error parsing response: " + e.getMessage(), e);
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        employeeNumberTextView.setText("Employee Number: 0");
                    }
                },
                error -> {
                    Log.e("EmployeesActivity", "Volley error: " + error.getMessage(), error);
                    Toast.makeText(this, "Error fetching employees: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    employeeNumberTextView.setText("Employee Number: 0");
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEmployees();
    }
}