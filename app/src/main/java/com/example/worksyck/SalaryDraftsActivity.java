package com.example.worksyck;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalaryDraftsActivity extends AppCompatActivity {
    private RecyclerView draftsRecyclerView;
    private TextView noDraftsText;
    private SalaryDraftAdapter adapter;
    private List<SalaryDraft> draftList;
    private List<SalaryDraft> filteredDraftList;
    private RequestQueue requestQueue;
    private EditText searchEmployeeInput;
    private Button selectMonthBtn, deleteAllBtn;
    private int selectedMonth = 0, selectedYear = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_drafts);

        // Initialize views
        draftsRecyclerView = findViewById(R.id.draftsRecyclerView);
        noDraftsText = findViewById(R.id.noDraftsText);
        searchEmployeeInput = findViewById(R.id.searchEmployeeInput);
        selectMonthBtn = findViewById(R.id.selectMonthBtn);
        deleteAllBtn = findViewById(R.id.deleteAllBtn);
        draftList = new ArrayList<>();
        filteredDraftList = new ArrayList<>();
        adapter = new SalaryDraftAdapter(filteredDraftList, this::onViewDetailsClick, this::onApproveClick, this::onCancelClick);
        draftsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        draftsRecyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        // Setup month selection
        selectMonthBtn.setOnClickListener(v -> showMonthPickerDialog());

        // Setup delete all button
        deleteAllBtn.setOnClickListener(v -> showDeleteAllConfirmation());

        // Setup search filter
        searchEmployeeInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDrafts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Fetch drafts for the current month by default
        setDefaultMonth();
        fetchSalaryDrafts();
    }
    private void showMonthPickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    setDateRange(month + 1, year); // month is 0-based
                    fetchSalaryDrafts();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        // Hide day field
        try {
            DatePicker datePicker = datePickerDialog.getDatePicker();
            int dayPickerId = getResources().getIdentifier("day", "id", "android");
            if (dayPickerId != 0) {
                View dayPicker = datePicker.findViewById(dayPickerId);
                if (dayPicker != null) {
                    dayPicker.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            Log.e("DatePicker", "Error hiding day field: " + e.getMessage());
        }

        datePickerDialog.show();
    }

    private void setDefaultMonth() {
        // Set default to current month (June 2025)
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // 1-based month
        int year = calendar.get(Calendar.YEAR);
        setDateRange(month, year);
    }

    private void setDateRange(int month, int year) {
        selectedMonth = month;
        selectedYear = year;

        // Calculate start and end dates for display purposes
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1); // Month is 0-based in Calendar
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        selectMonthBtn.setText(String.format(Locale.US, "%s %d",
                new SimpleDateFormat("MMMM", Locale.US).format(calendar.getTime()), year));
    }

    private void fetchSalaryDrafts() {
        if (selectedMonth == 0 || selectedYear == 0) {
            Toast.makeText(this, "Please select a month", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = "http://10.0.2.2/worksync/fetch_salary_drafts.php?month=" + selectedMonth + "&year=" + selectedYear;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            draftList.clear();
                            filteredDraftList.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject draft = data.getJSONObject(i);
                                SalaryDraft salaryDraft = new SalaryDraft(
                                        draft.getInt("id"),
                                        draft.getInt("employee_id"),
                                        draft.getString("employee_name"),
                                        draft.getString("employee_code"),
                                        draft.getDouble("net_salary"),
                                        draft.getString("period_start"),
                                        draft.getString("period_end"),
                                        draft.getString("salary_structure_type"),
                                        draft.getString("department_name"),
                                        draft.getString("designation_name")
                                );
                                draftList.add(salaryDraft);

                            }
                            filteredDraftList.addAll(draftList);
                            adapter.notifyDataSetChanged();

                            // Show/hide no drafts message
                            if (filteredDraftList.isEmpty()) {
                                draftsRecyclerView.setVisibility(View.GONE);
                                noDraftsText.setVisibility(View.VISIBLE);
                            } else {
                                draftsRecyclerView.setVisibility(View.VISIBLE);
                                noDraftsText.setVisibility(View.GONE);
                            }
                        } else {
                            draftsRecyclerView.setVisibility(View.GONE);
                            noDraftsText.setVisibility(View.VISIBLE);
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                        draftsRecyclerView.setVisibility(View.GONE);
                        noDraftsText.setVisibility(View.VISIBLE);
                    }
                },
                error -> {
                    Log.e("Volley Error", error.getMessage());
                    draftsRecyclerView.setVisibility(View.GONE);
                    noDraftsText.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Error fetching drafts", Toast.LENGTH_SHORT).show();
                });
        requestQueue.add(request);
    }

    private void filterDrafts(String query) {
        filteredDraftList.clear();
        if (query.isEmpty()) {
            filteredDraftList.addAll(draftList);
        } else {
            for (SalaryDraft draft : draftList) {
                if (draft.getEmployeeName().toLowerCase(Locale.US).contains(query.toLowerCase(Locale.US))) {
                    filteredDraftList.add(draft);
                }
            }
        }
        adapter.notifyDataSetChanged();

        // Update visibility based on filter results
        if (filteredDraftList.isEmpty()) {
            draftsRecyclerView.setVisibility(View.GONE);
            noDraftsText.setVisibility(View.VISIBLE);
        } else {
            draftsRecyclerView.setVisibility(View.VISIBLE);
            noDraftsText.setVisibility(View.GONE);
        }
    }

    private void onViewDetailsClick(SalaryDraft draft) {
//        Intent intent = new Intent(this, SalaryDraftDetailsActivity.class);
//        intent.putExtra("salary_draft", draft);
//        startActivity(intent);
    }

    private void onApproveClick(SalaryDraft draft) {
        approveSalaryDraft(draft.getId(), draft.getEmployeeId());
    }

    private void onCancelClick(SalaryDraft draft) {
        showDeleteConfirmation(draft);
    }

    private void showDeleteConfirmation(SalaryDraft draft) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete the salary draft for " + draft.getEmployeeName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> deleteSalaryDraft(draft.getId(), draft.getEmployeeId()))
                .setNegativeButton("No", null)
                .show();
    }

    private void showDeleteAllConfirmation() {
        if (filteredDraftList.isEmpty()) {
            Toast.makeText(this, "No drafts to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete All")
                .setMessage("Are you sure you want to delete all salary drafts ")
                .setPositiveButton("Yes", (dialog, which) -> deleteAllSalaryDrafts())
                .setNegativeButton("No", null)
                .show();
    }

    private void approveSalaryDraft(int draftId, int employeeId) {
        String url = "http://10.0.2.2/worksync/approve_salary.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            String payslipNumber = jsonObject.optString("payslip_number", "N/A");
                            Toast.makeText(this, "Salary approved successfully (Payslip: " + payslipNumber + ")", Toast.LENGTH_SHORT).show();
                            fetchSalaryDrafts(); // Refresh list to remove approved drafts
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Error", error.getMessage());
                    Toast.makeText(this, "Error approving salary", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("draft_id", String.valueOf(draftId));
                params.put("employee_id", String.valueOf(employeeId));
                return params;
            }
        };
        requestQueue.add(request);
    }
    private void deleteSalaryDraft(int draftId, int employeeId) {
        String url = "http://10.0.2.2/worksync/delete_salary_draft.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "Salary draft deleted successfully", Toast.LENGTH_SHORT).show();
                            fetchSalaryDrafts(); // Refresh list to remove deleted draft
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Error", error.getMessage());
                    Toast.makeText(this, "Error deleting draft", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("draft_id", String.valueOf(draftId));
                params.put("employee_id", String.valueOf(employeeId));
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void deleteAllSalaryDrafts() {
        String url = "http://10.0.2.2/worksync/delete_all_salary_drafts.php";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("status").equals("success")) {
                            Toast.makeText(this, "All salary drafts deleted successfully", Toast.LENGTH_SHORT).show();
                            fetchSalaryDrafts(); // Refresh list to clear all drafts
                        } else {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON Error", e.getMessage());
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Volley Error", error.getMessage());
                    Toast.makeText(this, "Error deleting drafts", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("month", String.valueOf(selectedMonth));
                params.put("year", String.valueOf(selectedYear));
                return params;
            }
        };
        requestQueue.add(request);
    }


}
