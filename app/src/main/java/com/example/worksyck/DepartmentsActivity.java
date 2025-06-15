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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView recyclerViewDepartments;
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

        recyclerViewDepartments = findViewById(R.id.recyclerViewDepartments);
        searchEditText = findViewById(R.id.editTextDepartmentSearch);
        clearSearch = findViewById(R.id.clearSearchDepartment);
        addButton = findViewById(R.id.buttonAddDepartment);

        requestQueue = Volley.newRequestQueue(this);

        adapter = new DepartmentAdapter();
        recyclerViewDepartments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDepartments.setAdapter(adapter);

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
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (Departments dep : departmentsList) {
                        if (dep.getName().equalsIgnoreCase(name)) {
                            Toast.makeText(this, "Department already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    addDepartment(name);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Departments dep, int position) {
        EditText input = new EditText(this);
        input.setText(dep.getName());
        new AlertDialog.Builder(this)
                .setTitle("Edit Department")
                .setView(input)
                .setPositiveButton("Update", (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (Departments other : departmentsList) {
                        if (!other.getId().equals(dep.getId()) &&
                                other.getName().equalsIgnoreCase(name)) {
                            Toast.makeText(this, "Another department with this name exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    updateDepartment(dep.getId(), name, position);
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
                    fetchAllDepartments();
                } else {
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
            try {
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.getString("status");
                String message = jsonObject.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                if (status.equals("success")) {
                    fetchAllDepartments();
                }
            } catch (JSONException e) {
                Log.e("Delete Error", e.toString());
                Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            Log.e("Delete Error", error.toString());
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                return Collections.singletonMap("department_id", id);
            }
        };
        requestQueue.add(request);
    }
    private void updateDepartment(String id, String name, int position) {
        String url = BASE_URL + "update_department.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String message = obj.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                // تحديث في filteredList
                Departments dep = filteredList.get(position);
                dep.setName(name);

                // تحديث في departmentsList
                for (Departments d : departmentsList) {
                    if (d.getId().equals(id)) {
                        d.setName(name);
                        break;
                    }
                }

                adapter.notifyItemChanged(position);

            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    private class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

        @Override
        public DepartmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(DepartmentsActivity.this)
                    .inflate(R.layout.list_item_department, parent, false);
            return new DepartmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DepartmentViewHolder holder, int position) {
            Departments dep = filteredList.get(position);
            holder.bind(dep);
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        class DepartmentViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            ImageButton btnEdit, btnDelete;

            DepartmentViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.departmentNameTextView);
                btnEdit = itemView.findViewById(R.id.btnEditDepartment);
                btnDelete = itemView.findViewById(R.id.btnDeleteDepartment);
            }

            void bind(final Departments dep) {
                nameText.setText(dep.getName());

                btnEdit.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        showEditDialog(dep, position);
                    }
                });

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(DepartmentsActivity.this)
                            .setTitle("Delete")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Yes", (d, w) -> deleteDepartment(dep.getId()))
                            .setNegativeButton("No", null)
                            .show();
                });
            }
        }
    }

    private static class Departments {
        private final String id;
        private String name;

        public Departments(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
