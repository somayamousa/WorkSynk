package com.example.worksyck;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OvertimeRequest extends AppCompatActivity {

    private RecyclerView overtimeRequestsRecyclerView;
    private OvertimeRequestAdapter overtimeRequestAdapter;
    private List<OvertimeRequestModel> overtimeRequestList;
    private NavigationHelper navigationHelper; // استخدام NavigationHelper
    private ImageView backButton;
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofovertime);

        // تفعيل NavigationHelper
        navigationHelper = new NavigationHelper(this);
        navigationHelper.enableBackButton();  // تفعيل زر الرجوع في ActionBar
        initializeViews();
        // إعداد Bottom Navigation باستخدام الـ Helper
        // إعداد Bottom Navigation باستخدام الـ Helper
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout);
        // تهيئة الـ RecyclerView
        overtimeRequestsRecyclerView = findViewById(R.id.overtimeRequestsRecyclerView);
        overtimeRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        overtimeRequestList = new ArrayList<>();

        // جلب البيانات من الخادم
        fetchOvertimeDataFromServer();

        // زر إنشاء طلب إجازة جديدة
        findViewById(R.id.newOvertimeRequestButton).setOnClickListener(v -> {
            Intent intent = new Intent(OvertimeRequest.this, OvertimeRequests.class);
            startActivity(intent);
        });

        // تفعيل زر الرجوع
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        // ربط العناصر في XML بالكود
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);


        // تفعيل زر الرجوع باستخدام الـ Helper
        navigationHelper.setBackButtonListener(backButton);  // استدعاء زر الرجوع
    }
    private void navigateToActivity(Class<?> activityClass) {
        // التنقل إلى الأنشطة المناسبة
        Intent intent = new Intent(OvertimeRequest.this, activityClass);
        startActivity(intent);
    }


    private void fetchOvertimeDataFromServer() {
        String url = "http://10.0.2.2/leave_requests/get_overtime_data.php";  // استبدل بـ URL الخاص بك

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // تحليل JSON
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject overtimeRequest = jsonArray.getJSONObject(i);
                                String id = overtimeRequest.optString("id");
                                String overtimeDate = overtimeRequest.optString("overtime_date");
                                String reason = overtimeRequest.optString("reason");

                                // إضافة البيانات إلى القائمة
                                overtimeRequestList.add(new OvertimeRequestModel(id, overtimeDate, reason));
                            }

                            // إعداد الـ RecyclerView Adapter
                            overtimeRequestAdapter = new OvertimeRequestAdapter(overtimeRequestList);
                            overtimeRequestsRecyclerView.setAdapter(overtimeRequestAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(OvertimeRequest.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> Toast.makeText(OvertimeRequest.this, "Error fetching data: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        // إضافة الطلب إلى صف الانتظار
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Adapter class لـ RecyclerView
    class OvertimeRequestAdapter extends RecyclerView.Adapter<OvertimeRequestAdapter.OvertimeRequestViewHolder> {

        private List<OvertimeRequestModel> overtimeRequests;

        public OvertimeRequestAdapter(List<OvertimeRequestModel> overtimeRequests) {
            this.overtimeRequests = overtimeRequests;
        }

        @Override
        public OvertimeRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.overtime_request_item, parent, false);
            return new OvertimeRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(OvertimeRequestViewHolder holder, int position) {
            OvertimeRequestModel overtimeRequest = overtimeRequests.get(position);
            holder.overtimeDateRange.setText(overtimeRequest.getOvertimeDate());
            holder.overtimeReason.setText(overtimeRequest.getReason());

            // تعيين OnClickListener على CardView
            holder.itemView.setOnClickListener(v -> {
                // إرسال ID طلب العمل الإضافي إلى صفحة OvertimeOverview
                Intent intent = new Intent(OvertimeRequest.this, OvertimeOverview.class);
                intent.putExtra("overtimeRequestId", overtimeRequest.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return overtimeRequests.size();
        }

        // ViewHolder لعرض طلبات العمل الإضافي
        class OvertimeRequestViewHolder extends RecyclerView.ViewHolder {
            TextView overtimeDateRange, overtimeReason;

            public OvertimeRequestViewHolder(View itemView) {
                super(itemView);
                overtimeDateRange = itemView.findViewById(R.id.overtimeDateRange);
                overtimeReason = itemView.findViewById(R.id.overtimeReason);
            }
        }
    }

    // نموذج طلب العمل الإضافي
    class OvertimeRequestModel {
        private String id;
        private String overtimeDate;
        private String reason;

        // Constructor
        public OvertimeRequestModel(String id, String overtimeDate, String reason) {
            this.id = id;
            this.overtimeDate = overtimeDate;
            this.reason = reason;
        }

        public String getId() {
            return id;
        }

        public String getOvertimeDate() {
            return overtimeDate;
        }

        public String getReason() {
            return reason;
        }
    }
}
