package com.example.worksyck;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class show_holidays extends AppCompatActivity {

    private RecyclerView rvHolidays;
    private HolidayAdapter adapter;
    private ArrayList<Holiday> allHolidaysList;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_holiday);

        findViewById(R.id.buttonAddHoliday).setOnClickListener(v -> {
            Intent intent = new Intent(show_holidays.this, AddHoliday.class);
            startActivity(intent);
        });

        rvHolidays = findViewById(R.id.rvHolidays);
        searchEditText = findViewById(R.id.searchEditText);
        allHolidaysList = new ArrayList<>();

        rvHolidays.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HolidayAdapter(allHolidaysList);
        rvHolidays.setAdapter(adapter);

        fetchDataFromServer();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (input.length() == 4) {
                    try {
                        int year = Integer.parseInt(input);
                        fetchHolidaysForYear(year);
                    } catch (NumberFormatException e) {
                        Toast.makeText(show_holidays.this, "Please enter correct values", Toast.LENGTH_SHORT).show();
                    }
                } else if (input.isEmpty()) {
                    fetchDataFromServer();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchDataFromServer();
    }

    private void fetchDataFromServer() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/WorkSync/Insert_show_holidays.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {
                            JSONArray holidaysArray = jsonObject.getJSONArray("data");
                            allHolidaysList.clear();

                            for (int i = 0; i < holidaysArray.length(); i++) {
                                JSONObject holiday = holidaysArray.getJSONObject(i);
                                int id = holiday.getInt("id");
                                String name = holiday.getString("name");
                                String date = holiday.getString("date");
                                String description = holiday.getString("description");

                                allHolidaysList.add(new Holiday(id, name, date, description));
                            }
                            adapter.notifyDataSetChanged();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(show_holidays.this, "Error", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(show_holidays.this, "Connenction filled", Toast.LENGTH_LONG).show();
                    Log.e("fetch_error", error.toString());
                });

        requestQueue.add(stringRequest);
    }

    private void fetchHolidaysForYear(int year) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/WorkSync/spesfic_year_holidays.php?year=" + year;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("status");

                        allHolidaysList.clear(); // امسح القائمة القديمة قبل الإضافة

                        if (status.equals("success")) {
                            JSONArray holidaysArray = jsonObject.getJSONArray("data");

                            for (int i = 0; i < holidaysArray.length(); i++) {
                                JSONObject holiday = holidaysArray.getJSONObject(i);
                                int id = holiday.getInt("id");
                                String name = holiday.getString("name");
                                String date = holiday.getString("date");
                                String description = holiday.getString("description");

                                allHolidaysList.add(new Holiday(id, name, date, description));
                            }

                            adapter.notifyDataSetChanged();

                            if (allHolidaysList.isEmpty()) {
                                Toast.makeText(show_holidays.this, "No holidays found for the specified year", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            // حالة الفشل - امسح البيانات وأعلم المستخدم
                            adapter.notifyDataSetChanged();
                            Toast.makeText(show_holidays.this, "No holidays found for the specified year", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(show_holidays.this, "Error parsing data", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(show_holidays.this, "Failed to connect to server", Toast.LENGTH_LONG).show();
                    Log.e("year_fetch_error", error.toString());
                });

        requestQueue.add(stringRequest);
    }


    private class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.HolidayViewHolder> {
        private final ArrayList<Holiday> holidays;

        public HolidayAdapter(ArrayList<Holiday> holidays) {
            this.holidays = holidays;
        }

        @NonNull
        @Override
        public HolidayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.holiday_item, parent, false);
            return new HolidayViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HolidayViewHolder holder, int position) {
            Holiday holiday = holidays.get(position);
            holder.holidayName.setText(holiday.getName());
            holder.holidayDate.setText(holiday.getDate());
            holder.description.setText(holiday.getDescription());

            holder.btnEdit.setOnClickListener(v -> showEditDialog(holiday));
            holder.btnDelete.setOnClickListener(v -> deleteHolidayFromServer(holiday.getId()));
        }

        @Override
        public int getItemCount() {
            return holidays.size();
        }

        class HolidayViewHolder extends RecyclerView.ViewHolder {
            TextView holidayName, holidayDate, description;
            MaterialButton btnEdit, btnDelete;

            public HolidayViewHolder(@NonNull View itemView) {
                super(itemView);
                holidayName = itemView.findViewById(R.id.holiday_name);
                holidayDate = itemView.findViewById(R.id.holiday_date);
                description = itemView.findViewById(R.id.description);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }
    }

    private void showEditDialog(Holiday holiday) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_holiday);

        EditText editName = dialog.findViewById(R.id.editHolidayName);
        EditText editDate = dialog.findViewById(R.id.editHolidayDate);
        EditText editDescription = dialog.findViewById(R.id.editHolidayDescription);
        MaterialButton btnSave = dialog.findViewById(R.id.btnSave);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(30f);

        dialog.getWindow().setBackgroundDrawable(drawable);

        editName.setText(holiday.getName());
        editDate.setText(holiday.getDate());
        editDescription.setText(holiday.getDescription());

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        btnSave.setOnClickListener(view -> {
            String newName = editName.getText().toString().trim();
            String newDate = editDate.getText().toString().trim();
            String newDescription = editDescription.getText().toString().trim();

            if (newName.isEmpty() || newDate.isEmpty()) {
                Toast.makeText(this, "يجب إدخال الاسم والتاريخ", Toast.LENGTH_SHORT).show();
                return;
            }

            holiday.setName(newName);
            holiday.setDate(newDate);
            holiday.setDescription(newDescription);

            updateHolidayOnServer(holiday);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateHolidayOnServer(Holiday holiday) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/WorkSync/update_holiday.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        fetchDataFromServer();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "خطأ في التحديث", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "فشل التحديث", Toast.LENGTH_LONG).show();
                    Log.e("update_error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("holiday_id", String.valueOf(holiday.getId()));
                params.put("holiday_name", holiday.getName());
                params.put("holiday_date", holiday.getDate());
                params.put("description", holiday.getDescription());
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void deleteHolidayFromServer(int holidayId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/WorkSync/delete_holiday.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String message = jsonObject.getString("message");
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        fetchDataFromServer();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "خطأ في الحذف", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "فشل في الاتصال", Toast.LENGTH_LONG).show();
                    Log.e("delete_error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("holiday_id", String.valueOf(holidayId));
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}
