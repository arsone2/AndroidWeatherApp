package com.example.arsone.weather;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;


public class DataContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.arsone.weather.provider";


    // ----------------------------------------------------

    // city
    private static final String CITY_BASE_PATH = "city";
    private static final String CITY_BASE_PATH_ID = "city/";

    public static final Uri CITY_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CITY_BASE_PATH);
    public static final Uri CITY_CONTENT_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + CITY_BASE_PATH_ID);

    private static HashMap cityProjectionMap;

    // MIME types
    public static final String CITY_CONTENT_TYPE = "vnd.android.cursor.dir/com.example.arsone.cities";
    public static final String CITY_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.example.arsone.cities";

    // ----------------------------------------------------

    // weather
    private static final String WEATHER_BASE_PATH = "weather";
    private static final String WEATHER_BASE_PATH_ID = "weather/";

    public static final Uri WEATHER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + WEATHER_BASE_PATH);
    public static final Uri WEATHER_CONTENT_ID_URI = Uri.parse("content://" + AUTHORITY + "/" + WEATHER_BASE_PATH_ID);

    private static HashMap weatherProjectionMap;

    // MIME types
    public static final String WEATHER_CONTENT_TYPE = "vnd.android.cursor.dir/com.example.arsone.weather";
    public static final String WEATHER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.example.arsone.weather";

    // ----------------------------------------------------

    // requested operations
    private static final int URI_CITIES = 1;
    private static final int URI_CITY_ID = 2;
    private static final int URI_WEATHERS = 3;
    private static final int URI_WEATHER_ID = 4;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, CITY_BASE_PATH, URI_CITIES);
        uriMatcher.addURI(AUTHORITY, CITY_BASE_PATH + "/#", URI_CITY_ID);
        uriMatcher.addURI(AUTHORITY, WEATHER_BASE_PATH, URI_WEATHERS);
        uriMatcher.addURI(AUTHORITY, WEATHER_BASE_PATH + "/#", URI_WEATHER_ID);
    }

    static {

        cityProjectionMap = new HashMap();

        for (int i = 0; i < DataContract.CityEntry.DEFAULT_PROJECTION.length; i++) {

            cityProjectionMap.put(DataContract.CityEntry.DEFAULT_PROJECTION[i],
                    DataContract.CityEntry.DEFAULT_PROJECTION[i]);
        }

        weatherProjectionMap = new HashMap();

        for (int i = 0; i < DataContract.WeatherEntry.DEFAULT_PROJECTION.length; i++) {

            weatherProjectionMap.put(DataContract.WeatherEntry.DEFAULT_PROJECTION[i],
                    DataContract.WeatherEntry.DEFAULT_PROJECTION[i]);
        }
    }

    private final String LOG_TAG = "AAAAA";

    private DBHelper dbHelper;

    private SQLiteDatabase database;


    @Override
    public boolean onCreate() {

     //   Log.d(LOG_TAG, "DataContentProvider: onCreate()");

        dbHelper = new DBHelper(getContext());
        // database = dbHelper.getWritableDatabase();

/*        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }*/

        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

      //  Log.d(LOG_TAG, "query: " + uri.toString());

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String orderBy = null;

        // database = dbHelper.getReadableDatabase();

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                qb.setTables(DataContract.CityEntry.TABLE_NAME);
                qb.setProjectionMap(cityProjectionMap);
                orderBy = DataContract.CityEntry.DEFAULT_SORT_ORDER;

                break;

            case URI_CITY_ID:

                qb.setTables(DataContract.CityEntry.TABLE_NAME);
                qb.setProjectionMap(cityProjectionMap);
                qb.appendWhere(DataContract.CityEntry._ID + "="
                        + uri.getPathSegments().get(DataContract.CityEntry.ID_PATH_POSITION));
                orderBy = DataContract.CityEntry.DEFAULT_SORT_ORDER;

                break;

            case URI_WEATHERS:

                qb.setTables(DataContract.WeatherEntry.TABLE_NAME);
                qb.setProjectionMap(weatherProjectionMap);
                orderBy = DataContract.WeatherEntry.DEFAULT_SORT_ORDER;

                break;

            case URI_WEATHER_ID:

                qb.setTables(DataContract.WeatherEntry.TABLE_NAME);
                qb.setProjectionMap(weatherProjectionMap);
                qb.appendWhere(DataContract.WeatherEntry._ID + "="
                        + uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION));
                orderBy = DataContract.WeatherEntry.DEFAULT_SORT_ORDER;

                break;

            default:
                throw new IllegalArgumentException("query: unknown URI " + uri);
        }

        database = dbHelper.getReadableDatabase();

        Cursor cursor = qb.query(database,
                projection,    // the columns to return from the query
                selection,     // the columns in where clause
                selectionArgs, // the values for where clause
                null,          // don`t group the rows
                null,          // don`t filter by row groups
                orderBy);      // sort order

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;

/*        return database.query(DataContract.CityEntry.TABLE_NAME,
                DataContract.CityEntry.CITY_ALL_COLUMNS, // columns list
                selection, null, null, null,
                DataContract.CityEntry._ID + " DESC" // sort by _id DESC
        );*/
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        Log.d(LOG_TAG, "getType, " + uri.toString());

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:
                return CITY_CONTENT_TYPE;

            case URI_CITY_ID:
                return CITY_CONTENT_ITEM_TYPE;

            case URI_WEATHERS:
                return WEATHER_CONTENT_TYPE;

            case URI_WEATHER_ID:
                return WEATHER_CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // can insert only in tables
        if (uriMatcher.match(uri) != URI_CITIES && uriMatcher.match(uri) != URI_WEATHERS) {
            throw new IllegalArgumentException("insert: wrong URI " + uri);
        }

        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }

        long id = -1;

        Uri rowUri = Uri.EMPTY;

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                id = database.insert(DataContract.CityEntry.TABLE_NAME, null, values);

                if (id > 0) {
                    rowUri = ContentUris.withAppendedId(CITY_CONTENT_ID_URI, id);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }

                // return Uri.parse(CITY_BASE_PATH + "/" + id);

                break;

            case URI_WEATHERS:

                id = database.insert(DataContract.WeatherEntry.TABLE_NAME, null, values);

                if (id > 0) {
                    rowUri = ContentUris.withAppendedId(WEATHER_CONTENT_ID_URI, id);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }

                break;
        }

        return rowUri;
/*
        long id = database.insert(DataContract.CityEntry.TABLE_NAME, null, values);

        getContext().getContentResolver().notifyChange(uri, null); // add

        return Uri.parse(CITY_BASE_PATH + "/" + id);
*/

    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }

        int count = 0;
        String id = "";

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                count = database.delete(DataContract.CityEntry.TABLE_NAME, selection, selectionArgs);

                break;

            case URI_CITY_ID:

                id = uri.getPathSegments().get(DataContract.CityEntry.ID_PATH_POSITION);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + id;
                }

                count = database.delete(DataContract.CityEntry.TABLE_NAME, selection, selectionArgs);

                break;

            case URI_WEATHERS:

                count = database.delete(DataContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);

                break;

            case URI_WEATHER_ID:

                id = uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry._ID + "=" + id;
                }

                count = database.delete(DataContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);

                break;

            default:
                throw new IllegalArgumentException("update: unknown URI " + uri);
        }

        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return count;

/*        int rowsDeleted = database.delete(DataContract.CityEntry.TABLE_NAME, selection, selectionArgs);
        //getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;*/
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }

        int count = 0;
        String id = "";

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                count = database.update(DataContract.CityEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            case URI_CITY_ID:

                id = uri.getPathSegments().get(DataContract.CityEntry.ID_PATH_POSITION);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + id;
                }

                count = database.update(DataContract.CityEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            case URI_WEATHERS:

                count = database.update(DataContract.WeatherEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            case URI_WEATHER_ID:

                id = uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry._ID + "=" + id;
                }

                count = database.update(DataContract.WeatherEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            default:
                throw new IllegalArgumentException("update: unknown URI " + uri);
        }

        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return count;

      /*  return database.update(DataContract.CityEntry.TABLE_NAME, values, selection, selectionArgs);*/
    }

    // http://www.programcreek.com/java-api-examples/index.php?api=android.content.ContentProviderResult
    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {

        // return super.applyBatch(operations);

            database = dbHelper.getWritableDatabase();

            database.beginTransaction();

        try {
            final int numOperations = operations.size();

            final ContentProviderResult[] results = new ContentProviderResult[numOperations];

            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }

            /// ContentProviderResult[] results = super.applyBatch(operations);

            // http://www.grokkingandroid.com/better-performance-with-contentprovideroperation/
            // https://stackoverflow.com/questions/4655291/what-are-the-semantics-of-withvaluebackreference
            // https://www.codota.com/android/methods/android.content.ContentProvider/applyBatch
            // http://www.java2s.com/Open-Source/Android_Free_Code/PhoneGap/JQuery/org_apache_cordova_contactsContactAccessorSdk5_java.htm

            database.setTransactionSuccessful();
            return results;

        } catch (OperationApplicationException e) {
            Log.d(LOG_TAG, "OperationApplicationException: " + e.getMessage());
        } finally {
            database.endTransaction();
        }
        return null;
    }


    // ----------------------------------------------
    // DBHelper class
    private class DBHelper extends SQLiteOpenHelper {

        // constructor
        public DBHelper(Context context) {
            // super(context, name, factory, version);
            super(context, DataContract.DATABASE_NAME, null, DataContract.DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL("CREATE TABLE " + DataContract.CityEntry.TABLE_NAME + " ("
                    + DataContract.CityEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // foreign key
                    + DataContract.CityEntry.COLUMN_ENTERED_CITY + " TEXT DEFAULT '', "
                    + DataContract.CityEntry.COLUMN_RETURNED_CITY + " TEXT DEFAULT '',"
                    + DataContract.CityEntry.COLUMN_COUNTRY_CODE + " TEXT DEFAULT '',"
                    + DataContract.CityEntry.COLUMN_LATITUDE + " DOUBLE,"
                    + DataContract.CityEntry.COLUMN_LONGITUDE + " DOUBLE,"
                    + DataContract.CityEntry.COLUMN_CITY_ID + " INTEGER"
                    + ");");

            db.execSQL("CREATE TABLE " + DataContract.WeatherEntry.TABLE_NAME + " ("
                    + DataContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," // inner data ID
                    + DataContract.WeatherEntry.COLUMN_CITY_ID_FK + " INTEGER," // foreign key
                    + DataContract.WeatherEntry.COLUMN_TIMESTAMP + " INTEGER,"
                    + DataContract.WeatherEntry.COLUMN_MORNING_TEMP + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_DAY_TEMP + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_MIN_TEMP + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_MAX_TEMP + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_EVENING_TEMP + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_NIGHT_TEMP + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_HUMIDITY + " INTEGER,"
                    + DataContract.WeatherEntry.COLUMN_PRESSURE + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_SPEED + " DOUBLE,"
                    + DataContract.WeatherEntry.COLUMN_DIRECTION + " INTEGER,"
                    + DataContract.WeatherEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '',"
                    + DataContract.WeatherEntry.COLUMN_ICON_NAME + " TEXT DEFAULT '',"
                    + "FOREIGN KEY(" + DataContract.WeatherEntry.COLUMN_CITY_ID_FK + ") REFERENCES "
                    + DataContract.CityEntry.TABLE_NAME + "(" + DataContract.CityEntry._ID + ")"
                    + ");");

            Log.d(LOG_TAG, "onCreate(): tables CREATED");

            // add initial data
            ContentValues cv = new ContentValues();

            // test data
            cv.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, "Москва");
            db.insert(DataContract.CityEntry.TABLE_NAME, null, cv);

            cv.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, "Санкт-Петербург");
            db.insert(DataContract.CityEntry.TABLE_NAME, null, cv);

/*
            cv.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, "Москва");
            db.insert(DataContract.CityEntry.TABLE_NAME, null, cv);
*/
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + DataContract.CityEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataContract.WeatherEntry.TABLE_NAME);

            onCreate(db);

            Log.d(LOG_TAG, "onUpgrade(): tables DROPPED");
        }


        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + DataContract.CityEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataContract.WeatherEntry.TABLE_NAME);

            onCreate(db);

            Log.d(LOG_TAG, "onDowngrade(): tables DROPPED");
        }
    }
}