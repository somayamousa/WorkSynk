package com.example.worksyck;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
public class AdditionalSalaryActivity extends AppCompatActivity {

    private Spinner spinnerDepartment;

    private ArrayList<Departments> departments;
    private ArrayList<String> departmentNames;
    private EditText editSearchEmployee;
    private static final String BASE_URL = "http://10.0.2.2/worksync/";
    private Spinner spinnerEmployees;
    private int company_id;
    private ArrayList<Employee> employeeList = new ArrayList<>();
    private RequestQueue requestQueue;
    private FloatingActionButton fab;
    private   LinearLayout emptyLayout;
    private    RecyclerView recyclerView;
    private boolean isSpinnerInitialized = false; // New flag
    private SalaryIncreaseAdapter adapter;
    private EditText editFilterIncreaseType;
    private Spinner spinnerFilterDurationType;
    private EditText editFilterStartDate;
    private Button btnApplyFilter, btnResetFilter;
    private List<SalaryIncrease> originalIncreaseList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_additional_salary);
        // Initialize Views
        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        editFilterIncreaseType = findViewById(R.id.editFilterIncreaseType);
        spinnerFilterDurationType = findViewById(R.id.spinnerFilterDurationType);
        editFilterStartDate = findViewById(R.id.editFilterStartDate);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        originalIncreaseList = new ArrayList<>();
        departments = new ArrayList<>();
        departmentNames = new ArrayList<>();
        spinnerEmployees = findViewById(R.id.spinnerEmployees);
        employeeList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);
        editSearchEmployee = findViewById(R.id.editSearchEmployee);
        fab = findViewById(R.id.fab);
        emptyLayout = findViewById(R.id.emptyLayout);
        recyclerView = findViewById(R.id.recyclerView);
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"All", "Permanent", "Temporary"});
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterDurationType.setAdapter(durationAdapter);
        editFilterStartDate.setOnClickListener(v -> showDatePicker(editFilterStartDate));

        btnApplyFilter.setOnClickListener(v -> applyFilter());

        btnResetFilter.setOnClickListener(v -> resetFilter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SalaryIncreaseAdapter(this, new ArrayList<>(), new SalaryIncreaseAdapter.OnSalaryIncreaseActionListener() {
            @Override
            public void onDeleteClicked(int increaseId, String employeeId) {
                showDeleteConfirmationDialog(increaseId, employeeId);
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
        // Add OnClickListener for fab to show salary increase dialog
        fab.setOnClickListener(v -> {
            Object selectedItem = spinnerEmployees.getSelectedItem();
            if (selectedItem instanceof Employee) {
                Employee selectedEmployee = (Employee) selectedItem;
                if (!selectedEmployee.getId().equals("-1")) {
                    showEmployeeSalaryIncreaseDialog(selectedEmployee);
                } else {
                    Toast.makeText(AdditionalSalaryActivity.this, "Please select an employee", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AdditionalSalaryActivity.this, "Please select an employee", Toast.LENGTH_SHORT).show();
            }
        });




        // Fetch The Company Id
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
        // Search functionality
        editSearchEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().toLowerCase().trim();
                ArrayList<Employee> filteredList = new ArrayList<>();
                filteredList.add(new Employee("-1", "choose employee", "", "", "", ""));
                for (Employee emp : employeeList) {
                    if (emp.getId().equals("-1")) continue;
                    if (emp.getFullname().toLowerCase().contains(query)) {
                        filteredList.add(emp);
                    }
                }
                EmployeeSpinnerAdapter adapter = new EmployeeSpinnerAdapter(AdditionalSalaryActivity.this, filteredList);
                spinnerEmployees.setAdapter(adapter);
                isSpinnerInitialized = true; // Set flag after adapter is set
                spinnerEmployees.setSelection(0);
            }
        });
        spinnerEmployees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialized && spinnerEmployees.getAdapter() != null && spinnerEmployees.isEnabled()) {
                    Object selectedItem = spinnerEmployees.getSelectedItem();
                    if (selectedItem instanceof Employee) {
                        Employee selectedEmployee = (Employee) selectedItem;
                        if (selectedEmployee.getId().equals("-1")) {
                            // Reset to initial state: hide RecyclerView, emptyLayout, and fab
                            adapter.setList(new ArrayList<>());
                            adapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.GONE);
                            fab.setVisibility(View.GONE);
                        } else {
                            fetchSalaryIncreases(selectedEmployee.getId());
                            fab.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        departmentNames.clear();
        departments.clear();
        isSpinnerInitialized = false; // Reset flag on resume
        FetchAllDepartments();
    }
    private void showEditDialog(SalaryIncrease increase) {
        // Inflate the dialog view
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_salary_increase, null);

        EditText etIncreaseAmount = dialogView.findViewById(R.id.etIncreaseAmount);
        EditText etIncreaseType = dialogView.findViewById(R.id.etIncreaseType);
        Spinner spinnerDurationType = dialogView.findViewById(R.id.spinnerDurationType);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        // Set initial values
        etIncreaseAmount.setText(String.valueOf(increase.getIncreaseAmount()));
        etIncreaseType.setText(increase.getIncreaseType());
        etStartDate.setText(increase.getStartDate());
        etEndDate.setText(increase.getEndDate());
        etNotes.setText(increase.getNotes());

        // إعداد Spinner للنوع durationType
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Permanent", "Temporary"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDurationType.setAdapter(adapter);

        // تعيين اختيار spinner حسب القيمة
        if ("Permanent".equalsIgnoreCase(increase.getDurationType())) {
            spinnerDurationType.setSelection(0);
            etEndDate.setVisibility(View.GONE);  // إخفاء تاريخ الانتهاء للنوع الدائم
        } else {
            spinnerDurationType.setSelection(1);
            etEndDate.setVisibility(View.VISIBLE);
        }

        // عند تغيير نوع الـ durationType نتحكم بعرض حقل endDate
        spinnerDurationType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // Permanent
                    etEndDate.setVisibility(View.GONE);
                    etEndDate.setText("");
                } else { // Temporary
                    etEndDate.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        // لجعل حقول التواريخ تظهر DatePicker عند الضغط عليها
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

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
                // Validation
                String amountStr = etIncreaseAmount.getText().toString().trim();
                String increaseTypeStr = etIncreaseType.getText().toString().trim();
                String durationTypeStr = spinnerDurationType.getSelectedItem().toString();
                String startDateStr = etStartDate.getText().toString().trim();
                String endDateStr = etEndDate.getText().toString().trim();
                String notesStr = etNotes.getText().toString().trim();
                if (amountStr.isEmpty()) {
                    etIncreaseAmount.setError("Please enter increase amount");
                    return;
                }


                if (increaseTypeStr.isEmpty()) {
                    etIncreaseType.setError("Please enter increase type");
                    return;
                }
                if (!Character.isLetter(increaseTypeStr.charAt(0))) {
                    etIncreaseType.setError("Increase type must start with a letter");
                    return;
                }

                if (startDateStr.isEmpty()) {
                    etStartDate.setError("Please select start date");
                    return;
                }

                if (durationTypeStr.equals("Temporary")) {
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
                        e.printStackTrace();
                        Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0){
                    etIncreaseAmount.setError("increase type must be greater than 0");
                    return;
                }
                increase.setIncreaseAmount(amount);
                increase.setIncreaseType(increaseTypeStr);
                increase.setDurationType(durationTypeStr);
                increase.setStartDate(startDateStr);
                increase.setEndDate(durationTypeStr.equals("Temporary") ? endDateStr : "");
                increase.setNotes(notesStr);
                updateSalaryIncrease(increase);
                dialog.dismiss();
            });
        });

        dialog.show();
    }
    private void updateSalaryIncrease(SalaryIncrease increase) {
        String url = "http://10.0.2.2/worksync/update_on_increase.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Update",response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean status = Boolean.parseBoolean(jsonObject.getString("status"));
                        String message = jsonObject.getString("message");

                        if (status) {
                            Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show();
                            fetchSalaryIncreases(String.valueOf(increase.getEmployeeId()));  // حدث القائمة بعد التعديل


                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Update error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Update failed: " + error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(increase.getId()));
                params.put("increase_amount", String.valueOf(increase.getIncreaseAmount()));
                params.put("increase_type", increase.getIncreaseType());
                params.put("durationType", increase.getDurationType());
                params.put("start_date", increase.getStartDate());
                params.put("end_date", increase.getEndDate());
                params.put("notes", increase.getNotes());
                params.put("employee_id",String.valueOf(increase.getEmployeeId()) );
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
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    deleteSalaryIncrease(increaseId, employeeId);
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_rounded_background);
            }
        });

        dialog.show();
    }


    private void FetchAllDepartments() {
        String get_all_departments_url = "http://10.0.2.2/worksync/fetch_all_departments.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_departments_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AdditionalSalaryActivity.this, android.R.layout.simple_spinner_item, departmentNames);
                    adapter.setDropDownViewResource(R.layout.department_spinner_item);
                    spinnerDepartment.setAdapter(adapter);
                    spinnerDepartment.setEnabled(!departmentNames.contains("No departments available"));
                    spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedDepartment = departmentNames.get(position);
                            if (selectedDepartment.equals("All Departments") || selectedDepartment.equals("No departments available")) {
                                fetchEmployees(null);
                            } else {
                                String departmentId = departments.get(position - 1).getId();
                                fetchEmployees(departmentId);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error:", String.valueOf(error));
            }
        });
        requestQueue.add(stringRequest);
    }

    private void showEmployeeSalaryIncreaseDialog(Employee employee) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_employee_info);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Initialize Dialog Views
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




        // Set Employee Data
        editEmployeeName.setText(employee.getFullname() != null ? employee.getFullname() : "");
        editEmployeeDesignation.setText(employee.getDesignation_name() != null ? employee.getDesignation_name() : "");
        editEmployeeDepartment.setText(employee.getDepartment_name() != null ? employee.getDepartment_name() : "");
        editEmployeeCompany.setText(employee.getCompany_name() != null ? employee.getCompany_name() : "");
        radioGroupType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioPermanent) {
                    editEndDate.setVisibility(View.GONE);
                } else if (checkedId == R.id.radioTemporary) {
                    editEndDate.setVisibility(View.VISIBLE);
                }
            }
        });
        editStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AdditionalSalaryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String month = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                                String day = String.format(Locale.getDefault(), "%02d", selectedDay);
                                String date = selectedYear + "-" + month + "-" + day;
                                editStartDate.setText(date);
                            }
                        }, year, month, day);
                datePickerDialog.show();

            }
        });
        editEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AdditionalSalaryActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                String month = String.format(Locale.getDefault(), "%02d", selectedMonth + 1);
                                String day = String.format(Locale.getDefault(), "%02d", selectedDay);
                                String date = selectedYear + "-" + month + "-" + day;
                                editEndDate.setText(date);
                            }
                        }, year, month, day);
                datePickerDialog.show();

            }
        });
        // Button Listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAddIncrease.setOnClickListener(v -> {
            String type = editIncreaseType.getText().toString().trim();
            String amount = editIncreaseAmount.getText().toString().trim();
            if (type.isEmpty()) {
                Toast.makeText(AdditionalSalaryActivity.this, "enter the increase type", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Character.isLetter(type.charAt(0))) {
                Toast.makeText(AdditionalSalaryActivity.this, "The type of increase must start with a letter.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (amount.isEmpty()) {
                Toast.makeText(AdditionalSalaryActivity.this, "enter the increase amount", Toast.LENGTH_SHORT).show();
                return;
            }
            String duration = "";
            String startDateStr = editStartDate.getText().toString().trim();
            String endDateStr = editEndDate.getText().toString().trim();
            String notes = editIncreaseNotes.getText().toString().trim();
            double amountValue=0;
            try {
                amountValue = Double.parseDouble(amount);
                if (amountValue <= 0) {
                    Toast.makeText(AdditionalSalaryActivity.this, "The increment amount must be greater than zero.", Toast.LENGTH_SHORT).show();
                    return;
                }


                int selectedId = radioGroupType.getCheckedRadioButtonId();
                if (selectedId == R.id.radioPermanent) {
                    duration = "Permanent";

                } else if (selectedId == R.id.radioTemporary) {
                    duration = "Temporary";
                } else {
                    Toast.makeText(AdditionalSalaryActivity.this, "Please select the duration type", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (startDateStr.isEmpty()) {
                    Toast.makeText(AdditionalSalaryActivity.this, "Please enter the start date of the increase.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (duration.equals("Temporary")) {
                    if (endDateStr.isEmpty()) {
                        Toast.makeText(AdditionalSalaryActivity.this, "Please enter the increase end date.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                try {
                    Date startDate = sdf.parse(startDateStr);
                    if (duration.equals("Temporary")) {
                        Date endDate = sdf.parse(endDateStr);
                        if (endDate.before(startDate)) {
                            Toast.makeText(AdditionalSalaryActivity.this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(AdditionalSalaryActivity.this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    return;
                }

            } catch (NumberFormatException e) {
                Toast.makeText(AdditionalSalaryActivity.this, "The increase amount must be a valid number.", Toast.LENGTH_SHORT).show();
                return;
            }
            addSalaryIncreaseToDatabase(employee.getId(),type,amountValue,duration,startDateStr,endDateStr,notes);
//            dialog.dismiss();
        });

        dialog.show();
    }
    private void addSalaryIncreaseToDatabase(String employeeId, String increaseType, double amount, String duration, String startDate, String endDate, String notes) {
        String url = BASE_URL + "add_salary_increase.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("type", increaseType);
                        Log.d("Response", response);
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");
                        if (status.equals("success")) {
                            Toast.makeText(AdditionalSalaryActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Refresh the salary increases list
                            fetchSalaryIncreases(employeeId);
                        } else {
                            Toast.makeText(AdditionalSalaryActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AdditionalSalaryActivity.this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(AdditionalSalaryActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
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
                            employeeList.add(new Employee("-1", "choose employee", "", "", "", ""));
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
                            EmployeeSpinnerAdapter adapter = new EmployeeSpinnerAdapter(this, employeeList);
                            spinnerEmployees.setAdapter(adapter);
                            isSpinnerInitialized = true; // Set flag even if no employees
                            spinnerEmployees.setEnabled(true);
                            spinnerEmployees.setSelection(0);

                        } else {
                            Log.d("No Employee Found", "No employee found");
                            ArrayList<String> noEmployeeList = new ArrayList<>();
                            noEmployeeList.add("No Employee Found");
                            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, noEmployeeList);
                            emptyAdapter.setDropDownViewResource(R.layout.department_spinner_item);
                            spinnerEmployees.setAdapter(emptyAdapter);

                            spinnerEmployees.setEnabled(false);
                            isSpinnerInitialized = true; // Set flag after adapter is set

                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                    }
                },
                error -> Log.e("Volley Error", error.toString())
        );
        requestQueue.add(request);
    }

    private void fetchSalaryIncreases(String employeeId) {
        String url = "http://10.0.2.2/worksync/get_salary_increases.php?employee_id=" + employeeId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("Increases Response", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyLayout.setVisibility(View.GONE);
                            fab.setVisibility(View.VISIBLE);
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
                                String notes = obj.optString("notes", "");
                                String created_at = obj.optString("created_at", "");
                                String updated_at = obj.optString("updated_at", "");
                                SalaryIncrease increase = new SalaryIncrease(
                                        id, Integer.parseInt(employeeId), increase_amount,
                                        increase_type, durationType, start_date, end_date,
                                        notes, created_at, updated_at
                                );
                                increaseList.add(increase);
                            }
                            originalIncreaseList.clear();
                            originalIncreaseList.addAll(increaseList);
                            adapter.setList(increaseList);
                            adapter.notifyDataSetChanged();
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            emptyLayout.setVisibility(View.VISIBLE);
                            fab.setVisibility(View.VISIBLE);
                            originalIncreaseList.clear();
                            adapter.setList(new ArrayList<>());
                            adapter.notifyDataSetChanged();
                            Toast.makeText(AdditionalSalaryActivity.this, "No salary increases found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(AdditionalSalaryActivity.this, "Load Data Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Error", error.toString());
                    Toast.makeText(AdditionalSalaryActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(stringRequest);
    }
    private void deleteSalaryIncrease(int increaseId,String employee_id) {
        String url = "http://10.0.2.2/worksync/delete_salary_increase.php";

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
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Delete failed: " + error.toString(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(increaseId));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void applyFilter() {
        String filterIncreaseType = editFilterIncreaseType.getText().toString().trim().toLowerCase();
        String filterDurationType = spinnerFilterDurationType.getSelectedItem().toString();
        String filterStartDate = editFilterStartDate.getText().toString().trim();

        List<SalaryIncrease> filteredList = new ArrayList<>();
        for (SalaryIncrease increase : originalIncreaseList) {
            boolean matches = true;

            if (!filterIncreaseType.isEmpty()) {
                if (!increase.getIncreaseType().toLowerCase().contains(filterIncreaseType)) {
                    matches = false;
                }
            }

            if (!filterDurationType.equals("All")) {
                if (!increase.getDurationType().equalsIgnoreCase(filterDurationType)) {
                    matches = false;
                }
            }

            if (!filterStartDate.isEmpty()) {
                if (!increase.getStartDate().equals(filterStartDate)) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(increase);
            }
        }

        adapter.setList(filteredList);
        adapter.notifyDataSetChanged();

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, "No salary increases match the filter.", Toast.LENGTH_SHORT).show();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }

    private void resetFilter() {
        editFilterIncreaseType.setText("");
        spinnerFilterDurationType.setSelection(0);
        editFilterStartDate.setText("");

        adapter.setList(originalIncreaseList);
        adapter.notifyDataSetChanged();

        if (originalIncreaseList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }
}