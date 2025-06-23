package com.example.worksyck;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HrLeaveOverview extends AppCompatActivity {

    // TextViews for leave details
    TextView leaveTypeTextView, startDateTextView, endDateTextView, reasonTextView,
            numberOfDaysTextView, statusTextView, attachmentTextView;
    private ImageView backButton;
    private Button acceptButton, rejectButton;

    private String leaveRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hr_leave_details);

        // Initialize views
        leaveTypeTextView = findViewById(R.id.leaveTypeTextView);
        startDateTextView = findViewById(R.id.startDateTextView);
        endDateTextView = findViewById(R.id.endDateTextView);
        reasonTextView = findViewById(R.id.reasonTextView);
        numberOfDaysTextView = findViewById(R.id.numberOfDaysTextView);
        statusTextView = findViewById(R.id.statusTextView);
        attachmentTextView = findViewById(R.id.attachmentTextView);
        backButton = findViewById(R.id.backButton);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);

        // Get leave request ID from Intent
        Intent intent = getIntent();
        leaveRequestId = intent.getStringExtra("leaveRequestId");

        // Load leave details
        loadLeaveDetailsFromServer(leaveRequestId);

        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Accept / Reject buttons
        acceptButton.setOnClickListener(v -> updateLeaveStatus(leaveRequestId, "Accepted"));
        rejectButton.setOnClickListener(v -> updateLeaveStatus(leaveRequestId, "Rejected"));
    }

    private void loadLeaveDetailsFromServer(String leaveRequestId) {
        String url = "http://10.0.2.2/worksync/get_leave_request.php?id=" + leaveRequestId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("LeaveOverview", "Response: " + response);

                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        String leaveType = jsonObject.getString("leave_type");
                        String startDate = jsonObject.getString("start_date");
                        String endDate = jsonObject.getString("end_date");
                        String reason = jsonObject.getString("reason");
                        String attachment = jsonObject.optString("attachment", "No file attachment");
                        String status = capitalizeFirstLetter(jsonObject.optString("status", "Pending"));

                        statusTextView.setText(status);
                        setStatusColor(status); // استدعاء لتلوين الحالة

                        long numberOfDays = calculateNumberOfDays(startDate, endDate);


                        leaveTypeTextView.setText("Leave Type: " + leaveType);
                        startDateTextView.setText("Start Date: " + startDate);
                        endDateTextView.setText("End Date: " + endDate);
                        reasonTextView.setText("Reason: " + reason);
                        attachmentTextView.setText("Attachment: " + attachment);
                        numberOfDaysTextView.setText("Number of Days: " + numberOfDays);


                        // Disable or hide buttons if not pending
                        if (!status.equalsIgnoreCase("Pending")) {
                            acceptButton.setVisibility(Button.GONE);
                            rejectButton.setVisibility(Button.GONE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(HrLeaveOverview.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(HrLeaveOverview.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private long calculateNumberOfDays(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            long diffInMillies = end.getTime() - start.getTime();
            return TimeUnit.MILLISECONDS.toDays(diffInMillies) + 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateLeaveStatus(String leaveRequestId, String status) {
        String url = "http://10.0.2.2/worksync/update_leave_status.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(HrLeaveOverview.this, "Status updated to: " + status, Toast.LENGTH_SHORT).show();
                    statusTextView.setText(status);
                    setStatusColor(status); // استدعاء لتلوين الحالة

                    // Hide buttons after update
                    acceptButton.setVisibility(Button.GONE);
                    rejectButton.setVisibility(Button.GONE);
                    setResult(RESULT_OK);
                },
                error -> Toast.makeText(HrLeaveOverview.this, "Error updating status: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", leaveRequestId);
                params.put("status", status);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    private void setStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "accepted":
                statusTextView.setTextColor(ContextCompat.getColor(this, R.color.status_accepted));
                break;
            case "rejected":
                statusTextView.setTextColor(ContextCompat.getColor(this, R.color.status_rejected));
                break;
            case "pending":
            default:
                statusTextView.setTextColor(ContextCompat.getColor(this, R.color.status_pending));
                break;
        }
    }



}
