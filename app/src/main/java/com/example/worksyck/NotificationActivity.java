package com.example.worksyck;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotificationAdapter adapter;
    List<Notification> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerView = findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationsList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationsList);
        recyclerView.setAdapter(adapter);

        loadNotificationsFromServer();

        // ✅ عندما يفتح المستخدم صفحة الإشعارات، نعتبر أنه قرأها
        SharedPreferences prefs = getSharedPreferences("notifs", MODE_PRIVATE);
        prefs.edit().putBoolean("hasUnread", false).apply();
    }

    private void loadNotificationsFromServer() {
        int userId = 0; // الرقم ثابت كما طلبت

        String url = "http://10.0.2.2/worksync/get_notifications.php?user_id=" + userId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // ✅ إذا في إشعارات جديدة، نعتبرها غير مقروءة
                            if (response.length() > 0) {
                                SharedPreferences prefs = getSharedPreferences("notifs", MODE_PRIVATE);
                                prefs.edit().putBoolean("hasUnread", true).apply();
                            }

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject obj = response.getJSONObject(i);
                                String title = obj.getString("title");
                                String message = obj.getString("message");
                                String time = obj.getString("time");

                                notificationsList.add(new Notification(title, message, time));
                            }

                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }

    // نموذج بيانات الإشعار
    public static class Notification {
        String title;
        String message;
        String time;

        public Notification(String title, String message, String time) {
            this.title = title;
            this.message = message;
            this.time = time;
        }
    }

    // Adapter للـ RecyclerView
    public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        List<Notification> notifications;

        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.notification_item, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
            Notification notification = notifications.get(position);
            holder.title.setText(notification.title);
            holder.message.setText(notification.message);
            holder.time.setText(notification.time);
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder {
            TextView title, message, time;

            public NotificationViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textViewNotificationTitle);
                message = itemView.findViewById(R.id.textViewNotificationMessage);
                time = itemView.findViewById(R.id.textViewNotificationTime);
            }
        }
    }
}