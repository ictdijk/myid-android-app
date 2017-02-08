package in.yagnyam.myid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.Date;

import in.yagnyam.myid.model.ProfileEntry;


public class DbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DbHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "myid.db";

    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String DATE_TYPE = " NUMBER";
    public static final String NUMBER_TYPE = " NUMBER";
    public static final String BIG_TEXT_TYPE = " BLOB";
    public static final String BIG_INTEGER_TYPE = " TEXT";
    public static final String BOOLEAN_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";
    private static DbHelper sInstance;
    private final SQLiteDatabase db;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase();
    }

    public static synchronized DbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DbHelper(context);
        }
        return sInstance;
    }

    public static synchronized void deleteInstance(Context context) {
        context.deleteDatabase(DATABASE_NAME);
        sInstance = null;
    }

    public static boolean has(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return columnIndex != -1 && !cursor.isNull(columnIndex);
    }

    public static int getInteger(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? 0 : cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static long getLong(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? 0 : cursor.getLong(cursor.getColumnIndex(columnName));
    }

    public static double getDouble(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? 0 : cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    public static String getString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? null : cursor.getString(cursor.getColumnIndex(columnName));
    }

    public static BigInteger getBigInteger(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? null : new BigInteger(cursor.getString(cursor.getColumnIndex(columnName)));
    }

    public static Date getDate(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? null : new Date(cursor.getLong(cursor.getColumnIndex(columnName)));
    }

    public static boolean getBoolean(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return !cursor.isNull(columnIndex) && cursor.getInt(cursor.getColumnIndex(columnName)) != 0;
    }

    public static ContentValues putBigInteger(ContentValues contentValues, String columnName, BigInteger value) {
        contentValues.put(columnName, value == null ? null : value.toString());
        return contentValues;
    }

    public static ContentValues putBigText(ContentValues contentValues, String columnName, String value) {
        contentValues.put(columnName, value.getBytes());
        return contentValues;
    }

    public static String getBigText(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.isNull(columnIndex) ? null : new String(cursor.getBlob(cursor.getColumnIndex(columnName)));
    }

    public static DbTransaction startTransaction(Context context) {
        return new DbTransaction(getInstance(context).db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String s : ProfileEntry.createStatements()) {
            db.execSQL(s);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        onCreate(db);
    }

    private void dropAllTables(SQLiteDatabase db) {
        for (String s : ProfileEntry.dropStatements()) {
            db.execSQL(s);
        }
    }


    public ProfileEntry insertProfile(ProfileEntry cheque) {
        db.beginTransaction();
        try {
            cheque = ProfileEntry.insertProfile(db, cheque);
            db.setTransactionSuccessful();
            return cheque;
        } finally {
            db.endTransaction();
        }
    }

    public ProfileEntry updateProfile(ProfileEntry cheque) {
        db.beginTransaction();
        try {
            cheque = ProfileEntry.updateProfile(db, cheque);
            db.setTransactionSuccessful();
            return cheque;
        } finally {
            db.endTransaction();
        }
    }


    public static class DbTransaction implements Closeable {

        private final SQLiteDatabase db;

        public DbTransaction(SQLiteDatabase db) {
            this.db = db;
            db.beginTransaction();
        }

        public SQLiteDatabase getDb() {
            return db;
        }

        public void setSuccessful() {
            db.setTransactionSuccessful();
        }

        @Override
        public void close() {
            db.endTransaction();
        }
    }

}
