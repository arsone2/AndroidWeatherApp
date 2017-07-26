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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;


public class DataContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.arsone.weather.provider";

    private static final String CITY_PATH = "city";
    private static final String WEATHER_PATH = "weather";
    private static final String CITY_WEATHER_PATH = "current";
    private static final String SETTINGS_PATH = "settings";

    // requested operations
    private static final int URI_CITIES = 1;
    private static final int URI_CITY_ID = 2;
    private static final int URI_WEATHERS = 3;
    private static final int URI_WEATHER_ID = 4;
    private static final int URI_DATE = 5;
    private static final int URI_SETTINGS = 6;


    public static final Uri CITY_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CITY_PATH);
    public static final Uri WEATHER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + WEATHER_PATH);
    public static final Uri CITY_WEATHER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + CITY_WEATHER_PATH);
    public static final Uri SETTINGS_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + SETTINGS_PATH);

    // ----------------------------------------------------

    private static final String CITY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CITY_PATH;
    private static final String CITY_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + CITY_PATH;
    private static final String WEATHER_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + WEATHER_PATH;
    private static final String WEATHER_CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + WEATHER_PATH;
    private static final String CITY_WEATHER_CONTENT_ITEM_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CITY_WEATHER_PATH;
    private static final String SETTINGS_CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + SETTINGS_PATH;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        // get all cities
        uriMatcher.addURI(AUTHORITY, CITY_PATH, URI_CITIES);

        // get city by "_id"
        uriMatcher.addURI(AUTHORITY, CITY_PATH + "/#", URI_CITY_ID);

        // get all weather data
        uriMatcher.addURI(AUTHORITY, WEATHER_PATH, URI_WEATHERS);

        // get weather for certain city "city_id"
        uriMatcher.addURI(AUTHORITY, WEATHER_PATH + "/#", URI_WEATHER_ID);

        // get all cities and it`s weather for date "yyyy-MM-dd"
        uriMatcher.addURI(AUTHORITY, CITY_WEATHER_PATH + "/*", URI_DATE);

        // get all settings data
        uriMatcher.addURI(AUTHORITY, SETTINGS_PATH, URI_SETTINGS);
    }

    //   static {

/*        cityProjectionMap = new HashMap();

        for (int i = 0; i < DataContract.CityEntry.DEFAULT_PROJECTION.length; i++) {

            cityProjectionMap.put(DataContract.CityEntry.DEFAULT_PROJECTION[i],
                    DataContract.CityEntry.DEFAULT_PROJECTION[i]);
        }

        weatherProjectionMap = new HashMap();

        for (int i = 0; i < DataContract.WeatherEntry.DEFAULT_PROJECTION.length; i++) {

            weatherProjectionMap.put(DataContract.WeatherEntry.DEFAULT_PROJECTION[i],
                    DataContract.WeatherEntry.DEFAULT_PROJECTION[i]);
        }*/
    //   }


    private DBHelper dbHelper;

    private SQLiteDatabase database;


    @Override
    public boolean onCreate() {

        dbHelper = new DBHelper(getContext());

        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        //      database = dbHelper.getReadableDatabase();

        //     SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String table;
        String id;

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                //        Log.d("AAAAA", "URI_CITIES: " + uri);

                // if sort order don`t specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = DataContract.CityEntry._ID + " DESC";
                }

                table = DataContract.CityEntry.TABLE_NAME;

                //  orderBy = DataContract.CityEntry.DEFAULT_SORT_ORDER;

                // Log.d("AAAAA", "case URI_CITIES");

                break;

            case URI_CITY_ID:

                id = uri.getLastPathSegment();

                //      Log.d("AAAAA", "URI_CITY_ID: " + uri + " id =" + id);

                /// Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);

                table = DataContract.CityEntry.TABLE_NAME;

                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + id;
                }

                //     qb.setTables(DataContract.CityEntry.TABLE_NAME);

                //       qb.appendWhere(selection);

/*                qb.setTables(DataContract.CityEntry.TABLE_NAME);
                qb.setProjectionMap(cityProjectionMap);
                qb.appendWhere(DataContract.CityEntry._ID + "="
                        + uri.getPathSegments().get(DataContract.CityEntry.ID_PATH_POSITION));
                orderBy = DataContract.CityEntry.DEFAULT_SORT_ORDER;*/

                break;

            case URI_WEATHERS:

                //       Log.d("AAAAA", "URI_WEATHERS: " + uri);

                // if sort order don`t specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = DataContract.WeatherEntry._ID + " ASC";
                }

                table = DataContract.WeatherEntry.TABLE_NAME;

                ///       Log.d("AAAAA", "URI_WEATHERS: " + uri);

/*                qb.setTables(DataContract.WeatherEntry.TABLE_NAME);
                qb.setProjectionMap(weatherProjectionMap);
                orderBy = DataContract.WeatherEntry.DEFAULT_SORT_ORDER;*/

                break;

            case URI_WEATHER_ID:

                id = uri.getLastPathSegment();

                //         Log.d("AAAAA", "URI_WEATHER_ID: " + uri + ", id = " + id);

                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=" + id;
                }


/*                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry.COLUMN_SERVER_CITY_ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry.COLUMN_SERVER_CITY_ID + "=" + id;
                }*/

                table = DataContract.WeatherEntry.TABLE_NAME;

/*                qb.setTables(DataContract.WeatherEntry.TABLE_NAME);
                qb.setProjectionMap(weatherProjectionMap);
                qb.appendWhere(DataContract.WeatherEntry._ID + "="
                        + uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION));
                orderBy = DataContract.WeatherEntry.DEFAULT_SORT_ORDER;*/

                break;


            case URI_DATE:

                database = dbHelper.getWritableDatabase();

                String date = uri.getLastPathSegment();

                //     Log.d("AAAAA", "URI_DATE: " + uri + " date = " + date);

                /// Log.d(LOG_TAG, "URI_CONTACTS_ID, " + id);

/*                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + id;
                }*/

                //      Log.d("AAAAA", "URI_DATE: " + uri);

                //  qb.setTables(DataContract.CityEntry.TABLE_NAME);
                // orderBy = DataContract.CityEntry.DEFAULT_SORT_ORDER;
                //    orderBy = DataContract.WeatherEntry.COLUMN_CITY_ID_FK;
                //    String groupBy = DataContract.WeatherEntry.COLUMN_CITY_ID_FK;

/*                String query = "SELECT c.entered_city, c.returned_city, c._id, w.temp_day, w.description, w.icon_name"
                        + " FROM weather w INNER JOIN cities c ON c._id = w.city_id"
                        + " WHERE strftime('%Y-%m-%d', w.insert_timestamp) = '" + date + "'"  //" '2017-07-04'
                        + " OR c._id > 0"
                        /// + " GROUP BY w.city_id"
                        + " GROUP BY c._id"
                        + " ORDER BY c._id DESC";*/

                // if sort order don`t specified then set default sort order
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = " ORDER BY c." + DataContract.CityEntry._ID + " DESC";
                }

                ///     Log.d("AAAAA", "URI_DATE : sortOrder = " + sortOrder);

                String query = "SELECT "
                        + "c.entered_city, c.returned_city, c._id, c.update_timestamp, c.latitude, c.longitude,"
                        + "w.temp_day, w.description, w.icon_name, w.insert_timestamp"
                        + " FROM cities c LEFT OUTER JOIN weather w ON c._id = w.city_id"
                        + " AND strftime('%Y-%m-%d',datetime(dt, 'unixepoch', 'localtime')) = '" + date + "'"  //" '2017-07-04'
                        ///+ " " + "ORDER BY c." + sortOrder + " ASC"
                        + " " + sortOrder
                        + ";";

                ///    Log.d("AAAAA", "case URI_DATE: " + uri.getPathSegments().get(DataContract.DATE_PATH_POSITION));

                /// Cursor cursor = database.rawQuery(query, new String[]{date});// selectionArgs); // new String[]{ "2017-07-04" });
                Cursor cursor = database.rawQuery(query, null);// selectionArgs); // new String[]{ "2017-07-04" });

                ///       Log.d("AAAAA", "URI_DATE: " + uri + " cursor.getCount() = " + cursor.getCount());

                if (cursor != null) {
                    cursor.setNotificationUri(getContext().getContentResolver(), uri);
                }

                return cursor;


            case URI_SETTINGS:

                ///       Log.d("AAAAA", "URI_SETTINGS: " + uri);

/*                // if sort order don`t specified
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = DataContract.SettingsEntry._ID + " ASC";
                }*/

                table = DataContract.SettingsEntry.TABLE_NAME;

                break;

            default:
                throw new IllegalArgumentException("query: unknown URI " + uri);
        }

        database = dbHelper.getReadableDatabase();

        Cursor cursor = database.query(table,
                projection,    // the columns to return from the query
                selection,     // the columns in where clause
                selectionArgs, // the values for where clause
                null,          // don`t group the rows
                null,          // don`t filter by row groups
                sortOrder);      // sort order

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

//        getContext().getContentResolver().notifyChange(uri, null);

        // просим ContentResolver уведомлять этот курсор
        // об изменениях данных в CONTACT_CONTENT_URI
        //  cursor.setNotificationUri(getContext().getContentResolver(), CONTACT_CONTENT_URI);


        return cursor;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:
                return CITY_CONTENT_TYPE;

            case URI_CITY_ID:
                return CITY_CONTENT_ITEM_TYPE;

            case URI_WEATHERS:
                return WEATHER_CONTENT_TYPE;

            case URI_WEATHER_ID:
                return WEATHER_CONTENT_ITEM_TYPE;

            case URI_DATE:
                return CITY_WEATHER_CONTENT_ITEM_TYPE;

            case URI_SETTINGS:
                return SETTINGS_CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("getType: unknown URI " + uri);
        }
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        // can insert only in tables
/*        if (uriMatcher.match(uri) != URI_CITIES && uriMatcher.match(uri) != URI_WEATHERS) {
            throw new IllegalArgumentException("insert: wrong URI " + uri);
        }*/

        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }

        long rowID;

        Uri rowUri = Uri.EMPTY;

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                ///           Log.d("AAAAA", "insert: URI_CITIES: " + uri);

                rowID = database.insert(DataContract.CityEntry.TABLE_NAME, null, values);

                if (rowID > 0) {
                    rowUri = ContentUris.withAppendedId(CITY_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                break;

            case URI_WEATHERS:

                //   Log.d("AAAAA", "insert: URI_WEATHERS: " + uri);

                rowID = database.insert(DataContract.WeatherEntry.TABLE_NAME, null, values);

                if (rowID > 0) {
                    rowUri = ContentUris.withAppendedId(WEATHER_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(rowUri, null);
                }

                break;

            default:
                throw new IllegalArgumentException("insert: unknown URI " + uri);
        }

        return rowUri;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }

        int count = 0;
        String city_id = "";

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                count = database.delete(DataContract.CityEntry.TABLE_NAME, selection, selectionArgs);

                break;

            case URI_CITY_ID:

                city_id = uri.getLastPathSegment();
                /// id = uri.getPathSegments().get(DataContract.CityEntry.ID_PATH_POSITION);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + city_id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + city_id;
                }

                // id = uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION);


                //      Log.d("AAAAA", "delete: URI_CITY_ID: " + uri + " city_id =" + city_id);

/*                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry._ID + "=" + id;
                }*/

                count = database.delete(DataContract.CityEntry.TABLE_NAME, selection, selectionArgs);

                break;

            case URI_WEATHERS:

                count = database.delete(DataContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);

                break;

            case URI_WEATHER_ID:

                /// id = uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION);
                city_id = uri.getLastPathSegment();

                //        Log.d("AAAAA", "delete: URI_WEATHER_ID: " + uri + " city_id =" + city_id);

/*
                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry._ID + "=" + id;
                }
*/

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=" + city_id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=" + city_id;
                }


                count = database.delete(DataContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);

                break;

            default:
                throw new IllegalArgumentException("update: unknown URI " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        database = dbHelper.getWritableDatabase();

        if (!database.isReadOnly()) {
            // Enable foreign key constraints
            database.execSQL("PRAGMA foreign_keys=ON;");
        }

        int count;
        String id;

        switch (uriMatcher.match(uri)) {

            case URI_CITIES:

                count = database.update(DataContract.CityEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            case URI_CITY_ID:


                id = uri.getLastPathSegment();

                //        Log.d("AAAAA", "update: URI_CITY_ID: " + uri + " id =" + id);

                // добавляем ID к условию выборки
                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + id;
                }

                count = database.update(DataContract.CityEntry.TABLE_NAME, values, selection, selectionArgs);

/*                id = uri.getPathSegments().get(DataContract.CityEntry.ID_PATH_POSITION);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.CityEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.CityEntry._ID + "=" + id;
                }

                count = database.update(DataContract.CityEntry.TABLE_NAME, values, selection, selectionArgs);*/

                break;

            case URI_WEATHERS:

                count = database.update(DataContract.WeatherEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            case URI_WEATHER_ID:

                // id = uri.getPathSegments().get(DataContract.WeatherEntry.ID_PATH_POSITION);
                id = uri.getLastPathSegment();

                //      Log.d("AAAAA", "update: URI_WEATHER_ID: " + uri + " id =" + id);

                if (TextUtils.isEmpty(selection)) {
                    selection = DataContract.WeatherEntry._ID + "=" + id;
                } else {
                    selection = selection + " AND " + DataContract.WeatherEntry._ID + "=" + id;
                }

                count = database.update(DataContract.WeatherEntry.TABLE_NAME, values, selection, selectionArgs);

                break;


            case URI_SETTINGS:

                count = database.update(DataContract.SettingsEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            default:
                throw new IllegalArgumentException("update: unknown URI " + uri);
        }

        if (count > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }


    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {

        database = dbHelper.getWritableDatabase();

        database.beginTransaction();

        try {
            final int numOperations = operations.size();

            final ContentProviderResult[] results = new ContentProviderResult[numOperations];

            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }

            database.setTransactionSuccessful();

            return results;

        } catch (OperationApplicationException e) {
            Log.d("AAAAA", "OperationApplicationException: " + e.getMessage());
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
                    + DataContract.CityEntry.COLUMN_ENTERED_CITY + " TEXT,"
                    + DataContract.CityEntry.COLUMN_RETURNED_CITY + " TEXT,"
                    + DataContract.CityEntry.COLUMN_COUNTRY_CODE + " TEXT,"
                    + DataContract.CityEntry.COLUMN_LATITUDE + " DOUBLE,"
                    + DataContract.CityEntry.COLUMN_LONGITUDE + " DOUBLE,"
                    + DataContract.CityEntry.COLUMN_SERVER_CITY_ID + " INTEGER,"
                    + DataContract.CityEntry.COLUMN_UPDATE_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
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
                    + DataContract.WeatherEntry.COLUMN_DESCRIPTION + " TEXT,"
                    + DataContract.WeatherEntry.COLUMN_ICON_NAME + " TEXT,"
                    + DataContract.WeatherEntry.COLUMN_INSERT_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY(" + DataContract.WeatherEntry.COLUMN_CITY_ID_FK + ") REFERENCES "
                    + DataContract.CityEntry.TABLE_NAME + "(" + DataContract.CityEntry._ID + ")"
                    + ");");

            db.execSQL("CREATE TABLE " + DataContract.SettingsEntry.TABLE_NAME + " ("
                    + DataContract.SettingsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"    //  (int/INTEGER)
                    + DataContract.SettingsEntry.COLUMN_UNITS_FORMAT + " INTEGER DEFAULT 0,"    //  (int/INTEGER)
                    + DataContract.SettingsEntry.COLUMN_SORT_CITIES + " INTEGER DEFAULT 0,"     //  (int/INTEGER)
                    + DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE + " INTEGER DEFAULT 0,"    //  (int/INTEGER)
                    + DataContract.SettingsEntry.COLUMN_SEND_NOTIFY + " INTEGER DEFAULT 0,"     //  (int/INTEGER)
                    + DataContract.SettingsEntry.COLUMN_CAMERA_LATITUDE + " DOUBLE DEFAULT 0,"  // (double/DOUBLE)
                    + DataContract.SettingsEntry.COLUMN_CAMERA_LONGITUDE + " DOUBLE DEFAULT 0," // (double/DOUBLE)
                    + DataContract.SettingsEntry.COLUMN_CAMERA_BEARING + " DOUBLE DEFAULT 0,"   // (double/DOUBLE)
                    + DataContract.SettingsEntry.COLUMN_CAMERA_TILT + " DOUBLE DEFAULT 0,"      // (double/DOUBLE)
                    + DataContract.SettingsEntry.COLUMN_CAMERA_ZOOM + " DOUBLE DEFAULT 0"       // (double/DOUBLE)
                    + ");");



            Log.d("AAAAA", "onCreate(): tables CREATED");

            // add default settings - DO NOT DELETE !!
            ContentValues cvSettings = new ContentValues();

            cvSettings.put(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT, 0);
            cvSettings.put(DataContract.SettingsEntry.COLUMN_SORT_CITIES, 0);
            cvSettings.put(DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE, 0);
            cvSettings.put(DataContract.SettingsEntry.COLUMN_SEND_NOTIFY, 0);

            db.insert(DataContract.SettingsEntry.TABLE_NAME, null, cvSettings);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + DataContract.CityEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataContract.WeatherEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataContract.SettingsEntry.TABLE_NAME);

            onCreate(db);

            Log.d("AAAAA", "onUpgrade(): tables DROPPED");
        }


        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + DataContract.CityEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataContract.WeatherEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + DataContract.SettingsEntry.TABLE_NAME);

            onCreate(db);

            Log.d("AAAAA", "onDowngrade(): tables DROPPED");
        }
    }
}