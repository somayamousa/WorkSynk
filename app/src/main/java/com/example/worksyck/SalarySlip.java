package com.example.worksyck;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SalarySlip extends AppCompatActivity {
    private Button createSalarySlipBtn ,selectEmployeesBtn;
    private String startDate, endDate;
    private RequestQueue requestQueue;
    private String specialDayPolicy;
    private EditText holidayHourRateInput;
    private double holidayHourRate;
    private TextView startDateText, endDateText, holidayHourRateLabel;
    private RadioGroup employeeTypeGroup, salaryTypeGroup;
    private Spinner departmentSpinner;
    private int processedEmployees = 0;
    private int totalEmployeesToProcess = 0;


    private RadioButton radioAllEmployees, radioByDepartment, radioByEmployee;
    private ArrayList<Employee> employeeList = new ArrayList<Employee>();


    private ArrayList<String> departments = new ArrayList<>();
    private Set<Integer> selectedIds;
    private int company_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salary_slip);
        /**** Set Views*/
        startDateText = findViewById(R.id.startDateText);
        createSalarySlipBtn = findViewById(R.id.createSalarySlipBtn);
        holidayHourRateInput = findViewById(R.id.holidayHourRateInput);
        holidayHourRateLabel = findViewById(R.id.holidayHourRateLabel);
        selectEmployeesBtn = findViewById(R.id.selectEmployeesBtn);


        selectedIds = new HashSet<Integer>();
        /** initialized Parameters**/
        requestQueue = Volley.newRequestQueue(this);
        startDate = "";
        endDate = "";
        SharedPreferences sharedPreferences = getSharedPreferences("WorkHourPolicies", MODE_PRIVATE);
        int selectedHolidayPolicyId = sharedPreferences.getInt("holidayPolicy", R.id.holiday_normal); // default: normal
        specialDayPolicy = "normal"; // default
        if (selectedHolidayPolicyId == R.id.holiday_normal) {
            specialDayPolicy = "normal";
            holidayHourRateInput.setVisibility(View.GONE);
            holidayHourRateLabel.setVisibility(View.GONE);
        } else if (selectedHolidayPolicyId == R.id.holiday_overtime) {
            specialDayPolicy = "overtime";
            holidayHourRateInput.setVisibility(View.GONE);
            holidayHourRateLabel.setVisibility(View.GONE);
        } else if (selectedHolidayPolicyId == R.id.holiday_special) {
            specialDayPolicy = "custom_rate";
            holidayHourRateInput.setVisibility(View.VISIBLE);
            holidayHourRateLabel.setVisibility(View.VISIBLE);

        }
/**choose Start Date**/
        startDateText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(SalarySlip.this,
                    (view, year, month, dayOfMonth) -> {
                        String dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        startDateText.setText("Start: " + dateStr);
                        startDate = dateStr;
                        Log.d("Start Date", startDate);

                        // حساب endDate: بعد شهر من startDate
                        Calendar startCal = Calendar.getInstance();
                        startCal.set(year, month, dayOfMonth);

                        Calendar endCal = (Calendar) startCal.clone();
                        endCal.add(Calendar.MONTH, 1);

                        // ضمان أن اليوم لا يتجاوز نهاية الشهر الجديد
                        int startDay = startCal.get(Calendar.DAY_OF_MONTH);
                        int maxDayInEndMonth = endCal.getActualMaximum(Calendar.DAY_OF_MONTH);
                        endCal.set(Calendar.DAY_OF_MONTH, Math.min(startDay, maxDayInEndMonth));
                        endCal.add(Calendar.DATE, -1);


                        // Format endDate
                        String endDateStr = String.format("%04d-%02d-%02d",
                                endCal.get(Calendar.YEAR),
                                endCal.get(Calendar.MONTH) + 1,
                                endCal.get(Calendar.DAY_OF_MONTH));

                        endDate = endDateStr;
                        Log.d("End Date", endDateStr);

                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
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
        // Select Employees Button Listener
        selectEmployeesBtn.setOnClickListener(v -> {
            fetchEmployees(null, String.valueOf(company_id));
        });


//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        departmentSpinner.setAdapter(adapter);


///**choose End Date**/
//        endDateText.setOnClickListener(v -> {
//            Calendar calendar = Calendar.getInstance();
//            DatePickerDialog datePickerDialog = new DatePickerDialog(SalarySlip.this,
//                    (view, year, month, dayOfMonth) -> {
//                        String dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
//                        endDateText.setText("End: " + dateStr);
//                        endDate = dateStr;
//                        Log.d("End Date" , endDate);
////                    },
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH)
//            );
//            datePickerDialog.show();
//        });

        createSalarySlipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**Validation Fields**/
                // Check start date and end date are not empty
                if (startDate.isEmpty() || endDate.isEmpty()) {
                    Toast.makeText(SalarySlip.this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check that startDate is before endDate
                if (!isStartBeforeEnd(startDate, endDate)) {
                    Toast.makeText(SalarySlip.this, "Start date must be before end date", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Check that dates are not in the future
                if (isDateInFuture(startDate) || isDateInFuture(endDate)) {
                    Toast.makeText(SalarySlip.this, "Dates cannot be in the future", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (specialDayPolicy.equalsIgnoreCase("custom_rate")) {
                    String holidayRateStr = holidayHourRateInput.getText().toString().trim();
                    Log.d("DEBUG", "holidayRateStr = " + holidayRateStr);
                    if (holidayRateStr.isEmpty()) {
                        Toast.makeText(SalarySlip.this, "Enter the value of holiday rate or change the holiday policy.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    holidayHourRate = 0.0;
                    try {
                        holidayHourRate = Double.parseDouble(holidayRateStr);
                        Log.d("DEBUG", "holidayRateStr = " + holidayHourRate);
                        if (holidayHourRate <= 0) {
                            Toast.makeText(SalarySlip.this, "Holiday rate must be greater than zero.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Log.d("DEBUG", "holidayRateStr = " + e);
                        Toast.makeText(SalarySlip.this, "Invalid holiday rate value.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (selectedIds.isEmpty()) {
                    Toast.makeText(SalarySlip.this, "No employees selected", Toast.LENGTH_SHORT).show();
                    return;
                }
                processedEmployees = 0;
                totalEmployeesToProcess = selectedIds.size();
                for (Integer employeeId : selectedIds) {
                    fetchEmployeeDetails(employeeId);
                }
            }
        });
    }
    private void showEmployeeSelectionDialog(List<Employee> employeesList) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_employees, null);

        EditText searchInput = dialogView.findViewById(R.id.searchInput);
        RecyclerView recyclerView = dialogView.findViewById(R.id.employeesRecyclerView);
        TextView selectAllText = dialogView.findViewById(R.id.selectAllText);
        TextView unselectAllText = dialogView.findViewById(R.id.unselectAllText);
        List<Employee> originalList = new ArrayList<>(employeesList);

        // Pass selectedIds to the adapter
        EmployeeSelectionAdapter adapter = new EmployeeSelectionAdapter(employeesList, selectedIds);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Select All button click listener
        selectAllText.setOnClickListener(v -> {
            adapter.getSelectedEmployeeIds().clear(); // Clear current selections
            for (Employee employee : employeesList) {
                try {
                    adapter.getSelectedEmployeeIds().add(Integer.parseInt(employee.getId()));
                } catch (NumberFormatException e) {
                    Log.e("SelectAll", "Invalid employee ID: " + employee.getId());
                }
            }
            adapter.notifyDataSetChanged(); // Refresh UI
            Toast.makeText(SalarySlip.this, "All employees selected", Toast.LENGTH_SHORT).show();
            selectAllText.setEnabled(false); // Disable Select All
            unselectAllText.setEnabled(true); // Enable Unselect All
        });

        // Unselect All button click listener
        unselectAllText.setOnClickListener(v -> {
            adapter.getSelectedEmployeeIds().clear(); // Clear all selections
            adapter.notifyDataSetChanged(); // Refresh UI
            Toast.makeText(SalarySlip.this, "All employees unselected", Toast.LENGTH_SHORT).show();
            selectAllText.setEnabled(true); // Enable Select All
            unselectAllText.setEnabled(false); // Disable Unselect All
        });

        // Search input listener
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int after) {
                adapter.filter(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Employees")
                .setView(dialogView)
                .setPositiveButton("Confirm", (dialogInterface, which) -> {
                    selectedIds.clear(); // Clear previous selections
                    selectedIds.addAll(adapter.getSelectedEmployeeIds()); // Update with new selections
                    if (selectedIds.isEmpty()) {
                        Toast.makeText(SalarySlip.this, "Please select at least one employee", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d("Confirmed Employees", selectedIds.toString());
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> {
                    employeesList.clear();
                    employeesList.addAll(originalList);
                    Log.d("Canceled Selection", "Reverted to original selection.");
                    dialogInterface.dismiss();
                })
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_dialog);
            dialog.show();
        }}
    /******Check If An Date Is In Future*******/
    private boolean isDateInFuture(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = sdf.parse(dateStr);
            return date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    /*****Check If The Start Date Greater Than End Date****/
    private boolean isStartBeforeEnd(String start, String end) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date startDate = sdf.parse(start);
            Date endDate = sdf.parse(end);
            return startDate.before(endDate);
        } catch (ParseException e) {
            return false;
        }
    }

    private void fetchAllDepartments() {
        String url = "http://10.0.2.2/worksync/fetch_all_departments.php";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                departments.clear();
                departments.add("All Departments");
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equals("success")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dep = data.getJSONObject(i);
                        String id = dep.getString("id");
                        String name = dep.getString("name");
                        departments.add(name);
                    }
                } else {
                    Log.d("Departments", json.getString("message"));
//                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("JSON Error", e.toString());
            }
        }, error -> Log.e("Volley Error", error.toString()));
        requestQueue.add(request);
    }

    public void fetchAttendanceRecord(int employee_id, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, String salaryStructureType, double baseSalary, int workingDaysPerWeek) {
        String url = "http://10.0.2.2/worksync/fetch_employee_attendance_records.php" + "?employee_id=" + employee_id + "&start_date=" + startDate + "&end_date=" + endDate;
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Attendance Record", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");
                    if (status.equals("success")) {
                        if (!jsonObject.isNull("data")) {
                            JSONArray attendanceRecords = jsonObject.getJSONArray("data");
                            List<AttendanceRecord> attendanceList = new ArrayList<>();
                            double finalSalary = 0;
                            for (int i = 0; i < attendanceRecords.length(); i++) {
                                JSONObject record = attendanceRecords.getJSONObject(i);
                                String idStr = record.getString("id");
                                String userIdStr = record.getString("user_id");
                                String dateStr = record.getString("date");
                                String checkInStr = record.getString("start_time");
                                String checkOutStr = record.getString("end_time");
                                AttendanceRecord attendanceRecord = new AttendanceRecord(Integer.parseInt(idStr), Integer.parseInt(userIdStr), dateStr, checkInStr, checkOutStr);
                                attendanceList.add(attendanceRecord);
                            }
                            /****Fetch The Holidays ....***/
                            fetchHolidaysInPeriod(employee_id, attendanceList, expectedHoursPerDay, normalHourRate, overtimeHourRate, salaryStructureType, baseSalary,workingDaysPerWeek);
                        } else {
                            Log.d("Attendance", "No attendance records found.");
//                           PeriodSalary = 0;
//                            generateSalarySlip(..., PeriodSalary);
                            //handle there are no records for the employee does not work any day this period...
//                            return;
                        }
                    } else {
                        Log.e("Attendance Error", message);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /***Error**/
                Log.e("Attendance", "Volley error: " + error.toString());
            }
        });
        requestQueue.add(request);
    }

    private void fetchHolidaysInPeriod(int employeeId, List<AttendanceRecord> attendanceList, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, String salaryStructureType, double baseSalary, int workingDaysPerWeek) {
        String url = "http://10.0.2.2/worksync/get_holidays_InPeriod.php?start_date=" + startDate + "&end_date=" + endDate;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            Set<String> holidaysDates = new HashSet<>();
                            if (status.equals("success") && !message.equals("No holidays found")) {
                                JSONArray holidaysArray = jsonObject.getJSONArray("data");
                                for (int i = 0; i < holidaysArray.length(); i++) {
                                    JSONObject holiday = holidaysArray.getJSONObject(i);
                                    String date = holiday.getString("date");
                                    holidaysDates.add(date);
                                }
                                Log.d("holidays", holidaysDates.toString());
                            } else {
                                Log.d("Holidays", "No holidays found");
                            }
                            fetchSalaryIncreases(employeeId, expectedHoursPerDay, normalHourRate, overtimeHourRate, attendanceList, holidaysDates, salaryStructureType, baseSalary,workingDaysPerWeek);
                        } catch (JSONException e) {
                            Log.e("Holidays_JSONError", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Holidays_Error", error.toString());
                    }
                });
        requestQueue.add(stringRequest);
    }

    private void fetchEmployees(String departmentId, String company_id) {
        String url;
        if (departmentId == null) {
            url = "http://10.0.2.2/worksync/fetch_all_employees.php?company_id=" + company_id;
        } else {
            url = "http://10.0.2.2/worksync/fetch_employees_by_department.php?department_id=" + departmentId + "&company_id=" + company_id;
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
                                String employee_code = obj.getString("employee_code");

                                String designation = obj.optString("designation_name", "");
                                employeeList.add(new Employee(id, full_name, email, department, company, designation,employee_code));
                            }
                            showEmployeeSelectionDialog(employeeList);
                        } else {
                            Log.d("No Employee Found", "No employee found");
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                    }
                },
                error -> Log.e("Volley Error", error.toString())
        );
        requestQueue.add(request);
    }
    public int calculateExpectedWorkingDays(String startDate, String endDate, HashMap<Integer, Integer> workDay, Set<String> holidaysDates, List<Leave> paidLeaves, int workingDaysPerWeek) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // Ensure end date is not before start date
            if (end.before(start)) {
                Log.e("Error", "End date is before start date");
                return 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            int expectedWorkingDays = 0;

            // Iterate through each day in the period
            while (!calendar.getTime().after(end)) {
                String currentDateStr = sdf.format(calendar.getTime());
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Check if the day is a working day
                boolean isWorkingDay = workDay.get(dayOfWeek) != null && workDay.get(dayOfWeek) == 1;
                boolean isHoliday = holidaysDates.contains(currentDateStr);
                boolean isPaidLeave = false;

                // Check if the day is a paid leave
                for (Leave leave : paidLeaves) {
                    try {
                        Date leaveStart = sdf.parse(leave.getStartDate());
                        Date leaveEnd = sdf.parse(leave.getEndDate());
                        if (!calendar.getTime().before(leaveStart) && !calendar.getTime().after(leaveEnd)) {
                            if (leave.getIsPaid().trim().equalsIgnoreCase("1")) {
                                isPaidLeave = true;
                                break;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Count as a working day if:
                // 1. It's a working day and not a holiday, OR
                // 2. It's a paid leave day, OR
                // 3. It's a holiday but treated as a normal working day (based on specialDayPolicy)
                if (isPaidLeave || (isWorkingDay && (!isHoliday || specialDayPolicy.equalsIgnoreCase("normal")))) {
                    expectedWorkingDays++;
                }

                // Move to the next day
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Alternative: Estimate based on workingDaysPerWeek if workDay map is unavailable
            if (workDay.isEmpty()) {
                long totalDays = ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;
                int weeks = (int) Math.ceil((double) totalDays / 7);
                expectedWorkingDays = weeks * workingDaysPerWeek;
            }

            Log.d("ExpectedWorkingDays", "Total Expected Working Days: " + expectedWorkingDays);
            return expectedWorkingDays;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void fetchWorkingsDays(int employeeId, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, List<AttendanceRecord> attendanceList, Set<String> holidaysDates, String salaryStructureType, double baseSalary, double totalSalaryIncrease, List<SalaryIncrease> temporaryIncreases, List<SalaryIncrease> permanentIncreases, ArrayList<Leave> paidLeaves, int workingDaysPerWeek) {
        double totalPermanentIncrease = 0;
        for (SalaryIncrease increase : permanentIncreases) {
            totalPermanentIncrease += increase.getIncreaseAmount();
        }
        double totalTemporaryIncrease = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        for (SalaryIncrease increase : temporaryIncreases) {
            try {
                Date startDate = sdf.parse(increase.getStartDate());
                Date endDate = increase.getEndDate() == null || increase.getEndDate().equals("null") ? null : sdf.parse(increase.getEndDate());

                if (!today.before(startDate) && (endDate == null || !today.after(endDate))) {
                    totalTemporaryIncrease += increase.getIncreaseAmount();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Log.d("SalaryIncrease", "Total Permanent Increase: " + totalPermanentIncrease);
        Log.d("SalaryIncrease", "Total Temporary Increase: " + totalTemporaryIncrease);
        Log.d("Debug", "specialDayPolicy = " + specialDayPolicy);
        Log.d("Hi Fetch Workings Days", "Hi");
        {
            String url = "http://10.0.2.2/worksync/get_working_days.php?employee_id=" + employeeId;
            Log.d("Hi Fetch Workings Days", "Hi");
            double finalTotalTemporaryIncrease = totalTemporaryIncrease;
            double finalTotalPermanentIncrease = totalPermanentIncrease;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Hi Fetch Workings Days", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String status = jsonObject.getString("status");
                                if (status.equals("success")) {
                                    if (!jsonObject.getString("message").equals("No working days found")) {
                                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                                        HashMap<Integer, Integer> workDay = new HashMap<>();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            int dayOfWeek = Integer.parseInt(jsonArray.getJSONObject(i).getString("day_of_week"));
                                            int isWorkDay = Integer.parseInt(jsonArray.getJSONObject(i).getString("is_working_day"));
                                            workDay.put(dayOfWeek, isWorkDay);
                                        }
                                        // Inside the onResponse method, after populating workDay
                                        int expectedWorkingDays = calculateExpectedWorkingDays(startDate, endDate, workDay, holidaysDates, paidLeaves, workingDaysPerWeek);
                                        Log.d("ExpectedWorkingDays", "Employee ID: " + employeeId + " | Expected Working Days: " + expectedWorkingDays);
                                        // Calculate absent days
                                        int absentDays = calculateAbsentDays(startDate, endDate, workDay, holidaysDates, paidLeaves, attendanceList, workingDaysPerWeek);
                                        Log.d("AbsentDays", "Employee ID: " + employeeId + " | Absent Days: " + absentDays);
                                        switch (salaryStructureType.toLowerCase()) {
                                            case "per hour": {
                                                double finalSalary = 0;
                                                double DayRate = 0;
                                                double totalWorkingHours = 0;
                                                double totalOvertimeHours = 0;
                                                double totalNormalHours = 0;
                                                double totalNormalHourSalary = 0;
                                                double totalOvertimeSalary = 0;

                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                                                for (int i = 0; i < attendanceList.size(); i++) {
                                                    AttendanceRecord record = attendanceList.get(i);
                                                    double totalWorkedHourPerDay = record.getWorkedHours();
                                                    totalWorkingHours += totalWorkedHourPerDay;

                                                    try {
                                                        String dateStr = record.getDate();
                                                        Date date = sdf.parse(dateStr);
                                                        Calendar calendar = Calendar.getInstance();
                                                        calendar.setTime(date);
                                                        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                                                        boolean isWeekend = workDay.get(dayOfWeek) != null && workDay.get(dayOfWeek) == 0;
                                                        boolean isHoliday = holidaysDates.contains(dateStr);
                                                        boolean isSpecialDay = isHoliday || isWeekend;

                                                        boolean isPaidLeave = false;
                                                        for (Leave leave : paidLeaves) {
                                                            try {
                                                                Date leaveStart = sdf.parse(leave.getStartDate());
                                                                Date leaveEnd = sdf.parse(leave.getEndDate());
                                                                if (!date.before(leaveStart) && !date.after(leaveEnd)) {
                                                                    if (leave.getIsPaid().trim().equalsIgnoreCase("1")) {
                                                                        isPaidLeave = true;
                                                                        break;
                                                                    }
                                                                }
                                                            } catch (ParseException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }

                                                        double normalWorkedHour = 0;
                                                        double overTimeWorkedHourPerDay = 0;

                                                        if (isPaidLeave) {
                                                            DayRate = expectedHoursPerDay * normalHourRate;
                                                            normalWorkedHour = expectedHoursPerDay; // Count paid leave as normal hours
                                                            totalNormalHours += normalWorkedHour;
                                                            totalNormalHourSalary += DayRate;
                                                        } else if (!isSpecialDay || specialDayPolicy.equalsIgnoreCase("normal")) {
                                                            // Calculate normal and overtime hours
                                                            if (totalWorkedHourPerDay > expectedHoursPerDay) {
                                                                normalWorkedHour = expectedHoursPerDay;
                                                                overTimeWorkedHourPerDay = totalWorkedHourPerDay - expectedHoursPerDay;
                                                            } else {
                                                                normalWorkedHour = totalWorkedHourPerDay;
                                                                overTimeWorkedHourPerDay = 0;
                                                            }

                                                            totalNormalHours += normalWorkedHour;
                                                            totalOvertimeHours += overTimeWorkedHourPerDay;

                                                            double normalSalary = normalWorkedHour * normalHourRate;
                                                            double overtimeSalary = overTimeWorkedHourPerDay * overtimeHourRate;

                                                            totalNormalHourSalary += normalSalary;
                                                            totalOvertimeSalary += overtimeSalary;

                                                            DayRate = normalSalary + overtimeSalary;
                                                        } else if (specialDayPolicy.equalsIgnoreCase("overtime")) {
                                                            DayRate = totalWorkedHourPerDay * overtimeHourRate;
                                                            totalOvertimeHours += totalWorkedHourPerDay;
                                                            totalOvertimeSalary += DayRate;
                                                        } else if (specialDayPolicy.equalsIgnoreCase("custom_rate")) {
                                                            DayRate = totalWorkedHourPerDay * normalHourRate * holidayHourRate;
                                                            totalNormalHours += totalWorkedHourPerDay; // Treat as normal hours for tracking
                                                            totalNormalHourSalary += DayRate;
                                                            Log.d("Holiday rate", String.valueOf(holidayHourRate));
                                                        }

                                                        if (isSpecialDay) {
                                                            Log.d("Special Day", "Date: " + dateStr + " | Policy: " + specialDayPolicy);
                                                        }

                                                        finalSalary += DayRate;

                                                        Log.d("Daily Rate", "Date: " + dateStr + " | Worked: " + totalWorkedHourPerDay + " | Rate: " + DayRate);

                                                    } catch (Exception e) {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                                finalSalary += totalSalaryIncrease;
                                                Log.d("Salary", "Employee ID: " + employeeId + " | Final Salary: " + finalSalary);
                                                Log.d("totalSalaryIncrease", "Employee ID: " + employeeId + " | totalSalaryIncrease: " + totalSalaryIncrease);
                                                Log.d("Total Working Hours", "Employee ID: " + employeeId + " | Total Hours: " + totalWorkingHours);
                                                Log.d("Total Overtime Hours", "Employee ID: " + employeeId + " | Total Overtime: " + totalOvertimeHours);
                                                Log.d("Summary", "Total Normal Hours: " + totalNormalHours + " | Salary: $" + totalNormalHourSalary);
                                                Log.d("Summary", "Total Overtime Hours: " + totalOvertimeHours + " | Overtime Salary: $" + totalOvertimeSalary);
                                                Log.d("Summary", "Total Salary Increase: $" + totalSalaryIncrease);
                                                Log.d("Summary", "Final Salary (including increases): $" + finalSalary);
                                                StoreSalaryInDataBase_per_hour(
                                                        SalarySlip.this,
                                                        employeeId,
                                                        finalSalary,
                                                        startDate,
                                                        endDate,
                                                        finalTotalPermanentIncrease,
                                                        finalTotalTemporaryIncrease,
                                                        totalNormalHours,
                                                        normalHourRate,
                                                        totalNormalHourSalary,
                                                        totalOvertimeHours,
                                                        overtimeHourRate,
                                                        totalOvertimeSalary,salaryStructureType,expectedWorkingDays,absentDays
                                                );
                                            }
                                            break;
                                            case "base salary": {
                                                double totalMissingHours = 0;
                                                double totalOvertimeHours = 0;
                                                double totalMissingRate = 0;
                                                double totalOvertimeRate = 0;
                                                List<String> absentDates = new ArrayList<>();
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                                Date start = null;
                                                Date end = null;
                                                try {
                                                    start = sdf.parse(startDate);
                                                    end = sdf.parse(endDate);
                                                } catch (ParseException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                Calendar cal = Calendar.getInstance();
                                                cal.setTime(start);
                                                while (!cal.getTime().after(end)) {
                                                    Date currentDate = cal.getTime();
                                                    String dateStr = sdf.format(currentDate);
                                                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                                                    boolean isWorkDay = workDay.get(dayOfWeek) != null && workDay.get(dayOfWeek) == 1;
                                                    boolean isHoliday = holidaysDates.contains(dateStr);
                                                    boolean isWeekend = !isWorkDay;
                                                    boolean isPresent = false;
                                                    boolean isPaidLeave = false;
                                                    for (Leave leave : paidLeaves) {
                                                        try {
                                                            Date leaveStart = sdf.parse(leave.getStartDate());
                                                            Date leaveEnd = sdf.parse(leave.getEndDate());
                                                            if (!currentDate.before(leaveStart) && !currentDate.after(leaveEnd)) {
                                                                if (leave.getIsPaid().trim().equalsIgnoreCase("1")) {
                                                                    isPaidLeave = true;
                                                                    break;
                                                                }
                                                            }
                                                        } catch (ParseException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    AttendanceRecord todayRecord = null;
                                                    for (AttendanceRecord record : attendanceList) {
                                                        if (record.getDate().equals(dateStr)) {
                                                            isPresent = true;
                                                            todayRecord = record;
                                                            break;
                                                        }
                                                    }

                                                    double workedHours = isPresent ? todayRecord.getWorkedHours() : 0;

                                                    if (isWorkDay && !isHoliday) {
                                                        if (isPresent) {
                                                            double diff = workedHours - expectedHoursPerDay;
                                                            if (diff > 0) {
                                                                totalOvertimeHours += diff;
                                                                totalOvertimeRate += diff * overtimeHourRate;
                                                            } else if (diff < 0) {
                                                                double missing = -diff;
                                                                totalMissingHours += missing;
                                                                totalMissingRate += missing * normalHourRate;
                                                            }
                                                        } else {
                                                            if (!isPaidLeave) {
                                                                totalMissingHours += expectedHoursPerDay;
                                                                totalMissingRate += expectedHoursPerDay * normalHourRate;
                                                                absentDates.add(dateStr);
                                                            }
                                                        }
                                                    } else if (isHoliday || isWeekend) {
                                                        if (isPresent) {
                                                            switch (specialDayPolicy.trim()) {
                                                                case "normal": {
                                                                    double diff = workedHours - expectedHoursPerDay;
                                                                    if (diff > 0) {
                                                                        totalOvertimeHours += diff;
                                                                        totalOvertimeRate += diff * overtimeHourRate;
                                                                    } else if (diff < 0) {
                                                                        double missing = -diff;
                                                                        totalMissingHours += missing;
                                                                        totalMissingRate += missing * normalHourRate;
                                                                    }
                                                                    break;
                                                                }
                                                                case "overtime": {
                                                                    totalOvertimeHours += workedHours;
                                                                    totalOvertimeRate += workedHours * holidayHourRate;
                                                                    break;
                                                                }
                                                                case "custom_rate": {
                                                                    totalOvertimeHours += workedHours;
                                                                    totalOvertimeRate += workedHours * holidayHourRate * normalHourRate;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    } else if (isWorkDay && isHoliday) {
                                                        if (isPresent) {
                                                            switch (specialDayPolicy.trim()) {
                                                                case "normal": {
                                                                    double diff = workedHours - expectedHoursPerDay;
                                                                    if (diff > 0) {
                                                                        totalOvertimeHours += diff;
                                                                        totalOvertimeRate += diff * overtimeHourRate;
                                                                    } else if (diff < 0) {
                                                                        double missing = -diff;
                                                                        totalMissingHours += missing;
                                                                        totalMissingRate += missing * normalHourRate;
                                                                    }
                                                                    break;
                                                                }
                                                                case "overtime": {
                                                                    totalOvertimeHours += workedHours;
                                                                    totalOvertimeRate += workedHours * overtimeHourRate;
                                                                    break;
                                                                }
                                                                case "custom_rate": {
                                                                    totalOvertimeHours += workedHours;
                                                                    totalOvertimeRate += workedHours * holidayHourRate * normalHourRate;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }

                                                    cal.add(Calendar.DAY_OF_MONTH, 1);
                                                }

                                                double periodSalary = baseSalary - totalMissingRate + totalOvertimeRate + totalSalaryIncrease;

                                                Log.d("Final Salary", "Final Salary = " + periodSalary);
                                                Log.d("OverTime Rate", " = " + totalOvertimeRate);
                                                Log.d("Missing Rate", " = " + totalMissingRate);
                                                Log.d("Total Salary Increase ", String.valueOf(totalSalaryIncrease));
                                                Log.d("Absent Dates", "Absent Days: " + absentDates.toString());
                                                Log.d("Base Salary", "Base Salary = " + baseSalary);
                                                Log.d("Expected Working Days", "Expected Working Days = " + expectedWorkingDays);
                                                Log.d("Absent Days", "Absent Days Count = " + absentDays);

                                                if (periodSalary < 0) {
                                                    Log.w("SalaryCalc", "Salary is negative! Adjusting to zero. Employee ID: " + employeeId);
                                                    periodSalary = 0;
                                                }

                                                // Store the salary in the database using the correct method for base salary
                                                StoreSalaryInDataBase_base_salary(
                                                        SalarySlip.this,
                                                        employeeId,
                                                        periodSalary, // netSalary
                                                        baseSalary,   // baseSalary
                                                        startDate,
                                                        endDate,
                                                        finalTotalPermanentIncrease,
                                                        finalTotalTemporaryIncrease,
                                                        expectedWorkingDays,
                                                        absentDays,
                                                        normalHourRate,
                                                        totalOvertimeRate, // overtimeSalary
                                                        overtimeHourRate,
                                                        salaryStructureType ,totalOvertimeHours
                                                );

                                                break;
                                            }
                                            default: {
                                                Log.e("SalaryType", "Unknown salary structure type: " + salaryStructureType);
                                            }
                                            break;
                                        }
                                    } else {
                                        Log.d("Message", jsonObject.getString("message"));
                                    }
                                } else {
                                    Log.d("WorkDays", jsonObject.getString("message"));
                                }
                            } catch (JSONException e) {
                                Log.e("WorkDays_JSONError", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("WorkDays_Error", error.toString());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("employee_id", String.valueOf(employeeId));
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        }
    }

    private int calculateAbsentDays(String startDate, String endDate, HashMap<Integer, Integer> workDay, Set<String> holidaysDates, List<Leave> paidLeaves, List<AttendanceRecord> attendanceList, int workingDaysPerWeek) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            // Ensure end date is not before start date
            if (end.before(start)) {
                Log.e("Error", "End date is before start date");
                return 0;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            int absentDays = 0;

            // Iterate through each day in the period
            while (!calendar.getTime().after(end)) {
                String currentDateStr = sdf.format(calendar.getTime());
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // Check if the day is a working day
                boolean isWorkingDay = workDay.get(dayOfWeek) != null && workDay.get(dayOfWeek) == 1;
                boolean isHoliday = holidaysDates.contains(currentDateStr);
                boolean isPaidLeave = false;

                // Check if the day is a paid leave
                for (Leave leave : paidLeaves) {
                    try {
                        Date leaveStart = sdf.parse(leave.getStartDate());
                        Date leaveEnd = sdf.parse(leave.getEndDate());
                        if (!calendar.getTime().before(leaveStart) && !calendar.getTime().after(leaveEnd)) {
                            if (leave.getIsPaid().trim().equalsIgnoreCase("1")) {
                                isPaidLeave = true;
                                break;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                // Check if the employee was present (has attendance record)
                boolean isPresent = false;
                for (AttendanceRecord record : attendanceList) {
                    if (record.getDate().equals(currentDateStr)) {
                        isPresent = true;
                        break;
                    }
                }

                // Count as absent if:
                // 1. It's a working day
                // 2. Not a holiday (or holiday is treated as normal per policy)
                // 3. Not a paid leave
                // 4. No attendance record
                if (isWorkingDay && (!isHoliday || specialDayPolicy.equalsIgnoreCase("normal")) && !isPaidLeave && !isPresent) {
                    absentDays++;
                }

                // Move to the next day
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            // Alternative: Estimate based on workingDaysPerWeek if workDay map is unavailable
            if (workDay.isEmpty()) {
                int expectedWorkingDays = calculateExpectedWorkingDays(startDate, endDate, workDay, holidaysDates, paidLeaves, workingDaysPerWeek);
                int attendedDays = attendanceList.size();
                absentDays = Math.max(0, expectedWorkingDays - attendedDays);
            }

            Log.d("AbsentDays", "Total Absent Days: " + absentDays);
            return absentDays;

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }




    private void StoreSalaryInDataBase_base_salary(Context context, int employeeId, double netSalary, double baseSalary, String startDate, String endDate, double finalTotalPermanentIncrease, double finalTotalTemporaryIncrease, int expectedWorkingDays, int absentDays, double normalHourRate, double overtimeSalary, double overtimeHourRate, String salaryStructureType , double overtimeHours) {
        LocalDate end = LocalDate.parse(endDate);
        int month = end.getMonthValue();
        int year = end.getYear();
        String url = "http://10.0.2.2/worksync/salary_drafts.php";
        Map<String, String> params = new HashMap<>();
        params.put("employee_id", String.valueOf(employeeId));
        params.put("net_salary", String.valueOf(netSalary));
        params.put("base_salary", String.valueOf(baseSalary)); // Store base salary
        params.put("period_start", startDate);
        params.put("period_end", endDate);
        params.put("month", String.valueOf(month));
        params.put("year", String.valueOf(year));
        params.put("bonus", String.valueOf(finalTotalTemporaryIncrease)); // Temporary increase
        params.put("salary_increment", String.valueOf(finalTotalPermanentIncrease)); // Permanent increase
        params.put("salary_structure_type", salaryStructureType);
        params.put("regular_hour_rate", String.valueOf(normalHourRate)); // Regular hour rate
        params.put("overtime_salary", String.valueOf(overtimeSalary)); // Overtime salary
        params.put("overtime_hours", String.valueOf(overtimeHours)); // عدد ساعات العمل الإضافية
        params.put("overtime_hour_rate", String.valueOf(overtimeHourRate)); // Overtime hour rate
        params.put("expected_working_days", String.valueOf(expectedWorkingDays)); // Expected working days
        params.put("absent_days", String.valueOf(absentDays)); // Absent days
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response salary", response);
                    processedEmployees++;
                    Toast.makeText(context, processedEmployees + " out of " + totalEmployeesToProcess + " salaries processed", Toast.LENGTH_SHORT).show();
                    if (processedEmployees == totalEmployeesToProcess) {
                        Toast.makeText(SalarySlip.this, "All salary slips generated successfully", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    processedEmployees++;
                    Toast.makeText(context, "Error while storing salary for employee " + employeeId + ": " + error.getMessage(), Toast.LENGTH_LONG).show();
                    if (processedEmployees == totalEmployeesToProcess) {
                        Toast.makeText(SalarySlip.this, "Processing completed with some errors", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
    private void StoreSalaryInDataBase_per_hour(Context context, int employeeId, double finalSalary, String startDate, String endDate, double finalTotalPermanentIncrease, double finalTotalTemporaryIncrease, double normalHours, double normalHourRate, double normalSalary, double overtimeHours, double overtimeHourRate, double overtimeSalary, String salaryStructureType, int expectedWorkingDays, int absentDays) {
        LocalDate end = LocalDate.parse(endDate);
        int month = end.getMonthValue();
        int year = end.getYear();
        String url = "http://10.0.2.2/worksync/salary_drafts.php";
        Map<String, String> params = new HashMap<>();
        params.put("employee_id", String.valueOf(employeeId));
        params.put("net_salary", String.valueOf(finalSalary));
        params.put("period_start", startDate);
        params.put("period_end", endDate);
        params.put("month", String.valueOf(month));
        params.put("year", String.valueOf(year));
        params.put("bonus", String.valueOf(finalTotalTemporaryIncrease));
        params.put("salary_increment", String.valueOf(finalTotalPermanentIncrease));
        params.put("salary_structure_type",salaryStructureType);
        params.put("regular_hours", String.valueOf(normalHours)); // عدد ساعات العمل العادية
        params.put("regular_hour_rate", String.valueOf(normalHourRate)); // سعر الساعة العادية
        params.put("regular_salary", String.valueOf(normalSalary)); // الراتب الناتج من الساعات العادية
        params.put("overtime_hours", String.valueOf(overtimeHours)); // عدد ساعات العمل الإضافية
        params.put("overtime_hour_rate", String.valueOf(overtimeHourRate)); // سعر الساعة الإضافية
        params.put("overtime_salary", String.valueOf(overtimeSalary)); // الراتب الناتج من الساعات الإضافية
        params.put("expected_working_days", String.valueOf(expectedWorkingDays)); // Expected working days
        params.put("absent_days", String.valueOf(absentDays)); // Absent days

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Response salary", response);
                    processedEmployees++;
                    // عرض رسالة Toast تُظهر عدد الموظفين المعالجين
                    Toast.makeText(context, processedEmployees + " out of " + totalEmployeesToProcess + " salaries processed", Toast.LENGTH_SHORT).show();
                    // إذا اكتملت معالجة جميع الموظفين، اعرض رسالة نهائية
                    if (processedEmployees == totalEmployeesToProcess) {
                        Toast.makeText(SalarySlip.this, "All salary slips generated successfully", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    processedEmployees++; // زيادة العداد حتى في حالة الخطأ لضمان المتابعة
                    Toast.makeText(context, "Error while storing salary for employee " + employeeId + ": " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // إذا اكتملت معالجة جميع الموظفين (بما في ذلك الأخطاء)، اعرض رسالة نهائية
                    if (processedEmployees == totalEmployeesToProcess) {
                        Toast.makeText(SalarySlip.this, "Processing completed with some errors", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

//    private void generateSalarySlipPdf(
//            String employeeName,
//            String month,
//            double baseSalary,
//            double totalSalary,
//            double normalHours,
//            double normalHourRate,
//            double normalHourSalary,
//            double overtimeHours,
//            double overtimeHourRate,
//            double overtimeSalary,
//            double permanentIncrease,
//            double temporaryIncrease,
//            String startDate,
//            String endDate
//    ) {
//        PdfDocument document = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 size in points
//        PdfDocument.Page page = document.startPage(pageInfo);
//        Canvas canvas = page.getCanvas();
//        Paint paint = new Paint();
//        paint.setTextSize(14);
//        paint.setFakeBoldText(true);
//
//        int y = 50;
//        int x = 50;
//
//        // Title
//        canvas.drawText("Salary Slip", x, y, paint);
//        y += 30;
//
//        // Employee Details
//        paint.setFakeBoldText(false);
//        paint.setTextSize(12);
//        canvas.drawText("Employee Name: " + employeeName, x, y, paint);
//        y += 20;
//        canvas.drawText("Period: " + startDate + " to " + endDate, x, y, paint);
//        y += 20;
//        canvas.drawText("Month: " + month, x, y, paint);
//        y += 30;
//
//        // Salary Breakdown
//        paint.setFakeBoldText(true);
//        canvas.drawText("Salary Breakdown", x, y, paint);
//        y += 20;
//        paint.setFakeBoldText(false);
//
//        if (baseSalary > 0) { // For base salary structure
//            canvas.drawText("Base Salary: $" + String.format("%.2f", baseSalary), x, y, paint);
//            y += 20;
//        } else { // For per hour structure
//            canvas.drawText("Regular Hours: " + String.format("%.2f", normalHours) + " @ $" + String.format("%.2f", normalHourRate) + "/hr", x, y, paint);
//            y += 20;
//            canvas.drawText("Regular Salary: $" + String.format("%.2f", normalHourSalary), x, y, paint);
//            y += 20;
//            canvas.drawText("Overtime Hours: " + String.format("%.2f", overtimeHours) + " @ $" + String.format("%.2f", overtimeHourRate) + "/hr", x, y, paint);
//            y += 20;
//            canvas.drawText("Overtime Salary: $" + String.format("%.2f", overtimeSalary), x, y, paint);
//            y += 20;
//        }
//
//        // Increases
//        canvas.drawText("Permanent Increase: $" + String.format("%.2f", permanentIncrease), x, y, paint);
//        y += 20;
//        canvas.drawText("Temporary Increase: $" + String.format("%.2f", temporaryIncrease), x, y, paint);
//        y += 30;
//
//        // Total
//        paint.setFakeBoldText(true);
//        canvas.drawText("Net Salary: $" + String.format("%.2f", totalSalary), x, y, paint);
//
//        document.finishPage(page);
//
//        String fileName = "SalarySlip_" + employeeName.replace(" ", "_") + "_" + month + ".pdf";
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            ContentValues contentValues = new ContentValues();
//            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
//            contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
//            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
//
//            ContentResolver resolver = getContentResolver();
//            Uri pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
//
//            if (pdfUri != null) {
//                try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
//                    document.writeTo(outputStream);
//                    Toast.makeText(this, "PDF saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Toast.makeText(this, "Error writing PDF", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//            File file = new File(downloadsDir, fileName);
//            try (FileOutputStream outputStream = new FileOutputStream(file)) {
//                document.writeTo(outputStream);
//                Toast.makeText(this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(this, "Error writing PDF", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        document.close();
//    }

    private void getSpescficEmployee( int employee_id) {
        String employeeDetailsUrl = "http://10.0.2.2/worksync/get_spesfic_employee.php?employee_id=" + employee_id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, employeeDetailsUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Employee Details", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")) {
                        String message = jsonObject.getString("message");
                        if (!message.equals("No employee found")) {
                            /**Fetch the employee Information*/
                            JSONObject employeeDetailsObj = jsonObject.getJSONObject("data");
                            String full_name = employeeDetailsObj.getString("fullname");
                            String phone = employeeDetailsObj.getString("phone");
                            String employee_status = employeeDetailsObj.getString("status");
                            String email = employeeDetailsObj.getString("email");
                            String base_salary = employeeDetailsObj.getString("base_salary");
                            String salary_structure_type = employeeDetailsObj.getString("salary_structure_type");
                            String overtime_hour_rate = employeeDetailsObj.getString("overtime_hour_rate");
                            String normal_hour_rate = employeeDetailsObj.getString("normal_hour_rate");
                            String expected_hours_per_day = employeeDetailsObj.getString("expected_hours_per_day");
                            String working_day_per_week = employeeDetailsObj.getString("working_day_per_week");
                            String company_name = employeeDetailsObj.getString("company_name");
                            String designation_name = employeeDetailsObj.getString("designation_name");
                            String employee_code = employeeDetailsObj.getString("employee_code");
                            String department_name = employeeDetailsObj.getString("department_name");
                        }else {
                            Log.d("Employee Details ", "No employee found with this Id");
                        }
                    } else {
                        Log.e("Employee Details Error", "Failed to fetch employee details (Error in Query or data base and so on ");
                    }
                } catch (JSONException e) {
                    Log.d("Exception", String.valueOf(e));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyError", String.valueOf(error));
            }
        });
        requestQueue.add(stringRequest);
    }
    private void fetchEmployeeDetails( int employee_id) {
        String employeeDetailsUrl = "http://10.0.2.2/worksync/get_spesfic_employee.php?employee_id=" + employee_id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, employeeDetailsUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Employee Details", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")) {
                        String message = jsonObject.getString("message");
                        if (!message.equals("No employee found")) {
                            /**Fetch the employee Information*/
                            JSONObject employeeDetailsObj = jsonObject.getJSONObject("data");
                            String full_name = employeeDetailsObj.getString("fullname");
                            String phone = employeeDetailsObj.getString("phone");
                            String employee_status = employeeDetailsObj.getString("status");
                            String email = employeeDetailsObj.getString("email");
                            String base_salary = employeeDetailsObj.getString("base_salary");
                            String salary_structure_type = employeeDetailsObj.getString("salary_structure_type");
                            String overtime_hour_rate = employeeDetailsObj.getString("overtime_hour_rate");
                            String normal_hour_rate = employeeDetailsObj.getString("normal_hour_rate");
                            String expected_hours_per_day = employeeDetailsObj.getString("expected_hours_per_day");
                            String working_day_per_week = employeeDetailsObj.getString("working_day_per_week");
                            String company_name = employeeDetailsObj.getString("company_name");
                            String designation_name = employeeDetailsObj.getString("designation_name");
                            String department_name = employeeDetailsObj.getString("department_name");
                            Log.d("Hello" ,"Hello");
                            if (normal_hour_rate.isEmpty() || overtime_hour_rate.isEmpty() ||
                                    expected_hours_per_day.isEmpty() || working_day_per_week.isEmpty()) {
                                String warning ="Error in Employee Data :over time rate or normal rate is empty for this employee: " + full_name +" "+ employee_id;
                                Log.d("Warning while create  Salary Slip" ,warning);
                                return;
                            }
                            double normalHourRate;
                            try {
                                normalHourRate = Double.parseDouble(normal_hour_rate);
                                if (normalHourRate <= 0) throw new NumberFormatException();
                            } catch (NumberFormatException e) {
                                String warning ="Error in Employee Data : normal rate is not valid format for this employee Or Zero: " + full_name +" "+ employee_id;
                                Log.d("Warning while create  Salary Slip" ,warning);
                                return;
                            }
                            double baseSalary;
                            if (salary_structure_type.equalsIgnoreCase("base salary")) {
                                try {
                                    baseSalary = Double.parseDouble(base_salary);
                                    if (baseSalary <= 0) throw new NumberFormatException();
                                } catch (NumberFormatException e) {
                                    String warning = "Error in Employee Data: Base Salary is not valid format or Zero for this employee: " + full_name + " " + employee_id;
                                    Log.d("Warning while creating Salary Slip", warning);
                                    return;
                                }
                            } else {
                                baseSalary = 0;
                            }

                            double overtimeHourRate;
                            try {
                                overtimeHourRate = Double.parseDouble(overtime_hour_rate);
                                if (overtimeHourRate <= 0) throw new NumberFormatException();
                            } catch (NumberFormatException e) {
                                String warning ="Error in Employee Data : overtime rate is not valid format for this employee Or Zero: " + full_name +" "+ employee_id;
                                Log.d("Warning while create  Salary Slip" ,warning);
                                return;
                            }
                            double expectedHoursPerDay;
                            try {
                                expectedHoursPerDay = Double.parseDouble(expected_hours_per_day);
                                if (expectedHoursPerDay <= 0 || expectedHoursPerDay > 24) throw new NumberFormatException();
                            } catch (NumberFormatException e) {
                                String warning ="Error in Employee Data :  expected Hours Per Day is not valid format for this employee: " + full_name +" "+ employee_id;
                                Log.d("Warning while create  Salary Slip" ,warning);
                                return;
                            }
                            int workingDaysPerWeek;
                            try {
                                workingDaysPerWeek = Integer.parseInt(working_day_per_week);
                                if (workingDaysPerWeek <= 0 || workingDaysPerWeek > 7) throw new NumberFormatException();
                            } catch (NumberFormatException e) {
                                String warning ="Error in Employee Data :  working Days Per Week is not valid format for this employee: " + full_name +" "+ employee_id;
                                Log.d("Warning while create  Salary Slip" ,warning);
                                return;
                            }
                            fetchAttendanceRecord(employee_id,expectedHoursPerDay , normalHourRate , overtimeHourRate,salary_structure_type,baseSalary,workingDaysPerWeek);
                        }else {
                            Log.d("Employee Details ", "No employee found with this Id");
                        }
                    } else {
                        Log.e("Employee Details Error", "Failed to fetch employee details (Error in Query or data base and so on ");

// handle



                    }
                } catch (JSONException e) {
                    Log.d("Exception", String.valueOf(e));
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyError", String.valueOf(error));
            }
        });
        requestQueue.add(stringRequest);
    }

    private void fetchPaidLeaveDates(int employeeId, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, List<AttendanceRecord> attendanceList, Set<String> holidaysDates, String salaryStructureType, double baseSalary, double totalSalaryIncrease, List<SalaryIncrease> temporaryIncreases, List<SalaryIncrease> permanentIncreases, int workingDaysPerWeek) {
        StringBuilder permanentIncreasesStr = new StringBuilder();






        ArrayList<Leave> paidLeaves = new ArrayList<Leave>();
        String url = "http://10.0.2.2/worksync/get_paid_leave_inPeriod.php?employee_id=" + employeeId
                + "&start_date=" + startDate + "&end_date=" + endDate;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.d("PaidLeaves", "Fetched: " + response );

            try {
                JSONObject object = new JSONObject(response);
                if (object.getString("status").equals("success")) {
                    JSONArray datesArray = object.getJSONArray("data");
                    for (int i = 0; i < datesArray.length(); i++) {
                        String  id=    datesArray.getJSONObject(i).getString("id");
                        String leave_type =  datesArray.getJSONObject(i).getString("leave_type");
                        String start_date=    datesArray.getJSONObject(i).getString("start_date");
                        String end_date =    datesArray.getJSONObject(i).getString("end_date");
                        String status =    datesArray.getJSONObject(i).getString("status");
                        String is_paid=    datesArray.getJSONObject(i).getString("is_paid");
                        String reason=     datesArray.getJSONObject(i).getString("reason");
                        Leave leave = new Leave(id,leave_type,start_date,end_date,status,is_paid,reason);
                        paidLeaves.add(leave);
                    }
                }
                else{

                }
                fetchWorkingsDays(employeeId, expectedHoursPerDay, normalHourRate, overtimeHourRate, attendanceList, holidaysDates, salaryStructureType, baseSalary,totalSalaryIncrease,temporaryIncreases,permanentIncreases,paidLeaves,workingDaysPerWeek);

//there is call methods
//                Log.d("PaidLeaves", "Fetched: " + paidLeaveDates.size() + " days");

            } catch (JSONException e) {
                Log.e("Leave JSON Error", e.getMessage());
            }
        }, error -> {
            Log.e("Volley Error", error.getMessage());
        });

        requestQueue.add(stringRequest);
    }

    private void fetchSalaryIncreases(int employeeId, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, List<AttendanceRecord> attendanceList, Set<String> holidaysDates, String salaryStructureType, double baseSalary, int workingDaysPerWeek) {




        String url = "http://10.0.2.2/worksync/get_salary_increases.php?employee_id=" + String.valueOf(employeeId);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Log.d("Increase Response", response);
            double totalSalaryIncrease = 0.0;
            List<SalaryIncrease> permanentIncreases = new ArrayList<>();
            List<SalaryIncrease> temporaryIncreases = new ArrayList<>();
            try {
                JSONObject object = new JSONObject(response);
                String status = object.getString("status");
                if (status.equals("success")) {
                    JSONArray increasesArray = object.getJSONArray("data");
                    for (int i = 0; i < increasesArray.length(); i++) {
                        JSONObject increaseObj = increasesArray.getJSONObject(i);
                        String type = increaseObj.getString("increase_type");
                        String duration_type = increaseObj.getString("durationType");
                        double amount = Double.parseDouble(increaseObj.getString("increase_amount"));
                        String start_date_str = increaseObj.optString("start_date", "");
                        LocalDate salaryMonthEnd = LocalDate.parse(start_date_str);
                        LocalDate increasestartDate=null;
                        if (!start_date_str.isEmpty()) {
                            increasestartDate = LocalDate.parse(start_date_str);
                        }
                        if (duration_type.equalsIgnoreCase("Permanent")) {
                            if (!increasestartDate.isAfter(salaryMonthEnd)) {
                                totalSalaryIncrease += amount;
                                permanentIncreases.add(new SalaryIncrease(type,duration_type,amount,String.valueOf(increasestartDate)));
                            }
                        } else if (duration_type.equalsIgnoreCase("Temporary")) {
                            String end_date_str = increaseObj.isNull("end_date") ? "" : increaseObj.optString("end_date", "");
                            if (!start_date_str.isEmpty() && !end_date_str.isEmpty()) {
                                LocalDate increaseStart = LocalDate.parse(start_date_str);
                                LocalDate increaseEnd = LocalDate.parse(end_date_str);
                                double applicableAmount = calculateIncreaseForMonth(
                                        increaseStart,
                                        increaseEnd,
                                        startDate,
                                        endDate,
                                        amount
                                );
                                if(applicableAmount > 0) {
                                    totalSalaryIncrease += applicableAmount;
                                    temporaryIncreases.add(new SalaryIncrease(type,duration_type,applicableAmount, String.valueOf(increaseStart),String.valueOf(increaseEnd)));
                                }
                            }
                        }
                    }

                    Log.d("Total Increase", "Total applicable increase: " + totalSalaryIncrease);
                }
                else{

                }
                fetchPaidLeaveDates(employeeId, expectedHoursPerDay, normalHourRate, overtimeHourRate, attendanceList, holidaysDates, salaryStructureType, baseSalary,totalSalaryIncrease,temporaryIncreases,permanentIncreases,workingDaysPerWeek);


            } catch (JSONException e) {
                Log.d("Salary Increase JSON", "Parsing error: " + e.getMessage());
            }

        }, error -> {
            Log.d("VolleyError", "Error fetching salary increases: " + error.getMessage());
        });

        requestQueue.add(stringRequest);
    }
    private double calculateIncreaseForMonth(
            LocalDate increaseStart,
            LocalDate increaseEnd,
            String salaryMonthStart,
            String salaryMonthEnd,
            double totalIncreaseAmount
    ) {
        LocalDate salaryStartDate = LocalDate.parse(salaryMonthStart);
        LocalDate salaryEndDate = LocalDate.parse(salaryMonthEnd);

        LocalDate effectiveStart = increaseStart.isAfter(salaryStartDate) ? increaseStart : salaryStartDate;
        LocalDate effectiveEnd = increaseEnd.isBefore(salaryEndDate) ? increaseEnd : salaryEndDate;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0.0;
        }
        long overlappingDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd.plusDays(1));
        long totalIncreaseDays = ChronoUnit.DAYS.between(increaseStart, increaseEnd.plusDays(1));

        return totalIncreaseAmount * ((double) overlappingDays / totalIncreaseDays);
    }


}