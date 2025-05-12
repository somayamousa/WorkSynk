package com.example.worksyck;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LeaveRequests extends AppCompatActivity {

    Spinner leaveTypeSpinner;
    Button selectDateButton, chooseFileButton, submitButton;
    EditText reasonTextView;
    TextView fileNameTextView;

    String startDate = "";
    String endDate = "";
    Uri fileUri = null;

    final Calendar calendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();
    boolean isStartSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_requests);

        leaveTypeSpinner = findViewById(R.id.leaveTypeSpinner);
        selectDateButton = findViewById(R.id.selectDateButton);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        submitButton = findViewById(R.id.submitButton);
        reasonTextView = findViewById(R.id.reasonTextView);
        fileNameTextView = findViewById(R.id.fileNameTextView);

        submitButton.setOnClickListener(v -> {
            // الانتقال إلى صفحة LeaveOverview عند النقر على زر Submit
            Intent intent = new Intent(LeaveRequests.this, LeaveOverview.class);
            startActivity(intent);
        });


        // إعداد أنواع الإجازة
        String[] leaveTypes = {
                "Select Leave Type",
                "Sick Leave",
                "Casual Leave",
                "Annual Leave",
                "Emergency Leave",
                "Maternity Leave",
                "Unpaid Leave"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, leaveTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(adapter);

        // التعامل مع زر اختيار التاريخ
        selectDateButton.setOnClickListener(v -> showStartDatePicker());

        // زر الإرسال
        submitButton.setOnClickListener(v -> {
            if (!validateForm()) return;
            sendLeaveRequestToServer();
        });

        // زر الرجوع
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // تفعيل اختيار المستند
        chooseFileButton.setOnClickListener(v -> openFilePicker());
    }

    private void sendLeaveRequestToServer() {
        String leaveType = leaveTypeSpinner.getSelectedItem().toString();
        String reason = reasonTextView.getText().toString().trim();

        String url = "http://10.0.2.2/leave_requests/insert_leave_request.php"; // تعديل عنوان الخادم

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(LeaveRequests.this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
                        clearFields();  // Clear all fields after submission
                    }
                },
                error -> Toast.makeText(LeaveRequests.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("leave_type", leaveType);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("reason", reason);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void openFilePicker() {
        // فتح مستعرض الملفات لاختيار المستند
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");  // يسمح باختيار أي نوع من الملفات
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose a file"), 1); // طلب اختيار الملف
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            // عند اختيار الملف
            if (data != null && data.getData() != null) {
                fileUri = data.getData();  // الحصول على URI للملف
                String fileName = getFileName(fileUri);  // الحصول على اسم الملف
                fileNameTextView.setText(fileName); // عرض اسم الملف في TextView
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        String[] projection = { android.provider.MediaStore.Images.Media.DISPLAY_NAME };
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(projection[0]);
                result = cursor.getString(columnIndex);
            }
        }
        return result;
    }

    private void showStartDatePicker() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        DatePickerDialog startPicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            startDate = sdf.format(calendar.getTime());
            isStartSelected = true;
            showEndDatePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        startPicker.setTitle("Select Start Date");
        startPicker.show();
    }

    private void showEndDatePicker() {
        endCalendar.setTimeInMillis(System.currentTimeMillis());
        DatePickerDialog endPicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            endCalendar.set(year, month, dayOfMonth);

            if (endCalendar.compareTo(calendar) < 0) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                return;
            }

            long diff = endCalendar.getTimeInMillis() - calendar.getTimeInMillis();
            if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 30) {
                Toast.makeText(this, "Leave duration must be less than 30 days", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            endDate = sdf.format(endCalendar.getTime());
            selectDateButton.setText(startDate + " to " + endDate);
        }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH));

        endPicker.setTitle("Select End Date");
        endPicker.show();
    }

    private boolean validateForm() {
        String reason = reasonTextView.getText().toString().trim();

        if (leaveTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a leave type", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select a valid date range", Toast.LENGTH_SHORT).show();
            return false;
        }

        Calendar today = Calendar.getInstance();
        if (calendar.before(today)) {
            Toast.makeText(this, "Start date cannot be in the past", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (endCalendar.compareTo(calendar) < 0 || endCalendar.before(calendar)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            return false;
        }

        long diff = endCalendar.getTimeInMillis() - calendar.getTimeInMillis();
        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 30) {
            Toast.makeText(this, "Leave duration must be less than 30 days", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Please provide a reason for leave", Toast.LENGTH_SHORT).show();
            return false;
        }


        submitButton.setOnClickListener(v -> {
            if (!validateForm()) return;
            sendLeaveRequestToServer();

            // إرسال البيانات إلى صفحة Overview
            Intent intent = new Intent(LeaveRequests.this, LeaveOverview.class);
            intent.putExtra("leaveType", leaveTypeSpinner.getSelectedItem().toString());
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);
            intent.putExtra("reason", reasonTextView.getText().toString());
            startActivity(intent);
        });



        return true;
    }
    private void clearFields() {
        leaveTypeSpinner.setSelection(0);  // Clear overtime hours input
        reasonTextView.setText("");  // Clear reason input
        selectDateButton.setText("Select Date");  // Reset the date button
        fileNameTextView.setText("");  // Clear file name text view
    }

}
