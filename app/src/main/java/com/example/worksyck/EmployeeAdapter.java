package com.example.worksyck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.ViewHolder> {
    private List<Employee> employees;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Employee employee);
    }

    public EmployeeAdapter(List<Employee> employees, OnItemClickListener listener) {
        this.employees = employees;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Employee employee = employees.get(position);
        holder.employeeName.setText(employee.getFullname());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(employee));
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName;

        public ViewHolder(View itemView) {
            super(itemView);
            employeeName = itemView.findViewById(R.id.employee_name);
        }
    }
}