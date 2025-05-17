package com.example.worksyck;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class DepartmentsActivity extends AppCompatActivity {

    private EditText searchEditText;
    private ImageView clearSearch;
    private ListView listView;
    private FloatingActionButton addButton;

    private ArrayList<Departments> departmentsList = new ArrayList<>();
    private ArrayList<Departments> filteredList = new ArrayList<>();
    private DepartmentAdapter adapter;

    private RequestQueue requestQueue;
    private static final String BASE_URL = "http://10.0.2.2/worksync/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_departments);

        listView = findViewById(R.id.listViewDesignations);
        searchEditText = findViewById(R.id.editTextDepartmentSearch);
        clearSearch = findViewById(R.id.clearSearchDepartment);
        addButton = findViewById(R.id.buttonAddDepartment);

        requestQueue = Volley.newRequestQueue(this);

        adapter = new DepartmentAdapter();
        listView.setAdapter(adapter);

        addButton.setOnClickListener(v -> showAddDepartmentDialog());

        clearSearch.setOnClickListener(v -> searchEditText.setText(""));
        searchEditText.addTextChangedListener(searchWatcher);

        fetchAllDepartments();
    }

    private void fetchAllDepartments() {
        String url = BASE_URL + "fetch_all_departments.php";
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                departmentsList.clear();
                JSONObject json = new JSONObject(response);
                if (json.getString("status").equals("success")) {
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject dep = data.getJSONObject(i);
                        String id = dep.getString("id");
                        String name = dep.getString("name");
                        departmentsList.add(new Departments(id, name));
                    }
                    filterDepartments(searchEditText.getText().toString());
                } else {
                    Toast.makeText(this, json.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Log.e("JSON Error", e.toString());
            }
        }, error -> Log.e("Volley Error", error.toString()));
        requestQueue.add(request);
    }

    private void filterDepartments(String query) {
        filteredList.clear();
        for (Departments dep : departmentsList) {
            if (dep.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(dep);
            }
        }
        adapter.notifyDataSetChanged();
        clearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showAddDepartmentDialog() {
        EditText input = new EditText(this);
        input.setHint("Department Name");
        new AlertDialog.Builder(this)
                .setTitle("Add Department")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        addDepartment(name);
                    } else {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Departments dep) {
        EditText input = new EditText(this);
        input.setText(dep.getName());
        new AlertDialog.Builder(this)
                .setTitle("Edit Department")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        updateDepartment(dep.getId(), name);
                    } else {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addDepartment(String name) {
        String url = BASE_URL + "add_new_department.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");

                if (status.equals("success")) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    fetchAllDepartments(); // لتحديث القائمة بعد الإضافة الناجحة
                } else {
                    // هنا نعرض رسالة الخطأ اللي جتنا من السيرفر
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Add Error", error.toString());
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                return Collections.singletonMap("department_name", name);
            }
        };
        requestQueue.add(request);
    }


    private void deleteDepartment(String id) {
        String url = BASE_URL + "delete_department.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            fetchAllDepartments();
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        }, error -> Log.e("Delete Error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                return Collections.singletonMap("department_id", id);
            }
        };
        requestQueue.add(request);
    }

    private void updateDepartment(String id, String name) {
        String url = BASE_URL + "update_department.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            fetchAllDepartments();
            Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        }, error -> Log.e("Update Error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("department_id", id);
                map.put("department_name", name);
                return map;
            }
        };
        requestQueue.add(request);
    }

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            filterDepartments(s.toString());
        }
        @Override public void afterTextChanged(Editable s) {}
    };

    private class DepartmentAdapter extends BaseAdapter {
        @Override public int getCount() { return filteredList.size(); }
        @Override public Object getItem(int i) { return filteredList.get(i); }
        @Override public long getItemId(int i) { return i; }

        @Override
        public View getView(int i, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(DepartmentsActivity.this)
                        .inflate(R.layout.list_item_department, parent, false);
            }

            TextView nameText = view.findViewById(R.id.departmentNameTextView);
            ImageButton btnEdit = view.findViewById(R.id.btnEditDepartment);
            ImageButton btnDelete = view.findViewById(R.id.btnDeleteDepartment);

            Departments dep = filteredList.get(i);
            nameText.setText(dep.getName());

            btnEdit.setOnClickListener(v -> showEditDialog(dep));
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(DepartmentsActivity.this)
                        .setTitle("Delete")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", (d, w) -> deleteDepartment(dep.getId()))
                        .setNegativeButton("No", null)
                        .show();
            });

            return view;
        }
    }

    // كلاس داخلي Departments
    private static class Departments {
        private final String id;
        private final String name;

        public Departments(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }
}
