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

public class LeaveRequest extends AppCompatActivity {

    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private ImageView backButton;

    private RecyclerView leaveRequestsRecyclerView;
    private LeaveRequestAdapter leaveRequestAdapter;
    private List<LeaveRequestModel> leaveRequestList;
    private NavigationHelper navigationHelper; // استخدام NavigationHelper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofleave);

        // تفعيل NavigationHelper
        navigationHelper = new NavigationHelper(this);
        navigationHelper.enableBackButton();  // تفعيل زر الرجوع في ActionBar
        initializeViews();
        // إعداد Bottom Navigation باستخدام الـ Helper
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout);


        // إعداد RecyclerView
        leaveRequestsRecyclerView = findViewById(R.id.leaveRequestsRecyclerView);
        leaveRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // قائمة الطلبات
        leaveRequestList = new ArrayList<>();

        // جلب البيانات من الخادم
        fetchLeaveDataFromServer();

        // زر إنشاء طلب إجازة جديدة
        findViewById(R.id.newLeaveRequestButton).setOnClickListener(v -> {
            Intent intent = new Intent(LeaveRequest.this, LeaveRequests.class);
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
        Intent intent = new Intent(LeaveRequest.this, activityClass);
        startActivity(intent);
    }


    // جلب بيانات الإجازات من الخادم
    private void fetchLeaveDataFromServer() {
        String url = "http://10.0.2.2/leave_requests/get_leave_request.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // تحليل JSON
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject leaveRequest = jsonArray.getJSONObject(i);
                                String id = leaveRequest.getString("id");
                                String leaveType = leaveRequest.getString("leave_type");
                                String startDate = leaveRequest.getString("start_date");
                                String endDate = leaveRequest.getString("end_date");

                                // إضافة البيانات إلى القائمة
                                leaveRequestList.add(new LeaveRequestModel(id, leaveType, startDate, endDate));
                            }

                            // إعداد الـ RecyclerView Adapter
                            leaveRequestAdapter = new LeaveRequestAdapter(leaveRequestList);
                            leaveRequestsRecyclerView.setAdapter(leaveRequestAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(LeaveRequest.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> Toast.makeText(LeaveRequest.this, "Error fetching data: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        // إضافة الطلب إلى صف الانتظار
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Adapter class لـ RecyclerView
    class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.LeaveRequestViewHolder> {

        private List<LeaveRequestModel> leaveRequests;

        public LeaveRequestAdapter(List<LeaveRequestModel> leaveRequests) {
            this.leaveRequests = leaveRequests;
        }

        @Override
        public LeaveRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leave_request_item, parent, false);
            return new LeaveRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LeaveRequestViewHolder holder, int position) {
            LeaveRequestModel leaveRequest = leaveRequests.get(position);
            holder.leaveTypeTextView.setText(leaveRequest.getLeaveType());
            holder.leaveDatesTextView.setText(leaveRequest.getStartDate() + " to " + leaveRequest.getEndDate());

            // تعيين OnClickListener على CardView
            holder.itemView.setOnClickListener(v -> {
                // إرسال ID طلب الإجازة إلى صفحة LeaveOverview
                Intent intent = new Intent(LeaveRequest.this, LeaveOverview.class);
                intent.putExtra("leaveRequestId", leaveRequest.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return leaveRequests.size();
        }

        // ViewHolder لعرض طلبات الإجازة
        class LeaveRequestViewHolder extends RecyclerView.ViewHolder {
            TextView leaveTypeTextView, leaveDatesTextView;

            public LeaveRequestViewHolder(View itemView) {
                super(itemView);
                leaveTypeTextView = itemView.findViewById(R.id.leaveTypeTextView);
                leaveDatesTextView = itemView.findViewById(R.id.leaveDatesTextView);
            }
        }
    }

    // نموذج طلب الإجازة
    class LeaveRequestModel {
        private String id;
        private String leaveType;
        private String startDate;
        private String endDate;

        // Constructor
        public LeaveRequestModel(String id, String leaveType, String startDate, String endDate) {
            this.id = id;
            this.leaveType = leaveType;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getId() {
            return id;
        }

        public String getLeaveType() {
            return leaveType;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }
}
