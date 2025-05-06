package com.example.worksyck;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Payroll_Entry extends AppCompatActivity {

    private String  company_id;
    private  int user_id;

    private RequestQueue requestQueue ;
    private  TextView textViewCompanyName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payroll_entry);

        Button selectDateButton = findViewById(R.id.button_select_date);
         textViewCompanyName=findViewById(R.id.textViewCompanyName);
        TextView selectedDateTextView = findViewById(R.id.text_selected_date);
        Intent intent=getIntent();
        company_id= intent.getStringExtra("company_id");
        user_id   = intent.getIntExtra("user_id",-1);

        final Calendar calendar = Calendar.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        selectDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        Payroll_Entry.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // Format the selected date and display it
                                String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                selectedDateTextView.setText("Selected Date: " + selectedDate);
                            }
                        },
                        year, month, day);
                datePickerDialog.show();
            }
        });
        FillHrCompany(String.valueOf(user_id));


    }

    private void FillHrCompany(String hr_id) {

String get_hr_company_url="http://10.0.2.2/worksync/get_hr_company.php?hr_id="+hr_id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_hr_company_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("User_id",hr_id);
                Log.d("Response",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status=jsonObject.getString("status");
                    if (status.equals("success")){

                        JSONObject data_= jsonObject.getJSONObject("data");
                        String company_name=data_.getString("name");
                       String company_id=data_.getString("id");
                       textViewCompanyName.setText(company_name);
//                       Fetch all employee in this Company

//                        get_all_company_employees();

                    }else{
                        Log.e("error",response);
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Company Name Error:",String.valueOf(error));
            }
        });


        requestQueue.add(stringRequest);
    }

//    private void get_all_company_employees() {
//
//        String get_all_company_employee_url="http://10.0.2.2/worksync/get_all_comany_employee.php?company_id="+company_id;
//
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_company_employee_url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                Log.d("User_id",company_id);
//                Log.d("Response",response);
//                if (response == null || response.trim().isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Empty response from server", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                try {
//                    JSONObject jsonObject = new JSONObject(response);
//                    String status=jsonObject.getString("status");
//                    if (status.equals("success")){
//
//                        JSONObject data_= jsonObject.getJSONObject("data");
//                        String company_name=data_.getString("name");
//                        String company_id=data_.getString("id");
//                        textViewCompanyName.setText(company_name);
////                       Fetch all employee in this Company
//
//                        get_all_company_employees();
//
//                    }else{
//                        Log.e("error",response);
//                    }
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
//
//
//            }
//
//
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("Company Name Error:",String.valueOf(error));
//            }
//        });
//
//
//        requestQueue.add(stringRequest);
//    }
//








    }


