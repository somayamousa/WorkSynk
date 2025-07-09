 package com.example.worksyck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SalaryDraftAdapter extends RecyclerView.Adapter<SalaryDraftAdapter.ViewHolder> {
    private List<SalaryDraft> draftList;
    private OnViewDetailsClickListener viewDetailsListener;
    private OnApproveClickListener approveListener;
    private OnCancelClickListener cancelListener;

    public interface OnViewDetailsClickListener {
        void onViewDetailsClick(SalaryDraft draft);
    }

    public interface OnApproveClickListener {
        void onApproveClick(SalaryDraft draft);
    }

    public interface OnCancelClickListener {
        void onCancelClick(SalaryDraft draft);
    }

    public SalaryDraftAdapter(List<SalaryDraft> draftList, OnViewDetailsClickListener viewDetailsListener,
                              OnApproveClickListener approveListener, OnCancelClickListener cancelListener) {
        this.draftList = draftList;
        this.viewDetailsListener = viewDetailsListener;
        this.approveListener = approveListener;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_salary_draft, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalaryDraft draft = draftList.get(position);
        holder.employeeNameText.setText(draft.getEmployeeName());
        holder.netSalaryText.setText("Net Salary: " + String.format("%.2f", draft.getNetSalary()) + " ₪");

        holder.viewDetailsBtn.setOnClickListener(v -> viewDetailsListener.onViewDetailsClick(draft));
        holder.approveBtn.setOnClickListener(v -> approveListener.onApproveClick(draft));
        holder.cancelBtn.setOnClickListener(v -> cancelListener.onCancelClick(draft));
    }

    @Override
    public int getItemCount() {
        return draftList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView employeeNameText, employeeCodeText, departmentText, designationText, netSalaryText;
        Button viewDetailsBtn, approveBtn, cancelBtn;

        ViewHolder(View itemView) {
            super(itemView);
            employeeNameText = itemView.findViewById(R.id.employeeNameText);
            employeeCodeText = itemView.findViewById(R.id.employeeCodeText);
            departmentText = itemView.findViewById(R.id.departmentText);
            designationText = itemView.findViewById(R.id.designationText);
            netSalaryText = itemView.findViewById(R.id.netSalaryText);
            viewDetailsBtn = itemView.findViewById(R.id.viewDetailsBtn);
            approveBtn = itemView.findViewById(R.id.approveBtn);
            cancelBtn = itemView.findViewById(R.id.cancelBtn);
        }
    }
}