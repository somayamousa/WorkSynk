package com.example.worksyck;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OvertimeRequests extends AppCompatActivity {

    private Button selectDateButton, chooseFileButton, submitButton;
    private EditText overtimeHoursEditText, reasonTextView;
    private TextView fileNameTextView;
    private ImageView backButton;

    private String overtimeDate = "";
    private String overtimeHours = "";
    private String overtimeReason = "";
    private Uri fileUri = null;
    private String fileName = "";
    String status = "Pending";
    private final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overtime_requests);

        // Initialize UI elements
        selectDateButton = findViewById(R.id.selectDateButton);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        submitButton = findViewById(R.id.submitButton);
        overtimeHoursEditText = findViewById(R.id.overtimeHoursEditText);
        reasonTextView = findViewById(R.id.reasonTextView);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        backButton = findViewById(R.id.backButton);

        // Handle back button click
        backButton.setOnClickListener(v -> finish());

        // Set up date picker
        selectDateButton.setOnClickListener(v -> showDatePicker());

        // Handle file picker
        chooseFileButton.setOnClickListener(v -> openFilePicker());

        // Handle submit button click
        submitButton.setOnClickListener(v -> {
            if (!validateForm()) return;
            sendOvertimeRequestToServer(); // Only send data to the server
        });
    }

    // Show the date picker for overtime date
    private void showDatePicker() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            overtimeDate = sdf.format(calendar.getTime());
            selectDateButton.setText(overtimeDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePicker.setTitle("Select Overtime Date");
        datePicker.show();
    }

    // Open file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Choose a file"), 1);
    }

    // Get file name from URI
    private String getFileName(Uri uri) {
        String result = null;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                result = cursor.getString(nameIndex);
            }
        }
        return result != null ? result : "attachment";
    }

    // Validate the form inputs
    private boolean validateForm() {
        String hoursStr = overtimeHoursEditText.getText().toString().trim();
        overtimeReason = reasonTextView.getText().toString().trim();

        // Check if the overtime date is selected
        if (overtimeDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            selectDateButton.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            return false;
        } else {
            selectDateButton.setTextColor(getResources().getColor(android.R.color.black));
        }


        // Check if the overtime hours input is empty
        if (hoursStr.isEmpty()) {
            Toast.makeText(this, "Please enter number of hours", Toast.LENGTH_SHORT).show();
            overtimeHoursEditText.setError("Required");
            overtimeHoursEditText.setBackgroundResource(R.drawable.edittext_error_border);
            return false;
        }


        // Parse the hours to float and validate
        try {
            overtimeHours = hoursStr;  // Store the overtime hours as string
            float hours = Float.parseFloat(overtimeHours);  // Convert to float

            // Check if overtime hours are within the valid range (greater than 0 and less than or equal to 2)
            if (hours <= 0 || hours > 2) {
                Toast.makeText(this, "Hours must be between 0 and 2", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid hours input", Toast.LENGTH_SHORT).show();
            return false;
        }


        // Check if the reason is provided
        if (overtimeReason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason", Toast.LENGTH_SHORT).show();
            reasonTextView.setError("Required");
            reasonTextView.setBackgroundResource(R.drawable.edittext_error_border);
            return false;
        } else {
            reasonTextView.setError(null);
            reasonTextView.setBackgroundResource(R.drawable.edittext_normal_border);

            return true;
        }
    }
    // Send the overtime request to the server
    private void sendOvertimeRequestToServer() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://10.0.2.2/worksync/insert_overtime_request.php"; // Change the URL to match your server's path

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(OvertimeRequests.this, "Request submitted successfully", Toast.LENGTH_SHORT).show();
                    clearFields();  // Clear all fields after submission
                    setResult(RESULT_OK);

                },
                error -> Toast.makeText(OvertimeRequests.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId)); // <--- هنا تضيف user_id
                params.put("overtime_date", overtimeDate);
                params.put("hours", overtimeHours);
                params.put("reason", overtimeReason);
                params.put("status", status);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Method to clear all fields
    private void clearFields() {
        overtimeHoursEditText.setText("");  // Clear overtime hours input
        reasonTextView.setText("");  // Clear reason input
        selectDateButton.setText("Select Overtime Date");  // Reset the date button
        fileNameTextView.setText("");  // Clear file name text view
        // Reset background colors after clearing the fields
        overtimeHoursEditText.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        reasonTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        selectDateButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));


    }

}
