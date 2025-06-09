package com.example.worksyck;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
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
import org.json.JSONObject;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LeaveOverview extends AppCompatActivity {

    // TextViews for leave details
    TextView leaveTypeTextView, startDateTextView, endDateTextView, reasonTextView,
            numberOfDaysTextView, statusTextView, attachmentTextView;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaveoverview); // التأكد من أن اسم الـ layout صحيح

        // Initialize the views
        leaveTypeTextView = findViewById(R.id.leaveTypeTextView);
        startDateTextView = findViewById(R.id.startDateTextView);
        endDateTextView = findViewById(R.id.endDateTextView);
        reasonTextView = findViewById(R.id.reasonTextView);
        numberOfDaysTextView = findViewById(R.id.numberOfDaysTextView);
        statusTextView = findViewById(R.id.statusTextView);
        attachmentTextView = findViewById(R.id.attachmentTextView);
        backButton = findViewById(R.id.backButton);

        // Get the leave request data from the Intent
        Intent intent = getIntent();
        String leaveRequestId = intent.getStringExtra("leaveRequestId");

        // Load the leave details from the server
        loadLeaveDetailsFromServer(leaveRequestId);

        // Set back button listener
        setBackButtonListener();
    }

    private void setBackButtonListener() {
        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void loadLeaveDetailsFromServer(String leaveRequestId) {
        String url = "http://10.0.2.2/worksync/get_leave_request.php?id=" + leaveRequestId;

        // إرسال طلب GET باستخدام Volley
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    // إضافة Log لعرض الاستجابة
                    Log.d("LeaveOverview", "Response: " + response);

                    // التحقق من الاستجابة وتحليلها
                    try {
                        JSONArray jsonArray = new JSONArray(response);  // Parse the response as an array

                        // Iterate over the JSON array and parse the first item
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String leaveType = jsonObject.getString("leave_type");
                        String startDate = jsonObject.getString("start_date");
                        String endDate = jsonObject.getString("end_date");
                        String reason = jsonObject.getString("reason");

                        // استخدام optString للحصول على attachment مع القيمة الافتراضية "No file attachment"
                        String attachment = jsonObject.optString("attachment", "No file attachment");
                        String status = capitalizeFirstLetter(jsonObject.optString("status", "Pending"));

                        statusTextView.setText(status);
                        setStatusColor(status); // استدعاء لتلوين الحالة

// حساب عدد الأيام بين start_date و end_date
                        long numberOfDays = calculateNumberOfDays(startDate, endDate);
                        // عرض البيانات في الـ TextViews
                        leaveTypeTextView.setText("Leave Type: " + leaveType);
                        startDateTextView.setText("Start Date: " + startDate);
                        endDateTextView.setText("End Date: " + endDate);
                        reasonTextView.setText("Reason: " + reason);
                        attachmentTextView.setText("Attachment: " + attachment);
                        numberOfDaysTextView.setText("Number of Days: " + numberOfDays);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(LeaveOverview.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(LeaveOverview.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                });

        // إضافة الطلب إلى الـ Request Queue
        Volley.newRequestQueue(this).add(stringRequest);
    }
    // دالة لحساب عدد الأيام بين startDate و endDate
    private long calculateNumberOfDays(String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // تحويل التاريخين من String إلى Date
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            // حساب الفرق بين التاريخين بالميلي ثانية
            long diffInMillies = end.getTime() - start.getTime();

            // تحويل الفرق إلى عدد الأيام (تقسيم على 86400000 = عدد الميلي ثانية في اليوم)
            return TimeUnit.MILLISECONDS.toDays(diffInMillies) + 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;  // في حالة حدوث خطأ نرجع 0 أيام
        }


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