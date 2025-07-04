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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
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
        Log.d("", companyId);
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
            Intent intent = new Intent(this, activity_update_employee.class);
            intent.putExtra("user_id", employee.getId());

            startActivity(intent);
        });

        bottomSheetView.findViewById(R.id.delete_employee).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Deactivate Employee")
                    .setMessage("Are you sure you want to deactivate " + employee.getFullname() + "?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deactivateEmployee(employee.getId());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        bottomSheetView.findViewById(R.id.calculate_salary).setOnClickListener(v -> {
            Toast.makeText(this, "Calculate Salary for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement calculate salary logic here
        });

        bottomSheetView.findViewById(R.id.attendance_records).setOnClickListener(v -> {
            Toast.makeText(this, "View Attendance Records for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, ShowAttendanceRecordManagerSide.class);
            intent.putExtra("user_id", employee.getId());
            intent.putExtra("email", employee.getEmail());
            intent.putExtra("fullname", employee.getFullname());
            intent.putExtra("company_id", employee.getCompanyId());
            startActivity(intent);
        });

        bottomSheetView.findViewById(R.id.add_extra_salary).setOnClickListener(v -> {
            Toast.makeText(this, "Add Extra Salary for " + employee.getFullname(), Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
            // Implement add extra salary logic here
        });

        bottomSheetView.findViewById(R.id.view_profile).setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(this, EmployeeProfileActivity.class);
            intent.putExtra("id", employee.getId());
            intent.putExtra("fullname", employee.getFullname());
            intent.putExtra("email", employee.getEmail());
            intent.putExtra("phone", employee.getPhone());
            intent.putExtra("department", employee.getDepartment_name());
            intent.putExtra("designation", employee.getDesignation_name());
            intent.putExtra("employee_code", employee.getEmployee_code());
            intent.putExtra("status", employee.getStatus());
            intent.putExtra("mac_address", employee.getMacAddress());
            intent.putExtra("base_salary", employee.getBaseSalary());
            startActivity(intent);
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
        String url = "http://192.168.1.108/worksync/reset_employee_password.php";

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

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void resetDeviceId(String employeeId, Dialog dialog) {
        String url = "http://192.168.1.108/worksync/reset_employee_device.php";

        Map<String, String> params = new HashMap<>();
        params.put("employee_id", employeeId);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (status.equals("success")) {
                            for (Employee emp : employeeList) {
                                if (emp.getId().equals(employeeId)) {
                                    emp.setMacAddress("N/A");
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                            dialog.dismiss();
                        } else if (status.equals("info")) {
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

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void deactivateEmployee(String employeeId) {
        String url = "http://192.168.1.108/worksync/deactivate_employee.php";

        // Debug logging
        Log.d("DeactivateEmployee", "Starting deactivation for employee ID: " + employeeId);
        Log.d("DeactivateEmployee", "URL: " + url);

        // Create JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("employee_id", employeeId);
            Log.d("DeactivateEmployee", "JSON Body: " + jsonBody.toString());
        } catch (JSONException e) {
            Log.e("DeactivateEmployee", "JSON creation error: " + e.getMessage());
            Toast.makeText(this, "Error creating request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("DeactivateEmployee", "Raw response: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        Log.d("DeactivateEmployee", "Status: " + status + ", Message: " + message);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                        if (status.equals("success")) {
                            // Remove employee from list
                            for (int i = 0; i < employeeList.size(); i++) {
                                if (employeeList.get(i).getId().equals(employeeId)) {
                                    employeeList.remove(i);
                                    Log.d("DeactivateEmployee", "Employee removed from list");
                                    break;
                                }
                            }
                            adapter.notifyDataSetChanged();
                            employeeNumberTextView.setText("Employee Number: " + employeeList.size());
                        }
                    } catch (JSONException e) {
                        Log.e("DeactivateEmployee", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Invalid response format", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("DeactivateEmployee", "Network error occurred");
                    String errorMessage = "Network error";
                    if (error.networkResponse != null) {
                        Log.e("DeactivateEmployee", "Status Code: " + error.networkResponse.statusCode);
                        errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                        if (error.networkResponse.data != null) {
                            try {
                                String errorBody = new String(error.networkResponse.data, "UTF-8");
                                Log.e("DeactivateEmployee", "Error Body: " + errorBody);
                                errorMessage += ": " + errorBody;
                            } catch (Exception e1) {
                                Log.e("DeactivateEmployee", "Error reading response: " + e1.getMessage());
                                errorMessage += ": " + e1.getMessage();
                            }
                        }
                    } else {
                        Log.e("DeactivateEmployee", "No network response: " + error.getMessage());
                        errorMessage += ": " + error.getMessage();
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                return jsonBody.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Log.d("DeactivateEmployee", "Adding request to queue");
        Volley.newRequestQueue(this).add(request);
    }


    private void fetchEmployees() {
        if (companyId == null) {
            Toast.makeText(this, "Invalid company ID", Toast.LENGTH_SHORT).show();
            employeeNumberTextView.setText("Employee Number: 0");
            return;
        }

        String url = "http://192.168.1.108/worksync/fetch_all_employees.php?company_id=" + companyId;

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
                                emp.setMacAddress(employee.optString("android_id", "N/A"));
                                emp.setStatus(employee.getString("status"));
                                emp.setBaseSalary(employee.optDouble("base_salary", 0.0));
                                emp.setSalaryStructureType(employee.getString("salary_structure_type"));
                                emp.setNormalHourRate(employee.getDouble("normal_hour_rate"));
                                emp.setOvertimeHourRate(employee.getDouble("overtime_hour_rate"));
                                emp.setRegularWorkingHour(employee.getDouble("expected_hours_per_day"));
                                emp.setWorkingDaysPerWeek(employee.getInt("working_day_per_week"));

                                // Only add active employees
                                if (emp.getStatus().equals("active")) {
                                    employeeList.add(emp);
                                }
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

        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEmployees();
    }
}
