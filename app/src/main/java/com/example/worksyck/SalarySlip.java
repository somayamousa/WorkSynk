package com.example.worksyck;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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


public class SalarySlip extends AppCompatActivity {

    private  RequestQueue requestQueue ;

private Button createSalarySlipBtn;
    private  double base_salary;
    private  double  de_hour_rate;
    private TextView startDateText, endDateText;
    private String startDate, endDate;
    private double total_working_hours;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_salary_slip);
         requestQueue=Volley.newRequestQueue(this);
         createSalarySlipBtn = findViewById(R.id.createSalarySlipBtn);
         startDate="" ;
         endDate="";

         startDateText=findViewById(R.id.startDateText);
        endDateText=findViewById(R.id.endDateText);
        startDateText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(SalarySlip.this,
                    (view, year, month, dayOfMonth) -> {
                        String dateStr = year + "-" + (month + 1) + "-" + dayOfMonth;
                        startDateText.setText("Start: " + dateStr);
                        startDate = dateStr;
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });
        endDateText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(SalarySlip.this,
                    (view, year, month, dayOfMonth) -> {
                        String dateStr = year + "-" + (month + 1) + "-" + dayOfMonth;
                        endDateText.setText("End: " + dateStr);
                        endDate = dateStr;
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            datePickerDialog.show();
        });
        createSalarySlipBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

                 int employee_id=19;
                 fetchEmployeeDetails(employee_id);





             }
         });







    }

    private void fetchEmployeeDetails(int employee_id) {
        String employeeDetailsUrl= "http://10.0.2.2/worksync/get_spesfic_employee.php?employee_id=" + employee_id;
        StringRequest stringRequest = new StringRequest(Request.Method.GET,employeeDetailsUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("success")) {
                        String message = jsonObject.getString("message");
                        if (!message.equals("No employee found")) {
                            JSONObject employeeDetailsObj = jsonObject.getJSONObject("data");
                            String salary_structure = employeeDetailsObj.getString("salary_structure_type");
                            if (salary_structure.equals("base salary")) {
                                String base_salaryStr = employeeDetailsObj.getString("base_salary");
                                String de_hour_rateStr = employeeDetailsObj.getString("hour_deduction_cost");
                                base_salary= Double.parseDouble(base_salaryStr);
                                de_hour_rate= Double.parseDouble(de_hour_rateStr);
//                         Then Calculate Fixed Salary
                                FixedSalary(startDate,endDate,employee_id);



                            } else if (salary_structure.equals("per hour")) {

                                String normal_hour_rate = employeeDetailsObj.getString("normal_hour_rate");
                                String overtime_hour_rate = employeeDetailsObj.getString("overtime_hour_rate");

                            }


                        } else {
                            Log.d("another Case", "No employee found with this Id");
                        }

                    } else {
                        Log.d("Error", "Failed to fetch employee details (Error in Query or data base and so on ");
                    }

                }
                catch (JSONException e){
                    Log.d("Exception" , String.valueOf(e));

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VolleyError" ,String.valueOf(error));
            }
        });
        requestQueue.add(stringRequest);
    }

    private void FixedSalary(String startDate , String EndDate  , int employee_id) {
//fetch the total Working Hour for the employee from this Date to this Date..
        FetchTotalWorkingsHour(employee_id);
//        value =normal_working_houre-total_working_hours;
//      double  BasicSalary= base_salary - (de_hour_rate * value);
    }

    private void FetchTotalWorkingsHour(int employee_id) {
        String totalWorkingsHourUrl = "http://10.0.2.2/worksync/fetch_total_workingHours.php?employee_id="+employee_id+"&start_date="+startDate+"&end_date="+endDate;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, totalWorkingsHourUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("Response",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.get("status").equals("success")){
if (!jsonObject.getString("message").equals("No attendance records found")){
    total_working_hours = Double.parseDouble(jsonObject.getString("data"));

}else{
    Log.d("Case","No attendance records found");
}

                    }else{
                        Log.d("Error" ,jsonObject.getString("message"));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error" , String.valueOf(error));

            }
        });

        requestQueue.add(stringRequest);











    }


}
