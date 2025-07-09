package com.example.worksyck;

import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

public class HrOvertimeOverview extends AppCompatActivity {

    private TextView overtimeDateRange, overtimeHours, overtimeReason,   statusTextView, attachmentTextView, employeecodeTypeTextView;
    private ImageView backButton;
    private Button acceptButton, rejectButton;

    private String overtimeRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hr_overtime_details);

        // Initialize views
        employeecodeTypeTextView = findViewById(R.id.employeecodeTypeTextView);
        overtimeDateRange = findViewById(R.id.overtimeDateRange);
        overtimeHours = findViewById(R.id.overtimeHours);
        overtimeReason = findViewById(R.id.overtimeReason);
        attachmentTextView = findViewById(R.id.attachmentTextView);
        statusTextView = findViewById(R.id.statusTextView);
        backButton = findViewById(R.id.backButton);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);

        // Get the request ID from intent
        Intent intent = getIntent();
        overtimeRequestId = intent.getStringExtra("overtimeRequestId");

        // Load overtime details
        loadOvertimeDetails(overtimeRequestId);

        // Back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Accept / Reject buttons
        acceptButton.setOnClickListener(v -> updateOvertimeStatus(overtimeRequestId, "Accepted"));
        rejectButton.setOnClickListener(v -> updateOvertimeStatus(overtimeRequestId, "Rejected"));
    }

    private void loadOvertimeDetails(String overtimeRequestId) {
        String url = "http://10.0.2.2/worksync/get_overtime_data.php?id=" + overtimeRequestId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("OvertimeOverview", "Response: " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        String overtimeDate = jsonObject.getString("overtime_date");
                        String employeecode = jsonObject.getString("employee_code");
                        String hours = jsonObject.getString("hours");
                        String reason = jsonObject.getString("reason");
                        String attachment = jsonObject.optString("attachment", "No file attachment");
                        String status = capitalizeFirstLetter(jsonObject.optString("status", "Pending"));
                        statusTextView.setText(status);
                        setStatusColor(status); // استدعاء لتلوين الحالة


                        overtimeDateRange.setText(overtimeDate);
                        employeecodeTypeTextView.setText("Employee Code: " +employeecode);
                        overtimeHours.setText(hours + " Hours");
                        overtimeReason.setText(reason);
                        attachmentTextView.setText("Attachment: " + attachment);

                        // Hide buttons if status is not pending
                        if (!status.equalsIgnoreCase("Pending")) {
                            acceptButton.setVisibility(Button.GONE);
                            rejectButton.setVisibility(Button.GONE);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(HrOvertimeOverview.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(HrOvertimeOverview.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void updateOvertimeStatus(String requestId, String status) {
        String url = "http://10.0.2.2/worksync/overtime_update.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Status updated to: " + status, Toast.LENGTH_SHORT).show();
                    statusTextView.setText(status);
                    setStatusColor(status); // استدعاء لتلوين الحالة

                    acceptButton.setVisibility(Button.GONE);
                    rejectButton.setVisibility(Button.GONE);
                    setResult(RESULT_OK);


                },
                error -> Toast.makeText(this, "Error updating status: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", requestId);
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
