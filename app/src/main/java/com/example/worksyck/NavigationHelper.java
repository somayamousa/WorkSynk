package com.example.worksyck;

import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class NavigationHelper {

    private AppCompatActivity activity;

    public NavigationHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    // تفعيل زر الرجوع في ActionBar
    public void enableBackButton() {
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    // إعداد مستمعات الأزرار في Bottom Navigation
    public void setBottomNavigationListeners(LinearLayout[] bottomNavItems, LinearLayout homeLayout, LinearLayout requestsLayout, LinearLayout checkInLayout) {
        for (LinearLayout item : bottomNavItems) {
            item.setOnClickListener(v -> {
                navigateToActivity(getActivityClass(item, homeLayout, requestsLayout, checkInLayout));  // التنقل للنشاط المناسب
                updateSelection(item, homeLayout, requestsLayout, checkInLayout);  // تحديث حالة التحديد
            });
        }
    }

    // التنقل إلى النشاط المناسب
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
    }

    // تحديد النشاط المناسب بناءً على العنصر الذي تم النقر عليه
    private Class<?> getActivityClass(LinearLayout item, LinearLayout homeLayout, LinearLayout requestsLayout, LinearLayout checkInLayout) {
        if (item == homeLayout) return MainActivity.class;
        if (item == requestsLayout) return RequestsActivity.class;
        if (item == checkInLayout) return attendance.class;  // إضافة التنقل إلى CheckInActivity
        return null;
    }

    // تحديث حالة التحديد للـ Bottom Navigation Items
    private void updateSelection(LinearLayout selectedLayout, LinearLayout homeLayout, LinearLayout requestsLayout, LinearLayout checkInLayout) {
        homeLayout.setSelected(false);
        requestsLayout.setSelected(false);
        checkInLayout.setSelected(false);  // إضافة التحديد لـ checkInLayout
        selectedLayout.setSelected(true);
    }

    // تعيين مستمع للضغط على زر الرجوع
    public void setBackButtonListener(ImageView backButton) {
        // تعيين مستمع للنقر على زر الرجوع
        backButton.setOnClickListener(v -> activity.onBackPressed());  // العودة للنشاط السابق عند الضغط على زر الرجوع
    }
}
