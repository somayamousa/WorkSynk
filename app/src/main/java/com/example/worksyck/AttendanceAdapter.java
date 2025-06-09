package com.example.worksyck;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {
    private final List<AttendanceRecord> records;

    public AttendanceAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord record = records.get(position);
        holder.dayTextView.setText(record.getDayAndWeek());
        holder.checkInTextView.setText(record.getStartTimeFormatted());
        holder.checkOutTextView.setText(record.getEndTimeFormatted());
        holder.totalHrsTextView.setText(String.format("%.0f Hrs", record.getWorkedHours()));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView, checkInTextView, checkOutTextView, totalHrsTextView;
        ImageView locationIcon, clipboardIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            checkInTextView = itemView.findViewById(R.id.checkInTextView);
            checkOutTextView = itemView.findViewById(R.id.checkOutTextView);
            totalHrsTextView = itemView.findViewById(R.id.totalHrsTextView);
            locationIcon = itemView.findViewById(R.id.locationIcon);

        }
    }
}
