package com.example.worksyck;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.worksyck.Employee;
import com.example.worksyck.R;
import java.util.List;
public class EmployeeSpinnerAdapter extends ArrayAdapter<Employee> {
    private Context context;
    private List<Employee> employees;
    public EmployeeSpinnerAdapter(Context context, List<Employee> employees) {
        super(context, 0, employees);
        this.context = context;
        this.employees = employees;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }
    private View createCustomView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_employee_item, parent, false);
        }
        TextView textName = convertView.findViewById(R.id.textName);
        TextView textEmail = convertView.findViewById(R.id.textEmail);
        Employee employee = employees.get(position);
        String fullName = employee.getFullname();
        String email = employee.getEmail();
        textName.setText(fullName != null ? fullName : "");
        textEmail.setText(email != null ? email : "");
        return convertView;
    }
}