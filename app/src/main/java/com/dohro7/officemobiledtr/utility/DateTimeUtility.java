package com.dohro7.officemobiledtr.utility;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateTimeUtility {

    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        return format.format(calendar.getTime());

    }

    public static int getDayOfTheWeek() {
        Calendar calendar = Calendar.getInstance();

        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);

        return year + "-" + new DecimalFormat("00").format(month) + "-" + new DecimalFormat("00").format(day);
    }

    public static String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);



        return year + "-" + new DecimalFormat("00").format(month) + "-" + new DecimalFormat("00").format(day)
                + " " + format.format(calendar.getTime());
    }

    public static String getFilenameDate(String userid) {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR);
        int minutes = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int milliseconds = calendar.get(Calendar.MILLISECOND);

        return year + "" + new DecimalFormat("00").format(month) + ""
                + new DecimalFormat("00").format(day) + "" + new DecimalFormat("00").format(minutes) + "" + new DecimalFormat("00").format(hour)
                + "" + new DecimalFormat("00").format(second) + "" + new DecimalFormat("00").format(milliseconds) + "" + userid;
    }

    public static String getCurrentDateString(int year, int month, int day) {
        return getMonthById(month) + " " + day + ", " + year;
    }


    public static String dateSlashFormatter(String date) {
        String[] mDate = date.split("-");
        String from = mDate[0].replace("-", "/");
        String to = mDate[2].replace("-", "/");
        return from + "-" + to;
    }

    public static String twoDigitFormat(int value)
    {
        return new DecimalFormat("00").format(value);
    }

    public static String getMonthById(int id) {
        switch (id) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "DEFAULT";
        }

    }
}