
package com.example.worksyck;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView dateText;
    private TextView hoursText;
    private Handler handler;
    private int userId, company_id;
    private String email, fullname, role;
    private LinearLayout checkInLayout, salaryLayout, homeLayout, attendanceLayout, requestsLayout;
    private NavigationHelper navigationHelper;
    private ImageView notificationBell;
    private View notificationDot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = getIntent().getStringExtra("email");
        fullname = getIntent().getStringExtra("fullname");
        role = getIntent().getStringExtra("role");
        userId = getIntent().getIntExtra("user_id", 0);
        company_id = getIntent().getIntExtra("company_id", 0);

        navigationHelper = new NavigationHelper(this, userId, email, fullname, role, company_id);
        navigationHelper.enableBackButton();

        initializeViews();

        LinearLayout[] bottomNavItems = {homeLayout, requestsLayout, checkInLayout};
        navigationHelper.setBottomNavigationListeners(bottomNavItems, homeLayout, requestsLayout, checkInLayout);

        updateDate();
        startUpdatingHours();

        // زر الجرس ونقطة الإشعار
        notificationBell = findViewById(R.id.notificationBell);
        notificationDot = findViewById(R.id.notificationDot);

        notificationBell.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        checkNotifications(); // استدعاء التحقق من الإشعارات
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNotifications();  // تحديث نقطة الإشعار عند العودة للنشاط
    }

    private void initializeViews() {
        checkInLayout = findViewById(R.id.checkInLayout);
        salaryLayout = findViewById(R.id.salaryLayout);
        homeLayout = findViewById(R.id.homeLayout);
        attendanceLayout = findViewById(R.id.attendanceLayout);
        requestsLayout = findViewById(R.id.requestsLayout);

        dateText = findViewById(R.id.dateText);
        hoursText = findViewById(R.id.hoursText);
        handler = new Handler(Looper.getMainLooper());
    }
    private void navigateToActivity(Class<?> activityClass) {
        // التنقل إلى الأنشطة المناسبة
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }
    private void updateDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd,MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        dateText.setText(currentDate);
    }

    private void startUpdatingHours() {
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    final String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    handler.post(() -> hoursText.setText(currentTime + " Hours"));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void checkNotifications() {
        SharedPreferences prefs = getSharedPreferences("notifs", MODE_PRIVATE);
        boolean hasUnread = prefs.getBoolean("hasUnread", false);

        if (hasUnread) {
            notificationDot.setVisibility(View.VISIBLE);
            Log.d("NOTIF_CHECK", "Notification dot set to VISIBLE (hasUnread=true)");
        } else {
            notificationDot.setVisibility(View.GONE);
            Log.d("NOTIF_CHECK", "Notification dot set to GONE (hasUnread=false)");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
