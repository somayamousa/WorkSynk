package com.example.worksyck;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EmployeeAddActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextFullName, editTextPhone, editTextMacAddress;
    private RadioGroup radioGroupStatus;
    private Button btnAddEmployee;
    private RequestQueue requestQueue;
    private static final String TAG = "EmployeeAddActivity";
    private static final String ADD_EMPLOYEE_URL = "http://192.168.1.11/worksync/add_employee.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_add);

        // تهيئة عناصر الواجهة
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextMacAddress = findViewById(R.id.editTextMacAddress);
        radioGroupStatus = findViewById(R.id.radioGroupStatus);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);

        requestQueue = Volley.newRequestQueue(this);

        btnAddEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewEmployee();
            }
        });
    }

    private void addNewEmployee() {
        // الحصول على القيم من الحقول
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String macAddress = editTextMacAddress.getText().toString().trim();

        // الحصول على حالة الموظف
        int selectedId = radioGroupStatus.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        String status = radioButton.getText().toString().toLowerCase();

        // التحقق من الحقول المطلوبة
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال البريد الإلكتروني وكلمة المرور والاسم الكامل", Toast.LENGTH_SHORT).show();
            return;
        }

        // إنشاء كائن JSON لإرسال البيانات
        Map<String, String> params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
        params.put("fullname", fullName);
        params.put("phone", phone);
        params.put("mac_address", macAddress);
        params.put("status", status);
        params.put("company_id", "1"); // يمكنك تغيير هذا لاستخدام company_id الخاص بـ HR

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                ADD_EMPLOYEE_URL,
                new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                Toast.makeText(EmployeeAddActivity.this, "تمت إضافة الموظف بنجاح", Toast.LENGTH_SHORT).show();
                                finish(); // إغلاق النشاط بعد الإضافة
                            } else {
                                Toast.makeText(EmployeeAddActivity.this, "خطأ: " + message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(EmployeeAddActivity.this, "خطأ في معالجة الاستجابة", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(EmployeeAddActivity.this, "خطأ في الاتصال: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(request);
    }
}