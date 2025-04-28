package com.example.worksyck;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;

import java.util.*;

public class OvertimeRequests extends AppCompatActivity {

    private Button selectDateButton, chooseFileButton, submitButton;
    private EditText hoursWorkedEditText, reasonEditText;
    private TextView fileNameTextView, selectedDateTextView;
    private String selectedDate = "";

    private static final int PICK_FILE_REQUEST = 1;
    private Uri selectedFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overtime_requests);
        setBackButtonListener();
        selectDateButton = findViewById(R.id.selectDateButton);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        submitButton = findViewById(R.id.submitButton);
        hoursWorkedEditText = findViewById(R.id.hoursWorkedEditText);
        reasonEditText = findViewById(R.id.reasonEditText);
        fileNameTextView = findViewById(R.id.fileNameTextView);

        selectDateButton.setOnClickListener(v -> showDatePickerDialog());
        chooseFileButton.setOnClickListener(v -> openFilePicker());
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }
    private void setBackButtonListener() {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed()); // This will call the default back button functionality
    }


    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            selectDateButton.setText(selectedDate);
        }, year, month, day).show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            String fileName = selectedFileUri.getLastPathSegment();
            fileNameTextView.setText(fileName != null ? fileName : "File selected");
        }
    }

    private void validateAndSubmit() {
        String hours = hoursWorkedEditText.getText().toString().trim();
        String reason = reasonEditText.getText().toString().trim();

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (hours.isEmpty()) {
            hoursWorkedEditText.setError("Enter number of hours");
            return;
        }

        if (reason.isEmpty()) {
            reasonEditText.setError("Enter reason for overtime");
            return;
        }

        float hoursWorked = Float.parseFloat(hours);

        if (hoursWorked > 2) {
            showExceedLimitDialog(hoursWorked, reason);
        } else {
            submitOvertimeRequest(hoursWorked, reason, false); // Regular request
        }
    }

    private void showExceedLimitDialog(float hours, String reason) {
        new AlertDialog.Builder(this)
                .setTitle("Exceeds Daily Limit")
                .setMessage("You entered " + hours + " hours, which exceeds the 2-hour daily limit. Do you want to submit a special request?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    submitOvertimeRequest(hours, reason, true); // Special request
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitOvertimeRequest(float hours, String reason, boolean isSpecialRequest) {
        String requestType = isSpecialRequest ? "Special Overtime Request" : "Regular Overtime Request";

        Toast.makeText(this, "Leave request submitted successfully", Toast.LENGTH_LONG).show();

        // Clear all fields after successful submission
        clearFields();
    }

    private void clearFields() {
        // Clear the input fields
        hoursWorkedEditText.setText("");
        reasonEditText.setText("");
        fileNameTextView.setText("No file selected");
        selectedDate = "";
        selectDateButton.setText("Select Date");
    }
}
