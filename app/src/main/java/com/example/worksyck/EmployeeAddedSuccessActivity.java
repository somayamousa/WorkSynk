package com.example.worksyck;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EmployeeAddedSuccessActivity extends AppCompatActivity {

    private String  companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_added_success);




        String fullName = getIntent().getStringExtra("fullName");
        companyId = getIntent().getStringExtra("company_id");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");
        String phone = getIntent().getStringExtra("phone");
        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvPassword = findViewById(R.id.tvPassword);
        TextView tvEmployeeId = findViewById(R.id.tvEmployeeId);
        tvUsername.setText(email);
        tvPassword.setText(password);
        tvEmployeeId.setText(phone);

        // Button and ImageButton listeners
        ImageButton btnHome = findViewById(R.id.btnHome);
        Button btnViewEmployees = findViewById(R.id.btnViewEmployees);
        Button btnShareData = findViewById(R.id.btnShareData);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeAddedSuccessActivity.this, MainActivity2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnViewEmployees.setOnClickListener(v -> {
            // Navigate to EmployeesActivity
            Intent intent = new Intent(EmployeeAddedSuccessActivity.this, EmployeesActivity.class);
            intent.putExtra("company_id",companyId);
            startActivity(intent);
        });
        btnShareData.setOnClickListener(v -> {
            // Share employee data via available apps
            String shareText = "Employee Details:\nEmail: " + email + "\nPassword: " + password + "\nPhone: " + phone;
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            // Check for WhatsApp
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.setType("text/plain");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            // Check for SMS
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + phone));
            smsIntent.putExtra("sms_body", shareText);

            Intent chooserIntent = Intent.createChooser(shareIntent, "Share via");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{whatsappIntent, smsIntent});
            startActivity(chooserIntent);
        });
    }
}