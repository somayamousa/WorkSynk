package com.example.worksyck;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeesListAdapter extends SimpleAdapter {

    private final LayoutInflater mInflater;
    private final int mResource;
    private final String[] mFrom;
    private final int[] mTo;

    public EmployeesListAdapter(Context context, List<? extends Map<String, ?>> data,
                                int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        mResource = resource;
        mFrom = from;
        mTo = to;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = mInflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        HashMap<String, String> data = (HashMap<String, String>) getItem(position);

        TextView textName = view.findViewById(R.id.textEmployeeName);
        TextView textEmail = view.findViewById(R.id.textEmployeeEmail);
        TextView textStatus = view.findViewById(R.id.textEmployeeStatus);

        textName.setText(data.get("fullname"));
        textEmail.setText(data.get("email"));

        String status = data.get("status");
        textStatus.setText("الحالة: " + (status.equals("active") ? "نشط" : "غير نشط"));
        textStatus.setTextColor(status.equals("active") ?
                view.getContext().getResources().getColor(android.R.color.holo_green_dark) :
                view.getContext().getResources().getColor(android.R.color.holo_red_dark));

        return view;
    }
}