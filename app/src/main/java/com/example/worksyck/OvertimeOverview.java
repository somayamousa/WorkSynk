package com.example.worksyck;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OvertimeOverview extends AppCompatActivity {

    // TextViews for overtime details
    private TextView overtimeDateRange, overtimeHours, overtimeReason, statusTextView, attachmentTextView, employeecodeTypeTextView;
    private ImageView backButton;
    private String overtimeRequestId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeoverview); // Make sure to set your layout correctly

        // Initialize the views
        employeecodeTypeTextView = findViewById(R.id.employeecodeTypeTextView);
        overtimeDateRange = findViewById(R.id.overtimeDateRange);
        overtimeHours = findViewById(R.id.overtimeHours);
        overtimeReason = findViewById(R.id.overtimeReason);
        attachmentTextView = findViewById(R.id.attachmentTextView);
        statusTextView = findViewById(R.id.statusTextView);
        backButton = findViewById(R.id.backButton);


        // Get the overtime request ID from the Intent
        Intent intent = getIntent();
        overtimeRequestId = getIntent().getStringExtra("overtimeRequestId");

        // Load overtime details from the server
        loadOvertimeDetails(overtimeRequestId);

        // Set back button listener
        setBackButtonListener();
    }

    private void setBackButtonListener() {
        backButton.setOnClickListener(v -> onBackPressed());
    }

    // Load overtime details from the server
    private void loadOvertimeDetails(String overtimeRequestId) {
        String url = "http://10.0.2.2/worksync/get_overtime_data.php?id=" + overtimeRequestId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // إضافة Log لعرض الاستجابة
                    Log.d("OvertimeOverview", "Response: " + response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        // Parse the first item of the JSON array
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String overtimeDate = jsonObject.getString("overtime_date");
                        String employeecode = jsonObject.getString("employee_code");
                        String hours = jsonObject.getString("hours");
                        String reason = jsonObject.getString("reason");
                        String attachment = jsonObject.optString("attachment", "No file attachment");
                        String status = capitalizeFirstLetter(jsonObject.optString("status", "Pending"));

                        statusTextView.setText(status);

                        setStatusColor(status); // استدعاء لتلوين الحالة

                        // Display the data in the TextViews
                        overtimeDateRange.setText(overtimeDate);
                        employeecodeTypeTextView.setText("Employee Code: " +employeecode);
                        overtimeHours.setText(hours + " Hours");
                        overtimeReason.setText(reason);
                        attachmentTextView.setText("Attachment: " + attachment);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OvertimeOverview.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(OvertimeOverview.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                });

        // Add the request to the queue
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
