package in.yagnyam.myid.data;

import android.content.Context;
import android.util.Log;

import java.lang.ref.WeakReference;

import in.yagnyam.myid.model.ProfileEntry;

public class ProfilesDataSet {

    private static final String TAG = "ProfilesDataSet";

    private static final Object lock = new Object();
    private static volatile WeakReference<DataSet<ProfileEntry>> instance = null;

    public static DataSet<ProfileEntry> getInstance(Context context) {
        synchronized (lock) {
            DataSet<ProfileEntry> dataSet;
            if (instance == null) {
                Log.d(TAG, "Creating instance for ProfilesDataSet");
                dataSet = createDataSet(context);
                instance = new WeakReference<>(dataSet);
                Log.d(TAG, "Set instance to: " + instance + " on " + Thread.currentThread().getName() + " in process " + android.os.Process.myPid());
            } else {
                dataSet = instance.get();
                if (dataSet == null) {
                    Log.d(TAG, "Re-creating Data Set");
                    dataSet = createDataSet(context);
                    instance = new WeakReference<>(dataSet);
                    Log.d(TAG, "Set instance to: " + instance + " on " + Thread.currentThread().getName() + " in process " + android.os.Process.myPid());
                } else {
                    Log.d(TAG, "Valid instance: " + instance + " on " + Thread.currentThread().getName() + " in process " + android.os.Process.myPid());
                }
            }
            return dataSet;
        }
    }


    private static DataSet<ProfileEntry> getDataSet() {
        synchronized (lock) {
            DataSet<ProfileEntry> dataSet = instance == null ? null : instance.get();
            Log.d(TAG, "getDataSet() => instance:" + instance + ", dataSet:" + dataSet + " on " + Thread.currentThread().getName());
            return dataSet;
        }
    }


    private static DataSet<ProfileEntry> createDataSet(Context context) {
        Log.d(TAG, "Fetching profiles from DB");
        return new DataSet<>(DbHelper.getInstance(context).fetchProfiles());
    }


    public static void addProfile(ProfileEntry contact) {
        synchronized (lock) {
            DataSet<ProfileEntry> dataSet = getDataSet();
            if (dataSet != null) {
                Log.d(TAG, "Adding " + contact + " to Cache");
                dataSet.addItem(contact);
            } else {
                Log.d(TAG, "Ignoring new Profile " + contact + " as instance: " + instance + " isn't valid on " + Thread.currentThread().getName() + " in process " + android.os.Process.myPid());
            }
        }
    }

    public static void updateProfile(ProfileEntry contact) {
        synchronized (lock) {
            DataSet<ProfileEntry> dataSet = getDataSet();
            if (dataSet != null) {
                Log.d(TAG, "Updating " + contact + " in Cache");
                dataSet.updateItem(contact);
            } else {
                Log.d(TAG, "Ignoring update Profile " + contact + " as instance: " + instance + " isn't valid on " + Thread.currentThread().getName() + " in process " + android.os.Process.myPid());
            }
        }
    }

}
