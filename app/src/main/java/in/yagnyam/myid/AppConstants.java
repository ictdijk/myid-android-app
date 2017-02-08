package in.yagnyam.myid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppConstants {

    private static final String TAG = "AppConstants";

    private static final String BSN = "bsn";
    private static final String NODE_PATH = "nodePath";
    private static final String NAME = "name";
    private static final String IDENTITY = "identity";
    private static final String DOB = "dob";
    private static final String DIGID = "digid";

    public static String getDigid(Context context) {
        return getStringProperty(context, DIGID);
    }

    public static void setDigid(Context context, String digid) {
        setStringProperty(context, DIGID, digid);
    }

    public static String getBsn(Context context) {
        return getStringProperty(context, BSN);
    }

    public static void setBsn(Context context, String bsn) {
        setStringProperty(context, BSN, bsn);
    }

    public static String getNodePath(Context context) {
        return getStringProperty(context, NODE_PATH);
    }

    public static void setNodePath(Context context, String nodePath) {
        setStringProperty(context, NODE_PATH, nodePath);
    }

    public static boolean hasName(Context context) {
        return hasProperty(context, NAME);
    }

    public static void setName(Context context, String name) {
        setStringProperty(context, NAME, name);
    }

    public static String getName(Context context) {
        return getStringProperty(context, NAME);
    }

    public static boolean hasId(Context context) {
        return hasProperty(context, IDENTITY);
    }

    public static void setId(Context context, String id) {
        setStringProperty(context, IDENTITY, id);
    }

    public static String getId(Context context) {
        return getStringProperty(context, IDENTITY);
    }

    public static boolean hasDob(Context context) {
        return hasProperty(context, DOB);
    }

    public static void setDob(Context context, Date value) {
        setDateProperty(context, DOB, value);
    }

    public static void setDob(Context context, String value) throws ParseException {
        setDateProperty(context, DOB, stringToDate(value));
    }

    public static Date getDob(Context context) {
        return getDateProperty(context, DOB);
    }

    public static String getDobString(Context context) {
        return dateToString(getDateProperty(context, DOB));
    }

    private static boolean hasProperty(Context context, String property) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.contains(property);
    }

    private static void setStringProperty(Context context, String property, String value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(property, value.trim());
        editor.apply();
    }

    private static String getStringProperty(Context context, String property) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(property, "");
    }

    private static void setDateProperty(Context context, String property, Date value) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(property, value.getTime());
        editor.apply();
    }

    private static Date getDateProperty(Context context, String property) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long value = preferences.getLong(property, 0);
        return value == 0 ? null : new Date(value);
    }

    public static boolean isValidDate(String value) {
        try {
            stringToDate(value);
            return true;
        } catch (ParseException e) {
            Log.e(TAG, "Invalid Date", e);
            return false;
        }
    }

    public static Date stringToDate(String value) throws ParseException {
        return new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault()).parse(value);
    }

    public static String dateToString(Date value) {
        return value == null ? "" : new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault()).format(value);
    }

}
