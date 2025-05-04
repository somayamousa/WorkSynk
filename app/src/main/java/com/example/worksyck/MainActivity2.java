package com.example.worksyck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Buttons
        Button btnQr = findViewById(R.id.btnQr);
        Button btnAddEmployee = findViewById(R.id.btnAddEmployee);
        Button   Employee= findViewById(R.id.Employee);

        // QR Button Click
        btnQr.setOnClickListener(v -> {
            Intent qrIntent = new Intent(MainActivity2.this, QrDisplayActivity.class);
            startActivity(qrIntent);
        });

        // Add Employee Button Click
        btnAddEmployee.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, EmployeeAddActivity.class);
            startActivity(addIntent);
        });Employee.setOnClickListener(v -> {
            Intent addIntent = new Intent(MainActivity2.this, EmployeesListActivity.class);
            startActivity(addIntent);
        });
    }
}
