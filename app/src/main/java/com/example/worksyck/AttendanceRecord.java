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

    public AttendanceRecord(int id, int userId, String date, String startTime, String endTime) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    public double getWorkedHours() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);
        try {
            Date start = format.parse(startTime);
            Date end = format.parse(endTime);

            if (end == null || endTime.equals("00:00:00")) {
                long diffMillis = (24 * 60 * 60 * 1000) - start.getTime();
                return diffMillis / (1000.0 * 60 * 60);
            } else {
                long diffMillis = end.getTime() - start.getTime();
                if (diffMillis < 0) {
                    diffMillis += 24 * 60 * 60 * 1000;
                }
                return diffMillis / (1000.0 * 60 * 60);
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
