package com.example.worksyck;

import static android.R.layout.simple_list_item_1;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class show_holidays extends AppCompatActivity {
    private ArrayList<Holiday> allHolidaysList;
    private ArrayList<Holiday> SpesficYearHolidaysList;
    private ListView holidaysListView;
    private int holiday_id;
    private int year;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_holidays);
        holidaysListView=findViewById(R.id.list);
        year=2025;
        holiday_id=1;
        allHolidaysList = new ArrayList<Holiday>();
        SpesficYearHolidaysList = new ArrayList<Holiday>();
       FetchDataFromServer();
//        DeleteHolidayFromServer();
//        FetchDataFromServer();
        Holiday holiday = new Holiday(2,"Holiday","2025-06-04","update");
//        UpdateOnHoliday(holiday);
//        FetchHolidaysForYear(year);
    }
    private void FetchHolidaysForYear(int year) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String Url="http://10.0.2.2/WorkSync/spesfic_year_holidays.php?year=" + year;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

//                Log.d("response" , response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);
                    String status=responseJsonObject.getString("status");
                    if (status.equals("success")){
                        JSONArray holidaysArray = responseJsonObject.getJSONArray("data");
                        for (int i=0;i<holidaysArray.length();i++){
                            JSONObject holiday= holidaysArray.getJSONObject(i);
                            int holiday_id=   holiday.getInt("id");
                            String holiday_name=   holiday.getString("name");
                            String holiday_date=   holiday.getString("date");
                            String holiday_description=   holiday.getString("description");
//                        9.Save in array list
                            SpesficYearHolidaysList.add(new Holiday(holiday_id,holiday_name , holiday_date,holiday_description));

////        handle the array list on UI


                        }
                        ArrayList<String> strArray = new ArrayList<>();
                        for (int i=0;i<SpesficYearHolidaysList.size();i++){
                            Holiday holidaystr=SpesficYearHolidaysList.get(i);
                            String id= String.valueOf(holidaystr.getId());
                            String name=holidaystr.getName();
                            String description= holidaystr.getDescription();
                            String Date = holidaystr.getDate();
                            String all = id+"," + name + ","+ Date +"," + description ;
                            strArray.add(all);

                        }
                        ArrayAdapter<String> holidayAdapter = new ArrayAdapter<String>(show_holidays.this, simple_list_item_1,strArray);

                        holidaysListView.setAdapter(holidayAdapter);

                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  5.handle the error response   (error in connection.)
                Toast toast= Toast.makeText(show_holidays.this,"Something went wrong. Please try again later",Toast.LENGTH_LONG);
                toast.show();
                Log.e("show holidays error",String.valueOf(error));


            }
        })


        ;
//        11.add the request to request queue
        requestQueue.add(stringRequest);




}

    private void UpdateOnHoliday(Holiday holiday) {

        String Url="http://10.0.2.2/WorkSync//update_holiday.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST,Url,
                response -> {
                    try {
                        JSONObject jsonObjectc= new JSONObject(response);
                        String messsage = jsonObjectc.getString("message");

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                },
                error -> {

                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("holiday_id",String.valueOf(holiday.getId()));
                params.put("holiday_name",holiday.getName());
                params.put("holiday_date", holiday.getDate());
                params.put("description", holiday.getDescription());
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }


    private void DeleteHolidayFromServer() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://10.0.2.2/WorkSync/delete_holiday.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                Log.d("response" , response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message= jsonObject.getString("message");
                    Toast toast;
                    toast= Toast.makeText(show_holidays.this,message,Toast.LENGTH_LONG);
                    toast.show();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }}





            }
        , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  5.handle the error response   (error in connection.)
                Toast toast= Toast.makeText(show_holidays.this,"Something went wrong. Please try again later",Toast.LENGTH_LONG);
                toast.show();
                Log.e("Delete holiday",String.valueOf(error));

            }
        }) {

            @Override
            protected Map<String, String> getParams()  {
                Map<String, String> params = new HashMap<>();
                params.put("holiday_id", String.valueOf(holiday_id));
                return params;


            }

        };

        requestQueue.add(stringRequest);

    }





    public void FetchDataFromServer(){
        //  1.Create Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //  2.url
        String url = "http://10.0.2.2/WorkSync/Insert_show_holidays.php";

        //  3.create a request (stringRequest) , send the request and the response is  as a string

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                //  4.handle the success response

                //6. convert the string response to json
                Log.d("response" , response);
                try {
                    JSONObject responseJsonObject = new JSONObject(response);

//                    6. show the status of response if success or error
                    String status=responseJsonObject.getString("status");
                    if (status.equals("success")){
//                        7.Extract the array of result
                        JSONArray holidaysArray = responseJsonObject.getJSONArray("data");
//                        8.Extract all records and save in array List
                        for (int i=0;i<holidaysArray.length();i++){
                           JSONObject holiday= holidaysArray.getJSONObject(i);
                           int holiday_id=   holiday.getInt("id");
                        String holiday_name=   holiday.getString("name");
                        String holiday_date=   holiday.getString("date");
                        String holiday_description=   holiday.getString("description");
//                        9.Save in array list
                           allHolidaysList.add(new Holiday(holiday_id,holiday_name , holiday_date,holiday_description));

////        handle the array list on UI


                        }
                        ArrayList<String> strArray = new ArrayList<>();
                        for (int i=0;i<allHolidaysList.size();i++){
                            Holiday holidaystr=allHolidaysList.get(i);
                            String id= String.valueOf(holidaystr.getId());
                            String name=holidaystr.getName();
                            String description= holidaystr.getDescription();
                            String Date = holidaystr.getDate();
                            String all = id+"," + name + ","+ Date +"," + description ;
                            strArray.add(all);

                        }
                        ArrayAdapter<String> holidayAdapter = new ArrayAdapter<String>(show_holidays.this, simple_list_item_1,strArray);

                        holidaysListView.setAdapter(holidayAdapter);

                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }





            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //  5.handle the error response   (error in connection.)
               Toast toast= Toast.makeText(show_holidays.this,"Something went wrong. Please try again later",Toast.LENGTH_LONG);
               toast.show();
                Log.e("show holidays error",String.valueOf(error));

            }
        });
//        11.add the request to request queue
        requestQueue.add(stringRequest);

    }





}