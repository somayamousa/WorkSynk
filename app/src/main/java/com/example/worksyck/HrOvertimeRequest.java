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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HrOvertimeRequest extends AppCompatActivity {
    private static final int VIEW_Overtime_REQUEST_CODE = 100;

    private RecyclerView overtimeRequestsRecyclerView;
    private OvertimeRequestAdapter overtimeRequestAdapter;
    private List<OvertimeRequestModel> overtimeRequestList;
    private NavigationHelper navigationHelper;
    private ImageView backButton;
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hr_overtime_requests);

        overtimeRequestsRecyclerView = findViewById(R.id.overtimeRequestsRecyclerView);
        overtimeRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        overtimeRequestList = new ArrayList<>();

        fetchOvertimeDataFromServer();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void fetchOvertimeDataFromServer() {
        String url = "http://10.0.2.2/worksync/get_overtime_data.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        overtimeRequestList.clear();

                        JSONArray jsonArray = new JSONArray(response);

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject overtimeRequest = jsonArray.getJSONObject(i);
                            String id = overtimeRequest.optString("id");
                           // String overtimeDate = overtimeRequest.optString("overtime_date");
                            String employeeCode = overtimeRequest.getString("employee_code");
                            String reason = overtimeRequest.optString("fullname");
                            String status = capitalizeFirstLetter(overtimeRequest.optString("status", "Pending"));

                            overtimeRequestList.add(new OvertimeRequestModel(id, employeeCode, reason, status));
                        }

                        if (overtimeRequestAdapter == null) {
                            overtimeRequestAdapter = new OvertimeRequestAdapter(overtimeRequestList);
                            overtimeRequestsRecyclerView.setAdapter(overtimeRequestAdapter);
                        } else {
                            overtimeRequestAdapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(HrOvertimeRequest.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(HrOvertimeRequest.this, "Error fetching data: " + error.toString(), Toast.LENGTH_SHORT).show()
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

        if (requestCode == VIEW_Overtime_REQUEST_CODE && resultCode == RESULT_OK) {
            fetchOvertimeDataFromServer(); // تحديث البيانات بعد العودة من شاشة التفاصيل
        }
    }

    class OvertimeRequestAdapter extends RecyclerView.Adapter<OvertimeRequestAdapter.OvertimeRequestViewHolder> {

        private List<OvertimeRequestModel> overtimeRequests;

        public OvertimeRequestAdapter(List<OvertimeRequestModel> overtimeRequests) {
            this.overtimeRequests = overtimeRequests;
        }

        @Override
        public OvertimeRequestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hr_overtime_item, parent, false);
            return new OvertimeRequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(OvertimeRequestViewHolder holder, int position) {
            OvertimeRequestModel overtimeRequest = overtimeRequests.get(position);
            holder.overtimeDateRange.setText(overtimeRequest.getEmployeeCode());
            holder.overtimeReason.setText(overtimeRequest.getReason());
            holder.statusTextView.setText(overtimeRequest.getStatus());
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
                Intent intent = new Intent(HrOvertimeRequest.this, HrOvertimeOverview.class);
                intent.putExtra("overtimeRequestId", overtimeRequest.getId());
                startActivityForResult(intent, VIEW_Overtime_REQUEST_CODE);
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
                statusTextView = itemView.findViewById(R.id.overtimeStatusTextView);
            }
        }
    }

    class OvertimeRequestModel {
        private String id;
        private String employeeCode;
        private String reason;
        private String status;

        public OvertimeRequestModel(String id, String employeeCode, String reason, String status) {
            this.id = id;
            this.employeeCode = employeeCode;
            this.reason = reason;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getEmployeeCode() {
            return employeeCode;
        }

        public String getReason() {
            return reason;
        }

        public String getStatus() {
            return status;
        }
    }
}
