package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class EmployeesListActivity extends AppCompatActivity {

    private ListView listViewEmployees;
    private RequestQueue requestQueue;
    private ArrayList<HashMap<String, String>> employeesList;
    private static final String TAG = "EmployeesListActivity";
    private static final String GET_EMPLOYEES_URL = "http://10.0.2.2/worksync/get_employees.php";
    private int companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees_list);

        // الحصول على company_id من بيانات تسجيل الدخول
        companyId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("company_id", 1);

        listViewEmployees = findViewById(R.id.listViewEmployees);
        requestQueue = Volley.newRequestQueue(this);
        employeesList = new ArrayList<>();

        // تحميل قائمة الموظفين
        loadEmployees();

        // إضافة حدث النقر على عنصر في القائمة
        listViewEmployees.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // الحصول على بيانات الموظف المحدد
                HashMap<String, String> selectedEmployee = employeesList.get(position);

                // فتح نشاط تفاصيل الموظف أو التعديل
                Intent intent = new Intent(EmployeesListActivity.this, EmployeeDetailsActivity.class);
                intent.putExtra("employee_id", selectedEmployee.get("id"));
                intent.putExtra("email", selectedEmployee.get("email"));
                intent.putExtra("fullname", selectedEmployee.get("fullname"));
                intent.putExtra("phone", selectedEmployee.get("phone"));
                intent.putExtra("status", selectedEmployee.get("status"));
                startActivity(intent);
            }
        });
    }

    private void loadEmployees() {
        String url = GET_EMPLOYEES_URL + "?company_id=" + companyId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            employeesList.clear();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject employee = response.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<>();

                                map.put("id", employee.getString("id"));
                                map.put("email", employee.getString("email"));
                                map.put("fullname", employee.getString("fullname"));
                                map.put("phone", employee.getString("phone"));
                                map.put("status", employee.getString("status"));
                                map.put("mac_address", employee.getString("mac_address"));

                                employeesList.add(map);
                            }

                            // استخدام Adapter مخصص لعرض البيانات
                            EmployeesListAdapter adapter = new EmployeesListAdapter(
                                    EmployeesListActivity.this,
                                    employeesList,
                                    R.layout.employee_list_item,
                                    new String[]{"fullname", "email", "status"},
                                    new int[]{R.id.textEmployeeName, R.id.textEmployeeEmail, R.id.textEmployeeStatus}
                            );

                            listViewEmployees.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EmployeesListActivity.this, "خطأ في تحميل البيانات", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EmployeesListActivity.this, "خطأ في الاتصال: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // إعادة تحميل البيانات عند العودة إلى النشاط
        loadEmployees();
    }
}