package in.yagnyam.myid.data;

import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


public class DataSet<T> {

    private static final String TAG = "DataSet";

    private final Object lock = new Object();
    private final ArrayList<T> items;
    private final LinkedList<WeakReference<? extends DataSetObserver<T>>> observers = new LinkedList<>();

    public DataSet(Collection<T> items) {
        this.items = new ArrayList<>(items);
    }
    public DataSet() {
        this(new ArrayList<T>());
    }

    public T get(int pos) {
        synchronized (lock) {
            return items.get(pos);
        }
    }

    public void registerObserver(DataSetObserver<T> observer) {
        synchronized (lock) {
            for (Iterator<WeakReference<? extends DataSetObserver<T>>> iterator = observers.iterator(); iterator.hasNext(); ) {
                WeakReference<? extends DataSetObserver<T>> e = iterator.next();
                DataSetObserver o = e.get();
                if (o == null) {
                    iterator.remove();
                } else if (o == observer) {
                    // Return not break
                    return;
                }
            }
            observers.add(new WeakReference<DataSetObserver<T>>(observer));
        }
    }

    public void unRegisterObserver(DataSetObserver<T> observer) {
        synchronized (lock) {
            for (Iterator<WeakReference<? extends DataSetObserver<T>>> iterator = observers.iterator(); iterator.hasNext(); ) {
                WeakReference<? extends DataSetObserver<T>> e = iterator.next();
                DataSetObserver o = e.get();
                if (o == null || o == observer) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public void addItem(T item) {
        synchronized (lock) {
            int pos = items.size();
            items.add(item);
            for (Iterator<WeakReference<? extends DataSetObserver<T>>> iterator = observers.iterator(); iterator.hasNext(); ) {
                WeakReference<? extends DataSetObserver<T>> e = iterator.next();
                DataSetObserver o = e.get();
                if (o == null) {
                    iterator.remove();
                } else {
                    o.itemInserted(pos);
                    break;
                }
            }
        }
    }

    public void updateItem(T item) {
        synchronized (lock) {
            int pos = -1;
            for (int i = 0; i < items.size(); ++i) {
                if (items.get(i).equals(item)) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                Log.d(TAG, "updateItem(" + item + ") - nothing matched in " + this);
                addItem(item);
                return;
            }
            updateItem(pos, item);
        }
    }

    private void updateItem(int pos, T item) {
        Log.d(TAG, "updateItem(" + pos + ", " + item + ")");
        synchronized (lock) {
            items.set(pos, item);
            for (Iterator<WeakReference<? extends DataSetObserver<T>>> iterator = observers.iterator(); iterator.hasNext(); ) {
                WeakReference<? extends DataSetObserver<T>> e = iterator.next();
                DataSetObserver o = e.get();
                if (o == null) {
                    iterator.remove();
                } else {
                    o.itemChanged(pos);
                    break;
                }
            }
        }
    }


    public void removeItem(T item) {
        Log.d(TAG, "removeItem(" + item + ")");
        synchronized (lock) {
            int pos = -1;
            for (int i = 0; i < items.size(); ++i) {
                if (items.get(i).equals(item)) {
                    pos = i;
                    break;
                }
            }
            if (pos == -1) {
                return;
            }
            removeItem(pos);
        }
    }


    private void removeItem(int pos) {
        Log.d(TAG, "removeItem(" + pos + ")");
        synchronized (lock) {
            T removed = items.remove(pos);
            Log.d(TAG, "removeItem(" + pos + ") => " + removed);
            for (Iterator<WeakReference<? extends DataSetObserver<T>>> iterator = observers.iterator(); iterator.hasNext(); ) {
                WeakReference<? extends DataSetObserver<T>> e = iterator.next();
                DataSetObserver o = e.get();
                if (o == null) {
                    iterator.remove();
                } else {
                    o.itemRemoved(pos);
                    break;
                }
            }
        }
    }

    public int size() {
        synchronized (lock) {
            return items.size();
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(items.toArray());
    }
}
