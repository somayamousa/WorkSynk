package com.example.worksyck;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class SalaryIncreaseAdapter extends RecyclerView.Adapter<SalaryIncreaseAdapter.ViewHolder> {
    private Context context;
    private List<SalaryIncrease> increaseList;
    private LayoutInflater inflater;
    private OnSalaryIncreaseActionListener actionListener;
    public SalaryIncreaseAdapter(Context context, List<SalaryIncrease> increaseList, OnSalaryIncreaseActionListener listener) {
        this.context = context;
        this.increaseList = increaseList;
        this.inflater = LayoutInflater.from(context);
        this.actionListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_salary_increase, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SalaryIncrease increase = increaseList.get(position);
        holder.textIncreaseType.setText(increase.getIncreaseType());
        holder.textAmount.setText(String.format(Locale.getDefault(), "%.2f", increase.getIncreaseAmount()));
        holder.textDuration.setText(increase.getDurationType());
        holder.textStartDate.setText(increase.getStartDate());
        holder.textEndDate.setText(increase.getEndDate().isEmpty() ? "N/A" : increase.getEndDate());
        holder.textNotes.setText(increase.getNotes().isEmpty() ? "No notes" : increase.getNotes());
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClicked(increase.getId(), String.valueOf(increase.getEmployeeId()));
            }
        });
        holder.btnUpdate.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEditClicked(increase);
            }
        });

    }

    @Override
    public int getItemCount() {
        return increaseList.size();
    }

    public void setList(List<SalaryIncrease> newList) {
        this.increaseList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textIncreaseType, textAmount, textDuration, textStartDate, textEndDate, textNotes;
        ImageButton btnDelete, btnUpdate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textIncreaseType = itemView.findViewById(R.id.textIncreaseType);
            textAmount = itemView.findViewById(R.id.textAmount);
            textDuration = itemView.findViewById(R.id.textDuration);
            textStartDate = itemView.findViewById(R.id.textStartDate);
            textEndDate = itemView.findViewById(R.id.textEndDate);
            textNotes = itemView.findViewById(R.id.textNotes);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
        }
    }

    public interface OnSalaryIncreaseActionListener {
        void onDeleteClicked(int increaseId, String employeeId);
        void onEditClicked(SalaryIncrease increase);
    }

    public boolean removeIncreaseById(int increaseId) {
        for (int i = 0; i < increaseList.size(); i++) {
            if (increaseList.get(i).getId() == increaseId) {
                increaseList.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, increaseList.size());
                return increaseList.isEmpty();
            }
        }
        return false;
    }
    public void updateIncrease(SalaryIncrease updatedIncrease) {
        for (int i = 0; i < increaseList.size(); i++) {
            if (increaseList.get(i).getId() == updatedIncrease.getId()) {
                increaseList.set(i, updatedIncrease);
                notifyItemChanged(i);
                break;
            }
        }
    }




}