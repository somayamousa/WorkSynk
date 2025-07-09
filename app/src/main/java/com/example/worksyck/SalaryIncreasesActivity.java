package com.example.worksyck;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalaryIncreasesActivity extends AppCompatActivity {

    private SearchView searchView;
    private FloatingActionButton fab, btnOpenFilters;
    private LinearLayout emptyLayout;
    private RecyclerView recyclerView;
    private SalaryIncreaseAdapter adapter;
    private List<SalaryIncrease> originalIncreaseList;
    private String employeeId;
    private Employee selectedEmployee;
    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://10.0.2.2/worksync/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salary_increases);

        // Initialize Views
        searchView = findViewById(R.id.searchView);
        btnOpenFilters = findViewById(R.id.btnOpenFilters);
        fab = findViewById(R.id.fab);
        emptyLayout = findViewById(R.id.emptyLayout);
        recyclerView = findViewById(R.id.recyclerView);
        ImageView backButton = findViewById(R.id.backButton);
        originalIncreaseList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        // Setup Back Button
        backButton.setOnClickListener(v -> finish());

        // Get Employee ID
        Intent intent = getIntent();
        employeeId = intent.getStringExtra("employee_id");
        if (employeeId == null || employeeId.isEmpty()) {
            Toast.makeText(this, "Invalid Employee ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchEmployeeDetails(employeeId);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalaryIncreaseAdapter(this, new ArrayList<>(), new SalaryIncreaseAdapter.OnSalaryIncreaseActionListener() {
            @Override
            public void onDeleteClicked(int increaseId, String empId) {
                showDeleteConfirmationDialog(increaseId, empId);
            }

            @Override
            public void onEditClicked(SalaryIncrease increase) {
                showEditDialog(increase);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);

        // Setup SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterByIncreaseType(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterByIncreaseType(newText);
                return true;
            }
        });

        // Setup Filter Button Click
        btnOpenFilters.setOnClickListener(v -> showFilterBottomSheet());

        // Setup FAB
        fab.setOnClickListener(v -> {
            if (selectedEmployee != null) {
                showEmployeeSalaryIncreaseDialog(selectedEmployee);
            } else {
                Toast.makeText(SalaryIncreasesActivity.this, "Employee data not loaded", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch Salary Increases
        fetchSalaryIncreases(employeeId);
    }

    private void fetchEmployeeDetails(String employeeId) {
        String url = BASE_URL + "get_spesfic_employee.php?employee_id=" + employeeId;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            String id = data.optString("id", "");
                            String full_name = data.optString("fullname", "");
                            String email = data.optString("email", "");
                            String department = data.optString("department_name", "");
                            String company = data.optString("company_name", "");
                            String designation = data.optString("designation_name", "");
                            selectedEmployee = new Employee(id, full_name, email, department, company, designation);
                        } else {
                            Toast.makeText(SalaryIncreasesActivity.this, "Failed to load employee details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                    }
                },
                error -> Log.e("Volley Error", error.toString()));
        requestQueue.add(request);
    }

    private void fetchSalaryIncreases(String employeeId) {
        String url = BASE_URL + "get_salary_increases.php?employee_id=" + employeeId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("Increases Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        Log.d("SalaryIncreases", "Status: " + status);
                        if (status.equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            List<SalaryIncrease> increaseList = new ArrayList<>();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);
                                int id = obj.optInt("id", -1);
                                double increase_amount = obj.optDouble("increase_amount", 0.0);
                                String increase_type = obj.optString("increase_type", "");
                                String durationType = obj.optString("durationType", "");
                                String start_date = obj.optString("start_date", "");
                                String end_date = obj.isNull("end_date") ? "" : obj.optString("end_date", "");
                                String notes = obj.isNull("notes") ? "No Notes" : obj.optString("notes", "No Notes");
                                if (notes.isEmpty()) {
                                    notes = "No Notes";
                                }
                                String created_at = obj.optString("created_at", "");
                                String updated_at = obj.optString("updated_at", "");
                                SalaryIncrease increase = new SalaryIncrease(
                                        id, Integer.parseInt(employeeId), increase_amount,
                                        increase_type, durationType, start_date, end_date,
                                        notes, created_at, updated_at
                                );
                                increaseList.add(increase);
                                Log.d("SalaryIncreases", "Item: ID=" + id + ", Amount=" + increase_amount + ", Type=" + increase_type + ", Notes=" + notes);
                            }
                            originalIncreaseList.clear();
                            originalIncreaseList.addAll(increaseList);
                            runOnUiThread(() -> {
                                adapter.setList(increaseList);
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyLayout.setVisibility(View.GONE);
                                fab.setVisibility(View.VISIBLE);
                                Log.d("SalaryIncreases", "List size: " + increaseList.size());
                                Log.d("SalaryIncreases", "RecyclerView Visibility: " + (recyclerView.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
                                Log.d("SalaryIncreases", "EmptyLayout Visibility: " + (emptyLayout.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
                            });
                        } else {
                            runOnUiThread(() -> {
                                adapter.setList(new ArrayList<>());
                                recyclerView.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                                fab.setVisibility(View.VISIBLE);
                                Log.d("SalaryIncreases", "No data, showing empty layout");
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                        runOnUiThread(() -> {
                            recyclerView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                            Toast.makeText(SalaryIncreasesActivity.this, "Load Data Error", Toast.LENGTH_SHORT).show();
                        });
                    }
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    runOnUiThread(() -> {
                        recyclerView.setVisibility(View.GONE);
                        emptyLayout.setVisibility(View.VISIBLE);
                        Toast.makeText(SalaryIncreasesActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                    });
                });

        requestQueue.add(stringRequest);
    }

    private void filterByIncreaseType(String query) {
        List<SalaryIncrease> filteredList = new ArrayList<>();
        for (SalaryIncrease increase : originalIncreaseList) {
            if (increase.getIncreaseType().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(increase);
            }
        }
        adapter.setList(filteredList);
        recyclerView.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
        emptyLayout.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
        Log.d("SalaryIncreases", "Filtered list size: " + filteredList.size());
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_filters_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Spinner spinnerFilterDurationType = bottomSheetView.findViewById(R.id.spinnerFilterDurationType);
        TextInputEditText editFilterStartDate = bottomSheetView.findViewById(R.id.editFilterStartDate);
        TextInputEditText editFilterEndDate = bottomSheetView.findViewById(R.id.editFilterEndDate);
        MaterialButton btnApplyFilter = bottomSheetView.findViewById(R.id.btnApplyFilter);
        MaterialButton btnResetFilter = bottomSheetView.findViewById(R.id.btnResetFilter);

        // Setup Duration Type Spinner
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"All", "Temporary", "Permanent"});
        spinnerFilterDurationType.setAdapter(durationAdapter);
        spinnerFilterDurationType.setSelection(0); // Default to "All"

        // Show/hide end date based on duration type
        spinnerFilterDurationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDuration = parent.getItemAtPosition(position).toString();
                if (selectedDuration.equals("Permanent") || selectedDuration.equals("All")) {
                    editFilterEndDate.setVisibility(View.GONE);
                    editFilterEndDate.setText("");
                } else {
                    editFilterEndDate.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup Date Pickers
        editFilterStartDate.setOnClickListener(v -> showDatePicker(editFilterStartDate));
        editFilterEndDate.setOnClickListener(v -> showDatePicker(editFilterEndDate));

        btnApplyFilter.setOnClickListener(v -> {
            String filterDurationType = spinnerFilterDurationType.getSelectedItem().toString();
            String filterStartDate = editFilterStartDate.getText().toString().trim();
            String filterEndDate = editFilterEndDate.getText().toString().trim();

            List<SalaryIncrease> filteredList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (SalaryIncrease increase : originalIncreaseList) {
                boolean matches = true;

                // Filter by Duration Type
                if (!filterDurationType.equals("All")) {
                    if (!increase.getDurationType().equalsIgnoreCase(filterDurationType)) {
                        matches = false;
                    }
                }

                // Filter by Start Date
                if (!filterStartDate.isEmpty()) {
                    try {
                        Date filterDate = sdf.parse(filterStartDate);
                        Date increaseStartDate = sdf.parse(increase.getStartDate());
                        if (filterDate != null && increaseStartDate != null) {
                            if (!sdf.format(filterDate).equals(sdf.format(increaseStartDate))) {
                                matches = false;
                            }
                        }
                    } catch (ParseException e) {
                        Log.e("Date Parse Error", e.getMessage());
                        matches = false;
                    }
                }

                // Filter by End Date (only for Temporary)
                if (filterDurationType.equals("Temporary") && !filterEndDate.isEmpty()) {
                    try {
                         filterEndDate = String.valueOf(sdf.parse(filterEndDate));
                        Date increaseEndDate = sdf.parse(increase.getEndDate());
                        if (filterEndDate != null && increaseEndDate != null) {
                            if (!sdf.format(filterEndDate).equals(sdf.format(increaseEndDate))) {
                                matches = false;
                            }
                        }
                    } catch (ParseException e) {
                        Log.e("Date Parse Error", e.getMessage());
                        matches = false;
                    }
                }

                if (matches) {
                    filteredList.add(increase);
                }
            }

            adapter.setList(filteredList);
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(filteredList.isEmpty() ? View.GONE : View.VISIBLE);
            emptyLayout.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
            Log.d("SalaryIncreases", "Filtered list size (Bottom Sheet): " + filteredList.size());
            bottomSheetDialog.dismiss();
        });

        btnResetFilter.setOnClickListener(v -> {
            spinnerFilterDurationType.setSelection(0); // Reset to "All"
            editFilterStartDate.setText("");
            editFilterEndDate.setText("");
            editFilterEndDate.setVisibility(View.GONE);
            adapter.setList(originalIncreaseList);
            adapter.notifyDataSetChanged();
            recyclerView.setVisibility(originalIncreaseList.isEmpty() ? View.GONE : View.VISIBLE);
            emptyLayout.setVisibility(originalIncreaseList.isEmpty() ? View.VISIBLE : View.GONE);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }





    private void showEditDialog(SalaryIncrease increase) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_salary_increase, null);

        EditText etIncreaseAmount = dialogView.findViewById(R.id.etIncreaseAmount);
        EditText etIncreaseType = dialogView.findViewById(R.id.etIncreaseType);
        Spinner spinnerDurationType = dialogView.findViewById(R.id.spinnerDurationType);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        TextInputLayout tilEndDate = dialogView.findViewById(R.id.tilEndDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        // Set up duration type spinner
        String[] durationTypes = {"Permanent", "Temporary"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durationTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDurationType.setAdapter(adapter);

        // Populate fields with existing data
        etIncreaseAmount.setText(String.valueOf(increase.getIncreaseAmount()));
        etIncreaseType.setText(increase.getIncreaseType());
        spinnerDurationType.setSelection("Permanent".equalsIgnoreCase(increase.getDurationType()) ? 0 : 1);
        etStartDate.setText(increase.getStartDate());
        etEndDate.setText(increase.getEndDate());
        etNotes.setText(increase.getNotes().equals("No Notes") ? "" : increase.getNotes());

        // Toggle end date visibility based on duration type
        if ("Permanent".equalsIgnoreCase(increase.getDurationType())) {
            tilEndDate.setVisibility(View.GONE);
        } else {
            tilEndDate.setVisibility(View.VISIBLE);
        }

        // Update end date visibility when duration type changes
        spinnerDurationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tilEndDate.setVisibility(durationTypes[position].equalsIgnoreCase("Permanent") ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Set up date pickers
        etStartDate.setOnClickListener(v -> showDatePicker(this, etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(this, etEndDate));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Salary Increase")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
        }

        dialog.setOnShowListener(d -> {
            Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                String amountStr = etIncreaseAmount.getText().toString().trim();
                String increaseTypeStr = etIncreaseType.getText().toString().trim();
                String durationTypeStr = spinnerDurationType.getSelectedItem().toString();
                String startDateStr = etStartDate.getText().toString().trim();
                String endDateStr = etEndDate.getText().toString().trim();
                String notesStr = etNotes.getText().toString().trim();
                if (notesStr.isEmpty()) {
                    notesStr = "No Notes";
                }

                // Validate increase amount
                if (amountStr.isEmpty()) {
                    etIncreaseAmount.setError("Please enter increase amount");
                    return;
                }
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        etIncreaseAmount.setError("Increase must be greater than 0");
                        return;
                    }
                } catch (NumberFormatException e) {
                    etIncreaseAmount.setError("Invalid amount format");
                    return;
                }

                // Validate increase type
                if (increaseTypeStr.isEmpty()) {
                    etIncreaseType.setError("Please enter increase type");
                    return;
                }
                if (!Character.isLetter(increaseTypeStr.charAt(0))) {
                    etIncreaseType.setError("Increase type must start with a letter");
                    return;
                }

                // Validate start date
                if (startDateStr.isEmpty()) {
                    etStartDate.setError("Please select start date");
                    return;
                }

                // Validate end date for temporary increases
                if (durationTypeStr.equalsIgnoreCase("Temporary")) {
                    if (endDateStr.isEmpty()) {
                        etEndDate.setError("Please select end date");
                        return;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        Date startDate = sdf.parse(startDateStr);
                        Date endDate = sdf.parse(endDateStr);
                        if (startDate != null && endDate != null && !startDate.before(endDate)) {
                            etEndDate.setError("End date must be after start date");
                            return;
                        }
                    } catch (ParseException e) {
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // Update SalaryIncrease object
                increase.setIncreaseAmount(amount);
                increase.setIncreaseType(increaseTypeStr);
                increase.setDurationType(durationTypeStr);
                increase.setStartDate(startDateStr);
                increase.setEndDate(durationTypeStr.equalsIgnoreCase("Temporary") ? endDateStr : null);
                increase.setNotes(notesStr);
                updateSalaryIncrease(increase);
                dialog.dismiss();
            });
        });

        dialog.show();
    }
    private void showDatePicker(Context context, EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            editText.setText(formattedDate);
        }, year, month, day).show();
    }

    private void updateSalaryIncrease(SalaryIncrease increase) {
        String url = BASE_URL + "update_on_increase.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Update", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean status = Boolean.parseBoolean(jsonObject.getString("status"));
                        String message = jsonObject.getString("message");

                        if (status) {
                            Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                            fetchSalaryIncreases(employeeId);
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Update error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Update failed: " + error.toString(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(increase.getId()));
                params.put("increase_amount", String.valueOf(increase.getIncreaseAmount()));
                params.put("increase_type", increase.getIncreaseType());
                params.put("durationType", increase.getDurationType());
                params.put("start_date", increase.getStartDate());
                params.put("notes", increase.getNotes());
                params.put("employee_id", String.valueOf(increase.getEmployeeId()));
                if (increase.getEndDate() != null && !increase.getEndDate().isEmpty()) {
                    params.put("end_date", increase.getEndDate());
                }
                Log.d("RequestParams", params.toString());
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    targetEditText.setText(date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showDeleteConfirmationDialog(int increaseId, String employeeId) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Confirmation")
                .setMessage("Are you sure you want to delete this salary increase?")
                .setPositiveButton("Yes", (dialogInterface, which) -> deleteSalaryIncrease(increaseId, employeeId))
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
            }
        });

        dialog.show();
    }

    private void deleteSalaryIncrease(int increaseId, String employeeId) {
        String url = BASE_URL + "delete_salary_increase.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(getApplicationContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                            boolean isEmpty = adapter.removeIncreaseById(increaseId);
                            if (isEmpty) {
                                recyclerView.setVisibility(View.GONE);
                                emptyLayout.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "Delete failed: " + error.toString(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(increaseId));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void showEmployeeSalaryIncreaseDialog(Employee employee) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_employee_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // تعديل حجم الدايلوج
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9); // 90% من عرض الشاشة
        params.height = WindowManager.LayoutParams.WRAP_CONTENT; // الارتفاع حسب المحتوى
        dialog.getWindow().setAttributes(params);

        TextView editEmployeeName = dialog.findViewById(R.id.textEmployeeName);
        TextView editEmployeeDesignation = dialog.findViewById(R.id.textEmployeeDesignation);
        TextView editEmployeeDepartment = dialog.findViewById(R.id.textEmployeeDepartment);
        TextView editEmployeeCompany = dialog.findViewById(R.id.textEmployeeCompany);
        TextInputEditText editIncreaseType = dialog.findViewById(R.id.editIncreaseType);
        TextInputEditText editIncreaseAmount = dialog.findViewById(R.id.editIncreaseAmount);
        EditText editIncreaseNotes = dialog.findViewById(R.id.editIncreaseNotes);
        Button btnAddIncrease = dialog.findViewById(R.id.btnAddIncrease);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        RadioGroup radioGroupType = dialog.findViewById(R.id.radioGroupType);
        RadioButton radioPermanent = dialog.findViewById(R.id.radioPermanent);
        RadioButton radioTemporary = dialog.findViewById(R.id.radioTemporary);
        EditText editEndDate = dialog.findViewById(R.id.editEndDate);
        EditText editStartDate = dialog.findViewById(R.id.editStartDate);

        editEmployeeName.setText(employee.getFullname() != null ? employee.getFullname() : "");
        editEmployeeDesignation.setText(employee.getDesignation_name() != null ? employee.getDesignation_name() : "");
        editEmployeeDepartment.setText(employee.getDepartment_name() != null ? employee.getDepartment_name() : "");
        editEmployeeCompany.setText(employee.getCompany_name() != null ? employee.getCompany_name() : "");

        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioPermanent) {
                editEndDate.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioTemporary) {
                editEndDate.setVisibility(View.VISIBLE);
            }
        });

        editStartDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SalaryIncreasesActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String monthStr = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                        String dayStr = String.format(Locale.getDefault(), "%02d", selectedDay);
                        String date = selectedYear + "-" + monthStr + "-" + dayStr;
                        editStartDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        editEndDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SalaryIncreasesActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String monthStr = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                        String dayStr = String.format(Locale.getDefault(), "%02d", selectedDay);
                        String date = selectedYear + "-" + monthStr + "-" + dayStr;
                        editEndDate.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddIncrease.setOnClickListener(v -> {
            String type = editIncreaseType.getText().toString().trim();
            String amount = editIncreaseAmount.getText().toString().trim();
            String notes = editIncreaseNotes.getText().toString().trim();
            if (notes.isEmpty()) {
                notes = "No Notes";
            }
            if (type.isEmpty()) {
                Toast.makeText(SalaryIncreasesActivity.this, "Enter the increase type", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Character.isLetter(type.charAt(0))) {
                Toast.makeText(SalaryIncreasesActivity.this, "The type of increase must start with a letter.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount.isEmpty()) {
                Toast.makeText(SalaryIncreasesActivity.this, "Enter the increase amount", Toast.LENGTH_SHORT).show();
                return;
            }
            String duration = "";
            String startDateStr = editStartDate.getText().toString().trim();
            String endDateStr = editEndDate.getText().toString().trim();
            double amountValue;
            try {
                amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    Toast.makeText(SalaryIncreasesActivity.this, "The increment amount must be greater than zero.", Toast.LENGTH_SHORT).show();
                    return;
                }

                int selectedId = radioGroupType.getCheckedRadioButtonId();
                if (selectedId == R.id.radioPermanent) {
                    duration = "Permanent";
                } else if (selectedId == R.id.radioTemporary) {
                    duration = "Temporary";
                } else {
                    Toast.makeText(SalaryIncreasesActivity.this, "Please select the duration type", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (startDateStr.isEmpty()) {
                    Toast.makeText(SalaryIncreasesActivity.this, "Please enter the start date of the increase.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (duration.equals("Temporary")) {
                    if (endDateStr.isEmpty()) {
                        Toast.makeText(SalaryIncreasesActivity.this, "Please enter the increase end date.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date startDate = sdf.parse(startDateStr);
                    if (duration.equals("Temporary")) {
                        Date endDate = sdf.parse(endDateStr);
                        if (endDate.before(startDate)) {
                            Toast.makeText(SalaryIncreasesActivity.this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (ParseException e) {
                    Toast.makeText(SalaryIncreasesActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(SalaryIncreasesActivity.this, "The increase amount must be a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }
            addSalaryIncreaseToDatabase(employeeId, type, amountValue, duration, startDateStr, endDateStr, notes);
            dialog.dismiss();
        });

        dialog.show();
    }
    private void addSalaryIncreaseToDatabase(String employeeId, String increaseType, double amount, String duration, String startDate, String endDate, String notes) {
        String url = BASE_URL + "add_salary_increase.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        if (status.equals("success")) {
                            Toast.makeText(SalaryIncreasesActivity.this, message, Toast.LENGTH_SHORT).show();
                            fetchSalaryIncreases(employeeId);
                        } else {
                            Toast.makeText(SalaryIncreasesActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(SalaryIncreasesActivity.this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(SalaryIncreasesActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("employee_id", employeeId);
                params.put("increase_type", increaseType);
                params.put("increase_amount", String.valueOf(amount));
                params.put("durationType", duration);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("notes", notes);
                return params;
            }
        };

        requestQueue.add(postRequest);
    }
}