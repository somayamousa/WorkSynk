package com.example.worksyck;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    private static final int VIEW_OVERTIME_REQUEST_CODE = 100; // Request code for new overtime request

    private RecyclerView overtimeRequestsRecyclerView;
    private OvertimeRequestAdapter overtimeRequestAdapter;
    private List<OvertimeRequestModel> overtimeRequestList;
    private NavigationHelper navigationHelper;
    private ImageView backButton;
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private int userId, company_id;
    private String email, fullname, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofovertime);

        // Retrieve intent extras
        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getIntExtra("company_id", 0);

        // Initialize NavigationHelper
        navigationHelper = new NavigationHelper(this, userId, email, fullname, role, company_id);
        navigationHelper.enableBackButton();

        // Initialize views
        initializeViews();

        // Set up Bottom Navigation using NavigationHelper
        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout, salaryLayout, attendanceLayout);

        // Initialize RecyclerView
        overtimeRequestsRecyclerView = findViewById(R.id.overtimeRequestsRecyclerView);
        overtimeRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        overtimeRequestList = new ArrayList<>();

        // Fetch data from server
        fetchOvertimeDataFromServer();

        // Set up New Overtime Request button
        findViewById(R.id.newOvertimeRequestButton).setOnClickListener(v -> {
            Intent intent = new Intent(OvertimeRequest.this, OvertimeRequests.class);
            startActivityForResult(intent, VIEW_OVERTIME_REQUEST_CODE);
        });

        // Back button listener
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);

        navigationHelper.setBackButtonListener(backButton);
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(OvertimeRequest.this, activityClass);
        intent.putExtra("user_id", userId);
        intent.putExtra("email", email);
        intent.putExtra("fullname", fullname);
        intent.putExtra("role", role);
        intent.putExtra("company_id", company_id);
        startActivity(intent);
    }

    private void fetchOvertimeDataFromServer() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://10.0.2.2/worksync/get_overtime_data.php?user_id=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        overtimeRequestList.clear();
                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject overtimeRequest = jsonArray.getJSONObject(i);
                            String id = overtimeRequest.optString("id");
                            String overtimeDate = overtimeRequest.optString("overtime_date");
                            String reason = overtimeRequest.optString("reason");
                            String status = capitalizeFirstLetter(overtimeRequest.optString("status", "Pending"));
                            overtimeRequestList.add(new OvertimeRequestModel(id, overtimeDate, reason, status));
                        }

                        if (overtimeRequestAdapter == null) {
                            overtimeRequestAdapter = new OvertimeRequestAdapter(overtimeRequestList);
                            overtimeRequestsRecyclerView.setAdapter(overtimeRequestAdapter);
                        } else {
                            overtimeRequestAdapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OvertimeRequest.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(OvertimeRequest.this, "Error fetching data: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIEW_OVERTIME_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchOvertimeDataFromServer();
        }
    }

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
            holder.statusTextView.setText(overtimeRequest.getStatus());
            Log.d("OvertimeRequest", "Status value: " + overtimeRequest.getStatus());

            String status = overtimeRequest.getStatus().toLowerCase();

            switch (status) {
                case "accepted":
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.statusTextView.getContext(), R.color.status_accepted));
                    break;
                case "rejected":
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.statusTextView.getContext(), R.color.status_rejected));
                    break;
                case "pending":
                default:
                    holder.statusTextView.setTextColor(ContextCompat.getColor(holder.statusTextView.getContext(), R.color.status_pending));
                    break;
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(OvertimeRequest.this, OvertimeOverview.class);
                intent.putExtra("overtimeRequestId", overtimeRequest.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return overtimeRequests.size();
        }

        class OvertimeRequestViewHolder extends RecyclerView.ViewHolder {
            TextView overtimeDateRange, overtimeReason, statusTextView;

            public OvertimeRequestViewHolder(View itemView) {
                super(itemView);
                overtimeDateRange = itemView.findViewById(R.id.overtimeDateRange);
                overtimeReason = itemView.findViewById(R.id.overtimeReason);
                statusTextView = itemView.findViewById(R.id.statusTextView);
            }
        }
    }

    class OvertimeRequestModel {
        private String id;
        private String overtimeDate;
        private String reason;
        private String status;

        public OvertimeRequestModel(String id, String overtimeDate, String reason, String status) {
            this.id = id;
            this.overtimeDate = overtimeDate;
            this.reason = reason;
            this.status = status;
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

        public String getStatus() {
            return status;
        }
    }
}