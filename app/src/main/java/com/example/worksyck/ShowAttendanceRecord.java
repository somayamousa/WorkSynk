package com.example.worksyck;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ShowAttendanceRecord extends AppCompatActivity {
    private RequestQueue requestQueue;
    private String startDate = "", endDate = "";
    private TextView startDateText, endDateText;
    private Button searchBtn;
    private int loggedInUserId;
    private RecyclerView recyclerView;
    private AttendanceAdapter adapter;
    private List<AttendanceRecord> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_attendance_record);

        loggedInUserId = getIntent().getIntExtra("user_id", -1);
        if (loggedInUserId == -1) {
            Toast.makeText(this, "User ID not found!", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        requestQueue = Volley.newRequestQueue(this);
        startDateText = findViewById(R.id.startDateText1);
        endDateText = findViewById(R.id.endDateText1);
        searchBtn = findViewById(R.id.showAttendanceBtn);
        recyclerView = findViewById(R.id.attendanceRecyclerView);

        attendanceList = new ArrayList<>();
        adapter = new AttendanceAdapter(attendanceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        startDateText.setOnClickListener(v -> showDatePicker(true));
        endDateText.setOnClickListener(v -> showDatePicker(false));

        searchBtn.setOnClickListener(v -> {
            if (startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(this, "Please select both dates", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isStartBeforeEnd(startDate, endDate)) {
                Toast.makeText(this, "Start must be before End", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchAttendanceRecord(loggedInUserId);
        });
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, y, m, d) -> {
                    String ds = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
                    if (isStart) {
                        startDate = ds; startDateText.setText(ds);
                    } else {
                        endDate = ds; endDateText.setText(ds);
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private boolean isStartBeforeEnd(String s, String e) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date ds = sdf.parse(s), de = sdf.parse(e);
            return ds.before(de) || ds.equals(de);
        } catch (ParseException x) {
            return false;
        }
    }

    public void fetchAttendanceRecord(int userId) {
        String url = "http://192.168.1.6/worksync/fetch_employee_attendance_records.php"
                + "?employee_id=" + userId
                + "&start_date=" + startDate
                + "&end_date=" + endDate;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jo = new JSONObject(response);
                        if ("success".equals(jo.optString("status"))) {
                            JSONArray arr = jo.optJSONArray("data");
                            attendanceList.clear();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                            Date start, end;
                            try {
                                start = sdf.parse(startDate);
                                end = sdf.parse(endDate);

                                HashMap<String, AttendanceRecord> recordMap = new HashMap<>();
                                if (arr != null) {
                                    for (int i = 0; i < arr.length(); i++) {
                                        JSONObject r = arr.getJSONObject(i);
                                        AttendanceRecord rec = new AttendanceRecord(
                                                r.getInt("id"),
                                                r.getInt("user_id"),
                                                r.getString("date"),
                                                r.getString("start_time"),
                                                r.getString("end_time")
                                        );
                                        recordMap.put(rec.getDate(), rec);
                                    }
                                }

                                Calendar cal = Calendar.getInstance();
                                cal.setTime(start);
                                while (!cal.getTime().after(end)) {
                                    String dateStr = sdf.format(cal.getTime());
                                    if (recordMap.containsKey(dateStr)) {
                                        attendanceList.add(recordMap.get(dateStr));
                                    } else {
                                        attendanceList.add(new AttendanceRecord(
                                                0,
                                                userId,
                                                dateStr,
                                                "",
                                                ""
                                        ));
                                    }
                                    cal.add(Calendar.DAY_OF_MONTH, 1);
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            adapter.notifyDataSetChanged();

                        } else {
                            Toast.makeText(this, jo.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        Toast.makeText(this, "JSON error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error fetching data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
        );
        requestQueue.add(req);
    }
}
