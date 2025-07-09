package com.example.worksyck;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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
    String status = "Pending";

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

        String[] leaveTypes = {
                "Select Leave Type",
                "Sick Leave",
                "Annual Leave",
                "Emergency Leave",
                "Marriage Leave",
                "Bereavement Leave",
                "Maternity Leave",
                "Unpaid Leave"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, leaveTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(adapter);

        selectDateButton.setOnClickListener(v -> showStartDatePicker());
        chooseFileButton.setOnClickListener(v -> openFilePicker());
        submitButton.setOnClickListener(v -> {
            if (!validateForm()) return;
            sendLeaveRequestToServer();
        });
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void sendLeaveRequestToServer() {
        String leaveType = leaveTypeSpinner.getSelectedItem().toString();
        if (leaveType.equals("Select Leave Type")) {
            Toast.makeText(this, "Please select a valid leave type", Toast.LENGTH_SHORT).show();
            return;
        }
        String reason = reasonTextView.getText().toString().trim();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            long diffMillis = sdf.parse(endDate).getTime() - sdf.parse(startDate).getTime();
            long daysRequested = TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS) + 1;

            int maxDays = 30; // default
            switch (leaveType) {
                case "Sick Leave": maxDays = 14; break;
                case "Annual Leave": maxDays = 21; break;
                case "Emergency Leave": maxDays = 7; break;
                case "Marriage Leave": maxDays = 3; break;
                case "Bereavement Leave": maxDays = 3; break;
                case "Maternity Leave": maxDays = 70; break;
                case "Unpaid Leave": maxDays = -1; break;  // <-- بدون حد أقصى
            }

            checkUsedDaysAndSubmit(leaveType, daysRequested, maxDays, reason);

        } catch (Exception e) {
            Toast.makeText(this, "Date parsing error", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkUsedDaysAndSubmit(String leaveType, long daysRequested, int maxDays, String reason) {
        String url = "http://10.0.2.2/worksync/get_total_leave_days.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        int usedDays = json.optInt("total_days", 0);

                        if (maxDays != -1 && usedDays + daysRequested > maxDays) {
                            Toast.makeText(this, "You have already used " + usedDays + " days for " + leaveType +
                                    ". Max allowed is " + maxDays + " days.", Toast.LENGTH_LONG).show();
                        } else {
                            submitLeaveRequestToServer(leaveType, reason);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Server response error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error checking used days: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("leave_type", leaveType);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private void submitLeaveRequestToServer(String leaveType, String reason) {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/worksync/insert_leave_request.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(LeaveRequests.this, "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    setResult(RESULT_OK);
                },
                error -> Toast.makeText(LeaveRequests.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId)); // <--- هنا تضيف user_id
                params.put("leave_type", leaveType);
                params.put("start_date", startDate);
                params.put("end_date", endDate);
                params.put("reason", reason);
                params.put("status", status);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose a file"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data != null && data.getData() != null) {
                fileUri = data.getData();
                String fileName = getFileName(fileUri);
                fileNameTextView.setText(fileName);
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
            long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1;

            String leaveType = leaveTypeSpinner.getSelectedItem().toString();
            int maxDays = 30;

            switch (leaveType) {
                case "Sick Leave":
                    maxDays = 14;
                    break;
                case "Annual Leave":
                    maxDays = 21;
                    break;
                case "Emergency Leave":
                    maxDays = 7;
                    break;
                case "Marriage Leave":
                    maxDays = 3;
                    break;
                case "Bereavement Leave":
                    maxDays = 3;
                    break;
                case "Maternity Leave":
                    maxDays = 70;
                    break;
                case "Unpaid Leave":
                    maxDays = -1;  // <-- بدون حد أقصى
                    break;
            }

            if (maxDays != -1 && days > maxDays) {
                Toast.makeText(this, "The maximum allowed days for " + leaveType + " is " + maxDays + " days", Toast.LENGTH_LONG).show();
                return;
            }

            endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(endCalendar.getTime());
            selectDateButton.setText(startDate + " to " + endDate);

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        endPicker.setTitle("Select End Date");
        endPicker.show();
    }

    private boolean validateForm() {
        String reason = reasonTextView.getText().toString().trim();

        if (leaveTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a leave type", Toast.LENGTH_SHORT).show();
            leaveTypeSpinner.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            return false;
        } else {
            leaveTypeSpinner.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select a valid date range", Toast.LENGTH_SHORT).show();
            selectDateButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            return false;
        } else {
            selectDateButton.setTextColor(getResources().getColor(android.R.color.black));
        }

        Calendar today = Calendar.getInstance();
        if (calendar.before(today)) {
            Toast.makeText(this, "Start date cannot be in the past", Toast.LENGTH_SHORT).show();
            selectDateButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            return false;
        }

        if (endCalendar.compareTo(calendar) < 0 || endCalendar.before(calendar)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            selectDateButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            return false;
        }

        long diff = endCalendar.getTimeInMillis() - calendar.getTimeInMillis();
        if (TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) > 30) {
            Toast.makeText(this, "Leave duration must be less than 30 days", Toast.LENGTH_SHORT).show();
            selectDateButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            return false;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Please provide a reason for leave", Toast.LENGTH_SHORT).show();
            reasonTextView.setError("This field is required");
            reasonTextView.setBackgroundResource(R.drawable.edittext_error_border);
            return false;
        } else {
            reasonTextView.setError(null);
            reasonTextView.setBackgroundResource(R.drawable.edittext_normal_border);
        }

        return true;
    }



    private void clearFields() {
        leaveTypeSpinner.setSelection(0);
        selectDateButton.setText("Select Date");
        reasonTextView.setText("");
        fileNameTextView.setText("");
        startDate = "";
        endDate = "";
        fileUri = null;
        isStartSelected = false;
    }
}
