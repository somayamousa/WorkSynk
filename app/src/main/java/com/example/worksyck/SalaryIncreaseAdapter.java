package com.example.worksyck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SalaryIncreaseAdapter extends RecyclerView.Adapter<SalaryIncreaseAdapter.ViewHolder> {

    private Context context;
    private List<SalaryIncrease> salaryIncreases;
    private OnSalaryIncreaseActionListener listener;

    public interface OnSalaryIncreaseActionListener {
        void onEditClicked(SalaryIncrease increase);
        void onDeleteClicked(int increaseId, String employeeId);
    }

    public SalaryIncreaseAdapter(Context context, List<SalaryIncrease> salaryIncreases, OnSalaryIncreaseActionListener listener) {
        this.context = context;
        this.salaryIncreases = salaryIncreases;
        this.listener = listener;
    }

    public void setList(List<SalaryIncrease> increases) {
        this.salaryIncreases.clear();
        this.salaryIncreases.addAll(increases);
        notifyDataSetChanged();
    }

    public boolean removeIncreaseById(int increaseId) {
        for (int i = 0; i < salaryIncreases.size(); i++) {
            if (salaryIncreases.get(i).getId() == increaseId) {
                salaryIncreases.remove(i);
                notifyItemRemoved(i);
                return salaryIncreases.isEmpty();
            }
        }
        return false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_salary_increase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalaryIncrease increase = salaryIncreases.get(position);

        holder.tvAmount.setText(String.format("Amount: %.2f", increase.getIncreaseAmount()));
        holder.tvType.setText("Type: " + increase.getIncreaseType());
        holder.tvDuration.setText("Duration: " + increase.getDurationType());
        holder.tvStartDate.setText("Start: " + increase.getStartDate());
        holder.tvEndDate.setText("End: " + (increase.getEndDate().isEmpty() ? "N/A" : increase.getEndDate()));
        holder.tvNotes.setText("Notes: " + increase.getNotes());

        holder.btnEdit.setOnClickListener(v -> listener.onEditClicked(increase));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClicked(increase.getId(), String.valueOf(increase.getEmployeeId())));
    }

    @Override
    public int getItemCount() {
        return salaryIncreases.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAmount, tvType, tvDuration, tvStartDate, tvEndDate, tvNotes;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvType = itemView.findViewById(R.id.tvType);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvStartDate = itemView.findViewById(R.id.tvStartDate);
            tvEndDate = itemView.findViewById(R.id.tvEndDate);
            tvNotes = itemView.findViewById(R.id.tvNotes);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}