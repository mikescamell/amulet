package scamell.michael.amulet;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateAndTime {

//    public static String getDateAndTimeNow() {
//        String dateFormatString = "dd-MM-yyyy HH:mm:ss";
//
//        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
//        Date dateNow = new Date();
//        return dateFormat.format(dateNow);
//    }

    /**
     * Date Formats:
     * http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
     */
    public static String getDateAndTimeNowForTasks() {
        String dateFormatString = "yyyy-MM-dd HH:mm:ss.SSS";

        DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        Date dateNow = new Date();
        return dateFormat.format(dateNow);
    }

    public static String setDateAndTimeForApp(String dateToConvert) {
        String newDate = null;
        dateToConvert = dateToConvert.replace("T", " ");
        SimpleDateFormat convertFromDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat convertToDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            Date date = convertFromDate.parse(dateToConvert);
            newDate = convertToDate.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("ERROR_PARSING_DATE", "Couldn't parse date from server");
        }
        return newDate;
    }

    private static String getDateSuffix(int day) {
        switch (day) {
            case 1:
            case 21:
            case 31:
                return ("st");
            case 2:
            case 22:
                return ("nd");
            case 3:
            case 23:
                return ("rd");
            default:
                return ("th");
        }
    }

    public static String getDateAndTime(Context context) {
        String dateString = SharedPreferencesWrapper.getFromPrefs(context, "last_date_played", "Today");
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdfTimeDay = new SimpleDateFormat("h:mmaaa, d");
        SimpleDateFormat sdfMonthYear = new SimpleDateFormat(" MMMM, yyyy");
        Date date = new Date();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HOME_FRAGMENT", "Couldn't parse date string or no previous date");
        }
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dateSuffix = DateAndTime.getDateSuffix(day);
        String timeDay = sdfTimeDay.format(date);
        //http://stackoverflow.com/questions/13581608/displaying-am-and-pm-in-small-letter-after-date-formatting
        timeDay = timeDay.replace("AM", "am").replace("PM", "pm");
        String monthYear = sdfMonthYear.format(date);
        return timeDay + dateSuffix + monthYear;
    }
}
