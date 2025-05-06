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
//
//        AddNewDesignation(new Designations());
        DeleteDesignation();
        UpdateOnDesignations();
    }

    private void AddNewDesignation() {



    }

    private void DeleteDesignation() {
    }

    private void UpdateOnDesignations() {
    }

    private void FetchAllDesignations() {


        String get_all_designations_url="http://10.0.2.2/worksync/fetch_all_job_titles.php";

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
                        Log.d("Case 2 :","No Designations found.");
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
}