package com.example.worksyck;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceRecord {
    private int id;
    private int userId;
    private String date;       // بالشكل "yyyy-MM-dd"
    private String startTime;  // بالشكل "HH:mm:ss"
    private String endTime;    // بالشكل "HH:mm:ss"
    private String status; // NEW
    public AttendanceRecord(int id, int userId, String date, String startTime, String endTime, String status) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }  public AttendanceRecord(int id, int userId, String date, String startTime, String endTime) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;

    }

    public String getDayAndDate() {
        // ترجع مثلاً: "03 Tue" مثل ما في الصورة
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd EEE", Locale.US);
        try {
            Date date = inputFormat.parse(this.date);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return this.date;
        }
    }


    // Getters...
    public String getStatus() {
        return status;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public double getWorkedHours() {
        if (startTime == null || endTime == null || startTime.trim().isEmpty() || endTime.trim().isEmpty())
            return 0;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
            Date start = sdf.parse(startTime.trim());
            Date end = sdf.parse(endTime.trim());
            long diffMs = end.getTime() - start.getTime();
            return diffMs / (1000.0 * 60 * 60);
        } catch (Exception e) {
            return 0;
        }
    }

    public String getDayAndWeek() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsedDate = sdf.parse(date);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd EEE", Locale.US);
            return displayFormat.format(parsedDate);
        } catch (ParseException e) {
            return date;
        }
    }

    public String getStartTimeFormatted() {
        return convertTo12Hour(startTime);
    }

    public String getEndTimeFormatted() {
        return convertTo12Hour(endTime);
    }

    private String convertTo12Hour(String time24) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("HH:mm:ss", Locale.US);
            SimpleDateFormat output = new SimpleDateFormat("hh:mm a", Locale.US);
            Date date = input.parse(time24);
            return output.format(date);
        } catch (ParseException e) {
            return time24;
        }
    }
}
