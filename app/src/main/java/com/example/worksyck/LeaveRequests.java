package com.example.worksyck;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.database.Cursor;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.*;

public class LeaveRequests extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 101;

    private Spinner leaveTypeSpinner;
    private EditText reasonEditText;
    private Button selectDateButton, submitButton, chooseFileButton;
    private TextView fileNameTextView;
    private List<LeavePeriod> leavePeriods = new ArrayList<>();
    private String selectedTimeRange = null;
    private Uri selectedFileUri = null;

    private final Calendar calendar = Calendar.getInstance();
    private Date selectedStartDate = null;

    private static class LeavePeriod {
        Date startDate;
        Date endDate;

        LeavePeriod(Date startDate, Date endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        String getFormattedPeriod() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return "From: " + sdf.format(startDate) + " To: " + sdf.format(endDate);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leave_requests);
        setBackButtonListener();
        leaveTypeSpinner = findViewById(R.id.leaveTypeSpinner);
        reasonEditText = findViewById(R.id.reasonTextView);
        selectDateButton = findViewById(R.id.selectDateButton);
        submitButton = findViewById(R.id.submitButton);
        chooseFileButton = findViewById(R.id.chooseFileButton);
        fileNameTextView = findViewById(R.id.fileNameTextView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.leave_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveTypeSpinner.setAdapter(adapter);

        selectDateButton.setOnClickListener(v -> showDateSelectionDialog());
        chooseFileButton.setOnClickListener(v -> openFilePicker());
        submitButton.setOnClickListener(v -> validateForm());

    }
    private void setBackButtonListener() {
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed()); // This will call the default back button functionality
    }

    private void showDateSelectionDialog() {
        String[] options = {"Pick Start Date", "Pick End Date"};
        new AlertDialog.Builder(this)
                .setItems(options, (dialog, which) -> {
                    calendar.setTime(new Date());
                    if (which == 0) {
                        openStartDatePicker();
                    } else {
                        openEndDatePicker();
                    }
                })
                .show();
    }

    private void openStartDatePicker() {
        if (selectedStartDate != null) {
            Toast.makeText(this, "Please select end date for the previous start date first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            selectedStartDate = calendar.getTime();
            updateDateButton();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void openEndDatePicker() {
        if (selectedStartDate == null) {
            Toast.makeText(this, "Please select a start date first", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            Date selectedEndDate = calendar.getTime();

            if (selectedEndDate.before(selectedStartDate)) {
                Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
            } else if (isSameDay(selectedEndDate, selectedStartDate)) {
                Toast.makeText(this, "End date cannot be the same as the start date", Toast.LENGTH_SHORT).show();
            } else {
                leavePeriods.add(new LeavePeriod(selectedStartDate, selectedEndDate));
                selectedStartDate = null; // Reset
                updateDateButton();
            }
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        StringBuilder builder = new StringBuilder();

        if (leavePeriods.isEmpty() && selectedStartDate != null) {
            builder.append("Start: ").append(sdf.format(selectedStartDate));
        } else {
            for (LeavePeriod period : leavePeriods) {
                builder.append(period.getFormattedPeriod()).append("\n");
            }
        }

        if (selectedTimeRange != null) {
            builder.append("\nTime: ").append(selectedTimeRange);
        }

        selectDateButton.setText(builder.toString().trim());
    }


    private void askIfAddTime() {
        new AlertDialog.Builder(this)
                .setTitle("Add Time Range")
                .setMessage("Do you want to specify a time range?")
                .setPositiveButton("Yes", (dialog, which) -> openTimePicker())
                .setNegativeButton("No", null)
                .show();
    }

    private void openTimePicker() {
        final Calendar startCalendar = Calendar.getInstance();
        TimePickerDialog startTimePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String startTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                    final Calendar endCalendar = Calendar.getInstance();
                    TimePickerDialog endTimePicker = new TimePickerDialog(this,
                            (view1, endHour, endMinute) -> {
                                String endTime = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute);
                                selectedTimeRange = startTime + " - " + endTime;
                                updateDateButton();
                            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), true);
                    endTimePicker.setTitle("Select End Time");
                    endTimePicker.show();

                }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), true);
        startTimePicker.setTitle("Select Start Time");
        startTimePicker.show();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Please choose the file"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            String fileName = getFileName(selectedFileUri);
            fileNameTextView.setText(fileName);
        }
    }

    private String getFileName(Uri uri) {
        String result = "Unknown";
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
            result = cursor.getString(nameIndex);
            cursor.close();
        }
        return result;
    }
    private void validateForm() {
        String leaveType = leaveTypeSpinner.getSelectedItem().toString();
        String reason = reasonEditText.getText().toString().trim();

        boolean hasError = false; // Flag to track if any error occurred

        if (leaveTypeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select leave type", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (leavePeriods.isEmpty()) {
            Toast.makeText(this, "Please select at least one start and end date", Toast.LENGTH_SHORT).show();
            hasError = true;
        }

        if (reason.isEmpty()) {
            reasonEditText.setError("Please enter a reason"); // Set error on reason EditText
            hasError = true;
        } else {
            reasonEditText.setError(null); // Clear error if it was previously set and is now valid
        }

        if (hasError) {
            return; // Stop submission if there are any errors
        }

        Toast.makeText(this, "Leave request submitted successfully", Toast.LENGTH_LONG).show();

        leaveTypeSpinner.setSelection(0);
        reasonEditText.setText("");
        fileNameTextView.setText("");
        leavePeriods.clear();
        selectedStartDate = null;
        selectedTimeRange = null;
        selectedFileUri = null;
        selectDateButton.setText("Select Date");
    }
}
