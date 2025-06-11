package com.example.worksyck;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
public class workPolicySettingsActivity extends AppCompatActivity {
    private RadioGroup holidayPolicyGroup, weekendPolicyGroup;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "WorkHourPolicies";
    private static final String KEY_HOLIDAY_POLICY = "holidayPolicy";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_policy_settings);
        // Initialize views
        holidayPolicyGroup = findViewById(R.id.holidayPolicyGroup);
        // Load SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Restore saved selections if any
        restoreSelectedPolicies();

        // Optional: Listen for changes to save immediately when user selects
        holidayPolicyGroup.setOnCheckedChangeListener((group, checkedId) -> savePolicy(KEY_HOLIDAY_POLICY, checkedId));

    }
    private void restoreSelectedPolicies() {
        int holidaySelectedId = sharedPreferences.getInt(KEY_HOLIDAY_POLICY, -1);
        if (holidaySelectedId != -1) {
            holidayPolicyGroup.check(holidaySelectedId);
        }
    }

    private void savePolicy(String key, int checkedRadioButtonId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, checkedRadioButtonId);
        editor.apply();

        // For feedback, you can show a toast with selected option text
        RadioButton selectedButton = findViewById(checkedRadioButtonId);
        if (selectedButton != null) {
            Toast.makeText(this, "Saved: " + selectedButton.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    // If you want to retrieve the selected policy value later:
    public String getHolidayPolicySelected() {
        int selectedId = sharedPreferences.getInt(KEY_HOLIDAY_POLICY, R.id.holiday_normal);
        RadioButton rb = findViewById(selectedId);
        return rb != null ? rb.getText().toString() : "Regular Hour Rate";
    }


}
