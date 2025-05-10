package com.example.worksyck;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class EmployeeAddActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword, editTextFullName, editTextPhone, editTextMacAddress;
    private RadioGroup radioGroupStatus;
        private Button btnAddEmployee;
    private RequestQueue requestQueue;
    private ArrayList<Departments> departments;
    private ArrayList<Designations> designations;
    private Spinner departmentsSpinner;
    private Spinner designationsSpinner;
    private List<String> departmentNames;
    private List<Integer> departmentIds;
    private List<String> designationsName;
    private List<Integer> designationsId;
    private int department_id;
    private int designation_id;
    private String company_id;
    private String selectedStatusValue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_add);
        Intent intent = getIntent();
        company_id = intent.getStringExtra("company_id");

// Set views
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextMacAddress = findViewById(R.id.editTextMacAddress);
        departmentsSpinner = findViewById(R.id.departments_spinner);
        designationsSpinner = findViewById(R.id.designations_spinner);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);
//        set arrays
        departments = new ArrayList<>();
        departmentNames = new ArrayList<>();
        departmentIds = new ArrayList<>();
        designations = new ArrayList<>();
        designationsName = new ArrayList<>();
        designationsId = new ArrayList<>();


//        set request queue
        requestQueue = Volley.newRequestQueue(this);

//        Fill the departments  && designations on the spinner
        FetchAllDepartments();
        FetchAllDesignations();


        departmentsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int selectedIndex = position;
                department_id = departmentIds.get(selectedIndex);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                department_id = -1;

            }
        });
        designationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int selectedIndex = position;
                designation_id = designationsId.get(selectedIndex);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                designation_id = -1;


            }
        });


        btnAddEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Extract the values from fields
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String fullName = editTextFullName.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();
                String macAddress = editTextMacAddress.getText().toString().trim();
                int selectedId = radioGroupStatus.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedId);
                selectedStatusValue = selectedRadioButton.getText().toString().trim();
                Employee employee = new Employee(email, password, fullName, phone, macAddress, selectedStatusValue, Integer.valueOf(company_id), department_id, designation_id);
                Add_new_Employeee(employee);

            }
        });
    }
                                      private void Add_new_Employeee(Employee employee) {


                                          String add_new_employee_url = "http://10.0.2.2//worksync/add_employee.php";
                                          StringRequest stringRequest = new StringRequest(Request.Method.POST, add_new_employee_url, new Response.Listener<String>() {
                                              @Override
                                              public void onResponse(String response) {
                                                  Log.d("Response", response);
                                                  try {
                                                      JSONObject jsonObject = new JSONObject(response);
                                                      String status = jsonObject.getString("status");
                                                      String message = jsonObject.getString("message");
                                                      Log.d("Response", message);
                                                  } catch (JSONException e) {
                                                      throw new RuntimeException(e);
                                                  }


                                              }
                                          }, new Response.ErrorListener() {
                                              @Override
                                              public void onErrorResponse(VolleyError error) {

//                handle the error

                                                  Log.d("Error", String.valueOf(error));
                                                  Log.d("Error", "Error While Adding try again please");

                                              }

                                          }) {
                                              @Override
                                              protected Map<String, String> getParams() {
                                                  Map<String, String> params = new HashMap<>();
                                                  params.put("employee_full_name", employee.getFullname());
                                                  params.put("email", employee.getEmail());
                                                  params.put("password", employee.getPassword());
                                                  params.put("status", employee.getStatus());
                                                  params.put("mac_address", employee.getMacAddress());
                                                  params.put("phone_number", employee.getPhone());
                                                  params.put("company_id", String.valueOf(employee.getCompanyId()));
                                                  params.put("department_id", String.valueOf(employee.getDepartmentId()));
                                                  params.put("designation_id", String.valueOf(employee.getDesignationId()));
                                                  return params;
                                              }


                                          };


                                          requestQueue.add(stringRequest);


                                      }


                                      private void FetchAllDepartments() {

                                          String get_all_departments_url = "http://10.0.2.2/worksync/fetch_all_departments.php";

                                          StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_departments_url, new Response.Listener<String>() {
                                              @Override
                                              public void onResponse(String response) {

                                                  Log.d("Response", response);
                                                  try {
                                                      JSONObject jsonObject = new JSONObject(response);
                                                      String status = jsonObject.getString("status");
                                                      String message = jsonObject.getString("message");
                                                      if (status.equals("success") && !message.equals("No departments found.")) {

                                                          JSONArray data = jsonObject.getJSONArray("data");
                                                          for (int i = 0; i < data.length(); i++) {
                                                              String dep_name = data.getJSONObject(i).getString("name");
                                                              String dep_id = data.getJSONObject(i).getString("id");
                                                              departments.add(new Departments(dep_id, dep_name));
                                                              departmentNames.add(dep_name);
                                                              departmentIds.add(Integer.valueOf(dep_id));

                                                          }


                                                          ArrayAdapter<String> adapter = new ArrayAdapter<>(EmployeeAddActivity.this,
                                                                  android.R.layout.simple_spinner_item, departmentNames);
                                                          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                                          departmentsSpinner.setAdapter(adapter);


                                                      } else {
                                                          Log.d("Case 2 :", "No departments found.");
//                                UI there is No Departments .the page is empty.
                                                      }
                                                  } catch (JSONException e) {
                                                      throw new RuntimeException(e);
                                                  }


                                              }


                                          }, new Response.ErrorListener() {
                                              @Override
                                              public void onErrorResponse(VolleyError error) {
                                                  Log.e(" Error:", String.valueOf(error));
                                              }
                                          });


                                          requestQueue.add(stringRequest);


                                      }

                                      @Override
                                      protected void onResume() {
                                          super.onResume();
                                          departments.clear();
                                          designations.clear();
                                          departmentNames.clear();
                                          departmentIds.clear();
                                          designationsName.clear();
                                          designationsId.clear();
                                          FetchAllDesignations();
                                          FetchAllDepartments();
                                      }

                                      private void FetchAllDesignations() {


                                          String get_all_designations_url = "http://10.0.2.2/worksync/fetch_all_job_titiles.php";

                                          StringRequest stringRequest = new StringRequest(Request.Method.GET, get_all_designations_url, new Response.Listener<String>() {
                                              @Override
                                              public void onResponse(String response) {

                                                  Log.d("Response", response);
                                                  try {
                                                      JSONObject jsonObject = new JSONObject(response);
                                                      String status = jsonObject.getString("status");
                                                      String message = jsonObject.getString("message");
                                                      if (status.equals("success") && !message.equals("No job_titles found.")) {

                                                          JSONArray data = jsonObject.getJSONArray("data");
                                                          for (int i = 0; i < data.length(); i++) {
                                                              String dep_name = data.getJSONObject(i).getString("name");
                                                              String dep_id = data.getJSONObject(i).getString("id");
                                                              designations.add(new Designations(dep_id, dep_name));

                                                              designationsName.add(dep_name);
                                                              designationsId.add(Integer.valueOf(dep_id));

                                                          }
                                                          ArrayAdapter<String> adapter = new ArrayAdapter<>(EmployeeAddActivity.this,
                                                                  android.R.layout.simple_spinner_item, designationsName);
                                                          adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                                          designationsSpinner.setAdapter(adapter);
                                                      } else {
                                                          Log.d("Case 2 :", message);
////                                UI there is No  DesignationsActivity .the page is empty.
                                                      }
                                                  } catch (JSONException e) {
                                                      throw new RuntimeException(e);
                                                  }
                                              }
                                          }, new Response.ErrorListener() {
                                              @Override
                                              public void onErrorResponse(VolleyError error) {
                                                  Log.e(" Error:", String.valueOf(error));
                                              }
                                          });


                                          requestQueue.add(stringRequest);


                                      }


                                  }


