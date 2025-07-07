package com.example.worksyck;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
public class AddHoliday extends AppCompatActivity {
    EditText editTextName, editTextDate, editTextDescription;
    Button buttonAddHoliday;
    TextView textViewMessage;
    private ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_holidays_settings);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            finish();
        });

        editTextName = findViewById(R.id.editTextName);
        editTextDate = findViewById(R.id.editTextDate);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonAddHoliday = findViewById(R.id.buttonAddHoliday);
        textViewMessage = findViewById(R.id.textViewMessage);

        buttonAddHoliday.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String date = editTextDate.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            if (name.isEmpty() || date.isEmpty()) {
                Toast.makeText(AddHoliday.this, "Fill all required filled", Toast.LENGTH_SHORT).show();
                return;
            }

            sendHolidayToServer(name, date, description);
        });
        editTextDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddHoliday.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {

                        String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                        editTextDate.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });


    }
    private void sendHolidayToServer(String name, String date, String description) {

        String URL_ADD_HOLIDAY="http:10.0.2.2/WorkSync//Insert_show_holidays.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ADD_HOLIDAY,
                response -> {
                    try {
                        JSONObject jsonObjectc= new JSONObject(response);
                        String messsage = jsonObjectc.getString("message");
                        textViewMessage.setText(messsage);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                },
                error -> {

                    textViewMessage.setText("Wrong While adding");
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("holiday_name", name);
                params.put("holiday_date", date);
                params.put("description", description);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

}