package in.yagnyam.myid.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import in.yagnyam.myid.data.DbHelper;
import in.yagnyam.myid.utils.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "profileId")
@NoArgsConstructor
public class ProfileEntry implements BaseColumns {

    private static final String TAG = "ProfileEntry";

    private static final String TABLE_NAME = "ProfileEntry";
    private static final String COLUMN_NAME_PROFILE_ID = BaseColumns._ID;
    private static final String COLUMN_NAME_PROFILE_NAME = "profileName";
    private static final String COLUMN_NAME_BSN = "bsn";
    private static final String COLUMN_NAME_NAME = "name";
    private static final String COLUMN_NAME_DIGID = "digid";
    private static final String COLUMN_NAME_PATH = "path";
    private static final String COLUMN_NAME_DOB = "dob";
    private static final String COLUMN_NAME_BLOOD_GROUP = "bloodGroup";
    private static final String COLUMN_NAME_REFUGEE_STATUS = "refugeeStatus";
    private static final String COLUMN_NAME_DEAF = "deaf";
    private static final String COLUMN_NAME_DUMB = "dumb";

    private static final String[] COLUMN_NAMES = {
            COLUMN_NAME_PROFILE_ID,
            COLUMN_NAME_PROFILE_NAME,
            COLUMN_NAME_BSN,
            COLUMN_NAME_NAME,
            COLUMN_NAME_DIGID,
            COLUMN_NAME_PATH,
            COLUMN_NAME_DOB,
            COLUMN_NAME_BLOOD_GROUP,
            COLUMN_NAME_REFUGEE_STATUS,
            COLUMN_NAME_DEAF,
            COLUMN_NAME_DUMB
    };

    private String profileId;
    private String profileName;
    private String bsn;
    private String name;
    private String digid;
    private String path;
    private Date dob;
    private String bloodGroup;
    private String refugeeStatus;
    private String deaf;
    private String dumb;

    private static String addIfValid(String existing, String name, String value) {
        if (StringUtils.isEmpty(value)) {
            return existing;
        } else if (!StringUtils.isEmpty(existing)) {
            return existing + "\n" + name + ": " + value;
        } else {
            return name + ": " + value;
        }
    }

    private static Map<String, Object> addIfValid(Map<String, Object> existing, String name, String value) {
        if (!StringUtils.isEmpty(value)) {
            existing.put(name, value);
        }
        return existing;
    }

    private static String addIfValid(String existing, String name, Date value) {
        return value == null ? existing : addIfValid(existing, name, new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault()).format(value));
    }

    private static Map<String, Object> addIfValid(Map<String, Object> existing, String name, Date value) {
        return value == null ? existing : addIfValid(existing, name, new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault()).format(value));
    }

    public Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims = addIfValid(claims, "bsn", bsn);
        claims = addIfValid(claims, "dob", dob);
        claims = addIfValid(claims, "name", name);
        claims = addIfValid(claims, "bloodGroup", bloodGroup);
        claims = addIfValid(claims, "refugeeStatus", refugeeStatus);
        claims = addIfValid(claims, "deaf", deaf);
        claims = addIfValid(claims, "dumb", dumb);
        return claims;
    }

    public String getDescription() {
        String description = "";
        description = addIfValid(description, "bsn", bsn);
        description = addIfValid(description, "dob", dob);
        description = addIfValid(description, "name", name);
        description = addIfValid(description, "bloodGroup", bloodGroup);
        description = addIfValid(description, "refugeeStatus", refugeeStatus);
        description = addIfValid(description, "deaf", deaf);
        description = addIfValid(description, "dumb", dumb);
        if (StringUtils.isEmpty(description)) {
            description = "None";
        }
        return description;
    }

    private ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_BSN, bsn);
        values.put(COLUMN_NAME_PROFILE_ID, profileId);
        values.put(COLUMN_NAME_PROFILE_NAME, profileName);
        values.put(COLUMN_NAME_NAME, name);
        values.put(COLUMN_NAME_DIGID, digid);
        values.put(COLUMN_NAME_PATH, path);
        if (dob != null) {
            values.put(COLUMN_NAME_DOB, dob.getTime());
        }
        values.put(COLUMN_NAME_BLOOD_GROUP, bloodGroup);
        values.put(COLUMN_NAME_REFUGEE_STATUS, refugeeStatus);
        values.put(COLUMN_NAME_DEAF, deaf);
        values.put(COLUMN_NAME_DUMB, dumb);
        return values;
    }


    private static ProfileEntry create(Cursor cursor) {
        ProfileEntry profileEntry = new ProfileEntry();
        profileEntry.profileId = DbHelper.getString(cursor, COLUMN_NAME_PROFILE_ID);
        profileEntry.profileName = DbHelper.getString(cursor, COLUMN_NAME_PROFILE_NAME);
        profileEntry.bsn = DbHelper.getString(cursor, COLUMN_NAME_BSN);
        profileEntry.name = DbHelper.getString(cursor, COLUMN_NAME_NAME);
        profileEntry.digid = DbHelper.getString(cursor, COLUMN_NAME_DIGID);
        profileEntry.path = DbHelper.getString(cursor, COLUMN_NAME_PATH);
        profileEntry.dob = DbHelper.getDate(cursor, COLUMN_NAME_DOB);
        profileEntry.bloodGroup = DbHelper.getString(cursor, COLUMN_NAME_BLOOD_GROUP);
        profileEntry.refugeeStatus = DbHelper.getString(cursor, COLUMN_NAME_REFUGEE_STATUS);
        profileEntry.deaf = DbHelper.getString(cursor, COLUMN_NAME_DEAF);
        profileEntry.dumb = DbHelper.getString(cursor, COLUMN_NAME_DUMB);
        return profileEntry;
    }

    public static List<String> createStatements() {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_NAME_PROFILE_ID + DbHelper.TEXT_TYPE + " PRIMARY KEY" + DbHelper.COMMA_SEP +
                COLUMN_NAME_PROFILE_NAME + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_BSN + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_NAME + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_DIGID + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_PATH + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_DOB + DbHelper.DATE_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_BLOOD_GROUP + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_REFUGEE_STATUS + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_DEAF + DbHelper.TEXT_TYPE + DbHelper.COMMA_SEP +
                COLUMN_NAME_DUMB + DbHelper.TEXT_TYPE +
                " )";
        return Collections.singletonList(createTable);
    }

    public static List<String> dropStatements() {
        String dropTable = "DROP TABLE IF EXISTS " + TABLE_NAME;
        return Collections.singletonList(dropTable);
    }

    public static ProfileEntry fetchUser(SQLiteDatabase db) {
        Log.d(TAG, "fetchUser()");
        Cursor cursor = db.query(TABLE_NAME, COLUMN_NAMES, null, null, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            return create(cursor);
        }
        return null;
    }


    public static ProfileEntry fetchProfile(SQLiteDatabase db, String profileId) {
        Log.d(TAG, "fetchProfile(" + profileId + ")");
        String selection = COLUMN_NAME_PROFILE_ID + " = ?";
        String[] selectionArgs = new String[]{profileId};
        Cursor cursor = db.query(TABLE_NAME, COLUMN_NAMES, selection, selectionArgs, null, null, null);
        if (!cursor.moveToNext()) {
            Log.e(TAG, "No profile found with ID: " + profileId);
            throw new IllegalArgumentException("No profile found for profile ID: " + profileId);
        }
        ProfileEntry profile = create(cursor);
        Log.d(TAG, "fetchProfile(" + profileId + ") => " + profile);
        return profile;
    }

    public static ProfileEntry updateProfile(SQLiteDatabase db, ProfileEntry profile) {
        Log.d(TAG, "updateProfile(" + profile + ")");
        String selection = COLUMN_NAME_PROFILE_ID + " = ?";
        String[] selectionArgs = new String[]{profile.profileId};
        db.update(TABLE_NAME, profile.toContentValues(), selection, selectionArgs);
        return profile;
    }

    public static ProfileEntry insertProfile(SQLiteDatabase db, ProfileEntry profile) {
        Log.d(TAG, "insertProfile(" + profile + ")");
        db.insert(TABLE_NAME, null, profile.toContentValues());
        return profile;
    }

    public static List<ProfileEntry> fetchProfiles(SQLiteDatabase db) {
        List<ProfileEntry> results = new ArrayList<>();
        Cursor cursor = db.query(TABLE_NAME, COLUMN_NAMES, null, null, null, null, null);
        while (cursor.moveToNext()) {
            results.add(create(cursor));
        }
        Log.d(TAG, "fetchProfiles() => " + results);
        return results;
    }
}
