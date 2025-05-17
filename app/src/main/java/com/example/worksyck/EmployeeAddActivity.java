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
    private EditText    normalHourCostEditText,   overTimeHourCostEditText, detectionhourCostEditText, baseSalaryEditText, editTextEmail, editTextPassword, editTextFullName, editTextPhone;
    private RadioGroup radioGroupStatus;
    private Button btnAddEmployee;
    private RequestQueue requestQueue;
    private ArrayList<Departments> departments;
    private ArrayList<Designations> designations;
    private Spinner departmentsSpinner;

    private String salary_structure;

    private double base_salary;
    private double hourCost;

    private Spinner designationsSpinner;

    private   Spinner SalaryStructureSpinner ;
    private List<String> departmentNames;
    private List<Integer> departmentIds;
    private List<String> designationsName;
    private List<Integer> designationsId;
    private int department_id;
    private int designation_id;
    private String company_id;
    private double overTimeRate;
    private double normalHourRate;
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
        normalHourCostEditText=findViewById(R.id.normalHourCostEditText);
        overTimeHourCostEditText=findViewById(R.id.OverTimeHourCostEditText);
//        editTextMacAddress = findViewById(R.id.editTextMacAddress);
        departmentsSpinner = findViewById(R.id.departments_spinner);
        designationsSpinner = findViewById(R.id.designations_spinner);
        SalaryStructureSpinner = findViewById(R.id.salary_structure_spinner);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);
        baseSalaryEditText= findViewById(R.id.baseSalaryEditText);
        detectionhourCostEditText= findViewById(R.id.hourCostEditText);
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

        String[] Salary_structure_item = {"Salary Structure","Basic Salary" ,"per Hour"};


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Salary_structure_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SalaryStructureSpinner.setAdapter(adapter);


        SalaryStructureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.d("SpinnerSelection", "Position: " + position + ", Value: " + Salary_structure_item[position] + SalaryStructureType);
                if (position == 0) {
//                     Log.d("SpinnerSelection", "Position: " + position + ", Value: " + Salary_structure_item[position] + SalaryStructureType);
                    salary_structure="";
//                     Log.d("SpinnerSelection", "Position: " + position + ", Value: " + Salary_structure_item[position] + SalaryStructureType);

                    base_salary=0;
                    overTimeRate=0;
                    normalHourRate=0;
                    hourCost=0;
                    normalHourCostEditText.setVisibility(View.GONE);
                    overTimeHourCostEditText.setVisibility(View.GONE);
                    baseSalaryEditText.setVisibility(View.GONE);
                    detectionhourCostEditText.setVisibility(View.GONE);
                }
                else if (position == 1 ) {
                    salary_structure="base salary";
                    baseSalaryEditText.setVisibility(View.VISIBLE);
                    detectionhourCostEditText.setVisibility(View.VISIBLE);
                    normalHourCostEditText.setVisibility(View.GONE);
                    overTimeHourCostEditText.setVisibility(View.GONE);
                    overTimeRate=0;
                    normalHourRate=0;


                }
                else {
                    salary_structure="per Hour";
                    base_salary=0;
                    hourCost=0;

                    overTimeHourCostEditText.setVisibility(View.VISIBLE);
                    normalHourCostEditText.setVisibility(View.VISIBLE);
                    baseSalaryEditText.setVisibility(View.GONE);
                    detectionhourCostEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        departmentsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                int selectedIndex = position;
                department_id = departmentIds.get(selectedIndex);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

                int selectedId = radioGroupStatus.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = findViewById(selectedId);
                selectedStatusValue = selectedRadioButton.getText().toString().trim();

                if (salary_structure.equals("base salary")){
                    String baseSalaryStr= baseSalaryEditText.getText().toString().trim();
                    String hourCostString  =detectionhourCostEditText.getText().toString().trim();
                    if (!baseSalaryStr.isEmpty()) {
                        try {
                            base_salary = Double.parseDouble(baseSalaryStr);
                        } catch (NumberFormatException e) {

                            Toast.makeText(EmployeeAddActivity.this, "الرجاء إدخال رقم صالح للراتب الأساسي", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(EmployeeAddActivity.this,"base salary is required", Toast.LENGTH_SHORT).show();
                    }
                    if(!hourCostString.isEmpty()){
                        try {
                            hourCost = Double.parseDouble(hourCostString);

                        } catch (NumberFormatException e) {

                            Toast.makeText(EmployeeAddActivity.this, "الرجاء إدخال رقم صالح لسعر الساعه", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(EmployeeAddActivity.this,"hour Cost is required", Toast.LENGTH_SHORT).show();
                    }

                }else  if (salary_structure.equals("per Hour")) {
                    String overTimeRateSalaryStr = overTimeHourCostEditText.getText().toString().trim();
                    String normalHourRateStr = normalHourCostEditText.getText().toString().trim();
                    if (!overTimeRateSalaryStr.isEmpty()) {
                        try {
                            overTimeRate = Double.parseDouble(overTimeRateSalaryStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(EmployeeAddActivity.this, "الرجاء إدخال رقم صالح لسعر الساعه الاضافيه", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(EmployeeAddActivity.this, "over time hour Cost is required", Toast.LENGTH_SHORT).show();

                    }
                    if (!normalHourRateStr.isEmpty()) {
                        try {
                            normalHourRate = Double.parseDouble(normalHourRateStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(EmployeeAddActivity.this, "الرجاء إدخال رقم صالح لسعر الساعه العاديه", Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        Toast.makeText(EmployeeAddActivity.this, "normal hour Cost is required", Toast.LENGTH_SHORT).show();
                    }
                }
                Employee employee = new Employee(email, password, fullName, phone, selectedStatusValue, Integer.valueOf(company_id), department_id, designation_id,base_salary,hourCost,salary_structure,normalHourRate,overTimeRate);
                Log.d("Dep_id" , String.valueOf(department_id));
//                Log.d("Salary type" , SalaryStructureType);
                Add_new_Employeee(employee);
            }
        });
    }
    private void Add_new_Employeee(Employee employee) {
        Log.d("Dep_id1" , String.valueOf(department_id));
        Log.d("test" , salary_structure);

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
//                                                  params.put("mac_address", employee.getMacAddress());
                params.put("phone_number", employee.getPhone());
                params.put("company_id", String.valueOf(employee.getCompanyId()));
                params.put("department_id", String.valueOf(employee.getDepartmentId()));
                params.put("designation_id", String.valueOf(employee.getDesignationId()));

                params.put("salary_structure_type", employee.getSalaryStructureType());
                if (salary_structure.equals("base salary")){
                    params.put("base_salary", String.valueOf(employee.getBaseSalary()));
                    Log.d("base_salary", String.valueOf(employee.getBaseSalary()));
                    params.put("hour_deduction_cost", String.valueOf(employee.getHourCost()));
                }else if(salary_structure.equals("per Hour")){
                    params.put("overtime_hour_rate", String.valueOf(employee.getOvertimeHourRate()));
                    params.put("normal_hour_rate", String.valueOf(employee.getNormalHourRate()));


                }

//


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
                        departmentNames.add("Choose Department");
                        departmentIds.add(-1);
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
                        designationsName.add("Choose Designations");
                        designationsId.add(-1);
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

