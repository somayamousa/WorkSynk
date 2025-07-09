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

public class HrLeaveRequest extends AppCompatActivity {

    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private ImageView backButton;

    private RecyclerView leaveRequestsRecyclerView;
    private LeaveRequestAdapter leaveRequestAdapter;
    private List<LeaveRequestModel> leaveRequestList;
    private NavigationHelper navigationHelper;

    private int userId, company_id;
    private String email, fullname, role;

    private static final int VIEW_LEAVE_REQUEST_CODE = 100;
    // request code for new leave

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hr_leave_requests);

        leaveRequestsRecyclerView = findViewById(R.id.leaveRequestsRecyclerView);
        leaveRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        leaveRequestList = new ArrayList<>();

        fetchLeaveDataFromServer();

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

    private void fetchLeaveDataFromServer() {
        String url = "http://10.0.2.2/worksync/get_leave_request.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            leaveRequestList.clear();
                            JSONArray jsonArray = new JSONArray(response);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject leaveRequest = jsonArray.getJSONObject(i);
                                String id = leaveRequest.getString("id");
                                String leaveType = leaveRequest.getString("fullname");
                                String employeeCode = leaveRequest.getString("employee_code");
                               // String endDate = leaveRequest.getString("end_date");

                                String status = capitalizeFirstLetter(leaveRequest.optString("status", "Pending"));
                                leaveRequestList.add(new LeaveRequestModel(id, leaveType, employeeCode , status));
                            }

                            leaveRequestAdapter = new LeaveRequestAdapter(leaveRequestList);
                            leaveRequestsRecyclerView.setAdapter(leaveRequestAdapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(HrLeaveRequest.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> Toast.makeText(HrLeaveRequest.this, "Error fetching data: " + error.toString(), Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(stringRequest);
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VIEW_LEAVE_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchLeaveDataFromServer();  // تحديث البيانات بعد الرجوع من صفحة العرض
        }
    }


    class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.LeaveRequestViewHolder> {

        private List<LeaveRequestModel> leaveRequests;

        public LeaveRequestAdapter(List<LeaveRequestModel> leaveRequests) {
            this.leaveRequests = leaveRequests;
        }

        @Override
        public LeaveRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hr_leave_item, parent, false);
            return new LeaveRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(LeaveRequestViewHolder holder, int position) {
            LeaveRequestModel leaveRequest = leaveRequests.get(position);
            holder.leaveTypeTextView.setText(leaveRequest.getLeaveType());
            holder.leaveDatesTextView.setText(leaveRequest.getEmployeeCode());
            holder.statusTextView.setText(leaveRequest.getStatus());
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
                Intent intent = new Intent(HrLeaveRequest.this, HrLeaveOverview.class);
                intent.putExtra("leaveRequestId", leaveRequest.getId());
                startActivityForResult(intent, VIEW_LEAVE_REQUEST_CODE);

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
        private String employeeCode;
        private String status;

        public LeaveRequestModel(String id, String leaveType, String employeeCode, String status) {
            this.id = id;
            this.leaveType = leaveType;
            this.employeeCode = employeeCode;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getLeaveType() {
            return leaveType;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }


        public String getStatus() {
            return status;
        }
    }
}
