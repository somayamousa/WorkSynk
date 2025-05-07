package com.example.worksyck;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class DesignationsActivity extends AppCompatActivity {
    private ArrayList<Designations> designations;
    private ArrayList<String> designationNames;
    private ArrayAdapter<String> adapter;
    private RequestQueue requestQueue;
    private ListView listView;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_designations);
        listView = findViewById(R.id.listViewDesignations);
        designations = new ArrayList<>();
        designationNames = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, designationNames);
        listView.setAdapter(adapter);
        requestQueue = Volley.newRequestQueue(this);
        FetchAllDesignations();
//      DeleteDesignation("1");
//    Designations designationObj=    new Designations("Customer Support Agent");
//        AddNewDesignation("Software EngineEr");
//        UpdateOnDesignation("1","Software Engineer1");
//
    }

    private void AddNewDesignation(String name) {

        //        Click on the  Add Button
//        Enter the New Designation Name.
//        Send to server to Add..


//        Check if the job_title name is not empty if empty show to the user dialog that is must fill


        String add_designation_url = "http://10.0.2.2//worksync/add_new_job_title.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, add_designation_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


//                handle the response
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    String message = jsonObject.getString("message");
                    Log.d("Response",message);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                handle the error

                Log.d("Error" , String.valueOf(error));
                Log.d("Error" , "Error While Adding try again please");

            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("job_title", name);

                return params;
            }


        };


        requestQueue.add(stringRequest);


    }





    private void UpdateOnDesignation(String id , String name) {
//        Click on the Edit Button Extract the Id of the clicked Department
//        Enter the New Department Name.
//        Send to server to Update..


//        Check if the department name is not empty if empty show to the user dialog that is must fill


        String update_department_url = "http://10.0.2.2//worksync/update_designation.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, update_department_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String message=jsonObject.getString("message");
                    Log.d("Response",message);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }





            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

//                handle the error
                Log.e("Error" , String.valueOf(error));
                Log.d("Error" , "Error While Update try again please");

            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("job_title_id", id);
                params.put("job_title_name", name);

                return params;
            }


        };
        requestQueue.add(stringRequest);
    }
    private void FetchAllDesignations() {


        String get_all_designations_url="http://10.0.2.2/worksync/fetch_all_job_titiles.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET,get_all_designations_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("Response",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status=jsonObject.getString("status");
                    String  message=jsonObject.getString("message");
                    if (status.equals("success") &&  !message.equals("No job_titles found.")){

                        JSONArray data= jsonObject.getJSONArray("data");
                        for (int i=0;i< data.length();i++){
                            String dep_name= data.getJSONObject(i).getString("name");
                            String dep_id= data.getJSONObject(i).getString("id");
                            designations.add(new Designations(dep_id,dep_name));

                        }

                        adapter.notifyDataSetChanged();
                    }

                    else{
                        Log.d("Case 2 :",message);
//                                UI there is No  DesignationsActivity .the page is empty.
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

    private void DeleteDesignation(String id) {
//        url
        String delete_department_url = "http://10.0.2.2/worksync/delete_designation.php";
//        string request


        StringRequest stringRequest = new StringRequest(Request.Method.POST, delete_department_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


//                handle the response
//                Log.d("Response",response);


                //                handle the response
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status =jsonObject.getString("status");
                    Log.d("Response",jsonObject.getString("message"));

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


//                handle the error
                Log.e("Error" , String.valueOf(error));
                Log.d("Error" , "Error While Deleting try again please");


            }

        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("designation_id", id);

                return params;
            }


        };
        requestQueue.add(stringRequest);


    }

}