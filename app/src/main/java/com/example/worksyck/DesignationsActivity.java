package com.example.worksyck;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DesignationsActivity extends AppCompatActivity {

    private ArrayList<Designations> designations;
    private ArrayList<Designations> filteredList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FloatingActionButton buttonAdd;
    private RequestQueue requestQueue;
    private DesignationsAdapter adapter;
    private EditText searchEditText;
    private ImageView clearSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designations);

        recyclerView = findViewById(R.id.recyclerViewDesignations);
        buttonAdd = findViewById(R.id.buttonAdd);
        searchEditText = findViewById(R.id.searchEditText);
        clearSearch = findViewById(R.id.clearSearch);

        designations = new ArrayList<>();
        adapter = new DesignationsAdapter(this, designations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(this);

        FetchAllDesignations();

        buttonAdd.setOnClickListener(v -> showAddDialog());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
                clearSearch.setVisibility(charSequence.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        clearSearch.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearch.setVisibility(View.GONE);
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Designation");

        final EditText input = new EditText(this);
        input.setHint("Enter designation name");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                AddNewDesignation(name);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditDialog(int position) {
        Designations designation = designations.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Designation");

        final EditText input = new EditText(this);
        input.setText(designation.getName());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                UpdateDesignation(designation.getId(), newName, position);
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteDialog(int position) {
        Designations designation = designations.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Designation");
        builder.setMessage("Are you sure you want to delete \"" + designation.getName() + "\"?");

        builder.setPositiveButton("Delete", (dialog, which) -> DeleteDesignation(designation.getId(), position));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void AddNewDesignation(String name) {
        String url = "http://10.0.2.2/worksync/add_new_job_title.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String status = obj.getString("status");
                String message = obj.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                if (status.equals("success")) {
                    FetchAllDesignations();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Add Error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("job_title", name);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void UpdateDesignation(String id, String name, int position) {
        String url = "http://10.0.2.2/worksync/update_designation.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String message = obj.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                designations.get(position).setName(name);
                adapter.notifyItemChanged(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Update Error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("job_title_id", id);
                params.put("job_title_name", name);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void DeleteDesignation(String id, int position) {
        String url = "http://10.0.2.2/worksync/delete_designation.php";

        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String message = obj.getString("message");
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                designations.remove(position);
                adapter.notifyItemRemoved(position);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Delete Error", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("designation_id", id);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void FetchAllDesignations() {
        String url = "http://10.0.2.2/worksync/fetch_all_job_titiles.php";

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String status = obj.getString("status");

                if (status.equals("success")) {
                    JSONArray data = obj.getJSONArray("data");
                    designations.clear();

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        String id = item.getString("id");
                        String name = item.getString("name");
                        designations.add(new Designations(id, name));
                    }
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.e("Fetch Error", error.toString()));

        requestQueue.add(request);
    }
    private void filterDesignations(String query) {
        filteredList.clear();
        for (Designations dep : designations) {
            if (dep.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(dep);
            }
        }
        adapter.notifyDataSetChanged();
        clearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
    }


    // ----------- Adapter داخلي --------------

    public class DesignationsAdapter extends RecyclerView.Adapter<DesignationsAdapter.ViewHolder> implements android.widget.Filterable {

        private Context context;
        private ArrayList<Designations> designationsList;
        private ArrayList<Designations> designationsListFull;

        public DesignationsAdapter(Context context, ArrayList<Designations> list) {
            this.context = context;
            this.designationsList = list;
            this.designationsListFull = new ArrayList<>(list);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_designation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Designations designation = designationsList.get(position);
            holder.designationNameTextView.setText(designation.getName());

            holder.buttonEditDesignation.setOnClickListener(v -> showEditDialog(position));
            holder.buttonDeleteDesignation.setOnClickListener(v -> showDeleteDialog(position));
        }

        @Override
        public int getItemCount() {
            return designationsList.size();
        }

        @Override
        public android.widget.Filter getFilter() {
            return new android.widget.Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    ArrayList<Designations> filteredList = new ArrayList<>();
                    if (constraint == null || constraint.length() == 0) {
                        filteredList.addAll(designationsListFull);
                    } else {
                        String filterPattern = constraint.toString().toLowerCase().trim();
                        for (Designations item : designationsListFull) {
                            if (item.getName().toLowerCase().contains(filterPattern)) {
                                filteredList.add(item);
                            }
                        }
                    }
                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    designationsList.clear();
                    if (results.values != null) {
                        designationsList.addAll((ArrayList<Designations>) results.values);
                    }
                    notifyDataSetChanged();
                }
            };
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView designationNameTextView;
            ImageButton buttonEditDesignation, buttonDeleteDesignation;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                designationNameTextView = itemView.findViewById(R.id.designationNameTextView);
                buttonEditDesignation = itemView.findViewById(R.id.buttonEditDesignation);
                buttonDeleteDesignation = itemView.findViewById(R.id.buttonDeleteDesignation);
            }
        }
    }
}
