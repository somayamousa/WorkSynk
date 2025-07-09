package com.example.worksyck;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalaryActivity extends AppCompatActivity {
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private ImageView backButton, noRecordsImage, deductionsArrow, bonusArrow, salaryIncrementArrow, earningsArrow;
    private LinearLayout bonusHeader, bonusDetails, permanentIncreasesHeader, permanentIncreasesDetails,
            deductionsHeader, deductionsDetails, earningsHeader, earningsDetails;
    private NavigationHelper navigationHelper;
    private int userId, company_id;
    private CardView deductionsCard;
    private String email, fullname, role;
    private EditText monthText, yearText;
    private TextView netSalaryText, baseSalaryText, bonusText, overtimeSalaryText,
            regularHoursText, overtimeHoursText, absentDaysText, payslipNumberText,
            noRecordsText, deductionsText, wordNetSalaryText, salaryIncrementText,
            regularSalaryText, earningsText;
    private Button fetchSalaryBtn, downloadPdfBtn;
    private ScrollView salaryDetailsContainer;
    private RequestQueue requestQueue;
    private int selectedMonth, selectedYear;
    private boolean isBonusExpanded = false;
    private boolean isPermanentIncreasesExpanded = false;
    private boolean isDeductionsExpanded = false;
    private boolean isEarningsExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary);

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

        navigationHelper = new NavigationHelper(this, userId, email, fullname, role, company_id);
        navigationHelper.enableBackButton();

        initializeViews();

        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);

        requestQueue = Volley.newRequestQueue(this);

        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH) + 1;
        selectedYear = cal.get(Calendar.YEAR);
        updateMonthYearText();

        fetchSalaryRecord();

        setupEditTextListeners();

        fetchSalaryBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                fetchSalaryRecord();
                fetchSalaryBtn.setSelected(true);
                downloadPdfBtn.setSelected(false);
            }
        });

        downloadPdfBtn.setOnClickListener(v -> {
            Log.d("SalaryActivity", "Download button clicked");
            if (validateInputs()) {
                fetchSalaryBtn.setSelected(false);
                downloadPdfBtn.setSelected(true);
                generateSalarySlipPDF();
            }
        });

        bonusHeader.setOnClickListener(v -> toggleBonusDetails());
        permanentIncreasesHeader.setOnClickListener(v -> togglePermanentIncreasesDetails());
        deductionsHeader.setOnClickListener(v -> toggleDeductionsDetails());
        earningsHeader.setOnClickListener(v -> toggleEarningsDetails());
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);
        monthText = findViewById(R.id.monthText);
        yearText = findViewById(R.id.yearText);
        fetchSalaryBtn = findViewById(R.id.fetchSalaryBtn);
        downloadPdfBtn = findViewById(R.id.downloadPdfBtn);
        deductionsArrow = findViewById(R.id.deductions_arrow);
        bonusArrow = findViewById(R.id.bonus_arrow);
        salaryIncrementArrow = findViewById(R.id.salary_increment_arrow);
        earningsArrow = findViewById(R.id.earnings_arrow);
        bonusHeader = findViewById(R.id.bonusHeader);
        bonusDetails = findViewById(R.id.bonusDetails);
        permanentIncreasesHeader = findViewById(R.id.permanentIncreasesHeader);
        permanentIncreasesDetails = findViewById(R.id.permanentIncreasesDetails);
        deductionsHeader = findViewById(R.id.deductionsHeader);
        deductionsDetails = findViewById(R.id.deductionsDetails);
        earningsHeader = findViewById(R.id.earningsHeader);
        earningsDetails = findViewById(R.id.earningsDetails);
        salaryDetailsContainer = findViewById(R.id.salaryDetailsContainer);
        netSalaryText = findViewById(R.id.netSalaryText);
        baseSalaryText = findViewById(R.id.baseSalaryText);
        bonusText = findViewById(R.id.bonusText);
        overtimeSalaryText = findViewById(R.id.overtimeSalaryText);
        regularHoursText = findViewById(R.id.regularHoursText);
        overtimeHoursText = findViewById(R.id.overtimeHoursText);
        absentDaysText = findViewById(R.id.absentDaysText);
        payslipNumberText = findViewById(R.id.payslipNumberText);
        noRecordsImage = findViewById(R.id.noRecordsImage);
        noRecordsText = findViewById(R.id.noRecordsText);
        deductionsText = findViewById(R.id.deductionsText);
        wordNetSalaryText = findViewById(R.id.wordNetSalaryText);
        salaryIncrementText = findViewById(R.id.salaryIncrementText);
        regularSalaryText = findViewById(R.id.regularSalaryText);
        earningsText = findViewById(R.id.earningsText);
        deductionsCard = (CardView) deductionsHeader.getParent().getParent();
        navigationHelper.setBackButtonListener(backButton);
    }

    private void setupEditTextListeners() {
        monthText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                fetchSalaryBtn.setSelected(false);
                downloadPdfBtn.setSelected(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 2 && validateInputs()) {
                    fetchSalaryRecord();
                }
            }
        });
        yearText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                fetchSalaryBtn.setSelected(false);
                downloadPdfBtn.setSelected(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 4 && validateInputs()) {
                    fetchSalaryRecord();
                }
            }
        });
    }

    private boolean validateInputs() {
        String monthStr = monthText.getText().toString().trim();
        String yearStr = yearText.getText().toString().trim();

        if (monthStr.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Please enter both month and year", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            selectedMonth = Integer.parseInt(monthStr);
            selectedYear = Integer.parseInt(yearStr);

            if (selectedMonth < 1 || selectedMonth > 12) {
                Toast.makeText(this, "Month must be between 01 and 12", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (selectedYear < 1900 || selectedYear > 2100) {
                Toast.makeText(this, "Year must be between 1900 and 2100", Toast.LENGTH_SHORT).show();
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid month or year format", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void updateMonthYearText() {
        monthText.setText(String.format(Locale.getDefault(), "%02d", selectedMonth));
        yearText.setText(String.format(Locale.getDefault(), "%d", selectedYear));
    }

    private void toggleBonusDetails() {
        isBonusExpanded = !isBonusExpanded;
        bonusDetails.setVisibility(isBonusExpanded ? View.VISIBLE : View.GONE);
        bonusArrow.setRotation(isBonusExpanded ? 180 : 0);
    }

    private void togglePermanentIncreasesDetails() {
        isPermanentIncreasesExpanded = !isPermanentIncreasesExpanded;
        permanentIncreasesDetails.setVisibility(isPermanentIncreasesExpanded ? View.VISIBLE : View.GONE);
        salaryIncrementArrow.setRotation(isPermanentIncreasesExpanded ? 180 : 0);
    }

    private void toggleDeductionsDetails() {
        isDeductionsExpanded = !isDeductionsExpanded;
        deductionsDetails.setVisibility(isDeductionsExpanded ? View.VISIBLE : View.GONE);
        deductionsArrow.setRotation(isDeductionsExpanded ? 180 : 0);
    }

    private void toggleEarningsDetails() {
        isEarningsExpanded = !isEarningsExpanded;
        earningsDetails.setVisibility(isEarningsExpanded ? View.VISIBLE : View.GONE);
        earningsArrow.setRotation(isEarningsExpanded ? 180 : 0);
    }

    private void fetchSalaryRecord() {
        if (!validateInputs()) {
            return;
        }

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
                            showNoRecordMessage(noRecordsText.getText().toString().trim());
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoRecordMessage("Data analysis error");
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    showNoRecordMessage("Connection error");
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private void displaySalaryDetails(JSONObject data) throws JSONException {
        String department = data.getString("department_name");
        String company_name = data.getString("company_name");
        String designation = data.getString("job_title");
        String employee_fullname = data.getString("employee_fullname");
        String periodStart = data.getString("period_start");
        String periodEnd = data.getString("period_end");
        String employee_code = data.getString("employee_code");
        int expected_working_days = data.optInt("expected_working_days", 0);
        String salaryType = data.getString("salary_structure_type");
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
        double hoursPerDay = Double.parseDouble(expected_hours_per_day);
        double missingHours = data.optDouble("missing_hours", 0.0);
        double baseSalary = data.has("base_salary") && !data.isNull("base_salary") ? data.getDouble("base_salary") : 0.0;
        double regularSalary = data.has("regular_salary") && !data.isNull("regular_salary") ? data.getDouble("regular_salary") : 0.0;
        double regularHours = data.has("regular_hours") && !data.isNull("regular_hours") ? data.getDouble("regular_hours") : 0.0;
        double deductions = missingHours * regularHourRate; // Deductions calculated but not shown for per hour in UI/PDF
        double totalEarnings = salaryType.equalsIgnoreCase("per hour") ?
                (regularSalary + overtimeSalary + bonus + salary_increment) :
                (baseSalary + overtimeSalary + bonus + salary_increment);

        netSalaryText.setText("₪" + String.format("%.3f", netSalary));
        wordNetSalaryText.setText(numberToWords(netSalary));
        bonusText.setText("₪" + String.format("%.3f", bonus));
        salaryIncrementText.setText("₪" + String.format("%.3f", salary_increment));

        bonusHeader.setVisibility(View.VISIBLE);
        bonusArrow.setVisibility(View.VISIBLE);
        permanentIncreasesHeader.setVisibility(View.VISIBLE);
        salaryIncrementArrow.setVisibility(View.VISIBLE);

        deductionsDetails.removeAllViews();
        earningsDetails.removeAllViews();

        if (salaryType.equalsIgnoreCase("base salary")) {
            baseSalaryText.setText("Base Salary: ₪" + String.format("%.3f", baseSalary));
            baseSalaryText.setVisibility(View.VISIBLE);
            regularSalaryText.setVisibility(View.GONE);
            regularHoursText.setVisibility(View.GONE);
            earningsText.setText("₪" + String.format("%.3f", totalEarnings));
            earningsHeader.setVisibility(View.VISIBLE);
            earningsArrow.setVisibility(View.VISIBLE);

            if (baseSalary > 0) {
                addEarningRow("Base Salary", "₪" + String.format("%.3f", baseSalary));
            }
            if (overtimeHours > 0) {
                addEarningRow("Overtime Hours", String.format("%.3f", overtimeHours));
                addEarningRow("Overtime Salary", "₪" + String.format("%.3f", overtimeSalary));
            }

            earningsDetails.setVisibility(isEarningsExpanded ? View.VISIBLE : View.GONE);

            // Show deductions for base salary employees in UI
            if (missingHours > 0) {
                deductionsText.setText("₪" + String.format("%.3f", deductions));
                deductionsCard.setVisibility(View.VISIBLE);
                deductionsHeader.setVisibility(View.VISIBLE);
                deductionsArrow.setVisibility(View.VISIBLE);
                addDeductionRow("Missing Hours", String.format("%.3f", missingHours));
                deductionsDetails.setVisibility(isDeductionsExpanded ? View.VISIBLE : View.GONE);
            } else {
                deductionsCard.setVisibility(View.GONE);
                deductionsDetails.setVisibility(View.GONE);
                deductionsHeader.setVisibility(View.GONE);
                deductionsArrow.setVisibility(View.GONE);
                deductionsText.setText("₪0.00");
                isDeductionsExpanded = false;
            }
        } else if (salaryType.equalsIgnoreCase("per hour")) {
            regularSalaryText.setText("Regular Salary: ₪" + String.format("%.3f", regularSalary));
            regularHoursText.setText("Regular Work Hours: " + String.format("%.3f", regularHours));
            overtimeHoursText.setText("Overtime Hours: " + String.format("%.3f", overtimeHours));
            overtimeSalaryText.setText("Overtime Pay: ₪" + String.format("%.3f", overtimeSalary));
            baseSalaryText.setVisibility(View.GONE);
            regularSalaryText.setVisibility(View.VISIBLE);
            regularHoursText.setVisibility(View.VISIBLE);
            overtimeHoursText.setVisibility(View.VISIBLE);
            overtimeSalaryText.setVisibility(View.VISIBLE);
            earningsText.setText("₪" + String.format("%.3f", totalEarnings));
            earningsHeader.setVisibility(View.VISIBLE);
            earningsArrow.setVisibility(View.VISIBLE);

            if (regularHours > 0) {
                addEarningRow("Regular Hours", String.format("%.3f", regularHours));
            }
            if (regularSalary > 0) {
                addEarningRow("Regular Salary", "₪" + String.format("%.3f", regularSalary));
            }
            if (overtimeHours > 0) {
                addEarningRow("Overtime Hours", String.format("%.3f", overtimeHours));
                addEarningRow("Overtime Salary", "₪" + String.format("%.3f", overtimeSalary));
            }
            earningsDetails.setVisibility(isEarningsExpanded ? View.VISIBLE : View.GONE);

            // Hide deductions section completely for per hour employees in UI
            deductionsCard.setVisibility(View.GONE);
            deductionsDetails.setVisibility(View.GONE);
            deductionsHeader.setVisibility(View.GONE);
            deductionsArrow.setVisibility(View.GONE);
            deductionsText.setText("₪0.00");
            isDeductionsExpanded = false;
        } else {
            baseSalaryText.setVisibility(View.GONE);
            regularSalaryText.setVisibility(View.GONE);
            regularHoursText.setVisibility(View.GONE);
            overtimeHoursText.setVisibility(View.GONE);
            overtimeSalaryText.setVisibility(View.GONE);
            earningsHeader.setVisibility(View.GONE);
            earningsArrow.setVisibility(View.GONE);
            earningsDetails.setVisibility(View.GONE);
            deductionsCard.setVisibility(View.GONE);
            deductionsDetails.setVisibility(View.GONE);
            deductionsHeader.setVisibility(View.GONE);
            deductionsArrow.setVisibility(View.GONE);
            deductionsText.setText("₪0.00");
            isDeductionsExpanded = false;
        }
        salaryDetailsContainer.setVisibility(View.VISIBLE);
        fetchSalaryBtn.setVisibility(View.VISIBLE);
        fetchSalaryBtn.setSelected(true);
        downloadPdfBtn.setVisibility(View.VISIBLE);
        downloadPdfBtn.setSelected(false);
        noRecordsImage.setVisibility(View.GONE);
        noRecordsText.setVisibility(View.GONE);
        fetchAndDisplaySalaryIncreases(userId, periodStart, periodEnd);
    }

    private void addEarningRow(String reason, String amount) {
        CardView cardView = new CardView(this);
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4f);
        cardView.setRadius(8f);
        cardView.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setPadding(16, 16, 16, 16);
        contentLayout.setWeightSum(2);

        TextView reasonText = new TextView(this);
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        reasonText.setLayoutParams(reasonParams);
        reasonText.setText(reason);
        reasonText.setTextSize(14);
        reasonText.setTextColor(getResources().getColor(android.R.color.black));
        reasonText.setTypeface(getResources().getFont(R.font.poppins_medium));

        TextView amountText = new TextView(this);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        amountText.setLayoutParams(amountParams);
        amountText.setText(amount);
        amountText.setTextSize(14);
        amountText.setTextColor(getResources().getColor(android.R.color.black));
        amountText.setTypeface(getResources().getFont(R.font.poppins_medium));
        amountText.setGravity(android.view.Gravity.END);

        contentLayout.addView(reasonText);
        contentLayout.addView(amountText);
        cardView.addView(contentLayout);
        earningsDetails.addView(cardView);
    }

    private void addDeductionRow(String reason, String amount) {
        CardView cardView = new CardView(this);
        CardView.LayoutParams cardParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(4f);
        cardView.setRadius(8f);
        cardView.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));

        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setPadding(16, 16, 16, 16);
        contentLayout.setWeightSum(2);

        TextView reasonText = new TextView(this);
        LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        reasonText.setLayoutParams(reasonParams);
        reasonText.setText(reason);
        reasonText.setTextSize(14);
        reasonText.setTextColor(getResources().getColor(android.R.color.black));
        reasonText.setTypeface(getResources().getFont(R.font.poppins_medium));

        TextView amountText = new TextView(this);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        amountText.setLayoutParams(amountParams);
        amountText.setText(amount);
        amountText.setTextSize(14);
        amountText.setTextColor(getResources().getColor(android.R.color.black));
        amountText.setTypeface(getResources().getFont(R.font.poppins_medium));
        amountText.setGravity(android.view.Gravity.END);

        contentLayout.addView(reasonText);
        contentLayout.addView(amountText);
        cardView.addView(contentLayout);
        deductionsDetails.addView(cardView);
    }

    private void generateSalarySlipPDF() {
        try {
            String salaryUrl = String.format("http://10.0.2.2/worksync/fetch_salary.php?employee_id=%d&month=%d&year=%d",
                    userId, selectedMonth, selectedYear);
            String increasesUrl = String.format("http://10.0.2.2/worksync/get_increases.php?employee_id=%d&start_date=%s&end_date=%s",
                    userId, String.format("%d-%02d-01", selectedYear, selectedMonth),
                    String.format("%d-%02d-%02d", selectedYear, selectedMonth,
                            LocalDate.of(selectedYear, selectedMonth, 1).lengthOfMonth()));

            StringRequest salaryRequest = new StringRequest(Request.Method.GET, salaryUrl,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (!"success".equals(status)) {
                                Toast.makeText(this, "No salary data available", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            JSONObject data = jsonResponse.getJSONObject("data");

                            // جلب بيانات الزيادات
                            StringRequest increasesRequest = new StringRequest(Request.Method.GET, increasesUrl,
                                    increasesResponse -> {
                                        try {
                                            JSONObject increasesJsonResponse = new JSONObject(increasesResponse);
                                            String increasesStatus = increasesJsonResponse.getString("status");
                                            List<JSONObject> permanentIncreases = new ArrayList<>();
                                            List<JSONObject> temporaryIncreases = new ArrayList<>();

                                            if (increasesStatus.equals("success")) {
                                                JSONArray increasesArray = increasesJsonResponse.getJSONArray("increases");
                                                for (int i = 0; i < increasesArray.length(); i++) {
                                                    JSONObject increase = increasesArray.getJSONObject(i);
                                                    String type = increase.getString("type");
                                                    if (type.equalsIgnoreCase("permanent")) {
                                                        permanentIncreases.add(increase);
                                                    } else if (type.equalsIgnoreCase("temporary")) {
                                                        temporaryIncreases.add(increase);
                                                    }
                                                }
                                            }

                                            // إنشاء PDF
                                            Document document = new Document();
                                            String fileName = String.format("Dermadoc_SalarySlip_%d_%02d_%d.pdf", userId, selectedMonth, selectedYear);
                                            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                            File file = new File(downloadsDir, fileName);
                                            PdfWriter.getInstance(document, new FileOutputStream(file));
                                            document.open();

                                            // بيانات الراتب
                                            String companyName = data.getString("company_name");
                                            String employeeName = data.getString("employee_fullname");
                                            String employeeCode = data.getString("employee_code");
                                            String department = data.getString("department_name");
                                            String designation = data.getString("job_title");
                                            String periodStart = data.getString("period_start");
                                            String periodEnd = data.getString("period_end");
                                            String salaryType = data.getString("salary_structure_type");
                                            double regularHourRate = data.optDouble("regular_hour_rate", 0.0);
                                            double overtimeHourRate = data.optDouble("overtime_hour_rate", 0.0);
                                            double baseSalary = data.optDouble("base_salary", 0.0);
                                            double regularSalary = data.optDouble("regular_salary", 0.0);
                                            double overtimeSalary = data.optDouble("overtime_salary", 0.0);
                                            double overtimeHours = data.optDouble("overtime_hours", 0.0);
                                            double regularHours = data.optDouble("regular_hours", 0.0);
                                            int absentDays = data.optInt("absent_days", 0);
                                            double bonus = data.optDouble("bonus", 0.0);
                                            double salary_increment = data.optDouble("salary_increment", 0.0);
                                            double netSalary = data.optDouble("net_salary", 0.0);
                                            String expectedHoursPerDay = data.getString("expected_hours_per_day");
                                            double hoursPerDay = Double.parseDouble(expectedHoursPerDay);
                                            int expectedWorkingDays = data.optInt("expected_working_days", 0);
                                            double missingHours = data.optDouble("missing_hours", 0.0);
                                            double deductions = missingHours * regularHourRate;
                                            double totalEarnings = salaryType.equalsIgnoreCase("per hour") ?
                                                    (regularSalary + overtimeSalary + bonus + salary_increment) :
                                                    (baseSalary + overtimeSalary + bonus + salary_increment);

                                            String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(
                                                    new SimpleDateFormat("MM").parse(String.valueOf(selectedMonth)));
                                            Paragraph title = new Paragraph(
                                                    String.format("%s\nSalary Slip of %s %d", companyName, monthName, selectedYear),
                                                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD)
                                            );
                                            title.setAlignment(Element.ALIGN_CENTER);
                                            document.add(title);
                                            document.add(new Paragraph(" "));

                                            // جدول المعلومات
                                            PdfPTable infoTable = new PdfPTable(2);
                                            infoTable.setWidthPercentage(100);
                                            addInfoCell(infoTable, "Name", employeeName);
                                            addInfoCell(infoTable, "Employee Code", employeeCode);
                                            addInfoCell(infoTable, "Department", department);
                                            addInfoCell(infoTable, "Designation", designation);
                                            addInfoCell(infoTable, "Expected Work Hours", expectedHoursPerDay);
                                            addInfoCell(infoTable, "Expected Work Days", String.valueOf(expectedWorkingDays));
                                            addInfoCell(infoTable, "Period", periodStart + " to " + periodEnd);
                                            addInfoCell(infoTable, "Work Type", salaryType);
                                            if (salaryType.equalsIgnoreCase("per hour")) {
                                                addInfoCell(infoTable, "Regular Hour Rate", String.format("₪%.2f", regularHourRate));
                                                addInfoCell(infoTable, "Overtime Hour Rate", String.format("₪%.2f", overtimeHourRate));
                                            }
                                            document.add(infoTable);
                                            document.add(new Paragraph(" "));

                                            // جدول الزيادات الدائمة
                                            if (!permanentIncreases.isEmpty()) {
                                                PdfPTable permanentIncreasesTable = new PdfPTable(new float[]{1, 3, 2});
                                                permanentIncreasesTable.setWidthPercentage(100);
                                                addTableHeader(permanentIncreasesTable, "Serial No.", "Salary Increment", "Amount ₪");
                                                int serialNo = 1;
                                                for (JSONObject increase : permanentIncreases) {
                                                    double amount = increase.getDouble("amount");
                                                    String reason = increase.optString("reason", "Permanent Increase");
                                                    addTableRow(permanentIncreasesTable, String.valueOf(serialNo++), reason, String.format("%.2f", amount));
                                                }
                                                document.add(permanentIncreasesTable);
                                                document.add(new Paragraph(" "));
                                            }

                                            // جدول الزيادات المؤقتة (البونس)
                                            if (!temporaryIncreases.isEmpty()) {
                                                PdfPTable temporaryIncreasesTable = new PdfPTable(new float[]{1, 3, 2});
                                                temporaryIncreasesTable.setWidthPercentage(100);
                                                addTableHeader(temporaryIncreasesTable, "Serial No.", "Bonus", "Amount ₪");
                                                int serialNo = 1;
                                                for (JSONObject increase : temporaryIncreases) {
                                                    double amount = increase.getDouble("amount");
                                                    String reason = increase.optString("reason", "Bonus");
                                                    String startDate = increase.getString("start_date");
                                                    String endDate = increase.getString("end_date");
                                                    double effectiveAmount = calculateIncreaseForMonth(
                                                            LocalDate.parse(startDate),
                                                            LocalDate.parse(endDate),
                                                            periodStart,
                                                            periodEnd,
                                                            amount
                                                    );
                                                    if (effectiveAmount > 0.0) {
                                                        addTableRow(temporaryIncreasesTable, String.valueOf(serialNo++), reason, String.format("%.2f", effectiveAmount));
                                                    }
                                                }
                                                if (temporaryIncreasesTable.getRows().size() > 1) { // إذا كان هناك صفوف بعد العنوان
                                                    document.add(temporaryIncreasesTable);
                                                    document.add(new Paragraph(" "));
                                                }
                                            }

                                            // جدول الأرباح
                                            PdfPTable earningsTable = new PdfPTable(new float[]{1, 3, 2});
                                            earningsTable.setWidthPercentage(48);
                                            earningsTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                                            addTableHeader(earningsTable, "Serial No.", "Salary Head", "Amount ₪");
                                            int serialNo = 1;
                                            if (salaryType.equalsIgnoreCase("per hour")) {
                                                if (regularHours > 0) {
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Regular Hours", String.format("%.2f", regularHours));
                                                }
                                                if (regularSalary > 0) {
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Regular Salary", String.format("%.2f", regularSalary));
                                                }
                                                if (overtimeSalary > 0) {
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Overtime Hours", String.format("%.2f", overtimeHours));
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Overtime Salary", String.format("%.2f", overtimeSalary));
                                                }
                                            } else {
                                                if (baseSalary > 0) {
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Base Salary", String.format("%.2f", baseSalary));
                                                }
                                                if (overtimeSalary > 0) {
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Overtime Hours", String.format("%.2f", overtimeHours));
                                                    addTableRow(earningsTable, String.valueOf(serialNo++), "Overtime Salary", String.format("%.2f", overtimeSalary));
                                                }
                                            }

                                            if (salaryType.equalsIgnoreCase("base salary")) {
                                                PdfPTable twoTables = new PdfPTable(2);
                                                twoTables.setWidthPercentage(100);

                                                PdfPTable deductionsTable = new PdfPTable(new float[]{1, 3, 2});
                                                deductionsTable.setWidthPercentage(48);
                                                deductionsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                                addTableHeader(deductionsTable, "Serial No.", "Salary Head", "Amount (₪)");
                                                serialNo = 1;
                                                if (missingHours > 0) {
                                                    addTableRow(deductionsTable, String.valueOf(serialNo++),
                                                            "Missing Hours (" + String.format("%.2f", missingHours) + ")",
                                                            String.format("%.2f", missingHours * regularHourRate));
                                                }

                                                PdfPCell left = new PdfPCell(earningsTable);
                                                PdfPCell right = new PdfPCell(deductionsTable);
                                                left.setBorder(Rectangle.NO_BORDER);
                                                right.setBorder(Rectangle.NO_BORDER);
                                                twoTables.addCell(left);
                                                twoTables.addCell(right);
                                                document.add(twoTables);
                                            } else {
                                                earningsTable.setWidthPercentage(100);
                                                document.add(earningsTable);
                                            }

                                            document.add(new Paragraph(" "));

                                            PdfPTable summary = new PdfPTable(2);
                                            summary.setWidthPercentage(100);
                                            addInfoCell(summary, "TOTAL EARNINGS", String.format("%.2f", totalEarnings));
                                            addInfoCell(summary, "NET SALARY", String.format("%.2f", netSalary));
                                            addInfoCell(summary, "NET SALARY IN WORDS", numberToWords(netSalary));
                                            if (salaryType.equalsIgnoreCase("per hour")) {
                                                addInfoCell(summary, "REGULAR HOURS", String.format("%.1f", regularHours));
                                                addInfoCell(summary, "OVERTIME HOURS", String.format("%.1f", overtimeHours));
                                            } else {
                                                addInfoCell(summary, "OVERTIME HOURS", String.format("%.1f", overtimeHours));
                                            }
                                            document.add(summary);

                                            document.close();
                                            Toast.makeText(this, "PDF Generated in Downloads: " + fileName, Toast.LENGTH_LONG).show();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    },
                                    error -> Toast.makeText(this, "Error fetching salary increases: " + error.getMessage(), Toast.LENGTH_SHORT).show());

                            requestQueue.add(increasesRequest);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error parsing salary data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> Toast.makeText(this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show());

            requestQueue.add(salaryRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initiating PDF generation: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, new Font(Font.FontFamily.HELVETICA, 12)));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, new Font(Font.FontFamily.HELVETICA, 12)));
        labelCell.setPadding(5);
        valueCell.setPadding(5);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String serial, String head, String amount) {
        PdfPCell serialCell = new PdfPCell(new Phrase(serial, new Font(Font.FontFamily.HELVETICA, 12)));
        PdfPCell headCell = new PdfPCell(new Phrase(head, new Font(Font.FontFamily.HELVETICA, 12)));
        PdfPCell amountCell = new PdfPCell(new Phrase("₪" + amount, new Font(Font.FontFamily.HELVETICA, 12)));
        serialCell.setPadding(5);
        headCell.setPadding(5);
        amountCell.setPadding(5);
        table.addCell(serialCell);
        table.addCell(headCell);
        table.addCell(amountCell);
    }

    private String numberToWords(double amount) {
        if (amount == 0) return "Zero";

        long integerPart = (long) amount;
        int decimalPart = (int) ((amount - integerPart) * 100);

        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        String[] teens = {"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        String[] thousands = {"", "Thousand", "Million", "Billion"};

        StringBuilder words = new StringBuilder();
        if (integerPart == 0) {
            words.append("Zero");
        } else {
            List<String> parts = new ArrayList<>();
            int thousandIndex = 0;

            while (integerPart > 0) {
                int chunk = (int) (integerPart % 1000);
                if (chunk > 0) {
                    String chunkWords = convertChunk(chunk, units, teens, tens);
                    if (!chunkWords.isEmpty()) {
                        parts.add(chunkWords + (thousands[thousandIndex].isEmpty() ? "" : " " + thousands[thousandIndex]));
                    }
                }
                integerPart /= 1000;
                thousandIndex++;
            }

            for (int i = parts.size() - 1; i >= 0; i--) {
                words.append(parts.get(i));
                if (i > 0) words.append(" ");
            }
        }

        if (decimalPart > 0) {
            words.append(" and ").append(String.format("%02d", decimalPart)).append("/100");
        }

        return words.toString().trim();
    }

    private String convertChunk(int number, String[] units, String[] teens, String[] tens) {
        StringBuilder chunkWords = new StringBuilder();

        if (number >= 100) {
            chunkWords.append(units[number / 100]).append(" Hundred");
            number %= 100;
            if (number > 0) chunkWords.append(" ");
        }

        if (number >= 10 && number <= 19) {
            chunkWords.append(teens[number - 10]);
        } else if (number >= 20) {
            chunkWords.append(tens[number / 10]);
            number %= 10;
            if (number > 0) chunkWords.append(" ").append(units[number]);
        } else if (number > 0) {
            chunkWords.append(units[number]);
        }

        return chunkWords.toString();
    }

    private void showNoRecordMessage(String message) {
        salaryDetailsContainer.setVisibility(View.GONE);
        fetchSalaryBtn.setSelected(false);
        fetchSalaryBtn.setVisibility(View.GONE);
        downloadPdfBtn.setSelected(false);
        downloadPdfBtn.setVisibility(View.GONE);
        noRecordsImage.setVisibility(View.VISIBLE);
        noRecordsText.setText(message);
        noRecordsText.setVisibility(View.VISIBLE);
        bonusDetails.setVisibility(View.GONE);
        permanentIncreasesDetails.setVisibility(View.GONE);
        deductionsDetails.setVisibility(View.GONE);
        earningsDetails.setVisibility(View.GONE);
        bonusArrow.setVisibility(View.GONE);
        salaryIncrementArrow.setVisibility(View.GONE);
        deductionsArrow.setVisibility(View.GONE);
        earningsArrow.setVisibility(View.GONE);
        bonusHeader.setVisibility(View.GONE);
        permanentIncreasesHeader.setVisibility(View.GONE);
        deductionsHeader.setVisibility(View.GONE);
        earningsHeader.setVisibility(View.GONE);
        isBonusExpanded = false;
        isPermanentIncreasesExpanded = false;
        isDeductionsExpanded = false;
        isEarningsExpanded = false;
    }

    private void fetchAndDisplaySalaryIncreases(int employeeId, String periodStart, String periodEnd) {
        String url = "http://10.0.2.2/worksync/get_increases.php?employee_id=" + employeeId
                + "&start_date=" + periodStart + "&end_date=" + periodEnd;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("Increases Response", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        bonusHeader.setVisibility(View.VISIBLE);
                        bonusArrow.setVisibility(View.VISIBLE);
                        permanentIncreasesHeader.setVisibility(View.VISIBLE);
                        salaryIncrementArrow.setVisibility(View.VISIBLE);

                        if (!status.equals("success")) {
                            bonusDetails.setVisibility(View.GONE);
                            permanentIncreasesDetails.setVisibility(View.GONE);
                            isBonusExpanded = false;
                            isPermanentIncreasesExpanded = false;
                            return;
                        }

                        JSONArray increasesArray = jsonResponse.getJSONArray("increases");

                        List<String> permanentIncreases = new ArrayList<>();
                        List<String> temporaryIncreases = new ArrayList<>();

                        for (int i = 0; i < increasesArray.length(); i++) {
                            JSONObject increase = increasesArray.getJSONObject(i);
                            String type = increase.getString("type");
                            double amount = increase.getDouble("amount");
                            String reason = increase.optString("reason", "");
                            String startDate = increase.getString("start_date");
                            String endDate = increase.getString("end_date");

                            // حساب القيمة الفعلية للزيادة المؤقتة
                            double effectiveAmount = type.equalsIgnoreCase("temporary")
                                    ? calculateIncreaseForMonth(
                                    LocalDate.parse(startDate),
                                    LocalDate.parse(endDate),
                                    periodStart,
                                    periodEnd,
                                    amount)
                                    : amount;

                            // إذا كانت القيمة الفعلية صفر، تخطى هذه الزيادة
                            if (effectiveAmount == 0.0) {
                                continue;
                            }

                            String label = reason.isEmpty()
                                    ? "₪" + String.format("%.2f", effectiveAmount)
                                    : reason + ": ₪" + String.format("%.2f", effectiveAmount);

                            if (type.equalsIgnoreCase("permanent")) {
                                permanentIncreases.add(label);
                            } else if (type.equalsIgnoreCase("temporary")) {
                                temporaryIncreases.add(label);
                            }
                        }

                        permanentIncreasesDetails.removeAllViews();
                        if (!permanentIncreases.isEmpty()) {
                            for (String increase : permanentIncreases) {
                                CardView cardView = new CardView(this);
                                CardView.LayoutParams cardParams = new CardView.LayoutParams(
                                        CardView.LayoutParams.MATCH_PARENT,
                                        CardView.LayoutParams.WRAP_CONTENT
                                );
                                cardParams.setMargins(0, 0, 0, 16);
                                cardView.setLayoutParams(cardParams);
                                cardView.setCardElevation(4f);
                                cardView.setRadius(8f);
                                cardView.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));

                                LinearLayout contentLayout = new LinearLayout(this);
                                contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                contentLayout.setOrientation(LinearLayout.HORIZONTAL);
                                contentLayout.setPadding(16, 16, 16, 16);
                                contentLayout.setWeightSum(2);

                                TextView reasonText = new TextView(this);
                                LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                );
                                reasonText.setLayoutParams(reasonParams);
                                reasonText.setText(increase.contains(":") ? increase.split(":")[0] : "Permanent Increase");
                                reasonText.setTextSize(14);
                                reasonText.setTextColor(getResources().getColor(android.R.color.black));
                                reasonText.setTypeface(getResources().getFont(R.font.poppins_medium));

                                TextView amountText = new TextView(this);
                                LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                );
                                amountText.setLayoutParams(amountParams);
                                amountText.setText(increase.contains(":") ? increase.split(":")[1].trim() : "₪" + String.format("%.2f", 0.0));
                                amountText.setTextSize(14);
                                amountText.setTextColor(getResources().getColor(android.R.color.black));
                                amountText.setTypeface(getResources().getFont(R.font.poppins_medium));
                                amountText.setGravity(android.view.Gravity.END);

                                contentLayout.addView(reasonText);
                                contentLayout.addView(amountText);
                                cardView.addView(contentLayout);
                                permanentIncreasesDetails.addView(cardView);
                            }
                            permanentIncreasesDetails.setVisibility(isPermanentIncreasesExpanded ? View.VISIBLE : View.GONE);
                        } else {
                            permanentIncreasesDetails.setVisibility(View.GONE);
                            isPermanentIncreasesExpanded = false;
                        }

                        bonusDetails.removeAllViews();
                        if (!temporaryIncreases.isEmpty()) {
                            for (String increase : temporaryIncreases) {
                                CardView cardView = new CardView(this);
                                CardView.LayoutParams cardParams = new CardView.LayoutParams(
                                        CardView.LayoutParams.MATCH_PARENT,
                                        CardView.LayoutParams.WRAP_CONTENT
                                );
                                cardParams.setMargins(0, 0, 0, 16);
                                cardView.setLayoutParams(cardParams);
                                cardView.setCardElevation(4f);
                                cardView.setRadius(8f);
                                cardView.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));

                                LinearLayout contentLayout = new LinearLayout(this);
                                contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                ));
                                contentLayout.setOrientation(LinearLayout.HORIZONTAL);
                                contentLayout.setPadding(16, 16, 16, 16);
                                contentLayout.setWeightSum(2);

                                TextView reasonText = new TextView(this);
                                LinearLayout.LayoutParams reasonParams = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                );
                                reasonText.setLayoutParams(reasonParams);
                                reasonText.setText(increase.contains(":") ? increase.split(":")[0] : "Bonus");
                                reasonText.setTextSize(14);
                                reasonText.setTextColor(getResources().getColor(android.R.color.black));
                                reasonText.setTypeface(getResources().getFont(R.font.poppins_medium));

                                TextView amountText = new TextView(this);
                                LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(
                                        0,
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        1
                                );
                                amountText.setLayoutParams(amountParams);
                                amountText.setText(increase.contains(":") ? increase.split(":")[1].trim() : "₪" + String.format("%.2f", 0.0));
                                amountText.setTextSize(14);
                                amountText.setTextColor(getResources().getColor(android.R.color.black));
                                amountText.setTypeface(getResources().getFont(R.font.poppins_medium));
                                amountText.setGravity(android.view.Gravity.END);

                                contentLayout.addView(reasonText);
                                contentLayout.addView(amountText);
                                cardView.addView(contentLayout);
                                bonusDetails.addView(cardView);
                            }
                            bonusDetails.setVisibility(isBonusExpanded ? View.VISIBLE : View.GONE);
                        } else {
                            bonusDetails.setVisibility(View.GONE);
                            isBonusExpanded = false;
                        }

                        Log.d("PermanentIncreases", permanentIncreases.toString());
                        Log.d("TemporaryIncreases", temporaryIncreases.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing salary increases", Toast.LENGTH_SHORT).show();
                        bonusHeader.setVisibility(View.VISIBLE);
                        bonusArrow.setVisibility(View.VISIBLE);
                        permanentIncreasesHeader.setVisibility(View.VISIBLE);
                        salaryIncrementArrow.setVisibility(View.VISIBLE);
                        bonusDetails.setVisibility(View.GONE);
                        permanentIncreasesDetails.setVisibility(View.GONE);
                        isBonusExpanded = false;
                        isPermanentIncreasesExpanded = false;
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error fetching salary increases", Toast.LENGTH_SHORT).show();
                    bonusHeader.setVisibility(View.VISIBLE);
                    bonusArrow.setVisibility(View.VISIBLE);
                    permanentIncreasesHeader.setVisibility(View.VISIBLE);
                    salaryIncrementArrow.setVisibility(View.VISIBLE);
                    bonusDetails.setVisibility(View.GONE);
                    permanentIncreasesDetails.setVisibility(View.GONE);
                    isBonusExpanded = false;
                    isPermanentIncreasesExpanded = false;
                });
        requestQueue.add(request);
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

        // إذا كانت فترة الراتب خارج فترة الزيادة، يتم إرجاع 0
        if (salaryEndDate.isBefore(increaseStart) || salaryStartDate.isAfter(increaseEnd)) {
            return 0.0;
        }

        // حساب عدد الأيام في فترة الراتب
        long salaryPeriodDays = ChronoUnit.DAYS.between(salaryStartDate, salaryEndDate.plusDays(1));

        // إذا كانت فترة الزيادة تغطي فترة الراتب بالكامل وكانت الفترة بين 28 و31 يومًا
        if (!salaryStartDate.isBefore(increaseStart) && !salaryEndDate.isAfter(increaseEnd)) {
            if (salaryPeriodDays >= 28 && salaryPeriodDays <= 31) {
                return totalIncreaseAmount;
            }
        }

        LocalDate effectiveStart = increaseStart.isAfter(salaryStartDate) ? increaseStart : salaryStartDate;
        LocalDate effectiveEnd = increaseEnd.isBefore(salaryEndDate) ? increaseEnd : salaryEndDate;

        // إذا كان تاريخ البداية بعد تاريخ النهاية، يتم إرجاع 0
        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0.0;
        }

        // حساب عدد الأيام المتداخلة
        long overlappingDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd.plusDays(1));

        // حساب القيمة النسبية للزيادة بناءً على نسبة الأيام المتداخلة
        return totalIncreaseAmount * ((double) overlappingDays / salaryPeriodDays);
    }
}