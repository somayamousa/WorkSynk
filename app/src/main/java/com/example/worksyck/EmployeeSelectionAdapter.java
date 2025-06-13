package com.example.worksyck;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmployeeSelectionAdapter extends RecyclerView.Adapter<EmployeeSelectionAdapter.EmployeeViewHolder> {
    private List<Employee> employeeList;
    private List<Employee> filteredList;
    private Set<Integer> selectedEmployeeIds;

    public EmployeeSelectionAdapter(List<Employee> employees, Set<Integer> selectedIds) {
        this.employeeList = employees;
        this.filteredList = new ArrayList<>(employees);
        this.selectedEmployeeIds = new HashSet<>();
        // Initialize with all employees selected by default
        for (Employee e : employees) {
            try {
                selectedEmployeeIds.add(Integer.parseInt(e.getId()));
            } catch (NumberFormatException ex) {
                Log.e("EmployeeSelectionAdapter", "Invalid employee ID: " + e.getId());
            }
        }
        // If selectedIds is provided and not empty, override with it
        if (selectedIds != null && !selectedIds.isEmpty()) {
            selectedEmployeeIds.retainAll(selectedIds); // Keep only IDs in selectedIds
        }
    }

    public Set<Integer> getSelectedEmployeeIds() {
        return selectedEmployeeIds;
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(employeeList);
        } else {
            for (Employee employee : employeeList) {
                if (employee.getFullname().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(employee);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_selection, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee emp = filteredList.get(position);
        holder.checkBox.setText(emp.getFullname());

        try {
            int empId = Integer.parseInt(emp.getId());
            holder.checkBox.setChecked(selectedEmployeeIds.contains(empId));
            holder.checkBox.setEnabled(true);
        } catch (NumberFormatException ex) {
            Log.e("EmployeeSelectionAdapter", "Invalid employee ID: " + emp.getId());
            holder.checkBox.setChecked(false);
            holder.checkBox.setEnabled(false);
        }

        // Reset listener to avoid duplicate listeners
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
            try {
                int empId = Integer.parseInt(emp.getId());
                if (isChecked) {
                    selectedEmployeeIds.add(empId);
                } else {
                    selectedEmployeeIds.remove(empId);
                }
            } catch (NumberFormatException ex) {
                Log.e("EmployeeSelectionAdapter", "Invalid employee ID: " + emp.getId());
            }
        });

        holder.detailsButton.setOnClickListener(v -> showEmployeeDetails(v.getContext(), emp));
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    private void showEmployeeDetails(Context context, Employee emp) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_employee_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView title = dialog.findViewById(R.id.title);
        TextView message = dialog.findViewById(R.id.message);
        ImageButton closeButton = dialog.findViewById(R.id.closeButton);
        Window window = dialog.getWindow();
        if (window != null) {
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        title.setText("Employee Details");
        message.setText("🔹 Employee Code: " + emp.getEmployee_code()
                + "\n\n" +
                "📧 Email: " + emp.getEmail() + "\n\n" +
                "🏢 Department: " + emp.getDepartment_name() + "\n\n" +
                "💼 Job Title: " + emp.getDesignation_name());
        closeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        ImageButton detailsButton;
        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            detailsButton = itemView.findViewById(R.id.detailsButton);
        }
    }
}