package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class LeaveRequest extends AppCompatActivity {

    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private ImageView backButton;
    private RecyclerView leaveRequestsRecyclerView;
    private LeaveRequestAdapter leaveRequestAdapter;
    private List<LeaveRequestModel> leaveRequestList;
    private NavigationHelper navigationHelper;

    private int userId, company_id;
    private String email, fullname, role;

    private static final int VIEW_LEAVE_REQUEST_CODE = 100; // request code for new leave

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listofleave);

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
        leaveRequestsRecyclerView = findViewById(R.id.leaveRequestsRecyclerView);
        leaveRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaveRequestList = new ArrayList<>();
        fetchLeaveDataFromServer();

        // Set up New Leave Request button
        findViewById(R.id.newLeaveRequestButton).setOnClickListener(v -> {
            Intent intent = new Intent(LeaveRequest.this, LeaveRequests.class);
            startActivityForResult(intent, VIEW_LEAVE_REQUEST_CODE);
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
        Intent intent = new Intent(LeaveRequest.this, activityClass);
        intent.putExtra("user_id", userId);
        intent.putExtra("email", email);
        intent.putExtra("fullname", fullname);
        intent.putExtra("role", role);
        intent.putExtra("company_id", company_id);
        startActivity(intent);
    }

    private void fetchLeaveDataFromServer() {
        String url = "http://192.168.1.6/worksync/get_leave_request.php?user_id=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Log.d("Response", response);
                        leaveRequestList.clear(); // Clear before adding new data

                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject leaveRequest = jsonArray.getJSONObject(i);
                            String id = leaveRequest.getString("id");
                            String leaveType = leaveRequest.getString("leave_type");
                            String startDate = leaveRequest.getString("start_date");
                            String endDate = leaveRequest.getString("end_date");
                            String status = capitalizeFirstLetter(leaveRequest.optString("status", "Pending"));
                            leaveRequestList.add(new LeaveRequestModel(id, leaveType, startDate, endDate, status));
                        }

                        leaveRequestAdapter = new LeaveRequestAdapter(leaveRequestList);
                        leaveRequestsRecyclerView.setAdapter(leaveRequestAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LeaveRequest.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(LeaveRequest.this, "Error fetching data: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode,  data);

        if (requestCode == VIEW_LEAVE_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchLeaveDataFromServer(); // Reload list when new leave added
        }
    }

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
            holder.statusTextView.setText(leaveRequest.getStatus());
            Log.d("LeaveRequest", "Status value: " + leaveRequest.getStatus());

            String status = leaveRequest.getStatus().toLowerCase();

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
                Intent intent = new Intent(LeaveRequest.this, LeaveOverview.class);
                intent.putExtra("leaveRequestId", leaveRequest.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return leaveRequests.size();
        }

        class LeaveRequestViewHolder extends RecyclerView.ViewHolder {
            TextView leaveTypeTextView, leaveDatesTextView, statusTextView;

            public LeaveRequestViewHolder(View itemView) {
                super(itemView);
                leaveTypeTextView = itemView.findViewById(R.id.leaveTypeTextView);
                leaveDatesTextView = itemView.findViewById(R.id.leaveDatesTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
            }
        }
    }

    class LeaveRequestModel {
        private String id;
        private String leaveType;
        private String startDate;
        private String endDate;
        private String status;

        public LeaveRequestModel(String id, String leaveType, String startDate, String endDate, String status) {
            this.id = id;
            this.leaveType = leaveType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.status = status;
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

        public String getStatus() {
            return status;
        }
    }
}