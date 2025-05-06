package com.example.worksyck;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

import java.util.ArrayList;

public class DepartmentsActivity extends AppCompatActivity {

    private ArrayList<Departments> departments;
    private RequestQueue requestQueue;
    private ListView listView;
    private ArrayList<String> departmentsNames;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }

    private void UpdateOnDepartment() {
    }

    private void DeleteDepartment() {
    }

    private void AddNewDepartment() {
    }


    private void FetchAllDepartments() {

        String get_all_departments_url="http://10.0.2.2/worksync/fetch_all_departments.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET,get_all_departments_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("Response",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status=jsonObject.getString("status");
                    String  message=jsonObject.getString("message");
                    if (status.equals("success") &&  !message.equals("No departments found.")){

                        JSONArray data= jsonObject.getJSONArray("data");
                        for (int i=0;i< data.length();i++){
                            String dep_name= data.getJSONObject(i).getString("name");
                            String dep_id= data.getJSONObject(i).getString("id");
                            departments.add(new Departments(dep_id,dep_name));

                        }
                    adapter.notifyDataSetChanged();
                    }

                    else{
                        Log.d("Case 2 :","No departments found.");
//                                UI there is No Departments .the page is empty.
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