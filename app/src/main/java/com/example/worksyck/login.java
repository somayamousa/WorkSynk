package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private RequestQueue requestQueue;
    private static final String TAG = "LoginActivity";
    private static final String LOGIN_URL = "http://192.168.1.11/worksync/loginapi.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.inputEmail);
        editTextPassword = findViewById(R.id.inputPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        requestQueue = Volley.newRequestQueue(this);

        btnLogin.setOnClickListener(v -> userLogin());
    }

    private void userLogin() {
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("username", email);
        params.put("password", password);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                new JSONObject(params),
                this::handleLoginResponse,
                this::handleLoginError
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {


                    String jsonString = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers, "utf-8"));

                    Log.d(TAG, "Raw response: " + jsonString);
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    return Response.success(jsonResponse,
                            HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException | UnsupportedEncodingException e) {
                    Log.e(TAG, "Parsing error: ", e);
                    return Response.error(new VolleyError("Error parsing response: " + e.getMessage()));
                }
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    private void handleLoginResponse(JSONObject response) {
        try {
            String status = response.optString("status", "");


            if ("success".equalsIgnoreCase(status)) {
                JSONObject user = response.getJSONObject("data").getJSONObject("user");
                Log.d("HR",String.valueOf(user));
                // استخراج بيانات المستخدم
                int id = user.optInt("id");
                String email = user.optString("email");
                String fullname = user.optString("fullname");
                String role = user.optString("role");
                String macAddress = user.optString("mac_address", "");
                int company_id = user.optInt("company_id");
                String companyCode = user.optString("company_code", "");

                Intent intent;
                if ("hr".equalsIgnoreCase(role)) {
                    intent = new Intent(this, MainActivity2.class);
                    Toast.makeText(this, "Logged in as HR", Toast.LENGTH_SHORT).show();
                } else {
                    intent = new Intent(this, MainActivity.class);
                    Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                }

                intent.putExtra("user_id", id);
                intent.putExtra("email", email);
                intent.putExtra("fullname", fullname);
                intent.putExtra("role", role);
                intent.putExtra("mac_address", macAddress);
                intent.putExtra("company_id", company_id);
                intent.putExtra("company_code", companyCode);

                startActivity(intent);
                finish();
            } else {
                String message = response.optString("message", "Invalid credentials");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Response parsing error: ", e);
            Toast.makeText(this, "Unexpected response format", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLoginError(VolleyError error) {
        Log.d("ServerResponse", String.valueOf(error)); // لعرض الرد من السيرفر
        String errorMessage = "Login failed";

        if (error.networkResponse != null) {
            try {
                String errorBody = new String(error.networkResponse.data);
                JSONObject errorObj = new JSONObject(errorBody);
                if (errorObj.has("message")) {
                    errorMessage = errorObj.getString("message");
                }
            } catch (Exception e) {
                errorMessage = "Server error: " + e.getMessage();
                Log.e(TAG, "Error reading server response", e);
            }
        } else {
            errorMessage = "Network error. Please check your internet connection.";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}
