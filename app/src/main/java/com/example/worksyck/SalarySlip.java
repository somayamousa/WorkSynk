package com.example.worksyck;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private TextView startDateText, endDateText;
    private Button createSalarySlipBtn;
    private String startDate, endDate;
    private RequestQueue requestQueue;
    private String specialDayPolicy;
    private EditText holidayHourRateInput;
    double holidayHourRate ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salary_slip);
        /**** Set Views*/
        startDateText = findViewById(R.id.startDateText);
        endDateText = findViewById(R.id.endDateText);
        createSalarySlipBtn = findViewById(R.id.createSalarySlipBtn);
        holidayHourRateInput = findViewById(R.id.holidayHourRateInput);
        /** initialized Parameters**/
        requestQueue = Volley.newRequestQueue(this);
        startDate = "";
        endDate = "";
        SharedPreferences sharedPreferences = getSharedPreferences("WorkHourPolicies", MODE_PRIVATE);
        int selectedHolidayPolicyId = sharedPreferences.getInt("holidayPolicy", R.id.holiday_normal); // default: normal
        specialDayPolicy = "normal"; // default
        if (selectedHolidayPolicyId == R.id.holiday_normal) {
            specialDayPolicy = "normal";
        } else if (selectedHolidayPolicyId == R.id.holiday_overtime) {
            specialDayPolicy = "overtime";
        } else if (selectedHolidayPolicyId == R.id.holiday_special) {
            specialDayPolicy = "custom_rate";
            holidayHourRateInput.setVisibility(View.VISIBLE);
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
                //  Check start date and end date are not empty
                if (startDate.isEmpty() || endDate.isEmpty()) {
                    Toast.makeText(SalarySlip.this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                    return;
                }
                //  Check that startDate is before endDate
                if (!isStartBeforeEnd(startDate, endDate)) {
                    Toast.makeText(SalarySlip.this, "Start date must be before end date", Toast.LENGTH_SHORT).show();
                    return;
                }
                //  Check that dates are not in the future
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
                /**fetch employee information from the server**/
                int employee_id = 9;
                fetchEmployeeDetails(employee_id);
            }
        });
    }
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

    public void  fetchAttendanceRecord(int employee_id, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, String salaryStructureType, double baseSalary) {
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
                            fetchHolidaysInPeriod(employee_id, attendanceList, expectedHoursPerDay, normalHourRate, overtimeHourRate, salaryStructureType, baseSalary);
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
    private void fetchHolidaysInPeriod(int employeeId, List<AttendanceRecord> attendanceList, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, String salaryStructureType, double baseSalary) {
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
                                Log.d("holidays" , holidaysDates.toString());
                            } else {
                                Log.d("Holidays", "No holidays found");
                            }
                            fetchSalaryIncreases(employeeId, expectedHoursPerDay, normalHourRate, overtimeHourRate, attendanceList, holidaysDates, salaryStructureType, baseSalary);
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

    private void fetchWorkingsDays(int employeeId, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, List<AttendanceRecord> attendanceList, Set<String> holidaysDates, String salaryStructureType, double baseSalary, double totalSalaryIncrease, List<SalaryIncrease> temporaryIncreases, List<SalaryIncrease> permanentIncreases, ArrayList<Leave> paidLeaves) {
        Log.d("Debug", "specialDayPolicy = " + specialDayPolicy);
        Log.d("Hi Fetch Workings Days","Hi");
        {
            String url = "http://10.0.2.2/worksync/get_working_days.php?employee_id="+employeeId;
            Log.d("Hi Fetch Workings Days","Hi");
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Hi Fetch Workings Days",response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String status = jsonObject.getString("status");
                                if (status.equals("success")) {
                                    if (!jsonObject.getString("message").equals("No working days found")){
                                        JSONArray jsonArray = jsonObject.getJSONArray("data");
                                        HashMap<Integer, Integer> workDay = new HashMap<>();
                                        for (int i =0; i<jsonArray.length();i++){
                                            int dayOfWeek=  Integer.parseInt(jsonArray.getJSONObject(i).getString("day_of_week"));
                                            int isWorkDay=Integer.parseInt(jsonArray.getJSONObject(i).getString("is_working_day"));
                                            workDay.put(dayOfWeek,isWorkDay);
                                        }
                                        switch(salaryStructureType.toLowerCase()) {
                                            case "per hour": {
                                                double finalSalary = 0;
                                                double DayRate = 0;
                                                double overTimeWorkedHourPerDay = 0;
                                                double normalWorkedHour = 0;

                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                                                for (int i = 0; i < attendanceList.size(); i++) {
                                                    AttendanceRecord record = attendanceList.get(i);
                                                    double totalWorkedHourPerDay = record.getWorkedHours();

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

                                                        if (isPaidLeave) {
                                                            DayRate = expectedHoursPerDay * normalHourRate;
                                                        } else {
                                                            if (!isSpecialDay || specialDayPolicy.equalsIgnoreCase("normal")) {
                                                                overTimeWorkedHourPerDay = totalWorkedHourPerDay - expectedHoursPerDay;
                                                                if (overTimeWorkedHourPerDay < 0) {
                                                                    overTimeWorkedHourPerDay = 0;
                                                                }
                                                                normalWorkedHour = totalWorkedHourPerDay - overTimeWorkedHourPerDay;
                                                                DayRate = (normalWorkedHour * normalHourRate) + (overTimeWorkedHourPerDay * overtimeHourRate);
                                                            } else if (specialDayPolicy.equalsIgnoreCase("overtime")) {
                                                                DayRate = (totalWorkedHourPerDay * overtimeHourRate);
                                                            } else if (specialDayPolicy.equalsIgnoreCase("custom_rate")) {
                                                                DayRate = totalWorkedHourPerDay * normalHourRate * holidayHourRate;
                                                                Log.d("Holiday rate", String.valueOf(holidayHourRate));
                                                            }
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

                                                generateSalarySlipPdf("Sojood Salah Aldeen", "Jun", 2800, 2800);
                                                Log.d("Salary", "Employee ID: " + employeeId + " | Final Salary: " + finalSalary);
                                                Log.d("totalSalaryIncrease", "Employee ID: " + employeeId + " | totalSalaryIncrease: " + totalSalaryIncrease);
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
                                                Date end=null;
                                                try {
                                                    start = sdf.parse(startDate);
                                                    end=sdf.parse(endDate);
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
                                                                // اليوم غياب فعلي، ينقص من الراتب
                                                                totalMissingHours += expectedHoursPerDay;
                                                                totalMissingRate += expectedHoursPerDay * normalHourRate;
                                                                absentDates.add(dateStr);
                                                            }
                                                            // إذا إجازة مدفوعة ما في خصم ولا نضيف يوم غياب
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
                                                        } else {
                                                        }
                                                    }

                                                    cal.add(Calendar.DAY_OF_MONTH, 1);
                                                }

                                                double PeriodSalary = baseSalary - totalMissingRate + totalOvertimeRate +totalSalaryIncrease;

                                                Log.d("Final Salary", "Final Salary = " + PeriodSalary);
                                                Log.d("OverTime Rate", " = " + totalOvertimeRate);
                                                Log.d("Missing Rate", " = " + totalMissingRate);
                                                Log.d("Total Salary Increase ", String.valueOf(totalSalaryIncrease));
                                                Log.d("Absent Dates", "Absent Days: " + absentDates.toString());

                                                if (PeriodSalary < 0) {
                                                    Log.w("SalaryCalc", "Salary is negative! Adjusting to zero The Employee Does not Working Any Day."+ employeeId);
                                                    PeriodSalary = 0;
                                                }
                                            }
                                            break;
                                            default: {
                                                Log.e("SalaryType", "Unknown salary structure type: " + salaryStructureType);
                                            }
                                            break;
                                        }
                                    }else {
                                        Log.d("Message",jsonObject.getString("message"));
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
    private void generateSalarySlipPdf(String employeeName, String month, double baseSalary, double totalSalary) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 400, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);
        int y = 25;
        canvas.drawText("Salary Slip", 80, y, paint);
        y += 25;
        canvas.drawText("Employee Name: " + employeeName, 10, y, paint);
        y += 20;
        canvas.drawText("Month: " + month, 10, y, paint);
        y += 20;
        canvas.drawText("Base Salary: $" + baseSalary, 10, y, paint);
        y += 20;
        canvas.drawText("Total Salary: $" + totalSalary, 10, y, paint);
        document.finishPage(page);
        String fileName = "SalarySlip_" + employeeName.replace(" ", "_") + ".pdf";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            ContentResolver resolver = getContentResolver();
            Uri pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

            if (pdfUri != null) {
                try (OutputStream outputStream = resolver.openOutputStream(pdfUri)) {
                    document.writeTo(outputStream);
                    Toast.makeText(this, "PDF saved to Downloads", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error writing PDF", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
            }
        } else {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                document.writeTo(outputStream);
                Toast.makeText(this, "PDF saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error writing PDF", Toast.LENGTH_SHORT).show();
            }
        }
        document.close();
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
                            fetchAttendanceRecord(employee_id,expectedHoursPerDay , normalHourRate , overtimeHourRate,salary_structure_type,baseSalary);
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
    private void fetchPaidLeaveDates(int employeeId, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, List<AttendanceRecord> attendanceList, Set<String> holidaysDates, String salaryStructureType, double baseSalary, double totalSalaryIncrease, List<SalaryIncrease> temporaryIncreases, List<SalaryIncrease> permanentIncreases) {
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
                fetchWorkingsDays(employeeId, expectedHoursPerDay, normalHourRate, overtimeHourRate, attendanceList, holidaysDates, salaryStructureType, baseSalary,totalSalaryIncrease,temporaryIncreases,permanentIncreases,paidLeaves);

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

    private void fetchSalaryIncreases(int employeeId, double expectedHoursPerDay, double normalHourRate, double overtimeHourRate, List<AttendanceRecord> attendanceList, Set<String> holidaysDates, String salaryStructureType, double baseSalary) {
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
                fetchPaidLeaveDates(employeeId, expectedHoursPerDay, normalHourRate, overtimeHourRate, attendanceList, holidaysDates, salaryStructureType, baseSalary,totalSalaryIncrease,temporaryIncreases,permanentIncreases);


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