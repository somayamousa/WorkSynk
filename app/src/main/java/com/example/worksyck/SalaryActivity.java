package com.example.worksyck;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SalaryActivity extends AppCompatActivity {
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private ImageView backButton;
    private NavigationHelper navigationHelper;
    private int userId, company_id;
    private String email, fullname, role;
    private TextView monthYearText, netSalaryText, baseSalaryText, bonusText, overtimeSalaryText,
            regularHoursText, overtimeHoursText, absentDaysText, payslipNumberText, noRecordText;
    private Button fetchSalaryBtn;
    private View salaryDetailsContainer;
    private RequestQueue requestQueue;
    private int selectedMonth, selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);
        // Retrieve intent extras
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getIntExtra("company_id", 0);

        if (userId == 0) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize NavigationHelper
        navigationHelper = new NavigationHelper(this, userId, email, fullname, role, company_id);
        navigationHelper.enableBackButton();

        // Initialize views
        initializeViews();

        // Set up Bottom Navigation
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);

        // Initialize Volley
        requestQueue = Volley.newRequestQueue(this);
        // Set default month/year to current
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH) + 1; // Month is 0-based
        selectedYear = cal.get(Calendar.YEAR);
        updateMonthYearText();
        // Fetch salary for current month/year
        fetchSalaryRecord();
        // Set listeners
        monthYearText.setOnClickListener(v -> showMonthYearPicker());
        fetchSalaryBtn.setOnClickListener(v -> fetchSalaryRecord());
    }
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);
        monthYearText = findViewById(R.id.monthYearText);
        fetchSalaryBtn = findViewById(R.id.fetchSalaryBtn);
        salaryDetailsContainer = findViewById(R.id.salaryDetailsContainer);
        netSalaryText = findViewById(R.id.netSalaryText);
        baseSalaryText = findViewById(R.id.baseSalaryText);
        bonusText = findViewById(R.id.bonusText);
        overtimeSalaryText = findViewById(R.id.overtimeSalaryText);
        regularHoursText = findViewById(R.id.regularHoursText);
        overtimeHoursText = findViewById(R.id.overtimeHoursText);
        absentDaysText = findViewById(R.id.absentDaysText);
        payslipNumberText = findViewById(R.id.payslipNumberText);
        noRecordText = findViewById(R.id.noRecordText);
        navigationHelper.setBackButtonListener(backButton);
    }
    private void showMonthYearPicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = month + 1; // Month is 0-based
                    selectedYear = year;
                    updateMonthYearText();
                },
                selectedYear, selectedMonth - 1, 1);
        datePicker.setTitle("Select Month and Year");
        datePicker.show();
    }
    private void updateMonthYearText() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, selectedMonth - 1);
        calendar.set(Calendar.YEAR, selectedYear); // مهم أيضًا تحديد السنة إذا كانت جزء من المعروض
        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        monthYearText.setText(String.format("%s %d", monthName, selectedYear));
    }


    private void fetchSalaryRecord() {
        String url = String.format("http://10.0.2.2/worksync/fetch_salary.php?employee_id=%d&month=%d&year=%d",
                userId, selectedMonth, selectedYear);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("Salary Response", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if ("success".equals(status)) {
                            JSONObject data = jsonResponse.getJSONObject("data");
                            displaySalaryDetails(data);
                        } else {
                            showNoRecordMessage(jsonResponse.optString("message", "قسيمة الراتب غير متوفرة"));
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoRecordMessage("parsing Error");
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoRecordMessage("Connection Error");
                });

        // لتجنب مشكلة التأخير أو الفشل المؤقت في الاتصال
        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }


    private void displaySalaryDetails(JSONObject data) throws JSONException {
        // Fetch shared data
        String department = data.getString("department_name");
        String company_name = data.getString("company_name");
        String designation = data.getString("job_title");
        String employee_fullname = data.getString("employee_fullname");
        String periodStart = data.getString("period_start");
        String periodEnd = data.getString("period_end");
        String employee_code = data.getString("employee_code");
        int expected_working_days = data.optInt("expected_working_days", 0);
        String salaryType = data.getString("salary_structure_type"); // "base_salary" or "per_hour"
        double regularHourRate = data.has("regular_hour_rate") && !data.isNull("regular_hour_rate") ? data.getDouble("regular_hour_rate") : 0.0;
        double overtimeHourRate = data.optDouble("overtime_hour_rate", 0.0);
        int absentDays = data.optInt("absent_days", 0);
        double bonus = data.optDouble("bonus", 0.0);
        double salary_increment = data.optDouble("salary_increment", 0.0);
        double netSalary = data.optDouble("net_salary", 0.0);
        double overtimeHours = data.optDouble("overtime_hours", 0.0);
        double overtimeSalary = data.optDouble("overtime_salary", 0.0);
        String payslipNumber = data.optString("payslip_number", "#");
        String expected_hours_per_day = data.getString("expected_hours_per_day");



        // Fetch non-shared data
        double baseSalary = data.has("base_salary") && !data.isNull("base_salary") ? data.getDouble("base_salary") : 0.0;
        double regularSalary = data.has("regular_salary") && !data.isNull("regular_salary") ? data.getDouble("regular_salary") : 0.0;
        double regularHours = data.has("regular_hours") && !data.isNull("regular_hours") ? data.getDouble("regular_hours") : 0.0;

        // Populate shared data
        ((TextView) findViewById(R.id.hourPerDay)).setText("Expected Hour Per Day:: " + expected_hours_per_day);
        ((TextView) findViewById(R.id.employeeFullnameText)).setText("Employee Name: " + employee_fullname);
        ((TextView) findViewById(R.id.employeeCodeText)).setText("Employee Code: " + employee_code);
        ((TextView) findViewById(R.id.departmentText)).setText("Department: " + department);
        ((TextView) findViewById(R.id.jobTitleText)).setText("Job Title: " + designation);
        ((TextView) findViewById(R.id.companyText)).setText("Company Name: " + company_name);
        ((TextView) findViewById(R.id.periodStartText)).setText("Start Date: " + periodStart);
        ((TextView) findViewById(R.id.periodEndText)).setText("End Date: " + periodEnd);
        ((TextView) findViewById(R.id.expectedWorkingDaysText)).setText("Working Days: " + expected_working_days);
        ((TextView) findViewById(R.id.regularHourRateText)).setText("Regular Hourly Rate: $" + String.format("%.2f", regularHourRate));
        ((TextView) findViewById(R.id.overtimeHourRateText)).setText("Overtime Rate: $" + String.format("%.2f", overtimeHourRate));
        ((TextView) findViewById(R.id.absentDaysText)).setText("Days of Absence: " + absentDays);
        ((TextView) findViewById(R.id.bonusText)).setText("Bonus: $" + String.format("%.2f", bonus));
        ((TextView) findViewById(R.id.salaryIncrementText)).setText("Salary Increment: $" + String.format("%.2f", salary_increment));
        ((TextView) findViewById(R.id.netSalaryText)).setText("Net Salary: $" + String.format("%.2f", netSalary));
        ((TextView) findViewById(R.id.overtimeHoursText)).setText("Overtime Hours: " + String.format("%.1f", overtimeHours));
        ((TextView) findViewById(R.id.overtimeSalaryText)).setText("Overtime Pay: $" + String.format("%.2f", overtimeSalary));
        ((TextView) findViewById(R.id.payslipNumberText)).setText("Salary Slip Number: " + payslipNumber);
        ((TextView) findViewById(R.id.salaryTypeText)).setText("Work Type: " + salaryType);

        // Conditionally display based on salary_structure_type
        TextView baseSalaryText = findViewById(R.id.baseSalaryText);
        TextView regularSalaryText = findViewById(R.id.regularSalaryText);
        TextView regularHoursText = findViewById(R.id.regularHoursText);
        if (salaryType.equalsIgnoreCase("base salary")) {
            // Show base salary, hide regular salary and regular hours
            baseSalaryText.setText("Base Salary: $" + String.format("%.2f", baseSalary));
            baseSalaryText.setVisibility(View.VISIBLE);
            regularSalaryText.setVisibility(View.GONE);
            regularHoursText.setVisibility(View.GONE);

        } else if (salaryType.equalsIgnoreCase("per hour")){
            // Show regular salary and regular hours, hide base salary
            regularSalaryText.setText("Regular Salary: $" + String.format("%.2f", regularSalary));
            regularHoursText.setText("Regular Work Hours: " + String.format("%.1f", regularHours));
            baseSalaryText.setVisibility(View.GONE);
            regularSalaryText.setVisibility(View.VISIBLE);
            regularHoursText.setVisibility(View.VISIBLE);
        }
        // Show the salary details container
        salaryDetailsContainer.setVisibility(View.VISIBLE);
        noRecordText.setVisibility(View.GONE);
        fetchAndDisplaySalaryIncreases(userId,periodStart, periodEnd);

    }
    private void showNoRecordMessage(String message) {
        salaryDetailsContainer.setVisibility(View.GONE);
        noRecordText.setText(message);
        noRecordText.setVisibility(View.VISIBLE);
    }
    private void fetchAndDisplaySalaryIncreases(int employeeId, String periodStart, String periodEnd) {
        String url = "http://10.0.2.2/worksync/get_increases.php?employee_id=" + employeeId
                + "&start_date=" + periodStart + "&end_date=" + periodEnd;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("Error",response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (!jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "No salary increases found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray increasesArray = jsonResponse.getJSONArray("increases");

                        List<String> permanentIncreases = new ArrayList<>();
                        List<String> temporaryIncreases = new ArrayList<>();

                        for (int i = 0; i < increasesArray.length(); i++) {
                            JSONObject increase = increasesArray.getJSONObject(i);
                            String type = increase.getString("type"); // "permanent" or "temporary"
                            double amount = increase.getDouble("amount");
                            String reason = increase.optString("reason", "");
                            String label = reason.isEmpty()
                                    ? "$" + String.format("%.2f", amount)
                                    : reason + ": $" + String.format("%.2f", amount);

                            if (type.equalsIgnoreCase("permanent")) {
                                permanentIncreases.add(label);
                            } else if (type.equalsIgnoreCase("temporary")) {
                                temporaryIncreases.add(label);
                            }
                        }

                        TextView permanentIncreasesText = findViewById(R.id.permanentIncreasesText);
                        if (!permanentIncreases.isEmpty()) {
                            permanentIncreasesText.setText("Permanent Increases:\n" + TextUtils.join("\n", permanentIncreases));
                            permanentIncreasesText.setVisibility(View.VISIBLE);
                        } else {
                            permanentIncreasesText.setVisibility(View.GONE);
                        }

                        TextView temporaryIncreasesText = findViewById(R.id.temporaryIncreasesText);
                        if (!temporaryIncreases.isEmpty()) {
                            temporaryIncreasesText.setText("Temporary Increases:\n" + TextUtils.join("\n", temporaryIncreases));
                            temporaryIncreasesText.setVisibility(View.VISIBLE);
                        } else {
                            temporaryIncreasesText.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing salary increases", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error fetching salary increases", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(request);
    }
}
