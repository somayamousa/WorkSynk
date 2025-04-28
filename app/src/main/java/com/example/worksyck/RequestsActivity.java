package com.example.worksyck;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class RequestsActivity extends AppCompatActivity {

    private static final int PURPLE_COLOR = Color.parseColor("#5C2E91");
    private static final int DEFAULT_COLOR = Color.BLACK;

    private ImageView backButton;
    private ImageView[] icons;
    private TextView[] texts;
    private LinearLayout[] bottomNavigationLayouts;
    private CardView leaveRequestCardView;
    private CardView overtimeRequestCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        initializeViews();
        setBottomNavigationListeners();
        setBackButtonListener();

        leaveRequestCardView = findViewById(R.id.leaveRequestCardView);
        overtimeRequestCardView = findViewById(R.id.overtimeRequestCardView);

        leaveRequestCardView.setOnClickListener(v -> {
            Intent intent = new Intent(RequestsActivity.this, LeaveRequests.class);
            startActivity(intent);
        });

        overtimeRequestCardView.setOnClickListener(v -> {
            Intent intent = new Intent(RequestsActivity.this, OvertimeRequests.class);
            startActivity(intent);
        });

    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        LinearLayout checkInLayout = findViewById(R.id.checkInLayout);
        LinearLayout hrLayout = findViewById(R.id.salaryLayout);
        LinearLayout homeLayout = findViewById(R.id.homeLayout);
        LinearLayout attendanceLayout = findViewById(R.id.attendanceLayout);
        LinearLayout requestsLayout = findViewById(R.id.requestsLayout);
        bottomNavigationLayouts = new LinearLayout[]{checkInLayout, hrLayout, homeLayout, attendanceLayout, requestsLayout};

        icons = new ImageView[] {
                findViewById(R.id.checkInIcon),
                findViewById(R.id.hrIcon),
                findViewById(R.id.homeIcon),
                findViewById(R.id.attendanceIcon),
                findViewById(R.id.requestsIcon)
        };

        texts = new TextView[] {
                findViewById(R.id.checkInText),
                findViewById(R.id.hrText),
                findViewById(R.id.homeText),
                findViewById(R.id.attendanceText),
                findViewById(R.id.requestsText)
        };
    }

    private void setBottomNavigationListeners() {
        for (LinearLayout layout : bottomNavigationLayouts) {
            layout.setOnClickListener(this::onBottomNavigationItemClick);
        }
    }

    private void setBackButtonListener() {
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(RequestsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void onBottomNavigationItemClick(View v) {
        setIconAndTextColor(v.getId(), PURPLE_COLOR);
        resetOtherItems(v.getId());

        if (v.getId() == R.id.homeLayout) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setIconAndTextColor(int layoutId, int color) {
        for (int i = 0; i < bottomNavigationLayouts.length; i++) {
            if (layoutId == bottomNavigationLayouts[i].getId()) {
                icons[i].setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                texts[i].setTextColor(color);
            }
        }
    }

    private void resetOtherItems(int currentItemId) {
        for (int i = 0; i < bottomNavigationLayouts.length; i++) {
            if (bottomNavigationLayouts[i].getId() != currentItemId) {
                icons[i].setColorFilter(new PorterDuffColorFilter(DEFAULT_COLOR, PorterDuff.Mode.SRC_IN));
                texts[i].setTextColor(DEFAULT_COLOR);
            }
        }
    }
}
